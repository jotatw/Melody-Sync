package com.melodysync.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class DatabaseConnectionTest {

    @TempDir
    lateinit var tmp: Path

    @Test
    fun `connect is idempotent for the same file`() {
        val db = tmp.resolve("a.db")

        DatabaseConnection.connectToFile(db)
        DatabaseConnection.connectToFile(db)

        MusicRepository.insert(
            com.melodysync.model.Song(path = tmp.resolve("song.mp3"), size = 1L),
        )
        assertEquals(1, MusicRepository.count())
    }

    @Test
    fun `switching database files reconnects`() {
        val dbA = tmp.resolve("a.db")
        val dbB = tmp.resolve("b.db")

        DatabaseConnection.connectToFile(dbA)
        MusicRepository.insert(
            com.melodysync.model.Song(path = tmp.resolve("song.mp3"), size = 1L),
        )
        assertEquals(1, MusicRepository.count())

        DatabaseConnection.connectToFile(dbB)
        assertEquals(0, MusicRepository.count())
        assertTrue(Files.exists(dbB))
    }

    @Test
    fun `withWriteLock runs the block and returns its value`() {
        val value = DatabaseConnection.withWriteLock { 42 }
        assertEquals(42, value)
    }
}
