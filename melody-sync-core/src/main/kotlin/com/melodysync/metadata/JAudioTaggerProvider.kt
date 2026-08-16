package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.tag.FieldKey
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.NoSuchFileException

/**
 * Metadata backend for formats handled by JAudioTagger
 * (mp3, flac, m4a, wav, ogg).
 */
object JAudioTaggerProvider : MetadataProvider {

    override val id = "JAudioTagger"
    override val formats = setOf("mp3", "flac", "m4a", "wav", "ogg")
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

        val title = tag?.getFirst(FieldKey.TITLE)?.let(::normalizeTag)?.ifBlank { song.filename } ?: song.filename
        val artist = tag?.getFirst(FieldKey.ARTIST)?.let(::normalizeTag)?.ifBlank { null }
        val album = tag?.getFirst(FieldKey.ALBUM)?.let(::normalizeTag)?.ifBlank { null }

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

    override fun write(song: Song, suggestion: TagSuggestion): WriteResult =
        try {
            val audio = AudioFileIO.read(song.path.toFile())
            val tag = audio.getTagOrCreateAndSetDefault()

            suggestion.title?.let { tag.setField(FieldKey.TITLE, it) }
            suggestion.artist?.let { tag.setField(FieldKey.ARTIST, it) }
            suggestion.album?.let { tag.setField(FieldKey.ALBUM, it) }

            AudioFileIO.write(audio)

            // Re-read so the returned Song reflects what is actually on disk.
            WriteResult(updated = read(song))
        } catch (e: Exception) {
            WriteResult(error = classify(song, e))
        }

    private fun normalizeTag(value: String): String = value.trimEnd('\u0000').trim()

    private fun classify(song: Song, e: Exception): TagWriteError {
        val message = e.message ?: ""
        return when {
            !Files.exists(song.path) || e is NoSuchFileException ->
                TagWriteError.NotFound(song.path.toString())
            e is AccessDeniedException || message.contains("permission", ignoreCase = true) ->
                TagWriteError.Permission(song.path.toString())
            e is CannotReadException || e is CannotWriteException ||
                message.contains("start of audio", ignoreCase = true) ||
                message.contains("audio header", ignoreCase = true) ||
                message.contains("unable to determine", ignoreCase = true) ->
                TagWriteError.Parse(message.ifBlank { "unable to read/write the file" })
            message.contains("locked", ignoreCase = true) ->
                TagWriteError.Locked
            else ->
                TagWriteError.Io(message.ifBlank { e.javaClass.simpleName })
        }
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
