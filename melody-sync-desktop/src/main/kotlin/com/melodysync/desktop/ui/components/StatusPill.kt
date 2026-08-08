package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.colorRoles

enum class PillTone { PRIMARY, SUCCESS, WARNING, INFO, DANGER, NEUTRAL }

/**
 * Small semantic status badge (Healthy, MOVE, DUP, Needs move…). Colors come
 * from [colorRoles], so state meaning is consistent across every screen.
 */
@Composable
fun StatusPill(
    text: String,
    tone: PillTone = PillTone.NEUTRAL,
    modifier: Modifier = Modifier,
) {
    val roles = colorRoles()
    val (background, foreground) = when (tone) {
        PillTone.PRIMARY -> roles.primaryAction to roles.primaryAction
        PillTone.SUCCESS -> roles.success to roles.success
        PillTone.WARNING -> roles.warning to roles.warning
        PillTone.INFO -> roles.info to roles.info
        PillTone.DANGER -> roles.danger to roles.danger
        PillTone.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = background.copy(alpha = 0.14f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
