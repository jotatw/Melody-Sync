package com.melodysync.cli

import com.github.ajalt.clikt.testing.test
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
        assertTrue(result.stdout.contains("v0.3.0-dev"))
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