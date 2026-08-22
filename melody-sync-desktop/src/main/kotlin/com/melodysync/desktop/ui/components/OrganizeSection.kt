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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import com.melodysync.model.OrganizationReport
import com.melodysync.model.PlannedMove

@Composable
fun OrganizeSection(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Folder Organization",
            subtitle = "Plan an Artist/Album folder structure; apply explicitly.",
        )
        Button(
            onClick = state::planOrganization,
            enabled = state.directory.isNotBlank() && state.organizeStatus != TaskStatus.RUNNING,
        ) {
            Text(if (state.organizeStatus == TaskStatus.RUNNING) "Analyzing…" else "Analyze Library")
        }

        when (state.organizeStatus) {
            TaskStatus.RUNNING -> {
                ProgressCard("Planning folder structure…")
            }
            TaskStatus.DONE -> {
                state.organizationReport?.let { report ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                    OrganizeReportView(state, report)
                }
            }
            TaskStatus.ERROR -> {
                state.errorMessage?.let {
                    StatusPill(it, PillTone.DANGER, Modifier.padding(top = Spacing.sm))
                }
            }
            TaskStatus.IDLE -> {
                Text(
                    "Keep your music arranged according to your library rules. " +
                        "Analysis creates a dry-run plan — nothing is moved until you apply it explicitly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrganizeReportView(state: AppState, report: OrganizationReport) {
    val roles = colorRoles()
    val success = roles.success
    val moves = report.plannedMoves.filter { it.from != it.to }
    var showConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ResultCard(
            headline = "Plan: ${report.toMove} move(s) · ${report.alreadyOrganized} already organized",
            accent = if (report.toMove > 0) colorRoles().primaryAction else success,
        ) {
            val sizeClass = LocalWindowSizeClass.current
            if (sizeClass.isCompact) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatCard(
                        "To move",
                        report.toMove.toString(),
                        modifier = Modifier.weight(1f),
                        accent = if (report.toMove > 0) colorRoles().primaryAction else success,
                    )
                    StatCard(
                        "Already organized",
                        report.alreadyOrganized.toString(),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.sm)) {
                    StatCard("Skipped", report.skipped.toString(), modifier = Modifier.weight(1f))
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    StatCard(
                        "To move",
                        report.toMove.toString(),
                        modifier = Modifier.weight(1f),
                        accent = if (report.toMove > 0) colorRoles().primaryAction else success,
                    )
                    StatCard(
                        "Already organized",
                        report.alreadyOrganized.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    StatCard("Skipped", report.skipped.toString(), modifier = Modifier.weight(1f))
                }
            }
        }

        when {
            state.organizeApplying -> {
                ProgressCard("Applying plan…")
            }

            state.organizeApplied -> {
                state.organizeMessage?.let { message ->
                    val tone = if (message.startsWith("Applied")) PillTone.SUCCESS else PillTone.DANGER
                    StatusPill(message, tone, Modifier.padding(top = Spacing.sm))
                }
            }

            report.toMove > 0 -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Text(
                        "Review the plan below, then apply it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    Button(onClick = { showConfirm = true }) {
                        Text("Apply Plan (${report.toMove})")
                    }
                }
            }

            else -> {
                Text(
                    "Everything is already organized.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.md),
                )
            }
        }

        Text(
            "Dry-run first, explicit apply second: nothing moves until you confirm.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = Spacing.md),
        ) {
            if (moves.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        MoveHeaderRow()
                        HorizontalDivider()
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(moves, key = { it.from.toString() }) { move ->
                                MoveRow(move, applied = state.organizeApplied)
                                HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Apply plan?") },
            text = {
                Text(
                    "Move ${report.toMove} file(s) into the Artist/Album structure? " +
                        "The library is re-scanned and synchronized afterwards.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        state.applyOrganization()
                    },
                ) {
                    Text("Apply", color = colorRoles().primaryAction)
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
private fun MoveHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "File",
            style = MaterialTheme.typography.labelLarge,
            color = colorRoles().primaryAction,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Destination",
            style = MaterialTheme.typography.labelLarge,
            color = colorRoles().primaryAction,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            "Reason",
            style = MaterialTheme.typography.labelLarge,
            color = colorRoles().primaryAction,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Status",
            style = MaterialTheme.typography.labelLarge,
            color = colorRoles().primaryAction,
            modifier = Modifier.width(64.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveRow(move: PlannedMove, applied: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            move.song.filename,
            style = TechnicalStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
            tooltip = { PlainTooltip { Text(move.to.toString()) } },
            state = rememberTooltipState(),
        ) {
            Text(
                move.to.toString(),
                style = TechnicalStyleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.5f),
            )
        }
        StatusPill(
            text = move.reason,
            tone = if (move.reason.contains("already")) PillTone.SUCCESS else PillTone.WARNING,
            modifier = Modifier.weight(1f),
        )
        StatusPill(
            text = if (applied) "DONE" else "MOVE",
            tone = if (applied) PillTone.SUCCESS else PillTone.PRIMARY,
            modifier = Modifier.width(64.dp),
        )
    }
}


