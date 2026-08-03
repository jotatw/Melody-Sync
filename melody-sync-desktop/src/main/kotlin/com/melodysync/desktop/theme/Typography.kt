package com.melodysync.desktop.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
// Each weight is loaded as its own family; bold/semi-bold are applied via
// FontWeight on the resolved family.

@OptIn(ExperimentalTextApi::class)
private fun fontResource(name: String): FontFamily =
    androidx.compose.ui.text.font.FontFamily("fonts/$name")

private val interRegular: FontFamily by lazy { fontResource("Inter-Regular.ttf") }
private val interMedium: FontFamily by lazy { fontResource("Inter-Medium.ttf") }
private val interSemiBold: FontFamily by lazy { fontResource("Inter-SemiBold.ttf") }
private val loraRegular: FontFamily by lazy { fontResource("Lora-Regular.ttf") }
private val loraBold: FontFamily by lazy { fontResource("Lora-Bold.ttf") }
private val jetBrainsMonoRegular: FontFamily by lazy { fontResource("JetBrainsMono-Regular.ttf") }

val HiFiTypography: Typography = Typography(
    displayLarge = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = loraBold, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = interSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = interSemiBold, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = interMedium, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = interRegular, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = interRegular, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = interRegular, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = interMedium, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = interMedium, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = interMedium, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

/** Mono style for technical data (extensions, bitrates, durations, paths). */
val TechnicalStyle = TextStyle(
    fontFamily = jetBrainsMonoRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 16.sp,
)

val TechnicalStyleSmall = TextStyle(
    fontFamily = jetBrainsMonoRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    lineHeight = 14.sp,
)
