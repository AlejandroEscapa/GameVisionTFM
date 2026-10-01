package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.data.library.DiaryUtils
import es.androidtfm.gamevision.data.library.LibraryEntry
import es.androidtfm.gamevision.data.library.LibraryInsights
import es.androidtfm.gamevision.data.library.StatisticsUtils
import es.androidtfm.gamevision.ui.designsystem.components.EmptyState
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.viewmodel.LibraryViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X

/*
 * Pantalla de estadísticas (F1 — Bloque 3, T1.12 y T1.13).
 *
 * Todo se calcula en el cliente a partir de la biblioteca (decisión D1.5) y con
 * lógica pura (StatisticsUtils), así que es rápido y testeable. El Rewind de F3
 * reutilizará estas mismas piezas.
 */

@Composable
fun StatsScreen(
    navController: NavController,
    paddingValues: PaddingValues,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel
) {
    val uid by userViewModel.currentUid.collectAsStateWithLifecycle()
    val library by remember(uid) { libraryViewModel.observeLibrary(uid.orEmpty()) }
        .collectAsStateWithLifecycle(initialValue = null)

    val entries: List<LibraryEntry>? = library?.getOrNull()
    val insights: LibraryInsights? = remember(entries) {
        entries?.let { StatisticsUtils.compute(it, System.currentTimeMillis()) }
    }

    // Instrumentación: registro de visita (F1 — Bloque 4).
    androidx.compose.runtime.LaunchedEffect(Unit) { libraryViewModel.logScreen("stats") }

    // Cabecera FIJA (iteración 02/10): el título no se desplaza, solo el contenido.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = GVSpacing.screenPadding)
    ) {
        GVScreenHeader(title = "Estadísticas")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        when {
            entries == null -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(3) { GameRowSkeleton() }
            }

            entries.isEmpty() -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "Todavía no hay datos",
                    hint = "Añade juegos a tu biblioteca y aquí aparecerán tus números",
                    icon = Lucide.X
                )
            }

            insights != null -> {
                // KPIs principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatKpi("Juegos", insights.gamesTotal.toString(), Modifier.weight(1f))
                    StatKpi("Horas", DiaryUtils.formatMinutes(insights.minutesTotal), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                StatKpi(
                    "Nota media",
                    insights.ratingAverage?.let { formatRating(it) } ?: "—",
                    Modifier.fillMaxWidth(),
                    subtitle = if (insights.ratedCount > 0) "${insights.ratedCount} ${if (insights.ratedCount == 1) "juego valorado" else "juegos valorados"}" else "Aún sin valorar"
                )

                Spacer(Modifier.height(20.dp))

                // Tu año en un vistazo (T1.13)
                YearInReviewCard(insights)

                Spacer(Modifier.height(20.dp))

                // Distribución por estado
                SectionTitle("Por estado")
                insights.byStatus.entries
                    .sortedByDescending { it.value }
                    .forEach { (status, count) ->
                        CountRow(status.label, count, insights.gamesTotal)
                    }

                Spacer(Modifier.height(20.dp))

                // Distribución de notas
                if (insights.ratedCount > 0) {
                    SectionTitle("Tus notas")
                    insights.ratingDistribution
                        .filter { it.second > 0 }
                        .reversed()
                        .forEach { (nota, count) ->
                            // Sin "★" en texto: el dato es la nota, no un glifo de
                            // otra fuente mezclado en el renglón.
                            CountRow("${formatRating(nota)} de 5", count, insights.ratedCount)
                        }
                    Spacer(Modifier.height(20.dp))
                }

                // Top géneros
                if (insights.topGenres.isNotEmpty()) {
                    SectionTitle("Géneros favoritos")
                    insights.topGenres.forEach { (genre, count) ->
                        CountRow(genre, count, insights.gamesTotal)
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Top plataformas
                if (insights.topPlatforms.isNotEmpty()) {
                    SectionTitle("Plataformas jugadas")
                    insights.topPlatforms.forEach { (platform, count) ->
                        val max = insights.topPlatforms.firstOrNull()?.second ?: count
                        CountRow(platform, count, max)
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Juegos por año de lanzamiento
                if (insights.gamesByYear.isNotEmpty()) {
                    SectionTitle("Juegos por año")
                    val maxYear = insights.gamesByYear.maxOf { it.second }
                    insights.gamesByYear.take(10).forEach { (year, count) ->
                        CountRow(year.toString(), count, maxYear)
                    }
                    Spacer(Modifier.height(20.dp))
                }

                Spacer(Modifier.height(40.dp))
            }
        }
        }
    }
}

@Composable
private fun StatKpi(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
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
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun YearInReviewCard(insights: LibraryInsights) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Tu ${insights.year} en un vistazo",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = buildString {
                    append("Este año has completado ${insights.completedThisYear} ${if (insights.completedThisYear == 1) "juego" else "juegos"}")
                    append(" y añadido ${insights.addedThisYear} a tu biblioteca.")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            insights.bestOfYear?.let { best ->
                Text(
                    text = "Tu mejor nota del año: ${best.name} (${formatRating(best.rating ?: 0.0)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            insights.mostPlayed?.let { top ->
                Text(
                    text = "El que más has jugado: ${top.name} (${DiaryUtils.formatMinutes(top.minutesTotal)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            if (insights.completedThisYear == 0 && insights.addedThisYear == 0) {
                Text(
                    text = "Aún no hay mucha actividad este año, pero cada partida cuenta.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

/** Fila con etiqueta, conteo y una barra proporcional al máximo del bloque. */
@Composable
private fun CountRow(label: String, count: Int, max: Int) {
    val fraction = if (max <= 0) 0f else (count.toFloat() / max).coerceIn(0f, 1f)
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

