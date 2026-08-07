package com.melodysync.platform.installation

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

/**
 * Metadata describing a Melody Sync installation.
 *
 * Persisted as INSTALLATION.json next to the VERSION file. [installerVersion]
 * enables future migrations of this file's own format.
 */
@Serializable
data class InstallationInfo(
    val version: String,
    val installedAt: String,
    val java: String,
    val os: String,
    val build: String,
    val projectDir: String? = null,
    val installDir: String? = null,
    val sourceBased: Boolean = false,
    val installerVersion: Int = 2,
    val channel: String = "source",
) {
    companion object {
        private val json = Json {
            prettyPrint = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        fun save(info: InstallationInfo, file: Path) {
            Files.createDirectories(file.parent)
            Files.writeString(file, json.encodeToString(info))
        }

        fun load(file: Path): InstallationInfo? {
            if (!Files.exists(file)) return null
            return try {
                json.decodeFromString<InstallationInfo>(Files.readString(file))
            } catch (_: Exception) {
                null
            }
        }
    }
}
