package app.notibatch.probe

import android.app.Notification
import android.content.ComponentName
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Sonda de Fase 0 (ver docs/04-fase0-viabilidad.md). Pospone las notificaciones del paquete
 * configurado hasta la hora configurada y registra, SIN CONTENIDO, lo necesario para responder
 * las preguntas Q1..Q6. No es el listener definitivo.
 */
class ProbeListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        instance = this
        ProbeLog.event("listener_connected", "sdk" to android.os.Build.VERSION.SDK_INT)
    }

    override fun onListenerDisconnected() {
        instance = null
        ProbeLog.event("listener_disconnected")
        requestRebind(ComponentName(this, ProbeListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName != ProbeConfig.pkg(this)) return

        val id = ProbeLog.id(this, sbn.key)
        val n = sbn.notification
        val now = System.currentTimeMillis()
        val until = ProbeConfig.until(this)
        val prefs = ProbeLog.prefs(this)
        val plannedRelease = prefs.getLong("snz.$id", 0L)

        // Q1/Q2/H3: ¿es un regreso de snooze o una notificación nueva/actualizada?
        val relation = when {
            plannedRelease == 0L -> "nueva"
            now >= plannedRelease -> "regreso_de_snooze"
            else -> "actualizacion_antes_de_tiempo"
        }

        val ongoing = n.flags and Notification.FLAG_ONGOING_EVENT != 0
        val fgs = n.flags and Notification.FLAG_FOREGROUND_SERVICE != 0
        ProbeLog.event(
            "posted",
            "id" to id, "pkg" to sbn.packageName, "relation" to relation,
            "planned" to plannedRelease,
            "delta_ms" to if (plannedRelease != 0L) now - plannedRelease else null,
            "postTime" to sbn.postTime,
            "category" to n.category, "flags" to n.flags,
            "ongoing" to ongoing, "fgs" to fgs,
            "groupSummary" to (n.flags and Notification.FLAG_GROUP_SUMMARY != 0),
            "hasGroup" to (n.group != null),
            "onlyAlertOnce" to (n.flags and Notification.FLAG_ONLY_ALERT_ONCE != 0),
            "isGroupSbn" to sbn.isGroup,
        )
        logMessagingShape(id, n.extras)

        if (ongoing || fgs) return ProbeLog.event("skip", "id" to id, "reason" to "ongoing_o_fgs")
        if (relation != "nueva" || until <= now) return
        // relation == "actualizacion_antes_de_tiempo" (Q2): se ve en el próximo SNAPSHOT si volvió a estar activa.

        try {
            snoozeNotification(sbn.key, until - now)
            prefs.edit().putLong("snz.$id", until).apply()
            ProbeLog.event("snoozed", "id" to id, "duration_ms" to (until - now), "planned" to until)
        } catch (e: Exception) {
            ProbeLog.event("snooze_failed", "id" to id, "error" to e.javaClass.simpleName)
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int,
    ) {
        sbn ?: return
        if (sbn.packageName != ProbeConfig.pkg(this)) return
        // reason 18 = REASON_SNOOZED (constante @hide): se registra el número crudo.
        ProbeLog.event("removed", "id" to ProbeLog.id(this, sbn.key), "reason" to reason)
    }

    /** Q5: ¿se puede saber remitente y grupo de forma fiable? Solo la forma, nunca los valores. */
    @Suppress("DEPRECATION")
    private fun logMessagingShape(id: String, extras: Bundle) {
        val template = extras.getString("android.template")
        val messages = extras.getParcelableArray("android.messages")
        val last = messages?.lastOrNull() as? Bundle
        ProbeLog.event(
            "shape",
            "id" to id,
            "messagingStyle" to (template?.endsWith("MessagingStyle") == true),
            "hasIsGroupExtra" to extras.containsKey("android.isGroupConversation"),
            "isGroupExtra" to extras.getBoolean("android.isGroupConversation", false),
            "hasConversationTitle" to !extras.getCharSequence("android.conversationTitle").isNullOrEmpty(),
            "messagesCount" to (messages?.size ?: 0),
            "lastHasSenderText" to !last?.getCharSequence("sender").isNullOrEmpty(),
            "lastHasSenderPerson" to (last?.containsKey("sender_person") == true),
            "hasTitle" to !extras.getCharSequence("android.title").isNullOrEmpty(),
        )
    }

    fun snapshot() {
        val active = try { activeNotifications } catch (e: Exception) { null }
        val target = active?.filter { it.packageName == ProbeConfig.pkg(this) }
        ProbeLog.event(
            "snapshot",
            "active_total" to active?.size, "active_target" to target?.size,
            "target_ids" to target?.joinToString(",") { ProbeLog.id(this, it.key) },
        )
    }

    /** Q2: ¿qué queda pospuesto según el sistema? getSnoozedNotifications por reflexión (API no garantizada). */
    fun logSnoozed() {
        try {
            val m = NotificationListenerService::class.java.getMethod("getSnoozedNotifications")
            @Suppress("UNCHECKED_CAST")
            val arr = m.invoke(this) as Array<StatusBarNotification>
            ProbeLog.event(
                "snoozed_list", "count" to arr.size,
                "ids" to arr.joinToString(",") { ProbeLog.id(this, it.key) },
            )
        } catch (e: Exception) {
            ProbeLog.event("snoozed_list_error", "error" to e.javaClass.simpleName)
        }
    }

    companion object {
        @Volatile
        var instance: ProbeListenerService? = null
    }
}
