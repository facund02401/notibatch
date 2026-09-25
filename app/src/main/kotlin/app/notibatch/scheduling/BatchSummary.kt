package app.notibatch.scheduling

import app.notibatch.data.Waiting

/** Texto de la notificación resumen `InboxStyle` de cada tanda. */
data class SummaryText(
    /** Una línea por app, con nombres (`VISIBILITY_PRIVATE`: solo tras desbloquear). */
    val lines: List<String>,
    /** Versión para la pantalla de bloqueo: sin nombres. */
    val publicText: String,
)

object BatchSummary {
    private const val MAX_NAMES = 4

    /** Ej.: "WhatsApp · Ana, Mamá (3)". Grupos se cuentan aparte, sin listar su nombre. */
    fun build(items: List<Waiting>): SummaryText {
        if (items.isEmpty()) return SummaryText(emptyList(), "Sin mensajes en espera")

        val lines = items.groupBy { it.appLabel }.map { (app, list) ->
            val individuals = list.filter { !it.isGroup }.groupingBy { it.sender }.eachCount()
            val groupCount = list.filter { it.isGroup }.map { it.sender }.distinct().size
            val names = individuals.keys.toList()
            val shown = names.take(MAX_NAMES).joinToString(", ") +
                if (names.size > MAX_NAMES) " y ${names.size - MAX_NAMES} más" else ""
            val parts = buildList {
                if (names.isNotEmpty()) add(shown)
                if (groupCount > 0) add(if (groupCount == 1) "1 grupo" else "$groupCount grupos")
            }
            "$app · ${parts.joinToString(" y ")} (${list.size})"
        }
        val n = items.size
        val public = if (n == 1) "1 mensaje en espera" else "$n mensajes en espera"
        return SummaryText(lines, public)
    }
}
