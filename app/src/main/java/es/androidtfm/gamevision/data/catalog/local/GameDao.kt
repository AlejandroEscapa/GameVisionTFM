package es.androidtfm.gamevision.data.catalog.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/*
 * DAO de la caché de catálogo (F0/T0.4).
 *
 * Se declara como interfaz para poder falsearlo en tests JVM sin Android.
 */
@Dao
interface GameDao {

    /** Inserta o actualiza una lista de juegos cacheados. */
    @Upsert
    suspend fun upsertGames(games: List<GameEntity>)

    /** Devuelve un juego cacheado por id, o null si no está. */
    @Query("SELECT * FROM cached_games WHERE id = :id LIMIT 1")
    suspend fun game(id: Int): GameEntity?

    /** Devuelve los juegos cacheados cuyos ids estén en la lista. */
    @Query("SELECT * FROM cached_games WHERE id IN (:ids)")
    suspend fun games(ids: List<Int>): List<GameEntity>

    /** Guarda (o reemplaza) el resultado de una búsqueda. */
    @Upsert
    suspend fun upsertSearch(search: SearchCacheEntity)

    /** Recupera el resultado cacheado de una búsqueda, o null. */
    @Query("SELECT * FROM cached_searches WHERE query = :query LIMIT 1")
    suspend fun search(query: String): SearchCacheEntity?
}
