package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.model.GameList
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.CircleUserRound
import com.composables.icons.lucide.Flag
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.UserMinus
import com.composables.icons.lucide.UserPlus

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 30/09/2026 (Fase 2 — Social, T2.4)
 *
 * Perfil público de otro jugador: cabecera con contadores, seguir/dejar de
 * seguir (D2.1), bloquear/reportar (D2.5), Top 4 (T2.5), biblioteca pública
 * gobernada por `users/{uid}.isPrivate` (D2.2: la REGLA manda — si la lectura
 * falla, se pinta "cuenta privada", la UI no decide por su cuenta) y listas
 * públicas (T2.6).
 */

@Composable
fun PublicProfileScreen(
    profileUid: String,
    paddingValues: PaddingValues,
    navController: NavHostController,
    userViewModel: UserViewModel,
    socialViewModel: SocialViewModel
) {
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()
    val profile by socialViewModel.publicProfile.collectAsStateWithLifecycle()
    val profileError by socialViewModel.publicProfileError.collectAsStateWithLifecycle()
    val counts by socialViewModel.profileCounts.collectAsStateWithLifecycle()
    val listsState by socialViewModel.lists.collectAsStateWithLifecycle()
    val libraryState by socialViewModel.publicLibrary.collectAsStateWithLifecycle()

    var isFollowing by remember(profileUid) { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var confirmBlock by remember { mutableStateOf(false) }

    // Carga del perfil + sus datos públicos.
    LaunchedEffect(profileUid) {
        socialViewModel.loadPublicProfile(profileUid)
        socialViewModel.loadLists(profileUid, onlyPublic = true)
        socialViewModel.loadPublicLibrary(profileUid)
    }
    LaunchedEffect(uid) {
        val me = uid
        if (me != null && me.isNotBlank()) {
            socialViewModel.refreshFollowEdges(me)
            isFollowing = socialViewModel.isFollowing(me, profileUid)
        }
    }

    // Cruce Top 4 ↔ biblioteca: nombre y portada reales desde la instantánea.
    val libraryById: Map<String, LibraryEntry> =
        libraryState?.getOrNull()?.associateBy { it.gameId }.orEmpty()

    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val p = profile
        when {
            p == null && profileError ->
                NotAvailableNotice(onBack = { navController.popBackStack() })
            p == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Cargando perfil…", style = MaterialTheme.typography.bodyMedium)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ProfileHeader(
                        profile = p,
                        followers = counts.first,
                        followingCount = counts.second,
                        isFollowing = isFollowing,
                        isMe = uid == profileUid,
                        onBack = { navController.popBackStack() },
                        onToggleFollow = {
                            val me = uid ?: return@ProfileHeader
                            if (isFollowing) {
                                socialViewModel.unfollow(me, profileUid) { result ->
                                    if (result.isSuccess) {
                                        isFollowing = false
                                        socialViewModel.loadPublicProfile(profileUid)
                                    } else {
                                        userViewModel.setMessage("No se pudo dejar de seguir")
                                    }
                                }
                            } else {
                                socialViewModel.follow(me, profileUid) { result ->
                                    if (result.isSuccess) {
                                        isFollowing = true
                                        socialViewModel.loadPublicProfile(profileUid)
                                    } else {
                                        userViewModel.setMessage("No se pudo seguir")
                                    }
                                }
                            }
                        },
                        onReport = { showReportDialog = true },
                        onBlock = { confirmBlock = true }
                    )
                }

                // Top 4 (T2.5) — identidad del jugador
                if (p.topGameIds.isNotEmpty()) {
                    item {
                        SectionTitle("Top 4")
                        TopGamesRow(topGameIds = p.topGameIds, catalog = libraryById)
                    }
                }

                // Biblioteca pública (D2.2: la regla decide; el fallo = cuenta privada)
                item {
                    when (val lib = libraryState) {
                        null -> {}
                        else -> when {
                            lib.isSuccess -> {
                                val entries = lib.getOrDefault(emptyList())
                                SectionTitle("Biblioteca (${entries.size})")
                                if (entries.isEmpty()) {
                                    EmptyLine("Sin juegos en la biblioteca todavía.")
                                } else {
                                    LibraryPreview(entries)
                                }
                            }
                            lib.isFailure -> PrivateNotice()
                        }
                    }
                }

                // Listas públicas (T2.6)
                item {
                    val lists = listsState?.getOrDefault(emptyList()).orEmpty()
                    SectionTitle("Listas (${lists.size})")
                    if (lists.isEmpty()) {
                        EmptyLine("Sin listas públicas.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            lists.forEach { list -> PublicListCard(list) }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSend = { reason ->
                showReportDialog = false
                socialViewModel.report(uid.orEmpty(), profileUid, reason)
                userViewModel.setMessage("Reporte enviado. Gracias por ayudar a la comunidad.")
            }
        )
    }

    if (confirmBlock) {
        AlertDialog(
            onDismissRequest = { confirmBlock = false },
            title = { Text("Bloquear a @${profile?.username.orEmpty()}") },
            text = { Text("No podrá ver tu perfil ni interactuar contigo, y dejaráis de veros en el feed.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmBlock = false
                    val me = uid.orEmpty()
                    socialViewModel.block(me, profileUid) { result ->
                        if (result.isSuccess) {
                            // El feed se filtra por la lista de bloqueados: refresca
                            // la caché para que desaparezca al volver a Social.
                            socialViewModel.refreshBlocked(me)
                            userViewModel.setMessage("Usuario bloqueado")
                            navController.popBackStack()
                        } else {
                            userViewModel.setMessage("No se pudo bloquear")
                        }
                    }
                }) { Text("Bloquear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmBlock = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    followers: Long,
    followingCount: Long,
    isFollowing: Boolean,
    isMe: Boolean,
    onBack: () -> Unit,
    onToggleFollow: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                }
                Spacer(Modifier.width(4.dp))
                if (profile.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = profile.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        Lucide.CircleUserRound,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = profile.nameSurname.ifBlank { profile.username.ifBlank { "Jugador" } },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${profile.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (!isMe) {
                    IconButton(onClick = onReport) {
                        Icon(
                            Lucide.Flag,
                            contentDescription = "Reportar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onBlock) {
                        Icon(
                            Lucide.Ban,
                            contentDescription = "Bloquear",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$followers seguidores", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(16.dp))
                Text("$followingCount seguidos", style = MaterialTheme.typography.bodyMedium)
            }
            if (profile.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(profile.description, style = MaterialTheme.typography.bodySmall)
            }
            if (!isMe) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onToggleFollow,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isFollowing) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Icon(
                        if (isFollowing) Lucide.UserMinus else Lucide.UserPlus,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isFollowing) "Siguiendo" else "Seguir",
                        color = if (isFollowing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun EmptyLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

/**
 * Aviso de perfil no disponible: la lectura del perfil se denegó por privacidad
 * o bloqueo (D2.2/D2.5) y no se puede distinguir el motivo. Se dice claro en vez
 * de dejar «Cargando perfil…» en bucle.
 */
@Composable
private fun NotAvailableNotice(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Lucide.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text("Perfil no disponible", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Esta cuenta es privada o no tienes acceso a su perfil.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("Volver") }
            }
        }
    }
}

/** Aviso de cuenta privada (D2.2): la regla denegó la lectura de la biblioteca. */
@Composable
private fun PrivateNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Lucide.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(
                "Esta cuenta es privada: su biblioteca no es visible.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Top 4 del jugador (T2.5). Los nombres y portadas salen de la instantánea de
 * catálogo guardada en la biblioteca; si un juego del Top no está en ella se
 * muestra la posición con placeholder — nunca un número suelto sin contexto.
 * La edición del Top vive en el perfil propio (T2.5).
 */
@Composable
private fun TopGamesRow(
    topGameIds: List<String>,
    catalog: Map<String, LibraryEntry>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        topGameIds.take(4).forEachIndexed { index, gameId ->
            val entry = catalog[gameId]
            Column(modifier = Modifier.weight(1f)) {
                if (entry != null) {
                    GameCover(
                        imageUrl = entry.coverUrl,
                        title = entry.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry?.name.orEmpty().ifBlank { "Sin definir" },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun LibraryPreview(entries: List<LibraryEntry>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(entries.take(12), key = { it.gameId }) { entry ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .width(120.dp)
                ) {
                    Text(
                        text = entry.name.ifBlank { "Juego" },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicListCard(list: GameList) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(list.name.ifBlank { "Lista" }, style = MaterialTheme.typography.titleSmall)
            if (list.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    list.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${list.gameIds.size} juegos",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Diálogo de reporte (D2.5): motivo obligatorio, detalle opcional en F3. */
@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var selected by remember { mutableStateOf("spam") }
    val reasons = listOf(
        "spam" to "Spam o contenido comercial",
        "acoso" to "Acoso o lenguaje ofensivo",
        "suplantacion" to "Suplantación de identidad",
        "otro" to "Otro motivo"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar usuario") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                reasons.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = value }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected == value,
                            onClick = { selected = value }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(selected) }) { Text("Enviar reporte") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
