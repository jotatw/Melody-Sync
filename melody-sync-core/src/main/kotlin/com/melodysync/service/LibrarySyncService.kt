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

        var added = 0
        var updated = 0

        songs.forEach { song ->
            if (MusicRepository.exists(song.path)) {
                MusicRepository.updateByPath(song)
                updated++
            } else {
                MusicRepository.insert(song)
                added++
            }
        }

        val pathsToRemove = existingPaths - scannedPaths
        pathsToRemove.forEach { MusicRepository.deleteByPath(it) }

        return SyncResult(
            added = added,
            updated = updated,
            removed = pathsToRemove.size,
            totalInDatabase = MusicRepository.count().toInt(),
        )
    }
}
