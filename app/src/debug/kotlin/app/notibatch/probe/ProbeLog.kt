package app.notibatch.probe

import android.content.Context
import android.util.Log
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Registro de la sonda (ADR-0008): nunca escribe texto, títulos, nombres ni la key cruda.
 * Correlaciona eventos con un id corto = primeros 6 hex de SHA-256(sal + key).
 *
 * La sal se guarda en SharedPreferences SOLO en la variante debug: hace falta que sea estable
 * entre procesos para poder seguir una notificación a través de un reinicio del teléfono (Q3).
 */
object ProbeLog {
    private const val TAG = "NotiBatchProbe"
    private const val PREFS = "probe"
    private const val SALT = "salt"

    private var salt: ByteArray? = null

    fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun salt(context: Context): ByteArray {
        salt?.let { return it }
        val p = prefs(context)
        val stored = p.getString(SALT, null)
        val bytes = if (stored != null) {
            stored.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        } else {
            ByteArray(16).also {
                SecureRandom().nextBytes(it)
                p.edit().putString(SALT, it.joinToString("") { b -> "%02x".format(b) }).apply()
            }
        }
        salt = bytes
        return bytes
    }

    fun id(context: Context, key: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt(context))
        md.update(key.toByteArray())
        return md.digest().take(3).joinToString("") { "%02x".format(it) }
    }

    /** `fields` solo con valores no sensibles: números, booleanos, nombres de paquete, categorías. */
    fun event(name: String, vararg fields: Pair<String, Any?>) {
        val body = fields.joinToString(" ") { "${it.first}=${it.second}" }
        Log.i(TAG, "$name t=${System.currentTimeMillis()} $body")
    }
}
