package es.androidtfm.gamevision.ui.views.composables

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import es.androidtfm.gamevision.data.model.ChatMessage
import es.androidtfm.gamevision.data.model.FeedEntry
import es.androidtfm.gamevision.data.model.MilestoneTypes
import es.androidtfm.gamevision.data.model.UserProfile
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.data.social.FeedEntryPair
import es.androidtfm.gamevision.ui.designsystem.components.GameRowSkeleton
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.viewmodel.SocialViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 26/02/2025
 * Descripción: 
 */

/**
 * Pantalla principal de la red social que muestra el timeline de mensajes.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param paddingValues PaddingValues para ajustar el layout.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para datos de usuario.
 * @param ddbbViewModel ViewModel para operaciones con la base de datos.
 */
@Composable
fun SocialScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavHostController,
    // Sin defaults: cada ruta pasa sus ViewModels explícitos. Un default viewModel() en
    // el ámbito de un composable() de navegación no tiene la factory de @HiltViewModel
    // y CRASHEA en runtime (probado en E2E). El compilador ahora lo impide.
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel,
    socialViewModel: SocialViewModel
) {
    // Identidad desde el SSOT de sesión (clave: uid — ADR-0008)
    val uid by userViewModel.currentUid.collectAsState()
    val myProfile by userViewModel.profile.collectAsState()
    val following by socialViewModel.following.collectAsState()
    val feedState by socialViewModel.feed.collectAsState()
    val searchResults by socialViewModel.searchResults.collectAsState()

    var tab by remember { mutableIntStateOf(0) }
    var searchText by remember { mutableStateOf("") }
    var postText by remember { mutableStateOf("") }
    val postMaxLength = 280

    // Grafo de seguimiento + feed en vivo (CA2.2). Re-observa al cambiar a quién sigues.
    LaunchedEffect(uid) {
        val userUid = uid
        if (userUid != null && userUid.isNotBlank()) socialViewModel.refreshFollowEdges(userUid)
    }
    LaunchedEffect(uid, following) {
        val userUid = uid
        if (userUid != null && userUid.isNotBlank()) {
            socialViewModel.observeFeed(following.toList() + userUid, userUid)
        }
    }

    Surface(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SocialHeader(
                onRefresh = { socialViewModel.refreshFeed() },
                navController = navController
            )
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Feed") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Buscar") })
            }
            when (tab) {
                0 -> FeedTab(
                    feedState = feedState,
                    myUid = uid.orEmpty(),
                    postText = postText,
                    onPostTextChange = { if (it.length <= postMaxLength) postText = it },
                    onPublish = {
                        val userUid = uid
                        if (userUid != null && postText.isNotBlank()) {
                            socialViewModel.publishPost(userUid, postText.trim()) { result ->
                                if (result.isSuccess) postText = ""
                                else userViewModel.setMessage("No se pudo publicar: ${result.exceptionOrNull()?.message}")
                            }
                        }
                    },
                    onToggleLike = { entry ->
                        val userUid = uid
                        if (userUid != null) {
                            socialViewModel.toggleLike(entry, userUid) { result ->
                                if (result.isFailure) {
                                    userViewModel.setMessage("No se pudo actualizar el me gusta")
                                }
                                socialViewModel.refreshFeed()
                            }
                        }
                    },
                    onOpenProfile = { authorUid ->
                        if (authorUid.isNotBlank()) navController.navigate("publicProfile/$authorUid")
                    }
                )
                else -> SearchTab(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    onSearch = { socialViewModel.search(searchText) },
                    results = searchResults,
                    myUid = uid.orEmpty(),
                    followingUids = following,
                    onFollow = { target ->
                        val userUid = uid
                        if (userUid != null) socialViewModel.follow(userUid, target) {
                            if (it.isFailure) userViewModel.setMessage("No se pudo seguir")
                            socialViewModel.refreshFeed()
                        }
                    },
                    onUnfollow = { target ->
                        val userUid = uid
                        if (userUid != null) socialViewModel.unfollow(userUid, target) {
                            if (it.isFailure) userViewModel.setMessage("No se pudo dejar de seguir")
                            socialViewModel.refreshFeed()
                        }
                    },
                    onOpenProfile = { authorUid -> navController.navigate("publicProfile/$authorUid") }
                )
            }
        }
    }
}

