package es.androidtfm.gamevision.ui.views.composables

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import es.androidtfm.gamevision.retrofit.Article
import es.androidtfm.gamevision.ui.designsystem.components.GVSkeleton
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVScreenHeader
import es.androidtfm.gamevision.viewmodel.NewsViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 15/01/2025
 * Descripción: Pantalla de noticias, que recupera las noticias mediante NewsAPI y además
 * realiza el fetch inicial de datos de usuario para centralizarlos en el UserViewModel.
 */

@Composable
fun NewsScreen(
    isDarkTheme: Boolean, // Indica si el tema oscuro está activado
    onThemeChange: (Boolean) -> Unit, // Función para cambiar el tema
    newsViewModel: NewsViewModel, // ViewModel para manejar las noticias
    userViewModel: UserViewModel, // ViewModel compartido para datos del usuario
    paddingValues: PaddingValues // Valores de padding para la pantalla
) {
    // Estado para la lista de artículos
    val newsState = remember { mutableStateOf<List<Article>>(emptyList()) }
    val context = LocalContext.current
    // Modo degradado: si la carga falla (p. ej. sin conexión) se muestra un aviso
    val loadFailed = newsViewModel.loadFailed.collectAsStateWithLifecycle().value
    // Clave para relanzar la carga desde el botón de reintento
    val retryKey = remember { mutableStateOf(0) }

    // Al iniciar la pantalla se obtienen las noticias; también al pulsar "Reintentar".
    LaunchedEffect(retryKey.value) {
        // El ViewModel ya no lanza: devuelve lista vacía y marca loadFailed si falla.
        newsState.value = newsViewModel.fetchFilteredNews("games")

        // El perfil del usuario ya no se pide aquí: UserViewModel.profile es un
        // flujo en vivo (SSOT) y se mantiene actualizado por sí solo.
    }

    // Cabecera FIJA (iteración 02/10): fuera de la lista, no se desplaza.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
    ) {
        GVScreenHeader(title = "Noticias", modifier = Modifier.padding(horizontal = GVSpacing.screenPadding))
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
        // Se muestran los artículos, un aviso de error sin conexión o el estado de carga
        if (newsState.value.isNotEmpty()) {
            items(newsState.value) { article ->
                ArticleCard(
                    article = article,
                    onArticleClick = { url ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    isDarkMode = isDarkTheme
                )
            }
        } else if (loadFailed) {
            item { NewsErrorState(onRetry = { retryKey.value++ }) }
        } else {
            item {
                NewsLoadingIndicator()
            }
        }
    }
    }
}

/*
 * Composable para representar cada artículo en una tarjeta.
 */
@Composable
fun ArticleCard(
    article: Article, // Artículo a mostrar
    onArticleClick: (String) -> Unit, // Función para manejar el clic en el artículo
    isDarkMode: Boolean, // Indica si el modo oscuro está activado
    viewModel: NewsViewModel = NewsViewModel() // ViewModel para formatear la fecha
) {
    // Superficie y forma del sistema (iteración 02/10, segunda vuelta). Antes:
    // sombra de 8 dp, radio 16 a mano y color calculado en la UI según el tema
    // (con borde solo en oscuro). La separación la da el TONO, no una sombra, y
    // el rol es el mismo en claro y en oscuro.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = GVSpacing.screenPadding,
                vertical = GVSpacing.sm
            ),
        shape = GVShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onArticleClick(article.url) }
        ) {
            // Imagen del artículo
            AsyncImage(
                model = article.urlToImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            // Contenido textual del artículo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.source.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = viewModel.formatPublishedAt(article.publishedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/*
 * Composable que muestra un indicador de carga mientras se obtienen las noticias.
 */
@Composable
fun NewsLoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GVSkeleton(Modifier.fillMaxWidth().padding(horizontal = 16.dp), height = 180.dp)
        GVSkeleton(Modifier.fillMaxWidth().padding(horizontal = 16.dp), height = 14.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cargando noticias...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/*
 * Composable que muestra un aviso cuando no se pudieron cargar las noticias
 * (modo degradado, F0) con un botón para reintentar.
 */
@Composable
fun NewsErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se pudieron cargar las noticias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Comprueba tu conexión a internet e inténtalo de nuevo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        es.androidtfm.gamevision.ui.designsystem.components.GVButton(
            text = "Reintentar",
            onClick = onRetry
        )
    }
}

/*
 * Vista previa de la pantalla de noticias.
 */
