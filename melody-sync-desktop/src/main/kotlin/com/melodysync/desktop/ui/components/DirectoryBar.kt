package com.melodysync.desktop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.ScanStatus
import com.melodysync.desktop.state.WatchStatus
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.Strokes
import javax.swing.JFileChooser

/**
 * Directory-scoped control strip. The layout deliberately treats Scan as the
 * primary action and Watch as a persistent state, matching the Hi-Fi console
 * metaphor without turning controls into literal hardware replicas.
 */
@Composable
fun DirectoryBar(state: AppState) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(
            Strokes.hairline,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                OutlinedTextField(
                    value = state.directory,
                    onValueChange = state::updateDirectory,
                    label = { Text("Music directory") },
                    placeholder = { Text("/home/you/Music") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { chooseDirectory()?.let(state::updateDirectory) }) {
                            Icon(Icons.Filled.Folder, contentDescription = "Choose directory")
                        }
                    },
                    modifier = Modifier.weight(1f),
                )

                Button(
                    onClick = state::scan,
                    enabled = state.directory.isNotBlank() && state.status != ScanStatus.SCANNING,
                    modifier = Modifier.padding(vertical = Spacing.xs),
                ) {
                    Text(
                        when {
                            state.status == ScanStatus.SCANNING -> "Scanning…"
                            state.songs.isNotEmpty() -> "Rescan Library"
                            else -> "Scan Library"
                        },
                    )
                }

                WatchToggle(state)
            }

            when (state.status) {
                ScanStatus.SCANNING -> StatusPill("SCANNING", PillTone.PRIMARY, Modifier.padding(top = Spacing.sm))
                ScanStatus.DONE -> {
                    if (state.progressText.isNotBlank()) {
                        StatusPill(
                            state.progressText,
                            PillTone.SUCCESS,
                            Modifier.padding(top = Spacing.sm),
                        )
                    }
                }
                ScanStatus.ERROR -> {
                    state.errorMessage?.let {
                        StatusPill(it, PillTone.DANGER, Modifier.padding(top = Spacing.sm))
                    }
                }
                ScanStatus.IDLE -> Unit
            }

            when (state.watchStatus) {
                WatchStatus.WATCHING -> StatusPill(
                    "WATCHING · library stays in sync",
                    PillTone.INFO,
                    Modifier.padding(top = Spacing.sm),
                )
                WatchStatus.ERROR -> {
                    state.errorMessage?.let {
                        StatusPill(it, PillTone.DANGER, Modifier.padding(top = Spacing.sm))
                    }
                }
                WatchStatus.STOPPED -> Unit
            }
        }
    }
}

@Composable
private fun WatchToggle(state: AppState) {
    val watching = state.watchStatus == WatchStatus.WATCHING
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = if (watching) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
            contentDescription = null,
            tint = if (watching) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Switch(
            checked = watching,
            onCheckedChange = {
                if (watching) state.stopWatching() else state.startWatching()
            },
            enabled = state.directory.isNotBlank(),
        )
        Text(
            "Watch",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
