package es.androidtfm.gamevision.ui.views.composables

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.androidtfm.gamevision.R
import es.androidtfm.gamevision.ui.designsystem.GVShapeFull
import es.androidtfm.gamevision.ui.designsystem.GVShapes
import es.androidtfm.gamevision.ui.designsystem.GVSpacing
import es.androidtfm.gamevision.ui.designsystem.components.GVButton
import es.androidtfm.gamevision.viewmodel.GoogleViewModel
import es.androidtfm.gamevision.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Mail

/*
 * Autor: Alejandro Olivares Escapa
 * Fecha: 17/01/2025
 * Descripción: 
 */

/**
 * Pantalla de login de la aplicación.
 *
 * @param isDarkTheme Indica si el tema oscuro está activado.
 * @param navController Controlador de navegación.
 * @param navconThemeChange Función para cambiar el tema.
 * @param userViewModel ViewModel para datos de usuario.
 * @param googleViewModel ViewModel para operaciones con Google.
 * @param onGoogleSignInClick Función para iniciar sesión con Google.
 */

@Composable
fun LoginScreen(
    isDarkTheme: Boolean,
    navController: NavController,
    navconThemeChange: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    googleViewModel: GoogleViewModel,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Estado del formulario y mensajes; la sesión y el perfil viven en el SSOT
    // (UserViewModel.session / UserViewModel.profile)
    val formFields by userViewModel.formFields.collectAsStateWithLifecycle()
    val message by userViewModel.message.collectAsStateWithLifecycle()
    val signInState by googleViewModel.signInState.observeAsState()

    // Autocompletado: recupera la credencial guardada en el gestor de contraseñas
    LaunchedEffect(Unit) {
        userViewModel.retrieveSavedPassword(context)?.let { saved ->
            if (formFields["email"].isNullOrBlank()) {
                userViewModel.prefillLogin(saved.email, saved.password)
            }
        }
    }

    // El error de Google Sign-In se muestra con el mismo canal de mensajes
    LaunchedEffect(signInState) {
        val state = signInState
        if (state is GoogleViewModel.SignInState.Error) {
            userViewModel.setMessage(state.message)
        }
    }

    // Diseño principal de la pantalla de inicio de sesión
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Encabezado de la pantalla de inicio de sesión
            LoginHeader(isDarkTheme)

            // Formulario de inicio de sesión
            LoginForm(
                formFields = formFields,
                message = message,
                onEmailChange = { userViewModel.onFormFieldChange("email", it) },
                onPasswordChange = { userViewModel.onFormFieldChange("password", it) },
                onLoginClick = {
                    userViewModel.clearMessage()
                    // El login corre en el ViewModel (sobrevive a la navegación)
                    userViewModel.signIn(
                        context = context,
                        email = formFields["email"].orEmpty(),
                        password = formFields["password"].orEmpty()
                    )
                },
                onForgotPasswordClick = { navController.navigate("passrecover") },
                onGoogleSignInClick = {
                    onGoogleSignInClick()
                    googleViewModel.signIn(context)
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
    }
}

@Composable
private fun LoginHeader(isDarkTheme: Boolean) {
    // Encabezado: TEXTO SIMPLE, como manda el sistema. Antes era "Bienvenido" en
    // azul de acción con peso Black (900) — un peso que no existe en GVTypography
    // y que además convertía el acento en decoración en vez de acción.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = GVSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(GVSpacing.sm))
        Text(
            text = "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(GVSpacing.md))
    }
}

/**
 * Formulario de inicio de sesión.
 *
 * @param formFields Campos del formulario.
 * @param message Mensaje de error o información.
 * @param onEmailChange Función para cambiar el correo electrónico.
 * @param onPasswordChange Función para cambiar la contraseña.
 * @param onLoginClick Función para iniciar sesión.
 * @param onForgotPasswordClick Función para recuperar la contraseña.
 * @param onGoogleSignInClick Función para iniciar sesión con Google.
 * @param onRegisterClick Función para navegar a la pantalla de registro.
 */

@Composable
private fun LoginForm(
    formFields: Map<String, String>,
    message: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Tarjeta del formulario re-anclada al sistema: radio 18 (GVShapes.large), SIN
    // sombra (la separación la da el tono de superficie) y superficie de
    // contenedor en vez de `surfaceVariant` (vocabulario M2).
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GVSpacing.screenPadding)
            .padding(top = GVSpacing.sm),
        shape = GVShapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(GVSpacing.lg)
                .fillMaxWidth()
        ) {
            // Mensaje de error o información. Antes: `onErrorContainer` sobre
            // `errorContainer` al 20% — dos rosas del mismo tono que daban ~3,3:1,
            // o sea que el mensaje que HAY que leer no pasaba AA. Ahora fondo
            // sólido del rol de error y su tinta correspondiente.
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(bottom = GVSpacing.lg)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = GVShapes.medium
                        )
                        .padding(GVSpacing.md)
                )
            }
            // Campo de correo electrónico
            OutlinedTextField(
                value = formFields["email"] ?: "",
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = GVShapes.medium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.Mail,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(GVSpacing.md))

            // Campo de contraseña
            OutlinedTextField(
                value = formFields["password"] ?: "",
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = GVShapes.medium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                leadingIcon = {
                    Icon(
                        imageVector = Lucide.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Lucide.EyeOff else Lucide.Eye,
                            contentDescription = if (passwordVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // Enlace para recuperar contraseña
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = GVSpacing.xs)
            ) {
                Text(
                    "He olvidado mi contraseña",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(Modifier.height(GVSpacing.sm))

            // Botón de inicio de sesión: píldora del sistema (GVButton), que es la
            // forma reservada a la ACCIÓN.
            GVButton(
                text = "Iniciar sesión",
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(GVSpacing.xl))

            // Divisor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "o continúa con",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = GVSpacing.sm)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Spacer(Modifier.height(GVSpacing.xl))

            // Google: acción SECUNDARIA (píldora outline), no un primario más.
            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = GVShapeFull,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.android_light_rd_na),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(GVSpacing.md))
                    Text(
                        "Continuar con Google",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Enlace para registrarse
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRegisterClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        "¿No tienes cuenta? Regístrate",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

