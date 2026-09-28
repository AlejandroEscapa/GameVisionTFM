package es.androidtfm.gamevision.data.catalog.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
 * Base de datos Room de la caché de catálogo (F0/T0.4).
 *
 * Alcance acotado a propósito (D0.3): fichas visitadas + búsquedas recientes.
 * NO cachea todo el catálogo (RAWG no lo permite y no cabe en el dispositivo).
 */
@Database(
    entities = [GameEntity::class, SearchCacheEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CatalogDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
