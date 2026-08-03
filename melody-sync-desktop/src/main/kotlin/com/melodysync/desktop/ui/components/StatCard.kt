package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.HiFiShapes
import com.melodysync.desktop.theme.Spacing

/**
 * Hi-Fi Editorial metric card: serif value, sans label, optional accent.
 * See docs/standards/DesignSystem.md §4.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    technical: Boolean = false,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = HiFiShapes.material.small,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                ),
                maxLines = 1,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = Spacing.xs),
                maxLines = 1,
            )
        }
    }
}
