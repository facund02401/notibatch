package app.notibatch.rules

/** Por qué se decidió lo que se decidió. Va a los logs (no contiene datos personales). */
enum class Reason {
    RETAIN,
    PASS_NOT_RETAINED_APP,
    PASS_OWN_APP,
    PASS_SYSTEM_APP,
    PASS_CATEGORY,
    PASS_ONGOING,
    PASS_MEDIA,
    PASS_HIDDEN_CONTENT,
    PASS_VERIFICATION_CODE,
    PASS_ALWAYS_APP,
    PASS_ALWAYS_SENDER,
    PASS_UNKNOWN_SENDER,
    PASS_INSISTENCE,
}

data class Decision(val retain: Boolean, val reason: Reason)

/**
 * Reglas de retención de la Fase 1. Orden = prioridad: primero lo que NUNCA se retiene, después las
 * excepciones del usuario y al final el default. Ante duda, pasa (ADR-0005).
 */
object RetentionRules {
    /** Categorías de `Notification.category` que nunca se retienen. */
    val NEVER_RETAINED_CATEGORIES = setOf(
        "call", "alarm", "reminder", "navigation", "transport", "sys", "stopwatch",
    )

    /**
     * @param insistencePassed la regla de insistencia ya decidió que este contacto pasa
     *   (el llamador la evalúa solo para chats individuales; ver [InsistenceCounter]).
     */
    fun decide(
        f: NotificationFacts,
        config: RetentionConfig,
        insistencePassed: Boolean = false,
    ): Decision {
        fun pass(r: Reason) = Decision(retain = false, reason = r)

        if (f.packageName == config.ownPackage) return pass(Reason.PASS_OWN_APP)
        if (f.packageName in config.systemPackages) return pass(Reason.PASS_SYSTEM_APP)
        if (f.category in NEVER_RETAINED_CATEGORIES) return pass(Reason.PASS_CATEGORY)
        if (f.isOngoing || f.isForegroundService) return pass(Reason.PASS_ONGOING)
        if (f.hasMediaSession) return pass(Reason.PASS_MEDIA)
        if (f.contentHiddenBySystem) return pass(Reason.PASS_HIDDEN_CONTENT)
        if (f.looksLikeVerificationCode) return pass(Reason.PASS_VERIFICATION_CODE)

        // P4: por defecto solo se retienen las apps elegidas.
        if (f.packageName !in config.retainedPackages) return pass(Reason.PASS_NOT_RETAINED_APP)
        if (f.packageName in config.alwaysPassPackages) return pass(Reason.PASS_ALWAYS_APP)

        // Grupos: retenidos por defecto; no se les aplica lista de contactos ni insistencia.
        val retain = Decision(retain = true, reason = Reason.RETAIN)
        return when (f.isGroupConversation) {
            true -> retain
            null -> pass(Reason.PASS_UNKNOWN_SENDER) // fail-open: no sabemos si es grupo
            false -> when {
                f.senderKey == null -> pass(Reason.PASS_UNKNOWN_SENDER) // fail-open
                f.senderKey in config.alwaysPassSenders -> pass(Reason.PASS_ALWAYS_SENDER)
                insistencePassed -> pass(Reason.PASS_INSISTENCE)
                else -> retain
            }
        }
    }
}
