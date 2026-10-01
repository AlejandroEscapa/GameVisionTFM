package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.library.TopGamesLogic
import es.androidtfm.gamevision.data.model.TopGame
import es.androidtfm.gamevision.data.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * T2.5 (revisión 02/10) — Lógica pura del Top 4.
 *
 * Sin Android ni Firestore: son las reglas del Top (cuántos caben, qué pasa al
 * repetir, cómo se resuelve una cuenta antigua sin miniatura). El bug que estos
 * tests cierran es el del perfil que pintaba «Sin definir»: la miniatura ya no
 * depende de la biblioteca.
 */
class TopGamesLogicTest {

    private fun juego(id: String) = TopGame(gameId = id, name = "Juego $id", coverUrl = "http://x/$id.jpg")

    // ---------------------------------------------------------- capacidad

    @Test
    fun `caben cuatro y el quinto no entra`() {
        var seleccion = emptyList<TopGame>()
        listOf("1", "2", "3", "4").forEach { id ->
            seleccion = TopGamesLogic.add(seleccion, juego(id)) ?: seleccion
        }
        assertEquals(4, seleccion.size)
        assertTrue(TopGamesLogic.contains(seleccion, "4"))
        // El quinto se rechaza en vez de recortar el primero en silencio.
        assertNull(TopGamesLogic.add(seleccion, juego("5")))
        assertEquals(4, seleccion.size)
        assertFalse(TopGamesLogic.contains(seleccion, "5"))
    }

    @Test
    fun `huecos libres bajan con cada juego`() {
        assertEquals(4, TopGamesLogic.freeSlots(emptyList()))
        assertEquals(3, TopGamesLogic.freeSlots(listOf(juego("1"))))
        assertEquals(0, TopGamesLogic.freeSlots(listOf(juego("1"), juego("2"), juego("3"), juego("4"))))
        assertFalse(TopGamesLogic.hasRoom(listOf(juego("1"), juego("2"), juego("3"), juego("4"))))
    }

    // ---------------------------------------------------------- idempotencia

    @Test
    fun `anadir un juego que ya esta no lo duplica`() {
        val seleccion = listOf(juego("7"))
        assertNull(TopGamesLogic.add(seleccion, juego("7")))
    }

    @Test
    fun `anadir con id vacio no hace nada`() {
        assertNull(TopGamesLogic.add(emptyList(), TopGame(gameId = "  ")))
    }

    @Test
    fun `toggle alterna alta y baja`() {
        val alta = TopGamesLogic.toggle(emptyList(), juego("10"))
        assertEquals(listOf("10"), TopGamesLogic.ids(alta))
        val baja = TopGamesLogic.toggle(alta, juego("10"))
        assertTrue(baja.isEmpty())
    }

    @Test
    fun `toggle con el top lleno y un juego nuevo no cambia nada`() {
        val lleno = listOf(juego("1"), juego("2"), juego("3"), juego("4"))
        assertEquals(lleno, TopGamesLogic.toggle(lleno, juego("9")))
    }

    // ---------------------------------------------------------- orden y borrado

    @Test
    fun `el orden de la lista es la posicion en el top`() {
        val seleccion = listOf(juego("b"), juego("a"), juego("c"))
        assertEquals(listOf("b", "a", "c"), TopGamesLogic.ids(seleccion))
        // Quitar el del medio conserva el orden de los demás.
        assertEquals(listOf("b", "c"), TopGamesLogic.ids(TopGamesLogic.remove(seleccion, "a")))
    }

    @Test
    fun `quitar un juego que no esta deja la lista igual`() {
        val seleccion = listOf(juego("1"))
        assertEquals(seleccion, TopGamesLogic.remove(seleccion, "404"))
    }

    @Test
    fun `from normaliza la portada nula a cadena vacia`() {
        val sinPortada = TopGamesLogic.from("5", "Hollow Knight", null)
        assertEquals("", sinPortada.coverUrl)
        assertEquals("Hollow Knight", sinPortada.name)
    }

    // ------------------------------------------- parseo y cuentas antiguas

    @Test
    fun `parsea topGames del documento ignorando entradas sin id`() {
        val perfil = UserProfile.fromMap(
            mapOf(
                "topGameIds" to listOf("1", "2"),
                "topGames" to listOf(
                    mapOf("gameId" to "1", "name" to "Zelda", "coverUrl" to "http://z.jpg"),
                    mapOf("name" to "sin id"),
                    mapOf("gameId" to "2", "name" to "Mario")
                )
            )
        )
        assertEquals(2, perfil.topGames.size)
        assertEquals("Zelda", perfil.topGames[0].name)
        assertEquals("", perfil.topGames[1].coverUrl)
    }

    @Test
    fun `cuenta antigua resuelve el top con la biblioteca como respaldo`() {
        // Perfil anterior al 02/10: ids y topGames vacío.
        val perfil = UserProfile.fromMap(mapOf("topGameIds" to listOf("10", "20")))
        val resuelto = perfil.topGamesResolved { id ->
            if (id == "10") "Juego viejo" to "http://viejo.jpg" else null
        }
        assertEquals(listOf("10", "20"), resuelto.map { it.gameId })
        assertEquals("Juego viejo", resuelto[0].name)
        assertEquals("http://viejo.jpg", resuelto[0].coverUrl)
        // El que la biblioteca no conoce no revienta: sale el slot con su id.
        assertEquals("", resuelto[1].name)
    }

    @Test
    fun `con miniatura propia la biblioteca no se consulta`() {
        val perfil = UserProfile.fromMap(
            mapOf(
                "topGameIds" to listOf("1"),
                "topGames" to listOf(mapOf("gameId" to "1", "name" to "Propio", "coverUrl" to "http://p.jpg"))
            )
        )
        var consultas = 0
        val resuelto = perfil.topGamesResolved { consultas++; "Biblioteca" to "http://b.jpg" }
        assertEquals("Propio", resuelto[0].name)
        assertEquals("http://p.jpg", resuelto[0].coverUrl)
        assertEquals(0, consultas)
    }

    @Test
    fun `el top nunca pinta mas de cuatro aunque el documento traiga mas`() {
        val perfil = UserProfile.fromMap(
            mapOf("topGameIds" to listOf("1", "2", "3", "4", "5", "6"))
        )
        assertEquals(4, perfil.topGamesResolved().size)
    }

    @Test
    fun `serializa las cuatro entradas con sus tres campos`() {
        val mapa = TopGame.toMapList(listOf(juego("3")))
        assertEquals(1, mapa.size)
        assertEquals("3", mapa[0]["gameId"])
        assertEquals("Juego 3", mapa[0]["name"])
        assertEquals("http://x/3.jpg", mapa[0]["coverUrl"])
    }
}
