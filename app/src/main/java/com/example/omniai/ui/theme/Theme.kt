package com.omniai.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryPurple,
    tertiary = AccentPink,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SystemGray6,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    primaryContainer = Color(0xFFE3F2FD),
    secondaryContainer = Color(0xFFF3E5F5),
    tertiaryContainer = Color(0xFFFFE4E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SecondaryPurple,
    tertiary = AccentPink,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = Color(0xFF2C2C2E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF1C3A57),
    secondaryContainer = Color(0xFF2C1C3A),
    tertiaryContainer = Color(0xFF3A1C24)
)

@Composable
fun OmniAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}