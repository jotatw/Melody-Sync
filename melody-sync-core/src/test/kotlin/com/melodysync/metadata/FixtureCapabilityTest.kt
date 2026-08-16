package com.melodysync.metadata

import com.melodysync.model.Song
import com.melodysync.scanner.MetadataDiagnosticService
import com.melodysync.scanner.readMetadata
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Phase E: verifies every format fixture against the supported capability
 * matrix. The matrix is based on measured behavior (read + safe write test on
 * a copy), not assumptions.
 */
class FixtureCapabilityTest {

    private val fixtures: Path =
        Path.of(javaClass.getResource("/fixtures/audio")!!.toURI())

    private val matrix: Map<String, Pair<Boolean, Boolean>> = mapOf(
        "mp3" to (true to true),
        "m4a" to (true to true),
        "flac" to (true to true),
        "wav" to (true to false),
        "ogg" to (true to true),
        "opus" to (true to true),
        // aac has no JAudioTagger reader: not supported for read or write.
        "aac" to (false to false),
    )

    @Test
    fun `verified capabilities match the supported matrix`() {
        Files.list(fixtures).use { dirs ->
            dirs.filter { Files.isDirectory(it) }.forEach { dir ->
                val format = dir.fileName.toString()
                val (expectRead, expectWrite) = matrix[format]
                    ?: error("no capability expectation for $format")
                Files.list(dir).use { files ->
                    files.filter { Files.isRegularFile(it) }.forEach { file ->
                        val diagnostic = MetadataDiagnosticService.inspect(file, runWriteTest = true)
                        assertEquals(expectRead, diagnostic.readOk, "$format read")
                        val actualWrite = diagnostic.writeTest?.passed ?: false
                        assertEquals(expectWrite, actualWrite, "$format write test")
                    }
                }
            }
        }
    }

    @Test
    fun `tagged fixtures expose title artist and album`() {
        listOf("mp3", "m4a", "flac", "wav", "ogg", "opus").forEach { format ->
            val song = readMetadata(Song(path = fixtures.resolve("$format/with_tags.$format"), size = 0))
            assertEquals("Fixture Song", song.title, "$format title")
            assertEquals("Fixture Artist", song.artist, "$format artist")
            assertEquals("Fixture Album", song.album, "$format album")
        }
    }

    @Test
    fun `untagged fixtures fall back to the file name with no artist or album`() {
        listOf("mp3", "m4a", "flac", "wav", "ogg", "opus").forEach { format ->
            val song = readMetadata(Song(path = fixtures.resolve("$format/no_tags.$format"), size = 0))
            assertEquals("no_tags.$format", song.title, "$format untagged title")
            assertTrue(song.artist == null, "$format untagged artist")
            assertTrue(song.album == null, "$format untagged album")
        }
    }
}
