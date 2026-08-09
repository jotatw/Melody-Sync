package com.melodysync.scanner

import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class MetadataTest {
    private fun fixture(name: String): Path =
        Path.of(javaClass.getResource("/fixtures/audio/mp3/$name")!!.toURI())

    @Test
    fun `uses filename when title is missing`() {
        val song = Song(path = fixture("no_tags.mp3"), size = 0L)
        val result = readMetadata(song)

        assertEquals("no_tags.mp3", result.title)
    }

    @Test
    fun `returns null when artist is missing`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNull(result.artist)
    }

    @Test
    fun `returns null when album is missing`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNull(result.album)
    }

    @Test
    fun `reads audio duration`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNotNull(result.duration)
        assertTrue(result.duration!! > 0)
    }

    @Test
    fun `reads audio bitrate`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNotNull(result.bitrate)
        assertTrue(result.bitrate!! > 0)
    }

    @Test
    fun `reads audio sample rate`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNotNull(result.sampleRate)
        assertTrue(result.sampleRate!! > 0)
    }

    @Test
    fun `reads audio channels`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNotNull(result.channels)
        assertTrue(result.channels!! > 0)
    }

    @Test
    fun `reads audio codec`() {
        val result = readMetadata(Song(path = fixture("no_tags.mp3"), size = 0L))
        assertNotNull(result.codec)
        assertTrue(result.codec is String)
    }

    @Test
    fun `reads title from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertEquals("Fixture Song", result.title)
    }

    @Test
    fun `reads artist from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertEquals("Fixture Artist", result.artist)
    }

    @Test
    fun `reads album from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertEquals("Fixture Album", result.album)
    }

    @Test
    fun `reads duration from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertNotNull(result.duration)
        assertTrue(result.duration!! > 0)
    }

    @Test
    fun `reads bitrate from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertNotNull(result.bitrate)
        assertTrue(result.bitrate!! > 0)
    }

    @Test
    fun `reads sample rate from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertNotNull(result.sampleRate)
        assertTrue(result.sampleRate!! > 0)
    }

    @Test
    fun `reads channels from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertNotNull(result.channels)
        assertTrue(result.channels!! > 0)
    }

    @Test
    fun `reads codec from tagged file`() {
        val result = readMetadata(Song(path = fixture("with_tags.mp3"), size = 0L))
        assertNotNull(result.codec)
        assertTrue(result.codec is String)
    }
}