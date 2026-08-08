package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

/**
 * Metadata backend for formats handled by JAudioTagger
 * (mp3, flac, m4a, wav, ogg, aac).
 */
object JAudioTaggerProvider : MetadataProvider {

    override val id = "JAudioTagger"
    override val formats = setOf("mp3", "flac", "m4a", "wav", "ogg", "aac")
    override val supportsWrite = true
    override val supportedFields = listOf("title", "artist", "album")

    override fun read(song: Song): Song {
        val audio = try {
            AudioFileIO.read(song.path.toFile())
        } catch (e: Exception) {
            return song
        }

        val tag = audio.tag
        val header = audio.audioHeader

        val title = tag?.getFirst(FieldKey.TITLE)?.ifBlank { song.filename } ?: song.filename
        val artist = tag?.getFirst(FieldKey.ARTIST)?.ifBlank { null }
        val album = tag?.getFirst(FieldKey.ALBUM)?.ifBlank { null }

        return song.copy(
            title = title,
            artist = artist,
            album = album,
            duration = header?.trackLength?.let { it.toDouble() },
            bitrate = header?.bitRateAsNumber?.let { it.toInt() },
            sampleRate = header?.sampleRateAsNumber,
            channels = header?.channels?.let(::parseChannels),
            codec = header?.format,
        )
    }

    override fun write(song: Song, suggestion: TagSuggestion): Song {
        val audio = AudioFileIO.read(song.path.toFile())
        val tag = audio.getTagOrCreateAndSetDefault()

        suggestion.title?.let { tag.setField(FieldKey.TITLE, it) }
        suggestion.artist?.let { tag.setField(FieldKey.ARTIST, it) }
        suggestion.album?.let { tag.setField(FieldKey.ALBUM, it) }

        AudioFileIO.write(audio)

        // Re-read so the returned Song reflects what is actually on disk.
        return read(song)
    }

    private val channelMap: Map<String, Int> = mapOf(
        "mono" to 1,
        "stereo" to 2,
        "joint stereo" to 2,
        "dual channel" to 2,
    )

    private fun parseChannels(value: String): Int? {
        val normalized = value.trim().lowercase()
        channelMap[normalized]?.let { return it }
        return normalized.toIntOrNull()
    }
}
