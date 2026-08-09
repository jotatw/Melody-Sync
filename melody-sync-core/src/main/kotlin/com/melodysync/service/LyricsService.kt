package com.melodysync.service

import com.melodysync.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Fetches lyrics for a song from the free Lyrics.ovh API
 * (https://api.lyrics.ovh/v1/{artist}/{title}). Report-first: lyrics are
 * informational, never applied to tags automatically.
 */
object LyricsService {

    private const val DEFAULT_BASE_URL = "https://api.lyrics.ovh/v1"

    private val json = Json { ignoreUnknownKeys = true }

    fun fetch(
        song: Song,
        baseUrl: String = DEFAULT_BASE_URL,
        client: HttpClient = defaultClient(),
    ): String? {
        val artist = song.artist?.trim().orEmpty()
        val title = song.title?.trim().orEmpty()
        if (artist.isBlank() || title.isBlank()) return null

        return try {
            val url = "$baseUrl/${encode(artist)}/${encode(title)}"
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val lyrics = json.decodeFromString<LyricsResponse>(response.body()).lyrics
                lyrics?.trim()?.ifBlank { null }
            } else {
                null // 404 or error means no lyrics for this song
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun defaultClient(): HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()
}

@Serializable
private data class LyricsResponse(
    val lyrics: String? = null,
    val status: Int? = null,
)
