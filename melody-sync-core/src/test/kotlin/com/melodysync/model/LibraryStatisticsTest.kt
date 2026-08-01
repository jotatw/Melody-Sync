package com.melodysync.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LibraryStatisticsTest {
    private val sampleStatistics = LibraryStatistics(
        totalSongs = 120,
        uniqueArtists = 30,
        uniqueAlbums = 20,
        totalSize = 1024L * 1024 * 1024,
        formats = mapOf("mp3" to 80, "flac" to 40),
        totalDuration = 7200.0,
        averageBitrate = 320000.0,
    )

    private val emptyStatistics = LibraryStatistics(
        totalSongs = 0,
        uniqueArtists = 0,
        uniqueAlbums = 0,
        totalSize = 0,
        formats = emptyMap(),
        totalDuration = 0.0,
        averageBitrate = 0.0,
    )

    @Test
    fun `detects empty library`() {
        assertFalse(sampleStatistics.isEmpty)
        assertTrue(emptyStatistics.isEmpty)
    }

    @Test
    fun `calculates total size in MB`() {
        val expected = sampleStatistics.totalSize / (1024.0 * 1024.0)
        assertEquals(expected, sampleStatistics.totalSizeMb, 0.001)
    }

    @Test
    fun `calculates total size in GB`() {
        val expected = sampleStatistics.totalSize / (1024.0 * 1024.0 * 1024.0)
        assertEquals(expected, sampleStatistics.totalSizeGb, 0.001)
    }

    @Test
    fun `calculates total duration in minutes`() {
        val expected = sampleStatistics.totalDuration / 60.0
        assertEquals(expected, sampleStatistics.totalDurationMinutes, 0.001)
    }

    @Test
    fun `calculates total duration in hours`() {
        val expected = sampleStatistics.totalDuration / 3600.0
        assertEquals(expected, sampleStatistics.totalDurationHours, 0.001)
    }

    @Test
    fun `calculates average duration`() {
        val expected = sampleStatistics.totalDuration / sampleStatistics.totalSongs
        assertEquals(expected, sampleStatistics.averageDuration, 0.001)
        assertEquals(0.0, emptyStatistics.averageDuration, 0.001)
    }

    @Test
    fun `calculates average bitrate in kbps`() {
        assertEquals(320.0, sampleStatistics.averageBitrateKbps, 0.001)
    }
}