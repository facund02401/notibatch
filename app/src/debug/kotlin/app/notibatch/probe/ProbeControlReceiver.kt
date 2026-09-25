package app.notibatch.probe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Control por `adb shell am broadcast` (ver docs/04-fase0-viabilidad.md).
 * Protegido con android:permission=DUMP en el manifest debug.
 */
class ProbeControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CONFIG -> {
                val pkg = intent.getStringExtra("pkg")
                    ?: return ProbeLog.event("config_error", "reason" to "sin_pkg")
                val until = when {
                    intent.hasExtra("until_ms") -> intent.getLongExtra("until_ms", 0L)
                    intent.hasExtra("minutes") ->
                        System.currentTimeMillis() + intent.getIntExtra("minutes", 0) * 60_000L
                    else -> return ProbeLog.event("config_error", "reason" to "sin_until")
                }
                ProbeConfig.set(context, pkg, until)
                ProbeLog.event("config", "pkg" to pkg, "until" to until)
            }
            ACTION_SNAPSHOT -> ProbeListenerService.instance?.snapshot()
                ?: ProbeLog.event("snapshot_error", "reason" to "servicio_no_conectado")
            ACTION_SNOOZED -> ProbeListenerService.instance?.logSnoozed()
                ?: ProbeLog.event("snoozed_error", "reason" to "servicio_no_conectado")
        }
    }

    companion object {
        const val ACTION_CONFIG = "app.notibatch.probe.CONFIG"
        const val ACTION_SNAPSHOT = "app.notibatch.probe.SNAPSHOT"
        const val ACTION_SNOOZED = "app.notibatch.probe.SNOOZED"
    }
}
