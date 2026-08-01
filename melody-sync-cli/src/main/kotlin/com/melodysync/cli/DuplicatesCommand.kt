package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.service.DuplicateDetectionService
import java.nio.file.Files
import java.nio.file.Path

class DuplicatesCommand : CliktCommand(
    name = "duplicates",
) {
    override fun help(context: Context): String =
        "Detect duplicate songs in a music library"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to analyze",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()

        val songs = MusicRepository.findAll().filter { song ->
            song.path.startsWith(dir)
        }

        val groups = DuplicateDetectionService.detectDuplicates(songs)

        printReport(dir, groups)
    }

    private fun printReport(dir: Path, groups: List<com.melodysync.model.DuplicateGroup>) {
        echo("══════════════════════════════════════════")
        echo("      Duplicate Report")
        echo("══════════════════════════════════════════")
        echo()
        echo("Directory: $dir")
        echo()
        echo("Duplicate groups: ${groups.size}")
        echo("Extra files: ${groups.sumOf { it.extraFiles }}")
        echo()

        if (groups.isEmpty()) {
            echo("No duplicates found.")
            return
        }

        groups.forEachIndexed { index, group ->
            echo("${index + 1}. ${group.artist ?: "Unknown"} — ${group.title ?: "Untitled"}")
            group.songs.forEachIndexed { songIndex, song ->
                val label = if (songIndex == 0) "  keep? " else "  dup:  "
                val size = "%.2f MB".format(song.sizeMb)
                echo("  $label ${song.filename}  ($size)")
            }
            echo()
        }

        echo("Suggestions (for your review):")
        echo("  • Review each group and decide which file to keep")
        echo("  • Nothing is deleted automatically")
    }
}

private fun String.Companion.format(format: String, vararg args: Any?): String =
    String.format(format, *args)
