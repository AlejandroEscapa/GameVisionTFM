package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.hltb.HltbPlaytimes
import es.androidtfm.gamevision.data.hltb.HltbUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests de la lógica pura de duración (F1/T1.11). JVM puro.
 */
class HltbUtilsTest {

    @Test
    fun `segundos a minutos, redondeando, y null si no es positivo`() {
        assertEquals(60, HltbUtils.secondsToMinutes(3600))
        assertEquals(61, HltbUtils.secondsToMinutes(3650))
        assertNull(HltbUtils.secondsToMinutes(0))
        assertNull(HltbUtils.secondsToMinutes(-5))
        assertNull(HltbUtils.secondsToMinutes(null))
    }

    @Test
    fun `la clave de cache normaliza acentos, mayusculas y signos`() {
        assertEquals("the legend of zelda the", HltbUtils.cacheKey("The Legend of Zelda:  The"))
        assertEquals("the legend of zelda breath of the wild", HltbUtils.cacheKey("The Legend of Zelda™: Breath of the Wild"))
        assertEquals("pokemon rojo", HltbUtils.cacheKey("POKÉMON Rojo!"))
        assertEquals("metal gear solid v", HltbUtils.cacheKey("Metal   Gear   Solid   V"))
        assertEquals("", HltbUtils.cacheKey("   "))
    }

    @Test
    fun `la frescura respeta el TTL de 90 dias`() {
        val now = 1_000_000_000_000L
        assertTrue(HltbUtils.isFresh(now - 1000, now))
        assertTrue(HltbUtils.isFresh(now - HltbUtils.CACHE_TTL_MS + 1000, now))
        assertFalse(HltbUtils.isFresh(now - HltbUtils.CACHE_TTL_MS - 1, now))
        assertFalse(HltbUtils.isFresh(null, now))
    }

    @Test
    fun `hasData es true solo si hay algun tiempo util`() {
        val withData = HltbPlaytimes(1, 60, null, null, 1L)
        val noData = HltbPlaytimes(2, 0, 0, 0, 1L)
        assertTrue(withData.hasData)
        assertFalse(noData.hasData)
    }

    @Test
    fun `formatea minutos de duracion`() {
        assertEquals("—", HltbUtils.format(null))
        assertEquals("45 min", HltbUtils.format(45))
        assertEquals("12 h", HltbUtils.format(720))
        assertEquals("1 h 30 min", HltbUtils.format(90))
    }
}
