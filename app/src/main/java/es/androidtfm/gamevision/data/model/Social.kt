package es.androidtfm.gamevision.data.model

/*
 * Modelos de dominio de las relaciones sociales del usuario.
 * Sustituyen a los Map<String, Any> que las pantallas manipulaban a mano.
 */

data class Friend(
    val uid: String,
    /** Nombre visible; puede ser vacío si el perfil aún no se ha resuelto. */
    val nameSurname: String = "",
    val username: String
)

data class ChatMessage(
    val id: String,
    val text: String,
    val time: String,
    /** uid del autor del mensaje (el propio usuario o un amigo). */
    val ownerUid: String
)
