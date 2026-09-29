package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.RatingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/*
 * Tests de la lógica pura de la biblioteca rica (F1 — Bloque 1).
 * Se ejecutan en JVM (sin Android ni Firebase).
 */
class RatingUtilsTest {

    @Test
    fun `la escala tiene 10 niveles de 0,5 a 5,0`() {
        assertEquals(10, RatingUtils.SCALE.size)
        assertEquals(0.5, RatingUtils.SCALE.first(), 0.0)
        assertEquals(5.0, RatingUtils.SCALE.last(), 0.0)
    }

    @Test
    fun `snap lleva cualquier valor a la rejilla de medias estrellas`() {
        assertEquals(3.5, RatingUtils.snap(3.4)!!, 0.0)
        assertEquals(3.5, RatingUtils.snap(3.6)!!, 0.0)
        assertEquals(4.0, RatingUtils.snap(3.99)!!, 0.0)
        assertEquals(0.5, RatingUtils.snap(0.2)!!, 0.0)
        assertEquals(1.0, RatingUtils.snap(1.24)!!, 0.0)
    }

    @Test
    fun `snap limita a los extremos de la escala`() {
        assertEquals(5.0, RatingUtils.snap(9.9)!!, 0.0)
        assertEquals(0.5, RatingUtils.snap(-3.0)!!, 0.0)
    }

    @Test
    fun `snap devuelve null para valores no representables`() {
        assertNull(RatingUtils.snap(null))
        assertNull(RatingUtils.snap(Double.NaN))
        assertNull(RatingUtils.snap(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `la media se calcula redondeada a un decimal`() {
        assertEquals(4.0, RatingUtils.average(12.0, 3)!!, 0.0)
        assertEquals(3.3, RatingUtils.average(10.0, 3)!!, 0.0)
        assertNull(RatingUtils.average(0.0, 0))
    }

    @Test
    fun `round1 elimina el ruido de coma flotante`() {
        assertEquals(2.5, RatingUtils.round1(2.5000000001), 0.0)
        assertEquals(4.5, RatingUtils.round1(4.4999999999), 0.0)
    }

    @Test
    fun `los 7 estados existen con sus valores estables`() {
        assertEquals(7, LibraryStatus.entries.size)
        assertEquals(LibraryStatus.PLAYING, LibraryStatus.fromValue("jugando"))
        assertEquals(LibraryStatus.COLLECTED, LibraryStatus.fromValue("coleccionado"))
        assertEquals(LibraryStatus.WISHED, LibraryStatus.fromValue("deseado"))
        assertNull(LibraryStatus.fromValue("inventado"))
    }
}
