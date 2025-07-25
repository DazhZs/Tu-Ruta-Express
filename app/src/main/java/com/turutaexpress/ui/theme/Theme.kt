package com.turutaexpress.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryJaguar,
    onPrimary = DarkOnPrimaryJaguar,
    primaryContainer = DarkPrimaryContainerJaguar,
    onPrimaryContainer = DarkOnPrimaryContainerJaguar,
    secondary = DarkSecondaryJaguar,
    onSecondary = DarkOnSecondaryJaguar,
    secondaryContainer = DarkSecondaryContainerJaguar,
    onSecondaryContainer = DarkOnSecondaryContainerJaguar,
    background = DarkBackgroundJaguar,
    onBackground = DarkOnBackgroundJaguar,
    surface = DarkSurfaceJaguar,
    onSurface = DarkOnSurfaceJaguar,
    error = DarkErrorJaguar,
    // --- INICIO DE LA CORRECCIÓN ---
    // onError debe ser un color definido. En este caso, el texto sobre el error en modo oscuro.
    onError = Color(0xFF690005),
    // --- FIN DE LA CORRECCIÓN ---
    surfaceVariant = DarkSurfaceVariantJaguar,
    onSurfaceVariant = DarkOnSurfaceVariantJaguar,
    outline = DarkOutlineJaguar,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryJaguar,
    onPrimary = OnPrimaryJaguar,
    primaryContainer = PrimaryContainerJaguar,
    onPrimaryContainer = OnPrimaryContainerJaguar,
    secondary = SecondaryJaguar,
    onSecondary = OnSecondaryJaguar,
    secondaryContainer = SecondaryContainerJaguar,
    onSecondaryContainer = OnSecondaryContainerJaguar,
    background = BackgroundJaguar,
    onBackground = OnBackgroundJaguar,
    surface = SurfaceJaguar,
    onSurface = OnSurfaceJaguar,
    error = ErrorJaguar,
    // --- INICIO DE LA CORRECCIÓN ---
    // onError para el modo claro. Generalmente es blanco.
    onError = Color(0xFFFFFFFF),
    // --- FIN DE LA CORRECCIÓN ---
    surfaceVariant = SurfaceVariantJaguar,
    onSurfaceVariant = OnSurfaceVariantJaguar,
    outline = OutlineJaguar,
)

@Composable
fun TuRutaExpressTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}