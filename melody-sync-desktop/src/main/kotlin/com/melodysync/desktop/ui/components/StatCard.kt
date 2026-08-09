package com.melodysync.desktop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.HiFiShapes
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.Strokes

/**
 * Hi-Fi Editorial metric card: serif value, sans label, optional leading
 * icon and a restrained accent rule. It reads like an instrument readout,
 * without imitating physical hardware literally.
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    icon: ImageVector? = null,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = HiFiShapes.material.small,
        border = BorderStroke(
            width = Strokes.hairline,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = Strokes.emphasis, width = 1.dp)
                    .background(accent.copy(alpha = 0.75f)),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Spacing.sm),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                    ),
                    maxLines = 1,
                    modifier = if (icon != null) Modifier.padding(start = Spacing.sm) else Modifier,
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(top = Spacing.xs),
                maxLines = 1,
            )
        }
    }
}
