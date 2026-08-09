package com.melodysync.service

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import com.melodysync.model.YouTubeVideoResult

/**
 * A concrete fix proposal coming from one suggestion source. Rendering is
 * source-agnostic: a title, an optional subtitle, the tags to apply and an
 * optional link to open for verification (e.g. the YouTube video).
 */
data class FixSuggestion(
    val sourceId: String,
    val sourceLabel: String,
    val title: String,
    val subtitle: String? = null,
    val tagSuggestion: TagSuggestion,
    val openUrl: String? = null,
)

/**
 * A pluggable source of fix suggestions (local heuristics today, YouTube
 * today, lyrics / other sources later). Keeps the Quick-Fix HUD open to new
 * sources without touching the UI.
 */
interface FixSuggestionSource {
    val id: String
    val label: String
    fun suggest(song: Song): List<FixSuggestion>
}

/**
 * Derives a fix from the file name and folder structure (Artist/Album/Title).
 */
object LocalFixSource : FixSuggestionSource {
    override val id = "local"
    override val label = "Local"

    override fun suggest(song: Song): List<FixSuggestion> {
        val suggestion = QuickFixService.localSuggestion(song)
        if (!suggestion.hasChanges) return emptyList()

        val parts = buildList {
            suggestion.title?.let { add("Title: $it") }
            suggestion.artist?.let { add("Artist: $it") }
            suggestion.album?.let { add("Album: $it") }
        }
        return listOf(
            FixSuggestion(
                sourceId = id,
                sourceLabel = label,
                title = "From file name & folder",
                subtitle = parts.joinToString(" · "),
                tagSuggestion = suggestion,
            ),
        )
    }
}

/**
 * Finds candidates on YouTube and turns the top hits into [FixSuggestion]s.
 * Returns empty when no key is set, the search fails or there are no results.
 */
class YoutubeFixSource(private val apiKey: String) : FixSuggestionSource {
    override val id = "youtube"
    override val label = "YouTube"

    override fun suggest(song: Song): List<FixSuggestion> {
        if (apiKey.isBlank()) return emptyList()
        val enrichment = QuickFixService.youtubeSuggestion(song, apiKey)
        return enrichment.results.take(3).map { video ->
            FixSuggestion(
                sourceId = id,
                sourceLabel = label,
                title = video.title,
                subtitle = video.channel,
                tagSuggestion = youtubeTagSuggestion(video),
                openUrl = video.url,
            )
        }
    }
}

/**
 * Builds a tag suggestion from a YouTube result: strips common title
 * suffixes ("(Official Audio)", "(Official Music Video)") so the title can be
 * presented as an editable candidate.
 *
 * The channel/uploader is NEVER mapped to Artist (reuploads make channel ≠
 * artist); it is exposed as identification context only via the suggestion
 * subtitle. See docs/integrations/youtube-identification.md.
 */
fun youtubeTagSuggestion(video: YouTubeVideoResult): TagSuggestion {
    val cleanedTitle = Regex("\\s*\\((Official (Audio|Video|Lyric Video)|Audio|Official)\\)\\s*$")
        .replace(video.title, "")
        .trim()
    return TagSuggestion(
        title = cleanedTitle.ifBlank { video.title },
        artist = null,
    )
}
