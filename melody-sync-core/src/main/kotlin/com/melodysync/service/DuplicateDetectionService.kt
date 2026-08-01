package com.melodysync.service

import com.melodysync.model.DuplicateGroup
import com.melodysync.model.Song

object DuplicateDetectionService {

    private const val DURATION_TOLERANCE_SECONDS = 3.0

    fun detectDuplicates(songs: List<Song>): List<DuplicateGroup> {
        val meaningful = songs.filter { it.hasMetadata }

        val groups = meaningful.groupBy { song ->
            val title = normalize(song.title)
            val artist = normalize(song.artist)
            "$title|$artist"
        }

        return groups
            .filter { it.value.size > 1 }
            .map { (key, groupSongs) -> buildGroupsWithinDuration(key, groupSongs) }
            .flatten()
            .filter { it.songs.size > 1 }
            .sortedWith(compareBy({ it.artist.orEmpty() }, { it.title.orEmpty() }))
    }

    private fun buildGroupsWithinDuration(key: String, songs: List<Song>): List<DuplicateGroup> {
        val groups = mutableListOf<MutableList<Song>>()

        songs.sortedBy { it.duration ?: 0.0 }.forEach { song ->
            val existing = groups.firstOrNull { group ->
                val base = group.first().duration ?: 0.0
                val candidate = song.duration ?: 0.0
                kotlin.math.abs(base - candidate) <= DURATION_TOLERANCE_SECONDS
            }
            if (existing == null) {
                groups.add(mutableListOf(song))
            } else {
                existing.add(song)
            }
        }

        return groups.map { group ->
            DuplicateGroup(
                key = key,
                title = group.first().title,
                artist = group.first().artist,
                songs = group,
            )
        }
    }

    private fun normalize(value: String?): String =
        value?.trim()?.lowercase()?.replace(Regex("\\s+"), " ") ?: ""
}
