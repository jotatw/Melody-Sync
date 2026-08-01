package com.melodysync.service

import com.melodysync.database.MusicRepository
import com.melodysync.model.HealthReport
import com.melodysync.model.Song
import com.melodysync.model.categoryForExtension
import com.melodysync.model.CATEGORY_DESCRIPTIONS
import com.melodysync.model.FileCategory
import com.melodysync.scanner.SUPPORTED_AUDIO_SUFFIXES
import java.nio.file.Files
import java.nio.file.Path

object LibraryHealthService {

    fun analyze(directory: Path): HealthReport {
        val allFiles = listFiles(directory)
        val audioFiles = allFiles.filter { isAudioFile(it) }
        val nonAudioFiles = allFiles.filter { !isAudioFile(it) }

        val grouped = groupByCategory(nonAudioFiles)
        val unknownExtensions = nonAudioFiles
            .map { it.extension() }
            .filter { it.isNotEmpty() && categoryForExtension(it) == null }
            .distinct()
            .sorted()

        val songsInDatabase = MusicRepository.findAll().filter { song ->
            song.path.startsWith(directory.toAbsolutePath())
        }

        return HealthReport(
            directory = directory,
            totalFiles = allFiles.size,
            audioFiles = audioFiles.size,
            nonAudio = grouped,
            unknownExtensions = unknownExtensions,
            songsWithoutMetadata = songsInDatabase.filter { !it.hasMetadata },
            songsWithZeroDuration = songsInDatabase.filter { it.duration == null || it.duration == 0.0 },
            orphanedEntries = songsInDatabase
                .map { it.path }
                .filter { !Files.exists(it) }
                .map { it.toString() },
        )
    }

    fun analyzeFromDatabase(directory: Path): HealthReport {
        val songsInDatabase = MusicRepository.findAll().filter { song ->
            song.path.startsWith(directory.toAbsolutePath())
        }
        val filesOnDisk = listFiles(directory)

        val audioFiles = filesOnDisk.filter { isAudioFile(it) }
        val nonAudioFiles = filesOnDisk.filter { !isAudioFile(it) }

        val grouped = groupByCategory(nonAudioFiles)
        val unknownExtensions = nonAudioFiles
            .map { it.extension() }
            .filter { it.isNotEmpty() && categoryForExtension(it) == null }
            .distinct()
            .sorted()

        return HealthReport(
            directory = directory,
            totalFiles = filesOnDisk.size,
            audioFiles = audioFiles.size,
            nonAudio = grouped,
            unknownExtensions = unknownExtensions,
            songsWithoutMetadata = songsInDatabase.filter { !it.hasMetadata },
            songsWithZeroDuration = songsInDatabase.filter { it.duration == null || it.duration == 0.0 },
            orphanedEntries = songsInDatabase
                .map { it.path }
                .filter { !Files.exists(it) }
                .map { it.toString() },
        )
    }

    private fun listFiles(directory: Path): List<Path> {
        if (!Files.exists(directory)) return emptyList()
        return Files.walk(directory).use { paths ->
            paths.filter { Files.isRegularFile(it) }.toList()
        }
    }

    private fun isAudioFile(path: Path): Boolean =
        ".${path.extension()}" in SUPPORTED_AUDIO_SUFFIXES

    private fun Path.extension(): String {
        val name = fileName.toString()
        val dot = name.lastIndexOf('.')
        return if (dot > 0 && dot < name.length - 1) name.substring(dot + 1).lowercase() else ""
    }

    private fun groupByCategory(files: List<Path>): List<FileCategory> =
        files.groupBy { categoryForExtension(it.extension()) ?: "unknown" }
            .map { (category, categoryFiles) ->
                FileCategory(
                    category = category,
                    description = CATEGORY_DESCRIPTIONS[category] ?: "unrecognized files",
                    extensions = categoryFiles.map { it.extension() }.toSet(),
                    count = categoryFiles.size,
                    totalSize = categoryFiles.sumOf { file ->
                        try {
                            Files.size(file)
                        } catch (_: Exception) {
                            0L
                        }
                    },
                )
            }
            .sortedBy { it.category }
}
