package com.melodysync.desktop.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.melodysync.desktop.state.AppState
import com.melodysync.desktop.state.Section

/**
 * Global keyboard shortcuts for the main window.
 *
 * - Ctrl+1..8 — switch section (Library, Statistics, Health, Review,
 *   Duplicates, Organize, Settings, About)
 * - Ctrl+B — toggle the sidebar
 * - F5 — rescan the library
 * - Esc — clear the song selection (close the Quick Fix panel)
 */
fun Modifier.keyboardShortcuts(state: AppState): Modifier =
    this.onPreviewKeyEvent { event ->
        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
        when {
            event.isCtrlPressed && event.key == Key.B -> {
                state.toggleSidebar()
                true
            }

            event.isCtrlPressed && event.key in sectionShortcuts -> {
                state.setSection(sectionShortcuts.getValue(event.key))
                true
            }

            event.key == Key.F5 -> {
                state.scan()
                true
            }

            event.key == Key.Escape -> {
                if (state.selectedSongPath != null) {
                    state.selectSong(null)
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }

private val sectionShortcuts: Map<Key, Section> = mapOf(
    Key.One to Section.LIBRARY,
    Key.Two to Section.STATISTICS,
    Key.Three to Section.HEALTH,
    Key.Four to Section.REVIEW,
    Key.Five to Section.DUPLICATES,
    Key.Six to Section.ORGANIZE,
    Key.Seven to Section.SETTINGS,
    Key.Eight to Section.ABOUT,
)