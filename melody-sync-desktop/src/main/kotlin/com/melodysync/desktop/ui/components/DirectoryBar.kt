package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.HealthStatus
import com.melodysync.desktop.state.ScanStatus
import java.io.File
import javax.swing.JFileChooser

@Composable
fun DirectoryBar(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = state.directory,
                onValueChange = state::updateDirectory,
                label = { Text("Music directory") },
                placeholder = { Text("/home/you/Music") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = { chooseDirectory()?.let(state::updateDirectory) }) {
                Text("Choose…")
            }
            Button(
                onClick = state::scan,
                enabled = state.directory.isNotBlank() && state.status != ScanStatus.SCANNING,
            ) {
                Text(if (state.status == ScanStatus.SCANNING) "Scanning…" else "Scan")
            }
            OutlinedButton(
                onClick = state::analyzeHealth,
                enabled = state.directory.isNotBlank() && state.healthStatus != HealthStatus.RUNNING,
            ) {
                Text(if (state.healthStatus == HealthStatus.RUNNING) "Checking…" else "Health")
            }
        }

        when (state.status) {
            ScanStatus.SCANNING -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Text(
                    state.progressText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            ScanStatus.DONE -> {
                if (state.progressText.isNotBlank()) {
                    Text(
                        state.progressText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            ScanStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            ScanStatus.IDLE -> Unit
        }

        when (state.healthStatus) {
            HealthStatus.RUNNING -> {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Text(
                    "Checking library health…",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            HealthStatus.DONE -> {
                state.healthReport?.let { report ->
                    HealthSummary(report)
                }
            }
            HealthStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            HealthStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun HealthSummary(report: com.melodysync.model.HealthReport) {
    androidx.compose.foundation.layout.Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Text(
            "Health: ${report.totalFiles} files | ${report.audioFiles} audio | ${report.totalNonAudio} non-audio",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "Issues: ${report.songsWithoutMetadata.size} without metadata · " +
                "${report.songsWithZeroDuration.size} zero duration · " +
                "${report.orphanedEntries.size} orphaned",
            style = MaterialTheme.typography.bodySmall,
            color = if (report.songsWithMetadataIssues > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = androidx.compose.ui.Modifier.padding(top = 2.dp),
        )
    }
}

private fun chooseDirectory(): String? {
    val chooser = JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    chooser.isAcceptAllFileFilterUsed = false
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile.absolutePath
    } else {
        null
    }
}
