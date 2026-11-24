package com.example.cliniclearai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MedicalTealPrimaryDark,
    onPrimary = MedicalTealOnContainerLight,
    primaryContainer = MedicalTealContainerDark,
    onPrimaryContainer = MedicalTealOnContainerDark,

    secondary = SlateNavySecondaryDark,
    onSecondary = SlateNavyOnContainerLight,
    secondaryContainer = SlateNavyContainerDark,
    onSecondaryContainer = SlateNavyOnContainerDark,

    tertiary = HealingMintTertiaryDark,
    onTertiary = HealingMintOnContainerLight,
    tertiaryContainer = HealingMintContainerDark,
    onTertiaryContainer = HealingMintOnContainerDark,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    error = StatusCritical,
    onError = SurfaceLight,
    errorContainer = StatusCriticalContainer,
    onErrorContainer = StatusCriticalOnContainer,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = MedicalTealContainerLight,
    onPrimaryContainer = MedicalTealOnContainerLight,

    secondary = SlateNavySecondary,
    onSecondary = SurfaceLight,
    secondaryContainer = SlateNavyContainerLight,
    onSecondaryContainer = SlateNavyOnContainerLight,

    tertiary = HealingMintTertiary,
    onTertiary = SurfaceLight,
    tertiaryContainer = HealingMintContainerLight,
    onTertiaryContainer = HealingMintOnContainerLight,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,

    error = StatusCritical,
    onError = SurfaceLight,
    errorContainer = StatusCriticalContainer,
    onErrorContainer = StatusCriticalOnContainer,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

@Composable
fun CliniClearAITheme(
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
