package com.melodysync.scanner

import com.melodysync.model.LibraryStatistics
import com.melodysync.model.Song

fun calculateStatistics(songs: List<Song>): LibraryStatistics {
    val artists = songs.mapNotNull { it.artist?.trim()?.takeIf { it.isNotBlank() } }.toSet()
    val albums = songs.mapNotNull { it.album?.trim()?.takeIf { it.isNotBlank() } }.toSet()

    val formats = songs.groupingBy { it.extension }.eachCount()

    val totalSize = songs.sumOf { it.size }
    val totalDuration = songs.sumOf { it.duration ?: 0.0 }

    val bitrates = songs.mapNotNull { it.bitrate }
    val averageBitrate = if (bitrates.isNotEmpty()) bitrates.average() else 0.0

    return LibraryStatistics(
        totalSongs = songs.size,
        uniqueArtists = artists.size,
        uniqueAlbums = albums.size,
        totalSize = totalSize,
        formats = formats,
        totalDuration = totalDuration,
        averageBitrate = averageBitrate,
    )
}