package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ZeedColorScheme = darkColorScheme(
    primary = ZeedCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A0A10),
    onPrimaryContainer = Color(0xFFFFD9DD),
    secondary = ZeedGold4K,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF422E00),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = ZeedCyanNeon,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF00363D),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background = CinemaDarkBackground,
    onBackground = TextPrimary,
    surface = CinemaSurface,
    onSurface = TextPrimary,
    surfaceVariant = CinemaSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CinemaCardBorder,
    outlineVariant = Color(0xFF1E2638)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ZeedColorScheme,
        typography = Typography,
        content = content
    )
}
