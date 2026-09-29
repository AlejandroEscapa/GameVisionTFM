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
    val imageUri: String = "",
    /** D2.2: cuenta privada; default false (perfiles antiguos sin el campo = públicos). */
    val isPrivate: Boolean = false,
    /** T2.5: Top 4 del perfil (identidad), como gameIds en orden. */
    val topGameIds: List<String> = emptyList()
) {
    companion object {
        /**
         * Construye el perfil desde un documento de Firestore (clave = uid; el
         * email es un CAMPO). Normaliza los nulos a cadena vacía para evitar el
         * clásico "null" como texto en pantalla.
         */
        fun fromMap(data: Map<String, Any?>?): UserProfile {
            if (data == null) return UserProfile()
            fun value(key: String): String = data[key]?.toString().orEmpty()
            return UserProfile(
                email = value("email"),
                nameSurname = value("nameSurname"),
                username = value("username"),
                description = value("description"),
                country = value("country"),
                imageUri = value("imageUri"),
                isPrivate = data["isPrivate"] as? Boolean ?: false,
                topGameIds = (data["topGameIds"] as? List<*>)?.map { it.toString() } ?: emptyList()
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
        "imageUri" to imageUri,
        "isPrivate" to isPrivate,
        "topGameIds" to topGameIds
    )
}
