package app.notibatch.scheduling

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Perfil de entregas de un tipo de día. `activeFrom`/`activeTo` delimitan cuándo la app actúa;
 * fuera de esa ventana todo pasa normal. `deliveryTimes` son horas fijas (nada de intervalos).
 * Un perfil sin ventana (`activeFrom == null`) es "Libre": la app no actúa.
 */
data class DeliveryProfile(
    val id: String,
    val name: String,
    val activeFrom: LocalTime?,
    val activeTo: LocalTime?,
    val deliveryTimes: List<LocalTime>,
) {
    val isFree: Boolean get() = activeFrom == null || activeTo == null

    companion object {
        const val CONSULTORIO = "consultorio"
        const val ESCRITURA = "escritura"
        const val LIBRE = "libre"

        /** Precargados como en el prompt (pendiente P6: horarios reales del dueño). */
        val DEFAULTS: List<DeliveryProfile> = listOf(
            DeliveryProfile(
                CONSULTORIO, "Consultorio",
                LocalTime.of(9, 0), LocalTime.of(19, 0),
                (9..18).map { LocalTime.of(it, 50) },
            ),
            DeliveryProfile(
                ESCRITURA, "Escritura/tesis",
                LocalTime.of(8, 0), LocalTime.of(21, 0),
                listOf(LocalTime.of(13, 0), LocalTime.of(20, 0)),
            ),
            DeliveryProfile(LIBRE, "Libre", null, null, emptyList()),
        )
    }
}

/** Perfil por día de la semana + cambio puntual para una fecha (el "hoy" desde la app). */
data class WeekPlan(
    val profileByDay: Map<DayOfWeek, String>,
    val overrideDate: LocalDate? = null,
    val overrideProfileId: String? = null,
) {
    fun profileIdFor(date: LocalDate): String =
        if (date == overrideDate && overrideProfileId != null) overrideProfileId
        else profileByDay[date.dayOfWeek] ?: DeliveryProfile.LIBRE

    companion object {
        /** Lunes a viernes consultorio, fin de semana libre (editable; pendiente P6). */
        val DEFAULT = WeekPlan(
            DayOfWeek.values().associateWith {
                if (it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY) DeliveryProfile.LIBRE
                else DeliveryProfile.CONSULTORIO
            },
        )
    }
}
