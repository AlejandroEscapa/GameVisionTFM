package es.androidtfm.gamevision.ui.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * GameVision Design System — tema (re-anclado, ADR-0010).
 *
 * Superficies blanco/pergamino y UN único azul interactivo (Action Blue). El
 * modo oscuro se DERIVA de los tiles del documento (#272729/#2A2A2C): superficie
 * casi-negra, texto pergamino, enlaces Sky Blue — no es una inversión del claro.
 * Los valores marcados DERIVADO no existen como token en el documento fuente:
 * son puentes que el documento no cubre (contenedores M3, escalera de superficies).
 */

// Acento interactivo único + variante para enlaces sobre oscuro (del documento)
val GVActionBlue = Color(0xFF0066CC)
val GVActionBlueFocus = Color(0xFF0071E3)
val GVSkyBlue = Color(0xFF2997FF)

// Superficies y tintas del documento
val GVCanvas = Color(0xFFFFFFFF)
val GVParchment = Color(0xFFF5F5F7)
val GVPearl = Color(0xFFFAFAFC)
val GVTile1 = Color(0xFF272729)
val GVTile2 = Color(0xFF2A2A2C)
val GVInk = Color(0xFF1D1D1F)
val GVMutedOnDark = Color(0xFFCCCCCC)
val GVHairline = Color(0xFFE0E0E0)
val GVDividerSoft = Color(0xFFF0F0F0)

private val DarkColors = darkColorScheme(
    primary = GVSkyBlue,
    onPrimary = Color(0xFF062033),          // DERIVADO: tinta azul oscura sobre sky (AA)
    primaryContainer = Color(0xFF0A3A66),   // DERIVADO
    onPrimaryContainer = Color(0xFFA8D8FF), // DERIVADO
    secondary = GVInk,                      // botón utilitario oscuro
    onSecondary = Color(0xFFF5F5F7),
    secondaryContainer = GVTile2,
    onSecondaryContainer = Color(0xFFF5F5F7),
    background = Color(0xFF0E0E10),         // DERIVADO: negro apagado, no puro
    onBackground = GVParchment,
    surface = Color(0xFF161619),            // DERIVADO: paso entre negro y tile-1
    onSurface = GVParchment,
    surfaceContainer = GVTile1,
    surfaceContainerLow = Color(0xFF202023),     // DERIVADO
    surfaceContainerLowest = Color(0xFF0E0E10),  // DERIVADO
    surfaceContainerHigh = GVTile2,
    surfaceContainerHighest = Color(0xFF2E2E30), // DERIVADO
    surfaceVariant = GVTile1,
    surfaceDim = Color(0xFF0E0E10),
    surfaceBright = Color(0xFF333335),           // DERIVADO
    onSurfaceVariant = GVMutedOnDark,
    outline = Color(0xFF3A3A3E),            // DERIVADO: hairline oscura
    error = Color(0xFFFF5449)
)

private val LightColors = lightColorScheme(
    primary = GVActionBlue,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E9FB),   // DERIVADO
    onPrimaryContainer = Color(0xFF00325A), // DERIVADO
    secondary = GVInk,                      // botón utilitario oscuro (Sign In/Bag)
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = GVDividerSoft,
    onSecondaryContainer = GVInk,
    background = GVCanvas,
    onBackground = GVInk,
    surface = GVCanvas,
    onSurface = GVInk,
    surfaceContainer = GVParchment,
    surfaceContainerLow = GVPearl,
    surfaceContainerLowest = GVCanvas,
    surfaceContainerHigh = GVDividerSoft,
    surfaceContainerHighest = Color(0xFFE8E8EC), // DERIVADO
    surfaceVariant = GVParchment,
    surfaceDim = Color(0xFFE8E8EC),              // DERIVADO
    surfaceBright = GVCanvas,
    onSurfaceVariant = Color(0xFF6E6E73),   // DERIVADO: gris de copy secundario
    outline = GVHairline,
    error = Color(0xFFB3261E)
)

/**
 * Tema de GameVision (sistema Apple re-anclado, ADR-0010).
 *
 * @param darkTheme true para el modo oscuro derivado de los tiles.
 * @param dynamicColor true para Material You; por defecto false: el único
 *   acento de marca manda.
 */
@Composable
fun GameVisionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = GVTypography,
        shapes = GVShapes,
        content = content
    )
}
