package com.melodysync.database

import com.melodysync.model.Song
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class MusicRepositoryTest {

    @TempDir
    lateinit var tmpDir: Path

    @BeforeEach
    fun setUp() {
        MusicDatabase.connectToFile(tmpDir.resolve("test.db"))
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(SongsTable)
        }
    }

    private fun sampleSong(): Song = Song(
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
    )

    @Test
    fun `inserts song and returns id`() {
        val id = MusicRepository.insert(sampleSong())

        assertTrue(id > 0)
        assertEquals(1L, MusicRepository.count())
    }

    @Test
    fun `inserts multiple songs`() {
        val songs = listOf(
            sampleSong(),
            sampleSong().copy(path = Path.of("/music/second.mp3")),
        )

        val ids = MusicRepository.insertAll(songs)

        assertEquals(2, ids.size)
        assertEquals(2L, MusicRepository.count())
    }

    @Test
    fun `finds all songs`() {
        MusicRepository.insert(sampleSong())
        MusicRepository.insert(sampleSong().copy(path = Path.of("/music/second.mp3")))

        val songs = MusicRepository.findAll()

        assertEquals(2, songs.size)
    }

    @Test
    fun `finds song by path`() {
        val expected = sampleSong()
        MusicRepository.insert(expected)

        val found = MusicRepository.findByPath(expected.path)

        assertNotNull(found)
        assertEquals(expected.title, found!!.title)
        assertEquals(expected.artist, found.artist)
        assertEquals(expected.album, found.album)
        assertEquals(expected.path, found.path)
    }

    @Test
    fun `returns null when path not found`() {
        val result = MusicRepository.findByPath(Path.of("/nonexistent/song.mp3"))
        assertNull(result)
    }

    @Test
    fun `deletes song by path`() {
        val song = sampleSong()
        MusicRepository.insert(song)

        val deleted = MusicRepository.deleteByPath(song.path)

        assertEquals(1, deleted)
        assertEquals(0L, MusicRepository.count())
    }

    @Test
    fun `handles song without metadata`() {
        val bare = Song(path = Path.of("/music/unknown.mp3"), size = 0L)
        MusicRepository.insert(bare)

        val found = MusicRepository.findByPath(bare.path)

        assertNotNull(found)
        assertNull(found!!.title)
        assertNull(found.artist)
        assertEquals(0L, found.size)
    }

    @Test
    fun `counts songs in empty database`() {
        assertEquals(0L, MusicRepository.count())
    }
}