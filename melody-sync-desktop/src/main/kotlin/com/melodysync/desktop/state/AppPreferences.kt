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
    val sidebarExpanded: Boolean = true,
    val visibleColumns: String = "title,artist,album,duration,format,bitrate",
    val groupByLetter: Boolean = false,
    val updateChannel: String = "stable",
    val autoUpdate: Boolean = false,
    val windowWidth: Double? = null,
    val windowHeight: Double? = null,
    val windowPositionX: Double? = null,
    val windowPositionY: Double? = null,
    val windowMaximized: Boolean = false,
) {
    fun save(file: Path = defaultFile()) {
        val props = Properties()
        props.setProperty("directory", directory)
        props.setProperty("theme", theme)
        props.setProperty("section", section)
        props.setProperty("sortColumn", sortColumn)
        props.setProperty("sortAscending", sortAscending.toString())
        props.setProperty("sidebarExpanded", sidebarExpanded.toString())
        props.setProperty("visibleColumns", visibleColumns)
        props.setProperty("groupByLetter", groupByLetter.toString())
        props.setProperty("updateChannel", updateChannel)
        props.setProperty("autoUpdate", autoUpdate.toString())
        windowWidth?.let { props.setProperty("windowWidth", it.toString()) }
        windowHeight?.let { props.setProperty("windowHeight", it.toString()) }
        windowPositionX?.let { props.setProperty("windowPositionX", it.toString()) }
        windowPositionY?.let { props.setProperty("windowPositionY", it.toString()) }
        props.setProperty("windowMaximized", windowMaximized.toString())

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
                    sidebarExpanded = props.getProperty("sidebarExpanded")?.toBoolean() ?: true,
                    visibleColumns = props.getProperty("visibleColumns")
                        ?: "title,artist,album,duration,format,bitrate",
                    groupByLetter = props.getProperty("groupByLetter")?.toBoolean() ?: false,
                    updateChannel = props.getProperty("updateChannel") ?: "stable",
                    autoUpdate = props.getProperty("autoUpdate")?.toBoolean() ?: false,
                    windowWidth = props.getProperty("windowWidth")?.toDoubleOrNull(),
                    windowHeight = props.getProperty("windowHeight")?.toDoubleOrNull(),
                    windowPositionX = props.getProperty("windowPositionX")?.toDoubleOrNull(),
                    windowPositionY = props.getProperty("windowPositionY")?.toDoubleOrNull(),
                    windowMaximized = props.getProperty("windowMaximized")?.toBoolean() ?: false,
                )
            } catch (_: Exception) {
                AppPreferences()
            }
        }
    }
}
