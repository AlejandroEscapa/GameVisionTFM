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

    @GET("games/{id}") // La ruta incluye el parámetro {id}
    suspend fun getGameDetails(
        @Path("id") gameId: Int, // El ID del juego se pasa como parámetro
        @Query("key") key: String = BuildConfig.RAWG_API_KEY // La clave de API
    ): Game // Asegúrate de que 'Game' sea el modelo correcto para los detalles del juego
}