package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.database.MusicDatabase
import com.melodysync.database.MusicRepository
import com.melodysync.service.ExportFormat
import com.melodysync.service.LibraryExportService
import java.nio.file.Files
import java.nio.file.Path

class ExportCommand : CliktCommand(
    name = "export",
) {
    override fun help(context: Context): String =
        "Export the library metadata to JSON or CSV"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to export",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val output by option(
        "--output", "-o",
        help = "Output file path (defaults to library.json or library.csv in the current directory)",
    ).path()

    private val format by option(
        "--format", "-f",
        help = "Export format",
    ).choice("json", "csv").default("json")

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()

        val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }
        if (songs.isEmpty()) {
            echo("No songs found in $dir")
            return
        }

        val exportFormat = if (format == "json") ExportFormat.JSON else ExportFormat.CSV
        val extension = format
        val out = output ?: Path.of("library.$extension")

        LibraryExportService.write(songs, out, exportFormat)

        echo("Exported ${songs.size} songs to ${out.toAbsolutePath()}")
    }
}
