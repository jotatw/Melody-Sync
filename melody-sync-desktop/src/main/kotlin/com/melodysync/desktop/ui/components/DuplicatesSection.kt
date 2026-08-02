package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.DuplicatesStatus

@Composable
fun DuplicatesSection(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Duplicate Detection", style = MaterialTheme.typography.titleMedium)
        Text(
            "Find songs with the same title, artist and similar duration.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = state::detectDuplicates,
            enabled = state.directory.isNotBlank() && state.duplicatesStatus != DuplicatesStatus.RUNNING,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(if (state.duplicatesStatus == DuplicatesStatus.RUNNING) "Checking…" else "Detect Duplicates")
        }

        when (state.duplicatesStatus) {
            DuplicatesStatus.RUNNING -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            DuplicatesStatus.DONE -> {
                if (state.duplicateGroups.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    DuplicatesSummary(state)
                } else {
                    Text(
                        "No duplicates found.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            DuplicatesStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            DuplicatesStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun DuplicatesSummary(state: AppState) {
    val extraFiles = state.duplicateGroups.sumOf { it.extraFiles }
    Column {
        Text(
            "Duplicates: ${state.duplicateGroups.size} groups · $extraFiles extra files",
            color = MaterialTheme.colorScheme.error,
        )
        state.duplicateGroups.forEach { group ->
            Text(
                "• ${group.artist ?: "Unknown"} — ${group.title ?: "Untitled"} (${group.songs.size} files)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            "Nothing is deleted automatically. Review and remove manually.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
