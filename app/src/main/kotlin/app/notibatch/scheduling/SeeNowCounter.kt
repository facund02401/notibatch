package app.notibatch.scheduling

import java.time.LocalDate

/**
 * Cuenta los usos de "Ver ahora" del día. Sin tope: desde el tercer uso solo se pide mostrar un
 * aviso suave (spec, Newport). Vive en memoria; si el proceso muere, empieza de cero (no bloquea).
 */
class SeeNowCounter(private val softLimit: Int = 3) {
    private var day: LocalDate? = null
    private var count = 0

    /** Registra un uso. Devuelve true si corresponde mostrar el aviso suave. */
    @Synchronized
    fun record(today: LocalDate): Boolean {
        if (day != today) {
            day = today
            count = 0
        }
        count++
        return count >= softLimit
    }
}
