package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.repository.IndexWrite
import es.androidtfm.gamevision.data.repository.createIndexWrites
import es.androidtfm.gamevision.data.repository.indexEmail
import es.androidtfm.gamevision.data.repository.updateIndexWrites
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/*
 * Tests de la lógica de índices inversos `email_index` / `usernames` (ADR-0008).
 * Se ejecutan en JVM (sin Android ni Firebase).
 *
 * Cubren el camino de escritura del perfil, que era el que no tenía ninguna prueba:
 * ahí vivían el índice fantasma al renombrar y las escrituras a medias.
 */
class UserIndexPlanTest {

    // ---- Normalización del email como clave de índice ----

    @Test
    fun `indexEmail normaliza a minusculas y sin bordes`() {
        assertEquals("alex@test.dev", indexEmail("  Alex@Test.DEV "))
        assertEquals("alex@test.dev", indexEmail("alex@test.dev"))
    }

    @Test
    fun `indexEmail de un email vacio queda en blanco`() {
        assertEquals("", indexEmail("   "))
    }

    // ---- Al crear el perfil ----

    @Test
    fun `createIndexWrites escribe los dos indices con el email normalizado`() {
        val writes = createIndexWrites("uid1", "  Alex@Test.DEV ", "alex")

        assertEquals(
            listOf(
                IndexWrite.LinkEmail("alex@test.dev", "uid1"),
                IndexWrite.LinkUsername("alex", "uid1")
            ),
            writes
        )
    }

    @Test
    fun `createIndexWrites sin email no escribe el indice de email`() {
        val writes = createIndexWrites("uid1", "   ", "alex")

        assertEquals(listOf(IndexWrite.LinkUsername("alex", "uid1")), writes)
    }

    @Test
    fun `createIndexWrites sin username no escribe el indice de username`() {
        val writes = createIndexWrites("uid1", "alex@test.dev", "")

        assertEquals(listOf(IndexWrite.LinkEmail("alex@test.dev", "uid1")), writes)
    }

    @Test
    fun `createIndexWrites sin nada no escribe nada`() {
        assertTrue(createIndexWrites("uid1", "  ", "   ").isEmpty())
    }

    // ---- Al actualizar el perfil ----

    @Test
    fun `updateIndexWrites no toca indices si el username no cambia`() {
        assertTrue(updateIndexWrites("uid1", newUsername = null, oldUsername = null).isEmpty())
        assertTrue(updateIndexWrites("uid1", newUsername = "", oldUsername = "alex").isEmpty())
        assertTrue(updateIndexWrites("uid1", newUsername = "   ", oldUsername = "alex").isEmpty())
    }

    @Test
    fun `rename retira el indice viejo para no dejar fantasma`() {
        val writes = updateIndexWrites("uid1", newUsername = "bob", oldUsername = "alex")

        assertEquals(
            listOf(
                IndexWrite.UnlinkUsername("alex"),
                IndexWrite.LinkUsername("bob", "uid1")
            ),
            writes
        )
    }

    @Test
    fun `rename al mismo nombre no retira ni duplica nada`() {
        val writes = updateIndexWrites("uid1", newUsername = "alex", oldUsername = "alex")

        assertEquals(listOf(IndexWrite.LinkUsername("alex", "uid1")), writes)
    }

    @Test
    fun `primer username solo enlaza, no retira`() {
        val writes = updateIndexWrites("uid1", newUsername = "bob", oldUsername = null)

        assertEquals(listOf(IndexWrite.LinkUsername("bob", "uid1")), writes)
    }

    @Test
    fun `rename sin nombre previo no intenta borrar nada`() {
        val writes = updateIndexWrites("uid1", newUsername = "bob", oldUsername = "")

        assertEquals(listOf(IndexWrite.LinkUsername("bob", "uid1")), writes)
    }

    @Test
    fun `un rename nunca deja dos usernames apuntando al mismo uid`() {
        val writes = updateIndexWrites("uid1", newUsername = "bob", oldUsername = "alex")
        val enlazados = writes.filterIsInstance<IndexWrite.LinkUsername>().map { it.username }
        val retirados = writes.filterIsInstance<IndexWrite.UnlinkUsername>().map { it.username }

        assertEquals(listOf("bob"), enlazados)
        assertEquals(listOf("alex"), retirados)
        assertTrue(retirados.none { it in enlazados })
    }
}
