package com.melodysync.scanner

import com.melodysync.model.Song
import java.nio.file.Files
import java.nio.file.Path

fun scan(directory: Path): List<Song> =
    discover(directory).map { path ->
        val size = Files.size(path)
        readMetadata(Song(path = path, size = size))
    }
