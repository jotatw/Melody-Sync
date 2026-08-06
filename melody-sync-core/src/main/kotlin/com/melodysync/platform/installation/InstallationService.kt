package com.melodysync.platform.installation

import com.melodysync.platform.shell.ShellExecutor
import com.melodysync.platform.system.VersionInfo
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Orchestrates install/update/repair of a local Melody Sync installation.
 *
 * Validates the environment and source checkout, compares versions, runs
 * scripts/install.sh and records INSTALLATION.json. Decoupled from the GUI
 * and CLI so both can share the same flow. Built to later grow a
 * "release download" strategy behind the same interface.
 */
/**
 * Result of a version check that does not perform any rebuild.
 */
data class UpdateCheck(
    val sourceVersion: String?,
    val installedVersion: String?,
    val sourceBased: Boolean,
    val updateAvailable: Boolean,
    val message: String? = null,
)

class InstallationService(
    private val validator: InstallationValidator = InstallationValidator(),
    private val shell: ShellExecutor = ShellExecutor(),
) {

    fun detectInstallation(installDir: Path = InstallationPaths.installDir()): InstallationInfo? {
        val json = InstallationInfo.load(InstallationPaths.installationJson(installDir))
        if (json != null) return json

        val version = InstallationPaths.readInstalledVersion(installDir) ?: return null
        return InstallationInfo(
            version = version,
            installedAt = "",
            java = "",
            os = "",
            build = "",
        )
    }

    fun update(
        projectDir: Path,
        installDir: Path = InstallationPaths.installDir(),
        build: String = "Unknown",
        force: Boolean = false,
        onProgress: (String) -> Unit = {},
    ): InstallationResult {
        val environmentIssues = validator.validateEnvironment()
        if (environmentIssues.isNotEmpty()) {
            return InstallationResult(
                version = VersionInfo.version,
                installed = false,
                rebuilt = false,
                sourceBased = false,
                message = environmentIssues.joinToString("; ") { "${it.check}: ${it.message}" },
            )
        }

        val projectIssues = validator.validateProject(projectDir)
        if (projectIssues.isNotEmpty()) {
            return InstallationResult(
                version = VersionInfo.version,
                installed = false,
                rebuilt = false,
                sourceBased = false,
                message = "Melody Sync was not installed from source. " +
                    "Automatic rebuild is unavailable. Use the release installer instead. " +
                    projectIssues.joinToString("; ") { it.message },
            )
        }

        val sourceVersion = readSourceVersion(projectDir)
            ?: return InstallationResult(
                version = VersionInfo.version,
                installed = false,
                rebuilt = false,
                sourceBased = true,
                message = "melodySyncVersion not found in gradle.properties",
            )

        val installedVersion = InstallationPaths.readInstalledVersion(installDir)
        if (!force && installedVersion == sourceVersion) {
            return InstallationResult(
                version = sourceVersion,
                installed = false,
                rebuilt = false,
                sourceBased = true,
                message = "Already up to date (v$sourceVersion)",
            )
        }

        onProgress("Running scripts/install.sh…")
        val result = shell.run(
            command = listOf("bash", "scripts/install.sh"),
            workingDir = projectDir,
            onLine = onProgress,
        )

        if (!result.succeeded) {
            return InstallationResult(
                version = sourceVersion,
                installed = false,
                rebuilt = true,
                sourceBased = true,
                message = "Install failed: ${result.stderr.ifBlank { result.stdout }}".trim(),
            )
        }

        val resolvedInstallDir = installDir.toAbsolutePath().normalize()
        InstallationInfo.save(
            InstallationInfo(
                version = sourceVersion,
                installedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                java = System.getProperty("java.version") ?: "unknown",
                os = System.getProperty("os.name") ?: "unknown",
                build = build,
                projectDir = projectDir.toAbsolutePath().normalize().toString(),
                installDir = resolvedInstallDir.toString(),
                sourceBased = true,
            ),
            InstallationPaths.installationJson(resolvedInstallDir),
        )

        return InstallationResult(
            version = sourceVersion,
            installed = true,
            rebuilt = true,
            sourceBased = true,
            message = "Installed v$sourceVersion",
        )
    }

    fun checkForUpdate(
        projectDir: Path,
        installDir: Path = InstallationPaths.installDir(),
    ): UpdateCheck {
        val environmentIssues = validator.validateEnvironment()
        if (environmentIssues.isNotEmpty()) {
            return UpdateCheck(
                sourceVersion = null,
                installedVersion = null,
                sourceBased = false,
                updateAvailable = false,
                message = environmentIssues.joinToString("; ") { "${it.check}: ${it.message}" },
            )
        }

        val projectIssues = validator.validateProject(projectDir)
        if (projectIssues.isNotEmpty()) {
            return UpdateCheck(
                sourceVersion = null,
                installedVersion = null,
                sourceBased = false,
                updateAvailable = false,
                message = "Melody Sync was not installed from source. " +
                    "Automatic rebuild is unavailable. Use the release installer instead.",
            )
        }

        val sourceVersion = readSourceVersion(projectDir)
        val installedVersion = InstallationPaths.readInstalledVersion(installDir)
        return UpdateCheck(
            sourceVersion = sourceVersion,
            installedVersion = installedVersion,
            sourceBased = true,
            updateAvailable = sourceVersion != null && sourceVersion != installedVersion,
        )
    }

    private fun readSourceVersion(projectDir: Path): String? {
        val file = projectDir.resolve("gradle.properties")
        if (!Files.exists(file)) return null
        return try {
            Files.readAllLines(file)
                .firstOrNull { it.trimStart().startsWith("melodySyncVersion=") }
                ?.substringAfter("=")
                ?.trim()
                ?.ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}
