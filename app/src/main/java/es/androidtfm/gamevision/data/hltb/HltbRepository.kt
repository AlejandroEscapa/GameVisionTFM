package es.androidtfm.gamevision.data.hltb

import android.util.Log
import es.androidtfm.gamevision.data.catalog.local.GameDao
import es.androidtfm.gamevision.data.catalog.local.HltbCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Repositorio de duraciones HLTB (F1/T1.11).
 *
 * Estrategia (decisión D1.4):
 *  1. Se consulta la CACHÉ por nombre normalizado.
 *  2. Si no hay dato o está caducado (> 90 días), se pregunta a HLTB y se cachea.
 *  3. Si HLTB falla, se devuelve lo que hubiera en caché (aunque esté caducado)
 *     y, si no hay nada, null → la UI ofrece valor manual. NUNCA rompe.
 *
 * No hay llamadas en bucle: una por juego y nombre, y solo cuando hace falta.
 */

@Singleton
class HltbRepository @Inject constructor(
    private val client: HltbClient,
    private val gameDao: GameDao
) {

    companion object {
        private const val TAG = "HltbRepository"
    }

    /** Memo por nombre normalizado; la instancia vive en Hilt durante la sesión. */
    private val memo = java.util.concurrent.ConcurrentHashMap<String, HltbPlaytimes>()

    /**
     * Duración de un juego por su nombre. `null` si no hay dato disponible.
     * Nunca lanza: ante cualquier fallo devuelve lo cacheado o null.
     */
    suspend fun playtimesFor(name: String): HltbPlaytimes? = withContext(Dispatchers.IO) {
        if (name.isBlank()) return@withContext null
        val key = HltbUtils.cacheKey(name)
        if (key.isBlank()) return@withContext null

        // Memo en memoria (iteración 02/10): reabrir la misma ficha no debe ni
        // tocar Room. Vida corta: la caché Room (TTL 90 días) sigue mandando.
        memo[key]?.let { return@withContext it }

        val cached = runCatching { gameDao.hltb(key) }.getOrNull()?.toDomain()
        val now = System.currentTimeMillis()

        if (cached != null && HltbUtils.isFresh(cached.fetchedAt, now)) {
            memo[key] = cached
            return@withContext cached
        }

        // Caducado o sin caché: se intenta la red.
        val fresh = runCatching { client.search(name) }
            .onFailure { Log.w(TAG, "HLTB no disponible para «$name»: ${it.message}") }
            .getOrNull()

        if (fresh != null && fresh.hasData) {
            runCatching {
                gameDao.upsertHltb(fresh.toEntity(key))
            }.onFailure { Log.w(TAG, "No se pudo cachear HLTB: ${it.message}") }
            memo[key] = fresh
            return@withContext fresh
        }

        // Sin dato nuevo: lo caducado sigue siendo mejor que nada.
        cached?.let { memo[key] = it }
        cached
    }
}

private fun HltbCacheEntity.toDomain() = HltbPlaytimes(
    hltbId = hltbId,
    mainMinutes = mainMinutes,
    plusMinutes = plusMinutes,
    completeMinutes = completeMinutes,
    fetchedAt = fetchedAt
)

private fun HltbPlaytimes.toEntity(key: String) = HltbCacheEntity(
    cacheKey = key,
    hltbId = hltbId,
    mainMinutes = mainMinutes,
    plusMinutes = plusMinutes,
    completeMinutes = completeMinutes,
    fetchedAt = fetchedAt ?: System.currentTimeMillis()
)
