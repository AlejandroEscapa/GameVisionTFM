package es.androidtfm.gamevision.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/01/2025
 * Descripción:
 */

/**
 * ViewModel para la gestión de la API de Google
 *
 * Contiene todos los metodos con los que se interactua con esta API.
 * Usa Jetpack Credential Manager (las APIs "One Tap" de Identity están deprecadas).
 */

class GoogleViewModel : ViewModel() {

    companion object {
        private const val TAG = "GoogleViewModel"
    }

    // Gestor de credenciales de Jetpack (se crea en initializeGoogleSignIn)
    private var credentialManager: CredentialManager? = null

    // Instancia de FirebaseAuth para la autenticación con Firebase
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // ID del cliente web para la autenticación con Google
    private var webClientId: String = "" // Se almacena el webClientId

    // Estado del inicio de sesión con Google (LiveData)
    private val _signInState = MutableLiveData<SignInState>()
    val signInState: LiveData<SignInState> get() = _signInState

    // Estados posibles del inicio de sesión
    sealed class SignInState {
        object Idle : SignInState()         // Estado inicial o inactivo
        object Loading : SignInState()      // Proceso de carga
        data class Success(val userUid: String) : SignInState()  // Inicio de sesión exitoso
        data class Error(val message: String) : SignInState()      // Error en el inicio de sesión
    }

    /**
     * Inicializa el inicio de sesión con Google.
     *
     * @param context Contexto para crear el CredentialManager.
     * @param webClientId ID del cliente web para la autenticación con Google.
     */
    fun initializeGoogleSignIn(context: Context, webClientId: String) {
        this.webClientId = webClientId
        credentialManager = CredentialManager.create(context)
    }

    /**
     * Inicia el proceso de inicio de sesión con Google.
     * Muestra el selector de cuentas (bottom sheet) y espera la credencial.
     *
     * @param context Contexto de la actividad.
     */
    suspend fun signIn(context: Context) {
        _signInState.value = SignInState.Loading

        try {
            val manager = credentialManager
                ?: throw IllegalStateException("CredentialManager no inicializado")

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false) // Permite cuentas no autorizadas
                        .setAutoSelectEnabled(true) // Habilita la selección automática de la cuenta
                        .build()
                )
                .build()

            val response = manager.getCredential(context, request)
            handleCredential(response.credential)
        } catch (e: GetCredentialCancellationException) {
            // El usuario cerró el selector de cuentas: no es un error
            Log.d(TAG, "Inicio de sesión cancelado por el usuario")
            _signInState.value = SignInState.Idle
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error obteniendo credencial: ${e.message}")
            _signInState.value = SignInState.Error("Error obteniendo credencial: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}")
            _signInState.value = SignInState.Error("Error inesperado: ${e.message}")
        }
    }

    /**
     * Extrae el token de ID de Google de la credencial obtenida.
     */
    private fun handleCredential(credential: Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdCredential.idToken)
        } else {
            _signInState.value = SignInState.Error("Tipo de credencial no reconocido")
        }
    }

    /**
     * Autentica con Firebase usando el token de ID de Google.
     *
     * @param idToken Token de ID de Google.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "Autenticando con Firebase...")
        val credential = GoogleAuthProvider.getCredential(idToken, null) // Crea la credencial de Firebase
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        Log.d(TAG, "Autenticación exitosa")
                        _signInState.value = SignInState.Success(it.uid) // Notifica el éxito
                    } ?: run {
                        Log.e(TAG, "Error: Usuario de Firebase es nulo")
                        _signInState.value = SignInState.Error("Usuario de Firebase es nulo")
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error en autenticación con Firebase"
                    Log.e(TAG, errorMessage)
                    _signInState.value = SignInState.Error(errorMessage)
                }
            }
    }

    /**
     * Cierra la sesión del usuario en Firebase y limpia el estado de credenciales de Google.
     *
     * @param onSuccess Callback que se ejecuta si el cierre de sesión es exitoso.
     * @param onError Callback que se ejecuta si ocurre un error.
     */
    suspend fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            // Cerrar sesión en Firebase
            firebaseAuth.signOut()

            // Limpiar el estado de credenciales de Google (Credential Manager)
            credentialManager?.clearCredentialState(ClearCredentialStateRequest())

            Log.d(TAG, "Sesión cerrada correctamente")
            _signInState.postValue(SignInState.Idle) // Restablece el estado a Idle
            onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Error general en logout: ${e.message}")
            _signInState.postValue(SignInState.Error(e.message ?: "Error general en logout"))
            onError(e.message ?: "Error general en logout")
        }
    }
}
