package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.Spacing

@Composable
fun SettingsSection(state: AppState) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "Settings",
            subtitle = "Application preferences",
        )

        Text(
            "Music directory",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = Spacing.md),
        )
        OutlinedTextField(
            value = state.directory,
            onValueChange = state::updateDirectory,
            label = { Text("Path") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
        )

        Text(
            "Theme",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Text(
            "Use the sun/moon icon in the top bar to switch between light and dark.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Text(
            "Library display",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = Spacing.lg),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Group songs by first letter", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Show a letter header above each alphabetical group in the library list.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
            Switch(
                checked = state.groupByLetter,
                onCheckedChange = { state.toggleGroupByLetter() },
            )
        }
    }
}
