package com.melodysync.database

import java.nio.file.Path

/**
 * Facade over [DatabaseConnection] kept for callers that reference
 * `MusicDatabase.connect(...)`. All connection lifecycle is owned by
 * [DatabaseConnection].
 */
object MusicDatabase {

    fun connect(url: String = "jdbc:sqlite:${defaultDatabaseFile()}") =
        DatabaseConnection.connectUrl(url)

    fun connectToFile(databaseFile: Path) =
        DatabaseConnection.connectToFile(databaseFile)

    fun defaultDatabaseFile(): String = DatabaseConnection.defaultDatabaseFile()
}
