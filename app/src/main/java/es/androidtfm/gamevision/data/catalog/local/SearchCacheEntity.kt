package es.androidtfm.gamevision.data.catalog.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Entidad Room que guarda el resultado de una búsqueda reciente (F0/T0.4).
 *
 * Se conserva el ORDEN de los resultados (por eso se guarda la lista de ids en
 * una cadena con separador), para poder reconstruir la misma respuesta sin red.
 */
@Entity(tableName = "cached_searches")
data class SearchCacheEntity(
    @PrimaryKey val query: String,
    val orderedGameIds: List<Int>,
    val cachedAt: Long
)
