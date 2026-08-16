package com.melodysync.scanner

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path

class MetadataDiagnosticServiceTest {

    @TempDir
    lateinit var tmp: Path

    private fun copyFixture(name: String, target: Path): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/mp3/$name")!!.toURI())
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
        return target
    }

    @Test
    fun `mp3 is read and written via JAudioTagger`() {
        val file = copyFixture("with_tags.mp3", tmp.resolve("song.mp3"))

        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertEquals("mp3", diagnostic.format)
        assertEquals("JAudioTagger", diagnostic.provider)
        assertTrue(diagnostic.readSupported)
        assertTrue(diagnostic.readOk)
        assertTrue(diagnostic.writeSupported)
        assertEquals(listOf("title", "artist", "album"), diagnostic.supportedFields)
        assertTrue(diagnostic.writeTest!!.passed)
    }

    @Test
    fun `opus is read and written via the Opus provider`() {
        val file = tmp.resolve("song.opus")
        Files.write(file, twoPageOpus())

        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertEquals("opus", diagnostic.format)
        assertEquals("OpusProvider", diagnostic.provider)
        assertTrue(diagnostic.readSupported)
        assertTrue(diagnostic.readOk)
        assertTrue(diagnostic.writeSupported)
        assertTrue(diagnostic.writeTest!!.passed)
    }

    @Test
    fun `unreadable file reports read failure and write test failure`() {
        val file = tmp.resolve("fake.mp3")
        Files.writeString(file, "this is not an audio file")

        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertTrue(diagnostic.readSupported)
        assertFalse(diagnostic.readOk)
        assertTrue(diagnostic.readReason.isNullOrBlank().not())
        assertTrue(diagnostic.writeSupported)
        assertFalse(diagnostic.writeTest!!.passed)
        assertTrue(diagnostic.writeTest.reason.isNullOrBlank().not())
    }

    @Test
    fun `write test never modifies the original file`() {
        val file = copyFixture("with_tags.mp3", tmp.resolve("song.mp3"))
        val before = Files.readAllBytes(file)

        MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertTrue(Files.readAllBytes(file).contentEquals(before))
    }

    @Test
    fun `unknown format reports no support and skips the write test`() {
        val file = tmp.resolve("notes.txt")
        Files.writeString(file, "text")

        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertFalse(diagnostic.readSupported)
        assertFalse(diagnostic.writeSupported)
        assertNull(diagnostic.writeTest)
    }

    @Test
    fun `wav is read-only and skips the write test`() {
        val file = tmp.resolve("song.wav")
        Files.copy(
            Path.of(javaClass.getResource("/fixtures/audio/wav/with_tags.wav")!!.toURI()),
            file,
        )

        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)

        assertTrue(diagnostic.readSupported)
        assertTrue(diagnostic.readOk)
        assertFalse(diagnostic.writeSupported)
        assertNull(diagnostic.writeTest)
    }

    private fun twoPageOpus(): ByteArray {
        val headPacket = "OpusHead".toByteArray() + byteArrayOf(
            1, 2, 0, 0,
            0x80.toByte(), 0xBB.toByte(), 0, 0,
            0, 0,
            0,
        )
        val comments = listOf("TITLE=Song", "ARTIST=Artist", "ALBUM=Album")
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
