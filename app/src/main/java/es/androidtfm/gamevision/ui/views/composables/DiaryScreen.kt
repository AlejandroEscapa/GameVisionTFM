package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.library.DiaryUtils
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.PlaySession
import es.androidtfm.gamevision.data.library.Platforms
import es.androidtfm.gamevision.ui.designsystem.components.EmptyState
import es.androidtfm.gamevision.ui.designsystem.components.GVChip
import es.androidtfm.gamevision.ui.designsystem.components.GameCover
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import java.util.Calendar
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X

/*
 * Pantalla del Diario (F1 — Bloque 2): T1.8, T1.9 y T1.10.
 *
 *  - Lista cronológica de sesiones agrupadas por mes (T1.8).
 *  - Botón para apuntar una sesión rápida: juego + minutos + fecha (T1.9).
 *  - Totales: esta semana y acumulado (T1.10).
 */

@Composable
fun DiaryScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel
) {
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val library by remember(uid) { libraryViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)
    val sessions by remember(uid) { libraryViewModel.observeSessions(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)

    var showLogDialog by remember { mutableStateOf(false) }

    // Instrumentación: registro de visita (F1 — Bloque 4).
    LaunchedEffect(Unit) { libraryViewModel.logScreen("diary") }

    val sessionList = sessions?.getOrNull().orEmpty()
    val months = remember(sessionList) { DiaryUtils.groupByMonth(sessionList) }
    val namesById: Map<String, LibraryEntry> = remember(library) {
        library?.getOrNull().orEmpty().associateBy { it.gameId }
    }
    val now = remember(sessions) { System.currentTimeMillis() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GVScreenHeader(title = "Diario", modifier = Modifier.padding(horizontal = 16.dp))

            // Totales (T1.10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiarySummaryCard(
                    label = "Esta semana",
                    value = DiaryUtils.formatMinutes(DiaryUtils.minutesThisWeek(sessionList, now)),
                    modifier = Modifier.weight(1f)
                )
                DiarySummaryCard(
                    label = "Total",
                    value = DiaryUtils.formatMinutes(DiaryUtils.totalMinutes(sessionList)),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                sessions == null -> Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) { repeat(4) { GameRowSkeleton() } }

                months.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = "Tu diario está vacío",
                        hint = "Apúntale una sesión a un juego de tu biblioteca",
                        icon = Lucide.X
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 88.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    months.forEach { month ->
                        item(key = "m-${month.year}-${month.month}") {
                            Text(
                                text = month.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                            )
                        }
                        items(month.sessions, key = { it.id }) { session ->
                            DiarySessionRow(
                                session = session,
                                gameName = namesById[session.gameId]?.name ?: "Juego",
                                coverUrl = namesById[session.gameId]?.coverUrl
                            )
                        }
                    }
                }
            }
        }

        // Botón para apuntar una sesión (dentro del área útil, por encima de la barra)
        FloatingActionButton(
            onClick = { showLogDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Lucide.Plus, contentDescription = "Apuntar sesión")
        }
    }

    if (showLogDialog) {
        LogSessionDialog(
            library = library?.getOrNull().orEmpty(),
            onDismiss = { showLogDialog = false },
            onSave = { gameId, minutes, date, platform, note ->
                showLogDialog = false
                coroutineScope.launch {
                    val userId = uid
                    if (userId.isNullOrBlank()) {
                        userViewModel.setMessage("Inicia sesión para apuntar sesiones")
                        return@launch
                    }
                    libraryViewModel.addSession(
                        userId,
                        PlaySession(
                            gameId = gameId,
                            date = date,
                            minutes = minutes,
                            note = note.ifBlank { null }
                        )
                    ).onSuccess {
                        // La plataforma de la última partida se refleja en la ficha (T1.6),
                        // sin crear una partida nueva.
                        if (platform != null) {
                            libraryViewModel.setLastPlatform(userId, gameId, platform)
                        }
                        userViewModel.setMessage("Sesión apuntada")
                    }.onFailure { e ->
                        userViewModel.setMessage("No se pudo apuntar: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
private fun DiarySummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DiarySessionRow(session: PlaySession, gameName: String, coverUrl: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameCover(
                imageUrl = coverUrl,
                title = gameName,
                contentDescription = "Portada de $gameName",
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DiaryUtils.formatDay(session.date) + " · " +
                        DiaryUtils.formatMinutes(session.minutes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                session.note?.takeIf { it.isNotBlank() }?.let { nota ->
                    Text(
                        text = nota,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para apuntar una sesión (T1.9): elegir juego de la biblioteca, minutos
 * con atajos, fecha (hoy por defecto) y nota opcional.
 */
@Composable
private fun LogSessionDialog(
    library: List<LibraryEntry>,
    onDismiss: () -> Unit,
    onSave: (gameId: String, minutes: Int, date: Long, platform: String?, note: String) -> Unit
) {
    var selectedGame by remember { mutableStateOf<LibraryEntry?>(library.firstOrNull()) }
    var minutes by remember { mutableStateOf(60) }
    var note by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf<String?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var gamePickerOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Apuntar sesión", style = MaterialTheme.typography.titleLarge)

                if (library.isEmpty()) {
                    Text(
                        "Tu biblioteca está vacía. Añade un juego primero.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                        Text("Cerrar")
                    }
                    return@Column
                }

                // Juego
                Text("Juego", style = MaterialTheme.typography.labelLarge)
                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { gamePickerOpen = !gamePickerOpen },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = selectedGame?.name ?: "Elige un juego",
                            modifier = Modifier.padding(14.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (gamePickerOpen) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .padding(top = 52.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            LazyColumn {
                                items(library, key = { it.gameId }) { entry ->
                                    Text(
                                        text = entry.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedGame = entry
                                                gamePickerOpen = false
                                            }
                                            .padding(14.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Minutos con atajos
                Text("¿Cuánto jugaste?", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 120, 180).forEach { m ->
                        GVChip(
                            text = when (m) { 30 -> "30m"; 60 -> "1h"; 120 -> "2h"; else -> "3h" },
                            selected = minutes == m,
                            onClick = { minutes = m }
                        )
                    }
                }
                OutlinedTextField(
                    value = minutes.toString(),
                    onValueChange = { txt -> txt.filter { it.isDigit() }.take(4).toIntOrNull()?.let { minutes = it } },
                    label = { Text("Minutos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Fecha (hoy por defecto)
                Text(
                    text = "Fecha: " + DiaryUtils.formatDay(date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { date = System.currentTimeMillis() }) { Text("Hoy") }
                    TextButton(onClick = { date = yesterday(date) }) { Text("Ayer") }
                }

                // Plataforma (opcional)
                Text("Plataforma (opcional)", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Platforms.ALL.take(8).forEach { p ->
                        GVChip(text = p, selected = platform == p, onClick = {
                            platform = if (platform == p) null else p
                        })
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(140) },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val g = selectedGame
                            if (g != null && minutes > 0) {
                                onSave(g.gameId, minutes, date, platform, note)
                            }
                        },
                        enabled = selectedGame != null && minutes > 0
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

private fun yesterday(now: Long): Long =
    (Calendar.getInstance().apply {
        timeInMillis = now
        add(Calendar.DAY_OF_MONTH, -1)
    }).timeInMillis
