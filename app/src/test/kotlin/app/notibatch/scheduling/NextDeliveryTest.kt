package app.notibatch.scheduling

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NextDeliveryTest {
    private val zone = ZoneId.of("America/Argentina/Buenos_Aires")
    private val profiles = DeliveryProfile.DEFAULTS.associateBy { it.id }

    // 2026-09-25 es viernes; 2026-09-26 sábado.
    private fun at(day: Int, h: Int, m: Int): Instant =
        LocalDateTime.of(2026, 9, day, h, m).atZone(zone).toInstant()

    private fun compute(now: Instant, plan: WeekPlan = WeekPlan.DEFAULT, paused: Instant? = null) =
        NextDelivery.compute(now, zone, plan, profiles, paused)

    @Test fun consultorio_antes_de_los_50_libera_a_los_50() {
        assertEquals(DeliveryPlan.HoldUntil(at(25, 10, 50), "consultorio"), compute(at(25, 10, 20)))
    }

    @Test fun consultorio_justo_a_los_50_libera_a_la_hora_siguiente() {
        assertEquals(DeliveryPlan.HoldUntil(at(25, 11, 50), "consultorio"), compute(at(25, 10, 50)))
    }

    @Test fun ultima_entrega_y_cierre_de_ventana() {
        assertEquals(DeliveryPlan.HoldUntil(at(25, 18, 50), "consultorio"), compute(at(25, 18, 10)))
        // Después de las 18:50 solo queda el cierre a las 19:00.
        assertEquals(DeliveryPlan.HoldUntil(at(25, 19, 0), "consultorio"), compute(at(25, 18, 55)))
    }

    @Test fun fuera_de_horario_pasa() {
        assertEquals(DeliveryPlan.PassThrough(Why.OUTSIDE_HOURS), compute(at(25, 8, 59)))
        assertEquals(DeliveryPlan.PassThrough(Why.OUTSIDE_HOURS), compute(at(25, 19, 0)))
        assertEquals(DeliveryPlan.PassThrough(Why.OUTSIDE_HOURS), compute(at(25, 23, 30)))
    }

    @Test fun inicio_de_ventana_ya_retiene() {
        assertEquals(DeliveryPlan.HoldUntil(at(25, 9, 50), "consultorio"), compute(at(25, 9, 0)))
    }

    @Test fun dia_libre_pasa() {
        assertEquals(DeliveryPlan.PassThrough(Why.FREE_DAY), compute(at(26, 11, 0)))
    }

    @Test fun cambio_de_perfil_de_hoy_manda_sobre_la_semana() {
        val plan = WeekPlan.DEFAULT.copy(
            overrideDate = LocalDate.of(2026, 9, 25),
            overrideProfileId = DeliveryProfile.ESCRITURA,
        )
        assertEquals(DeliveryPlan.HoldUntil(at(25, 13, 0), "escritura"), compute(at(25, 10, 20), plan))
        assertEquals(DeliveryPlan.HoldUntil(at(25, 20, 0), "escritura"), compute(at(25, 13, 0), plan))
        assertEquals(DeliveryPlan.HoldUntil(at(25, 21, 0), "escritura"), compute(at(25, 20, 30), plan))
    }

    @Test fun el_cambio_de_hoy_no_afecta_a_otro_dia() {
        val plan = WeekPlan.DEFAULT.copy(
            overrideDate = LocalDate.of(2026, 9, 24),
            overrideProfileId = DeliveryProfile.LIBRE,
        )
        assertTrue(compute(at(25, 10, 20), plan) is DeliveryPlan.HoldUntil)
    }

    @Test fun pausa_pasa_hasta_que_vence() {
        val until = at(25, 11, 0)
        assertEquals(DeliveryPlan.PassThrough(Why.PAUSED), compute(at(25, 10, 20), paused = until))
        assertTrue(compute(at(25, 11, 0), paused = until) is DeliveryPlan.HoldUntil)
    }

    @Test fun perfil_desconocido_pasa() {
        val plan = WeekPlan(mapOf(DayOfWeek.FRIDAY to "no-existe"))
        assertEquals(DeliveryPlan.PassThrough(Why.UNKNOWN_PROFILE), compute(at(25, 10, 0), plan))
    }
}
