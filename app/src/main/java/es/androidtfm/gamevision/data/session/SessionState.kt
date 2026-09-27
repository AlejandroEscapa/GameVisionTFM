package es.androidtfm.gamevision.data.session

/*
 * Estado de sesión de la aplicación — ÚNICA fuente de verdad de "quién ha
 * iniciado sesión". Antes esto estaba repartido entre formFields["email"],
 * FirebaseAuth.currentUser y userViewModel.isGuest (14 + 6 + 38 usos).
 */

sealed interface SessionState {

    /** Nadie ha iniciado sesión: hay que mostrar login/registro. */
    data object Anonymous : SessionState

    /** Sesión de invitado explícita (sin cuenta). Se persiste entre reinicios. */
    data object Guest : SessionState

    /**
     * Sesión iniciada.
     * @param uid identificador de Firebase Authentication.
     * @param email email del usuario (clave del documento de perfil en Firestore).
     * @param isGoogle true si la sesión viene de Google Sign-In.
     */
    data class LoggedIn(
        val uid: String,
        val email: String,
        val isGoogle: Boolean = false
    ) : SessionState
}
