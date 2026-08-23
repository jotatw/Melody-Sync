package com.melodysync.desktop.ui.window

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents the current window size class based on width breakpoints.
 * Used to adapt layout for different window sizes.
 */
data class WindowSizeClass(val width: Dp) {
    val isCompact: Boolean get() = width < 900.dp
    val isMedium: Boolean get() = width in 900.dp..1299.dp
    val isExpanded: Boolean get() = width >= 1300.dp

    override fun toString(): String = when {
        isCompact -> "Compact (< 900dp)"
        isMedium -> "Medium (900-1299dp)"
        else -> "Expanded (≥ 1300dp)"
    }
}

/**
 * CompositionLocal for the current window size class.
 */
val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass(1100.dp)
}

/**
 * Measures the available width and provides the resulting [WindowSizeClass]
 * to the composition tree via [LocalWindowSizeClass]. Must wrap the root
 * composable so every screen reads the real window width.
 */
@Composable
fun ProvideWindowSizeClass(content: @Composable () -> Unit) {
    BoxWithConstraints {
        val sizeClass = WindowSizeClass(maxWidth)
        CompositionLocalProvider(LocalWindowSizeClass provides sizeClass) {
            content()
        }
    }
}

/**
 * Convenience composable to access the current window size class.
 */
@Composable
fun currentWindowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current