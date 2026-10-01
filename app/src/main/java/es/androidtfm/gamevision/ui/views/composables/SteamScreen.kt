package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.viewmodel.SteamViewModel

/*
 * Vinculación de Steam (bloque 5, iteración 02/10).
 *
 * Flujo: consentimiento (qué leemos, qué nunca vemos, que es solo lectura) →
 * Auth Tab de Chrome contra el OpenID de Steam (NUNCA WebView) → el worker
 * verifica y devuelve el steamid a la app → guardado en el perfil.
 * Después: sync de horas reales a la biblioteca.
 */
@Composable
fun SteamScreen(
    viewModel: SteamViewModel,
    uid: String?,
    steamLink: String?,
    onSteamLinkConsumido: () -> Unit,
    onBack: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val progreso by viewModel.progreso.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Vuelta del OpenID: el worker nos manda a gamevision://steam/linked?steamid=...
    // MainActivity lo reenvía aquí vía onSteamLinked.
    LaunchedEffect(estado.steamidPendiente) {
        estado.steamidPendiente?.let { viewModel.confirmarVinculacion(uid, it) }
    }
    // Deep link del retorno del OpenID: gamevision://steam/linked?steamid=…
    LaunchedEffect(steamLink) {
        if (steamLink != null) {
            viewModel.confirmarVinculacion(uid, steamLink)
            onSteamLinkConsumido()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GVScreenHeader(
            title = "Steam",
            modifier = Modifier.padding(horizontal = GVSpacing.screenPadding),
            leading = {
                IconButton(onClick = onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GVSpacing.screenPadding)
        ) {
            if (estado.steamId == null) {
                // ---- Consentimiento (requisito Play: antes de salir de la app) ----
                Text(
                    "Conecta tu cuenta de Steam",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(GVSpacing.sm))
                Text(
                    "Tus horas reales de Steam entrarán en tu biblioteca y en tu perfil.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(GVSpacing.md))
                TarjetaConsentimiento("✅  Leeremos tu biblioteca, tus horas jugadas y tu perfil público")
                TarjetaConsentimiento("🚫  Nunca vemos tu contraseña: el login ocurre en steamcommunity.com")
                TarjetaConsentimiento("🔁  Es una conexión de SOLO LECTURA: no compramos ni modificamos nada")
                Spacer(Modifier.height(GVSpacing.lg))
                GVButton(
                    text = "Conectar Steam",
                    onClick = { viewModel.iniciarVinculacion(contexto) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // ---- Vinculada: estado y sync ----
                TarjetaEstado(
                    steamId = estado.steamId!!,
                    ultimaSync = estado.ultimaSyncTexto,
                    cargando = estado.sincronizando
                )
                Spacer(Modifier.height(GVSpacing.md))
                GVButton(
                    text = if (estado.sincronizando) "Sincronizando…" else "Sincronizar horas",
                    onClick = { viewModel.sincronizar(uid) },
                    modifier = Modifier.fillMaxWidth(),
                    secondary = true,
                    enabled = !estado.sincronizando
                )
                if (estado.sincronizando) {
                    Spacer(Modifier.height(GVSpacing.sm))
                    Text(
                        text = when {
                            progreso < 1f -> "Emparejando con el catálogo… ${(progreso * 100).toInt()} %"
                            else -> "Guardando cambios…"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                estado.resultado?.let { res ->
                    Spacer(Modifier.height(GVSpacing.md))
                    Text(
                        text = buildString {
                            append("✓ ${res.actualizados} juegos actualizados")
                            if (res.importados > 0) append(" · ${res.importados} importados")
                            if (res.sinEmparejar.isNotEmpty()) {
                                append(" · ${res.sinEmparejar.size} sin emparejar (no estaban en el catálogo)")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(GVSpacing.lg))
                Text(
                    text = "Las horas de Steam son de por vida: tu diario sigue siendo tuyo y no " +
                        "se inventan sesiones. Cada juego importado entra como «Jugando» (si jugaste " +
                        "en las últimas 2 semanas) o «Retirado», y puedes reclasificarlo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TarjetaConsentimiento(texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun TarjetaEstado(steamId: String, ultimaSync: String, cargando: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp)
    ) {
        Text("Cuenta vinculada", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(GVSpacing.xs))
        Text(
            text = "SteamID $steamId",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (ultimaSync.isNotBlank()) {
            Text(
                text = "Última sincronización: $ultimaSync",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
