package com.melodysync.scanner

import com.melodysync.model.TagSuggestion
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads and writes Ogg Opus metadata (the OggOpusTags comment header) because
 * JAudioTagger does not support Opus.
 *
 * Reading extracts title/artist/album from the Vorbis-comment style fields.
 * Writing supports the common layout where the OpusHead packet fills its own
 * page; files with a different layout are left untouched (returns false).
 */
object OpusMetadata {

    data class OpusTags(val title: String?, val artist: String?, val album: String?)

    private data class Layout(
        val page0End: Int,
        val packet1Start: Int,
        val packet1ContentStart: Int,
        val packet1End: Int,
        val serial: Int,
    )

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

    /**
     * Rewrites the OpusTags packet with the given fields. Returns false when
     * the file layout is not supported (OpusHead must occupy its own page).
     */
    fun writeTags(path: Path, suggestion: TagSuggestion): Boolean {
        val bytes = try {
            Files.readAllBytes(path)
        } catch (_: Exception) {
            return false
        }
        if (bytes.size < 28) return false

        val layout = findLayout(bytes) ?: return false
        val packet1 = bytes.copyOfRange(layout.packet1ContentStart, layout.packet1End)
        val newPacket1 = mergeFields(packet1, suggestion) ?: return false

        val pages = buildPages(newPacket1, layout.serial, startSequence = 1)
        val output = ByteArrayOutputStream(bytes.size - (layout.packet1End - layout.packet1Start) + pages.size)
        output.write(bytes, 0, layout.page0End)
        pages.forEach { output.write(it) }
        output.write(bytes, layout.packet1End, bytes.size - layout.packet1End)

        return try {
            Files.write(path, output.toByteArray())
            true
        } catch (_: Exception) {
            false
        }
    }

    /** Locates packet 0/1 boundaries and the stream serial number. */
    private fun findLayout(bytes: ByteArray): Layout? {
        if (bytes.size < 28 || !isOggPage(bytes, 0)) return null

        // Walk page 0, capture the serial and the end of the OpusHead packet.
        val segmentCount0 = bytes[26].toInt() and 0xFF
        val page0DataStart = 27 + segmentCount0
        var packet0End = -1
        var dataStart = page0DataStart
        for (i in 0 until segmentCount0) {
            val lacing = bytes[27 + i].toInt() and 0xFF
            if (dataStart + lacing > bytes.size) return null
            dataStart += lacing
            if (lacing < 255) {
                packet0End = dataStart
                break
            }
        }
        if (packet0End < 0) return null

        // Require the OpusHead packet to fill page 0 exactly, so packet 1
        // starts at a page boundary and the page can be rewritten safely.
        val page0End = dataStart
        if (packet0End != page0End) return null

        val serial = leInt(bytes, 14)
        // Walk the OpusTags packet starting at page0End.
        var offset = page0End
        var packet1End = -1
        var contentStart = -1
        while (offset + 27 <= bytes.size) {
            if (!isOggPage(bytes, offset)) return null
            val segCount = bytes[offset + 26].toInt() and 0xFF
            val tableEnd = offset + 27 + segCount
            if (tableEnd > bytes.size) return null
            var s = tableEnd
            var complete = false
            for (i in 0 until segCount) {
                val lacing = bytes[offset + 27 + i].toInt() and 0xFF
                if (s + lacing > bytes.size) return null
                s += lacing
                if (lacing < 255) {
                    packet1End = s
                    complete = true
                    break
                }
            }
            if (contentStart < 0) contentStart = tableEnd
            if (complete) break
            offset = s
        }
        if (packet1End < 0) return null

        return Layout(
            page0End = page0End,
            packet1Start = page0End,
            packet1ContentStart = contentStart,
            packet1End = packet1End,
            serial = serial,
        )
    }

    /** Rebuilds the OpusTags packet with updated TITLE/ARTIST/ALBUM fields. */
    private fun mergeFields(packet: ByteArray, suggestion: TagSuggestion): ByteArray? {
        val header = parseCommentHeader(packet) ?: return null
        val vendor = extractVendor(packet) ?: return null
        val fields = extractFields(packet)

        val updates = mapOf(
            "title" to suggestion.title,
            "artist" to suggestion.artist,
            "album" to suggestion.album,
        )
        val existing = mutableMapOf<String, String>()
        fields.forEach { (key, value) -> existing[key] = value }
        updates.forEach { (key, value) ->
            if (value != null) existing[key] = value
        }

        val out = ByteArrayOutputStream()
        out.write("OpusTags".toByteArray())
        val vendorBytes = vendor.toByteArray(StandardCharsets.UTF_8)
        out.write(le(vendorBytes.size))
        out.write(vendorBytes)
        out.write(le(existing.size))
        existing.forEach { (key, value) ->
            val field = "$key=$value".toByteArray(StandardCharsets.UTF_8)
            out.write(le(field.size))
            out.write(field)
        }
        return out.toByteArray()
    }

