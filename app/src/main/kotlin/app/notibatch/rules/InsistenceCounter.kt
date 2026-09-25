package app.notibatch.rules

/**
 * Regla de insistencia (opcional, apagada por defecto): si un mismo contacto de chat individual manda
 * [threshold] mensajes dentro de [windowMs], pasa. Vive solo en memoria y con la clave hash del
 * remitente; nunca se persiste. El llamador NO debe usarla con grupos.
 */
class InsistenceCounter(
    private val threshold: Int = 3,
    private val windowMs: Long = 10 * 60_000L,
) {
    private val hits = HashMap<String, ArrayDeque<Long>>()

    /** Registra un mensaje de [senderKey] en [nowMs]; devuelve true si ya alcanzó el umbral. */
    @Synchronized
    fun record(senderKey: String, nowMs: Long): Boolean {
        evict(nowMs)
        val q = hits.getOrPut(senderKey) { ArrayDeque() }
        q.addLast(nowMs)
        return q.size >= threshold
    }

    @Synchronized
    fun clear() = hits.clear()

    private fun evict(nowMs: Long) {
        val it = hits.entries.iterator()
        while (it.hasNext()) {
            val q = it.next().value
            while (q.isNotEmpty() && nowMs - q.first() >= windowMs) q.removeFirst()
            if (q.isEmpty()) it.remove()
        }
    }
}
