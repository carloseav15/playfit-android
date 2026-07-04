package com.carlosarancibia.playfit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
data class ExtendedColors(
    val playfitBackground: Color,
    val playfitForeground: Color,
    val playfitAccent: Color,
    val playfitInk: Color,
    val playfitPositive: Color,
    val playfitWarning: Color,
    val playfitNegative: Color,
    val playfitToneAccent: Color,
    val playfitIndigo: Color,
)

private val lightExtended = ExtendedColors(
    playfitBackground = Color(0xFFF8FAFC),
    playfitForeground = Color(0xFF0F172A),
    playfitAccent = Color(0xFF0F766E),
    playfitInk = Color(0xFF0D9488),
    playfitPositive = Color(0xFF047857),
    playfitWarning = Color(0xFFB45309),
    playfitNegative = Color(0xFFBE123C),
    playfitToneAccent = Color(0xFF0369A1),
    playfitIndigo = Color(0xFF4F46E5),
)

private val darkExtended = ExtendedColors(
    playfitBackground = Color(0xFF070A12),
    playfitForeground = Color(0xFFF8FAFC),
    playfitAccent = Color(0xFFFF6A3D),
    playfitInk = Color(0xFF38BDF8),
    playfitPositive = Color(0xFF34D399),
    playfitWarning = Color(0xFFFBBF24),
    playfitNegative = Color(0xFFFB7185),
    playfitToneAccent = Color(0xFF7DD3FC),
    playfitIndigo = Color(0xFF4F46E5),
)

val LocalExtendedColors = staticCompositionLocalOf { lightExtended }

object PlayfitExtendedTheme {
    val colors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF0F766E).copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF0F766E),
    secondary = Color(0xFF0F766E),
    tertiary = Color(0xFF4F46E5),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFF8FAFC),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5F9),
    surfaceContainer = Color(0xFFE2E8F0),
    surfaceContainerHigh = Color(0xFFCBD5E1),
    surfaceContainerHighest = Color(0xFF94A3B8),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0),
    error = Color(0xFFBE123C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFBE123C).copy(alpha = 0.12f),
    onErrorContainer = Color(0xFFBE123C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF6A3D),
    onPrimary = Color(0xFF070A12),
    primaryContainer = Color(0xFFFF6A3D).copy(alpha = 0.15f),
    onPrimaryContainer = Color(0xFFFF6A3D),
    secondary = Color(0xFFFF6A3D),
    tertiary = Color(0xFF38BDF8),
    background = Color(0xFF070A12),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF070A12),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceContainerLowest = Color(0xFF0F172A),
    surfaceContainerLow = Color(0xFF0F172A),
    surfaceContainer = Color(0xFF1E293B),
    surfaceContainerHigh = Color(0xFF334155),
    surfaceContainerHighest = Color(0xFF475569),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B),
    error = Color(0xFFFB7185),
    onError = Color(0xFF070A12),
    errorContainer = Color(0xFFFB7185).copy(alpha = 0.15f),
    onErrorContainer = Color(0xFFFB7185),
)

val PlayfitTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1.0).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.25).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    )
)

@Composable
fun PlayfitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val extendedColors = if (darkTheme) darkExtended else lightExtended
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = PlayfitTypography,
            content = content,
        )
    }
}
