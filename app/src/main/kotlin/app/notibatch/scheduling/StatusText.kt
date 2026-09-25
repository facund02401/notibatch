package app.notibatch.scheduling

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Textos de estado (pantalla de inicio e indicador persistente). Sin conteos, sin nombres. */
object StatusText {
    private val hhmm = DateTimeFormatter.ofPattern("HH:mm")

    fun forPlan(plan: DeliveryPlan, pausedUntil: Instant?, zone: ZoneId): String = when (plan) {
        is DeliveryPlan.HoldUntil -> "Activa · próxima entrega ${plan.at.atZone(zone).format(hhmm)}"
        is DeliveryPlan.PassThrough -> when (plan.why) {
            Why.PAUSED -> "En pausa hasta las ${pausedUntil?.atZone(zone)?.format(hhmm) ?: "--:--"}"
            Why.OUTSIDE_HOURS -> "Fuera de horario"
            Why.FREE_DAY -> "Día libre · la app no actúa"
            Why.UNKNOWN_PROFILE -> "Sin perfil configurado · todo pasa normal"
        }
    }
}

/** Borrador del mensaje de encuadre para pacientes (pendiente P9: el texto final lo decide el dueño). */
object FramingMessage {
    fun draft(times: List<LocalTime>): String {
        val hs = times.sorted().map { if (it.minute == 0) "${it.hour}" else "${it.hour}:%02d".format(it.minute) }
        if (hs.isEmpty()) return "Respondo mensajes en algunos momentos del día. Si es urgente, llamame."
        val partes = hs.map { "a las $it" }
        val cuando = if (partes.size == 1) partes[0] else partes.dropLast(1).joinToString(", ") + " y " + partes.last()
        return "Respondo mensajes $cuando. Si es urgente, llamame."
    }
}
