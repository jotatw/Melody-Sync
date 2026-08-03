package com.melodysync.desktop.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.sp

// ============================================================
// Hi-Fi Editorial — Typography
// Hybrid font system:
//   Serif  (Lora)          → editorial headers, prominent metrics
//   Sans   (Inter)         → navigation, song titles, buttons
//   Mono   (JetBrains Mono)→ file extensions, bitrates, paths, durations
// See docs/standards/DesignSystem.md §3
// ============================================================

// Fonts are loaded from classpath resources (src/main/resources/fonts).
// androidx.compose.ui.text.platform.Font(resource, weight) reads the TTF
// from the classpath via ClassLoader.getResourceAsStream.

private val interFamily: FontFamily = FontFamily(
    Font(resource = "fonts/Inter-Regular.ttf", weight = FontWeight.Normal),
    Font(resource = "fonts/Inter-Medium.ttf", weight = FontWeight.Medium),
    Font(resource = "fonts/Inter-SemiBold.ttf", weight = FontWeight.SemiBold),
)

private val serifFamily: FontFamily = FontFamily(
    Font(resource = "fonts/Lora-Regular.ttf", weight = FontWeight.Normal),
    Font(resource = "fonts/Lora-Bold.ttf", weight = FontWeight.Bold),
)

private val monoFamily: FontFamily = FontFamily(
    Font(resource = "fonts/JetBrainsMono-Regular.ttf", weight = FontWeight.Normal),
)

val HiFiTypography: Typography = Typography(
    displayLarge = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = serifFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = interFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

/** Mono style for technical data (extensions, bitrates, durations, paths). */
val TechnicalStyle = TextStyle(
    fontFamily = monoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 16.sp,
)

val TechnicalStyleSmall = TextStyle(
    fontFamily = monoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
)
