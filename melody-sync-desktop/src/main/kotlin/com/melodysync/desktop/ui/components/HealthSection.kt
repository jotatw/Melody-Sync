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
import com.melodysync.desktop.state.HealthStatus
import com.melodysync.model.HealthReport

@Composable
fun HealthSection(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Library Health", style = MaterialTheme.typography.titleMedium)
        Text(
            "Check for missing metadata, zero duration and orphaned entries.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = state::analyzeHealth,
            enabled = state.directory.isNotBlank() && state.healthStatus != HealthStatus.RUNNING,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(if (state.healthStatus == HealthStatus.RUNNING) "Checking…" else "Analyze Health")
        }

        when (state.healthStatus) {
            HealthStatus.RUNNING -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            HealthStatus.DONE -> {
                state.healthReport?.let { report ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    HealthSummary(report)
                }
            }
            HealthStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            HealthStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun HealthSummary(report: HealthReport) {
    Column {
        Text("Health: ${report.totalFiles} files | ${report.audioFiles} audio | ${report.totalNonAudio} non-audio")
        Text(
            "Issues: ${report.songsWithoutMetadata.size} without metadata · " +
                "${report.songsWithZeroDuration.size} zero duration · " +
                "${report.orphanedEntries.size} orphaned",
            color = if (report.songsWithMetadataIssues > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        if (report.nonAudio.isNotEmpty()) {
            Text(
                "Non-audio: " + report.nonAudio.joinToString(" · ") { "${it.category} (${it.count})" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
