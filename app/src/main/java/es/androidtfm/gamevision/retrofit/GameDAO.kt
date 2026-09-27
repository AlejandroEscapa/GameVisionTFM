package es.androidtfm.gamevision.retrofit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 19/01/2025
 * Descripción: Modelos de la API de RAWG.
 *
 * Todos los campos tienen valor por defecto para que el parseo sea tolerante
 * con respuestas que omiten claves (comportamiento equivalente al de Gson).
 */

@Serializable
data class ApiResponse(
    @SerialName("count") val count: Int = 0,
    @SerialName("next") val next: String? = null,
    @SerialName("previous") val previous: String? = null,
    @SerialName("results") val results: List<Game> = emptyList()
)

@Serializable
data class Game(
    @SerialName("slug") val slug: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("playtime") val playtime: Int = 0,
    @SerialName("platforms") val platforms: List<Platform> = emptyList(),
    @SerialName("stores") val stores: List<StoreWrapper> = emptyList(),
    @SerialName("released") val released: String = "",
    @SerialName("tba") val tba: Boolean = false,
    @SerialName("background_image") val backgroundImage: String? = null,
    @SerialName("rating") val rating: Double = 0.0,
    @SerialName("rating_top") val ratingTop: Int = 0,
    @SerialName("ratings") val ratings: List<Rating> = emptyList(),
    @SerialName("ratings_count") val ratingsCount: Int = 0,
    @SerialName("reviews_text_count") val reviewsTextCount: Int = 0,
    @SerialName("added") val added: Int = 0,
    @SerialName("added_by_status") val addedByStatus: AddedByStatus? = null,
    @SerialName("metacritic") val metacritic: Int? = null,
    @SerialName("suggestions_count") val suggestionsCount: Int = 0,
    @SerialName("updated") val updated: String = "",
    @SerialName("id") val id: Int = 0,
    @SerialName("score") val score: String? = null,
    @SerialName("clip") val clip: String? = null,
    @SerialName("tags") val tags: List<Tag> = emptyList(),
    @SerialName("esrb_rating") val esrbRating: EsrbRating? = null,
    @SerialName("user_game") val userGame: String? = null,
    @SerialName("reviews_count") val reviewsCount: Int = 0,
    @SerialName("saturated_color") val saturatedColor: String = "",
    @SerialName("dominant_color") val dominantColor: String = "",
    @SerialName("short_screenshots") val shortScreenshots: List<ShortScreenshot> = emptyList(),
    @SerialName("parent_platforms") val parentPlatforms: List<ParentPlatformWrapper> = emptyList(),
    @SerialName("genres") val genres: List<Genre> = emptyList()
)

@Serializable
data class Platform(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = ""
)

@Serializable
data class StoreWrapper(
    @SerialName("store") val store: Store = Store()
)

@Serializable
data class Store(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = ""
)

@Serializable
data class Rating(
    @SerialName("id") val id: Int = 0,
    @SerialName("title") val title: String = "",
    @SerialName("count") val count: Int = 0,
    @SerialName("percent") val percent: Double = 0.0
)

@Serializable
data class AddedByStatus(
    @SerialName("yet") val yet: Int? = null,
    @SerialName("owned") val owned: Int? = null,
    @SerialName("beaten") val beaten: Int? = null,
    @SerialName("toplay") val toplay: Int? = null,
    @SerialName("dropped") val dropped: Int? = null,
    @SerialName("playing") val playing: Int? = null
)

@Serializable
data class Tag(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = "",
    @SerialName("language") val language: String = "",
    @SerialName("games_count") val gamesCount: Int = 0,
    @SerialName("image_background") val imageBackground: String = ""
)

@Serializable
data class EsrbRating(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = "",
    @SerialName("name_en") val nameEn: String? = null,
    @SerialName("name_ru") val nameRu: String? = null
)

@Serializable
data class ShortScreenshot(
    @SerialName("id") val id: Int = 0,
    @SerialName("image") val image: String = ""
)

@Serializable
data class ParentPlatformWrapper(
    @SerialName("platform") val platform: ParentPlatform = ParentPlatform()
)

@Serializable
data class ParentPlatform(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = ""
)

@Serializable
data class Genre(
    @SerialName("id") val id: Int = 0,
    @SerialName("name") val name: String = "",
    @SerialName("slug") val slug: String = ""
)
