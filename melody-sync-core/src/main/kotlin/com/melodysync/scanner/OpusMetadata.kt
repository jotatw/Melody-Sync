package com.melodysync.scanner

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads Ogg Opus metadata (the OggOpusTags comment header) because
 * JAudioTagger does not support Opus. Only reads title/artist/album from the
 * Vorbis-comment style fields. Writing Opus tags is not supported yet.
 */
object OpusMetadata {

    data class OpusTags(val title: String?, val artist: String?, val album: String?)

    fun read(path: Path): OpusTags? {
        val bytes = try {
            // Comment headers can be large (embedded artwork); reading the whole
            // file is the reliable way to find the end of the OpusTags packet.
            Files.readAllBytes(path)
        } catch (_: Exception) {
            return null
        }
        if (bytes.size < 28) return null

        val packets = extractFirstPackets(bytes, 2) ?: return null
        if (packets.size < 2) return null
        return parseCommentHeader(packets[1])
    }

    private fun extractFirstPackets(bytes: ByteArray, maxPackets: Int): List<ByteArray>? {
        val packets = mutableListOf<ByteArray>()
        val current = ByteArrayOutputStream()
        var offset = 0
        while (offset + 27 <= bytes.size) {
            if (!isOggPage(bytes, offset)) return null
            val segmentCount = bytes[offset + 26].toInt() and 0xFF
            val tableEnd = offset + 27 + segmentCount
            if (tableEnd > bytes.size) return null

            var dataStart = tableEnd
            for (i in 0 until segmentCount) {
                val lacing = bytes[offset + 27 + i].toInt() and 0xFF
                if (dataStart + lacing > bytes.size) return null
                current.write(bytes, dataStart, lacing)
                dataStart += lacing
                if (lacing < 255) {
                    packets.add(current.toByteArray())
                    current.reset()
                    if (packets.size >= maxPackets) return packets
                }
            }
            offset = dataStart
        }
        return packets
    }

    private fun isOggPage(bytes: ByteArray, offset: Int): Boolean =
        bytes[offset] == 'O'.code.toByte() &&
            bytes[offset + 1] == 'g'.code.toByte() &&
            bytes[offset + 2] == 'g'.code.toByte() &&
            bytes[offset + 3] == 'S'.code.toByte()

    private fun parseCommentHeader(packet: ByteArray): OpusTags? {
        if (packet.size < 16) return null
        val magic = String(packet, 0, minOf(8, packet.size))
        val offset = when {
            magic.startsWith("OpusTags") -> 8
            magic.startsWith("vorbis") -> 7
            else -> return null
        }
        var cursor = offset
        if (cursor + 4 > packet.size) return null
        val vendorLength = leInt(packet, cursor)
        cursor += 4 + vendorLength
        if (cursor + 4 > packet.size) return null
        val count = leInt(packet, cursor)
        cursor += 4

        var title: String? = null
        var artist: String? = null
        var album: String? = null
        repeat(count) {
            if (cursor + 4 > packet.size) return@repeat
            val len = leInt(packet, cursor)
            cursor += 4
            if (cursor + len > packet.size) return@repeat
            val field = String(packet, cursor, len, StandardCharsets.UTF_8)
            cursor += len
            val eq = field.indexOf('=')
            if (eq > 0) {
                val value = field.substring(eq + 1)
                when (field.substring(0, eq).lowercase()) {
                    "title" -> if (title == null) title = value
                    "artist" -> if (artist == null) artist = value
                    "album" -> if (album == null) album = value
                }
            }
        }
        return OpusTags(
            title = title?.ifBlank { null },
            artist = artist?.ifBlank { null },
            album = album?.ifBlank { null },
        )
    }

    private fun leInt(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
}
