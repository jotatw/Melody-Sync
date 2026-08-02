package com.melodysync.desktop.state

import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

data class AppPreferences(
    val directory: String = "",
    val theme: String = "system",
    val section: String = "library",
    val sortColumn: String = "title",
    val sortAscending: Boolean = true,
) {
    fun save(file: Path = defaultFile()) {
        val props = Properties()
        props.setProperty("directory", directory)
        props.setProperty("theme", theme)
        props.setProperty("section", section)
        props.setProperty("sortColumn", sortColumn)
        props.setProperty("sortAscending", sortAscending.toString())

        Files.createDirectories(file.parent)
        Files.newOutputStream(file).use { props.store(it, "Melody Sync preferences") }
    }

    companion object {
        fun defaultFile(): Path {
            val home = System.getProperty("user.home") ?: "."
            return Path.of(home, ".config", "melody-sync", "settings.properties")
        }

        fun load(file: Path = defaultFile()): AppPreferences {
            if (!Files.exists(file)) return AppPreferences()

            return try {
                val props = Properties()
                Files.newInputStream(file).use { props.load(it) }

                AppPreferences(
                    directory = props.getProperty("directory") ?: "",
                    theme = props.getProperty("theme") ?: "system",
                    section = props.getProperty("section") ?: "library",
                    sortColumn = props.getProperty("sortColumn") ?: "title",
                    sortAscending = props.getProperty("sortAscending")?.toBoolean() ?: true,
                )
            } catch (_: Exception) {
                AppPreferences()
            }
        }
    }
}
