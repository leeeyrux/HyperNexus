package com.hypernexus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlue.copy(alpha = 0.2f),
    onPrimaryContainer = AccentBlue,
    
    secondary = SuccessGreen,
    onSecondary = Color.White,
    
    tertiary = WarningOrange,
    onTertiary = Color.White,
    
    background = BackgroundBlack,
    onBackground = TextPrimary,
    
    surface = CardGlass,
    onSurface = TextPrimary,
    surfaceVariant = CardGlassLight,
    onSurfaceVariant = TextSecondary,
    
    error = DangerRed,
    onError = Color.White,
    
    outline = TextTertiary,
    outlineVariant = Color(0xFF2C2C2E)
)

@Composable
fun HyperNexusTheme(
    darkTheme: Boolean = true,  // Forzamos dark siempre
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}