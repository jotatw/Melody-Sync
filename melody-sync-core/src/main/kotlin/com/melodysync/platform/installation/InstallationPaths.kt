package com.melodysync.platform.installation

import java.nio.file.Files
import java.nio.file.Path

/**
 * Resolves the well-known paths of a Melody Sync installation.
 *
 * Centralizing path resolution here keeps the rest of the platform layer
 * free of string concatenation and makes path handling unit-testable.
 */
object InstallationPaths {

    const val VERSION_FILE = "VERSION"
    const val INSTALLATION_JSON = "INSTALLATION.json"
    const val JAR_NAME = "melody-sync.jar"

    fun defaultInstallDir(): Path {
        val home = System.getProperty("user.home") ?: "."
        return Path.of(home, ".local", "share", "melody-sync")
    }

    fun defaultBinDir(): Path {
        val home = System.getProperty("user.home") ?: "."
        return Path.of(home, ".local", "bin")
    }

    fun defaultApplicationsDir(): Path {
        val home = System.getProperty("user.home") ?: "."
        return Path.of(home, ".local", "share", "applications")
    }

    fun installDir(overridden: Path? = null): Path =
        (overridden ?: defaultInstallDir()).toAbsolutePath().normalize()

    fun jarFile(installDir: Path): Path = installDir.resolve(JAR_NAME)

    fun versionFile(installDir: Path): Path = installDir.resolve(VERSION_FILE)

    fun installationJson(installDir: Path): Path = installDir.resolve(INSTALLATION_JSON)

    fun readInstalledVersion(installDir: Path): String? {
        val file = versionFile(installDir)
        if (!Files.exists(file)) return null
        return try {
            Files.readString(file).trim().ifBlank { null }
        } catch (_: Exception) {
            null
        }
    }
}
