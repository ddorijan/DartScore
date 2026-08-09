package com.example.dartscore.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DartScoreDarkColorScheme = darkColorScheme(
    primary = GreenAccent,
    secondary = GreenAccentDark,
    tertiary = OliveAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = RedAccent
)

@Composable
fun DartScoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DartScoreDarkColorScheme,
        typography = Typography,
        content = content
    )
}