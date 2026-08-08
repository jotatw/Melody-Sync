package com.melodysync.service

import com.melodysync.metadata.TagWriteError
import com.melodysync.model.MissingField
import com.melodysync.model.QualityFlag
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class QuickFixServiceTest {

    @TempDir
    lateinit var tmp: Path

    private fun song(
        path: Path,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        bitrate: Int? = 128_000,
        duration: Double? = 200.0,
    ): Song = Song(path = path, size = 0, title = title, artist = artist, album = album, bitrate = bitrate, duration = duration)

    @Test
    fun `diagnose lists missing fields`() {
        val diagnostics = QuickFixService.diagnose(song(Path.of("/a.mp3")))

        assertEquals(setOf(MissingField.TITLE, MissingField.ARTIST, MissingField.ALBUM), diagnostics.missing.toSet())
        assertTrue(diagnostics.hasIssues)
    }

    @Test
    fun `diagnose flags low bitrate and zero duration`() {
        val diagnostics = QuickFixService.diagnose(
            song(Path.of("/a.mp3"), title = "t", artist = "a", album = "al", bitrate = 96_000, duration = 0.0),
        )

        assertTrue(QualityFlag.LOW_BITRATE in diagnostics.flags)
        assertTrue(QualityFlag.ZERO_DURATION in diagnostics.flags)
        assertTrue(diagnostics.hasIssues)
    }

    @Test
    fun `diagnose clean song has no issues`() {
        val diagnostics = QuickFixService.diagnose(song(Path.of("/a.mp3"), title = "t", artist = "a", album = "al"))

        assertFalse(diagnostics.hasIssues)
        assertTrue(diagnostics.missing.isEmpty())
        assertTrue(diagnostics.flags.isEmpty())
    }

    @Test
    fun `localSuggestion only fills missing fields`() {
        val s = song(Path.of("/Music/Nirvana/Nevermind/01 - Song.mp3"), title = null, artist = "Nirvana", album = null)

        val suggestion = QuickFixService.localSuggestion(s)

        assertEquals("Song", suggestion.title)
        assertNull(suggestion.artist)
        assertEquals("Nevermind", suggestion.album)
    }

    @Test
    fun `localSuggestion is empty for a complete song`() {
        val s = song(Path.of("/Music/Nirvana/Nevermind/01 - Song.mp3"), title = "Song", artist = "Nirvana", album = "Nevermind")

        val suggestion = QuickFixService.localSuggestion(s)

        assertFalse(suggestion.hasChanges)
    }

    @Test
    fun `apply writes tags and returns the updated song`() {
        val path = tmp.resolve("song.mp3")
        Files.copy(Path.of(javaClass.getResource("/fixtures/audio/no_tags.mp3")!!.toURI()), path)
        val s = song(path, bitrate = null, duration = null)

        val result = QuickFixService.apply(s, TagSuggestion(title = "Title", artist = "Artist", album = "Album"))

        assertTrue(result.success)
        assertEquals("Title", result.updated!!.title)
        assertEquals("Artist", result.updated.artist)
        assertEquals("Album", result.updated.album)
    }

    @Test
    fun `apply reports a typed error when the file cannot be written`() {
        val path = tmp.resolve("fake.mp3")
        Files.writeString(path, "this is not an audio file")

        val result = QuickFixService.apply(song(path), TagSuggestion(title = "Title"))

        assertFalse(result.success)
        assertTrue(result.error is TagWriteError.Parse)
        assertTrue(result.error!!.userMessage.isNotBlank())
    }
}
