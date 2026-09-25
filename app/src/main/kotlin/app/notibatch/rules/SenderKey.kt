package app.notibatch.rules

import java.text.Normalizer

/** MAC de bytes; en producción es HMAC-SHA256 con clave en Android Keystore (ADR-0006). */
fun interface Mac {
    fun mac(data: ByteArray): ByteArray
}

/**
 * Clave de un remitente = HMAC(nombre normalizado). Nunca se guarda el nombre (H5/H6).
 *
 * La normalización es deliberadamente permisiva: sin tildes ni mayúsculas, sin caracteres invisibles
 * ni el "~" que WhatsApp antepone a contactos no guardados. Coincidir de más solo hace que pase de
 * más (ADR-0005), nunca que se pierda algo.
 */
class SenderKeys(private val mac: Mac) {

    /** Devuelve null si tras normalizar no queda nada (remitente indeterminado → fail-open). */
    fun keyFor(displayName: CharSequence?): String? {
        val n = normalize(displayName) ?: return null
        return mac.mac(n.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val invisible = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2066-\\u2069\\uFEFF]")
        private val marks = Regex("\\p{Mn}+")
        private val spaces = Regex("\\s+")

        fun normalize(name: CharSequence?): String? {
            if (name == null) return null
            var s = Normalizer.normalize(name.toString(), Normalizer.Form.NFKD)
            s = marks.replace(s, "")
            s = invisible.replace(s, "")
            s = s.trim().removePrefix("~").trim()
            s = spaces.replace(s, " ").lowercase()
            return s.ifEmpty { null }
        }
    }
}
