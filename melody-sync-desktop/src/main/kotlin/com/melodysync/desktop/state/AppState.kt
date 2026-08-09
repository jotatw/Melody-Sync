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
import com.melodysync.model.SongDiagnostics
import com.melodysync.model.TagSuggestion
import com.melodysync.platform.installation.InstallationChannel
import com.melodysync.platform.installation.InstallationInfo
import com.melodysync.platform.installation.InstallationService
import com.melodysync.platform.installation.InstallationValidator
import com.melodysync.platform.system.VersionInfo
import com.melodysync.scanner.calculateStatistics
import com.melodysync.service.DuplicateDetectionService
import com.melodysync.service.FixSuggestion
import com.melodysync.service.LibraryHealthService
import com.melodysync.service.LibraryOrganizationService
import com.melodysync.service.LibrarySyncService
import com.melodysync.service.LibraryWatcher
import com.melodysync.service.QuickFixService
import com.melodysync.service.SyncResult
import com.melodysync.service.TrashService
import com.melodysync.service.YoutubeFixSource
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
    REVIEW,
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

enum class UpdateStatus {
    IDLE,
    CHECKING,
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
    // Test seams: override the database/preferences location so the Apply
    // flow can be exercised headlessly against a temp database (Phase C).
    private val databaseFile: Path? = null,
    private val prefsFile: Path? = null,
) {

    private var watcher: LibraryWatcher? = null
    private val prefs = AppPreferences.load(prefsFile ?: AppPreferences.defaultFile())

    private fun connectDatabase() {
        val db = databaseFile
        if (db != null) {
            MusicDatabase.connectToFile(db)
        } else {
            MusicDatabase.connect()
        }
    }

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

    var groupByLetter by mutableStateOf(prefs.groupByLetter)
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

    // Review screen: songs with issues across the whole library.
    var reviewItems by mutableStateOf<List<SongDiagnostics>>(emptyList())
        private set

    var updateStatus by mutableStateOf(UpdateStatus.IDLE)
        private set

    var updatePhase by mutableStateOf("")
        private set

    var updateMessage by mutableStateOf<String?>(null)
        private set

    var updateAvailable by mutableStateOf(false)
        private set

    var updateSourceBased by mutableStateOf(false)
        private set

    var updateChannel by mutableStateOf(channelFromString(prefs.updateChannel))
        private set

    // Quick-Fix HUD (see docs/research/quick-fix-hud.md)
    // The key is read from YOUTUBE_API_KEY, falling back to
    // ~/.config/melody-sync/youtube-api-key (trimmed) so the installed
    // launcher can use it without shell env setup.
    private val youtubeApiKey: String =
        (System.getenv("YOUTUBE_API_KEY") ?: "")
            .ifBlank { readYoutubeKeyFile() }
            .trim()

    var quickFixYoutubeSuggestions by mutableStateOf<List<FixSuggestion>>(emptyList())
        private set

    var quickFixYoutubeLoading by mutableStateOf(false)
        private set

    var quickFixYoutubeLoaded by mutableStateOf(false)
        private set

    var quickFixApplying by mutableStateOf(false)
        private set

    val youtubeEnabled: Boolean
        get() = youtubeApiKey.isNotBlank()

    var installationInfo by mutableStateOf<InstallationInfo?>(null)
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

    /**
     * Recomputes the per-song diagnosis for the whole library (pure in-memory
     * checks — no file IO). Powers the Review screen.
     */
    fun refreshReview() {
        if (songs.isEmpty()) {
            reviewItems = emptyList()
            return
        }
        uiScope.launch {
            reviewItems = withContext(Dispatchers.Default) {
                songs.map { QuickFixService.diagnose(it) }.filter { it.hasIssues }
            }
        }
    }

    var pendingScrollPath by mutableStateOf<String?>(null)
        private set

    fun clearPendingScroll() {
        pendingScrollPath = null
    }

    /**
     * Health → Library: jump to the Library section and highlight (and
     * scroll to) the first affected song. Connects diagnosis with action.
     */
    fun reviewIssue(paths: List<String>) {
        val target = paths.firstOrNull() ?: return
        setSection(Section.LIBRARY)
        selectSong(target)
        pendingScrollPath = target
    }

    fun clearQuickFixYoutube() {
        quickFixYoutubeSuggestions = emptyList()
        quickFixYoutubeLoading = false
        quickFixYoutubeLoaded = false
    }

    fun loadYoutubeSuggestions(song: Song) {
        if (quickFixYoutubeLoading || youtubeApiKey.isBlank()) return
        quickFixYoutubeLoading = true
        uiScope.launch {
            try {
                quickFixYoutubeSuggestions = withContext(Dispatchers.Default) {
                    YoutubeFixSource(youtubeApiKey).suggest(song)
                }
                quickFixYoutubeLoaded = true
            } finally {
                quickFixYoutubeLoading = false
            }
        }
    }

    /**
     * Writes the suggested tags to the file, updates the database cache and
     * refreshes the in-memory library. The user validates every edit by
     * clicking Apply — nothing is applied automatically.
     */
    fun applyQuickFix(song: Song, suggestion: TagSuggestion) {
        if (quickFixApplying || !suggestion.hasChanges) return
        quickFixApplying = true
        uiScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    QuickFixService.apply(song, suggestion)
                }
                if (!result.success) {
                    showMessage("Cannot write tags to ${song.filename}: ${result.error?.userMessage ?: "unknown error"}")
                    return@launch
                }
                val updated = result.updated!!
                withContext(Dispatchers.Default) {
                    connectDatabase()
                    MusicRepository.updateByPath(updated)
                }
                songs = songs.map { if (it.path == updated.path) updated else it }
                statistics = calculateStatistics(songs)
                analytics = computeAnalytics(songs)
                refreshReview()
                showMessage("Tags updated · ${song.filename}")
            } catch (e: Exception) {
                showMessage("Apply failed: ${e.message ?: e::class.simpleName}")
            } finally {
                quickFixApplying = false
            }
        }
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

    fun toggleGroupByLetter() {
        groupByLetter = !groupByLetter
        savePrefs()
    }

    fun selectUpdateChannel(channel: InstallationChannel) {
        updateChannel = channel
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
                    connectDatabase()
                    LibrarySyncService.syncDirectory(dir)
                }
                lastResult = result
                val found = withContext(Dispatchers.Default) {
                    MusicRepository.findAll()
                }
                songs = found
                statistics = calculateStatistics(found)
                analytics = computeAnalytics(found)
                refreshReview()
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

    /**
     * Loads the previously scanned library for the current directory from
     * the database, so the app starts ready without a full rescan.
     * A manual "Rescan" re-syncs with the filesystem.
     */
    fun loadLibraryFromDatabase() {
        if (status == ScanStatus.SCANNING) return
        if (directory.isBlank()) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            try {
                val found = withContext(Dispatchers.Default) {
                    connectDatabase()
                    MusicRepository.findAll().filter { it.path.startsWith(dir) }
                }
                if (found.isNotEmpty()) {
                    songs = found
                    statistics = calculateStatistics(found)
                    analytics = computeAnalytics(found)
                    refreshReview()
                    progressText = "Loaded ${found.size} songs from database"
                    status = ScanStatus.DONE
                } else {
                    status = ScanStatus.IDLE
                    progressText = ""
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load library from database"
                status = ScanStatus.IDLE
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
                    connectDatabase()
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
                    connectDatabase()
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
                    refreshReview()
                    showMessage("Moved ${moved.size} file(s) to trash")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to move files to trash"
            } finally {
                duplicateTrashing = false
            }
        }
    }

    fun refreshInstallationInfo() {
        uiScope.launch {
            installationInfo = withContext(Dispatchers.Default) {
                InstallationService().detectInstallation()
            }
        }
    }

    fun checkForUpdates() {
        if (updateStatus == UpdateStatus.CHECKING || updateStatus == UpdateStatus.RUNNING) return
        updateStatus = UpdateStatus.CHECKING
        updatePhase = "Checking for updates…"
        updateMessage = null

        uiScope.launch {
            val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
            val service = InstallationService()
            val sourceCheckout = InstallationValidator().isSourceCheckout(projectDir)
            try {
                val result = withContext(Dispatchers.Default) {
                    if (sourceCheckout) {
                        service.checkForUpdate(projectDir)
                    } else {
                        service.checkForReleaseUpdate(channel = updateChannel)
                    }
                }
                val info = withContext(Dispatchers.Default) {
                    service.detectInstallation()
                }
                installationInfo = info
                updateSourceBased = sourceCheckout
                updateAvailable = result.updateAvailable
                updateMessage = when {
                    result.message != null -> result.message
                    result.updateAvailable -> {
                        val from = result.installedVersion?.let { "v$it" } ?: "nothing"
                        "Update available: $from → v${result.availableVersion}"
                    }
                    else -> "Already up to date (v${result.availableVersion})"
                }
                updateStatus = UpdateStatus.DONE
                updatePhase = "Done"
            } catch (e: Exception) {
                updateMessage = e.message ?: "Update check failed"
                updateStatus = UpdateStatus.ERROR
            }
        }
    }

    fun runUpdate(force: Boolean = true) {
        if (updateStatus == UpdateStatus.RUNNING) return
        updateStatus = UpdateStatus.RUNNING
        updatePhase = if (updateSourceBased) "Preparing build…" else "Preparing download…"
        updateMessage = null

        uiScope.launch {
            val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
            val service = InstallationService()
            try {
                val result = withContext(Dispatchers.Default) {
                    if (updateSourceBased) {
                        service.update(
                            projectDir = projectDir,
                            build = "Desktop",
                            force = force,
                            onProgress = { line ->
                                uiScope.launch { updatePhase = phaseFromLine(line) }
                            },
                        )
                    } else {
                        service.updateFromRelease(
                            channel = updateChannel,
                            build = "Desktop",
                            force = force,
                            onProgress = { line ->
                                uiScope.launch { updatePhase = phaseFromLine(line) }
                            },
                        )
                    }
                }
                installationInfo = service.detectInstallation()
                updateMessage = result.message
                updatePhase = if (result.installed) "Done" else "Failed"
                updateStatus = if (result.installed || result.sourceBased) {
                    UpdateStatus.DONE
                } else {
                    UpdateStatus.ERROR
                }
                if (result.installed) {
                    showMessage(result.message)
                }
            } catch (e: Exception) {
                updateMessage = e.message ?: "Update failed"
                updatePhase = "Failed"
                updateStatus = UpdateStatus.ERROR
            }
        }
    }

    private fun phaseFromLine(line: String): String = when {
        line.contains("Downloading", ignoreCase = true) -> "Downloading…"
        line.contains("Verifying", ignoreCase = true) -> "Verifying…"
        line.contains("Building", ignoreCase = true) -> "Compiling…"
        line.contains("Installing to", ignoreCase = true) -> "Installing…"
        line.contains("Installed!", ignoreCase = true) -> "Verifying…"
        else -> line.trim().ifBlank { "Working…" }
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
                    connectDatabase()
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
            groupByLetter = groupByLetter,
            updateChannel = updateChannel.name.lowercase(),
        ).save(prefsFile ?: AppPreferences.defaultFile())
    }

    private fun resyncFromWatch() {
        if (directory.isBlank()) return
        uiScope.launch {
            try {
                val dir = Path.of(directory.trim())
                val result = withContext(Dispatchers.Default) {
                    connectDatabase()
                    LibrarySyncService.syncDirectory(dir)
                }
                lastResult = result
                val found = withContext(Dispatchers.Default) {
                    MusicRepository.findAll()
                }
                songs = found
                statistics = calculateStatistics(found)
                analytics = computeAnalytics(found)
                refreshReview()
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

        fun channelFromString(value: String): InstallationChannel =
            try {
                InstallationChannel.valueOf(value.trim().uppercase())
            } catch (_: Exception) {
                InstallationChannel.STABLE
            }
    }
}

private fun readYoutubeKeyFile(): String {
    val home = System.getProperty("user.home") ?: "."
    val file = java.nio.file.Path.of(home, ".config", "melody-sync", "youtube-api-key")
    return try {
        if (java.nio.file.Files.exists(file)) java.nio.file.Files.readString(file).trim() else ""
    } catch (_: Exception) {
        ""
    }
}