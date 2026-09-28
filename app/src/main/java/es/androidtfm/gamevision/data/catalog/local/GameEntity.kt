package es.androidtfm.gamevision.data.catalog.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Entidad Room de un juego cacheado (F0/T0.4).
 *
 * Firestore/memory sigue siendo la verdad de la biblioteca; esta tabla es SOLO
 * caché del CATÁLOGO (fichas visitadas y búsquedas recientes), tal y como fija
 * la decisión D0.3 de la fase F0.
 */
@Entity(tableName = "cached_games")
data class GameEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val coverUrl: String?,
    val released: String,
    val rating: Double,
    val ratingsCount: Int,
    val metacritic: Int?,
    val suggestionsCount: Int,
    val genres: List<String>,
    val platforms: List<String>,
    val playtimeHours: Int?,
    val cachedAt: Long
)
