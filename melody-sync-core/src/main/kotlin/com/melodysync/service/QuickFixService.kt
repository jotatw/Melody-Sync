package com.melodysync.service

import com.melodysync.model.MissingField
import com.melodysync.model.QualityFlag
import com.melodysync.model.Song
import com.melodysync.model.SongDiagnostics
import com.melodysync.model.TagSuggestion
import com.melodysync.scanner.TagWriter

/**
 * Outcome of a tag write. On failure [error] carries the underlying reason
 * (e.g. JAudioTagger cannot write this specific file format), so the UI can
 * tell the user why a fix could not be applied.
 */
data class TagApplyResult(
    val updated: Song? = null,
    val error: String? = null,
) {
    val success: Boolean
        get() = updated != null
}

/**
 * Assists curation of a single song: diagnoses problems, suggests local and
 * YouTube fixes, and applies a chosen [TagSuggestion] to the file.
 *
 * [apply] only touches the audio file (via [TagWriter]); persisting the
 * updated [Song] to the database cache is the caller's responsibility.
 */
object QuickFixService {

    private const val LOW_BITRATE_THRESHOLD_BPS = 128_000

    fun diagnose(song: Song): SongDiagnostics {
        val missing = buildList {
            if (song.title.isNullOrBlank()) add(MissingField.TITLE)
            if (song.artist.isNullOrBlank()) add(MissingField.ARTIST)
            if (song.album.isNullOrBlank()) add(MissingField.ALBUM)
        }
        val flags = buildList {
            val bitrate = song.bitrate
            if (bitrate != null && bitrate < LOW_BITRATE_THRESHOLD_BPS) {
                add(QualityFlag.LOW_BITRATE)
            }
            if ((song.duration ?: 0.0) <= 0.0) {
                add(QualityFlag.ZERO_DURATION)
            }
        }
        return SongDiagnostics(song = song, missing = missing, flags = flags)
    }

    fun localSuggestion(song: Song): TagSuggestion {
        val diagnostics = diagnose(song)
        val raw = SongMatcher.suggest(song)
        return TagSuggestion(
            title = raw.title.takeIf { MissingField.TITLE in diagnostics.missing },
            artist = raw.artist.takeIf { MissingField.ARTIST in diagnostics.missing },
            album = raw.album.takeIf { MissingField.ALBUM in diagnostics.missing },
        )
    }

    fun youtubeSuggestion(song: Song, apiKey: String): EnrichmentSuggestion =
        SongEnrichmentService.findCandidates(song, apiKey)

    fun apply(song: Song, suggestion: TagSuggestion): TagApplyResult =
        try {
            TagApplyResult(updated = TagWriter.writeTags(song, suggestion))
        } catch (e: Exception) {
            TagApplyResult(error = e.message ?: "Could not write tags")
        }
}
