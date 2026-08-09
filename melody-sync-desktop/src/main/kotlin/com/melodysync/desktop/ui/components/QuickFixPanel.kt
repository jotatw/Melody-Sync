package com.melodysync.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.melodysync.model.TagSuggestion
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
    var reviewing by remember { mutableStateOf<FixSuggestion?>(null) }

    LaunchedEffect(song.path) {
        state.clearQuickFixYoutube()
        state.clearLyrics()
        reviewing = null
    }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Quick Fix",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { state.selectSong(null) }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close Quick Fix",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
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
            val onReview: (FixSuggestion) -> Unit = { reviewing = it }
            LocalSuggestionsSection(state, song, localSuggestions, writeSupported, onReview)
            if (state.youtubeEnabled) {
                YoutubeSuggestionsSection(state, song, writeSupported, onReview)
            }
            LyricsSection(state, song)
        }
    }

    reviewing?.let { suggestion ->
        SuggestionReviewDialog(
            song = song,
            suggestion = suggestion,
            onDismiss = { reviewing = null },
            onApply = { edited ->
                reviewing = null
                state.applyQuickFix(song, edited)
            },
        )
    }
}

@Composable
private fun LyricsSection(state: AppState, song: Song) {
    Text(
        "Lyrics",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    when {
        state.lyricsLoading -> {
            Row(
                modifier = Modifier.padding(top = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    "Fetching lyrics…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        state.lyricsLoaded && !state.lyrics.isNullOrBlank() -> {
            Text(
                state.lyrics!!,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        state.lyricsLoaded -> {
            Text(
                "No lyrics found.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        else -> {
            OutlinedButton(
                onClick = { state.loadLyrics(song) },
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text("Get lyrics")
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
private fun LocalSuggestionsSection(state: AppState, song: Song, suggestions: List<FixSuggestion>, writeSupported: Boolean, onReview: (FixSuggestion) -> Unit) {
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
        SuggestionItemCard(state, song, suggestion, writeSupported, onReview)
    }
}

@Composable
private fun YoutubeSuggestionsSection(state: AppState, song: Song, writeSupported: Boolean, onReview: (FixSuggestion) -> Unit) {
    Text(
        "YouTube suggestion",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    val suggestions = state.quickFixYoutubeSuggestions
    var selectedIndex by remember(suggestions) { mutableStateOf(0) }

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
        suggestions.isNotEmpty() -> {
            Text(
                "Select the candidate that best matches this file.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            suggestions.forEachIndexed { index, suggestion ->
                val selected = index == selectedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xs)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            MaterialTheme.shapes.small,
                        )
                        .clickable { selectedIndex = index }
                        .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            suggestion.title,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        suggestion.subtitle?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (suggestion.openUrl != null) {
                        IconButton(onClick = { openInBrowser(suggestion.openUrl!!) }) {
                            Icon(
                                Icons.Filled.OpenInNew,
                                contentDescription = "Open on YouTube",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { onReview(suggestions[selectedIndex]) },
                enabled = !state.quickFixApplying && writeSupported,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text(if (state.quickFixApplying) "Applying…" else "Review & Apply")
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
private fun SuggestionItemCard(state: AppState, song: Song, suggestion: FixSuggestion, writeSupported: Boolean, onReview: (FixSuggestion) -> Unit) {
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
                onClick = { onReview(suggestion) },
                enabled = !state.quickFixApplying && writeSupported,
            ) {
                Text(if (state.quickFixApplying) "Applying…" else "Review & Apply")
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

/**
 * Editable review of a suggestion before Apply (metadata workflow §5, §7):
 * the current values are shown separately from the editable suggestion, and
 * the source is visible. The user can edit or reject; nothing is written
 * without the explicit Apply action.
 */
@Composable
private fun SuggestionReviewDialog(
    song: Song,
    suggestion: FixSuggestion,
    onDismiss: () -> Unit,
    onApply: (TagSuggestion) -> Unit,
) {
    var title by remember(suggestion) { mutableStateOf(suggestion.tagSuggestion.title ?: "") }
    var artist by remember(suggestion) { mutableStateOf(suggestion.tagSuggestion.artist ?: "") }
    var album by remember(suggestion) { mutableStateOf(suggestion.tagSuggestion.album ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review suggestion") },
        text = {
            Column {
                Text(
                    "Source: ${suggestion.sourceLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Current",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
                Text("Title: ${song.title ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Artist: ${song.artist ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Album: ${song.album ?: "—"}", style = MaterialTheme.typography.bodySmall)

                Text(
                    "Suggested values (editable)",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = Spacing.md),
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApply(
                        TagSuggestion(
                            title = title.trim().ifBlank { null },
                            artist = artist.trim().ifBlank { null },
                            album = album.trim().ifBlank { null },
                        ),
                    )
                },
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
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
