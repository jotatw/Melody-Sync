package com.melodysync.desktop.state

import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Block 01 — Contextual Navigation: filter vs selection contexts for
 * Health → Library and Statistics → Library.
 */
class NavigationContextTest {

    @TempDir
    lateinit var tmp: Path

    private fun await(block: () -> Boolean) {
        var tries = 0
        while (!block() && tries++ < 400) {
            Thread.sleep(50)
        }
        assertTrue(block(), "condition not met in time")
    }

    private fun state(dbFile: Path, prefsFile: Path) = AppState(
        uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        databaseFile = dbFile,
        prefsFile = prefsFile,
    )

    private fun seed(dbFile: Path, audioDir: Path) {
        MusicDatabase.connectToFile(dbFile)
        MusicRepository.insert(Song(path = audioDir.resolve("a.mp3"), size = 1, artist = "Alpha", album = "One"))
        MusicRepository.insert(Song(path = audioDir.resolve("b.flac"), size = 1, artist = "Alpha", album = "Two"))
        MusicRepository.insert(Song(path = audioDir.resolve("c.mp3"), size = 1, artist = "Beta", album = "One"))
    }

    @Test
    fun `statistics exploreArtist opens Library filtered by artist`() {
        val dbFile = tmp.resolve("db.db")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        seed(dbFile, audioDir)

        val appState = state(dbFile, tmp.resolve("prefs.properties"))
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()
        await { appState.songs.size == 3 }

        appState.exploreArtist("Alpha")

        assertEquals(Section.LIBRARY, appState.currentSection)
        assertEquals("Alpha", appState.artistFilter)
        assertEquals(setOf("a.mp3", "b.flac"), appState.filteredSongs.map { it.filename }.toSet())
    }

    @Test
    fun `statistics exploreFormat opens Library filtered by format`() {
        val dbFile = tmp.resolve("db.db")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        seed(dbFile, audioDir)

        val appState = state(dbFile, tmp.resolve("prefs.properties"))
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()
        await { appState.songs.size == 3 }

        appState.exploreFormat("mp3")

        assertEquals(Section.LIBRARY, appState.currentSection)
        assertEquals("mp3", appState.formatFilter)
        assertEquals(setOf("a.mp3", "c.mp3"), appState.filteredSongs.map { it.filename }.toSet())
    }

    @Test
    fun `statistics exploreAlbum opens Library filtered by album`() {
        val dbFile = tmp.resolve("db.db")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        seed(dbFile, audioDir)

        val appState = state(dbFile, tmp.resolve("prefs.properties"))
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()
        await { appState.songs.size == 3 }

        appState.exploreAlbum("One")

        assertEquals(Section.LIBRARY, appState.currentSection)
        assertEquals("One", appState.albumFilter)
        assertEquals(setOf("a.mp3", "c.mp3"), appState.filteredSongs.map { it.filename }.toSet())
    }

    @Test
    fun `single health issue uses selection context`() {
        val dbFile = tmp.resolve("db.db")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        seed(dbFile, audioDir)

        val appState = state(dbFile, tmp.resolve("prefs.properties"))
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()
        await { appState.songs.size == 3 }

        val path = audioDir.resolve("a.mp3").toString()
        appState.reviewIssue(listOf(path), "Without metadata")

        assertEquals(Section.LIBRARY, appState.currentSection)
        assertNull(appState.issueContext)
        assertEquals(path, appState.selectedSongPath)
    }

    @Test
    fun `multi song health issue uses filter context`() {
        val dbFile = tmp.resolve("db.db")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        seed(dbFile, audioDir)

        val appState = state(dbFile, tmp.resolve("prefs.properties"))
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()
        await { appState.songs.size == 3 }

        val a = audioDir.resolve("a.mp3").toString()
        val b = audioDir.resolve("b.flac").toString()
        appState.reviewIssue(listOf(a, b), "Zero duration")

        assertEquals(Section.LIBRARY, appState.currentSection)
        assertNull(appState.selectedSongPath)
        assertEquals("Zero duration", appState.issueContext?.label)
        assertEquals(setOf(a, b), appState.issueContext?.paths)
        assertEquals(2, appState.filteredSongs.size)

        appState.clearIssueContext()
        assertNull(appState.issueContext)
        assertEquals(3, appState.filteredSongs.size)
    }
}
