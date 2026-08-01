package com.melodysync.service

import com.melodysync.model.OrganizationReport
import com.melodysync.model.PlannedMove
import com.melodysync.model.Song
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object LibraryOrganizationService {

    fun planOrganization(songs: List<Song>, root: Path): OrganizationReport {
        val planned = planMoves(songs, root)

        return OrganizationReport(
            directory = root,
            plannedMoves = planned,
            moved = 0,
            skipped = 0,
            errors = emptyList(),
        )
    }

    fun reorganize(songs: List<Song>, root: Path): OrganizationReport {
        val planned = planMoves(songs, root)

        val errors = mutableListOf<String>()
        var moved = 0
        var skipped = 0

        planned.forEach { move ->
            if (move.from == move.to) {
                skipped++
                return@forEach
            }
            try {
                Files.createDirectories(move.to.parent)
                Files.move(move.from, move.to, StandardCopyOption.REPLACE_EXISTING)
                moved++
            } catch (e: Exception) {
                errors.add("${move.from}: ${e.message}")
                skipped++
            }
        }

        return OrganizationReport(
            directory = root,
            plannedMoves = planned,
            moved = moved,
            skipped = skipped,
            errors = errors,
        )
    }

    private fun planMoves(songs: List<Song>, root: Path): List<PlannedMove> {
        val planned = songs.mapNotNull { song ->
            val target = targetPath(song, root) ?: return@mapNotNull null
            PlannedMove(
                song = song,
                from = song.path,
                to = target,
                reason = if (song.path == target) "already organized" else "needs move",
            )
        }

        return resolveNameCollisions(planned)
    }

    private fun resolveNameCollisions(planned: List<PlannedMove>): List<PlannedMove> {
        val usedTargets = mutableSetOf<Path>()
        val result = mutableListOf<PlannedMove>()

        planned.sortedBy { it.from }.forEach { move ->
            var candidate = move.to
            var counter = 2

            while (candidate in usedTargets || (Files.exists(candidate) && candidate != move.from)) {
                val parent = move.to.parent
                val name = move.to.fileName.toString()
                val dot = name.lastIndexOf('.')
                val base = if (dot > 0) name.substring(0, dot) else name
                val ext = if (dot > 0) name.substring(dot) else ""
                candidate = parent.resolve("$base ($counter)$ext")
                counter++
            }

            usedTargets.add(candidate)
            result.add(move.copy(to = candidate))
        }

        return result
    }

    fun targetPath(song: Song, root: Path): Path? {
        if (!song.path.startsWith(root)) return null

        val artist = sanitize(song.artist) ?: "Unknown Artist"
        val album = sanitize(song.album)
        val title = sanitizeTitle(song)
        val extension = if (song.extension.isNotBlank()) ".${song.extension}" else ""

        val base = root.resolve(artist)
        val file = if (album != null) {
            base.resolve(album).resolve("$title$extension")
        } else {
            base.resolve("$title$extension")
        }

        return file
    }

    private fun sanitizeTitle(song: Song): String {
        val title = sanitize(song.title) ?: song.filename.substringBeforeLast('.')
        return title
    }

    private fun sanitize(value: String?): String? {
        val cleaned = value?.trim()?.replace(Regex("[/\\\\:*?\"<>|]"), "_")?.takeIf { it.isNotBlank() }
        return cleaned
    }
}
