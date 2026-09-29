package es.androidtfm.gamevision.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.repository.UserRepository
import es.androidtfm.gamevision.data.session.SavedPassword
import es.androidtfm.gamevision.data.session.SessionRepository
import es.androidtfm.gamevision.data.session.SessionState
import es.androidtfm.gamevision.data.session.toAuthUserMessage
import es.androidtfm.gamevision.data.storage.ProfileImageStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 27/09/2026 (Frente 2 — SSOT de sesión y perfil)
 * Descripción:
 *
 * Punto único de la identidad y del perfil del usuario para toda la UI.
 *
 * Antes: la identidad viajaba en formFields["email"], en FirebaseAuth.currentUser
 * y en un flag en memoria, y el perfil se copiaba a mano de pantalla en pantalla.
 * Ahora: `session` (de SessionRepository) dice quién ha iniciado sesión y
 * `profile` es un flujo en vivo del documento de Firestore — todas las pantallas
 * leen de aquí y se actualizan solas.
 *
 * `formFields` queda reducido a lo que debe ser: estado de entrada del formulario.
 */

@HiltViewModel
class UserViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val profileImageStorage: ProfileImageStorage
) : ViewModel() {

    private val emptyForm = mutableMapOf(
        "username" to "",
        "password" to "",
        "confirmPassword" to "",
        "country" to "",
        "email" to "",
        "nameSurname" to "",
        "description" to ""
    )

    // ---- Estado de formulario (solo entrada de datos) ----
    private val _formFields = MutableStateFlow<Map<String, String>>(emptyForm.toMap())
    val formFields: StateFlow<Map<String, String>> = _formFields.asStateFlow()

    // ---- SSOT de sesión ----
    /** Estado de sesión de la app (única fuente de verdad). */
    val session: StateFlow<SessionState> = sessionRepository.sessionState

    /** Email del usuario autenticado, o null si es invitado/anónimo. */
    val currentEmail: StateFlow<String?> = session
        .map { (it as? SessionState.LoggedIn)?.email }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** UID de Firebase Authentication del usuario (clave de la biblioteca), o null. */
    val currentUid: StateFlow<String?> = session
        .map { (it as? SessionState.LoggedIn)?.uid }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** true solo en modo invitado explícito. */
    val isGuest: StateFlow<Boolean> = session
        .map { it is SessionState.Guest }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ---- SSOT del perfil: flujo en vivo ----
    /**
     * Perfil del usuario actual. Se re-suscribe automáticamente al cambiar de
     * usuario y refleja los cambios de Firestore sin refetch manual.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: StateFlow<UserProfile> = currentEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(Result.success(UserProfile()))
            else userRepository.observeProfile(email)
        }
        .map { result -> result.getOrElse { UserProfile() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())

    /** Último error de carga del perfil (para poder mostrarlo). */
    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    // ---- Estado de UI ----
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    // ---- Foto de perfil (ADR-0007: guardada en Firestore; se sirve como data URI) ----
    /** Cada incremento fuerza recargar la foto tras una subida. */
    private val imageRefresh = MutableStateFlow(0)

    /** Foto del usuario actual como data URI, o null si no tiene. Se recarga al cambiar de usuario o tras subir. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val profileImageData: StateFlow<String?> = combine(currentUid, imageRefresh) { uid, _ -> uid }
        .flatMapLatest { uid ->
            flow {
                emit(
                    if (uid.isNullOrBlank()) null
                    else profileImageStorage.loadProfileImageDataUri(uid).getOrNull()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Error de la última operación con la foto de perfil (lo muestra el Snackbar del perfil). */
    private val _imageError = MutableStateFlow<String?>(null)
    val imageError: StateFlow<String?> = _imageError.asStateFlow()

    fun clearImageError() {
        _imageError.value = null
    }

    private fun setImageError(msg: String) {
        _imageError.value = msg
    }

    // ------------------------------------------------------------------------
    // Acciones de sesión
    // ------------------------------------------------------------------------

    /**
     * Registro con email/password.
     *
     * Se ejecuta en viewModelScope (NO en el scope de la pantalla): al crearse la
     * cuenta cambia la sesión y la navegación saca la pantalla de composición; si
     * la corrutina viviera en la pantalla, se cancelaría el registro a medias.
     */
    fun signUp(email: String, password: String, confirmPassword: String) {
        if (!isRegisterFormValid()) {
            setMessage("Por favor, completa todos los campos")
            return
        }
        if (password != confirmPassword) {
            setMessage("Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            sessionRepository.signUp(
                email = email,
                password = password,
                nameSurname = _formFields.value["nameSurname"].orEmpty(),
                username = _formFields.value["username"].orEmpty()
            ).onFailure { setMessage(it.toAuthUserMessage()) }
            _isLoading.value = false
        }
    }

    /**
     * Inicio de sesión con email/password. Si va bien, ofrece guardar la
     * credencial en el gestor de contraseñas del sistema.
     */
    fun signIn(context: Context, email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            setMessage("Por favor, completa todos los campos")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            val result = sessionRepository.signIn(email, password)
            if (result.isSuccess) {
                sessionRepository.offerPasswordSave(context, email, password)
            } else {
                setMessage(result.exceptionOrNull()?.toAuthUserMessage() ?: "No se pudo iniciar sesión")
            }
            _isLoading.value = false
        }
    }

    /** Inicio de sesión con Google usando el idToken de Credential Manager. */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            sessionRepository.signInWithGoogle(idToken)
                .onFailure { setMessage(it.toAuthUserMessage()) }
            _isLoading.value = false
        }
    }

    /** Envía el correo de recuperación de contraseña (el resultado se muestra en `message`). */
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            setMessage("Introduce tu correo electrónico")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            sessionRepository.resetPassword(email)
                .onSuccess { setMessage("Te hemos enviado un correo para restablecer la contraseña") }
                .onFailure { setMessage(it.toAuthUserMessage()) }
            _isLoading.value = false
        }
    }

    /** Entra como invitado (persistido). */
    suspend fun enterAsGuest() = sessionRepository.enterAsGuest()

    /** Cierra la sesión y limpia el estado local. */
    suspend fun signOut() {
        sessionRepository.signOut()
        clearUserData()
    }

    // ------------------------------------------------------------------------
    // Credential Manager
    // ------------------------------------------------------------------------

    /** Credencial guardada para autocompletar el formulario de login. */
    suspend fun retrieveSavedPassword(context: Context): SavedPassword? =
        sessionRepository.retrieveSavedPassword(context)

    // ------------------------------------------------------------------------
    // Perfil
    // ------------------------------------------------------------------------

    /** Actualiza campos del perfil del usuario actual (en viewModelScope). */
    fun updateProfile(fields: Map<String, Any?>, onSuccess: () -> Unit = {}) {
        val email = currentEmail.value
        if (email.isNullOrBlank()) {
            setMessage("No hay sesión iniciada")
            return
        }
        viewModelScope.launch {
            userRepository.updateProfile(email, fields)
                .onSuccess {
                    setMessage("Perfil actualizado")
                    onSuccess()
                }
                .onFailure { setMessage("No se pudo actualizar el perfil") }
        }
    }

    /**
     * Actualiza la foto de perfil del usuario actual (F0/T0.12): sube la imagen
     * elegida a Firebase Storage y persiste su URL de descarga en el perfil.
     * Todo en viewModelScope (si la pantalla sale de composición a mitad, no se cancela).
     */
    fun updateProfileImage(uri: android.net.Uri) {
        val uid = currentUid.value
        val email = currentEmail.value
        if (uid.isNullOrBlank() || email.isNullOrBlank()) {
            setImageError("No hay sesión iniciada")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            profileImageStorage.uploadProfileImage(uid, uri)
                .onSuccess { pointer ->
                    // El perfil guarda solo un puntero; la imagen vive en profile_images/{uid}.
                    userRepository.updateProfileImage(email, pointer)
                        .onFailure { setImageError("No se pudo actualizar la foto de perfil") }
                    imageRefresh.value += 1
                }
                .onFailure { setImageError(it.message ?: "No se pudo actualizar la foto de perfil") }
            _isLoading.value = false
        }
    }

    // ------------------------------------------------------------------------
    // Formulario
    // ------------------------------------------------------------------------

    fun updateFormField(key: String, value: String) {
        _formFields.value = _formFields.value.toMutableMap().apply { this[key] = value }
    }

    fun onFormFieldChange(field: String, value: String) = updateFormField(field, value)

    /** Rellena el formulario de login con credenciales recuperadas. */
    fun prefillLogin(email: String, password: String) {
        updateFormField("email", email)
        updateFormField("password", password)
    }

    fun clearFormFields() {
        _formFields.value = emptyForm.toMap()
    }

    fun clearMessage() = setMessage("")

    fun setMessage(msg: String) {
        _message.value = msg
    }

    /** Limpia los datos del usuario y los mensajes (al cerrar sesión). */
    fun clearUserData() {
        clearFormFields()
        _message.value = ""
        _profileError.value = null
        _imageError.value = null
    }

    private fun isRegisterFormValid(): Boolean =
        listOf("username", "nameSurname", "email", "password", "confirmPassword")
            .all { _formFields.value[it].orEmpty().isNotEmpty() }
}
