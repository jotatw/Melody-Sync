package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.check
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.melodysync.database.MusicDatabase
import com.melodysync.model.HealthReport
import com.melodysync.service.LibraryHealthService
import java.nio.file.Files
import java.nio.file.Path

class HealthCommand : CliktCommand(
    name = "health",
) {
    override fun help(context: Context): String =
        "Analyze the health of a music library and report issues"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to analyze",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val fromDatabase by option(
        "--from-db",
        help = "Analyze only the data already persisted in the database (faster)",
    ).flag()

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()

        val report = if (fromDatabase) {
            LibraryHealthService.analyzeFromDatabase(dir)
        } else {
            LibraryHealthService.analyze(dir)
        }

        printReport(report)
    }

    private fun printReport(report: HealthReport) {
        echo("══════════════════════════════════════════")
        echo("      Library Health Report")
        echo("══════════════════════════════════════════")
        echo()
        echo("Directory: ${report.directory}")
        echo()
        echo("Files: ${report.totalFiles} total | ${report.audioFiles} audio | ${report.totalNonAudio} non-audio")
        echo()

        if (report.nonAudio.isNotEmpty()) {
            echo("Non-audio by type:")
            report.nonAudio.forEach { category ->
                val size = formatMb(category.totalSize)
                echo("  ${category.category.padEnd(10)} ${category.count.toString().padStart(4)} files  ($size)  ${category.extensions.sorted().joinToString(", ")}")
            }
            echo()
        }

        if (report.unknownExtensions.isNotEmpty()) {
            echo("Unknown extensions:")
            report.unknownExtensions.forEach { echo("  .$it") }
            echo()
        }

        echo("Metadata issues:")
        echo("  ${report.songsWithoutMetadata.size.toString().padStart(4)} songs without title/artist")
        echo("  ${report.songsWithZeroDuration.size.toString().padStart(4)} songs with zero duration")
        echo("  ${report.orphanedEntries.size.toString().padStart(4)} orphaned entries in database")
        echo()

        if (report.songsWithMetadataIssues > 0) {
            echo("Suggestions (for your review):")
            if (report.songsWithoutMetadata.isNotEmpty()) {
                echo("  • Review ${report.songsWithoutMetadata.size} songs missing metadata")
            }
            if (report.songsWithZeroDuration.isNotEmpty()) {
                echo("  • Check ${report.songsWithZeroDuration.size} songs with zero duration")
            }
            if (report.orphanedEntries.isNotEmpty()) {
                echo("  • Remove ${report.orphanedEntries.size} orphaned database entries")
            }
        } else {
            echo("No metadata issues found.")
        }
    }

    private fun formatMb(bytes: Long): String =
        "%.1f MB".format(bytes / (1024.0 * 1024.0))
}

private fun String.Companion.format(format: String, vararg args: Any?): String =
    String.format(format, *args)
