package es.androidtfm.gamevision

import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.session.SessionState
import es.androidtfm.gamevision.data.session.toAuthUserMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Tests JVM de la capa de datos/sesión introducida en el rediseño (SSOT).
 *
 * Solo lógica pura (sin runtime de Android): normalización del perfil y estado
 * de sesión. El mapeo de errores de Firebase Auth se prueba en androidTest,
 * porque instanciar sus excepciones requiere el framework de Android.
 */
class UserProfileAndAuthTest {

    // ---- UserProfile.fromMap: el bug del texto "null" en el perfil ----

    @Test
    fun `fromMap normaliza valores nulos a cadena vacia`() {
        val map = mapOf<String, Any?>(
            "email" to "alex@gmail.com",
            "nameSurname" to "Alejandro Escapa",
            "username" to "alex",
            "description" to null,
            "country" to null,
            "imageUri" to null
        )

        val profile = UserProfile.fromMap(map)

        assertEquals("Alejandro Escapa", profile.nameSurname)
        assertEquals("alex", profile.username)
        // Antes se mostraba el texto literal "null" en el perfil
        assertEquals("", profile.description)
        assertEquals("", profile.country)
        assertEquals("", profile.imageUri)
        // El email es un CAMPO del documento (ADR-0008).
        assertEquals("alex@gmail.com", profile.email)
    }

    @Test
    fun `fromMap con documento ausente devuelve perfil vacio`() {
        val profile = UserProfile.fromMap(null)

        assertEquals("", profile.email)
        assertEquals("", profile.nameSurname)
        assertTrue(profile.description.isEmpty())
    }

    @Test
    fun `fromMap ignora claves desconocidas y usa vacio si falta alguna`() {
        val profile = UserProfile.fromMap(mapOf("username" to "user_x", "campo_raro" to 42))

        assertEquals("user_x", profile.username)
        assertEquals("", profile.country)
    }

    // ---- SessionState: la identidad ya no viaja en el formulario ----

    @Test
    fun `solo LoggedIn expone email de sesion`() {
        val loggedIn: SessionState = SessionState.LoggedIn(uid = "uid1", email = "a@b.com")
        val guest: SessionState = SessionState.Guest
        val anonymous: SessionState = SessionState.Anonymous

        assertEquals("a@b.com", (loggedIn as? SessionState.LoggedIn)?.email)
        assertFalse(guest is SessionState.LoggedIn)
        assertFalse(anonymous is SessionState.LoggedIn)
    }

    // ---- Errores de Firebase Auth traducidos para el usuario ----

    @Test
    fun `error de red se traduce a mensaje de conexion`() {
        val message = IOException("Unable to resolve host").toAuthUserMessage()

        assertTrue(message.startsWith("Sin conexión"))
    }

    @Test
    fun `error desconocido usa su propio mensaje`() {
        val message = IllegalStateException("Algo raro").toAuthUserMessage()

        assertEquals("Algo raro", message)
    }
}
