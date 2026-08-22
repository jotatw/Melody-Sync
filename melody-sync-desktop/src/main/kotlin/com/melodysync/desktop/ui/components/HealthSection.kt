package com.melodysync.desktop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.TaskStatus
import com.melodysync.desktop.state.Section
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyleSmall
import com.melodysync.desktop.theme.colorRoles
import com.melodysync.desktop.ui.window.LocalWindowSizeClass
import com.melodysync.model.HealthReport

private data class IssueSection(
    val title: String,
    val total: Int,
    val shown: List<String>,
    val paths: List<String>,
)

@Composable
fun HealthSection(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Library Health",
            subtitle = "Missing metadata, zero duration and orphaned entries.",
        )
        Button(
            onClick = state::analyzeHealth,
            enabled = state.directory.isNotBlank() && state.healthStatus != TaskStatus.RUNNING,
        ) {
            Text(if (state.healthStatus == TaskStatus.RUNNING) "Checking…" else "Analyze Health")
        }

        when (state.healthStatus) {
            TaskStatus.RUNNING -> {
                ProgressCard("Checking library health…")
            }
            TaskStatus.DONE -> {
                state.healthReport?.let { report ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
                    HealthReportView(state, report)
                }
            }
            TaskStatus.ERROR -> {
                state.errorMessage?.let {
                    StatusPill(it, PillTone.DANGER, Modifier.padding(top = Spacing.sm))
                }
            }
            TaskStatus.IDLE -> Unit
        }

        // Block 03 — duplicate findings are a Health concern. A summary card
        // keeps them visible here; the full workflow (inspection, confirmation,
        // trash) is one action away.
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))
        DuplicateGroupsCard(state)
    }
}

@Composable
private fun DuplicateGroupsCard(state: AppState) {
    val count = state.duplicateGroups.size
    ResultCard(
        headline = if (count > 0) {
            "$count duplicate group(s) found"
        } else {
            "Duplicate groups"
        },
        accent = if (count > 0) colorRoles().danger else colorRoles().primaryAction,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (count > 0) {
                    "Review the duplicate groups and decide what to keep or move to trash."
                } else {
                    "Detect duplicate groups and review them before any file is moved to trash."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { state.setSection(Section.DUPLICATES) }) {
                Text(if (count > 0) "Review groups" else "Open duplicate detection")
            }
        }
    }
}

@Composable
private fun HealthReportView(state: AppState, report: HealthReport) {
    val issues = report.songsWithMetadataIssues
    val score =
        (100.0 * (1.0 - issues.toDouble() / report.audioFiles.coerceAtLeast(1)))
            .toInt()
            .coerceIn(0, 100)
    val roles = colorRoles()
    val success = roles.success
    val scoreColor = when {
        score >= 90 -> success
        score >= 60 -> roles.warning
        else -> roles.danger
    }
    val headline = if (issues == 0) {
        "Library healthy"
    } else {
        "Score $score/100 · $issues issue(s) found"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        ResultCard(
            headline = headline,
            accent = scoreColor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val sizeClass = LocalWindowSizeClass.current
            if (sizeClass.isCompact) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    HealthScoreRing(score, scoreColor)
                    Column(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            StatCard("Audio files", report.audioFiles.toString(), modifier = Modifier.weight(1f), accent = success)
                            StatCard("Non-audio", report.totalNonAudio.toString(), modifier = Modifier.weight(1f))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            modifier = Modifier.padding(top = Spacing.md),
                        ) {
                            StatCard(
                                "Issues",
                                issues.toString(),
                                modifier = Modifier.weight(1f),
                                accent = if (issues > 0) colorRoles().danger else success,
                            )
                            StatCard("Total files", report.totalFiles.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
                ) {
                    HealthScoreRing(score, scoreColor)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            StatCard("Audio files", report.audioFiles.toString(), modifier = Modifier.weight(1f), accent = success)
                            StatCard("Non-audio", report.totalNonAudio.toString(), modifier = Modifier.weight(1f))
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            modifier = Modifier.padding(top = Spacing.md),
                        ) {
                            StatCard(
                                "Issues",
                                issues.toString(),
                                modifier = Modifier.weight(1f),
                                accent = if (issues > 0) colorRoles().danger else success,
                            )
                            StatCard("Total files", report.totalFiles.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = Spacing.lg),
        ) {
            IssueBreakdown(state, report, success)
            Recommendations(report)
        }
    }
}

@Composable
private fun HealthScoreRing(score: Int, color: Color, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier = modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - 2 * inset, size.height - 2 * inset)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(inset, inset),
                size = arcSize,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * score / 100f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(inset, inset),
                size = arcSize,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", style = MaterialTheme.typography.headlineMedium.copy(color = color))
            Text(
                "score",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IssueBreakdown(state: AppState, report: HealthReport, success: Color) {
    val sections = buildList {
        if (report.songsWithoutMetadata.isNotEmpty()) {
            add(
                IssueSection(
                    title = "Without metadata",
                    total = report.songsWithoutMetadata.size,
                    shown = report.songsWithoutMetadata.map { it.title ?: it.filename },
                    paths = report.songsWithoutMetadata.map { it.path.toString() },
                ),
            )
        }
        if (report.songsWithZeroDuration.isNotEmpty()) {
            add(
                IssueSection(
                    title = "Zero duration",
                    total = report.songsWithZeroDuration.size,
                    shown = report.songsWithZeroDuration.map { it.title ?: it.filename },
                    paths = report.songsWithZeroDuration.map { it.path.toString() },
                ),
            )
        }
        if (report.orphanedEntries.isNotEmpty()) {
            add(
                IssueSection(
                    title = "Orphaned entries",
                    total = report.orphanedEntries.size,
                    shown = report.orphanedEntries,
                    paths = report.orphanedEntries,
                ),
            )
        }
    }

    if (sections.isEmpty()) {
        Text(
            "No issues found — your library metadata is complete.",
            style = MaterialTheme.typography.bodyMedium,
            color = success,
        )
        return
    }

    Text("Issues", style = MaterialTheme.typography.titleMedium)
    sections.forEach { section ->
        Card(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md)) {
            Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${section.title} (${section.total})",
                        style = MaterialTheme.typography.titleMedium,
                        color = colorRoles().danger,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { state.reviewIssue(section.paths, section.title) }) {
                        Text("Review all")
                    }
                }
                section.shown.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            item,
                            style = TechnicalStyleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = { state.reviewIssue(listOf(section.paths[index])) },
                        ) {
                            Text("Review")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Recommendations(report: HealthReport) {
    val tips = buildList {
        if (report.songsWithoutMetadata.isNotEmpty()) {
            add("Add title and artist tags to ${report.songsWithoutMetadata.size} song(s) without metadata.")
        }
        if (report.songsWithZeroDuration.isNotEmpty()) {
            add("Re-rip or repair ${report.songsWithZeroDuration.size} song(s) with zero duration.")
        }
        if (report.orphanedEntries.isNotEmpty()) {
            add("Remove ${report.orphanedEntries.size} database row(s) pointing to missing files.")
        }
        if (report.nonAudio.isNotEmpty()) {
            add("Review non-audio files: ${report.nonAudio.joinToString(" · ") { "${it.category} (${it.count})" }}.")
        }
        if (isEmpty()) {
            add("Everything looks great — keep it up.")
        }
    }

    Text(
        "Recommendations",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = Spacing.lg),
    )
    tips.forEach { tip ->
        Text(
            "• $tip",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}