/** Pestaña Feed (D2.3/D2.6): composer de posts, hitos y posts con me gusta. */
@Composable
private fun FeedTab(
    feedState: Result<List<FeedEntryPair>>?,
    myUid: String,
    postText: String,
    onPostTextChange: (String) -> Unit,
    onPublish: () -> Unit,
    onToggleLike: (FeedEntry) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    when {
        feedState == null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { GameRowSkeleton() }
            }
        }
        feedState.isFailure -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No se pudo cargar el feed")
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { /* el refresco llega desde el header */ }) { Text("Reintentar desde el icono de refresco") }
        }
        else -> {
            val entries = feedState.getOrDefault(emptyList())
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CommentBar(
                        comment = postText,
                        onCommentChange = onPostTextChange,
                        onSendClick = onPublish,
                        commentMaxLength = 280
                    )
                }
                if (entries.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Aquí aparecerán los hitos de la gente que sigues",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Busca jugadores por su nombre de usuario y síguelos; o estrena el feed con tu primer post.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                items(entries, key = { it.id }) { pair ->
                    FeedCard(
                        pair = pair,
                        isMine = pair.entry.authorUid == myUid,
                        onToggleLike = { onToggleLike(pair.entry) },
                        onOpenProfile = { onOpenProfile(pair.entry.authorUid) }
                    )
                }
            }
        }
    }
}

