package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import com.melodysync.scanner.TagWriter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path

class MetadataFormatRegistryTest {

    @TempDir
    lateinit var tmp: Path

    private fun fixture(name: String, target: Path): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/$name")!!.toURI())
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        return target
    }

    @Test
    fun `resolves providers by extension`() {
        assertEquals(JAudioTaggerProvider, MetadataFormatRegistry.providerFor("mp3"))
        assertEquals(JAudioTaggerProvider, MetadataFormatRegistry.providerFor("m4a"))
        assertEquals(JAudioTaggerProvider, MetadataFormatRegistry.providerFor("FLAC"))
        assertEquals(OpusProvider, MetadataFormatRegistry.providerFor("opus"))
        assertNull(MetadataFormatRegistry.providerFor("xyz"))
    }

    @Test
    fun `read delegates to the matching provider`() {
        val mp3 = fixture("with_tags.mp3", tmp.resolve("a.mp3"))
        val readMp3 = MetadataFormatRegistry.read(Song(path = mp3, size = 0))
        assertEquals("qualque coisa", readMp3.title)

        val opus = tmp.resolve("b.opus")
        Files.write(opus, twoPageOpus("Song", "Artist", "Album"))
        val readOpus = MetadataFormatRegistry.read(Song(path = opus, size = 0))
        assertEquals("Song", readOpus.title)
        assertEquals("Artist", readOpus.artist)
    }

    @Test
    fun `write delegates and re-reads for mp3`() {
        val mp3 = fixture("no_tags.mp3", tmp.resolve("a.mp3"))
        val result = TagWriter.writeTags(
            Song(path = mp3, size = Files.size(mp3)),
            TagSuggestion(title = "Title", artist = "Artist", album = "Album"),
        )
        assertTrue(result.success)
        assertEquals("Title", result.updated!!.title)
        assertEquals("Artist", result.updated.artist)
        assertEquals("Album", result.updated.album)
    }

    @Test
    fun `write delegates for opus`() {
        val opus = tmp.resolve("b.opus")
        Files.write(opus, twoPageOpus("Song", "Artist", "Album"))
        val result = TagWriter.writeTags(
            Song(path = opus, size = Files.size(opus)),
            TagSuggestion(title = "New Title", artist = "New Artist"),
        )
        assertTrue(result.success)
        assertEquals("New Title", result.updated!!.title)
        assertEquals("New Artist", result.updated.artist)
    }

    @Test
    fun `write returns Unsupported for an unknown format`() {
        val song = Song(path = tmp.resolve("notes.xyz"), size = 0)
        val result = MetadataFormatRegistry.write(song, TagSuggestion(title = "t"))

        assertFalse(result.success)
        assertEquals(TagWriteError.Unsupported, result.error)
    }

    @Test
    fun `write returns NotFound for a missing file`() {
        val song = Song(path = tmp.resolve("missing.mp3"), size = 0)
        val result = MetadataFormatRegistry.write(song, TagSuggestion(title = "t"))

        assertTrue(result.error is TagWriteError.NotFound)
    }

    @Test
    fun `write returns Parse for an unreadable file`() {
        val file = tmp.resolve("fake.mp3")
        Files.writeString(file, "this is not an audio file")
        val result = MetadataFormatRegistry.write(
            Song(path = file, size = Files.size(file)),
            TagSuggestion(title = "t"),
        )

        assertTrue(result.error is TagWriteError.Parse)
    }

    @Test
    fun `providers describe formats and fields`() {
        assertTrue(MetadataFormatRegistry.providerFor("mp3")!!.supportsWrite)
        assertTrue(MetadataFormatRegistry.providerFor("opus")!!.supportsWrite)
        assertEquals(listOf("title", "artist", "album"), MetadataFormatRegistry.providerFor("mp3")!!.supportedFields)
    }

    private fun twoPageOpus(title: String, artist: String, album: String): ByteArray {
        val headPacket = "OpusHead".toByteArray() + byteArrayOf(
            1, 2, 0, 0,
            0x80.toByte(), 0xBB.toByte(), 0, 0,
            0, 0,
            0,
        )
        val comments = listOf("TITLE=$title", "ARTIST=$artist", "ALBUM=$album")
        val buffer = ByteArrayOutputStream()
        buffer.write("OpusTags".toByteArray())
        buffer.write(le(0))
        buffer.write(le(comments.size))
        comments.forEach { comment ->
            val bytes = comment.toByteArray()
            buffer.write(le(bytes.size))
            buffer.write(bytes)
        }
        val commentPacket = buffer.toByteArray()
        val serial = 0x11223344
        return oggPage(serial, 0, 2, listOf(headPacket)) + oggPage(serial, 1, 0, listOf(commentPacket))
    }

    private fun oggPage(serial: Int, sequence: Int, headerType: Int, segments: List<ByteArray>): ByteArray {
        val header = ByteArrayOutputStream()
        header.write("OggS".toByteArray())
        header.write(byteArrayOf(0, headerType.toByte()))
        header.write(ByteArray(8))
        header.write(le(serial))
        header.write(le(sequence))
        header.write(ByteArray(4))
        header.write(byteArrayOf(segments.size.toByte()))
        segments.forEach { header.write(it.size) }
        val body = ByteArrayOutputStream()
        segments.forEach { body.write(it) }
        val out = ByteArrayOutputStream()
        out.write(header.toByteArray())
        out.write(body.toByteArray())
        return out.toByteArray()
    }

    private fun le(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte(),
    )
}
