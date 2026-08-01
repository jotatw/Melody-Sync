package com.melodysync.scanner

import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.nio.file.Path

val SUPPORTED_AUDIO_SUFFIXES: Set<String> = setOf(
    ".aac",
    ".flac",
    ".m4a",
    ".mp3",
    ".ogg",
    ".opus",
    ".wav",
)

fun discover(directory: Path): List<Path> {
    if (!Files.exists(directory)) throw NoSuchFileException(directory.toString())
    if (!Files.isDirectory(directory)) throw NotDirectoryException(directory.toString())

    return Files.walk(directory).use { paths ->
        paths.filter { file ->
            Files.isRegularFile(file) && file.fileName.toString().lowercase().substringAfterLast('.').let { ".$it" in SUPPORTED_AUDIO_SUFFIXES }
        }.sorted().toList()
    }
}
