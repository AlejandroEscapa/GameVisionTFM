package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.ui.views.composables.GameListItem
import es.androidtfm.gamevision.ui.views.composables.MAX_POR_GRUPO
import es.androidtfm.gamevision.ui.views.composables.agruparBiblioteca
import es.androidtfm.gamevision.ui.views.composables.ordenarBiblioteca
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests de la biblioteca apilada (F4.5/DX.9): agrupación por estado en orden
 * fijo sin los vacíos, y orden con el criterio del menú. Lógica pura en JVM.
 */
class GruposBibliotecaTest {

    private fun ficha(
        id: String,
        status: LibraryStatus = LibraryStatus.PLAYING,
        favorito: Boolean = false,
        minutos: Int = 0,
        nota: Double? = null,
        year: String = "2024"
    ) = GameListItem(
        gameId = id,
        name = id,
        coverUrl = null,
        released = year,
        genres = emptyList(),
        entry = LibraryEntry(
            gameId = id,
            status = status,
            favorite = favorito,
            minutesTotal = minutos,
            rating = nota
        )
    )

    private fun reciente(id: String) =
        GameListItem(id, id, null, "", emptyList(), null)

    @Test
    fun vacia_no_hay_grupos() {
        assertTrue(agruparBiblioteca(emptyList()).isEmpty())
    }

    @Test
    fun orden_fijo_y_sin_vacios() {
        val grupos = agruparBiblioteca(
            listOf(
                ficha("deseado", LibraryStatus.WISHED),
                ficha("jugando", LibraryStatus.PLAYING),
                ficha("pausa", LibraryStatus.PAUSED)
            )
        )
        assertEquals(listOf("playing", "paused", "wished"), grupos.map { it.clave })
        assertEquals(listOf("Jugando", "En pausa", "Deseados"), grupos.map { it.titulo })
    }

    @Test
    fun favoritos_duplican_despues_de_coleccionados() {
        val grupos = agruparBiblioteca(
            listOf(ficha("juego", LibraryStatus.PLAYING, favorito = true))
        )
        assertEquals(listOf("playing", "favorites"), grupos.map { it.clave })
        assertEquals(listOf("juego"), grupos[0].items.map { it.gameId })
        assertEquals(listOf("juego"), grupos[1].items.map { it.gameId })
    }

    @Test
    fun historial_son_los_sin_ficha() {
        val grupos = agruparBiblioteca(
            listOf(ficha("biblio"), reciente("visto"))
        )
        val historial = grupos.first { it.clave == "history" }
        assertEquals(listOf("visto"), historial.items.map { it.gameId })
    }

    @Test
    fun orden_alfabetico_ignora_mayusculas() {
        val items = listOf(ficha("Zelda"), ficha("halo"), ficha("Elden"))
        val r = ordenarBiblioteca(items, "Alfabético")
        assertEquals(listOf("Elden", "halo", "Zelda"), r.map { it.gameId })
    }

    @Test
    fun mas_jugados_primero() {
        val items = listOf(ficha("poco", minutos = 30), ficha("mucho", minutos = 300))
        val r = ordenarBiblioteca(items, "Más jugados")
        assertEquals(listOf("mucho", "poco"), r.map { it.gameId })
    }

    @Test
    fun tope_visible_por_grupo_es_5() {
        assertEquals(5, MAX_POR_GRUPO)
    }
}
