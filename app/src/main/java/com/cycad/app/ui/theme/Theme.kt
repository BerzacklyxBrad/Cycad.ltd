package com.cycad.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LegoColorScheme = lightColorScheme(
    primary = PrimaryLego,
    onPrimary = OnPrimaryLego,
    primaryContainer = PrimaryContainerLego,
    onPrimaryContainer = OnPrimaryContainerLego,
    secondary = SecondaryLego,
    onSecondary = OnSecondaryLego,
    tertiary = TertiaryLego,
    onTertiary = OnTertiaryLego,
    background = BackgroundLego,
    surface = SurfaceLego,
    onBackground = LegoBlack,
    onSurface = LegoBlack,
)

// We can define a darker version if needed, but for "Lego" branding, 
// the bright primary colors are usually preferred even in dark mode 
// or we just stick to a vibrant light theme.
private val DarkLegoColorScheme = darkColorScheme(
    primary = PrimaryLego,
    onPrimary = OnPrimaryLego,
    secondary = SecondaryLego,
    background = LegoBlack,
    surface = Color(0xFF1A1A1A),
    onBackground = LegoWhite,
    onSurface = LegoWhite
)

@Composable
fun CYCADTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to enforce the LEGO branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkLegoColorScheme else LegoColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
