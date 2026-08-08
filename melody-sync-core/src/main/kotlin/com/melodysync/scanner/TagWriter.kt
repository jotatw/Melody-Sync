package com.melodysync.scanner

import com.melodysync.metadata.MetadataFormatRegistry
import com.melodysync.metadata.WriteResult
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * Writes tag fields through the provider registered for the song's format and
 * re-reads it, returning the updated [Song] or a typed [com.melodysync.metadata.TagWriteError].
 *
 * Only fields present in the [TagSuggestion] are written.
 */
object TagWriter {

    fun writeTags(song: Song, suggestion: TagSuggestion): WriteResult {
        if (!suggestion.hasChanges) return WriteResult(updated = song)
        return MetadataFormatRegistry.write(song, suggestion)
    }
}
