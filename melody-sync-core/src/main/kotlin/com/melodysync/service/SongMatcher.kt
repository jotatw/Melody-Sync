package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion

/**
 * Local heuristic that derives a [TagSuggestion] from a song's folder and
 * file name.
 *
 * - `Artist/Album/01 - Title.ext`  -> artist, album, title (track prefix is
 *   the reliable signal that the parent is the album).
 * - `Artist - Title.ext`           -> artist, title.
 * - `Artist/Title.ext`             -> artist, title.
 *
 * Everything produced here is a suggestion — the user always validates
 * before applying.
 */
object SongMatcher {

    private val trackPrefix = Regex("^(\\d{1,3})\\s*[-_.]\\s*(.+)$")
    private val separator = Regex("\\s+-\\s+")

    fun suggest(song: Song): TagSuggestion {
        val filename = song.path.fileName.toString().substringBeforeLast('.').trim()
        val dirPath = song.path.parent
        val dirName = dirPath?.fileName?.toString()
        val grandParent = dirPath?.parent?.fileName?.toString()

        trackPrefix.matchEntire(filename)?.let { match ->
            if (dirName != null && grandParent != null) {
                return TagSuggestion(
                    title = match.groupValues[2].trim(),
                    artist = grandParent,
                    album = dirName,
                )
            }
        }

        separator.find(filename)?.let { match ->
            val first = filename.substring(0, match.range.first).trim()
            val second = filename.substring(match.range.last + 1).trim()
            if (first.isNotBlank() && second.isNotBlank()) {
                return TagSuggestion(title = second, artist = first)
            }
        }

        val cleaned = trackPrefix.matchEntire(filename)?.groupValues?.get(2)?.trim() ?: filename
        return TagSuggestion(
            title = cleaned.ifBlank { null },
            artist = dirName?.ifBlank { null },
        )
    }
}
