package es.androidtfm.gamevision.retrofit

import es.androidtfm.gamevision.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción:
 */

interface GameApiService {
    @GET("games")
    suspend fun searchGames(
        @Query("search") search: String,
        @Query("key") key: String = BuildConfig.RAWG_API_KEY
    ): ApiResponse

    /**
     * Listado de catálogo sin término de búsqueda (bloque B de la iteración
     * 02/10): alimenta las filas de descubrimiento de la pantalla Buscar.
     *
     * @param ordering criterio de RAWG: `-added` (populares), `-rating` (mejor
     *   valorados), `-released` (novedades).
     * @param genresSlugs géneros separados por coma (p. ej. `rpg,indie`).
     * @param dates rango de lanzamiento `YYYY-MM-DD,YYYY-MM-DD`.
     * @param page página (el "aleatorio" diario varía la página con una semilla).
     */
    @GET("games")
    suspend fun discoverGames(
        @Query("ordering") ordering: String,
        @Query("genres") genresSlugs: String? = null,
        @Query("dates") dates: String? = null,
        @Query("metacritic") metacritic: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 12,
        @Query("key") key: String = BuildConfig.RAWG_API_KEY
    ): ApiResponse

    @GET("games/{id}") // La ruta incluye el parámetro {id}
    suspend fun getGameDetails(
        @Path("id") gameId: Int, // El ID del juego se pasa como parámetro
        @Query("key") key: String = BuildConfig.RAWG_API_KEY // La clave de API
    ): Game // Asegúrate de que 'Game' sea el modelo correcto para los detalles del juego
}