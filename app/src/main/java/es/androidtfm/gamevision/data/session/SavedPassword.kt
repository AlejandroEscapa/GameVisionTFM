package es.androidtfm.gamevision.data.session

/**
 * Credencial de contraseña recuperada del gestor de contraseñas del sistema
 * (Credential Manager), usada para autocompletar el formulario de login.
 */
data class SavedPassword(val email: String, val password: String)
