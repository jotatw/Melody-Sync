package com.melodysync.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.DuplicateGroup
import com.melodysync.model.HealthReport
import com.melodysync.model.LibraryStatistics
import com.melodysync.model.OrganizationReport
import com.melodysync.model.Song
import com.melodysync.scanner.calculateStatistics
import com.melodysync.service.DuplicateDetectionService
import com.melodysync.service.LibraryHealthService
import com.melodysync.service.LibraryOrganizationService
import com.melodysync.service.LibrarySyncService
import com.melodysync.service.LibraryWatcher
import com.melodysync.service.SyncResult
import com.melodysync.service.TrashService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Path

enum class Section {
    LIBRARY,
    STATISTICS,
    HEALTH,
    DUPLICATES,
    ORGANIZE,
    SETTINGS,
    ABOUT,
}

enum class SortColumn {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    FORMAT,
    BITRATE,
}

enum class SongColumn {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    FORMAT,
    BITRATE,
}

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

enum class OrganizeStatus {
    IDLE,
    RUNNING,
    DONE,
    ERROR,
}

class AppState(
    // State writes must happen on the Compose main thread to avoid
    // snapshot corruption when recomposition is concurrent (e.g. tab
    // switch + fullscreen during a scan).
    private val uiScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
    // Background scope for the file watcher loop (blocking WatchService).
    private val ioScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {

    private var watcher: LibraryWatcher? = null
    private val prefs = AppPreferences.load()

    var directory by mutableStateOf(prefs.directory)
        private set

    var currentSection by mutableStateOf(sectionFromString(prefs.section))
        private set

    var sortColumn by mutableStateOf(sortColumnFromString(prefs.sortColumn))
        private set

    var sortAscending by mutableStateOf(prefs.sortAscending)
        private set

    var sidebarExpanded by mutableStateOf(prefs.sidebarExpanded)
        private set

    var visibleColumns by mutableStateOf(parseColumns(prefs.visibleColumns))
        private set

    var artistFilter by mutableStateOf("")
        private set

    var formatFilter by mutableStateOf("")
        private set

    var transientMessage by mutableStateOf<String?>(null)
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

    var analytics by mutableStateOf<AnalyticsData?>(null)
        private set

    var query by mutableStateOf("")
        private set

    var selectedSongPath by mutableStateOf<String?>(null)
        private set

    var healthStatus by mutableStateOf(HealthStatus.IDLE)
        private set

    var healthReport by mutableStateOf<HealthReport?>(null)
        private set

    var duplicatesStatus by mutableStateOf(DuplicatesStatus.IDLE)
        private set

    var duplicateGroups by mutableStateOf<List<DuplicateGroup>>(emptyList())
        private set

    var duplicateTrashSelection by mutableStateOf<Set<String>>(emptySet())
        private set

    var duplicateTrashing by mutableStateOf(false)
        private set

    var watchStatus by mutableStateOf(WatchStatus.STOPPED)
        private set

    var organizeStatus by mutableStateOf(OrganizeStatus.IDLE)
        private set

    var organizationReport by mutableStateOf<OrganizationReport?>(null)
        private set

    val filteredSongs: List<Song>
        get() {
            val q = query.trim().lowercase()
            val artist = artistFilter.trim().lowercase()
            val format = formatFilter.trim().lowercase()

            val filtered = songs.filter { song ->
                val matchesQuery = q.isEmpty() ||
                    song.title?.lowercase()?.contains(q) == true ||
                    song.artist?.lowercase()?.contains(q) == true ||
                    song.album?.lowercase()?.contains(q) == true
                val matchesArtist = artist.isEmpty() ||
                    song.artist?.lowercase()?.contains(artist) == true
                val matchesFormat = format.isEmpty() || song.extension.lowercase() == format
                matchesQuery && matchesArtist && matchesFormat
            }
            val comparator = comparatorFor(sortColumn, sortAscending)
            return filtered.sortedWith(comparator)
        }

    fun updateDirectory(value: String) {
        directory = value
        savePrefs()
    }

    fun updateQuery(value: String) {
        query = value
    }

    fun selectSong(path: String?) {
        selectedSongPath = path
    }

    fun updateArtistFilter(value: String) {
        artistFilter = value
    }

    fun updateFormatFilter(value: String) {
        formatFilter = value
    }

    fun setSection(section: Section) {
        currentSection = section
        savePrefs()
    }

    fun toggleSort(column: SortColumn) {
        if (sortColumn == column) {
            sortAscending = !sortAscending
        } else {
            sortColumn = column
            sortAscending = true
        }
        savePrefs()
    }

    fun toggleColumn(column: SongColumn) {
        val updated = if (column in visibleColumns) {
            visibleColumns - column
        } else {
            visibleColumns + column
        }
        visibleColumns = updated
        savePrefs()
    }

    fun toggleSidebar() {
        sidebarExpanded = !sidebarExpanded
        savePrefs()
    }

    fun showMessage(message: String) {
        transientMessage = message
    }

    fun clearMessage() {
        transientMessage = null
    }

    fun scan() {
        if (status == ScanStatus.SCANNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            status = ScanStatus.SCANNING
            progressText = "Scanning..."
            try {
                val result = withContext(Dispatchers.Default) {
                    MusicDatabase.connect()
                    LibrarySyncService.syncDirectory(dir)
                }
                lastResult = result
                val found = withContext(Dispatchers.Default) {
                    MusicRepository.findAll()
                }
                songs = found
                statistics = calculateStatistics(found)
                analytics = computeAnalytics(found)
                progressText = "Library synchronized · ${found.size} songs analyzed"
                status = ScanStatus.DONE
                if (result.added > 0 || result.updated > 0) {
                    showMessage("Scan done: +${result.added} added, ${result.updated} updated")
                }
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

        uiScope.launch {
            healthStatus = HealthStatus.RUNNING
            try {
                healthReport = withContext(Dispatchers.Default) {
                    MusicDatabase.connect()
                    LibraryHealthService.analyze(dir)
                }
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
        duplicateTrashSelection = emptySet()

        uiScope.launch {
            duplicatesStatus = DuplicatesStatus.RUNNING
            try {
                val groups = withContext(Dispatchers.Default) {
                    MusicDatabase.connect()
                    val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
                    DuplicateDetectionService.detectDuplicates(songs)
                }
                duplicateGroups = groups
                duplicatesStatus = DuplicatesStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Duplicate detection failed"
                duplicatesStatus = DuplicatesStatus.ERROR
            }
        }
    }

    fun toggleDuplicateSelection(path: String) {
        duplicateTrashSelection = if (path in duplicateTrashSelection) {
            duplicateTrashSelection - path
        } else {
            duplicateTrashSelection + path
        }
    }

    fun trashSelectedDuplicates() {
        if (duplicateTrashing || duplicateTrashSelection.isEmpty()) return
        errorMessage = null

        uiScope.launch {
            duplicateTrashing = true
            try {
                val (moved, remainingSongs, newGroups) = withContext(Dispatchers.Default) {
                    val selected = duplicateTrashSelection
                    val movedList = selected.mapNotNull { path ->
                        try {
                            TrashService.moveToTrash(Path.of(path))
                            path
                        } catch (_: Exception) {
                            null
                        }
                    }
                    val remaining = songs.filterNot { it.path.toString() in movedList }
                    val groups = DuplicateDetectionService.detectDuplicates(remaining)
                    Triple(movedList, remaining, groups)
                }
                duplicateTrashSelection = emptySet()
                if (moved.isEmpty()) {
                    errorMessage = "Could not move the selected files to trash."
                } else {
                    songs = remainingSongs
                    duplicateGroups = newGroups
                    statistics = calculateStatistics(remainingSongs)
                    analytics = computeAnalytics(remainingSongs)
                    showMessage("Moved ${moved.size} file(s) to trash")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to move files to trash"
            } finally {
                duplicateTrashing = false
            }
        }
    }

    fun startWatching() {
        if (watchStatus == WatchStatus.WATCHING) return
        if (directory.isBlank()) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        try {
            val newWatcher = LibraryWatcher(ioScope)
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

    fun planOrganization() {
        if (organizeStatus == OrganizeStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            organizeStatus = OrganizeStatus.RUNNING
            try {
                val report = withContext(Dispatchers.Default) {
                    MusicDatabase.connect()
                    val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
                    LibraryOrganizationService.planOrganization(songs, dir)
                }
                organizationReport = report
                organizeStatus = OrganizeStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Organization failed"
                organizeStatus = OrganizeStatus.ERROR
            }
        }
    }

    fun savePrefs() {
        AppPreferences(
            directory = directory,
            section = currentSection.name.lowercase(),
            sortColumn = sortColumn.name.lowercase(),
            sortAscending = sortAscending,
            sidebarExpanded = sidebarExpanded,
            visibleColumns = visibleColumns.joinToString(",") { it.name.lowercase() },
        ).save()
    }

    private fun resyncFromWatch() {
        if (directory.isBlank()) return
        uiScope.launch {
            try {
                val dir = Path.of(directory.trim())
                val result = withContext(Dispatchers.Default) {
                    MusicDatabase.connect()
                    LibrarySyncService.syncDirectory(dir)
                }
                lastResult = result
                val found = withContext(Dispatchers.Default) {
                    MusicRepository.findAll()
                }
                songs = found
                statistics = calculateStatistics(found)
                analytics = computeAnalytics(found)
                progressText = "Auto-sync: +${result.added} added, ${result.updated} updated, ${result.removed} removed"
            } catch (e: Exception) {
                errorMessage = e.message ?: "Auto-sync failed"
            }
        }
    }

    private fun comparatorFor(column: SortColumn, ascending: Boolean): Comparator<Song> {
        val base: Comparator<Song> = when (column) {
            SortColumn.TITLE -> compareBy { it.title?.lowercase() ?: it.filename.lowercase() }
            SortColumn.ARTIST -> compareBy { it.artist?.lowercase() ?: "" }
            SortColumn.ALBUM -> compareBy { it.album?.lowercase() ?: "" }
            SortColumn.DURATION -> compareBy { it.duration ?: 0.0 }
            SortColumn.FORMAT -> compareBy { it.extension }
            SortColumn.BITRATE -> compareBy { it.bitrate ?: 0 }
        }
        return if (ascending) base else base.reversed()
    }

    companion object {
        fun sectionFromString(value: String): Section =
            try { Section.valueOf(value.uppercase()) } catch (_: Exception) { Section.LIBRARY }

        fun sortColumnFromString(value: String): SortColumn =
            try { SortColumn.valueOf(value.uppercase()) } catch (_: Exception) { SortColumn.TITLE }

        fun parseColumns(value: String): Set<SongColumn> {
            if (value.isBlank()) return SongColumn.entries.toSet()
            return value.split(",").mapNotNull { raw ->
                try { SongColumn.valueOf(raw.trim().uppercase()) } catch (_: Exception) { null }
            }.toSet()
        }
    }
}