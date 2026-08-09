package com.melodysync.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.theme.Indicators
import com.melodysync.desktop.theme.colorRoles

enum class PillTone { PRIMARY, SUCCESS, WARNING, INFO, DANGER, NEUTRAL }

/**
 * Semantic status badge with a small LED-style indicator. The LED is a visual
 * cue only; color meaning remains semantic and comes from [colorRoles].
 */
@Composable
fun StatusPill(
    text: String,
    tone: PillTone = PillTone.NEUTRAL,
    modifier: Modifier = Modifier,
) {
    val roles = colorRoles()
    val (foreground, background) = when (tone) {
        PillTone.PRIMARY -> roles.primaryAction to roles.primaryAction
        PillTone.SUCCESS -> roles.success to roles.success
        PillTone.WARNING -> roles.warning to roles.warning
        PillTone.INFO -> roles.info to roles.info
        PillTone.DANGER -> roles.danger to roles.danger
        PillTone.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        color = background.copy(alpha = 0.14f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            LedDot(foreground)
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = foreground,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun LedDot(color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(Indicators.led)
            .background(color, CircleShape),
    )
}
