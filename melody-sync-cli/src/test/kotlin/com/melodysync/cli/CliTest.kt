package com.melodysync.cli

import com.github.ajalt.clikt.testing.test
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.io.TempDir

class VersionCommandTest {
    @Test
    fun `prints version`() {
        val result = VersionCommand().test("")

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("v0.6.0-dev"))
    }
}

class ScanCommandTest {
    @TempDir
    lateinit var tmpDir: Path

    private fun createLibrary(): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/with_tags.mp3")!!.toURI())
        Files.copy(source, tmpDir.resolve("Queen.mp3"))
        Files.copy(source, tmpDir.resolve("Bach.mp3"))
        return tmpDir
    }

    @Test
    fun `scans empty directory`() {
        val result = ScanCommand().test(tmpDir.toString())

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Total songs:      0"))
    }

    @Test
    fun `scans directory with songs`() {
        createLibrary()
        val result = ScanCommand().test(tmpDir.toString())

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Total songs:      2"))
        assertTrue(result.stdout.contains(".mp3: 2"))
    }

    @Test
    fun `fails for missing directory`() {
        val missing = tmpDir.resolve("nonexistent").toString()
        val result = ScanCommand().test(missing)

        assertTrue(result.output.contains("Directory must exist"))
    }

    @Test
    fun `persists scan results to database`() {
        createLibrary()
        val db = tmpDir.resolve("test.db")

        val result = ScanCommand().test("--persist --db $db ${tmpDir}")

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Syncing to database"))
        assertTrue(result.stdout.contains("Database now holds 2 songs"))
        assertTrue(Files.exists(db))
    }
}

class HealthCommandTest {
    @TempDir
    lateinit var tmpDir: Path

    @TempDir
    lateinit var dbDir: Path

    private fun command(db: Path) = "--db $db"

    @Test
    fun `reports audio and non-audio files`() {
        val source = Path.of(javaClass.getResource("/fixtures/audio/with_tags.mp3")!!.toURI())
        Files.copy(source, tmpDir.resolve("song.mp3"))
        Files.writeString(tmpDir.resolve("cover.png"), "png")
        Files.writeString(tmpDir.resolve("sub.vtt"), "vtt")
        Files.writeString(tmpDir.resolve("mystery.xyz"), "xyz")

        val result = HealthCommand().test("${command(dbDir.resolve("h.db"))} ${tmpDir}")

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Library Health Report"))
        assertTrue(result.stdout.contains("1 audio"))
        assertTrue(result.stdout.contains("image"))
        assertTrue(result.stdout.contains("subtitle"))
        assertTrue(result.stdout.contains(".xyz"))
    }

    @Test
    fun `fails for missing directory`() {
        val missing = tmpDir.resolve("nonexistent").toString()
        val result = HealthCommand().test("${command(dbDir.resolve("h.db"))} $missing")

        assertTrue(result.output.contains("Directory must exist"))
    }

    @Test
    fun `reports empty directory as healthy`() {
        val result = HealthCommand().test("${command(dbDir.resolve("h.db"))} ${tmpDir}")

        assertEquals(0, result.statusCode)
        assertTrue(result.output.contains("0 total"))
    }
}

class DuplicatesCommandTest {
    @TempDir
    lateinit var tmpDir: Path

    @TempDir
    lateinit var dbDir: Path

    @Test
    fun `reports no duplicates for empty database`() {
        val db = dbDir.resolve("dup.db")
        val result = DuplicatesCommand().test("--db $db ${tmpDir}")

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Duplicate groups: 0"))
        assertTrue(result.stdout.contains("No duplicates found"))
    }

    @Test
    fun `reports duplicate groups from database`() {
        val db = dbDir.resolve("dup.db")
        MusicDatabase.connectToFile(db)
        MusicRepository.insert(
            Song(path = tmpDir.resolve("a.mp3"), size = 100L, title = "Song", artist = "Artist", duration = 200.0),
        )
        MusicRepository.insert(
            Song(path = tmpDir.resolve("b.mp3"), size = 100L, title = "Song", artist = "Artist", duration = 200.0),
        )

        val result = DuplicatesCommand().test("--db $db ${tmpDir}")

        assertEquals(0, result.statusCode)
        assertTrue(result.stdout.contains("Duplicate groups: 1"))
        assertTrue(result.stdout.contains("Artist — Song"))
    }

    @Test
    fun `fails for missing directory`() {
        val missing = tmpDir.resolve("nonexistent").toString()
        val result = DuplicatesCommand().test("--db ${dbDir.resolve("dup.db")} $missing")

        assertTrue(result.output.contains("Directory must exist"))
    }
}