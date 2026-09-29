package es.androidtfm.gamevision.data.catalog.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Caché de duraciones de HowLongToBeat (F1/T1.11).
 *
 * Clave: el nombre normalizado del juego (HltbUtils.cacheKey). TTL amplio,
 * porque los tiempos de HLTB derivan muy lentamente. Es SOLO caché.
 */
@Entity(tableName = "hltb_cache")
data class HltbCacheEntity(
    @PrimaryKey val cacheKey: String,
    val hltbId: Int,
    val mainMinutes: Int?,
    val plusMinutes: Int?,
    val completeMinutes: Int?,
    val fetchedAt: Long
)
