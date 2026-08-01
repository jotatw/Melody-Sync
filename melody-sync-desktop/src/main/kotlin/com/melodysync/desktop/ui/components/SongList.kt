package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.model.Song

@Composable
fun SongList(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
        if (state.songs.isEmpty()) {
            Text(
                "No songs loaded. Select a directory and press Scan.",
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            return
        }

        SongListHeader()
        HorizontalDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.filteredSongs, key = { it.path.toString() }) { song ->
                SongRow(song)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SongListHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Title", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(2f))
        Text("Artist", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1.5f))
        Text("Album", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1.5f))
        Text("Duration", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun SongRow(song: Song) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            song.title ?: song.filename,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.weight(2f),
        )
        Text(
            song.artist ?: "Unknown artist",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            song.album ?: "Unknown album",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            "%.1f min".format(song.durationMinutes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp),
        )
    }
}
