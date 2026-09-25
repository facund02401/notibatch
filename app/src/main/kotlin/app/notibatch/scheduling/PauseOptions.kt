package app.notibatch.scheduling

import java.time.Instant
import java.time.ZoneId

/**
 * Opciones de pausa del tile. Pausar solo afecta a lo que llegue DESPUÉS (ADR-0003): lo ya
 * pospuesto no se puede liberar.
 */
object PauseOptions {
    fun oneHour(now: Instant): Instant = now.plusSeconds(3600)

    /** "Hasta mañana" = hasta las 00:00 del día siguiente en la zona local. */
    fun untilTomorrow(now: Instant, zone: ZoneId): Instant =
        now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
}
