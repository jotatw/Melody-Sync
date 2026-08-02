package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
        ) {
            Text(if (state.status == ScanStatus.SCANNING) "Scanning…" else "Scan")
        }
        Button(
            onClick = {
                if (state.watchStatus == WatchStatus.WATCHING) {
                    state.stopWatching()
                } else {
                    state.startWatching()
                }
            },
            enabled = state.directory.isNotBlank(),
        ) {
            Text(if (state.watchStatus == WatchStatus.WATCHING) "Stop Watch" else "Watch")
        }
    }

    when (state.status) {
        ScanStatus.SCANNING -> Text(
            state.progressText,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
        )
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

    when (state.watchStatus) {
        WatchStatus.WATCHING -> Text(
            "Watching for changes in ${state.directory}…",
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
