package es.androidtfm.gamevision.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.data.catalog.rawg.RawgGameCatalog
import es.androidtfm.gamevision.retrofit.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: 
 */

/**
 * ViewModel para la búsqueda de juegos
 *
 * Contiene todos los métodos con los que se interactúa con la API de juegos.
 */

private const val TAG = "SearchViewModel"

@HiltViewModel
class SearchViewModel @Inject constructor(
    // Catálogo de dominio: la UI y este ViewModel no conocen al proveedor concreto
    private val catalog: GameCatalog
) : ViewModel() {

    /**
     * Constructor sin argumentos para previews y usos manuales (Hilt usa el primario).
     */
    constructor() : this(RawgGameCatalog(RetrofitInstance.gamesApi))

    // Estados para la búsqueda de juegos
    private val _games = MutableStateFlow<List<CatalogGame>>(emptyList())
    val games: StateFlow<List<CatalogGame>> = _games

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /** true si la última respuesta del catálogo salió de caché (F0/T0.5). */
    private val _fromCache = MutableStateFlow(false)
    val fromCache: StateFlow<Boolean> = _fromCache

    // ---- Descubrimiento (bloque B, iteración 02/10) ----------------------
    // Filas que llenan la pantalla Buscar cuando no hay término escrito.
    private val _populares = MutableStateFlow<List<CatalogGame>>(emptyList())
    val populares: StateFlow<List<CatalogGame>> = _populares

    private val _paraTi = MutableStateFlow<List<CatalogGame>>(emptyList())
    val paraTi: StateFlow<List<CatalogGame>> = _paraTi

    private val _descubre = MutableStateFlow<List<CatalogGame>>(emptyList())
    val descubre: StateFlow<List<CatalogGame>> = _descubre

    private val _cargandoDescubrimiento = MutableStateFlow(false)
    val cargandoDescubrimiento: StateFlow<Boolean> = _cargandoDescubrimiento

    private var descubrimientoCargado = false

    /**
     * Carga las tres filas de descubrimiento una sola vez por instancia.
     *
     * @param generosFavoritos géneros del perfil (onboarding): alimentan «Para ti».
     *   Si vienen vacíos, esa fila se rellena con las novedades del catálogo, de
     *   modo que una cuenta nueva también ve la pantalla llena.
     */
    fun cargarDescubrimiento(generosFavoritos: List<String>) {
        if (descubrimientoCargado) return
        descubrimientoCargado = true
        viewModelScope.launch {
            _cargandoDescubrimiento.value = true
            // Semilla del día: cada jornada una sorpresa distinta y estable.
            val semilla = java.util.Calendar.getInstance()
                .let { it.get(java.util.Calendar.YEAR) * 1000L + it.get(java.util.Calendar.DAY_OF_YEAR) }

            // Las tres en paralelo: son independientes y así la pantalla llena antes.
            val populares = async { catalog.popular() }
            val paraTi = async { catalog.byGenres(generosFavoritos) }
            val descubre = async { catalog.discover(semilla) }

            _populares.value = populares.await().getOrDefault(emptyList())
            _paraTi.value = paraTi.await().getOrDefault(emptyList())
            _descubre.value = descubre.await().getOrDefault(emptyList())
            _cargandoDescubrimiento.value = false
        }
    }

    /** Reintento manual del descubrimiento (el usuario puede pedirlo desde la UI). */
    fun reintentarDescubrimiento(generosFavoritos: List<String>) {
        descubrimientoCargado = false
        cargarDescubrimiento(generosFavoritos)
    }

    // Estado para almacenar múltiples juegos por ID
    private val _gamesMap = mutableStateMapOf<Int, CatalogGame>()
    val gamesMap: SnapshotStateMap<Int, CatalogGame> = _gamesMap

    // Estados para los detalles del juego
    private val _gameDetails = MutableStateFlow<CatalogGame?>(null)
    val gameDetails: StateFlow<CatalogGame?> = _gameDetails

    private val _isLoadingDetails = MutableStateFlow(false)
    val isLoadingDetails: StateFlow<Boolean> = _isLoadingDetails

    private val _errorDetails = MutableStateFlow<String?>(null)
    val errorDetails: StateFlow<String?> = _errorDetails

    /**
     * Función para buscar juegos.
     * @param query: Término de búsqueda para encontrar juegos.
     */
    fun fetchGames(query: String) {
        viewModelScope.launch {
            _isLoading.value = true // Indica que la carga ha comenzado
            _error.value = null    // Limpia cualquier error previo

            // Limpia la consulta eliminando espacios innecesarios y comillas
            val cleanedQuery = query.trim().replace("\"", "")

            catalog.search(cleanedQuery)
                .onSuccess { games ->
                    _games.value = games
                    _fromCache.value = catalog.isServingFromCache()
                }
                .onFailure {
                    _error.value = it.toCatalogUserMessage()
                    _fromCache.value = false
                }
            _isLoading.value = false
        }
    }

    /**
     * Función para obtener detalles del juego por ID.
     * @param gameId: ID del juego del cual se desean obtener los detalles.
     */
    fun fetchGameDetails(gameId: Int) {
        viewModelScope.launch {
            _isLoadingDetails.value = true // Indica que la carga ha comenzado
            _errorDetails.value = null     // Limpia cualquier error previo

            catalog.getDetails(gameId)
                .onSuccess { game ->
                    _gameDetails.value = game
                    _fromCache.value = catalog.isServingFromCache()
                }
                .onFailure {
                    _errorDetails.value = it.toCatalogUserMessage()
                    _fromCache.value = false
                }
            _isLoadingDetails.value = false
        }
    }

    /**
     * Función para obtener detalles de un juego y almacenarlos en el mapa.
     * @param gameId: ID del juego del cual se desean obtener los detalles.
     */
    suspend fun fetchAndStoreGameDetails(gameId: Int) {
        viewModelScope.launch {
            _isLoadingDetails.value = true // Indica que la carga ha comenzado
            catalog.getDetails(gameId)
                .onSuccess { game -> game?.let { _gamesMap[gameId] = it } }
                .onFailure { Log.w(TAG, "No se pudo cachear el juego $gameId: ${it.message}") }
            _isLoadingDetails.value = false
        }
    }

    /**
     * Función para eliminar un juego del mapa.
     * @param gameId: ID del juego que se desea eliminar.
     */
    fun removeGame(gameId: Int) {
        gamesMap.remove(gameId) // Elimina el juego del mapa
    }
}

/**
 * Traduce los fallos del catálogo a un mensaje claro para el usuario.
 * Nunca se muestra el detalle técnico (antes salía, p. ej.,
 * 'Unable to resolve host "api.rawg.io"' — hallazgo H1 de la auditoría de F0).
 */
private fun Throwable.toCatalogUserMessage(): String =
    if (this is java.io.IOException || cause is java.io.IOException) {
        "Sin conexión. Comprueba tu red e inténtalo de nuevo."
    } else {
        "No se pudo cargar la información. Inténtalo de nuevo."
    }