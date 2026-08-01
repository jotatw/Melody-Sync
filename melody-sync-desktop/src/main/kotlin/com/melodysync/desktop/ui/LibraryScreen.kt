package com.melodysync.desktop.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.desktop.ui.components.DirectoryBar
import com.melodysync.desktop.ui.components.SearchBar
import com.melodysync.desktop.ui.components.SongList
import com.melodysync.desktop.ui.components.TopBar

@Composable
fun LibraryScreen(
    state: AppState,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TopBar(state = state, theme = theme, onToggleTheme = onToggleTheme)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        DirectoryBar(state)
        SearchBar(state)
        SongList(state)
    }
}
