package com.melodysync.desktop.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class AppPreferencesTest {

    @TempDir
    lateinit var tmpDir: Path

    private fun prefsFile(): Path = tmpDir.resolve("settings.properties")

    @Test
    fun `load returns defaults when file missing`() {
        val prefs = AppPreferences.load(prefsFile())

        assertEquals("", prefs.directory)
        assertEquals("system", prefs.theme)
        assertEquals("library", prefs.section)
        assertEquals("title", prefs.sortColumn)
        assertTrue(prefs.sortAscending)
    }

    @Test
    fun `save and load round trip`() {
        val original = AppPreferences(
            directory = "/home/user/Music",
            theme = "dark",
            section = "health",
            sortColumn = "artist",
            sortAscending = false,
        )

        original.save(prefsFile())
        val loaded = AppPreferences.load(prefsFile())

        assertEquals(original, loaded)
    }

    @Test
    fun `section and sort column mapping`() {
        assertEquals(Section.LIBRARY, AppState.sectionFromString("library"))
        assertEquals(Section.HEALTH, AppState.sectionFromString("health"))
        assertEquals(Section.LIBRARY, AppState.sectionFromString("invalid"))

        assertEquals(SortColumn.TITLE, AppState.sortColumnFromString("title"))
        assertEquals(SortColumn.DURATION, AppState.sortColumnFromString("duration"))
        assertEquals(SortColumn.TITLE, AppState.sortColumnFromString("bogus"))
    }

    @Test
    fun `save creates parent directories`() {
        val nested = tmpDir.resolve("a/b/c/settings.properties")
        AppPreferences(directory = "x").save(nested)

        assertTrue(java.nio.file.Files.exists(nested))
        assertTrue(java.nio.file.Files.exists(nested.parent))
    }
}
