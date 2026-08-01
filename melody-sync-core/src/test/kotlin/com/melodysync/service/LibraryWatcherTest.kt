package com.melodysync.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class LibraryWatcherTest {

    @TempDir
    lateinit var tmpDir: Path

    private fun watcherScope(): CoroutineScope =
        CoroutineScope(Dispatchers.IO)

    private fun await(flag: AtomicBoolean): Unit = runBlocking {
        var tries = 0
        while (!flag.get() && tries < 100) {
            delay(100)
            tries++
        }
    }

    @Test
    @Timeout(15)
    fun `detects file creation in watched directory`() {
        val scope = watcherScope()
        val received = AtomicReference<List<LibraryWatchEvent>>()
        val done = AtomicBoolean(false)
        val watcher = LibraryWatcher(scope, debounceMillis = 300)

        watcher.start(tmpDir) { events ->
            received.set(events)
            done.set(true)
        }

        Files.writeString(tmpDir.resolve("new.mp3"), "content")
        await(done)
        watcher.stop()

        assertTrue(done.get(), "watcher should have received events")
        val events = received.get()
        assertTrue(events != null && events.isNotEmpty())
        assertTrue(events.any { it.change == LibraryChange.SONG_ADDED })
    }

    @Test
    @Timeout(15)
    fun `detects file deletion`() {
        val file = tmpDir.resolve("song.mp3")
        Files.writeString(file, "content")

        val scope = watcherScope()
        val received = AtomicReference<List<LibraryWatchEvent>>()
        val done = AtomicBoolean(false)
        val watcher = LibraryWatcher(scope, debounceMillis = 300)

        watcher.start(tmpDir) { events ->
            received.set(events)
            done.set(true)
        }

        Files.delete(file)
        await(done)
        watcher.stop()

        assertTrue(done.get())
        val events = received.get()
        assertTrue(events.any { it.change == LibraryChange.SONG_REMOVED })
    }

    @Test
    @Timeout(15)
    fun `registers nested directories`() {
        val nested = tmpDir.resolve("Sub").also { Files.createDirectories(it) }

        val scope = watcherScope()
        val received = AtomicReference<List<LibraryWatchEvent>>()
        val done = AtomicBoolean(false)
        val watcher = LibraryWatcher(scope, debounceMillis = 300)

        watcher.start(tmpDir) { events ->
            received.set(events)
            done.set(true)
        }

        Files.writeString(nested.resolve("inner.mp3"), "content")
        await(done)
        watcher.stop()

        assertTrue(done.get())
        val events = received.get()
        assertTrue(events.any { it.path?.toString()?.contains("inner.mp3") == true })
    }
}
