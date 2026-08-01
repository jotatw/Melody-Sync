package com.melodysync.scanner

import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

class StatisticsTest {
    @Test
    fun `returns empty statistics for empty library`() {
        val stats = calculateStatistics(emptyList())

        assertEquals(0, stats.totalSongs)
        assertEquals(0, stats.uniqueArtists)
        assertEquals(0, stats.uniqueAlbums)
        assertEquals(0L, stats.totalSize)
        assertEquals(0.0, stats.totalDuration)
        assertEquals(0.0, stats.averageBitrate)
        assertEquals(emptyMap<String, Int>(), stats.formats)
    }

    @Test
    fun `counts total songs`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L),
            Song(path = Path.of("song2.mp3"), size = 200L),
            Song(path = Path.of("song3.mp3"), size = 300L),
        )

        assertEquals(3, calculateStatistics(songs).totalSongs)
    }

    @Test
    fun `counts unique artists`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L, artist = "Queen"),
            Song(path = Path.of("song2.mp3"), size = 200L, artist = "Queen"),
            Song(path = Path.of("song3.mp3"), size = 300L, artist = "Pink Floyd"),
        )

        assertEquals(2, calculateStatistics(songs).uniqueArtists)
    }

    @Test
    fun `counts unique albums`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L, album = "Album A"),
            Song(path = Path.of("song2.mp3"), size = 200L, album = "Album A"),
            Song(path = Path.of("song3.mp3"), size = 300L, album = "Album B"),
        )

        assertEquals(2, calculateStatistics(songs).uniqueAlbums)
    }

    @Test
    fun `calculates total size`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 10_000_000L),
            Song(path = Path.of("song2.mp3"), size = 20_000_000L),
            Song(path = Path.of("song3.mp3"), size = 30_000_000L),
        )

        assertEquals(60_000_000L, calculateStatistics(songs).totalSize)
    }

    @Test
    fun `calculates total duration`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L, duration = 180.0),
            Song(path = Path.of("song2.mp3"), size = 100L, duration = 240.0),
            Song(path = Path.of("song3.mp3"), size = 100L, duration = 120.0),
        )

        assertEquals(540.0, calculateStatistics(songs).totalDuration)
    }

    @Test
    fun `counts audio formats`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L),
            Song(path = Path.of("song2.mp3"), size = 100L),
            Song(path = Path.of("song3.flac"), size = 100L),
            Song(path = Path.of("song4.wav"), size = 100L),
        )

        assertEquals(
            mapOf("mp3" to 2, "flac" to 1, "wav" to 1),
            calculateStatistics(songs).formats,
        )
    }

    @Test
    fun `calculates average bitrate`() {
        val songs = listOf(
            Song(path = Path.of("song1.mp3"), size = 100L, bitrate = 320000),
            Song(path = Path.of("song2.mp3"), size = 200L, bitrate = 160000),
            Song(path = Path.of("song3.mp3"), size = 300L, bitrate = 128000),
        )

        val expected = (320000 + 160000 + 128000) / 3.0
        assertEquals(expected, calculateStatistics(songs).averageBitrate, 0.001)
    }
}