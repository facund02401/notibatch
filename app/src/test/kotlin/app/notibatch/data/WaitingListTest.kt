package app.notibatch.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WaitingListTest {
    private val hour = 60 * 60_000L
    private fun w(at: Long, who: String = "Ana") = Waiting("WhatsApp", who, false, at)

    @Test fun drain_devuelve_todo_y_vacia() {
        val l = WaitingList()
        l.add(w(0)); l.add(w(1, "Mamá"))
        assertEquals(2, l.drain(hour).size)
        assertTrue(l.snapshot(hour).isEmpty())
    }

    @Test fun purga_lo_de_24_horas_o_mas() {
        val l = WaitingList()
        l.add(w(0)); l.add(w(23 * hour))
        val s = l.snapshot(24 * hour)
        assertEquals(1, s.size)
        assertEquals(23 * hour, s[0].postedAtMs)
    }

    @Test fun snapshot_no_vacia() {
        val l = WaitingList()
        l.add(w(0))
        l.snapshot(1)
        assertEquals(1, l.snapshot(2).size)
    }
}
