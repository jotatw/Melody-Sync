package com.melodysync.desktop.theme

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppThemeTest {

    @Test
    fun `dark background produces dark theme`() {
        val luminance = AppTheme.luminanceOf(listOf(24, 27, 40))
        assertTrue(AppTheme.isDarkLuminance(luminance))
    }

    @Test
    fun `light background produces light theme`() {
        val luminance = AppTheme.luminanceOf(listOf(240, 240, 240))
        assertFalse(AppTheme.isDarkLuminance(luminance))
    }

    @Test
    fun `dark and light themes use different color schemes`() {
        assertNotEquals(AppTheme.DARK.colorScheme.background, AppTheme.LIGHT.colorScheme.background)
    }

    @Test
    fun `dark theme background is a dark color`() {
        val background = AppTheme.DARK.colorScheme.background
        val luminance = 0.2126 * background.red * 255 + 0.7152 * background.green * 255 + 0.0722 * background.blue * 255
        assertTrue(luminance < 128.0)
    }

    @Test
    fun `light theme background is a light color`() {
        val background = AppTheme.LIGHT.colorScheme.background
        val luminance = 0.2126 * background.red * 255 + 0.7152 * background.green * 255 + 0.0722 * background.blue * 255
        assertTrue(luminance >= 128.0)
    }

    @Test
    fun `detect system theme returns light or dark`() {
        val theme = AppTheme.detectSystemTheme()
        assertTrue(theme == AppTheme.LIGHT || theme == AppTheme.DARK)
        assertEquals(theme, if (theme == AppTheme.DARK) AppTheme.DARK else AppTheme.LIGHT)
    }
}
