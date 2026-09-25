package app.notibatch.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import app.notibatch.rules.Mac
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Cifrado con claves en Android Keystore, no exportables (ADR-0006). Usa `javax.crypto` directo.
 * Sin ejecutar todavía en un teléfono (no hay prueba unitaria posible en JVM): ver docs/05.
 */
object KeystoreCrypto {
    private const val PROVIDER = "AndroidKeyStore"
    private const val AES_ALIAS = "notibatch.aes"
    private const val HMAC_ALIAS = "notibatch.hmac"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private fun ks(): KeyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }

    private fun key(alias: String, algorithm: String, purposes: Int, configure: KeyGenParameterSpec.Builder.() -> Unit): SecretKey {
        val store = ks()
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(alias, purposes).apply(configure).build()
        return KeyGenerator.getInstance(algorithm, PROVIDER).apply { init(spec) }.generateKey()
    }

    private fun aesKey() = key(
        AES_ALIAS, KeyProperties.KEY_ALGORITHM_AES,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
    ) {
        setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        setKeySize(256)
    }

    private fun hmacKey() = key(
        HMAC_ALIAS, KeyProperties.KEY_ALGORITHM_HMAC_SHA256,
        KeyProperties.PURPOSE_SIGN,
    ) {}

    /** Devuelve IV (12 bytes) + texto cifrado con etiqueta GCM. */
    fun encrypt(plain: ByteArray): ByteArray {
        val c = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, aesKey()) }
        return c.iv + c.doFinal(plain)
    }

    /** Devuelve null si el dato está corrupto o la clave cambió: quien llama debe fallar abierto. */
    fun decrypt(blob: ByteArray): ByteArray? = try {
        if (blob.size <= IV_BYTES) null else {
            val c = Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, aesKey(), GCMParameterSpec(TAG_BITS, blob, 0, IV_BYTES))
            }
            c.doFinal(blob, IV_BYTES, blob.size - IV_BYTES)
        }
    } catch (e: Exception) {
        null
    }

    /** HMAC-SHA256 con la clave del Keystore, para [app.notibatch.rules.SenderKeys]. */
    val hmac: Mac = Mac { data ->
        javax.crypto.Mac.getInstance("HmacSHA256").apply { init(hmacKey()) }.doFinal(data)
    }
}
