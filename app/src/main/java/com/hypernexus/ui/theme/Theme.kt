package com.hypernexus.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// UNIFICACIÓN EN CALIENTE DE LA PALETA APPLE DARK SETTINGS
private val ThemeAccentBlue = Color(0xFF007AFF)
private val ThemeSuccessGreen = Color(0xFF30D158)
private val ThemeWarningOrange = Color(0xFFFF9500)
private val ThemeDangerRed = Color(0xFFFF3B30)
private val ThemeBackgroundBlack = Color(0xFF000000)
private val ThemeCardGlass = Color(0xFF1C1C1E)
private val ThemeTextPrimary = Color(0xFFWhite.value) // Fallback seguro a Blanco Puro
private val ThemeTextSecondary = Color(0xFF8E8E93)

private val DarkColorScheme = darkColorScheme(
    primary = ThemeAccentBlue,
    onPrimary = Color.White,
    primaryContainer = ThemeAccentBlue.copy(alpha = 0.2f),
    onPrimaryContainer = ThemeAccentBlue,
    
    secondary = ThemeSuccessGreen,
    onSecondary = Color.White,
    
    tertiary = ThemeWarningOrange,
    onTertiary = Color.White,
    
    background = ThemeBackgroundBlack,
    onBackground = Color.White,
    
    surface = ThemeCardGlass,
    onSurface = Color.White,
    surfaceVariant = ThemeCardGlass.copy(alpha = 0.8f),
    onSurfaceVariant = ThemeTextSecondary,
    
    error = ThemeDangerRed,
    onError = Color.White,
    
    outline = ThemeTextSecondary,
    outlineVariant = Color(0xFF2C2C2E)
)

@Composable
fun HyperNexusTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
