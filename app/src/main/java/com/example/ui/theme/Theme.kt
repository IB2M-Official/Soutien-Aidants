package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkEditorialPrimary,
    onPrimary = EditorialOnPrimary,
    primaryContainer = DarkEditorialPrimaryContainer,
    onPrimaryContainer = EditorialOnBackground,
    secondary = EditorialSecondary,
    onSecondary = EditorialOnSecondary,
    secondaryContainer = EditorialSecondaryContainer,
    onSecondaryContainer = EditorialOnSecondaryContainer,
    background = DarkEditorialBackground,
    onBackground = DarkEditorialOnBackground,
    surface = DarkEditorialSurface,
    onSurface = DarkEditorialOnSurface,
    surfaceVariant = EditorialSecondaryContainer,
    onSurfaceVariant = EditorialSecondary,
    outline = EditorialOutline,
    error = EditorialError
)

private val LightColorScheme = lightColorScheme(
    primary = EditorialPrimary,
    onPrimary = EditorialOnPrimary,
    primaryContainer = EditorialPrimaryContainer,
    onPrimaryContainer = EditorialOnPrimaryContainer,
    secondary = EditorialSecondary,
    onSecondary = EditorialOnSecondary,
    secondaryContainer = EditorialSecondaryContainer,
    onSecondaryContainer = EditorialOnSecondaryContainer,
    background = EditorialBackground,
    onBackground = EditorialOnBackground,
    surface = EditorialSurface,
    onSurface = EditorialOnSurface,
    surfaceVariant = EditorialSurfaceVariant,
    onSurfaceVariant = EditorialOnSurfaceVariant,
    outline = EditorialOutline,
    error = EditorialError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Par défaut désactivé pour appliquer le design Éditorial
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
