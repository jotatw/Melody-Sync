package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.theme.Spacing

/**
 * Hi-Fi Editorial library dashboard header.
 * Shows the five key metrics as StatCards plus the last scan time.
 * Self-contained so it can be reused on other platforms.
 */
@Composable
fun LibraryHeader(state: AppState) {
    val stats = state.statistics ?: return

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatCard("Songs", stats.totalSongs.toString(), modifier = Modifier.weight(1f))
        StatCard("Artists", stats.uniqueArtists.toString(), modifier = Modifier.weight(1f))
        StatCard("Albums", stats.uniqueAlbums.toString(), modifier = Modifier.weight(1f))
        StatCard("Hours", "%.1f".format(stats.totalDurationHours), modifier = Modifier.weight(1f))
        StatCard("Size", "%.2f GB".format(stats.totalSizeGb), modifier = Modifier.weight(1f))
    }

    Text(
        "Library synchronized · ${stats.totalSongs} songs analyzed",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.sm),
    )
}