    private fun extractFields(packet: ByteArray): List<Pair<String, String>> {
        val vendor = extractVendor(packet) ?: return emptyList()
        var cursor = 8 + 4 + vendor.toByteArray(StandardCharsets.UTF_8).size
        if (cursor + 4 > packet.size) return emptyList()
        val count = leInt(packet, cursor)
        cursor += 4
        val result = mutableListOf<Pair<String, String>>()
        repeat(count) {
            if (cursor + 4 > packet.size) return@repeat
            val len = leInt(packet, cursor)
            cursor += 4
            if (cursor + len > packet.size) return@repeat
            val field = String(packet, cursor, len, StandardCharsets.UTF_8)
            cursor += len
            val eq = field.indexOf('=')
            if (eq > 0) result.add(field.substring(0, eq).lowercase() to field.substring(eq + 1))
        }
        return result
    }

    private fun extractVendor(packet: ByteArray): String? {
        if (packet.size < 8) return null
        val magic = String(packet, 0, minOf(8, packet.size))
        val offset = when {
            magic.startsWith("OpusTags") -> 8
            magic.startsWith("vorbis") -> 7
            else -> return null
        }
        if (offset + 4 > packet.size) return null
        val vendorLen = leInt(packet, offset)
        if (offset + 4 + vendorLen > packet.size) return null
        return String(packet, offset + 4, vendorLen, StandardCharsets.UTF_8)
    }

    /** Splits a packet into Ogg pages (segments <= 255, 255 segments/page). */
    private fun buildPages(packet: ByteArray, serial: Int, startSequence: Int): List<ByteArray> {
        val pages = mutableListOf<ByteArray>()
        var index = 0
        var sequence = startSequence
        while (index < packet.size) {
            val segments = mutableListOf<ByteArray>()
            var total = 0
            while (segments.size < 255 && index < packet.size) {
                val chunk = minOf(255, packet.size - index)
                val segment = packet.copyOfRange(index, index + chunk)
                segments.add(segment)
                total += chunk
                index += chunk
                if (chunk < 255) break
            }
            pages.add(buildPage(segments, serial, sequence++))
        }
        return pages
    }

    private fun buildPage(segments: List<ByteArray>, serial: Int, sequence: Int): ByteArray {
        val header = ByteArrayOutputStream()
        header.write("OggS".toByteArray())
        header.write(byteArrayOf(0, 0))
        header.write(ByteArray(8))
        header.write(le(serial))
        header.write(le(sequence))
        header.write(ByteArray(4)) // crc placeholder
        header.write(byteArrayOf(segments.size.toByte()))
        segments.forEach { header.write(it.size) }

        val body = ByteArrayOutputStream()
        segments.forEach { body.write(it) }

        val page = ByteArrayOutputStream()
        val headerBytes = header.toByteArray()
        val bodyBytes = body.toByteArray()
        page.write(headerBytes)
        page.write(bodyBytes)
        val crc = oggCrc(headerBytes, bodyBytes)
        val out = page.toByteArray()
        // Patch CRC into the page (offset 22..25 of the 27-byte header).
        out[22] = (crc and 0xFF).toByte()
        out[23] = ((crc shr 8) and 0xFF).toByte()
        out[24] = ((crc shr 16) and 0xFF).toByte()
        out[25] = ((crc shr 24) and 0xFF).toByte()
        return out
    }

    private fun oggCrc(header: ByteArray, body: ByteArray): Int {
        val crcTable = IntArray(256)
        for (i in 0 until 256) {
            var r = i shl 24
            repeat(8) {
                r = if (r and 0x80000000.toInt() != 0) (r shl 1) xor 0x04c11db7 else r shl 1
            }
            crcTable[i] = r
        }
        var crc = 0
        val all = header.copyOf(header.size + body.size)
        body.copyInto(all, header.size)
        all.forEach { b ->
            crc = ((crc shl 8) and 0xffffffff.toInt()) xor crcTable[((crc ushr 24) and 0xFF) xor (b.toInt() and 0xFF)]
        }
        return crc
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

    private fun le(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte(),
    )

    private fun leInt(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 0xFF) or
            ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
            ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
            ((bytes[offset + 3].toInt() and 0xFF) shl 24)
}
