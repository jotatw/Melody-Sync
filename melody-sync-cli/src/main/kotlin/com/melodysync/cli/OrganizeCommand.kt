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
import com.melodysync.service.LibraryOrganizationService
import java.nio.file.Files
import java.nio.file.Path

class OrganizeCommand : CliktCommand(
    name = "organize",
) {
    override fun help(context: Context): String =
        "Plan or apply an Artist/Album folder organization to a music library"

    private val directory by argument(
        name = "directory",
        help = "Music library directory to organize",
    ).path().check("Directory must exist and be a valid directory") { path ->
        Files.exists(path) && Files.isDirectory(path)
    }

    private val apply by option(
        "--apply",
        help = "Actually move files (default is a dry-run report only)",
    ).flag()

    private val dbFile by option(
        "--db",
        help = "Path to the SQLite database file (defaults to ~/.config/melody-sync/library.db)",
    ).path()

    override fun run() {
        val dir = directory.toAbsolutePath()

        dbFile?.let { MusicDatabase.connectToFile(it) } ?: MusicDatabase.connect()

        val songs = MusicRepository.findAll().filter { it.path.startsWith(dir) }

        val report = if (apply) {
            LibraryOrganizationService.reorganize(songs, dir)
        } else {
            LibraryOrganizationService.planOrganization(songs, dir)
        }

        printReport(report, apply)
    }

    private fun printReport(report: com.melodysync.model.OrganizationReport, applied: Boolean) {
        val mode = if (applied) "Applied" else "Dry-run (no files moved)"
        echo("══════════════════════════════════════════")
        echo("      Organization Report")
        echo("══════════════════════════════════════════")
        echo()
        echo("Directory: ${report.directory}")
        echo("Mode: $mode")
        echo()
        echo("Songs analyzed:   ${report.plannedMoves.size}")
        echo("Already organized: ${report.alreadyOrganized}")
        echo("Needs moving:     ${report.toMove}")
        if (applied) {
            echo("Moved:            ${report.moved}")
            echo("Skipped:          ${report.skipped}")
        }
        echo()

        if (report.errors.isNotEmpty()) {
            echo("Errors:")
            report.errors.forEach { echo("  ! $it") }
            echo()
        }

        report.plannedMoves.filter { it.from != it.to }.take(20).forEach { move ->
            echo("  ${move.from.fileName}")
            echo("    → ${move.to}")
        }
        if (report.toMove > 20) {
            echo("  … and ${report.toMove - 20} more")
        }
        echo()

        if (!applied) {
            echo("Nothing was moved. Run with --apply to reorganize.")
        }
    }
}
