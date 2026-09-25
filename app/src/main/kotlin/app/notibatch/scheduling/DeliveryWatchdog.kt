package app.notibatch.scheduling

/**
 * Vigilante de fail-open (spec): si una entrega se atrasa más del doble de lo previsto, se entrega
 * todo y se avisa. "Doble de lo previsto" = espera prevista × 2 desde que se retuvo, con un margen
 * mínimo [graceMs] para que esperas cortas no disparen falsas alarmas por la imprecisión de las
 * alarmas inexactas (ADR-0004).
 */
object DeliveryWatchdog {
    fun isLate(
        heldAtMs: Long,
        plannedAtMs: Long,
        nowMs: Long,
        graceMs: Long = 5 * 60_000L,
    ): Boolean {
        val expected = (plannedAtMs - heldAtMs).coerceAtLeast(0L)
        val deadline = heldAtMs + maxOf(expected * 2, expected + graceMs)
        return nowMs > deadline
    }
}
