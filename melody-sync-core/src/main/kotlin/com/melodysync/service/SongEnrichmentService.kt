package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.YouTubeVideoResult

data class EnrichmentSuggestion(
    val song: Song,
    val query: String,
    val results: List<YouTubeVideoResult>,
)

object SongEnrichmentService {

    fun buildQuery(song: Song): String {
        val parts = listOf(song.artist, song.title).filter { !it.isNullOrBlank() }
        return if (parts.isEmpty()) {
            song.filename.substringBeforeLast('.')
        } else {
            parts.joinToString(" ")
        }
    }

    fun findCandidates(
        song: Song,
        apiKey: String,
        search: (String) -> List<YouTubeVideoResult> = { q -> YouTubeSearchService.search(q, apiKey) },
    ): EnrichmentSuggestion {
        val query = buildQuery(song)
        val results = try {
            search(query)
        } catch (_: Exception) {
            emptyList()
        }
        return EnrichmentSuggestion(song = song, query = query, results = results)
    }
}
