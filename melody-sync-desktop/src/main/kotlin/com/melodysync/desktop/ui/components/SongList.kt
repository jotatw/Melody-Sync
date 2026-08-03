package com.melodysync.desktop.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.SongColumn
import com.melodysync.desktop.state.SortColumn
import com.melodysync.model.Song
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .onKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                        handleKeyEvent(event.key, state, songs, listState, coroutineScope)
                    },
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(songs, key = { it.path.toString() }) { song ->
                        SongRow(
                            song = song,
                            state = state,
                            onFocus = { focusRequester.requestFocus() },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        if (availableLetters.isNotEmpty()) {
            LetterScrubber(
                availableLetters = availableLetters,
                currentLetter = currentLetter,
                onLetterSelected = { letter ->
                    val index = songs.indexOfFirst {
                        it.title?.trim()?.firstOrNull()?.uppercaseChar() == letter
                    }
                    if (index >= 0) {
                        state.selectSong(songs[index].path.toString())
                        coroutineScope.launch { listState.scrollToItem(index) }
                    }
                },
            )
        }
    }
}

private fun handleKeyEvent(
    key: Key,
    state: AppState,
    songs: List<Song>,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
): Boolean {
    if (songs.isEmpty()) return false
    val currentIndex = songs.indexOfFirst { it.path.toString() == state.selectedSongPath }

    return when (key) {
        Key.DirectionDown -> {
            val start = if (currentIndex < 0) -1 else currentIndex
            val next = (start + 1).coerceAtMost(songs.lastIndex)
            state.selectSong(songs[next].path.toString())
            coroutineScope.launch { listState.animateScrollToItem(next) }
            true
        }
        Key.DirectionUp -> {
            val start = if (currentIndex < 0) 0 else currentIndex
            val prev = (start - 1).coerceAtLeast(0)
            state.selectSong(songs[prev].path.toString())
            coroutineScope.launch { listState.animateScrollToItem(prev) }
            true
        }
        Key.Enter -> {
            val song = songs.getOrNull(currentIndex) ?: return false
            revealInFolder(song)
            true
        }
        else -> false
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongRow(
    song: Song,
    state: AppState,
    onFocus: () -> Unit,
) {
    val selected = state.selectedSongPath == song.path.toString()

    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem(label = "Reveal in folder") { revealInFolder(song) },
                ContextMenuItem(label = "Copy path") { copyToClipboard(song.path.toString()) },
            )
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background)
                .clickable {
                    state.selectSong(song.path.toString())
                    onFocus()
                }
                .padding(vertical = 6.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (SongColumn.TITLE in state.visibleColumns) {
                Text(
                    song.title ?: song.filename,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(2f),
                )
            }
            if (SongColumn.ARTIST in state.visibleColumns) {
                Text(
                    song.artist ?: "Unknown artist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1.5f),
                )
            }
            if (SongColumn.ALBUM in state.visibleColumns) {
                Text(
                    song.album ?: "Unknown album",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1.5f),
                )
            }
            if (SongColumn.DURATION in state.visibleColumns) {
                Text(
                    "%.1f min".format(song.durationMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.width(80.dp),
                )
            }
            if (SongColumn.FORMAT in state.visibleColumns) {
                Text(
                    song.extension,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(64.dp),
                )
            }
            if (SongColumn.BITRATE in state.visibleColumns) {
                Text(
                    song.bitrate?.let { "${it / 1000} kbps" } ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(72.dp),
                )
            }
        }
    }
}

private fun revealInFolder(song: Song) {
    val dir = song.path.parent ?: return
    try {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(dir.toFile())
            return
        }
    } catch (_: Exception) {
        // fall through to xdg-open
    }
    try {
        ProcessBuilder("xdg-open", dir.toString()).start()
    } catch (_: Exception) {
        // no-op: nothing else to try
    }
}

private fun copyToClipboard(text: String) {
    try {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
    } catch (_: Exception) {
        // no-op
    }
}

@Composable
private fun SongListHeader(state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (SongColumn.TITLE in state.visibleColumns) {
            SortableHeader("Title", SortColumn.TITLE, Modifier.weight(2f), state)
        }
        if (SongColumn.ARTIST in state.visibleColumns) {
            SortableHeader("Artist", SortColumn.ARTIST, Modifier.weight(1.5f), state)
        }
        if (SongColumn.ALBUM in state.visibleColumns) {
            SortableHeader("Album", SortColumn.ALBUM, Modifier.weight(1.5f), state)
        }
        if (SongColumn.DURATION in state.visibleColumns) {
            SortableHeader("Duration", SortColumn.DURATION, Modifier.width(80.dp), state)
        }
        if (SongColumn.FORMAT in state.visibleColumns) {
            SortableHeader("Format", SortColumn.FORMAT, Modifier.width(64.dp), state)
        }
        if (SongColumn.BITRATE in state.visibleColumns) {
            SortableHeader("Bitrate", SortColumn.BITRATE, Modifier.width(72.dp), state)
        }
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
