package com.pronaycoding.blankee.core.ui.theme

/**
 * Material Design 3 theme system for the Blankee application.
 *
 * This file defines:
 * - Dark and Light color schemes
 * - Theme composition with Material Design 3
 * - Support for system theme following device settings
 * - Dynamic color support on Android 12+
 *
 * The theme is designed to evoke calm and relaxation, fitting the app's sleep/ambient sound purpose.
 *
 * Color scheme selection logic:
 * 1. Check user preference (light/dark/system)
 * 2. If system mode, follow device settings
 * 3. Apply dynamic colors if available (Android 12+) and enabled
 * 4. Fall back to custom light/dark color schemes
 *
 * System UI integration:
 * - Status bar color matches surface color
 * - Status bar icons adapt to theme brightness
 *
 * @see androidx.compose.material3.MaterialTheme for Material Design theme
 * @see PreferenceManagerRepository for theme preference storage
 */

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl

// Dark theme - Calm and minimalist like Blanket app
private val DarkColorScheme =
    darkColorScheme(
        primary = Primary, // Soft Blue
        onPrimary = Color.White,
        primaryContainer = PrimaryDark,
        onPrimaryContainer = Color.White,
        secondary = Secondary, // Cyan
        onSecondary = Color.White,
        secondaryContainer = SecondaryDark,
        onSecondaryContainer = Color.White,
        tertiary = Tertiary, // Soft Green
        onTertiary = Color.White,
        tertiaryContainer = TertiaryLight,
        onTertiaryContainer = Color.White,
        error = Error,
        onError = Color.White,
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color(0xFF410E0B),
        background = DarkBackground, // Almost black
        onBackground = LightText,
        surface = DarkSurface, // Dark card background
        onSurface = LightText,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = TextSecondary,
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFF49454E),
    )

// AMOLED theme
private val AmoledColorScheme =
    DarkColorScheme.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceVariant = Color(0xFF121212),
        surfaceContainerLow = Color.Black,
        surfaceContainer = Color(0xFF0A0A0A),
        surfaceContainerHigh = Color(0xFF121212),
        surfaceContainerHighest = Color(0xFF1E1E1E),
        surfaceTint = Color.Black,
    )

// Light theme
private val LightColorScheme =
    lightColorScheme(
        primary = Primary, // Soft Blue
        onPrimary = Color.White,
        primaryContainer = PrimaryLight,
        onPrimaryContainer = Color(0xFF062D5E),
        secondary = Secondary, // Cyan
        onSecondary = Color.White,
        secondaryContainer = SecondaryLight,
        onSecondaryContainer = Color(0xFF003D47),
        tertiary = Tertiary, // Soft Green
        onTertiary = Color.White,
        tertiaryContainer = TertiaryLight,
        onTertiaryContainer = Color.White,
        error = Error,
        onError = Color.White,
        errorContainer = Color(0xFFFCDEDB),
        onErrorContainer = Color(0xFF370B1E),
        background = LightBackground, // Off-white
        onBackground = DarkText,
        surface = LightSurface, // Pure white
        onSurface = DarkText,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = TextSecondary,
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC7D0),
    )

@Composable
fun BlankeeAppTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val preferenceManager = PreferenceManagerRepositoryImpl(context)
    val systemDark = isSystemInDarkTheme()
    val themeMode = preferenceManager.getThemeModeBlocking(Constants.MODE_DARK)
    val amoledDark = preferenceManager.getAmoledDarkBlocking(false)
    val darkTheme =
        when (themeMode) {
            Constants.MODE_LIGHT -> false
            Constants.MODE_SYSTEM -> systemDark
            else -> true
        }

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme && amoledDark -> AmoledColorScheme
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        @Suppress("DEPRECATION")
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)!!.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
