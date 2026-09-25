package app.notibatch.probe

import android.content.Context

/** Qué paquete pospone la sonda y hasta cuándo (epoch ms). Sin datos personales. */
object ProbeConfig {
    private const val PKG = "pkg"
    private const val UNTIL = "until"

    fun set(context: Context, pkg: String, untilMs: Long) {
        ProbeLog.prefs(context).edit().putString(PKG, pkg).putLong(UNTIL, untilMs).apply()
    }

    fun pkg(context: Context): String? = ProbeLog.prefs(context).getString(PKG, null)
    fun until(context: Context): Long = ProbeLog.prefs(context).getLong(UNTIL, 0L)
}
