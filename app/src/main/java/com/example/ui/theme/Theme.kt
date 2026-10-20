package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = MidnightBackground,
    primaryContainer = MidnightSurfaceVariant,
    onPrimaryContainer = AmberGold,
    secondary = WarmBronze,
    onSecondary = Color.White,
    background = MidnightBackground,
    onBackground = MoonlightPrimary,
    surface = MidnightSurface,
    onSurface = MoonlightPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = MoonlightSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = DarkAmber,
    onPrimary = Color.White,
    primaryContainer = PaperSurfaceVariant,
    onPrimaryContainer = InkPrimary,
    secondary = WarmBronze,
    onSecondary = Color.White,
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkSecondary
)

@Composable
fun LibrisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
