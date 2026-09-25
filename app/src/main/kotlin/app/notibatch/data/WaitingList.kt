package app.notibatch.data

/** Una notificación retenida, sin texto: solo app, quién y cuándo (ADR-0003, ADR-0007). */
data class Waiting(
    val appLabel: String,
    val sender: String,
    val isGroup: Boolean,
    val postedAtMs: Long,
)

/**
 * Lista "quién espera", SOLO en RAM (ADR-0007). Se purga por antigüedad (máx. 24 h, spec) y se vacía
 * al entregar la tanda. Si el proceso muere se pierde; la UI debe decirlo con honestidad.
 */
class WaitingList(private val maxAgeMs: Long = 24 * 60 * 60_000L) {
    private val items = ArrayList<Waiting>()

    @Synchronized
    fun add(w: Waiting) {
        items.add(w)
    }

    @Synchronized
    fun snapshot(nowMs: Long): List<Waiting> {
        items.removeAll { nowMs - it.postedAtMs >= maxAgeMs }
        return items.toList()
    }

    /** Entrega: devuelve lo pendiente y vacía la lista. */
    @Synchronized
    fun drain(nowMs: Long): List<Waiting> {
        val out = snapshot(nowMs)
        items.clear()
        return out
    }

    @Synchronized
    fun clear() = items.clear()
}
