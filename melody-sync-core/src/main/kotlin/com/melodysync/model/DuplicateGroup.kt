package com.melodysync.model

data class DuplicateGroup(
    val key: String,
    val title: String?,
    val artist: String?,
    val songs: List<Song>,
) {
    val size: Int get() = songs.size
    val extraFiles: Int get() = songs.size - 1
}
