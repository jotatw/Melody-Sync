package com.melodysync.desktop.state

import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import com.melodysync.scanner.readMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Headless integration test of the Apply flow (Phase C): a temp database and
 * a copied fixture are injected into AppState; applying a fix must update the
 * file, the database cache and the in-memory library.
 */
class ApplyIntegrationTest {

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

    @Test
    fun `apply writes tags, updates the database and the in-memory library`() {
        val dbFile = tmp.resolve("library.db")
        val prefsFile = tmp.resolve("settings.properties")
        val audioDir = Files.createDirectory(tmp.resolve("music"))
        val songFile = audioDir.resolve("song.mp3")
        Files.copy(
            Path.of(javaClass.getResource("/fixtures/audio/no_tags.mp3")!!.toURI()),
            songFile,
        )

        MusicDatabase.connectToFile(dbFile)
        MusicRepository.insert(
            Song(path = songFile, size = Files.size(songFile)),
        )

        val appState = state(dbFile, prefsFile)
        appState.updateDirectory(audioDir.toString())
        appState.loadLibraryFromDatabase()

        await { appState.songs.isNotEmpty() }
        val loaded = appState.songs.first()

        appState.applyQuickFix(loaded, TagSuggestion(title = "Applied Title", artist = "Applied Artist"))

        await { appState.songs.firstOrNull()?.title == "Applied Title" }
        assertEquals("Applied Artist", appState.songs.first().artist)

        MusicDatabase.connectToFile(dbFile)
        val fromDb = MusicRepository.findAll().first()
        assertEquals("Applied Title", fromDb.title)
        assertEquals("Applied Artist", fromDb.artist)

        val onDisk = readMetadata(Song(path = songFile, size = Files.size(songFile)))
        assertEquals("Applied Title", onDisk.title)
        assertEquals("Applied Artist", onDisk.artist)
    }
}
