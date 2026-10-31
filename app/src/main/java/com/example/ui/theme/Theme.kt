package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SazfloyColorScheme = darkColorScheme(
    primary = SazfloyCyan,
    onPrimary = SazfloyBackground,
    primaryContainer = SazfloySurfaceElevated,
    onPrimaryContainer = SazfloyCyan,
    secondary = SazfloyViolet,
    onSecondary = SazfloyTextPrimary,
    secondaryContainer = SazfloySurfaceVariant,
    onSecondaryContainer = SazfloyVioletLight,
    tertiary = SazfloyPink,
    onTertiary = SazfloyTextPrimary,
    background = SazfloyBackground,
    onBackground = SazfloyTextPrimary,
    surface = SazfloySurface,
    onSurface = SazfloyTextPrimary,
    surfaceVariant = SazfloySurfaceVariant,
    onSurfaceVariant = SazfloyTextSecondary,
    outline = SazfloyBorder,
    outlineVariant = SazfloyDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // SAZFLOY default is professional dark studio theme
    dynamicColor: Boolean = false, // Preserve dedicated brand aesthetic
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SazfloyBackground.toArgb()
                window.navigationBarColor = SazfloyBackground.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SazfloyColorScheme,
        typography = Typography,
        content = content
    )
}
