package com.melodysync.scanner

import com.melodysync.model.Song
import com.melodysync.model.TagSuggestion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class TagWriterTest {

    @TempDir
    lateinit var tmp: Path

    private fun copyFixture(name: String, targetName: String): Path {
        val source = Path.of(javaClass.getResource("/fixtures/audio/$name")!!.toURI())
        val target = tmp.resolve(targetName)
        Files.copy(source, target)
        return target
    }

    @Test
    fun `writes title artist and album and re-reads them`() {
        val path = copyFixture("with_tags.mp3", "song.mp3")
        val original = Song(path = path, size = Files.size(path))

        val updated = TagWriter.writeTags(
            original,
            TagSuggestion(title = "New Title", artist = "New Artist", album = "New Album"),
        )

        assertEquals("New Title", updated.title)
        assertEquals("New Artist", updated.artist)
        assertEquals("New Album", updated.album)
    }

    @Test
    fun `writes to a file without existing tags`() {
        val path = copyFixture("no_tags.mp3", "untagged.mp3")
        val song = Song(path = path, size = Files.size(path))

        val updated = TagWriter.writeTags(song, TagSuggestion(title = "Title", artist = "Artist"))

        assertEquals("Title", updated.title)
        assertEquals("Artist", updated.artist)
    }

    @Test
    fun `empty suggestion leaves the file untouched`() {
        val path = copyFixture("with_tags.mp3", "song.mp3")
        val original = Song(path = path, size = Files.size(path), title = "Original")

        val updated = TagWriter.writeTags(original, TagSuggestion())

        assertEquals("Original", updated.title)
    }
}
