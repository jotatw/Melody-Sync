package com.melodysync.model

data class LibraryStatistics(
    val totalSongs: Int,
    val uniqueArtists: Int,
    val uniqueAlbums: Int,
    val totalSize: Long,
    val formats: Map<String, Int>,
    val totalDuration: Double,
    val averageBitrate: Double,
) {
    val totalSizeMb: Double
        get() = totalSize / (1024.0 * 1024.0)

    val totalSizeGb: Double
        get() = totalSize / (1024.0 * 1024.0 * 1024.0)

    val totalDurationMinutes: Double
        get() = totalDuration / 60.0

    val totalDurationHours: Double
        get() = totalDuration / 3600.0

    val averageDuration: Double
        get() = if (totalSongs == 0) 0.0 else totalDuration / totalSongs

    val isEmpty: Boolean
        get() = totalSongs == 0

    val averageBitrateKbps: Double
        get() = averageBitrate / 1000.0
}