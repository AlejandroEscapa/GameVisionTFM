package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.RatingUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/*
 * Tests de los contadores agregados de la biblioteca (F1/T1.3 y cierre de ficha).
 *
 * `LibraryRepository` solo puede ajustar `ratingSum` y `ratingCount` por incrementos
 * (`FieldValue.increment`). Si los dos deltas se desalinean, la nota media de la
 * pantalla queda corrupta y ningún error lo delata: es la parte que hay que dejar
 * clavada con tests.
 */
class LibraryCountersTest {

    @Test
    fun `primera nota suma 1 a la cuenta`() {
        val d = RatingUtils.ratingDelta(previous = null, rating = 4.0)

        assertEquals(4.0, d.sumDelta, 0.0)
        assertEquals(1L, d.countDelta)
    }

    @Test
    fun `cambiar una nota no altera la cuenta`() {
        val d = RatingUtils.ratingDelta(previous = 3.5, rating = 4.0)

        assertEquals(0.5, d.sumDelta, 1e-9)
        assertEquals(0L, d.countDelta)
    }

    @Test
    fun `quitar una nota resta 1 de la cuenta`() {
        val d = RatingUtils.ratingDelta(previous = 3.5, rating = null)

        assertEquals(-3.5, d.sumDelta, 0.0)
        assertEquals(-1L, d.countDelta)
    }

    @Test
    fun `dejar la nota como estaba no mueve nada`() {
        val d = RatingUtils.ratingDelta(previous = 4.0, rating = 4.0)

        assertEquals(0.0, d.sumDelta, 0.0)
        assertEquals(0L, d.countDelta)
    }

    @Test
    fun `nada a nada no es un cambio`() {
        val d = RatingUtils.ratingDelta(previous = null, rating = null)

        assertEquals(0.0, d.sumDelta, 0.0)
        assertEquals(0L, d.countDelta)
    }

    @Test
    fun `los deltas siempre son coherentes con la media resultante`() {
        val cambios = listOf<Pair<Double?, Double?>>(
            null to 5.0, 5.0 to 0.5, 0.5 to null, null to 3.0, 3.0 to 4.5
        )
        var sum = 0.0
        var count = 0
        cambios.forEach { (prev, nueva) ->
            val d = RatingUtils.ratingDelta(prev, nueva)
            sum += d.sumDelta
            count += d.countDelta.toInt()

            // La media derivada de los agregados debe coincidir con la media real.
            val esperada = if (count <= 0) null else RatingUtils.round1(sum / count)
            val real = RatingUtils.average(sum, count)
            assertEquals(esperada, real)
        }
        assertEquals(4.5, sum, 1e-9)
        assertEquals(1, count)
        assertEquals(4.5, RatingUtils.average(sum, count)!!, 1e-9)
    }
}
