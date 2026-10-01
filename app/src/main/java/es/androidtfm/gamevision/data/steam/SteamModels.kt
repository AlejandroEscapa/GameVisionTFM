package es.androidtfm.gamevision.data.steam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/*
 * Cliente del proxy Steam (Cloudflare Worker, steam-worker/).
 *
 * La app NUNCA habla con la Steam Web API directamente (la clave no puede
 * viajar con la app, ToS de Valve): habla con el worker, que inyecta la clave
 * en el servidor. El parsing es tolerante (defaults en todo) para que una
 * respuesta inesperada degrade a lista vacía, nunca a crash.
 */
interface SteamProxyService {

    /** Biblioteca + horas del jugador (GetOwnedGames a través del proxy). */
    @GET("steam/owned")
    suspend fun owned(
        @Query("steamid") steamId: String
    ): SteamOwnedResponse
}

@Serializable
data class SteamOwnedResponse(
    val steamId: String = "",
    @SerialName("totalGames") val totalGames: Int = 0,
    val games: List<SteamGame> = emptyList()
)

@Serializable
data class SteamGame(
    val appid: Long = 0,
    val name: String = "",
    @SerialName("playtimeForever") val playtimeForever: Int = 0,  // minutos totales
    @SerialName("playtime2weeks") val playtime2weeks: Int = 0,    // minutos últimas 2 semanas
    @SerialName("imgIconUrl") val imgIconUrl: String = "",
    @SerialName("rtimeLastPlayed") val rtimeLastPlayed: Long = 0   // epoch segundos
)
