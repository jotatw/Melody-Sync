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
import com.melodysync.desktop.state.OrganizeStatus
import com.melodysync.model.OrganizationReport

@Composable
fun OrganizeSection(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Folder Organization", style = MaterialTheme.typography.titleMedium)
        Text(
            "Plan an Artist/Album folder structure. Dry-run only.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = state::planOrganization,
            enabled = state.directory.isNotBlank() && state.organizeStatus != OrganizeStatus.RUNNING,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(if (state.organizeStatus == OrganizeStatus.RUNNING) "Planning…" else "Plan Organization")
        }

        when (state.organizeStatus) {
            OrganizeStatus.RUNNING -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            OrganizeStatus.DONE -> {
                state.organizationReport?.let { report ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    OrganizeSummary(report)
                }
            }
            OrganizeStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
            OrganizeStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun OrganizeSummary(report: OrganizationReport) {
    Column {
        Text(
            "Organize: ${report.plannedMoves.size} songs · ${report.toMove} to move · ${report.alreadyOrganized} already organized",
            color = if (report.toMove > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        report.plannedMoves.filter { it.from != it.to }.take(10).forEach { move ->
            Text(
                "${move.from.fileName} → ${move.to}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            "Dry-run only — nothing moved. Use the CLI with --apply to reorganize.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
