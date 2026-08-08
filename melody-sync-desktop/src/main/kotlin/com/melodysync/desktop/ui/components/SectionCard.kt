package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.Spacing

/**
 * Card shown while a section-level operation is running: an indeterminate
 * progress bar plus the current status text. Shared by Health, Duplicates,
 * Organize (and future HUD actions) so every section reports progress the
 * same way.
 */
@Composable
fun ProgressCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(top = Spacing.md),
    ) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = Spacing.sm),
            )
        }
    }
}

/**
 * Card that frames a section result: an accent-colored headline with the
 * result content below. Used as the summary header of the Health,
 * Duplicates and Organize reports.
 */
@Composable
fun ResultCard(
    headline: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            Text(
                headline,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            content()
        }
    }
}
