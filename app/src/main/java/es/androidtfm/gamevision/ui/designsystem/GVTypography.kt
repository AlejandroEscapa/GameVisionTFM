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
 * GameVision Design System — tipografía (re-anclada, ADR-0010).
 *
 * Inter como sustituta de SF Pro (recomendación del propio documento para
 * plataformas no-Apple), descargada del proveedor de Google Fonts. Reglas del
 * sistema: peso 600 en titulares (nunca 700 por defecto), el peso 500 NO existe,
 * tracking negativo en display y cuerpo ("Apple tight"), cuerpo a 17 sp (no 16).
 */

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val inter = GoogleFont("Inter")

// Fallback a sans-serif del sistema mientras llega la descarga o sin Google Play
private val GVFontFamily = FontFamily(
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = inter, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val GVTypography = Typography(
    // Display: 40/600 del documento (display-lg), tracking neutro
    displayLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.em
    ),
    // 34/600 (display-md), tracking apretado
    headlineLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.011).em
    ),
    headlineMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.011).em
    ),
    // 21/600 (tagline): el único estilo con tracking POSITIVO del sistema
    titleLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.011.em
    ),
    // 17/600 (body-strong)
    titleMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.022).em
    ),
    titleSmall = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.016).em
    ),
    // 17/400/1.47 (body): el ritmo de lectura de la marca
    bodyLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.022).em
    ),
    // 14/400 (caption)
    bodyMedium = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.016).em
    ),
    bodySmall = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.01).em
    ),
    // 14/600 (caption-strong)
    labelLarge = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.016).em
    ),
    // 12/400 (fine-print)
    labelSmall = TextStyle(
        fontFamily = GVFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 15.sp,
        letterSpacing = (-0.01).em
    )
)
