package com.melodysync.desktop.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Hi-Fi Editorial — Dark Theme ("Studio Console")
// See docs/standards/DesignSystem.md §2.1
// ============================================================

object HiFiDarkColors {
    val Background = Color(0xFF161618)
    val Surface = Color(0xFF1D1D20)
    val SurfaceVariant = Color(0xFF242428)
    val Border = Color(0xFF323236)
    val Primary = Color(0xFFFF6B00) // Neon Amber (VU meter peaks) — actions
    val Secondary = Color(0xFFFFCC00) // VU Gold (warnings, suggestions)
    val TextPrimary = Color(0xFFF3F3F3) // Paper White
    val TextSecondary = Color(0xFFA1A1AA) // Tape Silver
    val Success = Color(0xFF4ADE80)
    val Warning = Color(0xFFFFCC00) // VU Gold
    val Info = Color(0xFF3DA5FF)
    val Error = Color(0xFFF87171)
}

// ============================================================
// Hi-Fi Editorial — Light Theme ("Music Review")
// See docs/standards/DesignSystem.md §2.2
// ============================================================

object HiFiLightColors {
    val Background = Color(0xFFFAF8F5) // Linen White
    val Surface = Color(0xFFF1ECE4) // Smooth Alabaster
    val SurfaceVariant = Color(0xFFE7E0D5)
    val Border = Color(0xFF1A1A1A) // Ink Black Thin
    val Primary = Color(0xFFB22222) // Editorial Crimson — actions
    val Secondary = Color(0xFF1A4331) // Forest Dark
    val TextPrimary = Color(0xFF1A1A1A) // Deep Ink
    val TextSecondary = Color(0xFF5F5F5F) // Dust Grey
    val Success = Color(0xFF166534)
    val Warning = Color(0xFFB45309) // Amber (readable on linen)
    val Info = Color(0xFF2563EB)
    val Error = Color(0xFFB91C1C)
}

// ============================================================
// Categorical chart palette — distinguishes categories (formats,
// artists) in data visualizations. Independent of semantic status
// roles; used only where color differentiates data groups.
// ============================================================

object ChartPalette {
    val Amber = Color(0xFFFF6B00)
    val Gold = Color(0xFFFFCC00)
    val Azure = Color(0xFF3DA5FF)
    val Emerald = Color(0xFF4ADE80)
    val Magenta = Color(0xFFC514D2)
    val Coral = Color(0xFFF87171)

    val series: List<Color> = listOf(Amber, Gold, Azure, Emerald, Magenta, Coral)
}
