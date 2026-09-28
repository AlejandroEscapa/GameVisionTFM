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
                .onSuccess { _games.value = it }
                .onFailure { _error.value = "Error al cargar juegos: ${it.message}" }
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
                .onSuccess { _gameDetails.value = it }
                .onFailure { _errorDetails.value = "Error al cargar detalles: ${it.message}" }
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