package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.SongField

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryToolbar(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        // FlowRow keeps the search/filter controls usable on narrow windows:
        // fields wrap to the next line instead of overflowing.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = state::updateQuery,
                label = { Text("Search title, artist or album") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { state.updateQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(2f),
            )
            OutlinedTextField(
                value = state.artistFilter,
                onValueChange = state::updateArtistFilter,
                label = { Text("Artist") },
                singleLine = true,
                trailingIcon = {
                    if (state.artistFilter.isNotEmpty()) {
                        IconButton(onClick = { state.updateArtistFilter("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear artist filter")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.albumFilter,
                onValueChange = state::updateAlbumFilter,
                label = { Text("Album") },
                singleLine = true,
                trailingIcon = {
                    if (state.albumFilter.isNotEmpty()) {
                        IconButton(onClick = { state.updateAlbumFilter("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear album filter")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.formatFilter,
                onValueChange = state::updateFormatFilter,
                label = { Text("Format") },
                placeholder = { Text("mp3, flac…") },
                singleLine = true,
                trailingIcon = {
                    if (state.formatFilter.isNotEmpty()) {
                        IconButton(onClick = { state.updateFormatFilter("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear format filter")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )
            ColumnMenu(state)
        }

        val issue = state.issueContext
        if (issue != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Showing only the affected songs — ${issue.label} (${issue.paths.size})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = state::clearIssueContext) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear issue context")
                }
            }
        }
    }
}

@Composable
private fun ColumnMenu(state: AppState) {
    var menuOpen by remember { mutableStateOf(false) }

    Column {
        IconButton(onClick = { menuOpen = true }) {
            Icon(
                imageVector = Icons.Filled.ViewColumn,
                contentDescription = "Toggle columns",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            SongField.entries.forEach { column ->
                DropdownMenuItem(
                    text = { Text(column.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        state.toggleColumn(column)
                        menuOpen = false
                    },
                    trailingIcon = {
                        if (column in state.visibleColumns) {
                            Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                )
            }
        }
    }
}
