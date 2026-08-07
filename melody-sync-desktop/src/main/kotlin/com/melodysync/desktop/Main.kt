package com.melodysync.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.melodysync.desktop.state.AppPreferences
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.theme.HiFiShapes
import com.melodysync.desktop.theme.HiFiTypography
import com.melodysync.desktop.ui.LibraryScreen

fun main() = application {
    val prefs = remember { AppPreferences.load() }

    val windowState = rememberWindowState(
        position = if (prefs.windowPositionX != null && prefs.windowPositionY != null) {
            WindowPosition.Absolute(prefs.windowPositionX!!.dp, prefs.windowPositionY!!.dp)
        } else {
            WindowPosition.PlatformDefault
        },
        size = if (prefs.windowWidth != null && prefs.windowHeight != null) {
            DpSize(prefs.windowWidth!!.dp, prefs.windowHeight!!.dp)
        } else {
            DpSize(1100.dp, 700.dp)
        },
    )

    val appState = remember { AppState() }
    var theme by remember {
        mutableStateOf(
            when (prefs.theme) {
                "light" -> AppTheme.LIGHT
                "dark" -> AppTheme.DARK
                else -> AppTheme.detectSystemTheme()
            },
        )
    }

    LaunchedEffect(Unit) { appState.loadLibraryFromDatabase() }

    fun savePrefs() {
        val position = windowState.position as? WindowPosition.Absolute
        AppPreferences(
            directory = appState.directory,
            theme = theme.name.lowercase(),
            section = appState.currentSection.name.lowercase(),
            sortColumn = appState.sortColumn.name.lowercase(),
            sortAscending = appState.sortAscending,
            sidebarExpanded = appState.sidebarExpanded,
            visibleColumns = appState.visibleColumns.joinToString(",") { it.name.lowercase() },
            groupByLetter = appState.groupByLetter,
            windowWidth = windowState.size.width.value.toDouble(),
            windowHeight = windowState.size.height.value.toDouble(),
            windowPositionX = position?.x?.value?.toDouble(),
            windowPositionY = position?.y?.value?.toDouble(),
        ).save()
    }

    Window(
        onCloseRequest = {
            savePrefs()
            exitApplication()
        },
        title = "Melody Sync",
        state = windowState,
    ) {
        MaterialTheme(
            colorScheme = theme.colorScheme,
            typography = HiFiTypography,
            shapes = HiFiShapes.material,
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                LibraryScreen(
                    state = appState,
                    theme = theme,
                    onToggleTheme = {
                        theme = if (theme == AppTheme.LIGHT) AppTheme.DARK else AppTheme.LIGHT
                        savePrefs()
                    },
                )
            }
        }
    }
}
