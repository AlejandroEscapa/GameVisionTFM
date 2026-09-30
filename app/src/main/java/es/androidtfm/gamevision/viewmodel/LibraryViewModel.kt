package es.androidtfm.gamevision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.hltb.HltbPlaytimes
import es.androidtfm.gamevision.data.hltb.HltbRepository
import es.androidtfm.gamevision.data.analytics.AnalyticsEvents
import es.androidtfm.gamevision.data.analytics.AnalyticsLogger
import es.androidtfm.gamevision.data.library.GameLog
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.library.LibraryStats
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.library.PlaySession
import es.androidtfm.gamevision.datastore.RecentGame
import es.androidtfm.gamevision.datastore.RecentGamesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 30/09/2026 (D-C2: partida de DDBBViewModel).
 * Descripción:
 *
 * Estado y operaciones de la biblioteca del usuario: fichas, diario de
 * sesiones, agregados (stats), duraciones estimadas (HLTB) y el historial
 * local de recientes. Los amigos y los mensajes del muro viven en
 * SocialViewModel; el perfil y la identidad, en UserViewModel (SSOT).
 *
 * Todos los métodos devuelven Result, así que la UI puede informar de los
 * fallos en vez de fallar en silencio.
 */

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val recentGamesStore: RecentGamesStore,
    private val hltbRepository: HltbRepository,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    // ------------------------------------------------------------------------
    // Biblioteca (F0-B): users/{uid}/library|logs|sessions|stats
    // ------------------------------------------------------------------------

    /** Flujo en vivo de la biblioteca del usuario (SSOT de sus fichas). */
    fun observeLibrary(uid: String): Flow<Result<List<LibraryEntry>>> =
        libraryRepository.observeLibrary(uid)

    /** Añade una ficha nueva a la biblioteca. */
    suspend fun addToLibrary(uid: String, entry: LibraryEntry): Result<Unit> =
        libraryRepository.addGame(uid, entry).onSuccess {
            analytics.log(
                AnalyticsEvents.ADD_GAME,
                mapOf("status" to entry.status.value, "favorite" to entry.favorite)
            )
        }

    /** Cambia el estado de una ficha (ajusta contadores en el mismo lote). */
    suspend fun updateStatus(
        uid: String,
        gameId: String,
        from: LibraryStatus,
        to: LibraryStatus
    ): Result<Unit> = libraryRepository.updateStatus(uid, gameId, from, to).onSuccess {
        analytics.log(
            AnalyticsEvents.STATUS_CHANGE,
            mapOf("from" to from.value, "to" to to.value)
        )
    }

    /** Fija (o borra con `rating = null`) la nota personal (F1/T1.3). */
    suspend fun setRating(
        uid: String,
        gameId: String,
        previous: Double?,
        rating: Double?
    ): Result<Unit> = libraryRepository.updateRating(uid, gameId, previous, rating).onSuccess {
        analytics.log(AnalyticsEvents.RATE_GAME, mapOf("rating" to rating))
    }

    /** Fija (o borra con `review = null`) la reseña escrita (F1/T1.4). */
    suspend fun setReview(uid: String, gameId: String, review: String?): Result<Unit> =
        libraryRepository.updateReview(uid, gameId, review).onSuccess {
            analytics.log(AnalyticsEvents.REVIEW_GAME, mapOf("has_text" to (review != null)))
        }

    /** Marca o desmarca el favorito. */
    suspend fun setFavorite(uid: String, gameId: String, favorite: Boolean): Result<Unit> =
        libraryRepository.updateFavorite(uid, gameId, favorite)

    /** Elimina una ficha (se pasa la ficha en vivo para ajustar contadores). */
    suspend fun removeFromLibrary(uid: String, entry: LibraryEntry): Result<Unit> =
        libraryRepository.removeGame(uid, entry)

    // ------------------------------------------------------------------------
    // Diario y partidas (F1/T1.5, T1.6, T1.8–T1.10)
    // ------------------------------------------------------------------------

    /** Sesiones del diario, más recientes primero (SSOT del diario). */
    fun observeSessions(uid: String): Flow<Result<List<PlaySession>>> =
        libraryRepository.observeSessions(uid)

    /** Apunta una sesión del diario (ajusta contadores en el mismo lote). */
    suspend fun addSession(uid: String, session: PlaySession): Result<Unit> =
        libraryRepository.addSession(uid, session).onSuccess {
            analytics.log(AnalyticsEvents.LOG_SESSION, mapOf("minutes" to session.minutes))
        }

    /** Crea una partida/rejugada (F1/T1.5) con su plataforma (F1/T1.6). */
    suspend fun createLog(uid: String, log: GameLog): Result<Unit> =
        libraryRepository.createLog(uid, log)

    /** Fija la plataforma de la última partida de una ficha (F1/T1.6). */
    suspend fun setLastPlatform(uid: String, gameId: String, platform: String?): Result<Unit> =
        libraryRepository.updateLastPlatform(uid, gameId, platform)

    /** Agregados de la biblioteca (stats/summary), para la pantalla de F1/T1.12. */
    fun observeStats(uid: String): Flow<Result<LibraryStats>> =
        libraryRepository.observeStats(uid)

    // ------------------------------------------------------------------------
    // Duración estimada (F1/T1.11 — HowLongToBeat, con caché)
    // ------------------------------------------------------------------------

    /**
     * Duración estimada de un juego por su nombre. Con caché de 90 días y
     * degradable: si HLTB falla, devuelve null y la UI ofrece valor manual.
     */
    suspend fun playtimesFor(name: String): HltbPlaytimes? = hltbRepository.playtimesFor(name)

    /** Fija (o borra con `minutes = null`) la duración manual de una ficha (T1.11). */
    suspend fun setManualPlaytime(uid: String, gameId: String, minutes: Int?): Result<Unit> =
        libraryRepository.updateManualPlaytime(uid, gameId, minutes).onSuccess {
            analytics.log(AnalyticsEvents.SET_MANUAL_PLAYTIME, mapOf("minutes" to minutes))
        }

    /** Registra el usuario en la analítica (solo uid; nunca email). */
    fun setAnalyticsUser(uid: String?) = analytics.setUserId(uid)

    /** Marca la pantalla actual en la analítica (embudos de navegación). */
    fun logScreen(name: String) = analytics.logScreen(name)

    // ------------------------------------------------------------------------
    // Historial local (recientes) — vive en el dispositivo
    // ------------------------------------------------------------------------

    /** Últimos juegos vistos (local; no viaja entre dispositivos). */
    val recentGames: Flow<List<RecentGame>> = recentGamesStore.recentGames

    /** Registra un juego en el historial local. */
    fun addRecentGame(game: RecentGame) {
        viewModelScope.launch { recentGamesStore.add(game) }
    }
}
