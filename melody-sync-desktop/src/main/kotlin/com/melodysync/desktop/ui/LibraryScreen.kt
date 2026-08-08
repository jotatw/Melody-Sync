package com.melodysync.desktop.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.Section
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.ui.components.AboutSection
import com.melodysync.desktop.ui.components.DirectoryBar
import com.melodysync.desktop.ui.components.DuplicatesSection
import com.melodysync.desktop.ui.components.HealthSection
import com.melodysync.desktop.ui.components.LibraryHeader
import com.melodysync.desktop.ui.components.LibraryToolbar
import com.melodysync.desktop.ui.components.OrganizeSection
import com.melodysync.desktop.ui.components.QuickFixPanel
import com.melodysync.desktop.ui.components.SettingsSection
import com.melodysync.desktop.ui.components.Sidebar
import com.melodysync.desktop.ui.components.SongList
import com.melodysync.desktop.ui.components.StatisticsSection
import com.melodysync.desktop.ui.components.TopBar
import kotlinx.coroutines.delay

@Composable
fun LibraryScreen(
    state: AppState,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val message = state.transientMessage
    if (message != null) {
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message, duration = androidx.compose.material3.SnackbarDuration.Short)
            state.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                state = state,
                theme = theme,
                onToggleTheme = onToggleTheme,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Sidebar(state)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                when (state.currentSection) {
                    Section.LIBRARY -> {
                        LibraryHeader(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DirectoryBar(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        LibraryToolbar(state)
                        val selectedSong = state.songs.firstOrNull {
                            it.path.toString() == state.selectedSongPath
                        }
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                                SongList(state)
                            }
                            if (selectedSong != null) {
                                VerticalDivider()
                                QuickFixPanel(state, selectedSong)
                            }
                        }
                    }
                    Section.STATISTICS -> {
                        DirectoryBar(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        StatisticsSection(state)
                    }
                    Section.HEALTH -> {
                        DirectoryBar(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        HealthSection(state)
                    }
                    Section.DUPLICATES -> {
                        DirectoryBar(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        DuplicatesSection(state)
                    }
                    Section.ORGANIZE -> {
                        DirectoryBar(state)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        OrganizeSection(state)
                    }
                    Section.SETTINGS -> {
                        SettingsSection(state)
                    }
                    Section.ABOUT -> {
                        AboutSection()
                    }
                }
            }
        }
    }
}