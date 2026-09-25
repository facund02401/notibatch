package app.notibatch.rules

import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SenderKeyTest {
    private fun keys(secret: String = "clave-de-prueba"): SenderKeys {
        val k = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        return SenderKeys { data ->
            javax.crypto.Mac.getInstance("HmacSHA256").apply { init(k) }.doFinal(data)
        }
    }

    @Test fun ignora_mayusculas_tildes_espacios_y_caracteres_invisibles() {
        val k = keys()
        val base = k.keyFor("Mamá")
        assertEquals(base, k.keyFor("  MAMA "))
        assertEquals(base, k.keyFor("‎mamá‏"))
        assertEquals(k.keyFor("Ana Pérez"), k.keyFor("ana   perez"))
    }

    @Test fun ignora_el_prefijo_tilde_de_contactos_no_guardados() {
        val k = keys()
        assertEquals(k.keyFor("Ana"), k.keyFor("~ Ana"))
        assertEquals(k.keyFor("Ana"), k.keyFor("~Ana"))
    }

    @Test fun nombres_distintos_dan_claves_distintas() {
        val k = keys()
        assertNotEquals(k.keyFor("Ana"), k.keyFor("Anabel"))
    }

    @Test fun la_clave_secreta_cambia_el_resultado_y_no_contiene_el_nombre() {
        val a = keys("uno").keyFor("Ana")!!
        assertNotEquals(a, keys("dos").keyFor("Ana"))
        assertEquals(64, a.length)
        assertNotEquals("ana", a)
    }

    @Test fun sin_nombre_no_hay_clave() {
        val k = keys()
        assertNull(k.keyFor(null))
        assertNull(k.keyFor("   "))
        assertNull(k.keyFor("~"))
        assertNull(k.keyFor("‎"))
    }
}
