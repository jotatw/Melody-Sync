package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.UpdateStatus
import com.melodysync.desktop.theme.Spacing
import com.melodysync.platform.installation.InstallationChannel
import com.melodysync.platform.installation.InstallationPaths
import com.melodysync.platform.system.VersionInfo

@Composable
fun SettingsSection(state: AppState) {
    LaunchedEffect(Unit) { state.refreshInstallationInfo() }

    Column(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Settings",
            subtitle = "Application preferences",
        )

        SettingsCard("Application") {
            Text("Music directory", style = MaterialTheme.typography.titleSmall)
            note("Set the music directory in the Library view (the path field above the Scan button).")
            note("Your library loads automatically from the database on startup — no rescan needed.")

            Text(
                "Theme",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = Spacing.md),
            )
            note("Use the sun/moon icon in the top bar to switch between light and dark.")

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Group songs by first letter", style = MaterialTheme.typography.bodyMedium)
                    note("Show a letter header above each alphabetical group in the library list.")
                }
                Switch(
                    checked = state.groupByLetter,
                    onCheckedChange = { state.toggleGroupByLetter() },
                )
            }
        }

        SettingsCard("Installation") {
            InstallationInformationSection(state)
        }

        SettingsCard("Updates") {
            UpdatesSection(state)
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md)) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(Spacing.sm))
            content()
        }
    }
}

@Composable
private fun note(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.xs),
    )
}

@Composable
private fun InstallationInformationSection(state: AppState) {
    val info = state.installationInfo
    val statusLabel = when {
        state.updateAvailable -> "Update available"
        state.updateStatus == UpdateStatus.ERROR -> "Needs attention"
        else -> "Healthy"
    }
    val statusColor = when {
        state.updateAvailable -> MaterialTheme.colorScheme.primary
        state.updateStatus == UpdateStatus.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.tertiary
    }

    Column(modifier = Modifier.padding(top = Spacing.xs)) {
        InfoRow("Version", VersionInfo.displayVersion)
        InfoRow("Installed", info?.let { "v${it.version}" } ?: "not detected")
        InfoRow("Channel", info?.channel?.ifBlank { "—" } ?: "—")
        InfoRow("Directory", InstallationPaths.installDir().toString())
        InfoRow("Java", System.getProperty("java.version") ?: "—")
        InfoRow("Build", info?.build?.ifBlank { "—" } ?: "—")
        InfoRow("Status", statusLabel, valueColor = statusColor)
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f),
        )
    }
}

@Composable
private fun UpdatesSection(state: AppState) {
    Text(
        if (state.updateSourceBased) {
            "Rebuilds and reinstalls from the Melody Sync source checkout. Requires Java 21."
        } else {
            "Downloads the latest published Melody Sync release and installs it."
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.xs),
    )

    Text(
        "Channel",
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = Spacing.md),
    )
    Row(
        modifier = Modifier.padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        val channelLocked = state.updateStatus == UpdateStatus.CHECKING ||
            state.updateStatus == UpdateStatus.RUNNING
        InstallationChannel.entries
            .filter { it != InstallationChannel.SOURCE }
            .forEach { channel ->
                FilterChip(
                    selected = state.updateChannel == channel,
                    onClick = { state.selectUpdateChannel(channel) },
                    enabled = !channelLocked,
                    label = {
                        Text(channel.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                )
            }
    }

    when (state.updateStatus) {
        UpdateStatus.IDLE -> {
            Button(
                onClick = state::checkForUpdates,
                modifier = Modifier.padding(top = Spacing.md),
            ) {
                Text("Check for updates")
            }
        }
        UpdateStatus.CHECKING -> {
            Button(onClick = {}, enabled = false, modifier = Modifier.padding(top = Spacing.md)) {
                Text("Checking…")
            }
            Text(
                state.updatePhase,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        UpdateStatus.RUNNING -> {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = Spacing.md))
            Text(
                state.updatePhase,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
        UpdateStatus.DONE -> {
            val message = state.updateMessage.orEmpty()
            val updateActionLabel = if (state.updateSourceBased) "Rebuild & Install" else "Download & Install"
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    state.updateAvailable -> MaterialTheme.colorScheme.primary
                    message.contains("failed", ignoreCase = true) -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(top = Spacing.md),
            )
            if (state.updateAvailable) {
                Button(
                    onClick = { state.runUpdate(force = true) },
                    modifier = Modifier.padding(top = Spacing.sm),
                ) {
                    Text(updateActionLabel)
                }
            } else if (message.contains("Already up to date")) {
                OutlinedButton(
                    onClick = { state.runUpdate(force = true) },
                    modifier = Modifier.padding(top = Spacing.sm),
                ) {
                    Text(if (state.updateSourceBased) "Force Rebuild" else "Force Update")
                }
            }
        }
        UpdateStatus.ERROR -> {
            state.updateMessage?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Spacing.md),
                )
            }
            OutlinedButton(
                onClick = state::checkForUpdates,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                Text("Retry")
            }
        }
    }
}
