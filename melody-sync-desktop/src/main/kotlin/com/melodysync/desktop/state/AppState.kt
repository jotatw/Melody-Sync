package com.melodysync.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.DuplicateGroup
import com.melodysync.model.HealthReport
import com.melodysync.model.LibraryStatistics
import com.melodysync.model.Song
import com.melodysync.scanner.calculateStatistics
import com.melodysync.service.DuplicateDetectionService
import com.melodysync.service.LibraryHealthService
import com.melodysync.service.LibrarySyncService
import com.melodysync.service.LibraryWatcher
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

enum class HealthStatus {
    IDLE,
    RUNNING,
    DONE,
    ERROR,
}

enum class DuplicatesStatus {
    IDLE,
    RUNNING,
    DONE,
    ERROR,
}

enum class WatchStatus {
    STOPPED,
    WATCHING,
    ERROR,
}

class AppState(private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {

    private var watcher: LibraryWatcher? = null

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

    var healthStatus by mutableStateOf(HealthStatus.IDLE)
        private set

    var healthReport by mutableStateOf<HealthReport?>(null)
        private set

    var duplicatesStatus by mutableStateOf(DuplicatesStatus.IDLE)
        private set

    var duplicateGroups by mutableStateOf<List<DuplicateGroup>>(emptyList())
        private set

    var watchStatus by mutableStateOf(WatchStatus.STOPPED)
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

    fun analyzeHealth() {
        if (healthStatus == HealthStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        scope.launch {
            healthStatus = HealthStatus.RUNNING
            try {
                MusicDatabase.connect()
                healthReport = LibraryHealthService.analyze(dir)
                healthStatus = HealthStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Health check failed"
                healthStatus = HealthStatus.ERROR
            }
        }
    }

    fun detectDuplicates() {
        if (duplicatesStatus == DuplicatesStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        scope.launch {
            duplicatesStatus = DuplicatesStatus.RUNNING
            try {
                MusicDatabase.connect()
                val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
                duplicateGroups = DuplicateDetectionService.detectDuplicates(songs)
                duplicatesStatus = DuplicatesStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Duplicate detection failed"
                duplicatesStatus = DuplicatesStatus.ERROR
            }
        }
    }

    fun startWatching() {
        if (watchStatus == WatchStatus.WATCHING) return
        if (directory.isBlank()) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        try {
            val newWatcher = LibraryWatcher(scope)
            newWatcher.start(dir) { _ ->
                resyncFromWatch()
            }
            watcher = newWatcher
            watchStatus = WatchStatus.WATCHING
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to start watching"
            watchStatus = WatchStatus.ERROR
        }
    }

    fun stopWatching() {
        watcher?.stop()
        watcher = null
        watchStatus = WatchStatus.STOPPED
    }

    private fun resyncFromWatch() {
        if (directory.isBlank()) return
        scope.launch {
            try {
                MusicDatabase.connect()
                val dir = Path.of(directory.trim())
                val result = LibrarySyncService.syncDirectory(dir)
                lastResult = result
                songs = MusicRepository.findAll()
                statistics = calculateStatistics(songs)
                progressText = "Auto-sync: +${result.added} added, ${result.updated} updated, ${result.removed} removed"
            } catch (e: Exception) {
                errorMessage = e.message ?: "Auto-sync failed"
            }
        }
    }
}
