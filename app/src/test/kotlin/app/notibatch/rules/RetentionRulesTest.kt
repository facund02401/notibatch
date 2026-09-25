package app.notibatch.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RetentionRulesTest {
    private val config = RetentionConfig(
        alwaysPassSenders = setOf("hash-mama"),
        systemPackages = setOf("com.android.systemui"),
    )

    private fun chat(
        pkg: String = "com.whatsapp",
        category: String? = "msg",
        group: Boolean? = false,
        sender: String? = "hash-paciente",
        ongoing: Boolean = false,
        fgs: Boolean = false,
        media: Boolean = false,
        hidden: Boolean = false,
        otp: Boolean = false,
    ) = NotificationFacts(pkg, category, ongoing, fgs, media, hidden, group, sender, otp)

    private fun decide(f: NotificationFacts, insist: Boolean = false) =
        RetentionRules.decide(f, config, insist)

    @Test fun mensaje_individual_de_whatsapp_se_retiene() {
        assertEquals(Decision(true, Reason.RETAIN), decide(chat()))
    }

    @Test fun categorias_criticas_nunca_se_retienen() {
        for (c in RetentionRules.NEVER_RETAINED_CATEGORIES) {
            val d = decide(chat(category = c))
            assertFalse("categoría $c", d.retain)
            assertEquals(Reason.PASS_CATEGORY, d.reason)
        }
    }

    @Test fun llamada_de_whatsapp_pasa() {
        assertFalse(decide(chat(category = "call")).retain)
    }

    @Test fun ongoing_foreground_media_y_contenido_oculto_pasan() {
        assertEquals(Reason.PASS_ONGOING, decide(chat(ongoing = true)).reason)
        assertEquals(Reason.PASS_ONGOING, decide(chat(fgs = true)).reason)
        assertEquals(Reason.PASS_MEDIA, decide(chat(media = true)).reason)
        assertEquals(Reason.PASS_HIDDEN_CONTENT, decide(chat(hidden = true)).reason)
    }

    @Test fun codigo_de_verificacion_pasa() {
        assertEquals(Reason.PASS_VERIFICATION_CODE, decide(chat(otp = true)).reason)
    }

    @Test fun apps_propias_y_de_sistema_pasan() {
        assertEquals(Reason.PASS_OWN_APP, decide(chat(pkg = "app.notibatch")).reason)
        assertEquals(Reason.PASS_SYSTEM_APP, decide(chat(pkg = "com.android.systemui")).reason)
    }

    @Test fun app_no_elegida_pasa_por_defecto() {
        assertEquals(Reason.PASS_NOT_RETAINED_APP, decide(chat(pkg = "com.banco.app")).reason)
    }

    @Test fun app_en_siempre_pasan_pasa() {
        val c = config.copy(alwaysPassPackages = setOf("com.whatsapp"))
        assertEquals(Reason.PASS_ALWAYS_APP, RetentionRules.decide(chat(), c).reason)
    }

    @Test fun contacto_que_siempre_pasa() {
        assertEquals(Reason.PASS_ALWAYS_SENDER, decide(chat(sender = "hash-mama")).reason)
    }

    @Test fun grupos_se_retienen_aunque_el_remitente_este_en_la_lista() {
        assertTrue(decide(chat(group = true, sender = "hash-mama")).retain)
    }

    @Test fun grupo_sin_remitente_igual_se_retiene() {
        assertTrue(decide(chat(group = true, sender = null)).retain)
    }

    @Test fun remitente_desconocido_falla_abierto() {
        assertEquals(Reason.PASS_UNKNOWN_SENDER, decide(chat(sender = null)).reason)
    }

    @Test fun no_saber_si_es_grupo_falla_abierto() {
        assertEquals(Reason.PASS_UNKNOWN_SENDER, decide(chat(group = null)).reason)
    }

    @Test fun insistencia_solo_aplica_a_chats_individuales() {
        assertEquals(Reason.PASS_INSISTENCE, decide(chat(), insist = true).reason)
        assertTrue(decide(chat(group = true), insist = true).retain)
    }
}
