package com.melodysync.scanner

import com.melodysync.model.Song
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

fun readMetadata(song: Song): Song {
    val audio = try {
        AudioFileIO.read(song.path.toFile())
    } catch (e: Exception) {
        // JAudioTagger has no Opus support; read Ogg/Opus comment tags instead.
        if (song.extension == "opus") return readOpusFallback(song)
        return song
    }

    val tag = audio.tag
    val header = audio.audioHeader

    val title = tag?.getFirst(FieldKey.TITLE)?.ifBlank { song.filename } ?: song.filename
    val artist = tag?.getFirst(FieldKey.ARTIST)?.ifBlank { null }
    val album = tag?.getFirst(FieldKey.ALBUM)?.ifBlank { null }

    val duration = header?.trackLength?.let { it.toDouble() }
    val bitrate = header?.bitRateAsNumber?.let { it.toInt() }
    val sampleRate = header?.sampleRateAsNumber
    val channels = header?.channels?.let(::parseChannels)
    val codec = header?.format

    return song.copy(
        title = title,
        artist = artist,
        album = album,
        duration = duration,
        bitrate = bitrate,
        sampleRate = sampleRate,
        channels = channels,
        codec = codec,
    )
}

private fun readOpusFallback(song: Song): Song {
    val tags = OpusMetadata.read(song.path) ?: return song
    return song.copy(
        title = tags.title ?: song.title,
        artist = tags.artist,
        album = tags.album,
        codec = "Opus",
    )
}

private val CHANNEL_MAP: Map<String, Int> = mapOf(
    "mono" to 1,
    "stereo" to 2,
    "joint stereo" to 2,
    "dual channel" to 2,
)

private fun parseChannels(value: String): Int? {
    val normalized = value.trim().lowercase()
    CHANNEL_MAP[normalized]?.let { return it }
    return normalized.toIntOrNull()
}
