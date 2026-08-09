package com.melodysync.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.Strokes

/**
 * Hi-Fi Editorial section header: serif title + restrained technical subtitle
 * + optional action. A thin rule gives sections the visual rhythm of an
 * editorial spread without turning the UI into literal magazine chrome.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
            action?.invoke()
        }

        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm)
                .height(Strokes.hairline)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        )
    }
}
