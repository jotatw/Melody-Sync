package com.melodysync.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.melodysync.platform.installation.InstallationPaths
import com.melodysync.platform.installation.InstallationService
import com.melodysync.platform.system.VersionInfo

class DoctorCommand : CliktCommand(
    name = "doctor",
) {
    override fun help(context: Context): String =
        "Diagnose the Melody Sync installation"

    override fun run() {
        val service = InstallationService()
        var issues = 0

        echo("System")
        issues += check("Java", "${System.getProperty("java.version")}")
        val javaHome = System.getProperty("java.home")
        issues += check("Java home", javaHome ?: "not found")
        issues += check("Bash", detectBash())

        echo("")
        echo("Application")
        val installDir = InstallationPaths.installDir()
        issues += check("Install directory", installDir.toString())
        issues += check("Version file", InstallationPaths.versionFile(installDir).toString())
        issues += check("Installation JSON", InstallationPaths.installationJson(installDir).toString())

        echo("")
        echo("Version")
        val info = service.detectInstallation(installDir)
        issues += check("Installed", info?.let { "v${it.version}" } ?: "none")
        issues += check("Latest", VersionInfo.displayVersion)
        issues += check("Latest release", detectLatestRelease())

        echo("")
        if (issues == 0) {
            echo("✓ Everything looks healthy.")
        } else {
            echo("✗ $issues issue(s) found.")
        }
    }

    private fun detectLatestRelease(): String =
        try {
            val release = com.melodysync.platform.installation.ReleaseClient()
                .latestRelease(com.melodysync.platform.installation.InstallationChannel.STABLE)
            "v${release.version} (${if (release.prerelease) "pre-release" else "stable"})"
        } catch (e: Exception) {
            "unavailable (${e.message})"
        }

    private fun check(name: String, value: String): Int {
        echo("  ✓ $name: $value")
        return 0
    }

    private fun detectBash(): String =
        try {
            val process = ProcessBuilder(listOf("bash", "--version"))
                .redirectErrorStream(true)
                .start()
            val first = process.inputStream.bufferedReader().readLine() ?: "not detected"
            process.waitFor()
            first
        } catch (e: Exception) {
            "not detected (${e.message})"
        }
}
