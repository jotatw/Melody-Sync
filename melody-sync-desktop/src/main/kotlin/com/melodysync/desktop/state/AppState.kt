package com.melodysync.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.LibraryStatistics
import com.melodysync.model.Song
import com.melodysync.scanner.calculateStatistics
import com.melodysync.service.LibrarySyncService
import com.melodysync.service.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path

enum class ScanStatus {
    IDLE,
    SCANNING,
    DONE,
    ERROR,
}

class AppState(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {

    var directory by mutableStateOf("")
        private set

    var status by mutableStateOf(ScanStatus.IDLE)
        private set

    var progressText by mutableStateOf("")
        private set

    var lastResult: SyncResult? by mutableStateOf(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var songs by mutableStateOf<List<Song>>(emptyList())
        private set

    var statistics by mutableStateOf<LibraryStatistics?>(null)
        private set

    var query by mutableStateOf("")
        private set

    val filteredSongs: List<Song>
        get() {
            val q = query.trim().lowercase()
            return if (q.isEmpty()) {
                songs
            } else {
                songs.filter { song ->
                    song.title?.lowercase()?.contains(q) == true ||
                        song.artist?.lowercase()?.contains(q) == true ||
                        song.album?.lowercase()?.contains(q) == true
                }
            }
        }

    fun updateDirectory(value: String) {
        directory = value
    }

    fun updateQuery(value: String) {
        query = value
    }

    fun scan() {
        if (status == ScanStatus.SCANNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        scope.launch {
            status = ScanStatus.SCANNING
            progressText = "Scanning..."
            try {
                MusicDatabase.connect()
                val result = LibrarySyncService.syncDirectory(dir)
                lastResult = result
                songs = MusicRepository.findAll()
                statistics = calculateStatistics(songs)
                progressText = "Done: +${result.added} added, ${result.updated} updated, ${result.removed} removed"
                status = ScanStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Scan failed"
                progressText = ""
                status = ScanStatus.ERROR
            }
        }
    }
}
