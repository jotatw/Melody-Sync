package com.melodysync.service

import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class DuplicateDetectionServiceTest {

    private fun song(
        name: String,
        title: String? = name,
        artist: String? = "Artist",
        duration: Double? = 200.0,
        size: Long = 1000L,
    ): Song = Song(
        path = Path.of("/music/$name"),
        size = size,
        title = title,
        artist = artist,
        duration = duration,
    )

    @Test
    fun `detects exact duplicates`() {
        val songs = listOf(
            song("a.mp3", "Song", "Artist", 200.0),
            song("b.mp3", "Song", "Artist", 200.0),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertEquals(1, groups.size)
        assertEquals(2, groups[0].songs.size)
    }

    @Test
    fun `ignores unique songs`() {
        val songs = listOf(
            song("a.mp3", "Song A", "Artist", 200.0),
            song("b.mp3", "Song B", "Artist", 200.0),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `ignores songs without metadata`() {
        val songs = listOf(
            Song(path = Path.of("/music/a.mp3"), size = 100L),
            Song(path = Path.of("/music/b.mp3"), size = 100L),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `groups similar durations within tolerance`() {
        val songs = listOf(
            song("a.mp3", "Song", "Artist", 200.0),
            song("b.mp3", "Song", "Artist", 201.5),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertEquals(1, groups.size)
        assertEquals(2, groups[0].songs.size)
    }

    @Test
    fun `separates very different durations`() {
        val songs = listOf(
            song("a.mp3", "Song", "Artist", 200.0),
            song("b.mp3", "Song", "Artist", 300.0),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertTrue(groups.isEmpty())
    }

    @Test
    fun `normalizes title and artist case and whitespace`() {
        val songs = listOf(
            song("a.mp3", "  Song  One ", "  Artist  ", 200.0),
            song("b.mp3", "song one", "artist", 200.0),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertEquals(1, groups.size)
    }

    @Test
    fun `detects duplicates among many songs`() {
        val songs = listOf(
            song("a.mp3", "Song A", "Artist", 200.0),
            song("b.mp3", "Song A", "Artist", 200.0),
            song("c.mp3", "Song B", "Artist", 250.0),
            song("d.mp3", "Song B", "Artist", 250.5),
            song("e.mp3", "Song C", "Artist", 300.0),
        )

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        assertEquals(2, groups.size)
    }
}