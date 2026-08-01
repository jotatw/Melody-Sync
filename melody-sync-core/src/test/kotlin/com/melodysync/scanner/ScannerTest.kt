package com.melodysync.scanner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class ScannerTest {
    @TempDir
    lateinit var tmpDir: Path

    private fun copyFixture(name: String, destName: String = name): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/$name")!!.toURI())
        val dest = tmpDir.resolve(destName)
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
        return dest
    }

    private fun copyFixtureTo(name: String, dest: Path): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/$name")!!.toURI())
        Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING)
        return dest
    }

    @Test
    fun `returns empty list for empty library`() {
        val result = scan(tmpDir)
        assertEquals(emptyList<com.melodysync.model.Song>(), result)
    }

    @Test
    fun `returns one song for single audio file`() {
        copyFixture("with_tags.mp3")
        val result = scan(tmpDir)
        assertEquals(1, result.size)
    }

    @Test
    fun `returns multiple songs`() {
        copyFixture("with_tags.mp3", "Queen.mp3")
        copyFixture("with_tags.mp3", "Pink Floyd.mp3")
        copyFixture("with_tags.mp3", "Bach.mp3")
        val result = scan(tmpDir)
        assertEquals(3, result.size)
    }

    @Test
    fun `preserves song path`() {
        val dest = copyFixture("with_tags.mp3", "Queen.mp3")
        val result = scan(tmpDir)
        assertEquals(1, result.size)
        assertEquals(dest, result[0].path)
    }

    @Test
    fun `loads song title`() {
        copyFixture("with_tags.mp3")
        val result = scan(tmpDir)
        assertEquals(1, result.size)
        assertEquals("qualque coisa", result[0].title)
    }

    @Test
    fun `uses filename when title is missing`() {
        copyFixture("no_tags.mp3")
        val result = scan(tmpDir)
        assertEquals(1, result.size)
        assertEquals("no_tags.mp3", result[0].title)
    }

    @Test
    fun `finds songs in nested directories`() {
        val rock = tmpDir.resolve("Rock").also { Files.createDirectory(it) }
        val pop = tmpDir.resolve("Pop").also { Files.createDirectory(it) }

        copyFixtureTo("with_tags.mp3", rock.resolve("Queen.mp3"))
        copyFixtureTo("with_tags.mp3", pop.resolve("Artist.mp3"))

        val result = scan(tmpDir)
        assertEquals(2, result.size)
    }
}