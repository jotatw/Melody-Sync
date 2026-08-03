package com.melodysync.desktop.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
}
