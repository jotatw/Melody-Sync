package com.melodysync.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

enum class LibraryChange {
    SONG_ADDED,
    SONG_REMOVED,
    SONG_MODIFIED,
}

data class LibraryWatchEvent(
    val directory: Path,
    val change: LibraryChange,
    val path: Path?,
)

class LibraryWatcher(
    private val scope: CoroutineScope,
    private val debounceMillis: Long = 2000L,
) {
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
    private var job: Job? = null
    private val watchedKeys: MutableMap<WatchKey, Path> = mutableMapOf()
    private val eventBuffer: MutableList<LibraryWatchEvent> = mutableListOf()

    var isRunning: Boolean = false
        private set

    fun start(directory: Path, onEvents: (List<LibraryWatchEvent>) -> Unit) {
        if (isRunning) return
        registerTree(directory)

        job = scope.launch {
            isRunning = true
            while (isActive) {
                val key = watchService.take()
                val dir = watchedKeys[key]
                if (dir != null) {
                    processKey(key, dir)
                    debounceAndFlush(onEvents)
                }
                if (!key.reset()) {
                    watchedKeys.remove(key)
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        job?.cancel()
        job = null
        watchedKeys.keys.forEach { it.cancel() }
        watchedKeys.clear()
        eventBuffer.clear()
    }

    private suspend fun debounceAndFlush(onEvents: (List<LibraryWatchEvent>) -> Unit) {
        delay(debounceMillis)
        if (eventBuffer.isEmpty()) return
        val events = eventBuffer.toList()
        eventBuffer.clear()
        onEvents(events)
    }

    private fun processKey(key: WatchKey, dir: Path) {
        key.pollEvents().forEach { event ->
            handleEvent(event, dir)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleEvent(event: WatchEvent<*>, dir: Path) {
        val kind = event.kind() as WatchEvent.Kind<Path>
        val context = event.context() as? Path ?: return
        val fullPath = dir.resolve(context)

        when (kind) {
            StandardWatchEventKinds.ENTRY_CREATE -> {
                if (Files.isDirectory(fullPath)) {
                    registerTree(fullPath)
                }
                eventBuffer.add(LibraryWatchEvent(dir, LibraryChange.SONG_ADDED, fullPath))
            }
            StandardWatchEventKinds.ENTRY_DELETE -> {
                eventBuffer.add(LibraryWatchEvent(dir, LibraryChange.SONG_REMOVED, fullPath))
            }
            StandardWatchEventKinds.ENTRY_MODIFY -> {
                eventBuffer.add(LibraryWatchEvent(dir, LibraryChange.SONG_MODIFIED, fullPath))
            }
            else -> Unit
        }
    }

    private fun registerTree(root: Path) {
        if (!Files.isDirectory(root)) return
        Files.walk(root).use { paths ->
            paths.filter { Files.isDirectory(it) }.forEach { registerDir(it) }
        }
    }

    private fun registerDir(dir: Path) {
        try {
            val key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY,
            )
            watchedKeys[key] = dir
        } catch (_: Exception) {
            // Directory may have been removed; ignore
        }
    }
}
