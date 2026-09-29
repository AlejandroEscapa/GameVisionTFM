package es.androidtfm.gamevision.data.repository

/*
 * Plan de escritura de los índices inversos (ADR-0008).
 *
 * Lógica pura, sin Firestore: decide QUÉ se escribe y QUÉ se retira. Es exactamente
 * donde vivían dos defectos — el índice fantasma al renombrar y las escrituras a
 * medias — y se extrae aquí para poder probarla en JVM, sin emulador.
 *
 * `UserRepository` solo traduce estas decisionas a `WriteBatch`; no vuelve a decidir.
 */

/** Escritura sobre los índices inversos `email_index` y `usernames`. */
sealed interface IndexWrite {
    /** `email_index/{email}` apunta a `uid`. */
    data class LinkEmail(val email: String, val uid: String) : IndexWrite

    /** `usernames/{username}` apunta a `uid`. */
    data class LinkUsername(val username: String, val uid: String) : IndexWrite

    /** Retira `usernames/{username}` (rename). */
    data class UnlinkUsername(val username: String) : IndexWrite
}

/**
 * Email como clave de índice: minúsculas y sin bordes, igual que se guarda como campo
 * del perfil. Sin esta normalización el índice y el campo divergen.
 */
fun indexEmail(email: String): String = email.trim().lowercase()

/** Índices al crear un perfil: email y username apuntan al uid. */
fun createIndexWrites(uid: String, email: String, username: String): List<IndexWrite> = buildList {
    val key = indexEmail(email)
    if (key.isNotBlank()) add(IndexWrite.LinkEmail(key, uid))
    if (username.isNotBlank()) add(IndexWrite.LinkUsername(username, uid))
}

/**
 * Índices al actualizar el perfil.
 *
 * `newUsername == null` significa que el nombre de usuario **no se está tocando**: no se
 * escribe nada y no se retira nada. Un rename real retira el índice viejo para no dejar
 * un fantasma apuntando a un nombre que el usuario ya no posee.
 */
fun updateIndexWrites(uid: String, newUsername: String?, oldUsername: String?): List<IndexWrite> {
    if (newUsername.isNullOrBlank()) return emptyList()
    return buildList {
        if (!oldUsername.isNullOrBlank() && oldUsername != newUsername) {
            add(IndexWrite.UnlinkUsername(oldUsername))
        }
        add(IndexWrite.LinkUsername(newUsername, uid))
    }
}
