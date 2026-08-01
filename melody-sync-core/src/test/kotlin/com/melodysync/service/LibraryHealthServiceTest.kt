package com.melodysync.service

import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.database.SongsTable
import com.melodysync.model.Song
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class LibraryHealthServiceTest {

    @TempDir
    lateinit var tmpDir: Path

    @TempDir
    lateinit var dbDir: Path

    @BeforeEach
    fun setUp() {
        MusicDatabase.connectToFile(dbDir.resolve("test.db"))
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(SongsTable)
        }
    }

    private fun copyAudioFixture(name: String, destName: String = name): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/with_tags.mp3")!!.toURI())
        val dest = tmpDir.resolve(destName)
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
        return dest
    }

    private fun createFile(name: String): Path {
        val file = tmpDir.resolve(name)
        Files.writeString(file, "test content")
        return file
    }

    @Test
    fun `classifies non-audio files by category`() {
        copyAudioFixture("with_tags.mp3", "song1.mp3")
        createFile("cover.png")
        createFile("sub.vtt")
        createFile("lyrics.lrc")
        createFile("meta.nfo")
        createFile("video.mp4")

        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(6, report.totalFiles)
        assertEquals(1, report.audioFiles)
        assertEquals(5, report.totalNonAudio)

        val categories = report.nonAudio.associateBy { it.category }
        assertEquals(1, categories["image"]?.count)
        assertEquals(1, categories["subtitle"]?.count)
        assertEquals(1, categories["lyrics"]?.count)
        assertEquals(1, categories["metadata"]?.count)
        assertEquals(1, categories["video"]?.count)
    }

    @Test
    fun `detects unknown extensions`() {
        createFile("mystery.xyz")
        createFile("data.zzz")

        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(listOf("xyz", "zzz"), report.unknownExtensions)
    }

    @Test
    fun `detects songs without metadata`() {
        MusicRepository.insert(Song(path = tmpDir.resolve("song1.mp3"), size = 100L))
        MusicRepository.insert(Song(path = tmpDir.resolve("song2.mp3"), size = 200L, title = "T", artist = "A"))

        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(1, report.songsWithoutMetadata.size)
        assertEquals("song1.mp3", report.songsWithoutMetadata[0].filename)
    }

    @Test
    fun `detects songs with zero duration`() {
        MusicRepository.insert(Song(path = tmpDir.resolve("song1.mp3"), size = 100L, title = "T", artist = "A", duration = 0.0))
        MusicRepository.insert(Song(path = tmpDir.resolve("song2.mp3"), size = 200L, title = "T", artist = "A", duration = null))
        MusicRepository.insert(Song(path = tmpDir.resolve("song3.mp3"), size = 300L, title = "T", artist = "A", duration = 120.0))

        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(2, report.songsWithZeroDuration.size)
    }

    @Test
    fun `detects orphaned database entries`() {
        val missingPath = tmpDir.resolve("deleted.mp3")
        MusicRepository.insert(Song(path = missingPath, size = 100L))

        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(1, report.orphanedEntries.size)
        assertEquals(missingPath.toString(), report.orphanedEntries[0])
    }

    @Test
    fun `analyzeFromDatabase reports database state`() {
        createFile("cover.png")
        MusicRepository.insert(Song(path = tmpDir.resolve("song1.mp3"), size = 100L))

        val report = LibraryHealthService.analyzeFromDatabase(tmpDir)

        assertEquals(1, report.totalFiles)
        assertEquals(0, report.audioFiles)
        assertEquals(1, report.totalNonAudio)
        assertEquals(1, report.orphanedEntries.size)
    }

    @Test
    fun `empty directory produces empty report`() {
        val report = LibraryHealthService.analyze(tmpDir)

        assertEquals(0, report.totalFiles)
        assertEquals(0, report.audioFiles)
        assertTrue(report.songsWithoutMetadata.isEmpty())
        assertTrue(report.orphanedEntries.isEmpty())
    }
}