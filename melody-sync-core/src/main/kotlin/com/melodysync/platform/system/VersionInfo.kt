package com.melodysync.platform.system

import java.util.Properties

/**
 * Single runtime access point for the Melody Sync version.
 *
 * Reads the [melody-sync-version.properties] resource generated at build
 * time from `gradle.properties` (melodySyncVersion). Desktop, CLI, Doctor
 * and Settings all use the same API. Falls back to "dev" when the resource
 * is missing (e.g. running from an IDE without a build).
 */
object VersionInfo {

    private const val RESOURCE = "melody-sync-version.properties"

    private val cachedVersion: String? by lazy { loadFromResource() }

    val version: String
        get() = cachedVersion ?: "dev"

    val displayVersion: String
        get() = if (version.startsWith("v")) version else "v$version"

    private fun loadFromResource(): String? {
        return try {
            val stream = VersionInfo::class.java.classLoader.getResourceAsStream(RESOURCE)
                ?: return null
            stream.use { input ->
                val props = Properties()
                props.load(input)
                props.getProperty("version")
            }
        } catch (_: Exception) {
            null
        }
    }
}
