package com.melodysync.service

import com.melodysync.model.Song
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

enum class ExportFormat {
    JSON,
    CSV,
}

@Serializable
data class SongExport(
    val path: String,
    val filename: String,
    val extension: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationSeconds: Double? = null,
    val durationMinutes: Double,
    val sizeBytes: Long,
    val sizeMb: Double,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
    val codec: String? = null,
    val isLossless: Boolean,
    val hasMetadata: Boolean,
)

object LibraryExportService {

    private val json = Json { prettyPrint = true }

    fun exportToJson(songs: List<Song>): String =
        json.encodeToString(songs.map(::toExport))

    fun exportToCsv(songs: List<Song>): String {
        val header = listOf(
            "path", "filename", "extension", "title", "artist", "album",
            "duration_seconds", "duration_minutes", "size_bytes", "size_mb",
            "bitrate", "sample_rate", "channels", "codec",
            "is_lossless", "has_metadata",
        )
        val rows = songs.map { song ->
            val e = toExport(song)
            listOf(
                e.path, e.filename, e.extension, e.title ?: "", e.artist ?: "", e.album ?: "",
                e.durationSeconds?.toString() ?: "", formatDouble(e.durationMinutes),
                e.sizeBytes.toString(), formatDouble(e.sizeMb),
                e.bitrate?.toString() ?: "", e.sampleRate?.toString() ?: "", e.channels?.toString() ?: "",
                e.codec ?: "",
                e.isLossless.toString(), e.hasMetadata.toString(),
            )
        }

        return buildString {
            append(header.joinToString(",")).append('\n')
            rows.forEach { row ->
                append(row.joinToString(",") { escapeCsv(it) }).append('\n')
            }
        }
    }

    fun write(songs: List<Song>, output: Path, format: ExportFormat) {
        val content = when (format) {
            ExportFormat.JSON -> exportToJson(songs)
            ExportFormat.CSV -> exportToCsv(songs)
        }
        Files.createDirectories(output.parent ?: Path.of("."))
        Files.writeString(output, content)
    }

    fun toExport(song: Song): SongExport = SongExport(
        path = song.path.toString(),
        filename = song.filename,
        extension = song.extension,
        title = song.title,
        artist = song.artist,
        album = song.album,
        durationSeconds = song.duration,
        durationMinutes = song.durationMinutes,
        sizeBytes = song.size,
        sizeMb = song.sizeMb,
        bitrate = song.bitrate,
        sampleRate = song.sampleRate,
        channels = song.channels,
        codec = song.codec,
        isLossless = song.isLossless,
        hasMetadata = song.hasMetadata,
    )

    private fun formatDouble(value: Double): String =
        String.format(Locale.ROOT, "%.2f", value)

    private fun escapeCsv(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
