package com.melodysync.scanner

import com.melodysync.metadata.MetadataFormatRegistry
import com.melodysync.metadata.MetadataProvider
import com.melodysync.metadata.OpusProvider
import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.jaudiotagger.audio.AudioFileIO
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Result of inspecting an audio file for metadata read/write support.
 * Backs `melody-sync metadata [--write-test] <file>` (Step 0 of the metadata
 * foundation). Never modifies the original file: the write test runs on a
 * temporary copy.
 */
data class MetadataDiagnostic(
    val file: Path,
    val format: String,
    val provider: String,
    val readSupported: Boolean,
    val readOk: Boolean,
    val readReason: String? = null,
    val writeSupported: Boolean,
    val supportedFields: List<String>,
    val writeTest: WriteTestResult?,
) {
    data class WriteTestResult(
        val passed: Boolean,
        val reason: String? = null,
    )
}

/**
 * Step 0 diagnostic: reports which provider handles a format, whether the
 * file can be read, and (optionally) whether tags can actually be written —
 * tested safely on a copy.
 */
object MetadataDiagnosticService {

    private val supportedFields = listOf("title", "artist", "album")

    fun inspect(file: Path, runWriteTest: Boolean): MetadataDiagnostic {
        val format = file.fileName.toString().substringAfterLast('.', "").lowercase()
        val provider = MetadataFormatRegistry.providerFor(format)

        val read = if (provider != null) attemptRead(file, provider) else null
        val writeSupported = provider?.supportsWrite ?: false
        val writeTest = if (runWriteTest && writeSupported) runWriteTest(file) else null

        return MetadataDiagnostic(
            file = file,
            format = format,
            provider = provider?.id ?: "Unknown",
            readSupported = provider != null,
            readOk = read?.first ?: false,
            readReason = read?.second,
            writeSupported = writeSupported,
            supportedFields = supportedFields,
            writeTest = writeTest,
        )
    }

    private fun attemptRead(file: Path, provider: MetadataProvider): Pair<Boolean, String?> =
        when (provider) {
            OpusProvider -> {
                if (OpusMetadata.read(file) != null) {
                    true to null
                } else {
                    false to "Could not parse Ogg/Opus comment header"
                }
            }
            else -> try {
                AudioFileIO.read(file.toFile())
                true to null
            } catch (e: Exception) {
                false to (e.message ?: e.javaClass.simpleName)
            }
        }

    private fun runWriteTest(file: Path): MetadataDiagnostic.WriteTestResult {
        val tempDir = Files.createTempDirectory("melody-sync-write-test")
        return try {
            val copy = tempDir.resolve(file.fileName.toString())
            Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING)
            val song = Song(path = copy, size = Files.size(copy))
            TagWriter.writeTags(song, TagSuggestion(title = "Write Test", artist = "Melody Sync"))
            MetadataDiagnostic.WriteTestResult(passed = true)
        } catch (e: Exception) {
            MetadataDiagnostic.WriteTestResult(passed = false, reason = e.message ?: e.javaClass.simpleName)
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }
}
