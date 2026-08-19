package com.melodysync.desktop.ui.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    WindowSizeClass(0.dp)
}

/**
 * Remembers the current window size class based on window metrics.
 * Uses a simple state-based approach that can be updated externally.
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val widthState = remember { mutableStateOf(0.dp) }
    
    return derivedStateOf {
        if (widthState.value == 0.dp) {
            WindowSizeClass(1100.dp)
        } else {
            widthState.value
        }
    }.value as WindowSizeClass
}

/**
 * Provides the current window size class to the composition tree.
 * Should be called at the root of the composition tree.
 */
@Composable
fun ProvideWindowSizeClass(content: @Composable () -> Unit) {
    val sizeClass = rememberWindowSizeClass()
    CompositionLocalProvider(LocalWindowSizeClass provides sizeClass) {
        content()
    }
}

/**
 * Convenience composable to access the current window size class.
 */
@Composable
fun currentWindowSizeClass(): WindowSizeClass = LocalWindowSizeClass.current