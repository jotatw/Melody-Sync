package com.melodysync.model

import java.nio.file.Path

data class Song(
    val path: Path,
    val size: Long,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val duration: Double? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    val codec: String? = null,
) {
    val filename: String
        get() = path.fileName.toString()

    val extension: String
        get() {
            val name = path.fileName.toString()
            val dot = name.lastIndexOf('.')
            return if (dot > 0 && dot < name.length - 1) name.substring(dot + 1).lowercase() else ""
        }

    val isLossless: Boolean
        get() = extension in setOf("flac", "wav", "aiff")
    
    val directory: Path
        get() = path.parent

    val sizeMb: Double
        get() = size / (1024.0 * 1024.0)

    val sizeGb: Double
        get() = size / (1024.0 * 1024.0 * 1024.0)

    val durationMinutes: Double
        get() = (duration ?: 0.0) / 60.0

    val hasMetadata: Boolean
        get() = title != null && title.isNotBlank() && artist != null && artist.isNotBlank()
}