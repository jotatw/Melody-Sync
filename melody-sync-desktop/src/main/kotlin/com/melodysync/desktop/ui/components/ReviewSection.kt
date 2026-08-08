package com.melodysync.desktop.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.Spacing
import com.melodysync.model.MissingField
import com.melodysync.model.QualityFlag
import com.melodysync.model.SongDiagnostics

private enum class ReviewFilter(val label: String) {
    ALL("All"),
    WITHOUT_METADATA("No metadata"),
    ZERO_DURATION("Zero duration"),
    LOW_BITRATE("Low bitrate"),
}

/**
 * Review screen: every song with a problem across the library, filterable by
 * issue type. Selecting a song opens the Quick-Fix panel on the right — the
 * Health → song → fix flow.
 */
@Composable
fun ReviewSection(state: AppState) {
    LaunchedEffect(Unit) { state.refreshReview() }

    var filter by remember { mutableStateOf(ReviewFilter.ALL) }
    val items = remember(state.reviewItems, filter) {
        when (filter) {
            ReviewFilter.ALL -> state.reviewItems
            ReviewFilter.WITHOUT_METADATA -> state.reviewItems.filter { it.missing.isNotEmpty() }
            ReviewFilter.ZERO_DURATION -> state.reviewItems.filter { QualityFlag.ZERO_DURATION in it.flags }
            ReviewFilter.LOW_BITRATE -> state.reviewItems.filter { QualityFlag.LOW_BITRATE in it.flags }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(top = Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Songs to review", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.reviewItems.size} song(s) with issues",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }

        Row(
            modifier = Modifier.padding(top = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            ReviewFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option.label) },
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))

        if (items.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.CheckCircle,
                title = "Nothing to review",
                message = if (state.reviewItems.isEmpty()) {
                    "Your library is in good shape."
                } else {
                    "No songs match this filter."
                },
                success = state.reviewItems.isEmpty(),
            )
            return@Column
        }

        val selectedSong = state.songs.firstOrNull {
            it.path.toString() == state.selectedSongPath
        }

        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.weight(1f).fillMaxSize()) {
                items(items, key = { it.song.path.toString() }) { diagnostics ->
                    ReviewRow(diagnostics, state)
                    HorizontalDivider()
                }
            }
            if (selectedSong != null) {
                VerticalDivider()
                QuickFixPanel(state, selectedSong)
            }
        }
    }
}

@Composable
private fun ReviewRow(diagnostics: SongDiagnostics, state: AppState) {
    val song = diagnostics.song
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { state.selectSong(song.path.toString()) }
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                song.title ?: song.filename,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.artist ?: "Unknown artist",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            diagnostics.missing.forEach { field ->
                StatusPill(
                    text = fieldLabel(field),
                    tone = PillTone.DANGER,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
            diagnostics.flags.forEach { flag ->
                StatusPill(
                    text = flagLabel(flag),
                    tone = PillTone.WARNING,
                    modifier = Modifier.padding(start = Spacing.xs),
                )
            }
        }
    }
}

private fun fieldLabel(field: MissingField): String = when (field) {
    MissingField.TITLE -> "Title"
    MissingField.ARTIST -> "Artist"
    MissingField.ALBUM -> "Album"
}

private fun flagLabel(flag: QualityFlag): String = when (flag) {
    QualityFlag.LOW_BITRATE -> "Low bitrate"
    QualityFlag.ZERO_DURATION -> "Zero duration"
}
