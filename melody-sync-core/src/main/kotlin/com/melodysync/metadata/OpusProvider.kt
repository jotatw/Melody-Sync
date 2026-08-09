package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import com.melodysync.scanner.OpusMetadata
import java.nio.file.Files

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
            // Fall back to the file name for untagged files, like other formats.
            title = tags.title ?: song.title ?: song.filename,
            artist = tags.artist,
            album = tags.album,
            codec = "Opus",
        )
    }

    override fun write(song: Song, suggestion: TagSuggestion): WriteResult {
        if (!Files.exists(song.path)) {
            return WriteResult(error = TagWriteError.NotFound(song.path.toString()))
        }
        val written = OpusMetadata.writeTags(song.path, suggestion)
        if (!written) {
            return WriteResult(error = TagWriteError.Parse("unsupported Opus layout"))
        }
        return WriteResult(updated = read(song))
    }
}
