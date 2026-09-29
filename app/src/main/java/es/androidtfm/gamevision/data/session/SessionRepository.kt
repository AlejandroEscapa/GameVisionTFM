package es.androidtfm.gamevision.data.session

import android.content.Context
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.repository.UserRepository
import es.androidtfm.gamevision.datastore.SessionPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (Frente 1 — autenticación real y SSOT de sesión)
 * Descripción:
 *
 * ÚNICA fuente de verdad de la sesión de la app. Antes "quién soy" estaba
 * repartido entre formFields["email"], FirebaseAuth.currentUser y un flag en
 * memoria; aquí se unifica en un StateFlow<SessionState>.
 *
 * La autenticación real la hace Firebase Authentication (las reglas de
 * Firestore ya asumen esto). El password NO se guarda nunca en Firestore.
 * Credential Manager se usa como superficie de guardado/autocompletado de
 * credenciales, según las guías de identidad de Android.
 */

@Singleton
class SessionRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferences,
    private val credentialManager: CredentialManager,
    applicationScope: CoroutineScope
) {

    companion object {
        private const val TAG = "SessionRepository"
        private const val PROVIDER_GOOGLE = "google.com"
    }

    private val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    /**
     * Estado de sesión de la aplicación — SSOT. Combina la sesión real de
     * Firebase Auth con el modo invitado persistido.
     */
    val sessionState: StateFlow<SessionState> =
        combine(authState, sessionPreferences.isGuest) { user, guest ->
            when {
                user != null -> SessionState.LoggedIn(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    isGoogle = user.isGoogleAccount()
                )
                guest -> SessionState.Guest
                else -> SessionState.Anonymous
            }
        }.stateIn(applicationScope, SharingStarted.Eagerly, SessionState.Anonymous)

    private fun FirebaseUser.isGoogleAccount(): Boolean =
        providerData.any { it.providerId == PROVIDER_GOOGLE }

    // ------------------------------------------------------------------------
    // Registro e inicio de sesión
    // ------------------------------------------------------------------------

    /**
     * Registra la cuenta en Firebase Auth y crea su perfil en Firestore.
     * Si el perfil no se puede crear, se elimina la cuenta recién creada para no
     * dejar usuarios huérfanos (consistencia entre Auth y Firestore).
     */
    suspend fun signUp(
        email: String,
        password: String,
        nameSurname: String,
        username: String
    ): Result<Unit> {
        val cleanEmail = email.trim()
        return runCatching {
            val result = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user ?: error("No se pudo crear la cuenta")

            user.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(nameSurname).build()
            ).await()

            val profile = UserProfile(
                email = cleanEmail,
                nameSurname = nameSurname,
                username = username
            )
            userRepository.createProfile(user.uid, profile).getOrElse { error ->
                Log.e(TAG, "Perfil no creado, se revierte la cuenta: ${error.message}")
                runCatching { user.delete().await() }
                throw error
            }
            Unit
        }.onFailure { Log.e(TAG, "Error en registro: ${it.message}") }
    }

    /** Inicia sesión con email y password (Firebase Authentication). */
    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
        Unit
    }.onFailure { Log.e(TAG, "Error en login: ${it.message}") }

    /** Inicia sesión con la credencial de Google (idToken de Credential Manager). */
    suspend fun signInWithGoogle(idToken: String): Result<Unit> = runCatching {
        firebaseAuth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        Unit
    }.onFailure { Log.e(TAG, "Error en login con Google: ${it.message}") }

    /** Envía el correo de recuperación de contraseña (antes era un stub). */
    suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        firebaseAuth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }.onFailure { Log.e(TAG, "Error enviando recuperación: ${it.message}") }

    /** Entra en modo invitado (persistido entre reinicios). */
    suspend fun enterAsGuest() = sessionPreferences.setGuest(true)

    /** Cierra la sesión actual. */
    suspend fun signOut() {
        sessionPreferences.setGuest(false)
        firebaseAuth.signOut()
    }

    // ------------------------------------------------------------------------
    // Credential Manager (guardado y autocompletado de credenciales)
    // ------------------------------------------------------------------------

    /**
     * Ofrece guardar la credencial en el gestor de contraseñas tras un login o
     * registro correcto. Nunca falla de forma visible: es una mejora opcional.
     */
    suspend fun offerPasswordSave(context: Context, email: String, password: String) {
        runCatching {
            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(id = email, password = password)
            )
        }.onFailure { Log.d(TAG, "No se ofreció guardar la credencial: ${it.message}") }
    }

    /**
     * Recupera la última credencial guardada para autocompletar el formulario.
     * Devuelve null si no hay ninguna o el usuario cancela.
     */
    suspend fun retrieveSavedPassword(context: Context): SavedPassword? = try {
        val response = credentialManager.getCredential(
            context = context,
            request = GetCredentialRequest.Builder()
                .addCredentialOption(androidx.credentials.GetPasswordOption())
                .build()
        )
        val credential = response.credential
        if (credential is PasswordCredential) {
            SavedPassword(credential.id, credential.password)
        } else null
    } catch (e: NoCredentialException) {
        null
    } catch (e: GetCredentialException) {
        Log.d(TAG, "Sin credenciales guardadas: ${e.message}")
        null
    }
}

/**
 * Traduce los errores de Firebase Auth a mensajes claros para el usuario.
 * La UI los muestra tal cual, sustituyendo al silencio anterior.
 */
fun Throwable.toAuthUserMessage(): String = when (this) {
    // OJO con el orden: WeakPassword y UserCollision heredan de
    // InvalidCredentials, así que deben comprobarse ANTES para no quedar
    // enmascarados por el mensaje genérico de credenciales.
    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil (mínimo 6 caracteres)"
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese email"
    is FirebaseAuthInvalidUserException -> "No existe ninguna cuenta con ese email"
    is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos"
    is IOException -> "Sin conexión. Revisa tu red e inténtalo de nuevo"
    else -> message ?: "Ha ocurrido un error inesperado"
}
