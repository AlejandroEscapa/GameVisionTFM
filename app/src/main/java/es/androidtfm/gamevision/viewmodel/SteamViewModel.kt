package es.androidtfm.gamevision.viewmodel

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.androidtfm.gamevision.BuildConfig
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryRepository
import es.androidtfm.gamevision.data.repository.UserRepository
import es.androidtfm.gamevision.data.steam.SteamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * ViewModel de la vinculación de Steam (bloque 5, iteración 02/10).
 *
 * Estados del flujo:
 *   sin vincular → [iniciarVinculacion] Auth Tab → OpenID → el worker verifica
 *   y devuelve a la app (gamevision://steam/linked?steamid=…) →
 *   [confirmarVinculacion] guarda el steamid en el perfil → [sincronizar]
 *   cruza horas con la biblioteca.
 */
@HiltViewModel
class SteamViewModel @Inject constructor(
    private val steamRepository: SteamRepository,
    private val userRepository: UserRepository,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    data class Estado(
        val steamId: String? = null,
        val ultimaSyncTexto: String = "",
        val sincronizando: Boolean = false,
        val resultado: SteamRepository.SyncResultado? = null,
        /** steamid que llega del OpenID y aún no está confirmado. */
        val steamidPendiente: String? = null
    )

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado

    /** Progreso del emparejamiento con el catálogo (0..1). */
    private val _progreso = MutableStateFlow(0f)
    val progreso: StateFlow<Float> = _progreso

    private var existentes: List<LibraryEntry> = emptyList()

    fun observar(uid: String?) {
        if (uid.isNullOrBlank()) return
        viewModelScope.launch {
            userRepository.observeProfile(uid).collect { result ->
                result.getOrNull()?.let { perfil ->
                    _estado.value = _estado.value.copy(
                        steamId = perfil.steamId,
                        ultimaSyncTexto = formatUltimaSync(perfil.steamSyncAt)
                    )
                }
            }
        }
    }

    fun cargarBiblioteca(uid: String) {
        if (uid.isBlank() || existentes.isNotEmpty()) return
        viewModelScope.launch {
            libraryRepository.observeLibrary(uid).collect { resultado ->
                resultado.getOrNull()?.let { existentes = it }
            }
        }
    }

    /** Abre el OpenID de Steam en un Auth Tab (Custom Tabs, nunca WebView). */
    fun iniciarVinculacion(contexto: Context) {
        val worker = BuildConfig.STEAM_PROXY_URL.trimEnd('/')
        require(worker.isNotBlank()) { "Falta steamProxyUrl en local.properties" }
        val openid = "https://steamcommunity.com/openid/login?" +
            "openid.ns=http://specs.openid.net/auth/2.0" +
            "&openid.mode=checkid_setup" +
            "&openid.return_to=$worker/steam/success" +
            "&openid.realm=$worker" +
            "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
            "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(contexto, android.net.Uri.parse(openid))
    }

    /** Llega el retorno del OpenID (desde MainActivity): guarda el steamid. */
    fun confirmarVinculacion(uid: String?, steamid: String) {
        if (uid.isNullOrBlank()) return
        viewModelScope.launch {
            userRepository.updateProfile(uid, mapOf("steamId" to steamid, "steamSyncAt" to 0L))
            _estado.value = _estado.value.copy(steamId = steamid, steamidPendiente = null)
        }
    }

    /** Cruza las horas de Steam con la biblioteca de GameVision. */
    fun sincronizar(uid: String?) {
        if (uid.isNullOrBlank()) return
        val steamId = _estado.value.steamId ?: return
        _estado.value = _estado.value.copy(sincronizando = true, resultado = null)
        _progreso.value = 0f
        viewModelScope.launch {
            steamRepository.ownedGames(steamId)
                .onSuccess { juegos ->
                    steamRepository.sincronizar(uid, existentes, juegos) { p ->
                        _progreso.value = p
                    }.onSuccess { res ->
                        val ahora = System.currentTimeMillis()
                        userRepository.updateProfile(uid, mapOf("steamSyncAt" to ahora))
                        _estado.value = _estado.value.copy(
                            sincronizando = false,
                            resultado = res,
                            ultimaSyncTexto = "hace un momento"
                        )
                    }.onFailure {
                        _estado.value = _estado.value.copy(sincronizando = false)
                    }
                }
                .onFailure {
                    _estado.value = _estado.value.copy(sincronizando = false)
                }
        }
    }

    private fun formatUltimaSync(epoch: Long): String =
        if (epoch <= 0L) "" else android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", epoch).toString()
}
