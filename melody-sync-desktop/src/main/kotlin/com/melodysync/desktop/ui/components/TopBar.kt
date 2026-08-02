package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.AppTheme

@Composable
fun TopBar(
    state: AppState,
    theme: AppTheme,
    onToggleTheme: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = state::toggleSidebar) {
                Icon(
                    imageVector = if (state.sidebarExpanded) Icons.Filled.MenuOpen else Icons.Filled.Menu,
                    contentDescription = if (state.sidebarExpanded) "Collapse sidebar" else "Expand sidebar",
                )
            }
            Text("🎵", style = MaterialTheme.typography.titleLarge)
            Text(
                "Melody Sync",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CompactStats(state)
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (theme == AppTheme.DARK) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = if (theme == AppTheme.DARK) "Switch to light theme" else "Switch to dark theme",
                )
            }
        }
    }
}

@Composable
private fun CompactStats(state: AppState) {
    val stats = state.statistics ?: return
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Stat("Songs", stats.totalSongs.toString())
        Stat("Artists", stats.uniqueArtists.toString())
        Stat("Albums", stats.uniqueAlbums.toString())
        Stat("Duration", "${"%.1f".format(stats.totalDurationHours)}h")
        Stat("Size", "${"%.2f".format(stats.totalSizeGb)}GB")
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row {
        Text(
            "$value $label",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
