package com.sourdough.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colours taken from the launcher icon: a baked crust on a cream background.

private val LightColors = lightColorScheme(
    primary = Color(0xFF8B5A2B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF0D2AE),
    onPrimaryContainer = Color(0xFF3B2410),
    secondary = Color(0xFFB9783F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEBD9BC),
    onSecondaryContainer = Color(0xFF3B2A17),
    tertiary = Color(0xFF6E5A2E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2E1B0),
    onTertiaryContainer = Color(0xFF2E2406),
    background = Color(0xFFFBF4E8),
    onBackground = Color(0xFF2E2118),
    surface = Color(0xFFFBF4E8),
    onSurface = Color(0xFF2E2118),
    surfaceVariant = Color(0xFFF4E4C8),
    onSurfaceVariant = Color(0xFF5C4632),
    surfaceContainerLowest = Color(0xFFFFFBF4),
    surfaceContainerLow = Color(0xFFF9F0E1),
    surfaceContainer = Color(0xFFF6EAD6),
    surfaceContainerHigh = Color(0xFFF4E4C8),
    surfaceContainerHighest = Color(0xFFEFDDBE),
    outline = Color(0xFF8C7660),
    outlineVariant = Color(0xFFDCC8AC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE8B987),
    onPrimary = Color(0xFF4A2A0C),
    primaryContainer = Color(0xFF6B4420),
    onPrimaryContainer = Color(0xFFFFDDB8),
    secondary = Color(0xFFD9A774),
    onSecondary = Color(0xFF452B12),
    secondaryContainer = Color(0xFF4F3B26),
    onSecondaryContainer = Color(0xFFF4DEC0),
    tertiary = Color(0xFFD9C38E),
    onTertiary = Color(0xFF3B2F0B),
    tertiaryContainer = Color(0xFF544620),
    onTertiaryContainer = Color(0xFFF6E1A8),
    background = Color(0xFF1E1611),
    onBackground = Color(0xFFF1E3D0),
    surface = Color(0xFF1E1611),
    onSurface = Color(0xFFF1E3D0),
    surfaceVariant = Color(0xFF4F4032),
    onSurfaceVariant = Color(0xFFD8C3AA),
    surfaceContainerLowest = Color(0xFF18110C),
    surfaceContainerLow = Color(0xFF261D16),
    surfaceContainer = Color(0xFF2B211A),
    surfaceContainerHigh = Color(0xFF362B22),
    surfaceContainerHighest = Color(0xFF41352B),
    outline = Color(0xFFA08D78),
    outlineVariant = Color(0xFF4F4032),
)

@Composable
fun SourdoughTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
