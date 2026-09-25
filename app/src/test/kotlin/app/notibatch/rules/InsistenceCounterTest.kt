package app.notibatch.rules

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InsistenceCounterTest {
    private val min = 60_000L

    @Test fun tres_en_diez_minutos_pasa() {
        val c = InsistenceCounter()
        assertFalse(c.record("a", 0))
        assertFalse(c.record("a", 4 * min))
        assertTrue(c.record("a", 9 * min))
    }

    @Test fun mensajes_fuera_de_ventana_no_cuentan() {
        val c = InsistenceCounter()
        c.record("a", 0)
        c.record("a", 5 * min)
        // El primero (t=0) ya venció a los 10 min exactos: quedan 2 en ventana.
        assertFalse(c.record("a", 10 * min))
        assertTrue(c.record("a", 11 * min))
    }

    @Test fun contactos_distintos_no_se_suman() {
        val c = InsistenceCounter()
        c.record("a", 0)
        c.record("b", 1)
        c.record("c", 2)
        assertFalse(c.record("a", 3))
    }

    @Test fun clear_borra_todo() {
        val c = InsistenceCounter()
        c.record("a", 0)
        c.record("a", 1)
        c.clear()
        assertFalse(c.record("a", 2))
    }
}
