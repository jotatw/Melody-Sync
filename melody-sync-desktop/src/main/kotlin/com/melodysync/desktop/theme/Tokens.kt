package com.melodysync.desktop.theme

/**
 * Hi-Fi Editorial — Design Tokens
 *
 * Aggregates the visual identity defined in docs/standards/DesignSystem.md:
 * colors, typography, shapes and dimensions.
 */
object DesignTokens {
    val colors = object {
        val dark = HiFiDarkColors
        val light = HiFiLightColors
    }

    val spacing = Spacing
    val heights = Heights
    val widths = Widths
    val shapes = HiFiShapes

    val technicalStyle = TechnicalStyle
    val technicalStyleSmall = TechnicalStyleSmall
}
