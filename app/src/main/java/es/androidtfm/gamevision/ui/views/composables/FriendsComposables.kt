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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.androidtfm.gamevision.viewmodel.DDBBViewModel
import es.androidtfm.gamevision.data.model.Friend
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 03/03/2025
 * Descripción: 
 */

/**
 * Pantalla de amigos.
 *
 * Muestra la lista de amigos del usuario actual.
 *
 * @param isDarkTheme Indica si el tema es oscuro.
 * @param paddingValues Valores de relleno para el diseño.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para el usuario.
 * @param ddbbViewModel ViewModel para la base de datos.
 */
@Composable
fun FriendsList(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController,
    userViewModel: UserViewModel,
    ddbbViewModel: DDBBViewModel
) {
    // Identidad desde el SSOT de sesión (clave: uid — ADR-0008)
    val uid by userViewModel.currentUid.collectAsState()

    var friendsList by remember { mutableStateOf(emptyList<Friend>()) }
    var searchField by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showNoFriendFound by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Efecto para cargar la lista de amigos cuando cambia el usuario
    LaunchedEffect(uid) {
        val userUid = uid
        if (userUid.isNullOrBlank()) {
            friendsList = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        friendsList = ddbbViewModel.getFriends(userUid).getOrElse { error ->
            userViewModel.setMessage("No se pudieron cargar tus amigos: ${error.message}")
            emptyList()
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .padding(bottom = paddingValues.calculateBottomPadding() + 80.dp)
        ) {
            if (isLoading) {
                // Puedes agregar un indicador de carga si lo consideras necesario
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp, 0.dp, 0.dp, 30.dp)
                ) {
                    item {
                        // Encabezado de la lista de amigos
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
                            Text(
                                text = "Seguidos",
                                style = MaterialTheme.typography.headlineLarge,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    // Lista de amigos
                    val currentUid = uid
                    items(friendsList) { friend ->
                        if (currentUid != null) {
                            FriendItem(
                                friend = friend,
                                ownerUid = currentUid,
                                ddbbViewModel = ddbbViewModel
                            ) { updatedFriendsList ->
                                friendsList = updatedFriendsList
                            }
                        }
                    }
                }
            }
        }

        // Campo de búsqueda y mensaje de error (zona inferior de la pantalla)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = 20.dp,
                    vertical = paddingValues.calculateBottomPadding() + 15.dp
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mensaje si no se encuentra un amigo
            if (showNoFriendFound) {
                Text(
                    text = "No existe ningún amigo con ese email",
                    style = MaterialTheme.typography.titleMedium.copy(color = Color.Red),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
            // Buscador + acción de añadir. El botón es explícito (un icono dentro
            // del campo no resultaba fiable al pulsarlo y además era poco claro).
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = searchField,
                    onValueChange = {
                        searchField = it
                        showNoFriendFound = false
                    },
                    placeholder = { Text("Email de tu amigo...") },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
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
                    singleLine = true,
                )
                GVButton(
                    text = "Añadir",
                    onClick = {
                        coroutineScope.launch {
                            val friendEmail = searchField.trim()
                            val myUid = uid.orEmpty()
                            val friendUid = ddbbViewModel.findUidByEmail(friendEmail).getOrNull()
                            if (!friendUid.isNullOrBlank()) {
                                ddbbViewModel.addFriend(myUid, friendUid)
                                    .onSuccess {
                                        friendsList = ddbbViewModel.getFriends(myUid)
                                            .getOrDefault(emptyList())
                                        searchField = ""
                                        showNoFriendFound = false
                                        userViewModel.setMessage("Amigo añadido")
                                    }
                                    .onFailure {
                                        userViewModel.setMessage("No se pudo añadir: ${it.message}")
                                    }
                            } else {
                                showNoFriendFound = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend, // Datos del amigo
    ownerUid: String, // uid del usuario actual (ADR-0008)
    ddbbViewModel: DDBBViewModel, // ViewModel para manejar la base de datos
    onFriendRemoved: (List<Friend>) -> Unit // Callback para actualizar la lista
) {
    val coroutineScope = rememberCoroutineScope()
    val username = friend.username
    val friendUid = friend.uid

    // Tarjeta que representa a un amigo en la lista
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp), // Reducción del padding
        shape = RoundedCornerShape(12.dp), // Reducción del tamaño del borde redondeado
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Reducción de la elevación
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Icono del amigo (inicial del correo electrónico)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = username.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(15.dp)) // Añadido un espacio entre elementos

            // Información del amigo (nombre de usuario y correo electrónico)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically) // Alinea verticalmente los textos al centro
            ) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Amigo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Botón para eliminar al amigo
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        ddbbViewModel.removeFriend(ownerUid, friendUid)
                        val updatedFriendsList = ddbbViewModel.getFriends(ownerUid)
                            .getOrDefault(emptyList())
                        onFriendRemoved(updatedFriendsList)
                    }
                },
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

