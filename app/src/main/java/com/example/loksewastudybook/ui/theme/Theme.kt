package com.example.loksewastudybook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.loksewastudybook.data.ColorTheme

private val LightColorScheme = lightColorScheme(
    primary = LoksewaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EAF6),
    onPrimaryContainer = LoksewaPrimary,
    secondary = LoksewaSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFCE4EC),
    onSecondaryContainer = LoksewaSecondary,
    tertiary = LoksewaGold,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = Color.White,
    primaryContainer = SepiaSurfaceVariant,
    onPrimaryContainer = SepiaOnSurface,
    secondary = Color(0xFF6D4C41),
    onSecondary = Color.White,
    tertiary = Color(0xFF8D6E63),
    background = SepiaBackground,
    onBackground = SepiaOnSurface,
    surface = SepiaSurface,
    onSurface = SepiaOnSurface,
    surfaceVariant = SepiaSurfaceVariant,
    onSurfaceVariant = SepiaOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFFF472B6),
    onSecondary = Color(0xFF4C0519),
    tertiary = LoksewaGoldLight,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

@Composable
fun LoksewaStudyBookTheme(
    selectedTheme: ColorTheme = ColorTheme.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (selectedTheme) {
        ColorTheme.LIGHT -> LightColorScheme
        ColorTheme.SEPIA -> SepiaColorScheme
        ColorTheme.DARK -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
