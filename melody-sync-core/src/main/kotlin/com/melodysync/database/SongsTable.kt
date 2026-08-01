package com.melodysync.database

import org.jetbrains.exposed.sql.Table

object SongsTable : Table("songs") {
    val id = long("id").autoIncrement()
    val path = varchar("path", 4096).uniqueIndex()
    val size = long("size")
    val title = varchar("title", 1024).nullable()
    val artist = varchar("artist", 1024).nullable()
    val album = varchar("album", 1024).nullable()
    val duration = double("duration").nullable()
    val bitrate = integer("bitrate").nullable()
    val sampleRate = integer("sample_rate").nullable()
    val channels = integer("channels").nullable()
    val codec = varchar("codec", 128).nullable()

    override val primaryKey = PrimaryKey(id)
}
