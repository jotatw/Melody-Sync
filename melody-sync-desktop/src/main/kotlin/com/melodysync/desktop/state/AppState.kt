package com.melodysync.desktop.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.melodysync.database.DatabaseConnection
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
import com.melodysync.desktop.theme.AppTheme
import com.melodysync.scanner.calculateStatistics
import com.melodysync.service.DuplicateDetectionService
import com.melodysync.service.FixSuggestion
import com.melodysync.service.LibraryHealthService
import com.melodysync.service.LibraryOrganizationService
import com.melodysync.service.LibrarySyncService
import com.melodysync.service.LibraryWatcher
import com.melodysync.service.LyricsService
import com.melodysync.service.QuickFixService
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

enum class SongField {
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    FORMAT,
    BITRATE,
}

enum class TaskStatus {
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

enum class UpdateStatus {
    IDLE,
    CHECKING,
    RUNNING,
    DONE,
    ERROR,
}

/**
 * Multi-song Health navigation context: Library shows only the affected songs.
 * A single affected song uses a selection context instead (see [AppState.reviewIssue]).
 */
data class IssueContext(
    val label: String,
    val paths: Set<String>,
)

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
            DatabaseConnection.connectToFile(db)
        } else {
            DatabaseConnection.connect()
        }
    }

    var directory by mutableStateOf(prefs.directory)
        private set

    var themeMode by mutableStateOf(prefs.theme)
        private set

    val theme: AppTheme
        get() = when (themeMode) {
            "light" -> AppTheme.LIGHT
            "dark" -> AppTheme.DARK
            else -> AppTheme.detectSystemTheme()
        }

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

    var albumFilter by mutableStateOf("")
        private set

    /** Multi-song Health context: shows only the affected songs in Library. */
    var issueContext by mutableStateOf<IssueContext?>(null)
        private set

    var transientMessage by mutableStateOf<String?>(null)
        private set

    var status by mutableStateOf(TaskStatus.IDLE)
        private set

    var progressText by mutableStateOf("")
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

    var healthStatus by mutableStateOf(TaskStatus.IDLE)
        private set

    var healthReport by mutableStateOf<HealthReport?>(null)
        private set

    var duplicatesStatus by mutableStateOf(TaskStatus.IDLE)
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

    var autoUpdate by mutableStateOf(prefs.autoUpdate)
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

    var lyrics by mutableStateOf<String?>(null)
        private set

    var lyricsLoading by mutableStateOf(false)
        private set

    var lyricsLoaded by mutableStateOf(false)
        private set

    val youtubeEnabled: Boolean
        get() = youtubeApiKey.isNotBlank()

    var installationInfo by mutableStateOf<InstallationInfo?>(null)
        private set

    var watchStatus by mutableStateOf(WatchStatus.STOPPED)
        private set

    var organizeStatus by mutableStateOf(TaskStatus.IDLE)
        private set

    var organizationReport by mutableStateOf<OrganizationReport?>(null)
        private set

    val filteredSongs: List<Song>
        get() {
            val q = query.trim().lowercase()
            val artist = artistFilter.trim().lowercase()
            val format = formatFilter.trim().lowercase()
            val album = albumFilter.trim().lowercase()

            val filtered = songs.filter { song ->
                val matchesQuery = q.isEmpty() ||
                    song.title?.lowercase()?.contains(q) == true ||
                    song.artist?.lowercase()?.contains(q) == true ||
                    song.album?.lowercase()?.contains(q) == true
                val matchesArtist = artist.isEmpty() ||
                    song.artist?.lowercase()?.contains(artist) == true
                val matchesFormat = format.isEmpty() || song.extension.lowercase() == format
                val matchesAlbum = album.isEmpty() ||
                    song.album?.lowercase()?.contains(album) == true
                val matchesIssue = issueContext?.let { song.path.toString() in it.paths } ?: true
                matchesQuery && matchesArtist && matchesFormat && matchesAlbum && matchesIssue
            }
            val comparator = comparatorFor(sortColumn, sortAscending)
            return filtered.sortedWith(comparator)
        }

    fun updateDirectory(value: String) {
        directory = value
        savePrefs()
    }

    fun selectThemeMode(mode: String) {
        themeMode = mode
        savePrefs()
    }

    fun toggleTheme() {
        themeMode = if (theme == AppTheme.LIGHT) "dark" else "light"
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
     * Health → Library contextual navigation (Block 01).
     *
     * A single affected song is a **selection** context: the song is selected
     * and scrolled into view. Multiple affected songs are a **filter** context:
     * Library shows only those songs, without implying a multi-selection that
     * the application does not support.
     */
    fun reviewIssue(paths: List<String>, label: String? = null) {
        if (paths.isEmpty()) return
        setSection(Section.LIBRARY)
        if (paths.size == 1) {
            issueContext = null
            selectSong(paths.first())
            pendingScrollPath = paths.first()
        } else {
            issueContext = IssueContext(label = label ?: "Issue", paths = paths.toSet())
            selectSong(null)
            pendingScrollPath = null
        }
    }

    fun clearIssueContext() {
        issueContext = null
    }

    /** Statistics → Library: open Library filtered by the selected artist. */
    fun exploreArtist(artist: String) {
        setSection(Section.LIBRARY)
        artistFilter = artist
    }

    /** Statistics → Library: open Library filtered by the selected format. */
    fun exploreFormat(format: String) {
        setSection(Section.LIBRARY)
        formatFilter = format
    }

    /** Statistics → Library: open Library filtered by the selected album. */
    fun exploreAlbum(album: String) {
        setSection(Section.LIBRARY)
        albumFilter = album
    }

    fun clearQuickFixYoutube() {
        quickFixYoutubeSuggestions = emptyList()
        quickFixYoutubeLoading = false
        quickFixYoutubeLoaded = false
    }

    fun clearLyrics() {
        lyrics = null
        lyricsLoading = false
        lyricsLoaded = false
    }

    fun loadLyrics(song: Song) {
        if (lyricsLoading) return
        lyricsLoading = true
        uiScope.launch {
            try {
                lyrics = withContext(Dispatchers.Default) { LyricsService.fetch(song) }
                lyricsLoaded = true
            } finally {
                lyricsLoading = false
            }
        }
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

    fun updateAlbumFilter(value: String) {
        albumFilter = value
    }

    fun setSection(section: Section) {
        currentSection = section
        savePrefs()
    }

    fun toggleSort(column: SongField) {
        if (sortColumn == column) {
            sortAscending = !sortAscending
        } else {
            sortColumn = column
            sortAscending = true
        }
        savePrefs()
    }

    fun toggleColumn(column: SongField) {
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

    fun setAutoUpdateEnabled(enabled: Boolean) {
        autoUpdate = enabled
        savePrefs()
    }

    /**
     * Unattended updates: called once at startup. When enabled and running a
     * release install (not a source checkout), checks the selected channel and
     * installs a newer release automatically. The new build takes effect on the
     * next launch.
     */
    fun autoUpdateIfEnabled() {
        if (!autoUpdate) return
        if (updateStatus == UpdateStatus.RUNNING || updateStatus == UpdateStatus.CHECKING) return

        val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
        if (InstallationValidator().isSourceCheckout(projectDir)) return

        uiScope.launch {
            val service = InstallationService()
            try {
                val check = withContext(Dispatchers.Default) {
                    service.checkForReleaseUpdate(channel = updateChannel)
                }
                if (check.updateAvailable && check.availableVersion != null) {
                    runUpdate(force = false)
                }
            } catch (_: Exception) {
                // offline or transient failure: skip silently, manual update remains available
            }
        }
    }

    fun showMessage(message: String) {
        transientMessage = message
    }

    fun clearMessage() {
        transientMessage = null
    }

    fun scan() {
        if (status == TaskStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            status = TaskStatus.RUNNING
            progressText = "Scanning..."
            try {
                val result = withContext(Dispatchers.Default) {
                    connectDatabase()
                    LibrarySyncService.syncDirectory(dir)
                }
                val found = withContext(Dispatchers.Default) {
                    MusicRepository.findAll()
                }
                songs = found
                statistics = calculateStatistics(found)
                analytics = computeAnalytics(found)
                refreshReview()
                progressText = "Library synchronized · ${found.size} songs analyzed"
                status = TaskStatus.DONE
                if (result.added > 0 || result.updated > 0) {
                    showMessage("Scan done: +${result.added} added, ${result.updated} updated")
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Scan failed"
                progressText = ""
                status = TaskStatus.ERROR
            }
        }
    }

    /**
     * Loads the previously scanned library for the current directory from
     * the database, so the app starts ready without a full rescan.
     * A manual "Rescan" re-syncs with the filesystem.
     */
    fun loadLibraryFromDatabase() {
        if (status == TaskStatus.RUNNING) return
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
                    status = TaskStatus.DONE
                } else {
                    status = TaskStatus.IDLE
                    progressText = ""
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load library from database"
                status = TaskStatus.IDLE
            }
        }
    }

    fun analyzeHealth() {
        if (healthStatus == TaskStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            healthStatus = TaskStatus.RUNNING
            try {
                healthReport = withContext(Dispatchers.Default) {
                    connectDatabase()
                    LibraryHealthService.analyze(dir)
                }
                healthStatus = TaskStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Health check failed"
                healthStatus = TaskStatus.ERROR
            }
        }
    }

    fun detectDuplicates() {
        if (duplicatesStatus == TaskStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null
        duplicateTrashSelection = emptySet()

        uiScope.launch {
            duplicatesStatus = TaskStatus.RUNNING
            try {
                val groups = withContext(Dispatchers.Default) {
                    connectDatabase()
                    val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
                    DuplicateDetectionService.detectDuplicates(songs)
                }
                duplicateGroups = groups
                duplicatesStatus = TaskStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Duplicate detection failed"
                duplicatesStatus = TaskStatus.ERROR
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
        if (organizeStatus == TaskStatus.RUNNING) return
        val dir = Path.of(directory.trim())
        errorMessage = null

        uiScope.launch {
            organizeStatus = TaskStatus.RUNNING
            try {
                val report = withContext(Dispatchers.Default) {
                    connectDatabase()
                    val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
                    LibraryOrganizationService.planOrganization(songs, dir)
                }
                organizationReport = report
                organizeStatus = TaskStatus.DONE
            } catch (e: Exception) {
                errorMessage = e.message ?: "Organization failed"
                organizeStatus = TaskStatus.ERROR
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
            autoUpdate = autoUpdate,
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

    private fun comparatorFor(column: SongField, ascending: Boolean): Comparator<Song> {
        val base: Comparator<Song> = when (column) {
            SongField.TITLE -> compareBy { it.title?.lowercase() ?: it.filename.lowercase() }
            SongField.ARTIST -> compareBy { it.artist?.lowercase() ?: "" }
            SongField.ALBUM -> compareBy { it.album?.lowercase() ?: "" }
            SongField.DURATION -> compareBy { it.duration ?: 0.0 }
            SongField.FORMAT -> compareBy { it.extension }
            SongField.BITRATE -> compareBy { it.bitrate ?: 0 }
        }
        return if (ascending) base else base.reversed()
    }

    companion object {
        fun sectionFromString(value: String): Section =
            try { Section.valueOf(value.uppercase()) } catch (_: Exception) { Section.LIBRARY }

        fun sortColumnFromString(value: String): SongField =
            try { SongField.valueOf(value.uppercase()) } catch (_: Exception) { SongField.TITLE }

        fun parseColumns(value: String): Set<SongField> {
            if (value.isBlank()) return SongField.entries.toSet()
            return value.split(",").mapNotNull { raw ->
                try { SongField.valueOf(raw.trim().uppercase()) } catch (_: Exception) { null }
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