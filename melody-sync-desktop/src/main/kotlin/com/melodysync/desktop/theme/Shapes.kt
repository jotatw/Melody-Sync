package com.melodysync.desktop.theme

import androidx.compose.material3.Shapes as Material3Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// ============================================================
// Hi-Fi Editorial — Shapes
// Brutalist wireframes: minimal rounding, flat sharp surfaces.
// See docs/standards/DesignSystem.md §4.1
// ============================================================

object HiFiShapes {
    val none = RoundedCornerShape(0.dp)
    val small = RoundedCornerShape(4.dp)
    val medium = RoundedCornerShape(8.dp)

    /** Material 3 Shapes instance for MaterialTheme. */
    val material: Material3Shapes = Material3Shapes(
        extraSmall = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(8.dp),
    )
}
