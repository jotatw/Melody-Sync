package com.melodysync.desktop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState

@Composable
fun StatisticsSection(state: AppState) {
    val stats = state.statistics ?: run {
        Text(
            "No statistics yet. Scan a library first.",
            modifier = Modifier.padding(top = 24.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Library Statistics", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 12.dp)) {
            StatCard("Songs", stats.totalSongs.toString())
            StatCard("Artists", stats.uniqueArtists.toString())
            StatCard("Albums", stats.uniqueAlbums.toString())
            StatCard("Duration", "${"%.1f".format(stats.totalDurationHours)}h")
            StatCard("Size", "${"%.2f".format(stats.totalSizeGb)}GB")
        }

        Text(
            "Average bitrate: ${"%.0f".format(stats.averageBitrateKbps)} kbps · " +
                "Average duration: ${"%.1f".format(stats.averageDuration / 60.0)} min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )

        if (stats.formats.isNotEmpty()) {
            Text(
                "Formats: " + stats.formats.entries
                    .sortedByDescending { it.value }
                    .joinToString(" · ") { ".${it.key} (${it.value})" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
