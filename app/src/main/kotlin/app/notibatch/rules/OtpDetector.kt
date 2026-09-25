package app.notibatch.rules

/**
 * Detecta "parece un código de verificación" (spec Fase 1). Recibe el texto solo para evaluarlo en
 * memoria y devolver un booleano; el texto no se guarda ni se loguea. Sesgo deliberado hacia
 * falsos positivos: un falso "es un código" solo hace que pase de más (ADR-0005).
 */
object OtpDetector {
    private val keyword = Regex(
        "c[oó]digo|verificaci[oó]n|verification|verify|otp|one[- ]time|contrase[nñ]a|password|pin|" +
            "token|2fa|clave|security code|login code|sign[- ]?in",
        RegexOption.IGNORE_CASE,
    )
    // 4 a 8 dígitos, opcionalmente con un guion o espacio en el medio (123-456, G-123456).
    private val code = Regex("(?<![\d])(?:[A-Za-z]-)?\d{3,4}[- ]?\d{1,4}(?![\d])")

    fun looksLikeVerificationCode(text: CharSequence?): Boolean {
        if (text.isNullOrBlank()) return false
        val s = text.toString()
        return keyword.containsMatchIn(s) && code.containsMatchIn(s)
    }
}
