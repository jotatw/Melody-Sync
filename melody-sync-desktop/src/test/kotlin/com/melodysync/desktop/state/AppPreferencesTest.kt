package com.melodysync.desktop.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import com.melodysync.platform.installation.InstallationChannel
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
        assertFalse(prefs.groupByLetter)
        assertEquals("stable", prefs.updateChannel)
        assertFalse(prefs.autoUpdate)
    }

    @Test
    fun `save and load round trip`() {
        val original = AppPreferences(
            directory = "/home/user/Music",
            theme = "dark",
            section = "health",
            sortColumn = "artist",
            sortAscending = false,
            visibleColumns = "title,artist,bitrate",
            groupByLetter = true,
            updateChannel = "beta",
            autoUpdate = true,
            windowWidth = 1280.0,
            windowHeight = 800.0,
            windowPositionX = 40.0,
            windowPositionY = 25.0,
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

        assertEquals(SongField.TITLE, AppState.sortColumnFromString("title"))
        assertEquals(SongField.DURATION, AppState.sortColumnFromString("duration"))
        assertEquals(SongField.BITRATE, AppState.sortColumnFromString("bitrate"))
        assertEquals(SongField.TITLE, AppState.sortColumnFromString("bogus"))
    }

    @Test
    fun `parse columns from preferences`() {
        val columns = AppState.parseColumns("title,artist,format,bitrate")

        assertEquals(setOf(SongField.TITLE, SongField.ARTIST, SongField.FORMAT, SongField.BITRATE), columns)
    }

    @Test
    fun `update channel mapping`() {
        assertEquals(InstallationChannel.STABLE, AppState.channelFromString("stable"))
        assertEquals(InstallationChannel.BETA, AppState.channelFromString("beta"))
        assertEquals(InstallationChannel.NIGHTLY, AppState.channelFromString("nightly"))
        assertEquals(InstallationChannel.STABLE, AppState.channelFromString("bogus"))
    }

    @Test
    fun `parse columns handles invalid and blank`() {
        assertEquals(setOf(SongField.TITLE), AppState.parseColumns("title,bogus"))
        assertEquals(SongField.entries.toSet(), AppState.parseColumns(""))
    }

    @Test
    fun `save creates parent directories`() {
        val nested = tmpDir.resolve("a/b/c/settings.properties")
        AppPreferences(directory = "x").save(nested)

        assertTrue(java.nio.file.Files.exists(nested))
        assertTrue(java.nio.file.Files.exists(nested.parent))
    }
}
