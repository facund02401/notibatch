package app.notibatch.rules

/**
 * Configuración de retención (ya descifrada, en memoria).
 *
 * Desvío del prompt, recomendado por el Consejo (pendiente P4): se retienen SOLO las apps de
 * [retainedPackages]; el resto pasa. Cambiarlo a "retener todo salvo excepciones" es tocar solo
 * [RetentionRules] (la línea marcada con P4).
 */
data class RetentionConfig(
    val retainedPackages: Set<String> = DEFAULT_RETAINED,
    /** Apps enteras que siempre pasan aunque estén en [retainedPackages]. */
    val alwaysPassPackages: Set<String> = emptySet(),
    /** HMAC de remitentes (chats individuales) que siempre pasan. */
    val alwaysPassSenders: Set<String> = emptySet(),
    /** Paquetes de SystemUI, alertas de emergencia y marcador por defecto (los resuelve el listener). */
    val systemPackages: Set<String> = emptySet(),
    val ownPackage: String = "app.notibatch",
) {
    companion object {
        val DEFAULT_RETAINED = setOf("com.whatsapp", "com.whatsapp.w4b")
    }
}
