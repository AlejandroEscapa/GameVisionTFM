package es.androidtfm.gamevision.data.model

/*
 * Modelo de dominio del perfil de usuario (SSOT de datos de perfil).
 *
 * Reemplaza al antiguo User.kt (que guardaba el password y no se usaba) y al
 * uso de HashMap<String, String> suelto por las pantallas.
 *
 * El password NUNCA forma parte del perfil: vive en Firebase Authentication.
 */

data class UserProfile(
    val email: String = "",
    val nameSurname: String = "",
    val username: String = "",
    val description: String = "",
    val country: String = "",
    val imageUri: String = ""
) {
    companion object {
        /**
         * Construye el perfil desde un documento de Firestore, normalizando los
         * nulos a cadena vacía (evita el clásico "null" como texto en pantalla).
         */
        fun fromMap(email: String, data: Map<String, Any?>?): UserProfile {
            if (data == null) return UserProfile(email = email)
            fun value(key: String): String = data[key]?.toString().orEmpty()
            return UserProfile(
                email = email,
                nameSurname = value("nameSurname"),
                username = value("username"),
                description = value("description"),
                country = value("country"),
                imageUri = value("imageUri")
            )
        }
    }

    /**
     * Campos editables que se persisten en Firestore (sin email ni password).
     */
    fun toEditableMap(): HashMap<String, Any> = hashMapOf(
        "nameSurname" to nameSurname,
        "username" to username,
        "description" to description,
        "country" to country,
        "imageUri" to imageUri
    )
}
