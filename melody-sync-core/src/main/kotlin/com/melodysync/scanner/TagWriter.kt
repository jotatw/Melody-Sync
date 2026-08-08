package com.melodysync.scanner

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey

/**
 * Writes tag fields to an audio file in place and re-reads it, returning the
 * updated [Song]. Throws on write/read failure so callers can surface the
 * error to the user (the Quick-Fix HUD "Apply" flow).
 *
 * Only fields present in the [TagSuggestion] are written.
 */
object TagWriter {

    fun writeTags(song: Song, suggestion: TagSuggestion): Song {
        if (!suggestion.hasChanges) return song

        val audio = AudioFileIO.read(song.path.toFile())
        val tag = audio.getTagOrCreateAndSetDefault()

        suggestion.title?.let { tag.setField(FieldKey.TITLE, it) }
        suggestion.artist?.let { tag.setField(FieldKey.ARTIST, it) }
        suggestion.album?.let { tag.setField(FieldKey.ALBUM, it) }

        AudioFileIO.write(audio)

        // Re-read so the returned Song reflects what is actually on disk.
        return readMetadata(song)
    }
}
