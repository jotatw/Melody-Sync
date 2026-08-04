package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.melodysync.platform.system.VersionInfo

class MelodySyncCommand : CliktCommand(
    name = "melody-sync",
) {
    override fun help(context: Context): String =
        "Organize, analyze and explore your local music library."

    override fun run() {
        if (currentContext.invokedSubcommand == null) {
            echo("Melody Sync ${VersionInfo.displayVersion}")
            echo("Run 'melody-sync scan <directory>' to scan a music library.")
        }
    }
}

fun main(args: Array<String>) =
    MelodySyncCommand()
        .subcommands(ScanCommand(), HealthCommand(), DuplicatesCommand(), OrganizeCommand(), ExportCommand(), EnrichCommand(), VersionCommand())
        .main(args)