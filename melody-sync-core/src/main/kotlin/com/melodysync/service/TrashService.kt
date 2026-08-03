package com.melodysync.service

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Moves files to the platform trash instead of deleting them.
 *
 * Implements the freedesktop.org XDG Trash spec (files + trashinfo records)
 * so files remain recoverable from the system trash. On non-Linux systems
 * the caller can point [trashRoot] elsewhere.
 */
object TrashService {

    private val deletionDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun moveToTrash(path: Path, trashRoot: Path = defaultTrashRoot()): Path {
        if (!Files.exists(path)) throw IOException("File not found: $path")
        if (!Files.isRegularFile(path)) throw IOException("Not a regular file: $path")

        val filesDir = trashRoot.resolve("files")
        val infoDir = trashRoot.resolve("info")
        Files.createDirectories(filesDir)
        Files.createDirectories(infoDir)

        val originalName = path.fileName?.toString()
            ?: throw IOException("No file name: $path")
        val (trashName, trashInfoName) = uniqueNames(filesDir, infoDir, originalName)

        val trashFile = filesDir.resolve(trashName)
        val trashInfo = infoDir.resolve(trashInfoName)

        Files.move(path, trashFile, StandardCopyOption.REPLACE_EXISTING)
        try {
            Files.writeString(
                trashInfo,
                trashInfoContent(path, trashName),
                StandardCharsets.UTF_8,
            )
        } catch (e: Exception) {
            Files.deleteIfExists(trashFile)
            throw e
        }
        return trashFile
    }

    fun defaultTrashRoot(): Path {
        val xdg = System.getenv("XDG_DATA_HOME")
        val base = if (!xdg.isNullOrBlank()) {
            Path.of(xdg)
        } else {
            Path.of(System.getProperty("user.home"), ".local", "share")
        }
        return base.resolve("Trash")
    }

    private fun uniqueNames(filesDir: Path, infoDir: Path, originalName: String): Pair<String, String> {
        var candidate = originalName
        var counter = 1
        while (
            Files.exists(filesDir.resolve(candidate)) ||
            Files.exists(infoDir.resolve("$candidate.trashinfo"))
        ) {
            val dot = originalName.lastIndexOf('.')
            candidate = if (dot > 0) {
                originalName.substring(0, dot) + ".$counter" + originalName.substring(dot)
            } else {
                "$originalName.$counter"
            }
            counter++
        }
        return candidate to "$candidate.trashinfo"
    }

    private fun trashInfoContent(original: Path, trashName: String): String {
        val encodedPath = encode(original.toAbsolutePath().normalize().toString())
        val deletionDate = LocalDateTime.now().format(deletionDateFormatter)
        return "[Trash Info]\nPath=$encodedPath\nDeletionDate=$deletionDate\n"
    }

    private fun encode(value: String): String {
        val sb = StringBuilder()
        for (byte in value.toByteArray(StandardCharsets.UTF_8)) {
            val c = byte.toInt() and 0xFF
            val ch = c.toChar()
            when {
                ch == '/' -> sb.append('/')
                ch in 'a'..'z' || ch in 'A'..'Z' || ch in '0'..'9' ||
                    ch == '-' || ch == '_' || ch == '.' -> sb.append(ch)
                else -> sb.append('%').append(String.format("%02X", c))
            }
        }
        return sb.toString()
    }
}
