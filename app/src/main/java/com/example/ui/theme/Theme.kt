package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WordCraftDarkColorScheme = darkColorScheme(
    primary = WordCraftSecondary,
    onPrimary = Color.White,
    primaryContainer = WordCraftPrimaryDark,
    onPrimaryContainer = Color(0xFFDCE2FF),
    secondary = WordCraftTertiary,
    onSecondary = Color.White,
    background = WordCraftCanvasDark,
    onBackground = Color(0xFFE2E2E6),
    surface = WordCraftRibbonBgDark,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2C3038),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF495057)
)

private val WordCraftLightColorScheme = lightColorScheme(
    primary = WordCraftPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE2FF),
    onPrimaryContainer = WordCraftPrimaryDark,
    secondary = WordCraftSecondary,
    onSecondary = Color.White,
    background = WordCraftCanvasLight,
    onBackground = WordCraftTextDark,
    surface = Color.White,
    onSurface = WordCraftTextDark,
    surfaceVariant = WordCraftRibbonBgLight,
    onSurfaceVariant = WordCraftTextMuted,
    outline = WordCraftBorder
)

@Composable
fun WordCraftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) WordCraftDarkColorScheme else WordCraftLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
