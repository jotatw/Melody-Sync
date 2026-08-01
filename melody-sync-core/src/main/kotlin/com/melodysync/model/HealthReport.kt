package com.melodysync.model

import java.nio.file.Path

data class HealthReport(
    val directory: Path,
    val totalFiles: Int,
    val audioFiles: Int,
    val nonAudio: List<FileCategory>,
    val unknownExtensions: List<String>,
    val songsWithoutMetadata: List<Song>,
    val songsWithZeroDuration: List<Song>,
    val orphanedEntries: List<String>,
) {
    val totalNonAudio: Int get() = nonAudio.sumOf { it.count }

    val songsWithMetadataIssues: Int
        get() = songsWithoutMetadata.size + songsWithZeroDuration.size + orphanedEntries.size
}