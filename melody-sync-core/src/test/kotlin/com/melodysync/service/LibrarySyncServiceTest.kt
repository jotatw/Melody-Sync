package com.melodysync.service

import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.database.SongsTable
import com.melodysync.model.Song
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class LibrarySyncServiceTest {

    @TempDir
    lateinit var tmpDir: Path

    @BeforeEach
    fun setUp() {
        MusicDatabase.connectToFile(tmpDir.resolve("test.db"))
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(SongsTable)
        }
    }

    private fun copyFixture(name: String): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/with_tags.mp3")!!.toURI())
        val dest = tmpDir.resolve(name)
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
        return dest
    }

    @Test
    fun `syncs songs into empty database`() {
        copyFixture("Queen.mp3")
        copyFixture("Bach.mp3")

        val result = LibrarySyncService.syncDirectory(tmpDir)

        assertEquals(2, result.added)
        assertEquals(0, result.updated)
        assertEquals(0, result.removed)
        assertEquals(2, result.totalInDatabase)
    }

    @Test
    fun `syncs is idempotent`() {
        copyFixture("Queen.mp3")

        LibrarySyncService.syncDirectory(tmpDir)
        val second = LibrarySyncService.syncDirectory(tmpDir)

        assertEquals(0, second.added)
        assertEquals(1, second.updated)
        assertEquals(0, second.removed)
        assertEquals(1, second.totalInDatabase)
    }

    @Test
    fun `removes songs that no longer exist`() {
        copyFixture("Queen.mp3")
        copyFixture("Bach.mp3")

        LibrarySyncService.syncDirectory(tmpDir)
        Files.delete(tmpDir.resolve("Bach.mp3"))

        val result = LibrarySyncService.syncDirectory(tmpDir)

        assertEquals(0, result.added)
        assertEquals(1, result.updated)
        assertEquals(1, result.removed)
        assertEquals(1, result.totalInDatabase)
    }

    @Test
    fun `persists songs that can be read back`() {
        val copied = copyFixture("Queen.mp3")

        LibrarySyncService.syncDirectory(tmpDir)

        val stored = MusicRepository.findAll()
        assertEquals(1, stored.size)
        assertEquals(copied, stored[0].path)
        assertEquals("qualque coisa", stored[0].title)
    }

    @Test
    fun `syncs empty directory removes everything`() {
        copyFixture("Queen.mp3")
        LibrarySyncService.syncDirectory(tmpDir)

        val result = LibrarySyncService.syncDirectory(tmpDir.resolve("empty").also { Files.createDirectory(it) })

        assertEquals(1, result.removed)
        assertEquals(0, result.totalInDatabase)
    }

    @Test
    fun `syncSongs adds only new songs`() {
        val song = Song(
            path = Path.of("/music/test.mp3"),
            size = 100L,
            title = "Test Song",
            artist = "Artist",
        )

        val result = LibrarySyncService.syncSongs(listOf(song))

        assertEquals(1, result.added)
        assertEquals(1, result.totalInDatabase)
    }
}