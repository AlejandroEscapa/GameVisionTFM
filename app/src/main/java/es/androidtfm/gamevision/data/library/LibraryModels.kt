package es.androidtfm.gamevision.data.library

/*
 * Modelos de la biblioteca (F0-B), según la propuesta aprobada el 28/09/2026
 * (docs/plan/F0B-propuesta-modelo-datos.md):
 *
 *  - Ficha  → LibraryEntry  (una por juego)
 *  - Partida → GameLog      (una por run/rejugada)
 *  - Sesión  → PlaySession  (una por sesión del diario)
 *  - Agregados → LibraryStats (documento de contadores)
 *
 * Nota: estos modelos son de dominio; el mapeo a Firestore vive en LibraryRepository.
 */

/**
 * Estados de una ficha, con sus valores estables para Firestore.
 * La UI traduce `value` a etiquetas en español.
 */
enum class LibraryStatus(val value: String) {
    PLAYING("jugando"),
    COMPLETED("completado"),
    COLLECTED("coleccionado"),
    PAUSED("en_pausa"),
    RETIRED("retirado"),
    ABANDONED("abandonado"),
    WISHED("deseado");

    companion object {
        /** Devuelve el estado a partir de su valor en Firestore (o null si no existe). */
        fun fromValue(value: String?): LibraryStatus? =
            entries.firstOrNull { it.value == value }

        /**
         * Mapeo de las listas antiguas → estado, usado por la migración (T0.11).
         * Decisión aprobada 28/09/2026: "Juegos jugados" entra como `jugando`.
         * (`favorites` no es un estado: se migra al flag `favorite`; `history` se descarta).
         */
        fun fromLegacyList(collection: String): LibraryStatus? = when (collection) {
            "playedlist" -> PLAYING
            "wishlist" -> WISHED
            else -> null
        }
    }
}

/** Ficha de un juego en la biblioteca del usuario (`users/{uid}/library/{gameId}`). */
data class LibraryEntry(
    val gameId: String,
    val status: LibraryStatus,
    /** Nota personal: 0,5–5,0 en pasos de 0,5 (medias estrellas); null = sin nota. */
    val rating: Double? = null,
    val review: String? = null,
    val favorite: Boolean = false,
    /** Plataforma de la última partida (copia para filtros sin joins). */
    val lastPlatform: String? = null,
    val addedAt: Long? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    // Instantánea del catálogo: permite pintar la biblioteca sin red y sin pedir a RAWG.
    val name: String = "",
    val coverUrl: String? = null,
    val released: String = "",
    val genres: List<String> = emptyList(),
    /** Minutos jugados acumulados (contador; la fuente detallada son las sesiones). */
    val minutesTotal: Int = 0,
)

/** Partida de un juego (`users/{uid}/logs/{logId}`): una por rejugada. */
data class GameLog(
    val id: String = "",
    val gameId: String,
    val runIndex: Int,
    val startedOn: Long? = null,
    val finishedOn: Long? = null,
    val platform: String? = null,
    /** Horas "de bolsillo" si el usuario no apunta sesiones. */
    val minutes: Int? = null,
    val note: String? = null,
)

/** Sesión del diario (`users/{uid}/sessions/{sessionId}`). */
data class PlaySession(
    val id: String = "",
    val gameId: String,
    val logId: String? = null,
    /** Día jugado (elegido por el usuario; por defecto hoy). Ordena el diario. */
    val date: Long,
    val minutes: Int,
    val note: String? = null,
)

/** Agregados de la biblioteca (`users/{uid}/stats/summary`). */
data class LibraryStats(
    val gamesTotal: Int = 0,
    val gamesByStatus: Map<String, Int> = emptyMap(),
    val minutesTotal: Int = 0,
    val sessionsTotal: Int = 0,
    val ratingSum: Double = 0.0,
    val ratingCount: Int = 0,
    val lastActiveAt: Long? = null,
)
