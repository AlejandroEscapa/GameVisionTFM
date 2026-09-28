package es.androidtfm.gamevision.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.session.SessionRepository
import es.androidtfm.gamevision.data.session.toAuthUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Named

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (Frente 1 — Credential Manager + session SSOT)
 * Descripción:
 *
 * Inicio de sesión con Google mediante Credential Manager. La autenticación
 * real la delega en SessionRepository, de modo que el login con Google alimenta
 * la MISMA fuente de verdad de sesión que el login con email/password.
 */

@HiltViewModel
class GoogleViewModel @Inject constructor(
    private val credentialManager: CredentialManager,
    private val sessionRepository: SessionRepository,
    @param:Named("webClientId") private val webClientId: String
) : ViewModel() {

    companion object {
        private const val TAG = "GoogleViewModel"
    }

    // Estado del inicio de sesión (LiveData para la observación desde la Activity)
    sealed class SignInState {
        object Idle : SignInState()
        object Loading : SignInState()
        data class Success(val userUid: String) : SignInState()
        data class Error(val message: String) : SignInState()
    }

    private val _signInState = MutableLiveData<SignInState>(SignInState.Idle)
    val signInState: LiveData<SignInState> get() = _signInState

    /** IdToken de Google obtenido, expuesto por si se necesita (p. ej. backend). */
    private val _idToken = MutableStateFlow<String?>(null)
    val idToken: StateFlow<String?> = _idToken

    /**
     * Muestra el selector de cuentas de Google y autentica en la sesión.
     * El resultado se refleja en signInState y, al tener éxito, en la sesión.
     */
    fun signIn(context: Context) {
        viewModelScope.launch {
        _signInState.value = SignInState.Loading
        try {
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(
                    GetGoogleIdOption.Builder()
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(true)
                        .build()
                )
                .build()

            val response = credentialManager.getCredential(context, request)
            handleCredential(response.credential)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Inicio de sesión cancelado por el usuario")
            _signInState.value = SignInState.Idle
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Error obteniendo credencial: ${e.message}")
            _signInState.value = SignInState.Error(e.toAuthUserMessage())
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado: ${e.message}")
            _signInState.value = SignInState.Error(e.toAuthUserMessage())
        }
        }
    }

    private suspend fun handleCredential(credential: androidx.credentials.Credential) {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            _idToken.value = googleCredential.idToken

            sessionRepository.signInWithGoogle(googleCredential.idToken)
                .onSuccess {
                    _signInState.value = SignInState.Success(googleCredential.uniqueId)
                    Log.d(TAG, "Sesión de Google iniciada correctamente")
                }
                .onFailure {
                    Log.e(TAG, "Error autenticando con Firebase: ${it.message}")
                    _signInState.value = SignInState.Error(it.toAuthUserMessage())
                }
        } else {
            _signInState.value = SignInState.Error("Tipo de credencial no reconocido")
        }
    }

    /** Limpia el estado de credenciales de Google (al cerrar sesión). */
    suspend fun clearCredentialState() {
        runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
            .onFailure { Log.d(TAG, "No se pudo limpiar el estado de credenciales: ${it.message}") }
        _signInState.value = SignInState.Idle
        _idToken.value = null
    }
}
