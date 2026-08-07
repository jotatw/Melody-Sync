package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.melodysync.platform.installation.InstallationChannel
import com.melodysync.platform.installation.InstallationPaths
import com.melodysync.platform.installation.InstallationService
import com.melodysync.platform.installation.InstallationValidator
import com.melodysync.platform.system.VersionInfo
import java.nio.file.Path

class UpdateCommand : CliktCommand(
    name = "update",
) {
    private val force by option("--force", help = "Install even when the version is already installed")
        .flag()

    private val channel by option(
        "--channel",
        help = "Update channel: stable, beta or nightly (used for release installs).",
    ).default("stable")

    private val installDir by option(
        "--install-dir",
        help = "Installation directory (defaults to ~/.local/share/melody-sync).",
    ).default("")

    override fun help(context: Context): String =
        "Update Melody Sync: rebuild from source (source checkout) or download the latest published release"

    override fun run() {
        val projectDir = Path.of(System.getProperty("user.dir") ?: ".")
        val targetInstallDir = if (installDir.isBlank()) {
            InstallationPaths.installDir()
        } else {
            Path.of(installDir)
        }
        val service = InstallationService()
        val validator = InstallationValidator()
        val sourceCheckout = validator.isSourceCheckout(projectDir)
        val selectedChannel = parseChannel(channel)

        val installed = service.detectInstallation(targetInstallDir)
        echo("Running:      ${VersionInfo.displayVersion}")
        echo("Installed:    ${installed?.let { "v${it.version}" } ?: "none"}")
        if (sourceCheckout) {
            echo("Mode:         source (rebuild from checkout)")
            echo("Project:      ${projectDir.toAbsolutePath().normalize()}")
        } else {
            echo("Mode:         release ($selectedChannel)")
        }
        echo()

        val result = if (sourceCheckout) {
            service.update(
                projectDir = projectDir,
                installDir = targetInstallDir,
                build = "CLI",
                force = force,
                onProgress = { line -> echo("  $line") },
            )
        } else {
            service.updateFromRelease(
                channel = selectedChannel,
                installDir = targetInstallDir,
                build = "CLI",
                force = force,
                onProgress = { line -> echo("  $line") },
            )
        }

        when {
            result.installed -> {
                echo("✓ ${result.message}")
                echo("Restart any running Melody Sync instance to use the new build.")
            }
            result.sourceBased -> echo(result.message)
            else -> echo("✗ ${result.message}")
        }

        echo()
        echo("Installation directory: $targetInstallDir")
    }

    private fun parseChannel(raw: String): InstallationChannel = when (raw.lowercase()) {
        "beta" -> InstallationChannel.BETA
        "nightly" -> InstallationChannel.NIGHTLY
        else -> InstallationChannel.STABLE
    }
}
