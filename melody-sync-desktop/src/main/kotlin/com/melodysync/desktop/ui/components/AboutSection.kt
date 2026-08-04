package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.melodysync.desktop.theme.Spacing
import com.melodysync.platform.system.VersionInfo

@Composable
fun AboutSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)) {
        SectionHeader(
            title = "About",
            subtitle = "Melody Sync",
        )

        Text(
            "Melody Sync is a personal tool for organizing, analyzing and exploring your local music library.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Spacing.md),
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
    }
}
