package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context

class VersionCommand : CliktCommand(
    name = "version",
) {
    override fun help(context: Context): String =
        "Show the Melody Sync version"

    override fun run() {
        echo("Melody Sync v0.6.0-dev")
    }
}