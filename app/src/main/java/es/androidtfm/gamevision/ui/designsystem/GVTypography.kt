package es.androidtfm.gamevision.ui.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import es.androidtfm.gamevision.R

/*
 * GameVision Design System — tipografía (ver DESIGN.md §3).
 *
 * Space Grotesk como única familia (grotesca de carácter, variable), descargada
 * del proveedor de Google Fonts. Jerarquía por tamaño y peso, tracking apretado
 * en display.
 */

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val spaceGrotesk = GoogleFont("Space Grotesk")

// Fallback a sans-serif del sistema mientras llega la descarga o sin Google Play
private val GVFontFamily = FontFamily(
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = spaceGrotesk, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val GVTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em
    ),
    headlineMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.02).em
    ),
    titleLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.01).em
    ),
    titleMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.02.em
    ),
    labelSmall = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.05.em
    )
)
