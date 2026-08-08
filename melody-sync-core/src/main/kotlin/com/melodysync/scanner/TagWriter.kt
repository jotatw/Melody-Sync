package com.melodysync.scanner

import com.melodysync.metadata.MetadataFormatRegistry
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * Writes tag fields through the provider registered for the song's format and
 * re-reads it, returning the updated [Song]. Throws on write/read failure so
 * callers can surface the error to the user (the Quick-Fix HUD "Apply" flow).
 *
 * Only fields present in the [TagSuggestion] are written.
 */
object TagWriter {

    fun writeTags(song: Song, suggestion: TagSuggestion): Song {
        if (!suggestion.hasChanges) return song
        return MetadataFormatRegistry.write(song, suggestion)
    }
}
