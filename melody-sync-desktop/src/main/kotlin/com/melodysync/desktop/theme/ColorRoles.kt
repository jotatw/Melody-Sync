package com.melodysync.desktop.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color roles — the single place that decides what color means what.
 *
 * - [primaryAction]: accent for primary actions only (Scan, Analyze, Apply…).
 * - [success]/[warning]/[info]/[danger]: application state.
 * - [surface]/[muted]: chrome and secondary text.
 *
 * Screens read these roles instead of reaching into individual palettes.
 */
data class ColorRoles(
    val primaryAction: Color,
    val success: Color,
    val warning: Color,
    val info: Color,
    val danger: Color,
    val surface: Color,
    val muted: Color,
)

@Composable
fun colorRoles(): ColorRoles {
    val scheme = MaterialTheme.colorScheme
    val dark = isDarkTheme()
    return ColorRoles(
        primaryAction = scheme.primary,
        success = if (dark) HiFiDarkColors.Success else HiFiLightColors.Success,
        warning = scheme.secondary,
        info = scheme.tertiary,
        danger = scheme.error,
        surface = scheme.surface,
        muted = scheme.onSurfaceVariant,
    )
}

@Composable
fun isDarkTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun Color.luminance(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)
