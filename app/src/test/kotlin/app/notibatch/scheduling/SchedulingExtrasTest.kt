package app.notibatch.scheduling

import app.notibatch.data.Waiting
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SchedulingExtrasTest {
    private val zone = ZoneId.of("America/Argentina/Buenos_Aires")
    private val min = 60_000L
    private fun at(h: Int, m: Int): Instant = LocalDateTime.of(2026, 9, 25, h, m).atZone(zone).toInstant()

    // --- BatchSummary
    private fun w(who: String, group: Boolean = false, app: String = "WhatsApp") = Waiting(app, who, group, 0)

    @Test fun resumen_agrupa_por_app_y_cuenta() {
        val s = BatchSummary.build(listOf(w("Ana"), w("Ana"), w("Mamá")))
        assertEquals(listOf("WhatsApp · Ana, Mamá (3)"), s.lines)
        assertEquals("3 mensajes en espera", s.publicText)
    }

    @Test fun resumen_no_lista_nombres_de_grupos() {
        val s = BatchSummary.build(listOf(w("Ana"), w("Padres 3ºB", group = true), w("Padres 3ºB", group = true)))
        assertEquals(listOf("WhatsApp · Ana y 1 grupo (3)"), s.lines)
        assertFalse(s.lines.joinToString().contains("Padres"))
    }

    @Test fun resumen_publico_no_tiene_nombres() {
        val s = BatchSummary.build(listOf(w("Ana")))
        assertEquals("1 mensaje en espera", s.publicText)
        assertFalse(s.publicText.contains("Ana"))
    }

    @Test fun resumen_recorta_muchos_nombres() {
        val s = BatchSummary.build((1..6).map { w("P$it") })
        assertEquals(listOf("WhatsApp · P1, P2, P3, P4 y 2 más (6)"), s.lines)
    }

    @Test fun resumen_vacio() {
        assertTrue(BatchSummary.build(emptyList()).lines.isEmpty())
    }

    // --- Watchdog
    @Test fun watchdog_espera_larga_tolera_hasta_el_doble() {
        val held = 0L; val planned = 50 * min
        assertFalse(DeliveryWatchdog.isLate(held, planned, 100 * min))
        assertTrue(DeliveryWatchdog.isLate(held, planned, 100 * min + 1))
    }

    @Test fun watchdog_espera_corta_usa_margen_minimo() {
        val held = 0L; val planned = 5 * min
        assertFalse(DeliveryWatchdog.isLate(held, planned, 10 * min))
        assertTrue(DeliveryWatchdog.isLate(held, planned, 10 * min + 1))
    }

    // --- Pausa
    @Test fun pausa_de_una_hora() {
        assertEquals(at(11, 30), PauseOptions.oneHour(at(10, 30)))
    }

    @Test fun pausa_hasta_manana_es_medianoche_local() {
        val expected = LocalDateTime.of(2026, 9, 26, 0, 0).atZone(zone).toInstant()
        assertEquals(expected, PauseOptions.untilTomorrow(at(23, 59), zone))
        assertEquals(expected, PauseOptions.untilTomorrow(at(0, 1), zone))
    }

    // --- Ver ahora
    @Test fun ver_ahora_avisa_desde_el_tercer_uso_y_reinicia_por_dia() {
        val c = SeeNowCounter()
        val d1 = LocalDate.of(2026, 9, 25)
        assertFalse(c.record(d1)); assertFalse(c.record(d1))
        assertTrue(c.record(d1)); assertTrue(c.record(d1))
        assertFalse(c.record(d1.plusDays(1)))
    }

    // --- Textos
    @Test fun estado_activa() {
        val plan = DeliveryPlan.HoldUntil(at(13, 50), "consultorio")
        assertEquals("Activa · próxima entrega 13:50", StatusText.forPlan(plan, null, zone))
    }

    @Test fun estado_en_pausa_y_fuera_de_horario() {
        val paused = DeliveryPlan.PassThrough(Why.PAUSED)
        assertEquals("En pausa hasta las 15:00", StatusText.forPlan(paused, at(15, 0), zone))
        assertEquals("Fuera de horario", StatusText.forPlan(DeliveryPlan.PassThrough(Why.OUTSIDE_HOURS), null, zone))
    }

    @Test fun mensaje_de_encuadre() {
        assertEquals(
            "Respondo mensajes a las 13 y a las 20. Si es urgente, llamame.",
            FramingMessage.draft(listOf(LocalTime.of(20, 0), LocalTime.of(13, 0))),
        )
        assertEquals(
            "Respondo mensajes a las 9:30, a las 13 y a las 20. Si es urgente, llamame.",
            FramingMessage.draft(listOf(LocalTime.of(9, 30), LocalTime.of(13, 0), LocalTime.of(20, 0))),
        )
    }
}
