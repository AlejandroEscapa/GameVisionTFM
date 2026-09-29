package es.androidtfm.gamevision.data.hltb

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Cliente mínimo de HowLongToBeat (F1/T1.11).
 *
 * IMPORTANTE: es un flujo NO OFICIAL, replicado de la propia web (ver
 * docs/investigacion-2026/hltb-verificacion-2026.md). Por eso:
 *  - Está AISLADO aquí: si cambia o cae, la app degrada (sin dato → valor manual).
 *  - Nunca se llama en bucle: solo bajo demanda y con caché (HltbRepository).
 *  - Si algo falla, devuelve Result.failure; la UI no muestra detalle técnico.
 */

@Singleton
class HltbClient @Inject constructor() {

    companion object {
        private const val TAG = "HltbClient"
        private const val BASE = "https://howlongtobeat.com"
        private const val UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }

    private val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Busca un juego por nombre y devuelve sus tiempos, o null si HLTB no
     * encuentra nada. Lanza excepción solo ante fallo de red/servidor.
     */
    suspend fun search(name: String): HltbPlaytimes? = withContext(Dispatchers.IO) {
        var token = fetchToken()
        var response = postSearch(name, token)
        if (response.first == 403) {
            // Token caducado: re-init y reintento único (como hace su propia web).
            token = fetchToken()
            response = postSearch(name, token)
        }
        val (status, body) = response
        if (status !in 200..299 || body == null) {
            Log.w(TAG, "HLTB respondió $status")
            return@withContext null
        }
        parseTop(body)
    }

    private fun fetchToken(): String {
        val request = Request.Builder()
            .url("$BASE/api/search/site/init?t=${System.currentTimeMillis()}")
            .header("User-Agent", UA)
            .header("Referer", "$BASE/")
            .build()
        http.newCall(request).execute().use { res ->
            val text = res.body?.string().orEmpty()
            return JSONObject(text).optString("token")
        }
    }

    private fun postSearch(name: String, token: String): Pair<Int, String?> {
        val body = buildBody(name).toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("$BASE/api/search/site")
            .header("User-Agent", UA)
            .header("Referer", "$BASE/")
            .header("Origin", BASE)
            .header("Content-Type", "application/json")
            .header("x-auth-token", token)
            .post(body)
            .build()
        return http.newCall(request).execute().use { res ->
            res.code to res.body?.string()
        }
    }

    /** Cuerpo con los valores por defecto de la web de HLTB (filtros vacíos). */
    private fun buildBody(name: String): JSONObject {
        val termsArray = org.json.JSONArray()
        name.trim().split(" ").filter { it.isNotBlank() }.forEach { termsArray.put(it) }

        val games = JSONObject().apply {
            put("userId", 0)
            put("platform", JSONObject().put("mode", "include").put("values", org.json.JSONArray()))
            put("sortCategory", "popular")
            put("rangeCategory", "main")
            put("rangeTime", JSONObject().put("min", JSONObject.NULL).put("max", JSONObject.NULL))
            put(
                "gameplay",
                JSONObject()
                    .put("perspective", JSONObject().put("mode", "include").put("values", org.json.JSONArray()))
                    .put("flow", JSONObject().put("mode", "include").put("values", org.json.JSONArray()))
                    .put("genre", JSONObject().put("mode", "include").put("values", org.json.JSONArray()))
            )
            put("year", JSONObject().put("mode", "include").put("values", org.json.JSONArray()))
            put("modifier", "")
        }
        val searchOptions = JSONObject().apply {
            put("games", games)
            put("users", JSONObject().put("sortCategory", "postcount"))
            put("lists", JSONObject().put("sortCategory", "follows"))
            put("filter", "")
            put("sort", 0)
            put("randomizer", 0)
        }
        return JSONObject().apply {
            put("searchType", "games")
            put("searchTerms", termsArray)
            put("searchPage", 1)
            put("size", 20)
            put("searchOptions", searchOptions)
            put("useCache", true)
        }
    }

    /** Toma el primer resultado y lo mapea a minutos. null si no hay resultados. */
    private fun parseTop(body: String): HltbPlaytimes? {
        val data = JSONObject(body).optJSONArray("data") ?: return null
        if (data.length() == 0) return null
        val top = data.getJSONObject(0)
        val id = top.optInt("game_id", 0)
        return HltbPlaytimes(
            hltbId = id,
            mainMinutes = HltbUtils.secondsToMinutes(top.optLong("comp_main", 0L)),
            plusMinutes = HltbUtils.secondsToMinutes(top.optLong("comp_plus", 0L)),
            completeMinutes = HltbUtils.secondsToMinutes(top.optLong("comp_100", 0L)),
            fetchedAt = System.currentTimeMillis()
        )
    }
}
