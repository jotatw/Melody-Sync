package com.melodysync.service

import com.melodysync.model.Song
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class LibraryExportServiceTest {

    @TempDir
    lateinit var tmpDir: Path

    private fun sampleSongs(): List<Song> = listOf(
        Song(
            path = Path.of("/music/Queen - Bohemian Rhapsody.mp3"),
            size = 12_500_000L,
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            duration = 354.0,
            bitrate = 320000,
            sampleRate = 44100,
            channels = 2,
            codec = "MPEG-1 Layer 3",
        ),
        Song(
            path = Path.of("/music/Pink Floyd - Time.flac"),
            size = 45_000_000L,
            title = "Time",
            artist = "Pink Floyd",
            album = "The Dark Side of the Moon",
            duration = 412.0,
            bitrate = 1_000_000,
            sampleRate = 96000,
            channels = 2,
            codec = "FLAC",
        ),
    )

    @Test
    fun `exports valid json with all songs`() {
        val json = LibraryExportService.exportToJson(sampleSongs())

        val element = Json.parseToJsonElement(json) as JsonArray
        assertEquals(2, element.size)
        val first = element[0] as kotlinx.serialization.json.JsonObject
        assertEquals("Bohemian Rhapsody", first["title"]?.toString()?.trim('"'))
        assertEquals("Queen", first["artist"]?.toString()?.trim('"'))
        assertEquals("mp3", first["extension"]?.toString()?.trim('"'))
        assertTrue(first["isLossless"]?.toString() == "false")
    }

    @Test
    fun `exports csv with header and rows`() {
        val csv = LibraryExportService.exportToCsv(sampleSongs())

        val lines = csv.trim().split("\n")
        assertEquals(3, lines.size)
        assertTrue(lines[0].contains("path"))
        assertTrue(lines[0].contains("has_metadata"))
        assertTrue(lines[1].contains("Bohemian Rhapsody"))
        assertTrue(lines[1].contains("mp3"))
        assertTrue(lines[2].contains("Time"))
    }

    @Test
    fun `csv escapes fields with commas`() {
        val songs = listOf(
            Song(
                path = Path.of("/music/comma.mp3"),
                size = 100L,
                title = "Title, with comma",
                artist = "Artist \"Quoted\"",
            ),
        )

        val csv = LibraryExportService.exportToCsv(songs)

        assertTrue(csv.contains("\"Title, with comma\""))
        assertTrue(csv.contains("\"Artist \"\"Quoted\"\"\""))
    }

    @Test
    fun `writes json file to disk`() {
        val output = tmpDir.resolve("library.json")
        LibraryExportService.write(sampleSongs(), output, ExportFormat.JSON)

        assertTrue(Files.exists(output))
        val content = Files.readString(output)
        assertTrue(content.contains("Bohemian Rhapsody"))
    }

    @Test
    fun `writes csv file to disk`() {
        val output = tmpDir.resolve("library.csv")
        LibraryExportService.write(sampleSongs(), output, ExportFormat.CSV)

        assertTrue(Files.exists(output))
        val content = Files.readString(output)
        assertTrue(content.startsWith("path,filename"))
    }

    @Test
    fun `exports song with missing metadata`() {
        val song = Song(path = Path.of("/music/unknown.mp3"), size = 0L)
        val json = LibraryExportService.exportToJson(listOf(song))

        val element = Json.parseToJsonElement(json) as JsonArray
        val first = element[0] as kotlinx.serialization.json.JsonObject
        assertTrue(first["hasMetadata"]?.toString() == "false")
    }
}
