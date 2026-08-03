package com.melodysync.desktop.state

import com.melodysync.model.Song

/** A ranked item with a count, used for top artists/albums lists. */
data class RankedItem(
    val name: String,
    val count: Int,
)

/** Derived analytics from the current songs in the library. */
data class AnalyticsData(
    val formats: List<RankedItem>,
    val topArtists: List<RankedItem>,
    val topAlbums: List<RankedItem>,
) {
    val totalFormats: Int get() = formats.sumOf { it.count }
}

fun computeAnalytics(songs: List<Song>, limit: Int = 10): AnalyticsData {
    val formats = songs
        .groupingBy { it.extension.ifBlank { "unknown" } }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(limit)
        .map { RankedItem(it.first, it.second) }

    val artists = songs
        .groupingBy { it.artist?.trim()?.takeIf { v -> v.isNotBlank() } ?: "Unknown" }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(limit)
        .map { RankedItem(it.first, it.second) }

    val albums = songs
        .groupingBy { it.album?.trim()?.takeIf { v -> v.isNotBlank() } ?: "Unknown" }
        .eachCount()
        .toList()
        .sortedByDescending { it.second }
        .take(limit)
        .map { RankedItem(it.first, it.second) }

    return AnalyticsData(formats = formats, topArtists = artists, topAlbums = albums)
}
