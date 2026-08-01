package com.melodysync.service

import com.melodysync.model.Song
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class LibraryOrganizationServiceTest {

    @TempDir
    lateinit var tmpDir: Path

    @TempDir
    lateinit var outsideDir: Path

    private fun songAt(
        name: String,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        inRoot: Boolean = true,
    ): Song {
        val dir = if (inRoot) tmpDir else outsideDir
        return Song(
            path = dir.resolve(name),
            size = 100L,
            title = title,
            artist = artist,
            album = album,
        )
    }

    @Test
    fun `plans move for song with full metadata`() {
        val song = songAt("song.mp3", "Song Title", "Queen", "A Night at the Opera")

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(1, report.plannedMoves.size)
        val move = report.plannedMoves[0]
        assertEquals(tmpDir.resolve("Queen/A Night at the Opera/Song Title.mp3"), move.to)
        assertEquals("needs move", move.reason)
    }

    @Test
    fun `uses Unknown Artist when artist missing`() {
        val song = songAt("song.mp3", "Song Title", null, "Album")

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(tmpDir.resolve("Unknown Artist/Album/Song Title.mp3"), report.plannedMoves[0].to)
    }

    @Test
    fun `uses filename as title when title missing`() {
        val song = songAt("mystery.mp3", null, "Artist", "Album")

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(tmpDir.resolve("Artist/Album/mystery.mp3"), report.plannedMoves[0].to)
    }

    @Test
    fun `song without album goes directly under artist`() {
        val song = songAt("song.mp3", "Song Title", "Artist", null)

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(tmpDir.resolve("Artist/Song Title.mp3"), report.plannedMoves[0].to)
    }

    @Test
    fun `sanitizes invalid filename characters`() {
        val song = songAt("song.mp3", "Song / With: * Bad?", "Art/ist", "Al:bum")

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(tmpDir.resolve("Art_ist/Al_bum/Song _ With_ _ Bad_.mp3"), report.plannedMoves[0].to)
    }

    @Test
    fun `song outside root is ignored`() {
        val song = songAt("song.mp3", "Song", "Artist", "Album", inRoot = false)

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertTrue(report.plannedMoves.isEmpty())
    }

    @Test
    fun `reorganize moves files to organized folders`() {
        val source = tmpDir.resolve("song.mp3")
        Files.writeString(source, "content")
        val song = Song(path = source, size = 7L, title = "Song Title", artist = "Queen", album = "Album")

        val report = LibraryOrganizationService.reorganize(listOf(song), tmpDir)

        assertEquals(1, report.moved)
        assertTrue(Files.exists(tmpDir.resolve("Queen/Album/Song Title.mp3")))
        assertTrue(!Files.exists(source))
    }

    @Test
    fun `reorganize skips already organized songs`() {
        val target = tmpDir.resolve("Artist/Album/song.mp3")
        Files.createDirectories(target.parent)
        Files.writeString(target, "content")
        val song = Song(path = target, size = 7L, title = "song", artist = "Artist", album = "Album")

        val report = LibraryOrganizationService.reorganize(listOf(song), tmpDir)

        assertEquals(0, report.moved)
        assertEquals(1, report.skipped)
        assertEquals(1, report.alreadyOrganized)
    }

    @Test
    fun `resolves name collisions with numeric suffix`() {
        val songs = listOf(
            songAt("a.mp3", "Same Title", "Artist", "Album"),
            songAt("b.mp3", "Same Title", "Artist", "Album"),
        )

        val report = LibraryOrganizationService.planOrganization(songs, tmpDir)

        assertEquals(2, report.plannedMoves.size)
        assertEquals(tmpDir.resolve("Artist/Album/Same Title.mp3"), report.plannedMoves[0].to)
        assertEquals(tmpDir.resolve("Artist/Album/Same Title (2).mp3"), report.plannedMoves[1].to)
    }

    @Test
    fun `avoids overwriting existing file on disk`() {
        val target = tmpDir.resolve("Artist/Album/Same Title.mp3")
        Files.createDirectories(target.parent)
        Files.writeString(target, "existing")
        val song = songAt("a.mp3", "Same Title", "Artist", "Album")

        val report = LibraryOrganizationService.planOrganization(listOf(song), tmpDir)

        assertEquals(tmpDir.resolve("Artist/Album/Same Title (2).mp3"), report.plannedMoves[0].to)
    }
}
