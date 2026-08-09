package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.YouTubeVideoResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class FixSuggestionSourceTest {

    private fun song(path: String, title: String?, artist: String?, album: String?): Song =
        Song(path = Path.of(path), size = 0, title = title, artist = artist, album = album)

    @Test
    fun `local source suggests only for missing fields`() {
        val s = song("/Music/Nirvana/Nevermind/01 - Song.mp3", title = null, artist = "Nirvana", album = null)

        val items = LocalFixSource.suggest(s)

        assertEquals(1, items.size)
        assertEquals("local", items.first().sourceId)
        assertEquals("Song", items.first().tagSuggestion.title)
        assertEquals(null, items.first().tagSuggestion.artist)
        assertEquals("Nevermind", items.first().tagSuggestion.album)
    }

    @Test
    fun `local source returns empty for a complete song`() {
        val s = song("/Music/Nirvana/Nevermind/01 - Song.mp3", title = "Song", artist = "Nirvana", album = "Nevermind")

        assertTrue(LocalFixSource.suggest(s).isEmpty())
    }

    @Test
    fun `youtube source returns empty without api key`() {
        assertTrue(YoutubeFixSource("").suggest(song("/a.mp3", null, null, null)).isEmpty())
    }

    @Test
    fun `youtube tag suggestion strips title suffix and never maps the channel to artist`() {
        val video = YouTubeVideoResult(
            videoId = "x",
            title = "Nirvana - Smells Like Teen Spirit (Official Audio)",
            channel = "Nirvana - Topic",
        )

        val suggestion = youtubeTagSuggestion(video)

        assertEquals("Nirvana - Smells Like Teen Spirit", suggestion.title)
        assertNull(suggestion.artist)
    }
}
