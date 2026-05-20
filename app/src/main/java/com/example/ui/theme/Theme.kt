package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MonochromeWhite,
    onPrimary = MonochromeBlack,
    secondary = DarkGray,
    onSecondary = MonochromeWhite,
    background = MonochromeBlack,
    onBackground = MonochromeWhite,
    surface = MonochromeBlack,
    onSurface = MonochromeWhite,
    outline = DarkGray
)

private val LightColorScheme = lightColorScheme(
    primary = MonochromeBlack,
    onPrimary = MonochromeWhite,
    secondary = LightGray,
    onSecondary = MonochromeBlack,
    background = MonochromeWhite,
    onBackground = MonochromeBlack,
    surface = MonochromeWhite,
    onSurface = MonochromeBlack,
    outline = LightGray
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
