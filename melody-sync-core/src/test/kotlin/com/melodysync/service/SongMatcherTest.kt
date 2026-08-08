package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.nio.file.Path

class SongMatcherTest {

    private fun song(path: String): Song = Song(path = Path.of(path), size = 0)

    @Test
    fun `artist album and track numbered filename`() {
        val suggestion = SongMatcher.suggest(song("/Music/Nirvana/Nevermind/01 - Smells Like Teen Spirit.mp3"))

        assertEquals("Nirvana", suggestion.artist)
        assertEquals("Nevermind", suggestion.album)
        assertEquals("Smells Like Teen Spirit", suggestion.title)
    }

    @Test
    fun `artist album and dot separated track`() {
        val suggestion = SongMatcher.suggest(song("/Music/Radiohead/OK Computer/02.No Surprises.mp3"))

        assertEquals("Radiohead", suggestion.artist)
        assertEquals("OK Computer", suggestion.album)
        assertEquals("No Surprises", suggestion.title)
    }

    @Test
    fun `artist from folder and title from filename`() {
        val suggestion = SongMatcher.suggest(song("/Music/Nirvana/Smells Like Teen Spirit.mp3"))

        assertEquals("Nirvana", suggestion.artist)
        assertEquals("Smells Like Teen Spirit", suggestion.title)
        assertNull(suggestion.album)
    }

    @Test
    fun `dash separated artist - title filename`() {
        val suggestion = SongMatcher.suggest(song("/Music/Unknown/Queen - Bohemian Rhapsody.mp3"))

        assertEquals("Queen", suggestion.artist)
        assertEquals("Bohemian Rhapsody", suggestion.title)
    }

    @Test
    fun `single file without folder context`() {
        val suggestion = SongMatcher.suggest(song("Untitled Song.mp3"))

        assertEquals("Untitled Song", suggestion.title)
        assertNull(suggestion.artist)
        assertNull(suggestion.album)
    }

    @Test
    fun `empty suggestion when nothing derivable`() {
        val suggestion = SongMatcher.suggest(song("/.mp3"))

        assertNull(suggestion.title)
        assertNull(suggestion.artist)
        assertNull(suggestion.album)
    }
}
