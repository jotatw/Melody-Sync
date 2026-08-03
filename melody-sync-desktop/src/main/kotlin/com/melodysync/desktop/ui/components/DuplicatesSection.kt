package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.DuplicatesStatus
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyle
import com.melodysync.desktop.theme.TechnicalStyleSmall
import com.melodysync.model.DuplicateGroup
import com.melodysync.model.Song

@Composable
fun DuplicatesSection(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Duplicate Detection",
            subtitle = "Songs with the same title, artist and similar duration.",
        )
        Button(
            onClick = state::detectDuplicates,
            enabled = state.directory.isNotBlank() && state.duplicatesStatus != DuplicatesStatus.RUNNING,
        ) {
            Text(if (state.duplicatesStatus == DuplicatesStatus.RUNNING) "Checking…" else "Detect Duplicates")
        }

        when (state.duplicatesStatus) {
            DuplicatesStatus.RUNNING -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md))
            }
            DuplicatesStatus.DONE -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                if (state.duplicateGroups.isEmpty()) {
                    Text(
                        "No duplicates found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    DuplicatesView(state)
                }
            }
            DuplicatesStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }
            DuplicatesStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun DuplicatesView(state: AppState) {
    val groups = state.duplicateGroups
    val extraFiles = groups.sumOf { it.extraFiles }
    val recoverable = groups.sumOf { group ->
        val primary = group.primary()
        group.songs.filter { it != primary }.sumOf { it.size }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            StatCard("Groups", groups.size.toString(), modifier = Modifier.weight(1f))
            StatCard(
                "Extra files",
                extraFiles.toString(),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.error,
            )
            StatCard(
                "Recoverable",
                formatSize(recoverable),
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.primary,
            )
        }

        Text(
            "Nothing is deleted automatically — review and remove manually.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.md),
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(top = Spacing.md)) {
            items(groups, key = { it.key }) { group ->
                DuplicateGroupCard(group)
            }
        }
    }
}

@Composable
private fun DuplicateGroupCard(group: DuplicateGroup) {
    val primary = group.primary()

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.md)) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    "${group.artist ?: "Unknown"} — ${group.title ?: "Untitled"}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "same title & artist",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                "${group.size} files · ${group.extraFiles} duplicate(s)",
                style = TechnicalStyleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))

            group.songs.forEach { song ->
                CandidateRow(song, isPrimary = song == primary)
            }
        }
    }
}

@Composable
private fun CandidateRow(song: Song, isPrimary: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Surface(
            color = if (isPrimary) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.width(72.dp),
        ) {
            Text(
                if (isPrimary) "KEEP" else "DUP",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = Spacing.xs),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.filename,
                style = TechnicalStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                song.directory?.toString() ?: "",
                style = TechnicalStyleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            song.extension,
            style = TechnicalStyleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(44.dp),
        )
        Text(
            formatSize(song.size),
            style = TechnicalStyleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(76.dp),
        )
        Text(
            formatDuration(song.duration),
            style = TechnicalStyleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(76.dp),
        )
    }
}

private fun DuplicateGroup.primary(): Song =
    songs.maxWithOrNull(
        compareBy<Song> { if (it.hasMetadata) 0 else 1 }
            .thenBy { -it.size },
    ) ?: songs.first()

private fun formatSize(bytes: Long): String = when {
    bytes >= 1024L * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
    bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
    else -> "%.0f KB".format(bytes / 1024.0)
}

private fun formatDuration(seconds: Double?): String =
    if (seconds != null && seconds > 0) {
        "%.1f min".format(seconds / 60.0)
    } else {
        "—"
    }
