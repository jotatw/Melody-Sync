package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.model.Song
import com.melodysync.service.SongEnrichmentService
import java.nio.file.Files
import java.nio.file.Path

class EnrichCommand : CliktCommand(
    name = "enrich",
) {
    override fun help(context: Context): String =
        "Search YouTube for candidate matches of songs missing metadata"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to enrich",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val onlyMissing by option(
        "--only-missing",
        help = "Only search songs missing title or artist",
    ).flag()

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        val apiKey = System.getenv("YOUTUBE_API_KEY")
        if (apiKey.isNullOrBlank()) {
            echo("Error: YOUTUBE_API_KEY environment variable is not set.")
            echo("Get a key at https://console.cloud.google.com/apis/credentials and export it.")
            return
        }

        dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()

        var songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
        if (onlyMissing) {
            songs = songs.filter { !it.hasMetadata }
        }

        if (songs.isEmpty()) {
            echo("No songs to enrich in $dir")
            return
        }

        echo("Searching YouTube for ${songs.size} songs (report-only, nothing is written)...")
        echo()

        var searched = 0
        songs.forEach { song ->
            val suggestion = SongEnrichmentService.findCandidates(song, apiKey)
            printSuggestion(song, suggestion.results)
            searched++
        }

        echo()
        echo("Searched $searched songs. Candidates shown above are for your review.")
    }

    private fun printSuggestion(song: Song, results: List<com.melodysync.model.YouTubeVideoResult>) {
        val label = song.artist?.let { "$it — ${song.title ?: song.filename}" } ?: song.filename
        echo("◆ $label")
        if (results.isEmpty()) {
            echo("    No candidates found.")
        } else {
            results.take(3).forEach { video ->
                val duration = video.durationSeconds?.let { "${it / 60}m${it % 60}s" } ?: "?"
                echo("    • ${video.title}  (${video.channel}, $duration)")
                echo("      ${video.url}")
            }
        }
        echo()
    }
}
