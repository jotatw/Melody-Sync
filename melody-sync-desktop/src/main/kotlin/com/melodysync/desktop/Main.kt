package com.melodysync.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.melodysync.desktop.state.AppPreferences
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.ui.LibraryScreen

fun main() = application {
    val windowState = rememberWindowState(width = 1100.dp, height = 700.dp)

    val appState = remember { AppState() }
    val savedTheme = remember { AppPreferences.load().theme }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Melody Sync",
        state = windowState,
    ) {
        var theme by remember {
            mutableStateOf(
                when (savedTheme) {
                    "light" -> AppTheme.LIGHT
                    "dark" -> AppTheme.DARK
                    else -> AppTheme.detectSystemTheme()
                },
            )
        }

        MaterialTheme(colorScheme = theme.colorScheme) {
            Surface(modifier = Modifier.fillMaxSize()) {
                LibraryScreen(
                    state = appState,
                    theme = theme,
                    onToggleTheme = {
                        theme = if (theme == AppTheme.LIGHT) AppTheme.DARK else AppTheme.LIGHT
                        AppPreferences(
                            directory = appState.directory,
                            theme = theme.name.lowercase(),
                            section = appState.currentSection.name.lowercase(),
                            sortColumn = appState.sortColumn.name.lowercase(),
                            sortAscending = appState.sortAscending,
                            sidebarExpanded = appState.sidebarExpanded,
                        ).save()
                    },
                )
            }
        }
    }
}
