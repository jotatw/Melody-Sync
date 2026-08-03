package com.melodysync.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager

object MusicDatabase {

    fun connect(url: String = "jdbc:sqlite:${defaultDatabaseFile()}") {
        configurePragmas(url)
        Database.connect(url, driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(SongsTable)
        }
    }

    fun connectToFile(databaseFile: Path) {
        Files.createDirectories(databaseFile.parent)
        val url = "jdbc:sqlite:$databaseFile"
        configurePragmas(url)
        Database.connect(url, driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(SongsTable)
        }
    }

    private fun configurePragmas(url: String) {
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA journal_mode=WAL;")
                statement.execute("PRAGMA busy_timeout=5000;")
            }
        }
    }

    private fun defaultDatabaseFile(): String {
        val home = System.getProperty("user.home") ?: "."
        val configDir = Path.of(home, ".config", "melody-sync")
        Files.createDirectories(configDir)
        return configDir.resolve("library.db").toString()
    }
}
