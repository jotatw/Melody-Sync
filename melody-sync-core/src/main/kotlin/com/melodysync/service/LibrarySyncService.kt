package com.melodysync.service

import com.melodysync.database.MusicRepository
import com.melodysync.model.Song
import com.melodysync.scanner.scan
import java.nio.file.Path

data class SyncResult(
    val added: Int,
    val updated: Int,
    val removed: Int,
    val totalInDatabase: Int,
)

object LibrarySyncService {

    fun syncDirectory(directory: Path): SyncResult =
        syncSongs(scan(directory))

    fun syncSongs(songs: List<Song>): SyncResult {
        val scannedPaths = songs.map { it.path }.toSet()
        val existingPaths = MusicRepository.findAll().map { it.path }.toSet()

        val (added, updated) = MusicRepository.upsertAll(songs, existingPaths)

        val pathsToRemove = existingPaths - scannedPaths
        val removed = MusicRepository.deleteAllByPath(pathsToRemove)

        return SyncResult(
            added = added,
            updated = updated,
            removed = removed,
            totalInDatabase = MusicRepository.count().toInt(),
        )
    }
}
