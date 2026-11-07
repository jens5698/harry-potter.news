package de.hpnews.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = darkColorScheme(
    primary = Color(0xFFE8B931),       // Gold
    onPrimary = Color(0xFF2A1A00),
    secondary = Color(0xFFC9A0A6),
    primaryContainer = Color(0xFF7A1520), // Weinrot
    onPrimaryContainer = Color(0xFFFFE9C2),
    secondaryContainer = Color(0xFF4A2A30),
    onSecondaryContainer = Color(0xFFFFE9C2),
    background = Color(0xFF1B1416),
    surface = Color(0xFF1B1416),
    surfaceVariant = Color(0xFF2C2124),
    onBackground = Color(0xFFF2E6E0),
    onSurface = Color(0xFFF2E6E0),
    onSurfaceVariant = Color(0xFFD5C2BE),
)

@Composable
fun HPTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, content = content)
}
