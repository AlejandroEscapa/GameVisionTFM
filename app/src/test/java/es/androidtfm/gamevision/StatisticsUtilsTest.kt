package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.StatisticsUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/*
 * Tests de la lógica de estadísticas (F1 — Bloque 3, T1.12/T1.13). JVM puro.
 */
class StatisticsUtilsTest {

    private fun millis(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun entry(
        id: String,
        name: String = "Juego $id",
        status: LibraryStatus = LibraryStatus.PLAYING,
        rating: Double? = null,
        genres: List<String> = emptyList(),
        platform: String? = null,
        released: String = "",
        minutes: Int = 0,
        finishedAt: Long? = null,
        addedAt: Long? = null,
        favorite: Boolean = false
    ) = LibraryEntry(
        gameId = id, name = name, status = status, rating = rating,
        genres = genres, lastPlatform = platform, released = released,
        minutesTotal = minutes, finishedAt = finishedAt, addedAt = addedAt, favorite = favorite
    )

    @Test
    fun `biblioteca vacia devuelve insights a cero`() {
        val i = StatisticsUtils.compute(emptyList(), millis(2026, Calendar.SEPTEMBER, 1))
        assertEquals(0, i.gamesTotal)
        assertNull(i.ratingAverage)
        assertTrue(i.topGenres.isEmpty())
    }

    @Test
    fun `cuenta juegos, minutos y estados`() {
        val list = listOf(
            entry("1", status = LibraryStatus.PLAYING, minutes = 60),
            entry("2", status = LibraryStatus.COMPLETED, minutes = 120),
            entry("3", status = LibraryStatus.COMPLETED, minutes = 30)
        )
        val i = StatisticsUtils.compute(list, millis(2026, 1, 1))
        assertEquals(3, i.gamesTotal)
        assertEquals(210, i.minutesTotal)
        assertEquals(2, i.completedCount)
        assertEquals(2, i.byStatus[LibraryStatus.COMPLETED])
        assertEquals("Juego 2", i.mostPlayed?.name)
    }

    @Test
    fun `media de notas redondeada a un decimal`() {
        val list = listOf(
            entry("1", rating = 4.0),
            entry("2", rating = 4.5),
            entry("3", rating = 3.0)
        )
        val i = StatisticsUtils.compute(list, millis(2026, 1, 1))
        assertEquals(3, i.ratedCount)
        assertEquals(3.8, i.ratingAverage!!, 0.0)
    }

    @Test
    fun `distribucion de notas cubre la escala y suma los valorados`() {
        val list = listOf(
            entry("1", rating = 4.5), entry("2", rating = 4.5), entry("3", rating = 2.0)
        )
        val i = StatisticsUtils.compute(list, millis(2026, 1, 1))
        assertEquals(10, i.ratingDistribution.size)
        assertEquals(2, i.ratingDistribution.first { it.first == 4.5 }.second)
        assertEquals(1, i.ratingDistribution.first { it.first == 2.0 }.second)
        assertEquals(3, i.ratingDistribution.sumOf { it.second })
    }

    @Test
    fun `top generos ordenados por frecuencia`() {
        val list = listOf(
            entry("1", genres = listOf("RPG", "Acción")),
            entry("2", genres = listOf("RPG")),
            entry("3", genres = listOf("RPG", "Acción", "Aventura"))
        )
        val i = StatisticsUtils.compute(list, millis(2026, 1, 1))
        assertEquals("RPG", i.topGenres[0].first)
        assertEquals(3, i.topGenres[0].second)
        assertEquals("Acción", i.topGenres[1].first)
        assertEquals(2, i.topGenres[1].second)
    }

    @Test
    fun `plataformas y años de lanzamiento`() {
        val list = listOf(
            entry("1", platform = "PC", released = "2020-01-01"),
            entry("2", platform = "PC", released = "2021-05-01"),
            entry("3", platform = "PS5", released = "2021-09-01")
        )
        val i = StatisticsUtils.compute(list, millis(2026, 1, 1))
        assertEquals("PC", i.topPlatforms[0].first)
        assertEquals(2, i.topPlatforms[0].second)
        assertEquals(listOf(2021 to 2, 2020 to 1), i.gamesByYear)
    }

    @Test
    fun `el ano en un vistazo cuenta solo lo del ano actual`() {
        val now = millis(2026, Calendar.JUNE, 1)
        val list = listOf(
            entry("1", status = LibraryStatus.COMPLETED, rating = 5.0, finishedAt = millis(2026, 3, 1), addedAt = millis(2026, 2, 1)),
            entry("2", status = LibraryStatus.COMPLETED, rating = 4.0, finishedAt = millis(2026, 4, 1), addedAt = millis(2025, 12, 1)),
            entry("3", status = LibraryStatus.COMPLETED, rating = 4.8, finishedAt = millis(2025, 5, 1), addedAt = millis(2025, 1, 1)),
            entry("4", status = LibraryStatus.PLAYING, addedAt = millis(2026, 5, 1))
        )
        val i = StatisticsUtils.compute(list, now)
        assertEquals(2026, i.year)
        assertEquals(2, i.completedThisYear)
        assertEquals(3, i.addedThisYear)
        assertEquals("Juego 1", i.bestOfYear?.name)
    }

    @Test
    fun `sin actividad del ano no falla y no culpabiliza`() {
        val now = millis(2026, Calendar.JUNE, 1)
        val list = listOf(entry("1", addedAt = millis(2024, 1, 1)))
        val i = StatisticsUtils.compute(list, now)
        assertEquals(0, i.completedThisYear)
        assertEquals(0, i.addedThisYear)
        assertNull(i.bestOfYear)
    }
}
