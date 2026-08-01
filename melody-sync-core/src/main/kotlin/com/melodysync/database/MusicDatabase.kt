package com.melodysync.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path

object MusicDatabase {

    fun connect(url: String = "jdbc:sqlite:${defaultDatabaseFile()}") {
        Database.connect(url, driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(SongsTable)
        }
    }

    fun connectToFile(databaseFile: Path) {
        Files.createDirectories(databaseFile.parent)
        Database.connect("jdbc:sqlite:$databaseFile", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(SongsTable)
        }
    }

    private fun defaultDatabaseFile(): String {
        val home = System.getProperty("user.home") ?: "."
        val configDir = Path.of(home, ".config", "melody-sync")
        Files.createDirectories(configDir)
        return configDir.resolve("library.db").toString()
    }
}
