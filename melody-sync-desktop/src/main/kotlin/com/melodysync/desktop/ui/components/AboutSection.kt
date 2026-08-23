package com.melodysync.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.ui.loadAppIcon
import com.melodysync.platform.system.VersionInfo

@Composable
fun AboutSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "About",
            subtitle = "Melody Sync",
        )

        val logo = loadAppIcon()
        Image(
            bitmap = logo,
            contentDescription = "Melody Sync logo",
            modifier = Modifier.size(96.dp).padding(top = Spacing.sm),
        )

        Text(
            "Melody Sync is a personal tool for organizing, analyzing and exploring your local music library.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Spacing.sm),
        )

        Text(
            "Version ${VersionInfo.displayVersion}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.md),
        )

        Text(
            "Built with Kotlin and Compose Desktop. MIT licensed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.xs),
        )

        Text(
            "Keyboard shortcuts",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = Spacing.lg),
        )

        listOf(
            "Ctrl+1…6" to "Switch section",
            "Ctrl+B" to "Toggle sidebar",
            "F5" to "Rescan the library",
            "Esc" to "Close the Quick Fix panel",
        ).forEach { (keys, action) ->
            Text(
                "$keys — $action",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.xs),
            )
        }
    }
}
