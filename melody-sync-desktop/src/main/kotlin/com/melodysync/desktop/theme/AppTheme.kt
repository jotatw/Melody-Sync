package com.melodysync.desktop.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import java.nio.file.Files
import java.nio.file.Path

enum class AppTheme {
    LIGHT,
    DARK;

    val colorScheme: ColorScheme
        get() = when (this) {
            LIGHT -> lightColorScheme()
            DARK -> darkColorScheme()
        }

    companion object {
        fun detectSystemTheme(): AppTheme {
            val os = System.getProperty("os.name")?.lowercase() ?: ""
            if (!os.contains("linux")) return LIGHT

            detectKdeTheme()?.let { return it }
            detectGnomeTheme()?.let { return it }

            return LIGHT
        }

        internal fun luminanceOf(rgb: List<Int>): Double {
            if (rgb.size < 3) return 255.0
            return 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2]
        }

        internal fun isDarkLuminance(luminance: Double): Boolean =
            luminance < 128.0

        private fun detectKdeTheme(): AppTheme? {
            val kdeGlobals = Path.of(System.getProperty("user.home"), ".config", "kdeglobals")
            if (!Files.exists(kdeGlobals)) return null

            return try {
                val content = Files.readString(kdeGlobals)
                val windowSection = content
                    .substringAfter("[Colors:Window]")
                    .substringBefore("\n[")
                val bgLine = windowSection.lineSequence()
                    .firstOrNull { it.trim().startsWith("BackgroundNormal=") }
                    ?.substringAfter("=")
                    ?: return null

                val rgb = bgLine.trim().split(",").mapNotNull { it.trim().toIntOrNull() }
                if (rgb.size < 3) return null

                if (isDarkLuminance(luminanceOf(rgb))) DARK else LIGHT
            } catch (_: Exception) {
                null
            }
        }

        private fun detectGnomeTheme(): AppTheme? {
            return try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("gsettings", "get", "org.gnome.desktop.interface", "color-scheme"),
                )
                val output = process.inputStream.bufferedReader().readText().trim()
                when {
                    output.contains("dark", ignoreCase = true) -> DARK
                    output.contains("light", ignoreCase = true) -> LIGHT
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
