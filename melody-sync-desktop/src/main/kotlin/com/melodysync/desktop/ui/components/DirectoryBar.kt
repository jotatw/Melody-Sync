package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.ScanStatus
import com.melodysync.desktop.state.WatchStatus
import javax.swing.JFileChooser

@Composable
fun DirectoryBar(state: AppState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(
                when {
                    state.status == ScanStatus.SCANNING -> "Scanning…"
                    state.songs.isNotEmpty() -> "Rescan"
                    else -> "Scan Library"
                },
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        WatchToggle(state)
    }

    when (state.status) {
        ScanStatus.SCANNING -> Text(
            "Scanning library…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        ScanStatus.DONE -> {
            if (state.progressText.isNotBlank()) {
                Text(
                    state.progressText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

    when (state.watchStatus) {
        WatchStatus.WATCHING -> Text(
            "File watcher active — library stays in sync automatically",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp),
        )
        WatchStatus.ERROR -> {
            state.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        WatchStatus.STOPPED -> Unit
    }
}

@Composable
private fun WatchToggle(state: AppState) {
    val watching = state.watchStatus == WatchStatus.WATCHING
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
