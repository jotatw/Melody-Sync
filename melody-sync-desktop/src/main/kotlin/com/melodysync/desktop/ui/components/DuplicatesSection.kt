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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.TaskStatus
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyle
import com.melodysync.desktop.theme.TechnicalStyleSmall
import com.melodysync.desktop.theme.colorRoles
import com.melodysync.desktop.ui.window.LocalWindowSizeClass
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
            enabled = state.directory.isNotBlank() && state.duplicatesStatus != TaskStatus.RUNNING,
        ) {
            Text(if (state.duplicatesStatus == TaskStatus.RUNNING) "Checking…" else "Detect Duplicates")
        }

        when (state.duplicatesStatus) {
            TaskStatus.RUNNING -> {
                ProgressCard("Checking for duplicates…")
            }
            TaskStatus.DONE -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                if (state.duplicateGroups.isEmpty()) {
                    EmptyState(
                        icon = Icons.Filled.CheckCircle,
                        title = "No duplicates found",
                        message = "Your library has no matching duplicate groups.",
                        success = true,
                    )
                } else {
                    DuplicatesView(state)
                }
            }
            TaskStatus.ERROR -> {
                state.errorMessage?.let {
                    StatusPill(it, PillTone.DANGER, Modifier.padding(top = Spacing.sm))
                }
            }
            TaskStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun DuplicatesView(state: AppState) {
    var showConfirm by remember { mutableStateOf(false) }
    val groups = state.duplicateGroups
    val extraFiles = groups.sumOf { it.extraFiles }
    val recoverable = groups.sumOf { group ->
        val primary = group.primary()
        group.songs.filter { it != primary }.sumOf { it.size }
    }
    val selected = state.duplicateTrashSelection

    Column(modifier = Modifier.fillMaxSize()) {
        ResultCard(
            headline = "${groups.size} duplicate group(s) · $extraFiles extra file(s)",
            accent = colorRoles().danger,
        ) {
            val sizeClass = LocalWindowSizeClass.current
            if (sizeClass.isCompact) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatCard("Groups", groups.size.toString(), modifier = Modifier.weight(1f))
                    StatCard(
                        "Extra files",
                        extraFiles.toString(),
                        modifier = Modifier.weight(1f),
                        accent = colorRoles().danger,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.sm)) {
                    StatCard(
                        "Recoverable",
                        formatSize(recoverable),
                        modifier = Modifier.weight(1f),
                        accent = colorRoles().primaryAction,
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatCard("Groups", groups.size.toString(), modifier = Modifier.weight(1f))
                    StatCard(
                        "Extra files",
                        extraFiles.toString(),
                        modifier = Modifier.weight(1f),
                        accent = colorRoles().danger,
                    )
                    StatCard(
                        "Recoverable",
                        formatSize(recoverable),
                        modifier = Modifier.weight(1f),
                        accent = colorRoles().primaryAction,
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                "Nothing is deleted automatically — review, select and move to trash.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = { showConfirm = true },
                enabled = selected.isNotEmpty() && !state.duplicateTrashing,
            ) {
                Text(if (state.duplicateTrashing) "Moving…" else "Move to Trash (${selected.size})")
            }
        }

        if (state.duplicateTrashing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm))
        }

        state.duplicateTrashMessage?.let { message ->
            val tone = if (message.startsWith("Moved ")) PillTone.SUCCESS else PillTone.DANGER
            StatusPill(message, tone, Modifier.padding(top = Spacing.sm))
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(top = Spacing.md)) {
            items(groups, key = { it.key }) { group ->
                DuplicateGroupCard(group, state)
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Move to trash?") },
            text = {
                Text(
                    "Move ${selected.size} duplicate file(s) to the system trash? " +
                        "They stay recoverable until you empty the trash.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        state.trashSelectedDuplicates()
                    },
                ) {
                    Text("Move to Trash", color = colorRoles().danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun DuplicateGroupCard(group: DuplicateGroup, state: AppState) {
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
                    color = colorRoles().danger,
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
                val isPrimary = song == primary
                CandidateRow(
                    song = song,
                    isPrimary = isPrimary,
                    selected = song.path.toString() in state.duplicateTrashSelection,
                    onToggle = if (isPrimary) {
                        null
                    } else {
                        { state.toggleDuplicateSelection(song.path.toString()) }
                    },
                )
            }
        }
    }
}

@Composable
private fun CandidateRow(
    song: Song,
    isPrimary: Boolean,
    selected: Boolean,
    onToggle: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (onToggle != null) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
            )
        } else {
            Checkbox(
                checked = true,
                onCheckedChange = null,
            )
        }
        StatusPill(
            text = if (isPrimary) "KEEP" else "DUP",
            tone = if (isPrimary) PillTone.SUCCESS else PillTone.DANGER,
            modifier = Modifier.width(72.dp),
        )
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
            color = colorRoles().primaryAction,
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
