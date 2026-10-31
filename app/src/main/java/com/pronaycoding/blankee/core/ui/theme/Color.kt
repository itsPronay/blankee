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
 * User-selectable accent colors, matching Blankie (iOS/macOS) in name and order.
 * The values are Apple's system colors (as used by SwiftUI's `.red`, `.blue`, ...),
 * with the light and dark appearance variants. "Default" keeps the app's own accent.
 */
data class AccentColor(
    val id: String,
    val light: Color,
    val dark: Color,
    val label: String,
) {
    fun color(darkTheme: Boolean): Color = if (darkTheme) dark else light
}

val AccentColors =
    listOf(
        AccentColor("default", DefaultAccent, DefaultAccent, "Default"),
        AccentColor("red", Color(0xFFFF3B30), Color(0xFFFF453A), "Red"),
        AccentColor("pink", Color(0xFFFF2D55), Color(0xFFFF375F), "Pink"),
        AccentColor("orange", Color(0xFFFF9500), Color(0xFFFF9F0A), "Orange"),
        AccentColor("brown", Color(0xFFA2845E), Color(0xFFAC8E68), "Brown"),
        AccentColor("yellow", Color(0xFFFFCC00), Color(0xFFFFD60A), "Yellow"),
        AccentColor("green", Color(0xFF34C759), Color(0xFF30D158), "Green"),
        AccentColor("mint", Color(0xFF00C7BE), Color(0xFF63E6E2), "Mint"),
        AccentColor("teal", Color(0xFF30B0C7), Color(0xFF40C8E0), "Teal"),
        AccentColor("cyan", Color(0xFF32ADE6), Color(0xFF64D2FF), "Cyan"),
        AccentColor("blue", Color(0xFF007AFF), Color(0xFF0A84FF), "Blue"),
        AccentColor("indigo", Color(0xFF5856D6), Color(0xFF5E5CE6), "Indigo"),
        AccentColor("purple", Color(0xFFAF52DE), Color(0xFFBF5AF2), "Purple"),
    )
