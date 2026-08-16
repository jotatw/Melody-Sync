package com.melodysync.scanner

import com.melodysync.metadata.TagWriteError
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * Metadata reliability: writes through TagWriter must round-trip the same
 * values across every supported format, not merely succeed as a boolean.
 * Always runs on a copy in a temporary directory; never the original fixture.
 *
 * WAV is excluded from round-trips because it is read-only (JAudioTagger's
 * WAV writer silently drops tags); it has a dedicated refusal test.
 */
class MetadataRoundTripTest {

    @TempDir
    lateinit var tmp: Path

    private val supportedFormats =
        listOf("mp3", "flac", "m4a", "ogg", "opus")

    private fun copyFixture(format: String, targetName: String): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/$format/with_tags.$format")!!.toURI())
        val target = tmp.resolve(targetName)
        Files.copy(source, target)
        return target
    }

    @Test
    fun `full tag write round-trips for every supported format`() {
        supportedFormats.forEach { format ->
            val copied = copyFixture(format, "roundtrip.$format")
            val original = Song(path = copied, size = Files.size(copied))
            val suggestion = TagSuggestion(
                title = "Round Trip Title",
                artist = "Round Trip Artist",
                album = "Round Trip Album",
            )

            val result = TagWriter.writeTags(original, suggestion)

            assertTrue(result.success, "$format write")
            val updated = result.updated!!
            assertEquals("Round Trip Title", updated.title, "$format title")
            assertEquals("Round Trip Artist", updated.artist, "$format artist")
            assertEquals("Round Trip Album", updated.album, "$format album")

            val reRead = readMetadata(Song(path = copied, size = Files.size(copied)))
            assertEquals("Round Trip Title", reRead.title, "$format re-read title")
            assertEquals("Round Trip Artist", reRead.artist, "$format re-read artist")
            assertEquals("Round Trip Album", reRead.album, "$format re-read album")
        }
    }

    @Test
    fun `partial title-only write preserves artist and album`() {
        supportedFormats.forEach { format ->
            val copied = copyFixture(format, "partial.$format")
            val original = Song(path = copied, size = Files.size(copied))

            val result = TagWriter.writeTags(original, TagSuggestion(title = "Only Title"))

            assertTrue(result.success, "$format title-only write")
            val reRead = readMetadata(Song(path = copied, size = Files.size(copied)))
            assertEquals("Only Title", reRead.title, "$format title")
            assertEquals("Fixture Artist", reRead.artist, "$format artist preserved")
            assertEquals("Fixture Album", reRead.album, "$format album preserved")
        }
    }

    @Test
    fun `wav write is refused and leaves the file untouched`() {
        val copied = copyFixture("wav", "unsupported.wav")
        val before = Files.readAllBytes(copied)

        val result = TagWriter.writeTags(
            Song(path = copied, size = Files.size(copied)),
            TagSuggestion(title = "Do Not Write", artist = "X", album = "Y"),
        )

        assertFalse(result.success)
        assertTrue(result.error is TagWriteError.Unsupported)
        assertTrue(Files.readAllBytes(copied).contentEquals(before), "wav file must be untouched")
    }
}