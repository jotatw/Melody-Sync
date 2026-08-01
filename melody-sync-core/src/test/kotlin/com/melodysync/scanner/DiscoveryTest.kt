package com.melodysync.scanner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.nio.file.Path

class DiscoveryTest {
    @TempDir
    lateinit var tmpDir: Path

    @Test
    fun `finds single audio file`() {
        val file = tmpDir.resolve("Queen.mp3")
        file.toFile().createNewFile()

        val result = discover(tmpDir)

        assertEquals(1, result.size)
        assertEquals(file, result[0])
    }

    @Test
    fun `finds multiple audio files`() {
        val files = listOf(
            tmpDir.resolve("Queen.mp3"),
            tmpDir.resolve("Pink Floyd.flac"),
            tmpDir.resolve("Metallica.wav"),
        )
        files.forEach { it.toFile().createNewFile() }

        val result = discover(tmpDir)

        assertEquals(3, result.size)
        files.forEach { assertTrue(it in result) }
    }

    @Test
    fun `returns empty list for empty directory`() {
        val result = discover(tmpDir)
        assertEquals(emptyList<Path>(), result)
    }

    @Test
    fun `ignores unsupported files`() {
        val supported = listOf("Queen.mp3", "Pink Floyd.flac").map { tmpDir.resolve(it) }
        val unsupported = listOf("image.png", "notes.txt", "video.mp4", "document.pdf").map { tmpDir.resolve(it) }
        (supported + unsupported).forEach { it.toFile().createNewFile() }

        val result = discover(tmpDir)

        assertEquals(2, result.size)
        supported.forEach { assertTrue(it in result) }
        unsupported.forEach { assertTrue(it !in result) }
    }

    @Test
    fun `finds audio files in nested directories`() {
        (tmpDir.resolve("Queen.mp3")).toFile().createNewFile()

        val rock = tmpDir.resolve("Rock").also { it.toFile().mkdir() }
        (rock.resolve("Pink Floyd.flac")).toFile().createNewFile()
        (rock.resolve("Metallica.mp3")).toFile().createNewFile()

        val classical = tmpDir.resolve("Classical").also { it.toFile().mkdir() }
        (classical.resolve("Bach.wav")).toFile().createNewFile()

        val result = discover(tmpDir)

        assertEquals(4, result.size)
    }

    @Test
    fun `accepts uppercase extensions`() {
        val files = listOf("Queen.MP3", "Pink Floyd.FlAc", "Metallica.WAV").map { tmpDir.resolve(it) }
        files.forEach { it.toFile().createNewFile() }

        val result = discover(tmpDir)

        assertEquals(3, result.size)
        files.forEach { assertTrue(it in result) }
    }

    @Test
    fun `returns sorted results`() {
        val files = listOf("Zeta.mp3", "Alpha.flac", "Delta.wav", "Beta.mp3").map { tmpDir.resolve(it) }
        files.forEach { it.toFile().createNewFile() }

        val result = discover(tmpDir)

        assertEquals(files.sorted(), result)
    }

    @Test
    fun `raises exception for missing directory`() {
        val missing = tmpDir.resolve("Music")
        assertThrows<NoSuchFileException> { discover(missing) }
    }

    @Test
    fun `raises exception for file path`() {
        val filePath = tmpDir.resolve("Queen.mp3")
        filePath.toFile().createNewFile()

        assertThrows<NotDirectoryException> { discover(filePath) }
    }
}