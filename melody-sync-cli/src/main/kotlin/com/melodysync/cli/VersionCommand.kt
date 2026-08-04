package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.melodysync.platform.system.VersionInfo

class VersionCommand : CliktCommand(
    name = "version",
) {
    override fun help(context: Context): String =
        "Show the Melody Sync version"

    override fun run() {
        echo("Melody Sync ${VersionInfo.displayVersion}")
    }
}
