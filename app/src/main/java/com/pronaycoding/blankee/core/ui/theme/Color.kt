package com.pronaycoding.blankee.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Material Design 3 Color Palette for the Blankee application.
 *
 * The color scheme is designed to evoke calm and relaxation, fitting the app's sleep/ambient sound purpose.
 * Colors are organized into:
 * - Primary: Soft blues for main branding and interactive elements
 * - Secondary: Cyan tones for secondary accents
 * - Tertiary: Soft greens for tertiary accents
 * - Neutral: Greys, whites, and background colors for light/dark modes
 * - Status: Error, success, and warning colors for user feedback
 *
 * @see androidx.compose.material3.MaterialTheme.colorScheme for accessing these colors
 */

val Primary = Color(0xFF1E88E5)
val PrimaryDark = Color(0xFF1565C0)
val PrimaryLight = Color(0xFF64B5F6)
val Secondary = Color(0xFF26C6DA)
val SecondaryDark = Color(0xFF00ACC1)
val SecondaryLight = Color(0xFF80DEEA)
val Tertiary = Color(0xFF4CAF50)
val TertiaryLight = Color(0xFF81C784)
val DarkBackground = Color(0xFF0F0F0F)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF2D2D2D)
val LightBackground = Color(0xFFFAFAFA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF5F5F5)
val DarkText = Color(0xFF1C1B1F)
val LightText = Color(0xFFFFFBFE)
val TextSecondary = Color(0xFF79747E)
val Error = Color(0xFFEF5350)
val Success = Color(0xFF66BB6A)
val Warning = Color(0xFFFFB74D)

/** Accent color used for section titles and tabs when no custom accent is chosen. */
val DefaultAccent = Color(0xFF27A157)

/**
 * User-selectable accent colors, in the spirit of Blankie (iOS/macOS) and Blanket (GNOME).
 * [Color.Unspecified] means "keep the app's original colors".
 */
data class AccentColor(
    val id: String,
    val color: Color,
    val label: String,
)

val AccentColors =
    listOf(
        AccentColor("default", Color.Unspecified, "Default"),
        AccentColor("blue", Color(0xFF3584E4), "Blue"),
        AccentColor("teal", Color(0xFF2190A4), "Teal"),
        AccentColor("green", Color(0xFF3A944A), "Green"),
        AccentColor("yellow", Color(0xFFC88800), "Yellow"),
        AccentColor("orange", Color(0xFFED5B00), "Orange"),
        AccentColor("red", Color(0xFFE62D42), "Red"),
        AccentColor("pink", Color(0xFFD56199), "Pink"),
        AccentColor("purple", Color(0xFF9141AC), "Purple"),
        AccentColor("slate", Color(0xFF6F8396), "Slate"),
    )
