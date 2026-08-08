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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.OrganizeStatus
import com.melodysync.desktop.theme.HiFiDarkColors
import com.melodysync.desktop.theme.HiFiLightColors
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyle
import com.melodysync.desktop.theme.TechnicalStyleSmall
import com.melodysync.model.OrganizationReport
import com.melodysync.model.PlannedMove

@Composable
fun OrganizeSection(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Folder Organization",
            subtitle = "Plan an Artist/Album folder structure. Dry-run only.",
        )
        Button(
            onClick = state::planOrganization,
            enabled = state.directory.isNotBlank() && state.organizeStatus != OrganizeStatus.RUNNING,
        ) {
            Text(if (state.organizeStatus == OrganizeStatus.RUNNING) "Planning…" else "Plan Organization")
        }

        when (state.organizeStatus) {
            OrganizeStatus.RUNNING -> {
                ProgressCard("Planning folder structure…")
            }
            OrganizeStatus.DONE -> {
                state.organizationReport?.let { report ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                    OrganizeReportView(report)
                }
            }
            OrganizeStatus.ERROR -> {
                state.errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = Spacing.sm),
                    )
                }
            }
            OrganizeStatus.IDLE -> Unit
        }
    }
}

@Composable
private fun OrganizeReportView(report: OrganizationReport) {
    val success = if (isDark()) HiFiDarkColors.Success else HiFiLightColors.Success
    val moves = report.plannedMoves.filter { it.from != it.to }

    Column(modifier = Modifier.fillMaxSize()) {
        ResultCard(
            headline = "Plan: ${report.toMove} move(s) · ${report.alreadyOrganized} already organized",
            accent = if (report.toMove > 0) MaterialTheme.colorScheme.primary else success,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                StatCard(
                    "To move",
                    report.toMove.toString(),
                    modifier = Modifier.weight(1f),
                    accent = if (report.toMove > 0) MaterialTheme.colorScheme.primary else success,
                )
                StatCard(
                    "Already organized",
                    report.alreadyOrganized.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard("Skipped", report.skipped.toString(), modifier = Modifier.weight(1f))
            }
        }

        Text(
            "Planned moves — dry-run, nothing is moved",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = Spacing.lg),
        )

        if (moves.isEmpty()) {
            Text(
                "Everything is already organized.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.md),
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = Spacing.md),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    MoveHeaderRow()
                    HorizontalDivider()
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(moves, key = { it.from.toString() }) { move ->
                            MoveRow(move)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.md))
                        }
                    }
                }
            }
        }

        Text(
            "Apply the plan with the CLI: melody-sync organize --apply",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
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
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Destination",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            "Reason",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Text(
            "Status",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(64.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveRow(move: PlannedMove) {
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
            text = "MOVE",
            tone = PillTone.PRIMARY,
            modifier = Modifier.width(64.dp),
        )
    }
}

@Composable
private fun isDark(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun androidx.compose.ui.graphics.Color.luminance(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)
