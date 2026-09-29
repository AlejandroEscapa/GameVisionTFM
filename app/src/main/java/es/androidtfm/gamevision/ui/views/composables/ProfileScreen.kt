package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.ui.designsystem.components.GVSkeleton
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 18/01/2025
 * Descripción:
 */

/**
 * Pantalla de perfil del usuario logueado.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param paddingValues Valores de padding para la pantalla.
 * @param navController Controlador de navegación.
 * @param userViewModel ViewModel para manejar los datos del usuario.
 * @param googleViewModel ViewModel para manejar la autenticación con Google.
 * @param onThemeChange Función para cambiar el tema.
 */
@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    paddingValues: PaddingValues,
    navController: NavController?, // Se usa para acceder al SavedStateHandle y para la navegación
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    onThemeChange: (Boolean) -> Unit
) {
    // Perfil en vivo desde el SSOT (UserViewModel.profile): se actualiza solo
    // cuando cambia el documento en Firestore, sin refetch manual por pantalla.
    val profile by userViewModel.profile.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val profileImageData by userViewModel.profileImageData.collectAsState()
    val imageError by userViewModel.imageError.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Imagen a mostrar: la gestionada por la app (data URI desde Firestore) o, si no,
    // una URL antigua que ya estuviera guardada en el perfil.
    val imageModel: String? = profileImageData
        ?: profile.imageUri.takeIf { it.isNotBlank() && !it.startsWith("firestore://") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Sección del encabezado con fondo gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            ) {
                // Botón para cambiar el tema
                val iconTextColor = if (isDarkTheme) Color.White else Color.Black

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { onThemeChange(!isDarkTheme) }
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.daynightthemewhite
                                else R.drawable.daynightthemeblack
                            ),
                            contentDescription = "Theme",
                            tint = iconTextColor
                        )
                    }
                    Text(
                        text = if (isDarkTheme) "Modo noche" else "Modo día",
                        fontSize = 12.sp,
                        color = iconTextColor
                    )
                }
            }

            if (isLoading) {
                // Indicador de carga mientras se obtiene la información del perfil
                ProfileLoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 60.dp, bottom = 20.dp)
                ) {
                    // Imagen de perfil con efecto de offset
                    Card(
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.CenterHorizontally)
                            .offset(y = (-25).dp)
                    ) {
                        ProfileImage(
                            imageUri = imageModel,
                            onImagePicked = { uri ->
                                coroutineScope.launch { userViewModel.updateProfileImage(uri) }
                            }
                        )
                    }

                    // Aviso si la foto elegida no cumple el límite de tamaño (escalabilidad del plan gratuito).
                    if (imageError != null) {
                        ProfileImageErrorBanner(
                            message = imageError.orEmpty(),
                            onDismiss = { userViewModel.clearImageError() },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    // Sección del encabezado del perfil
                    ProfileHeaderSection(
                        name = profile.nameSurname,
                        username = profile.username,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Tarjeta de detalles del perfil
                    ProfileDetailsCard(
                        description = profile.description,
                        country = profile.country,
                        email = profile.email
                    )

                    // Sección de acciones del perfil (incluye el botón de "Cerrar sesión")
                    ProfileActionsSection(
                        navController = navController,
                        userViewModel = userViewModel,
                        googleViewModel = googleViewModel,
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    // Espacio adicional
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileImage(
    imageUri: String?,
    onImagePicked: (Uri) -> Unit
) {    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // F0/T0.12: la imagen se sube a Storage desde el ViewModel; aquí solo se
        // entrega el Uri de contenido. La foto ya no se copia a filesDir (antes la
        // ruta local se guardaba en Firestore y no viajaba entre dispositivos).
        uri?.let(onImagePicked)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!imageUri.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { pickImageLauncher.launch("image/*") }
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Añadir foto de perfil",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .clickable { pickImageLauncher.launch("image/*") }
            )
        }
    }
}

@Composable
private fun ProfileImageErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    name: String,
    username: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "@$username",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileDetailsCard(description: String, country: String, email: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Biografía",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = if (description.isEmpty()) "No description provided" else description,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailItem(
                icon = Icons.Default.LocationOn,
                title = "Location",
                value = if (country.isEmpty()) "Not specified" else country
            )
            ProfileDetailItem(
                icon = Icons.Default.Email,
                title = "Contact",
                value = email
            )
        }
    }
}

@Composable
private fun ProfileDetailItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ProfileActionsSection(
    navController: NavController?,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        // Botón para ver la lista de amigos
        FilledTonalButton(
            onClick = { navController?.navigate("friendlist") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Seguidos")
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Botón para editar el perfil
        FilledTonalButton(
            onClick = { navController?.navigate("editProfile") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Editar perfil")
        }
        Spacer(modifier = Modifier.height(12.dp))
        // Botón para cerrar sesión
        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    // El cierre de sesión pasa por el SSOT: cierra Firebase Auth,
                    // limpia credenciales de Google y el estado local.
                    googleViewModel.clearCredentialState()
                    userViewModel.signOut()
                    navController?.navigate("main") {
                        popUpTo(id = navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun ProfileLoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GVSkeleton(shape = CircleShape, width = 140.dp, height = 140.dp)
        GVSkeleton(width = 160.dp, height = 18.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 120.dp)
        GVSkeleton(Modifier.fillMaxWidth(), height = 48.dp)
    }
}

