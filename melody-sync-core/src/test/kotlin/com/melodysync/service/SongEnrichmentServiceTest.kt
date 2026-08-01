package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.YouTubeVideoResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class SongEnrichmentServiceTest {

    private fun song(title: String? = null, artist: String? = null, filename: String = "song.mp3"): Song =
        Song(path = Path.of("/music/$filename"), size = 100L, title = title, artist = artist)

    @Test
    fun `builds query from artist and title`() {
        assertEquals("Queen Bohemian Rhapsody", SongEnrichmentService.buildQuery(song("Bohemian Rhapsody", "Queen")))
    }

    @Test
    fun `builds query from title only`() {
        assertEquals("Bohemian Rhapsody", SongEnrichmentService.buildQuery(song("Bohemian Rhapsody", null)))
    }

    @Test
    fun `builds query from filename when no metadata`() {
        assertEquals("mystery", SongEnrichmentService.buildQuery(song(null, null, "mystery.mp3")))
    }

    @Test
    fun `returns candidates from search`() {
        val candidates = listOf(
            YouTubeVideoResult("abc", "Bohemian Rhapsody", "Queen", durationSeconds = 354),
            YouTubeVideoResult("def", "Bohemian Rhapsody (Live)", "Queen", durationSeconds = 400),
        )

        val suggestion = SongEnrichmentService.findCandidates(
            song("Bohemian Rhapsody", "Queen"),
            apiKey = "test-key",
            search = { candidates },
        )

        assertEquals("Queen Bohemian Rhapsody", suggestion.query)
        assertEquals(2, suggestion.results.size)
        assertEquals("abc", suggestion.results[0].videoId)
    }

    @Test
    fun `returns empty results when search fails`() {
        val suggestion = SongEnrichmentService.findCandidates(
            song("Song", "Artist"),
            apiKey = "test-key",
            search = { throw RuntimeException("network error") },
        )

        assertTrue(suggestion.results.isEmpty())
    }
}
