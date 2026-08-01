package com.melodysync.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class SongTest {
    private val sampleSong = Song(
        path = Path.of("/music", "Queen - Bohemian Rhapsody.mp3"),
        size = 12_500_000L,
        title = "Bohemian Rhapsody",
        artist = "Queen",
        album = "A Night at the Opera",
        duration = 354.0,
        bitrate = 320000,
    )

    private val losslessSong = Song(
        path = Path.of("/music", "Pink Floyd - Time.flac"),
        size = 45_000_000L,
        title = "Time",
        artist = "Pink Floyd",
        album = "The Dark Side of the Moon",
        duration = 412.0,
        bitrate = 1_000_000,
    )

    private val emptySong = Song(
        path = Path.of("/music", "Unknown.mp3"),
        size = 0L,
    )

    @Test
    fun `returns correct filename`() {
        assertEquals("Queen - Bohemian Rhapsody.mp3", sampleSong.filename)
    }

    @Test
    fun `returns correct extension`() {
        assertEquals("mp3", sampleSong.extension)
    }

    @Test
    fun `returns correct directory`() {
        assertEquals(Path.of("/music"), sampleSong.directory)
    }

    @Test
    fun `detects metadata presence`() {
        assertTrue(sampleSong.hasMetadata)
        assertFalse(emptySong.hasMetadata)
    }

    @Test
    fun `calculates duration in minutes`() {
        val expected = sampleSong.duration!! / 60.0
        assertEquals(expected, sampleSong.durationMinutes, 0.001)
    }

    @Test
    fun `calculates size in MB`() {
        val expected = sampleSong.size / (1024.0 * 1024.0)
        assertEquals(expected, sampleSong.sizeMb, 0.001)
    }

    @Test
    fun `calculates size in GB`() {
        val expected = sampleSong.size / (1024.0 * 1024.0 * 1024.0)
        assertEquals(expected, sampleSong.sizeGb, 0.001)
    }

    @Test
    fun `identifies lossless audio`() {
        assertFalse(sampleSong.isLossless)
        assertTrue(losslessSong.isLossless)
    }
}