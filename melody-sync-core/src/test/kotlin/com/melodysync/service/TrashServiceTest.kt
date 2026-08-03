package com.melodysync.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class TrashServiceTest {

    @TempDir
    lateinit var tmp: Path

    private fun trash(): Path = tmp.resolve("Trash")

    @Test
    fun `moves file into trash files dir and writes trashinfo`() {
        val file = tmp.resolve("song.mp3").also { Files.writeString(it, "data") }

        val moved = TrashService.moveToTrash(file, trash())

        assertEquals(trash().resolve("files/song.mp3"), moved)
        assertTrue(Files.exists(trash().resolve("files/song.mp3")))
        assertTrue(Files.exists(trash().resolve("info/song.mp3.trashinfo")))
        assertFalse(Files.exists(file))
    }

    @Test
    fun `trashinfo encodes the original path`() {
        val file = tmp.resolve("a b.mp3").also { Files.writeString(it, "data") }

        TrashService.moveToTrash(file, trash())

        val info = Files.readString(trash().resolve("info/a b.mp3.trashinfo"))
        assertTrue(info.contains("[Trash Info]"))
        assertTrue(info.contains("Path="))
        assertTrue(info.contains("%20"))
        assertTrue(info.contains("DeletionDate="))
    }

    @Test
    fun `avoids name collision in trash`() {
        val filesDir = trash().resolve("files")
        Files.createDirectories(filesDir)
        Files.writeString(filesDir.resolve("song.mp3"), "occupied")

        val file = tmp.resolve("song.mp3").also { Files.writeString(it, "data") }
        val moved = TrashService.moveToTrash(file, trash())

        assertEquals("song.1.mp3", moved.fileName.toString())
        assertTrue(Files.exists(filesDir.resolve("song.1.mp3")))
    }

    @Test
    fun `throws for missing file`() {
        assertThrows<IOException> {
            TrashService.moveToTrash(tmp.resolve("nope.mp3"), trash())
        }
    }

    @Test
    fun `throws for directory`() {
        val dir = Files.createDirectory(tmp.resolve("folder"))
        assertThrows<IOException> {
            TrashService.moveToTrash(dir, trash())
        }
    }
}
