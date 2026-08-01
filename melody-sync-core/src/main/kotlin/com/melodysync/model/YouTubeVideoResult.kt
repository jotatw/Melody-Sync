package com.melodysync.model

import kotlinx.serialization.Serializable

@Serializable
data class YouTubeVideoResult(
    val videoId: String,
    val title: String,
    val channel: String,
    val description: String = "",
    val durationSeconds: Long? = null,
) {
    val url: String get() = "https://www.youtube.com/watch?v=$videoId"
}
