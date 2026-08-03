package com.melodysync.desktop.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppStateSelectionTest {

    private fun state() = AppState(
        uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
    )

    @Test
    fun `selectSong updates selectedSongPath`() {
        val state = state()

        assertNull(state.selectedSongPath)

        state.selectSong("/music/a.mp3")
        assertEquals("/music/a.mp3", state.selectedSongPath)

        state.selectSong("/music/b.flac")
        assertEquals("/music/b.flac", state.selectedSongPath)

        state.selectSong(null)
        assertNull(state.selectedSongPath)
    }

    @Test
    fun `toggleDuplicateSelection adds and removes paths`() {
        val state = state()

        state.toggleDuplicateSelection("/music/dup-a.mp3")
        assertTrue("/music/dup-a.mp3" in state.duplicateTrashSelection)

        state.toggleDuplicateSelection("/music/dup-b.mp3")
        assertEquals(2, state.duplicateTrashSelection.size)

        state.toggleDuplicateSelection("/music/dup-a.mp3")
        assertEquals(setOf("/music/dup-b.mp3"), state.duplicateTrashSelection)
    }
}
