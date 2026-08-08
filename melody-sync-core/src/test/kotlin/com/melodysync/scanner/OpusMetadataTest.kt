package com.melodysync.scanner

import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path

class OpusMetadataTest {

    @TempDir
    lateinit var tmp: Path

    @Test
    fun `reads vorbis comment tags from an opus file`() {
        val file = tmp.resolve("song.opus")
        Files.write(file, oggPageWithOpusTags("Song", "Artist", "Album"))

        val tags = OpusMetadata.read(file)

        assertEquals("Song", tags?.title)
        assertEquals("Artist", tags?.artist)
        assertEquals("Album", tags?.album)
    }

    @Test
    fun `readMetadata falls back to opus tags`() {
        val file = tmp.resolve("song.opus")
        Files.write(file, oggPageWithOpusTags("Song", "Artist", "Album"))

        val song = readMetadata(Song(path = file, size = Files.size(file)))

        assertEquals("Song", song.title)
        assertEquals("Artist", song.artist)
        assertEquals("Album", song.album)
    }

    @Test
    fun `returns null for non ogg data`() {
        val file = tmp.resolve("junk.opus")
        Files.write(file, byteArrayOf(1, 2, 3, 4, 5))

        assertNull(OpusMetadata.read(file))
    }

    @Test
    fun `writeTags updates the comment header and is readable again`() {
        val file = tmp.resolve("song.opus")
        Files.write(file, twoPageOpus("Song", "Artist", "Album"))

        val written = OpusMetadata.writeTags(
            file,
            com.melodysync.model.TagSuggestion(title = "New Title", artist = "New Artist", album = "New Album"),
        )

        assertTrue(written)
        val tags = OpusMetadata.read(file)
        assertEquals("New Title", tags?.title)
        assertEquals("New Artist", tags?.artist)
        assertEquals("New Album", tags?.album)
    }

    @Test
    fun `TagWriter applies to opus via the fallback`() {
        val file = tmp.resolve("song.opus")
        Files.write(file, twoPageOpus("Song", "Artist", "Album"))

        val song = com.melodysync.scanner.TagWriter.writeTags(
            com.melodysync.model.Song(path = file, size = Files.size(file)),
            com.melodysync.model.TagSuggestion(title = "Applied", artist = "From Test"),
        )

        assertEquals("Applied", song.title)
        assertEquals("From Test", song.artist)
        assertEquals("Album", song.album)
    }

    private fun twoPageOpus(title: String, artist: String, album: String): ByteArray {
        val headPacket = "OpusHead".toByteArray() + byteArrayOf(
            1, 2, 0, 0,
            0x80.toByte(), 0xBB.toByte(), 0, 0,
            0, 0,
            0,
        )
        val commentPacket = buildCommentPacket(title, artist, album)
        val serial = 0x11223344

        val page0 = oggPage(
            serial = serial,
            sequence = 0,
            headerType = 2,
            segments = listOf(headPacket),
        )
        val page1 = oggPage(
            serial = serial,
            sequence = 1,
            headerType = 0,
            segments = listOf(commentPacket),
        )
        return page0 + page1
    }

    private fun buildCommentPacket(title: String, artist: String, album: String): ByteArray {
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
        return buffer.toByteArray()
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

    private fun oggPageWithOpusTags(title: String, artist: String, album: String): ByteArray {
        val headPacket = "OpusHead".toByteArray() + byteArrayOf(
            1, 2, 0, 0,
            0x80.toByte(), 0xBB.toByte(), 0, 0,
            0, 0,
            0,
        )

        val comments = listOf("TITLE=$title", "ARTIST=$artist", "ALBUM=$album")
        val commentBuffer = ByteArrayOutputStream()
        commentBuffer.write("OpusTags".toByteArray())
        commentBuffer.write(le(0))
        commentBuffer.write(le(comments.size))
        comments.forEach { comment ->
            val bytes = comment.toByteArray()
            commentBuffer.write(le(bytes.size))
            commentBuffer.write(bytes)
        }
        val commentPacket = commentBuffer.toByteArray()

        val header = ByteArrayOutputStream()
        header.write("OggS".toByteArray())
        header.write(byteArrayOf(0, 0))
        header.write(ByteArray(8))
        header.write(le(0x11223344))
        header.write(le(0))
        header.write(ByteArray(4))
        header.write(byteArrayOf(2))
        header.write(byteArrayOf(headPacket.size.toByte(), commentPacket.size.toByte()))

        val page = ByteArrayOutputStream()
        page.write(header.toByteArray())
        page.write(headPacket)
        page.write(commentPacket)
        return page.toByteArray()
    }

    private fun le(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte(),
    )
}
