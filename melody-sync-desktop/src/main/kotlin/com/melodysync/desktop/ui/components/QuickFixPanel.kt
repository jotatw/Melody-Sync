package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyle
import com.melodysync.desktop.theme.colorRoles
import com.melodysync.model.MissingField
import com.melodysync.model.QualityFlag
import com.melodysync.model.Song
import com.melodysync.model.SongDiagnostics
import com.melodysync.model.TagSuggestion
import com.melodysync.model.YouTubeVideoResult
import com.melodysync.service.QuickFixService

/**
 * Quick-Fix HUD right panel: diagnosis for the selected song plus local and
 * YouTube suggestions. The user validates every edit — nothing is applied
 * automatically (report-first philosophy).
 */
@Composable
fun QuickFixPanel(state: AppState, song: Song) {
    val diagnostics = remember(song) { QuickFixService.diagnose(song) }
    val localSuggestion = remember(song) { QuickFixService.localSuggestion(song) }

    LaunchedEffect(song.path) { state.clearQuickFixYoutube() }

    Surface(
        modifier = Modifier.fillMaxHeight().width(320.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
        ) {
            Text("Quick Fix", style = MaterialTheme.typography.titleMedium)
            Text(
                song.filename,
                style = TechnicalStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))

            DiagnosisSection(diagnostics)
            LocalSuggestionSection(state, song, localSuggestion)
            if (state.youtubeEnabled) {
                YoutubeSuggestionSection(state, song)
            }
        }
    }
}

@Composable
private fun DiagnosisSection(diagnostics: SongDiagnostics) {
    Text("Diagnosis", style = MaterialTheme.typography.titleSmall)
    if (!diagnostics.hasIssues) {
        val roles = colorRoles()
        Text(
            "No issues — this song looks good.",
            style = MaterialTheme.typography.bodyMedium,
            color = roles.success,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        return
    }
    diagnostics.missing.forEach { field ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            StatusPill(
                text = field.name.lowercase().replaceFirstChar { it.uppercase() },
                tone = PillTone.DANGER,
            )
            Text(
                "Missing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    diagnostics.flags.forEach { flag ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill(
                text = flagLabel(flag),
                tone = PillTone.WARNING,
            )
        }
    }
}

@Composable
private fun LocalSuggestionSection(state: AppState, song: Song, suggestion: TagSuggestion) {
    Text(
        "Local suggestion",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    if (!suggestion.hasChanges) {
        Text(
            "No fix available from the file name or folder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        return
    }
    SuggestionPreview(suggestion)
    Button(
        onClick = { state.applyQuickFix(song, suggestion) },
        enabled = !state.quickFixApplying,
        modifier = Modifier.padding(top = Spacing.sm),
    ) {
        Text(if (state.quickFixApplying) "Applying…" else "Apply")
    }
}

@Composable
private fun YoutubeSuggestionSection(state: AppState, song: Song) {
    Text(
        "YouTube suggestion",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    val current = state.quickFixYoutubeSuggestion
    when {
        state.quickFixYoutubeLoading -> {
            Row(
                modifier = Modifier.padding(top = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    "Searching YouTube…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        current != null && current.results.isNotEmpty() -> {
            val top = current.results.first()
            Text(
                top.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            Text(
                top.channel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            Button(
                onClick = { state.applyQuickFix(song, youtubeTagSuggestion(top)) },
                enabled = !state.quickFixApplying,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text(if (state.quickFixApplying) "Applying…" else "Apply from YouTube")
            }
        }
        current != null -> {
            Text(
                "No YouTube results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        else -> {
            OutlinedButton(
                onClick = { state.loadYoutubeSuggestion(song) },
                enabled = !state.quickFixApplying,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text("Find on YouTube")
            }
        }
    }
}

@Composable
private fun SuggestionPreview(suggestion: TagSuggestion) {
    Column(modifier = Modifier.padding(top = Spacing.xs)) {
        suggestion.title?.let { PreviewRow("Title", it) }
        suggestion.artist?.let { PreviewRow("Artist", it) }
        suggestion.album?.let { PreviewRow("Album", it) }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun flagLabel(flag: QualityFlag): String = when (flag) {
    QualityFlag.LOW_BITRATE -> "Low bitrate"
    QualityFlag.ZERO_DURATION -> "Zero duration"
}

private fun youtubeTagSuggestion(video: YouTubeVideoResult): TagSuggestion {
    val cleanedTitle = Regex("\\s*\\((Official (Audio|Video|Lyric Video)|Audio|Official)\\)\\s*$")
        .replace(video.title, "")
        .trim()
    return TagSuggestion(
        title = cleanedTitle.ifBlank { video.title },
        artist = video.channel.removeSuffix(" - Topic").trim().ifBlank { null },
    )
}
