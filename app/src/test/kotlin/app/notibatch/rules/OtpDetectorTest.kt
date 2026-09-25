package app.notibatch.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpDetectorTest {
    private fun otp(s: String?) = OtpDetector.looksLikeVerificationCode(s)

    @Test fun detecta_codigos_tipicos() {
        assertTrue(otp("Tu código de verificación es 483920"))
        assertTrue(otp("Your verification code is 123-456"))
        assertTrue(otp("G-482913 is your Google verification code"))
        assertTrue(otp("Usá 7391 como clave para ingresar"))
    }

    @Test fun un_mensaje_comun_no_lo_es() {
        assertFalse(otp("Hola, ¿podemos mover el turno a las 15?"))
        assertFalse(otp("Nos vemos a las 1530 en el consultorio")) // número sin palabra clave
        assertFalse(otp(null))
        assertFalse(otp("   "))
    }
}
