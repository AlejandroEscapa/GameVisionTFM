package es.androidtfm.gamevision.retrofit

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {
    private const val NEWS_URL = "https://newsapi.org/"
    private const val RAWG_URL = "https://api.rawg.io/api/"
    private const val CHEAPSHARK_URL = "https://www.cheapshark.com/api/1.0/"

    // Parseo tolerante: ignora claves desconocidas y fuerza valores nulos a los defaults
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    val newsApi: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NewsApiService::class.java)
    }

    /** Precios (Home «Te está esperando»): gratis y sin clave. */
    val cheapSharkApi: CheapSharkService by lazy {
        Retrofit.Builder()
            .baseUrl(CHEAPSHARK_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CheapSharkService::class.java)
    }

    val gamesApi: GameApiService by lazy {
        Retrofit.Builder()
            .baseUrl(RAWG_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GameApiService::class.java)
    }
}
