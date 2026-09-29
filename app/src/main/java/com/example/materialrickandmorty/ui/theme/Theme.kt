package com.example.materialrickandmorty.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RickAndMortyDarkColorScheme = darkColorScheme(
    primary = IconPurple,
    onPrimary = CardContainer,
    primaryContainer = NavSelectionPill,
    onPrimaryContainer = IconPurple,
    secondary = CardText,
    onSecondary = CardContainer,
    background = DarkBackground,
    onBackground = HeaderTitleText,
    surface = CardContainer,
    onSurface = IconPurple,
    surfaceVariant = CardContainer,
    onSurfaceVariant = CardText,
    outline = CardText
)

@Composable
fun MaterialrickandmortyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = RickAndMortyDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
