package es.androidtfm.gamevision.retrofit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/*
 * Precios (iteración 02/10, Home «Te está esperando»).
 *
 * CheapShark: gratis, sin clave y con reglas claras (redirige a la tienda, no
 * se cachea su catálogo) — la opción recomendada en la investigación de APIs
 * (D-NH2). ITAD queda para cuando haya clave de publicación.
 *
 * El mapeo es tolerante (defaults en todos los campos): si CheapShark cambia
 * algo, la sección se degrada a vacío en vez de romper la Home.
 */
interface CheapSharkService {

    /**
     * Ofertas ordenadas por precio. `title` filtra por nombre aproximado
     * (lo usamos con los juegos deseados de la biblioteca).
     */
    @GET("deals")
    suspend fun deals(
        @Query("title") title: String? = null,
        @Query("upperPrice") upperPrice: Int? = null,
        @Query("pageSize") pageSize: Int = 5,
        @Query("sortBy") sortBy: String = "Price"
    ): List<CheapSharkDeal>
}

@Serializable
data class CheapSharkDeal(
    val title: String = "",
    val salePrice: String = "",
    val normalPrice: String = "",
    @SerialName("savings") val savings: String = "",
    @SerialName("dealID") val dealId: String = "",
    @SerialName("steamAppID") val steamAppId: String? = null
)
