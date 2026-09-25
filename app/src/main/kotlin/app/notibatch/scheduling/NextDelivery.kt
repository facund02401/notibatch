package app.notibatch.scheduling

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

sealed interface DeliveryPlan {
    /** Retener y liberar en [at] (el snooze dura `at - ahora`). */
    data class HoldUntil(val at: Instant, val profileId: String) : DeliveryPlan

    /** No retener: la notificación pasa normal. */
    data class PassThrough(val why: Why) : DeliveryPlan
}

enum class Why { PAUSED, FREE_DAY, UNKNOWN_PROFILE, OUTSIDE_HOURS }

/**
 * Cálculo de la próxima entrega (ADR-0004). Función pura de (ahora, plan, perfiles, pausa).
 *
 * Reglas:
 *  - Pausada (`now < pausedUntil`): pasa. Solo afecta a lo que llegue después (ADR-0003).
 *  - Perfil "Libre" o desconocido: pasa (fail-open).
 *  - Fuera de [activeFrom, activeTo): pasa.
 *  - Dentro: se libera en la primera hora fija estrictamente posterior a ahora; si ya no quedan,
 *    en `activeTo` (la ventana se cierra y lo pendiente se entrega ahí).
 */
object NextDelivery {
    fun compute(
        now: Instant,
        zone: ZoneId,
        plan: WeekPlan,
        profiles: Map<String, DeliveryProfile>,
        pausedUntil: Instant? = null,
    ): DeliveryPlan {
        if (pausedUntil != null && now < pausedUntil) return DeliveryPlan.PassThrough(Why.PAUSED)

        val local = now.atZone(zone)
        val date = local.toLocalDate()
        val profile = profiles[plan.profileIdFor(date)]
            ?: return DeliveryPlan.PassThrough(Why.UNKNOWN_PROFILE)
        if (profile.isFree) return DeliveryPlan.PassThrough(Why.FREE_DAY)

        val from = profile.activeFrom!!
        val to = profile.activeTo!!
        val t = local.toLocalTime()
        if (t < from || t >= to) return DeliveryPlan.PassThrough(Why.OUTSIDE_HOURS)

        val nextTime = profile.deliveryTimes.filter { it > t && it < to }.minOrNull() ?: to
        val at = ZonedDateTime.of(date, nextTime, zone).toInstant()
        return DeliveryPlan.HoldUntil(at, profile.id)
    }
}
