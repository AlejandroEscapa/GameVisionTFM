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
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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

    /**
     * true cuando hay una búsqueda en curso o resuelta (por teclado o por la lupa).
     * Lo lleva el ViewModel y no la pantalla: con la búsqueda en vivo, el término
     * llega en cada carácter y la UI no puede decidir por su cuenta cuándo dejar de
     * enseñar el descubrimiento.
     */
    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched

    /** Job de la búsqueda en vivo: se cancela con cada carácter nuevo. */
    private var liveSearchJob: Job? = null

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
     * Búsqueda EN VIVO de la pantalla Buscar (iteración 02/10, segunda vuelta).
     *
     * Antes había que pulsar la lupa para que pasara algo: escribir y no ver nada
     * se siente roto en 2026. Este método se llama en CADA carácter y espera
     * [debounceMs] desde el último, así que teclear «zelda» no dispara 5 consultas
     * al catálogo.
     *
     * Convenios (los mismos que el buscador del Top 4, para que se comporten igual):
     *  · Menos de [minChars] caracteres no toca la red: se limpia y se vuelve al
     *    descubrimiento (peor caso, una búsqueda real de 1 letra devuelve relleno
     *    y gasta cuota). Al limpiar el campo se sale del modo búsqueda.
     *  · `hasSearched` es del ViewModel, no de la pantalla: así el descubrimiento
     *    y los resultados no se pelean cuando el término llega por teclado.
     */
    fun buscarEnVivo(
        query: String,
        debounceMs: Long = 350L,
        minChars: Int = 2
    ) {
        liveSearchJob?.cancel()
        val limpio = query.trim().replace("\"", "")
        if (limpio.length < minChars) {
            _games.value = emptyList()
            _error.value = null
            _fromCache.value = false
            _isLoading.value = false
            _hasSearched.value = false
            return
        }
        liveSearchJob = viewModelScope.launch {
            delay(debounceMs)
            _isLoading.value = true
            _error.value = null
            _hasSearched.value = true
            catalog.search(limpio)
                .onSuccess { games ->
                    _games.value = games
                    _fromCache.value = catalog.isServingFromCache()
                }
                .onFailure {
                    _games.value = emptyList()
                    _fromCache.value = false
                    _error.value = it.toCatalogUserMessage()
                }
            _isLoading.value = false
        }
    }

    /** Limpia el estado del buscador en vivo (al salir de la pantalla). */
    fun limpiarBusquedaEnVivo() {
        liveSearchJob?.cancel()
        _games.value = emptyList()
        _error.value = null
        _fromCache.value = false
        _isLoading.value = false
        _hasSearched.value = false
    }

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
            // Vaciar el campo devuelve la pantalla al descubrimiento, no a un
            // «no hay resultados» que no ha pedido nadie.
            _hasSearched.value = cleanedQuery.isNotBlank()
            if (cleanedQuery.isBlank()) {
                _games.value = emptyList()
                _isLoading.value = false
                return@launch
            }

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

    // ---- Buscador del Top 4 (revisión 02/10) ----------------------------
    // Canal INDEPENDIENTE del buscador de la pantalla Buscar: si compartiera
    // `games`, escribir en el Top 4 cambiaría los resultados de la otra pantalla.
    // Con debounce, porque es un campo que se escribe letra a letra y cada tecla
    // sería una llamada al catálogo.
    private val _topSearchResults = MutableStateFlow<List<CatalogGame>>(emptyList())
    val topSearchResults: StateFlow<List<CatalogGame>> = _topSearchResults

    private val _topSearchLoading = MutableStateFlow(false)
    val topSearchLoading: StateFlow<Boolean> = _topSearchLoading

    private val _topSearchError = MutableStateFlow<String?>(null)
    val topSearchError: StateFlow<String?> = _topSearchError

    private var topSearchJob: Job? = null

    /**
     * Busca juegos para el Top 4, esperando [debounceMs] desde la última tecla.
     *
     * Con menos de 2 caracteres no se llama a la red (RAWG devuelve relleno y se
     * gasta cuota para nada) y se limpian los resultados.
     *
     * @param minChars mínimo de caracteres para disparar la búsqueda.
     */
    fun buscarParaTop(
        query: String,
        debounceMs: Long = 350L,
        minChars: Int = 2
    ) {
        topSearchJob?.cancel()
        val limpio = query.trim().replace("\"", "")
        if (limpio.length < minChars) {
            _topSearchResults.value = emptyList()
            _topSearchError.value = null
            _topSearchLoading.value = false
            return
        }
        topSearchJob = viewModelScope.launch {
            delay(debounceMs)
            _topSearchLoading.value = true
            _topSearchError.value = null
            catalog.search(limpio)
                .onSuccess { _topSearchResults.value = it }
                .onFailure {
                    _topSearchResults.value = emptyList()
                    _topSearchError.value = it.toCatalogUserMessage()
                }
            _topSearchLoading.value = false
        }
    }

    /** Limpia el estado del buscador del Top 4 (al cerrarlo o cerrar sesión). */
    fun limpiarBusquedaTop() {
        topSearchJob?.cancel()
        _topSearchResults.value = emptyList()
        _topSearchError.value = null
        _topSearchLoading.value = false
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