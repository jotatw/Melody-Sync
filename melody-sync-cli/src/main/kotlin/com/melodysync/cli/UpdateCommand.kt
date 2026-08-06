package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.melodysync.platform.installation.InstallationPaths
import com.melodysync.platform.installation.InstallationService
import com.melodysync.platform.system.VersionInfo
import java.nio.file.Path

class UpdateCommand : CliktCommand(
    name = "update",
) {
    private val force by option("--force", help = "Rebuild even when the version is already installed")
        .flag()

    override fun help(context: Context): String =
        "Rebuild Melody Sync from source and reinstall it (requires a source checkout)"

    override fun run() {
        val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
        val service = InstallationService()

        val installed = service.detectInstallation()
        echo("Current:      ${VersionInfo.displayVersion}")
        echo("Installed:    ${installed?.let { "v${it.version}" } ?: "none"}")
        echo("Project:      ${projectDir.toAbsolutePath().normalize()}")
        echo()

        val result = service.update(
            projectDir = projectDir,
            build = "CLI",
            force = force,
            onProgress = { line -> echo("  $line") },
        )

        when {
            result.installed -> {
                echo("✓ ${result.message}")
                echo("Restart any running Melody Sync instance to use the new build.")
            }
            result.sourceBased -> {
                echo(result.message)
            }
            else -> {
                echo("✗ ${result.message}")
            }
        }

        echo()
        echo("Installation directory: ${InstallationPaths.installDir()}")
    }
}
