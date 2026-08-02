package com.melodysync.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.SortColumn
import com.melodysync.model.Song
import kotlinx.coroutines.launch

@Composable
fun SongList(state: AppState) {
    if (state.songs.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.FolderOpen,
            title = "No songs loaded",
            message = "Choose a music directory above and press Scan to load your library.",
        )
        return
    }

    val songs = state.filteredSongs

    if (songs.isEmpty()) {
        EmptyState(
            icon = Icons.Filled.SearchOff,
            title = "No results",
            message = "No songs match \"${state.query}\". Try a different search.",
        )
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val availableLetters = remember(songs) {
        songs.mapNotNull { it.title?.trim()?.firstOrNull()?.uppercaseChar() }.toSet()
    }

    val currentLetter by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            if (index in songs.indices) {
                songs[index].title?.trim()?.firstOrNull()?.uppercaseChar()
            } else {
                null
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f).fillMaxSize().padding(top = 8.dp)) {
            SongListHeader(state)
            HorizontalDivider()

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(songs, key = { it.path.toString() }) { song ->
                    SongRow(song)
                    HorizontalDivider()
                }
            }
        }

        if (availableLetters.isNotEmpty()) {
            LetterIndex(
                availableLetters = availableLetters,
                currentLetter = currentLetter,
                onLetterSelected = { letter ->
                    val index = songs.indexOfFirst {
                        it.title?.trim()?.firstOrNull()?.uppercaseChar() == letter
                    }
                    if (index >= 0) {
                        coroutineScope.launch { listState.scrollToItem(index) }
                    }
                },
            )
        }
    }
}

@Composable
private fun SongListHeader(state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SortableHeader("Title", SortColumn.TITLE, Modifier.weight(2f), state)
        SortableHeader("Artist", SortColumn.ARTIST, Modifier.weight(1.5f), state)
        SortableHeader("Album", SortColumn.ALBUM, Modifier.weight(1.5f), state)
        SortableHeader("Duration", SortColumn.DURATION, Modifier.width(80.dp), state)
    }
}

@Composable
private fun SortableHeader(
    label: String,
    column: SortColumn,
    modifier: Modifier,
    state: AppState,
) {
    val arrow = when {
        state.sortColumn != column -> ""
        state.sortAscending -> " ▲"
        else -> " ▼"
    }
    Text(
        "$label$arrow",
        style = MaterialTheme.typography.labelLarge,
        color = if (state.sortColumn == column) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.clickable { state.toggleSort(column) },
    )
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
