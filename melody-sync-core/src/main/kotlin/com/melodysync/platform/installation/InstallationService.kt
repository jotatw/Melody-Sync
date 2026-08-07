package com.melodysync.platform.installation

import com.melodysync.platform.shell.ShellExecutor
import com.melodysync.platform.system.VersionComparator
import com.melodysync.platform.system.VersionInfo
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Result of a version check that does not perform any rebuild or download.
 */
data class UpdateCheck(
    val availableVersion: String?,
    val installedVersion: String?,
    val sourceBased: Boolean,
    val updateAvailable: Boolean,
    val message: String? = null,
)

/**
 * Orchestrates install/update of a local Melody Sync installation.
 *
 * Two strategies behind the same interface:
 * - source mode ([update], [checkForUpdate]): rebuilds from a local source
 *   checkout via scripts/install.sh.
 * - release mode ([updateFromRelease], [checkForReleaseUpdate]): downloads
 *   a published jar from GitHub Releases.
 * Both record VERSION + INSTALLATION.json so the installed version is always
 * recognized afterwards.
 */
class InstallationService(
    private val validator: InstallationValidator = InstallationValidator(),
    private val shell: ShellExecutor = ShellExecutor(),
    private val releaseClient: ReleaseClient = ReleaseClient(),
    private val releaseInstaller: ReleaseInstaller = ReleaseInstaller(releaseClient),
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

    // ------------------------------------------------------------------ source

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
                availableVersion = null,
                installedVersion = null,
                sourceBased = false,
                updateAvailable = false,
                message = environmentIssues.joinToString("; ") { "${it.check}: ${it.message}" },
            )
        }

        val projectIssues = validator.validateProject(projectDir)
        if (projectIssues.isNotEmpty()) {
            return UpdateCheck(
                availableVersion = null,
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
            availableVersion = sourceVersion,
            installedVersion = installedVersion,
            sourceBased = true,
            updateAvailable = VersionComparator.isNewer(sourceVersion, installedVersion),
        )
    }

    // ------------------------------------------------------------------ release

    fun checkForReleaseUpdate(
        channel: InstallationChannel = InstallationChannel.STABLE,
        installDir: Path = InstallationPaths.installDir(),
    ): UpdateCheck {
        val installedVersion = InstallationPaths.readInstalledVersion(installDir)
        val release = try {
            releaseClient.latestRelease(channel)
        } catch (e: Exception) {
            return UpdateCheck(
                availableVersion = null,
                installedVersion = installedVersion,
                sourceBased = false,
                updateAvailable = false,
                message = "Release check failed: ${e.message}",
            )
        }
        return UpdateCheck(
            availableVersion = release.version,
            installedVersion = installedVersion,
            sourceBased = false,
            updateAvailable = VersionComparator.isNewer(release.version, installedVersion),
        )
    }

    fun updateFromRelease(
        channel: InstallationChannel = InstallationChannel.STABLE,
        installDir: Path = InstallationPaths.installDir(),
        build: String = "Desktop",
        force: Boolean = false,
        onProgress: (String) -> Unit = {},
    ): InstallationResult {
        val release = try {
            releaseClient.latestRelease(channel)
        } catch (e: Exception) {
            return InstallationResult(
                version = VersionInfo.version,
                installed = false,
                rebuilt = false,
                sourceBased = false,
                message = "Update failed: ${e.message}",
            )
        }

        val installedVersion = InstallationPaths.readInstalledVersion(installDir)
        if (!force && !VersionComparator.isNewer(release.version, installedVersion)) {
            return InstallationResult(
                version = release.version,
                installed = false,
                rebuilt = false,
                sourceBased = false,
                message = "Already up to date (v${installedVersion ?: release.version})",
            )
        }

        return releaseInstaller.install(
            release = release,
            installDir = installDir,
            channel = channel,
            build = build,
            onProgress = onProgress,
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
