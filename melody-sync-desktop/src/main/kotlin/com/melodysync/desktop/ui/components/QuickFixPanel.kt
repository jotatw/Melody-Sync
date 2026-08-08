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
import com.melodysync.metadata.MetadataFormatRegistry
import com.melodysync.model.MissingField
import com.melodysync.model.QualityFlag
import com.melodysync.model.Song
import com.melodysync.model.SongDiagnostics
import com.melodysync.service.FixSuggestion
import com.melodysync.service.LocalFixSource
import com.melodysync.service.QuickFixService

/**
 * Quick-Fix HUD right panel: diagnosis for the selected song plus suggestions
 * rendered by source (local heuristics, YouTube, and future sources). The
 * user validates every edit — nothing is applied automatically.
 */
@Composable
fun QuickFixPanel(state: AppState, song: Song) {
    val diagnostics = remember(song) { QuickFixService.diagnose(song) }
    val localSuggestions = remember(song) { LocalFixSource.suggest(song) }
    val writeSupported = remember(song) {
        MetadataFormatRegistry.providerFor(song.extension)?.supportsWrite ?: false
    }

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
            if (!writeSupported) {
                Text(
                    "Tag writing is not supported for this format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
            LocalSuggestionsSection(state, song, localSuggestions, writeSupported)
            if (state.youtubeEnabled) {
                YoutubeSuggestionsSection(state, song, writeSupported)
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
private fun LocalSuggestionsSection(state: AppState, song: Song, suggestions: List<FixSuggestion>, writeSupported: Boolean) {
    Text(
        "Local suggestion",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    if (suggestions.isEmpty()) {
        Text(
            "No fix available from the file name or folder.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        return
    }
    suggestions.forEach { suggestion ->
        SuggestionItemCard(state, song, suggestion, writeSupported)
    }
}

@Composable
private fun YoutubeSuggestionsSection(state: AppState, song: Song, writeSupported: Boolean) {
    Text(
        "YouTube suggestion",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
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
        state.quickFixYoutubeSuggestions.isNotEmpty() -> {
            state.quickFixYoutubeSuggestions.forEach { suggestion ->
                SuggestionItemCard(state, song, suggestion, writeSupported)
            }
        }
        state.quickFixYoutubeLoaded -> {
            Text(
                "No YouTube results.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        else -> {
            OutlinedButton(
                onClick = { state.loadYoutubeSuggestions(song) },
                enabled = !state.quickFixApplying,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text("Find on YouTube")
            }
        }
    }
}

@Composable
private fun SuggestionItemCard(state: AppState, song: Song, suggestion: FixSuggestion, writeSupported: Boolean) {
    Column(modifier = Modifier.padding(top = Spacing.sm)) {
        Text(
            suggestion.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        suggestion.subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Button(
                onClick = { state.applyQuickFix(song, suggestion.tagSuggestion) },
                enabled = !state.quickFixApplying && writeSupported,
            ) {
                Text(if (state.quickFixApplying) "Applying…" else "Apply")
            }
            if (suggestion.openUrl != null) {
                OutlinedButton(onClick = { openInBrowser(suggestion.openUrl!!) }) {
                    Text("Open")
                }
            }
        }
    }
}

private fun flagLabel(flag: QualityFlag): String = when (flag) {
    QualityFlag.LOW_BITRATE -> "Low bitrate"
    QualityFlag.ZERO_DURATION -> "Zero duration"
}

private fun openInBrowser(url: String) {
    try {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop.getDesktop().browse(java.net.URI(url))
        }
    } catch (_: Exception) {
        // no browser available; the URL is still shown in the panel
    }
}
