package app.notibatch.rules

/**
 * Lo único que las reglas saben de una notificación (ADR-0011). Lo arma el listener a partir de la
 * `StatusBarNotification`; así las reglas son JVM puro y se prueban sin Android.
 *
 * Nunca lleva texto ni título del mensaje. `senderKey` es el HMAC del remitente normalizado (o null
 * si no se pudo determinar), nunca el nombre.
 */
data class NotificationFacts(
    val packageName: String,
    /** Valor de `Notification.category` (p. ej. "call", "alarm", "sys"), o null. */
    val category: String?,
    val isOngoing: Boolean,
    val isForegroundService: Boolean,
    val hasMediaSession: Boolean,
    /** Android 15+ oculta el contenido sensible (OTP) a los listeners. */
    val contentHiddenBySystem: Boolean,
    /** `true`/`false` si `isGroupConversation` es fiable; null si no se pudo determinar. */
    val isGroupConversation: Boolean?,
    val senderKey: String?,
    /** Resultado de [OtpDetector] calculado en memoria por el listener; el texto no sale de ahí. */
    val looksLikeVerificationCode: Boolean = false,
)
