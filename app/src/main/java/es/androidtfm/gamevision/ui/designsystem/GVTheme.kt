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
 * GameVision Design System — tema (ver DESIGN.md §2).
 *
 * Monocromo cálido + spot verde ácido. El color dinámico de Material You es
 * opcional (opt-in) y la identidad de marca es el fallback y el default.
 */

// Spot color: verde ácido (uso restringido: CTA, rating, foco)
val GVAcid = Color(0xFFC8F135)
val GVAcidContainer = Color(0xFF2E3A0A)
val GVAcidOnContainer = Color(0xFFE4FF87)

private val DarkColors = darkColorScheme(
    primary = GVAcid,
    onPrimary = Color(0xFF0D0D0D),
    primaryContainer = GVAcidContainer,
    onPrimaryContainer = GVAcidOnContainer,
    secondary = Color(0xFF8C8C8C),
    onSecondary = Color(0xFF0D0D0D),
    secondaryContainer = Color(0xFF242424),
    onSecondaryContainer = Color(0xFFF2F2F2),
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF2F2F2),
    surfaceContainer = Color(0xFF242424),
    surfaceContainerLow = Color(0xFF1F1F1F),
    surfaceContainerLowest = Color(0xFF141414),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF333333),
    surfaceVariant = Color(0xFF242424),
    surfaceDim = Color(0xFF0D0D0D),
    surfaceBright = Color(0xFF333333),
    onSurfaceVariant = Color(0xFF8C8C8C),
    outline = Color(0xFF2E2E2E),
    error = Color(0xFFFF5449)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A6B00),
    onPrimary = Color(0xFFF2F2F2),
    primaryContainer = Color(0xFFE4FF87),
    onPrimaryContainer = Color(0xFF1E2600),
    secondary = Color(0xFF5A5A5A),
    onSecondary = Color(0xFFF2F2F2),
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = Color(0xFF0D0D0D),
    background = Color(0xFFF2F2F2),
    onBackground = Color(0xFF0D0D0D),
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF0D0D0D),
    surfaceContainer = Color(0xFFEDEDED),
    surfaceContainerLow = Color(0xFFF2F2F2),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE6E6E6),
    surfaceContainerHighest = Color(0xFFD9D9D9),
    surfaceVariant = Color(0xFFE6E6E6),
    surfaceDim = Color(0xFFD6D6D6),
    surfaceBright = Color(0xFFFAFAFA),
    onSurfaceVariant = Color(0xFF5A5A5A),
    outline = Color(0xFFD4D4D4),
    error = Color(0xFFB3261E)
)

/**
 * Tema de GameVision. Sustituye al antiguo AppTheme.
 *
 * @param darkTheme true para tema oscuro (identidad principal).
 * @param dynamicColor true para Material You (extrae la paleta del wallpaper);
 *   por defecto false: la identidad verde/monocromo manda.
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
