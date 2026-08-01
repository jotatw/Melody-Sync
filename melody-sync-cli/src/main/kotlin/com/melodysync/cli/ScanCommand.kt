package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.database.MusicDatabase
import com.melodysync.scanner.calculateStatistics
import com.melodysync.scanner.scan
import com.melodysync.service.LibrarySyncService
import java.nio.file.Files
import java.nio.file.Path

class ScanCommand : CliktCommand(
    name = "scan",
) {
    override fun help(context: Context): String =
        "Scan a music library directory, display statistics, and sync with the local database"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to scan",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val persist by option(
        "--persist",
        help = "Save the scan results to the local SQLite database",
    ).flag()

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        echo("Scanning: $dir")
        echo()

        val songs = scan(dir)
        val stats = calculateStatistics(songs)

        echo("╔══════════════════════════════════════╗")
        echo("║        Library Statistics            ║")
        echo("╚══════════════════════════════════════╝")
        echo()
        echo("  Total songs:      ${stats.totalSongs}")
        echo("  Unique artists:   ${stats.uniqueArtists}")
        echo("  Unique albums:    ${stats.uniqueAlbums}")
        echo("  Total size:       ${"%.2f".format(stats.totalSizeMb)} MB (${"%.2f".format(stats.totalSizeGb)} GB)")
        echo("  Total duration:   ${"%.2f".format(stats.totalDurationMinutes)} min (${"%.2f".format(stats.totalDurationHours)} h)")
        echo("  Avg bitrate:      ${"%.0f".format(stats.averageBitrateKbps)} kbps")
        echo()

        if (stats.formats.isNotEmpty()) {
            echo("  Formats:")
            stats.formats.forEach { (fmt, count) ->
                echo("    .$fmt: $count")
            }
        }

        if (persist) {
            echo()
            echo("Syncing to database...")
            dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()
            val result = LibrarySyncService.syncDirectory(dir)
            echo("  Added:   ${result.added}")
            echo("  Updated: ${result.updated}")
            echo("  Removed: ${result.removed}")
            echo("  Database now holds ${result.totalInDatabase} songs.")
        }
    }
}

private fun String.Companion.format(format: String, vararg args: Any?): String =
    String.format(format, *args)
