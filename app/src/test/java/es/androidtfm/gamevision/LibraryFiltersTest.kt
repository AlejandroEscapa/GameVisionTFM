package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryFilters
import es.androidtfm.gamevision.data.library.LibraryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests de filtros y orden de la biblioteca (F1 — Bloque 4, T1.14/T1.15). JVM puro.
 */
class LibraryFiltersTest {

    private fun entry(
        id: String,
        name: String,
        genres: List<String> = emptyList(),
        platform: String? = null,
        released: String = "",
        rating: Double? = null,
        minutes: Int = 0
    ) = LibraryEntry(
        gameId = id, name = name, status = LibraryStatus.PLAYING,
        genres = genres, lastPlatform = platform, released = released,
        rating = rating, minutesTotal = minutes
    )

    @Test
    fun `sin criterios todo pasa`() {
        val c = LibraryFilters.Criteria()
        assertFalse(c.isActive)
        assertTrue(LibraryFilters.matches("Zelda", listOf("RPG"), "PC", c))
    }

    @Test
    fun `la busqueda por nombre ignora mayusculas y espacios`() {
        val c = LibraryFilters.Criteria(query = "  zel ")
        assertTrue(LibraryFilters.matches("The Legend of Zelda", emptyList(), null, c))
        assertFalse(LibraryFilters.matches("Halo 3", emptyList(), null, c))
    }

    @Test
    fun `el filtro de genero exige coincidencia`() {
        val c = LibraryFilters.Criteria(genre = "RPG")
        assertTrue(LibraryFilters.matches("X", listOf("Action", "rpg"), null, c))
        assertFalse(LibraryFilters.matches("X", listOf("Action"), null, c))
    }

    @Test
    fun `filtros combinados exigen que se cumplan todos`() {
        val c = LibraryFilters.Criteria(query = "elden", genre = "RPG", platform = "PC")
        assertTrue(LibraryFilters.matches("Elden Ring", listOf("RPG"), "PC", c))
        assertFalse(LibraryFilters.matches("Elden Ring", listOf("RPG"), "PS5", c))
        assertFalse(LibraryFilters.matches("Elden Ring", listOf("Shooter"), "PC", c))
    }

    @Test
    fun `isActive refleja si hay algun criterio`() {
        assertTrue(LibraryFilters.Criteria(query = "a").isActive)
        assertTrue(LibraryFilters.Criteria(genre = "RPG").isActive)
        assertTrue(LibraryFilters.Criteria(platform = "PC").isActive)
        assertFalse(LibraryFilters.Criteria().isActive)
    }

    @Test
    fun `ordenaciones basicas`() {
        val list = listOf(
            entry("1", "Zelda", released = "2020-01-01", rating = 4.0, minutes = 100),
            entry("2", "Halo", released = "2022-01-01", rating = null, minutes = 300),
            entry("3", "Mario", released = "2021-01-01", rating = 5.0, minutes = 50)
        )
        assertEquals(listOf("Halo", "Mario", "Zelda"), LibraryFilters.sort(list, LibraryFilters.Sort.ALPHA).map { it.name })
        assertEquals(listOf("Zelda", "Mario", "Halo"), LibraryFilters.sort(list, LibraryFilters.Sort.YEAR_ASC).map { it.name })
        assertEquals(listOf("Halo", "Mario", "Zelda"), LibraryFilters.sort(list, LibraryFilters.Sort.YEAR_DESC).map { it.name })
        assertEquals(listOf("Halo", "Zelda", "Mario"), LibraryFilters.sort(list, LibraryFilters.Sort.MOST_PLAYED).map { it.name })
    }

    @Test
    fun `mejor nota pone primero las notas altas y al final las sin nota`() {
        val list = listOf(
            entry("1", "A", rating = 3.0),
            entry("2", "B", rating = null),
            entry("3", "C", rating = 4.5)
        )
        assertEquals(listOf("C", "A", "B"), LibraryFilters.sort(list, LibraryFilters.Sort.BEST_RATED).map { it.name })
    }
}
