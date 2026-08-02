package com.melodysync.desktop.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.Section
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.ui.components.DirectoryBar
import com.melodysync.desktop.ui.components.DuplicatesSection
import com.melodysync.desktop.ui.components.HealthSection
import com.melodysync.desktop.ui.components.OrganizeSection
import com.melodysync.desktop.ui.components.SearchBar
import com.melodysync.desktop.ui.components.Sidebar
import com.melodysync.desktop.ui.components.SongList
import com.melodysync.desktop.ui.components.StatisticsSection
import com.melodysync.desktop.ui.components.TopBar

@Composable
fun LibraryScreen(
    state: AppState,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Sidebar(state)

        Column(modifier = Modifier.weight(1f).fillMaxSize()) {
            TopBar(state = state, theme = theme, onToggleTheme = onToggleTheme)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            DirectoryBar(state)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            when (state.currentSection) {
                Section.LIBRARY -> {
                    SearchBar(state)
                    SongList(state)
                }
                Section.STATISTICS -> StatisticsSection(state)
                Section.HEALTH -> HealthSection(state)
                Section.DUPLICATES -> DuplicatesSection(state)
                Section.ORGANIZE -> OrganizeSection(state)
            }
        }
    }
}
