package es.androidtfm.gamevision.data.steam

import es.androidtfm.gamevision.data.catalog.CatalogGame
import es.androidtfm.gamevision.data.catalog.GameCatalog
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.library.LibraryStatus
import es.androidtfm.gamevision.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/*
 * Repositorio de Steam (bloque 5, iteración 02/10).
 *
 * Responsabilidades:
 *  1. Resolución del OpenID: el worker hace la verificación y devuelve el
 *     steamid a la app por esquema propio; aquí solo se valida el formato.
 *  2. Sincronización de horas: cruza la biblioteca de Steam (con horas REALES
 *     de playtime_forever) con la biblioteca de GameVision:
 *       · El juego ya está en GameVision → se actualiza `minutesTotal`
 *         con el MÁXIMO entre lo local y lo de Steam (nunca se duplica:
 *         Steam da horas de por vida, las sesiones locales son la misma cosa).
 *       · El juego no está y se jugó → se crea ficha buscando su equivalente
 *         en RAWG por nombre (para heredar carátula, géneros e id del catálogo);
 *         jugado en las últimas 2 semanas → `Jugando`, resto → `Retirado`
 *         (el usuario lo reclasifica si quiere; nunca se inventa un diario).
 *
 * El import es PROGRESIVO (por lotes): una biblioteca de 100 juegos dispara
 * hasta 100 búsquedas en RAWG, así que se informan avances y se tolera que
 * algunos juegos queden sin emparejar (se listan en el resultado).
 */
@Singleton
class SteamRepository @Inject constructor(
    private val steamProxy: SteamProxyService,
    private val libraryRepository: LibraryRepository,
    private val userRepository: UserRepository,
    private val catalog: GameCatalog
) {

    /** Resultado de una sincronización, para la pantalla. */
    data class SyncResultado(
        val actualizados: Int,
        val importados: Int,
        val sinEmparejar: List<String>
    )

    /** Extrae el SteamID64 (17 dígitos) de un claimed_id del OpenID o de un id pelado. */
    fun steamIdValido(valor: String?): String? =
        valor?.trim()?.let { v ->
            val id = v.substringAfterLast("/id/")
            id.takeIf { it.matches(Regex("\\d{17}")) }
        }

    /** Descarga la biblioteca de Steam vía proxy. */
    suspend fun ownedGames(steamId: String): Result<List<SteamGame>> = runCatching {
        val resp = steamProxy.owned(steamId)
        resp.games
    }

    /**
     * Sincroniza horas con la biblioteca de GameVision del usuario [uid].
     *
     * @param existentes entradas actuales de la biblioteca (para no rebuscar).
     * @param onProgreso avanza de 0f a 1f mientras empareja en RAWG.
     */
    suspend fun sincronizar(
        uid: String,
        existentes: List<LibraryEntry>,
        juegosSteam: List<SteamGame>,
        onProgreso: (Float) -> Unit = {}
    ): Result<SyncResultado> = runCatching {
        val porNombre = existentes.associateBy { normaliza(it.name) }
        var actualizados = 0
        val porImportar = juegosSteam
            .filter { it.playtimeForever > 0 }
            .sortedByDescending { it.playtime2weeks }

        var hechos = 0
        val sinEmparejar = mutableListOf<String>()
        val importados = mutableListOf<LibraryEntry>()

        for (juego in porImportar) {
            val nombre = juego.name.trim()
            val existente = porNombre[normaliza(nombre)]

            if (existente != null) {
                // Ya está en la biblioteca: horas = MÁXIMO(local, steam).
                if (juego.playtimeForever > existente.minutesTotal) {
                    libraryRepository.setSteamPlaytime(uid, existente.gameId, juego.playtimeForever)
                    actualizados++
                }
            } else if (nombre.isNotBlank()) {
                // No está: buscar equivalente en RAWG para heredar id/carátula/géneros.
                val match = buscarEnCatalogo(nombre)
                if (match != null) {
                    val estado = if (juego.playtime2weeks > 0) LibraryStatus.PLAYING else LibraryStatus.RETIRED
                    importados += LibraryEntry(
                        gameId = match.id.toString(),
                        status = estado,
                        name = match.name.ifBlank { nombre },
                        coverUrl = match.coverUrl,
                        released = match.released,
                        genres = match.genres,
                        minutesTotal = juego.playtimeForever
                    )
                } else {
                    sinEmparejar += nombre
                }
            }
            hechos++
            onProgreso(if (porImportar.isEmpty()) 1f else hechos.toFloat() / porImportar.size)
        }

        // Altas en bloque al final (una escritura por juego importado).
        for (entry in importados) {
            libraryRepository.addGame(uid, entry)
        }
        SyncResultado(actualizados = actualizados, importados = importados.size, sinEmparejar = sinEmparejar)
    }

    /** Búsqueda en el catálogo con normalización (sin red si está cacheado). */
    private suspend fun buscarEnCatalogo(nombre: String): CatalogGame? =
        catalog.search(nombre).getOrNull()?.firstOrNull { g ->
            normaliza(g.name) == normaliza(nombre)
        } ?: catalog.search(nombre).getOrNull()?.firstOrNull()

    /** Normalización tolerante: minúsculas, sin acentos, sin puntuación. */
    private fun normaliza(s: String) = s.lowercase()
        .normalizeAcentos()
        .replace(Regex("[^a-z0-9 ]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun String.normalizeAcentos(): String =
        replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u')
            .replace('à', 'a').replace('è', 'e').replace('ì', 'i').replace('ò', 'o').replace('ù', 'u')
}
