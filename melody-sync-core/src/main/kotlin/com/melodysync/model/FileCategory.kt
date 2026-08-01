package com.melodysync.model

data class FileCategory(
    val category: String,
    val description: String,
    val extensions: Set<String>,
    val count: Int,
    val totalSize: Long,
) {
    val isNonEmpty: Boolean get() = count > 0
}

val KNOWN_NON_AUDIO: Map<String, Set<String>> = mapOf(
    "image" to setOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "svg"),
    "subtitle" to setOf("vtt", "srt", "ass", "ssa", "sub"),
    "lyrics" to setOf("lrc", "txt"),
    "metadata" to setOf("meta", "nfo", "xml", "cue"),
    "playlist" to setOf("m3u", "m3u8", "pls", "xspf"),
    "video" to setOf("mp4", "mkv", "avi", "webm", "mov"),
)

val CATEGORY_DESCRIPTIONS: Map<String, String> = mapOf(
    "image" to "cover art and images",
    "subtitle" to "subtitle files",
    "lyrics" to "lyrics and text files",
    "metadata" to "metadata files",
    "playlist" to "playlist files",
    "video" to "video files",
)

fun categoryForExtension(extension: String): String? =
    KNOWN_NON_AUDIO.entries
        .firstOrNull { extension in it.value }
        ?.key
