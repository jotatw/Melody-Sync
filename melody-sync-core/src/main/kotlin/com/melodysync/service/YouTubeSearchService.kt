package com.melodysync.service

import com.melodysync.model.YouTubeVideoResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

object YouTubeSearchService {

    private const val SEARCH_URL = "https://www.googleapis.com/youtube/v3/search"
    private const val VIDEOS_URL = "https://www.googleapis.com/youtube/v3/videos"
    private const val MAX_RESULTS = 5

    private val json = Json { ignoreUnknownKeys = true }

    fun search(query: String, apiKey: String): List<YouTubeVideoResult> {
        require(apiKey.isNotBlank()) { "YouTube API key is required (set YOUTUBE_API_KEY)" }
        require(query.isNotBlank()) { "Search query must not be blank" }

        val client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

        val searchResponse = get(client, "$SEARCH_URL?part=snippet&type=video&maxResults=$MAX_RESULTS&q=${encode(query)}&key=$apiKey")
        val videos = parseSearchResults(searchResponse)

        val ids = videos.map { it.videoId }.joinToString(",")
        if (ids.isEmpty()) return emptyList()

        val durations = try {
            val detailsResponse = get(client, "$VIDEOS_URL?part=contentDetails&id=$ids&key=$apiKey")
            parseDurations(detailsResponse)
        } catch (_: Exception) {
            emptyMap()
        }

        return videos.map { video ->
            video.copy(durationSeconds = durations[video.videoId])
        }
    }

    private fun get(client: HttpClient, url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException("YouTube API error ${response.statusCode()}: ${response.body().take(200)}")
        }
        return response.body()
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun parseSearchResults(body: String): List<YouTubeVideoResult> {
        val root = json.parseToJsonElement(body).jsonObject
        val items = root["items"] as? JsonArray ?: JsonArray(emptyList())

        return items.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.jsonObject ?: return@mapNotNull null
            val videoId = id["videoId"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val snippet = obj["snippet"]?.jsonObject ?: JsonObject(emptyMap())

            YouTubeVideoResult(
                videoId = videoId,
                title = snippet["title"]?.jsonPrimitive?.content ?: "",
                channel = snippet["channelTitle"]?.jsonPrimitive?.content ?: "",
                description = snippet["description"]?.jsonPrimitive?.content ?: "",
            )
        }
    }

    private fun parseDurations(body: String): Map<String, Long> {
        val root = json.parseToJsonElement(body).jsonObject
        val items = root["items"] as? JsonArray ?: JsonArray(emptyList())

        return items.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val iso = obj["contentDetails"]?.jsonObject?.get("duration")?.jsonPrimitive?.content ?: return@mapNotNull null
            id to parseIso8601Duration(iso)
        }.toMap()
    }

    private fun parseIso8601Duration(iso: String): Long {
        val regex = Regex("P(?:T)?(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?")
        val match = regex.find(iso) ?: return 0L
        val hours = match.groupValues[1].toLongOrNull() ?: 0L
        val minutes = match.groupValues[2].toLongOrNull() ?: 0L
        val seconds = match.groupValues[3].toLongOrNull() ?: 0L
        return hours * 3600 + minutes * 60 + seconds
    }
}
