package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.notifications.RewindAviso
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests de la decisión del aviso de fin de año (F3/T3.11, decisión D3.7).
 *
 * Es la parte del aviso que se puede probar sin Android, y donde está el riesgo de
 * producto: avisar a quien no jugó (D3.6 lo prohíbe) o avisar dos veces del mismo año.
 */
class RewindAvisoTest {

    private val enero2027 = LocalDate.of(2027, 1, 3)
    private val diciembre2026 = LocalDate.of(2026, 12, 20)

    @Test
    fun el_ano_que_cierra_es_el_anterior_al_de_hoy() {
        assertEquals(2026, RewindAviso.anioQueCierra(enero2027))
        assertEquals(2025, RewindAviso.anioQueCierra(diciembre2026))
    }

    @Test
    fun sin_actividad_no_avisa_nunca() {
        // D3.6: celebrar sin culpabilizar. A quien no jugó no se le avisa.
        assertFalse(RewindAviso.debeAvisar(0, null, enero2027))
        assertFalse(RewindAviso.debeAvisar(0, 2025, enero2027))
    }

    @Test
    fun con_actividad_y_sin_aviso_previo_avisa() {
        assertTrue(RewindAviso.debeAvisar(120, null, enero2027))
    }

    @Test
    fun no_avisa_dos_veces_del_mismo_ano() {
        assertFalse(RewindAviso.debeAvisar(120, 2026, enero2027))
    }

    @Test
    fun avisa_del_ano_nuevo_aunque_ya_avisara_del_anterior() {
        assertTrue(RewindAviso.debeAvisar(60, 2025, enero2027))
    }
}
