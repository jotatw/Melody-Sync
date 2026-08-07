package com.melodysync.platform.installation

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipFile

/**
 * Installs a [ReleaseInfo] into the Melody Sync install directory:
 * downloads the jar to a temp file, verifies the sha256 (falling back to
 * zip integrity when no checksum is published), swaps it in atomically with
 * a .bak rollback and records VERSION + INSTALLATION.json.
 */
class ReleaseInstaller(
    private val client: ReleaseClient = ReleaseClient(),
    private val binDir: Path = InstallationPaths.defaultBinDir(),
    private val applicationsDir: Path = InstallationPaths.defaultApplicationsDir(),
) {

    fun install(
        release: ReleaseInfo,
        installDir: Path,
        channel: InstallationChannel,
        build: String = "Desktop",
        onProgress: (String) -> Unit = {},
    ): InstallationResult {
        val resolvedDir = installDir.toAbsolutePath().normalize()
        val tempDir = Files.createTempDirectory("melody-sync-update")
        val tempJar = tempDir.resolve("melody-sync-$TEMPFILE.jar")
        val jarFile = InstallationPaths.jarFile(resolvedDir)

        try {
            onProgress("Downloading v${release.version}…")
            client.downloadJar(release.jarUrl, tempJar)

            onProgress("Verifying checksum…")
            verify(tempJar, release.sha256)

            Files.createDirectories(resolvedDir)
            val backup = resolvedDir.resolve("melody-sync.jar.bak")
            Files.deleteIfExists(backup)
            if (Files.exists(jarFile)) {
                Files.move(jarFile, backup, StandardCopyOption.REPLACE_EXISTING)
            }

            onProgress("Installing…")
            try {
                Files.move(tempJar, jarFile, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                if (Files.exists(backup)) {
                    Files.move(backup, jarFile, StandardCopyOption.REPLACE_EXISTING)
                }
                throw e
            }
            Files.deleteIfExists(backup)

            Files.writeString(InstallationPaths.versionFile(resolvedDir), "${release.version}\n")
            InstallationInfo.save(
                InstallationInfo(
                    version = release.version,
                    installedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    java = System.getProperty("java.version") ?: "unknown",
                    os = System.getProperty("os.name") ?: "unknown",
                    build = build,
                    projectDir = null,
                    installDir = resolvedDir.toString(),
                    sourceBased = false,
                    installerVersion = 2,
                    channel = channel.name.lowercase(),
                ),
                InstallationPaths.installationJson(resolvedDir),
            )
            writeLaunchArtifacts(resolvedDir)

            return InstallationResult(
                version = release.version,
                installed = true,
                rebuilt = false,
                sourceBased = false,
                message = "Installed v${release.version}",
            )
        } catch (e: Exception) {
            return InstallationResult(
                version = release.version,
                installed = false,
                rebuilt = false,
                sourceBased = false,
                message = "Update failed: ${e.message}",
            )
        } finally {
            runCatching { Files.deleteIfExists(tempJar) }
            runCatching { Files.deleteIfExists(tempDir) }
        }
    }

    /**
     * Creates the launcher script inside the install dir, a `melody-sync`
     * symlink in the user bin dir and a desktop entry, mirroring install.sh
     * so a release install is fully launchable.
     */
    private fun writeLaunchArtifacts(installDir: Path) {
        val launcher = installDir.resolve("melody-sync")
        Files.writeString(
            launcher,
            "#!/bin/sh\n" +
                "DIR=\"\$(dirname \"\$(readlink -f \"\$0\")\")\"\n" +
                "exec java -jar \"\$DIR/melody-sync.jar\" \"\$@\"\n",
        )
        launcher.toFile().setExecutable(true, false)

        Files.createDirectories(binDir)
        val link = binDir.resolve("melody-sync")
        Files.deleteIfExists(link)
        Files.createSymbolicLink(link, launcher)

        Files.createDirectories(applicationsDir)
        Files.writeString(
            applicationsDir.resolve("melody-sync.desktop"),
            "[Desktop Entry]\n" +
                "Version=1.0\n" +
                "Name=Melody Sync\n" +
                "Comment=Organize, analyze and explore your local music library\n" +
                "Exec=$launcher\n" +
                "Terminal=false\n" +
                "Type=Application\n" +
                "Categories=Audio;Music;\n" +
                "Keywords=music;library;organizer;metadata;\n",
        )
    }

    private fun verify(jar: Path, expectedSha256: String?) {
        if (expectedSha256 != null && expectedSha256.isNotBlank()) {
            val actual = sha256(jar)
            val expected = expectedSha256.trim().lowercase()
            if (!actual.equals(expected, ignoreCase = true)) {
                throw IllegalStateException("sha256 mismatch (expected $expected, got $actual)")
            }
            return
        }
        // No published checksum: at least confirm the file is a valid zip/jar.
        ZipFile(jar.toFile()).use { }
    }

    private fun sha256(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(file).use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val TEMPFILE = "release"
    }
}
