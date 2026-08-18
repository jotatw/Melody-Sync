package com.melodysync.desktop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.RankedItem
import com.melodysync.desktop.state.Section
import com.melodysync.desktop.theme.HiFiDarkColors
import com.melodysync.desktop.theme.HiFiLightColors
import com.melodysync.desktop.theme.Spacing
import com.melodysync.desktop.theme.TechnicalStyleSmall
import io.github.koalaplot.core.pie.DefaultSlice
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi

@Composable
fun StatisticsSection(state: AppState) {
    val stats = state.statistics
    val analytics = state.analytics

    if (stats == null || analytics == null) {
        EmptyState(
            icon = Icons.Filled.Insights,
            title = "No library data available",
            message = "Scan a music directory to build your library.",
            action = {
                Button(onClick = { state.setSection(Section.LIBRARY) }) {
                    Text("Go to Library to Scan")
                }
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = Spacing.sm),
    ) {
        SectionHeader(title = "Library Statistics", subtitle = "Overview of your library")

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md), modifier = Modifier.padding(top = Spacing.md)) {
            StatCard("Songs", stats.totalSongs.toString(), modifier = Modifier.weight(1f))
            StatCard("Artists", stats.uniqueArtists.toString(), modifier = Modifier.weight(1f))
            StatCard("Albums", stats.uniqueAlbums.toString(), modifier = Modifier.weight(1f))
            StatCard("Hours", "%.1f".format(stats.totalDurationHours), modifier = Modifier.weight(1f))
            StatCard("Size", "%.2f GB".format(stats.totalSizeGb), modifier = Modifier.weight(1f))
        }

        Text(
            "Average bitrate: ${"%.0f".format(stats.averageBitrateKbps)} kbps · " +
                "Average duration: ${"%.1f".format(stats.averageDuration / 60.0)} min",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.md),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            FormatDonut(
                formats = analytics.formats,
                onFormatClick = { state.exploreFormat(it) },
                modifier = Modifier.weight(1f),
            )
            TopListCard(
                title = "Top Artists",
                items = analytics.topArtists,
                onClick = { state.exploreArtist(it) },
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.lg))

        // Top albums navigate to Library with the album filter preserved (Block 04).
        TopListCard(
            title = "Top Albums",
            items = analytics.topAlbums,
            onClick = { state.exploreAlbum(it) },
        )
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun FormatDonut(
    formats: List<RankedItem>,
    onFormatClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            Text("Formats", style = MaterialTheme.typography.titleMedium)

            if (formats.isEmpty()) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
                return@Column
            }

            val total = formats.sumOf { it.count }.toFloat()
            val palette = listOf(
                Color(0xFFFF6B00),
                Color(0xFFFFCC00),
                Color(0xFF3DA5FF),
                Color(0xFF4ADE80),
                Color(0xFFC514D2),
                Color(0xFFF87171),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = Spacing.sm),
            ) {
                PieChart(
                    values = formats.map { it.count.toFloat() },
                    holeSize = 0.45f,
                    modifier = Modifier.fillMaxSize(),
                    slice = { index ->
                        DefaultSlice(
                            color = palette[index % palette.size],
                            gap = 1f,
                            hoverExpandFactor = 1.03f,
                        )
                    },
                )
            }

            formats.forEach { item ->
                val pct = item.count / total * 100
                val clickModifier = if (onFormatClick != null) {
                    Modifier.clickable { onFormatClick(item.name) }
                } else {
                    Modifier
                }
                Row(
                    modifier = clickModifier.fillMaxWidth().padding(top = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(".${item.name}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${item.count} · ${"%.0f".format(pct)}%  View →",
                        style = TechnicalStyleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TopListCard(
    title: String,
    items: List<RankedItem>,
    modifier: Modifier = Modifier,
    onClick: ((String) -> Unit)? = null,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacing.md).fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            if (items.isEmpty()) {
                Text("No data", style = MaterialTheme.typography.bodySmall)
                return@Column
            }

            val max = items.maxOf { it.count }
            val accent = if (isDark()) HiFiDarkColors.Primary else HiFiLightColors.Primary

            items.forEach { item ->
                val clickModifier = if (onClick != null) {
                    Modifier.clickable { onClick(item.name) }
                } else {
                    Modifier
                }
                Row(
                    modifier = clickModifier.fillMaxWidth().padding(top = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        modifier = Modifier.width(140.dp),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.small,
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(((item.count.toFloat() / max) * 100).dp)
                                .background(accent, MaterialTheme.shapes.small),
                        )
                    }
                    Text(
                        "${item.count}  View →",
                        style = TechnicalStyleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun isDark(): Boolean =
    MaterialTheme.colorScheme.background.luminance() < 0.5f

private fun Color.luminance(): Float =
    (0.299f * red + 0.587f * green + 0.114f * blue)
