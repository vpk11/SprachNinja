package com.vpk.sprachninja.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaceGrayDarkColorScheme = darkColorScheme(
    primary = SpaceGrayPrimary,
    onPrimary = Color.White,
    secondary = SpaceGraySecondary,
    onSecondary = Color.Black,
    background = SpaceGrayBackground,
    onBackground = SpaceGrayText,
    surface = SpaceGrayCard,
    onSurface = SpaceGrayText,
    surfaceVariant = SpaceGrayCard,
    onSurfaceVariant = SpaceGraySecondaryText,
    outline = SpaceGrayBorder,
    error = Color(0xFFF85149)
)

private val SolarizedLightColorScheme = lightColorScheme(
    primary = SolarizedPrimary,
    onPrimary = Color.White,
    secondary = SolarizedSecondary,
    onSecondary = Color.Black,
    background = SolarizedBackground,
    onBackground = SolarizedText,
    surface = SolarizedCard,
    onSurface = SolarizedText,
    surfaceVariant = SolarizedCard,
    onSurfaceVariant = SolarizedSecondaryText,
    outline = SolarizedBorder,
    error = Color(0xFFCF222E)
)

@Composable
fun SprachNinjaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> SpaceGrayDarkColorScheme
        else -> SolarizedLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}