package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests de la lógica pura de estados de la biblioteca (F0-B/B1).
 * Sin Android ni Firestore: solo el mapeo de valores y la migración de listas antiguas.
 */
class LibraryStatusTest {

    @Test
    fun `fromValue reconoce todos los estados`() {
        assertEquals(LibraryStatus.PLAYING, LibraryStatus.fromValue("jugando"))
        assertEquals(LibraryStatus.COMPLETED, LibraryStatus.fromValue("completado"))
        assertEquals(LibraryStatus.COLLECTED, LibraryStatus.fromValue("coleccionado"))
        assertEquals(LibraryStatus.PAUSED, LibraryStatus.fromValue("en_pausa"))
        assertEquals(LibraryStatus.RETIRED, LibraryStatus.fromValue("retirado"))
        assertEquals(LibraryStatus.ABANDONED, LibraryStatus.fromValue("abandonado"))
        assertEquals(LibraryStatus.WISHED, LibraryStatus.fromValue("deseado"))
    }

    @Test
    fun `fromValue devuelve null con valor desconocido o nulo`() {
        assertNull(LibraryStatus.fromValue("inexistente"))
        assertNull(LibraryStatus.fromValue(null))
    }

    @Test
    fun `fromLegacyList mapea las listas antiguas segun la decision de migracion`() {
        // Decisión aprobada 28/09/2026: "Juegos jugados" entra como jugando.
        assertEquals(LibraryStatus.PLAYING, LibraryStatus.fromLegacyList("playedlist"))
        assertEquals(LibraryStatus.WISHED, LibraryStatus.fromLegacyList("wishlist"))
        // favorites se migra al flag `favorite`, y history se descarta (local).
        assertNull(LibraryStatus.fromLegacyList("favorites"))
        assertNull(LibraryStatus.fromLegacyList("history"))
    }

    @Test
    fun `todos los valores de estado son unicos`() {
        val values = LibraryStatus.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
    }
}
