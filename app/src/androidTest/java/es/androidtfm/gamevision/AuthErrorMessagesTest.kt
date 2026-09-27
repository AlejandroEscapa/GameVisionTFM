package es.androidtfm.gamevision

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import es.androidtfm.gamevision.data.session.toAuthUserMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Traducción de errores de Firebase Authentication a mensajes de usuario.
 * Se ejecuta en dispositivo/emulador (las excepciones de Firebase necesitan el
 * runtime de Android para instanciarse): ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class AuthErrorMessagesTest {

    @Test
    fun credenciales_invalidas_dan_un_mensaje_claro() {
        val message = FirebaseAuthInvalidCredentialsException("ERROR_WRONG_PASSWORD", "raw")
            .toAuthUserMessage()

        assertEquals("Email o contraseña incorrectos", message)
    }

    @Test
    fun email_ya_registrado_se_explica_al_usuario() {
        val message = FirebaseAuthUserCollisionException("ERROR_EMAIL_ALREADY_IN_USE", "raw")
            .toAuthUserMessage()

        assertTrue(message.contains("Ya existe una cuenta"))
    }

    @Test
    fun password_debil_indica_el_minimo_exigido() {
        val message = FirebaseAuthWeakPasswordException("ERROR_WEAK_PASSWORD", "raw", "raw")
            .toAuthUserMessage()

        assertTrue(message.contains("6"))
    }
}
