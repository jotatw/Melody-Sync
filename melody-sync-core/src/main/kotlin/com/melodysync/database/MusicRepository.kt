package com.melodysync.database

import com.melodysync.model.Song
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.nio.file.Path

object MusicRepository {

    /** Serializes database writes through the single connection write lock. */
    private fun <T> write(block: org.jetbrains.exposed.sql.Transaction.() -> T): T =
        DatabaseConnection.withWriteLock { transaction { block() } }

    fun insert(song: Song): Long = write {
        SongsTable.insert {
            it[path] = song.path.toString()
            it[size] = song.size
            it[title] = song.title
            it[artist] = song.artist
            it[album] = song.album
            it[duration] = song.duration
            it[bitrate] = song.bitrate
            it[sampleRate] = song.sampleRate
            it[channels] = song.channels
            it[codec] = song.codec
        } get SongsTable.id
    }

    fun insertAll(songs: List<Song>): List<Long> = write {
        songs.map { song ->
            SongsTable.insert {
                it[path] = song.path.toString()
                it[size] = song.size
                it[title] = song.title
                it[artist] = song.artist
                it[album] = song.album
                it[duration] = song.duration
                it[bitrate] = song.bitrate
                it[sampleRate] = song.sampleRate
                it[channels] = song.channels
                it[codec] = song.codec
            } get SongsTable.id
        }
    }

    /**
     * Inserts new songs and updates existing ones in a single transaction.
     * Existing paths are provided by the caller (read outside the transaction),
     * so metadata reading never blocks the database.
     */
    fun upsertAll(songs: List<Song>, existingPaths: Set<Path>): Pair<Int, Int> = write {
        var added = 0
        var updated = 0
        songs.forEach { song ->
            if (song.path in existingPaths) {
                SongsTable.update({ SongsTable.path eq song.path.toString() }) {
                    it[size] = song.size
                    it[title] = song.title
                    it[artist] = song.artist
                    it[album] = song.album
                    it[duration] = song.duration
                    it[bitrate] = song.bitrate
                    it[sampleRate] = song.sampleRate
                    it[channels] = song.channels
                    it[codec] = song.codec
                }
                updated++
            } else {
                SongsTable.insert {
                    it[path] = song.path.toString()
                    it[size] = song.size
                    it[title] = song.title
                    it[artist] = song.artist
                    it[album] = song.album
                    it[duration] = song.duration
                    it[bitrate] = song.bitrate
                    it[sampleRate] = song.sampleRate
                    it[channels] = song.channels
                    it[codec] = song.codec
                }
                added++
            }
        }
        added to updated
    }

    /** Deletes the given paths in a single transaction. */
    fun deleteAllByPath(paths: Set<Path>): Int = write {
        paths.sumOf { path ->
            SongsTable.deleteWhere { SongsTable.path eq path.toString() }
        }
    }

    fun findAll(): List<Song> = transaction {
        SongsTable.selectAll().map(::toSong)
    }

    fun findByPath(path: Path): Song? = transaction {
        SongsTable.selectAll()
            .where { SongsTable.path eq path.toString() }
            .map(::toSong)
            .firstOrNull()
    }

    fun exists(path: Path): Boolean = transaction {
        SongsTable.selectAll()
            .where { SongsTable.path eq path.toString() }
            .any()
    }

    fun updateByPath(song: Song): Int = write {
        SongsTable.update({ SongsTable.path eq song.path.toString() }) {
            it[size] = song.size
            it[title] = song.title
            it[artist] = song.artist
            it[album] = song.album
            it[duration] = song.duration
            it[bitrate] = song.bitrate
            it[sampleRate] = song.sampleRate
            it[channels] = song.channels
            it[codec] = song.codec
        }
    }

    fun deleteByPath(path: Path): Int = write {
        SongsTable.deleteWhere { SongsTable.path eq path.toString() }
    }

    fun deleteAll(): Int = write {
        SongsTable.deleteAll()
    }

    fun count(): Long = transaction {
        SongsTable.selectAll().count().toLong()
    }

    private fun toSong(row: ResultRow): Song = Song(
        path = Path.of(row[SongsTable.path]),
        size = row[SongsTable.size],
        title = row[SongsTable.title],
        artist = row[SongsTable.artist],
        album = row[SongsTable.album],
        duration = row[SongsTable.duration],
        bitrate = row[SongsTable.bitrate],
        sampleRate = row[SongsTable.sampleRate],
        channels = row[SongsTable.channels],
        codec = row[SongsTable.codec],
    )
}
