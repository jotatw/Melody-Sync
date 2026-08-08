package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import com.melodysync.scanner.OpusMetadata

/**
 * Metadata backend for Opus via the built-in Ogg/OpusTags reader/writer
 * (JAudioTagger has no Opus support).
 */
object OpusProvider : MetadataProvider {

    override val id = "OpusProvider"
    override val formats = setOf("opus")
    override val supportsWrite = true
    override val supportedFields = listOf("title", "artist", "album")

    override fun read(song: Song): Song {
        val tags = OpusMetadata.read(song.path) ?: return song
        return song.copy(
            title = tags.title ?: song.title,
            artist = tags.artist,
            album = tags.album,
            codec = "Opus",
        )
    }

    override fun write(song: Song, suggestion: TagSuggestion): Song {
        val written = OpusMetadata.writeTags(song.path, suggestion)
        if (!written) {
            throw java.io.IOException("Unsupported Opus layout — could not write tags")
        }
        return read(song)
    }
}