/** Tarjeta de una entrada del feed: hito o post, con me gusta (D2.6). */
@Composable
private fun FeedCard(
    pair: FeedEntryPair,
    isMine: Boolean,
    onToggleLike: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val entry = pair.entry
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onOpenProfile),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pair.authorName.ifBlank { pair.authorUsername.ifBlank { "Jugador" } },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = pair.authorUsername.ifBlank { "" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (entry.isMilestone) {
                    Text(
                        text =                when (entry.milestoneType) {
                            MilestoneTypes.COMPLETED -> "Completó"
                            MilestoneTypes.REVIEW -> "Reseñó"
                            MilestoneTypes.LIST_PUBLIC -> "Publicó una lista"
                            else -> "Hito"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val milestoneLine = when (entry.milestoneType) {
                MilestoneTypes.COMPLETED ->
                    "se ha completado ${entry.gameName.ifBlank { "un juego" }} 🎉"
                MilestoneTypes.REVIEW ->
                    "ha reseñado ${entry.gameName.ifBlank { "un juego" }}"
                else -> entry.text
            }
            Text(text = milestoneLine, style = MaterialTheme.typography.bodyMedium)
            if (entry.rating != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Su nota: ${entry.rating}/5",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                InteractionButton(
                    icon = if (entry.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    text = "${entry.likesCount}",
                    onClick = onToggleLike,
                    isDarkMode = false
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (isMine) "tuyo" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

/** Pestaña Buscar (D2.4): por prefijo de username, con seguir/dejar de seguir. */
@Composable
private fun SearchTab(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    results: Result<List<Pair<String, UserProfile>>>?,
    myUid: String,
    followingUids: Set<String>,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Nombre de usuario (mín. 3)") },
                trailingIcon = {
                    IconButton(onClick = onSearch, enabled = searchText.trim().length >= 3) {
                        Icon(Icons.Filled.Person, contentDescription = "Buscar")
                    }
                }
            )
        }
        Spacer(Modifier.height(8.dp))
        when {
            results == null -> Text(
                "Encuentra jugadores por su nombre de usuario y sigue su progreso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            results.isFailure -> Text(
                "Escribe al menos 3 caracteres",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            else -> {
                val users = results.getOrDefault(emptyList())
                if (users.isEmpty()) {
                    Text("Sin resultados", style = MaterialTheme.typography.bodyMedium)
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // El uid viaja JUNTO al perfil (ADR-0008): nunca user.email como uid.
                    items(users, key = { it.first }) { (targetUid, user) ->
                        val isFollowing = targetUid != myUid && followingUids.contains(targetUid)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenProfile(targetUid) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.nameSurname.ifBlank { user.username }, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "@${user.username}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            if (targetUid != myUid) {
                                TextButton(onClick = {
                                    if (isFollowing) onUnfollow(targetUid) else onFollow(targetUid)
                                }) {
                                    Text(if (isFollowing) "Siguiendo" else "Seguir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialHeader(
    onRefresh: () -> Unit,
    navController: NavController
) {
    // Encabezado de la pantalla que muestra el título y botones para navegación y refresco
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium
            )
            .padding(16.dp, 10.dp, 16.dp, 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Título principal del timeline
        Text(
            text = "Timeline",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
        // Grupo de botones: navegación a la lista de amigos y refresco de datos
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.navigate("friendlist") }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Amigos",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refrescar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SocialCard(
    userUid: String,
    ownerUid: String,
    /** Nombre visible del autor; si viene vacío, la tarjeta no inventa etiquetas. */
    authorName: String = "",
    message: String,
    hora: String,
    messageID: String,
    ddbbViewModel: DDBBViewModel,
    isDarkMode: Boolean,
    onMessageDeleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // Fondo según el tema: en modo oscuro se usa un color acorde; en light, blanco.
    val backgroundColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant else Color.White

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.background(backgroundColor)
        ) {
            // Encabezado: avatar, nombre y hora
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = authorName.takeIf { it.isNotBlank() }
                            ?.take(1)?.uppercase() ?: "·",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Nombre y hora
                Column {
                    Text(
                        text = authorName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = hora,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            // Contenido del mensaje
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 15.dp)
            )
            // Divisor para separar el contenido de las acciones
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )
            // Fila de acciones (botones)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                InteractionButton(
                    icon = Icons.Default.FavoriteBorder,
                    text = "Me gusta",
                    onClick = { /* Acción para 'Me gusta' */ },
                    isDarkMode = isDarkMode
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (userUid == ownerUid) {
                    InteractionButton(
                        icon = Icons.Default.Delete,
                        text = "Borrar",
                        onClick = {
                            coroutineScope.launch {
                                ddbbViewModel.deleteMessage(ownerUid, messageID)
                                    .onSuccess { onMessageDeleted() }
                            }
                        },
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}



@Composable
private fun InteractionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    // Selección de color del botón basado en el tema actual
    val buttonColor = if (isDarkMode)
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    else
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
    // Botón de texto estilizado para acciones de interacción
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun CommentBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSendClick: () -> Unit,
    commentMaxLength: Int
) {
    // Fuente de interacción para detectar pulsaciones y aplicar animación en el botón de enviar
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value
    val sendButtonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Campo de entrada de texto para escribir el comentario
        TextField(
            value = comment,
            onValueChange = onCommentChange,
            placeholder = { Text("Haz un comentario...") },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(1.dp, RoundedCornerShape(24.dp)),
            trailingIcon = {
                // Botón de envío, animado según la interacción del usuario
                IconButton(
                    onClick = onSendClick,
                    enabled = comment.isNotBlank(),
                    modifier = Modifier.scale(sendButtonScale)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar comentario",
                        tint = if (comment.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            interactionSource = interactionSource
        )
        // Indicador de la cantidad de caracteres escritos vs. el máximo permitido
        Text(
            text = "${comment.length} / $commentMaxLength",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp, end = 8.dp)
        )
    }
}

// Preview retirada: la pantalla depende de ViewModels con inyección Hilt (UserViewModel,
// SocialViewModel) y de Firebase real; sin la factory de Hilt no es previsualizable.
// La cobertura visual de esta pantalla se hace en emulador (ver docs de verificación F2).