package com.melodysync.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Owns the single database connection lifecycle (Phase D of the metadata
 * foundation).
 *
 * [connectUrl] is idempotent per URL: production code calls it repeatedly and
 * only the first call actually connects. Tests may switch to a different
 * database file, which reconnects. Writes are serialized through [withWriteLock]
 * so concurrent tag application / watcher resyncs cannot interleave.
 */
object DatabaseConnection {

    @Volatile
    private var currentUrl: String? = null

    private val writeLock = ReentrantLock()

    fun connectUrl(url: String) {
        val normalized = normalizeUrl(url)
        if (currentUrl == normalized) return
        synchronized(this) {
            if (currentUrl == normalized) return
            configurePragmas(normalized)
            Database.connect(normalized, driver = "org.sqlite.JDBC")
            transaction { SchemaUtils.create(SongsTable) }
            currentUrl = normalized
        }
    }

    fun connectToFile(databaseFile: Path) =
        connectUrl("jdbc:sqlite:${databaseFile.toAbsolutePath().normalize()}")

    fun connect() = connectUrl("jdbc:sqlite:${defaultDatabaseFile()}")

    /** Runs a write under the connection write lock. */
    fun <T> withWriteLock(block: () -> T): T = writeLock.withLock(block)

    fun currentDatabaseFile(): Path {
        val raw = currentUrl?.removePrefix("jdbc:sqlite:")
        return raw?.let { Path.of(it) } ?: Path.of(defaultDatabaseFile())
    }

    private fun normalizeUrl(url: String): String = when {
        url.startsWith("jdbc:sqlite:") -> {
            val path = url.removePrefix("jdbc:sqlite:")
            "jdbc:sqlite:${Path.of(path).toAbsolutePath().normalize()}"
        }
        else -> url
    }

    private fun configurePragmas(url: String) {
        DriverManager.getConnection(url).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute("PRAGMA journal_mode=WAL;")
                statement.execute("PRAGMA busy_timeout=5000;")
            }
        }
    }

    fun defaultDatabaseFile(): String {
        val home = System.getProperty("user.home") ?: "."
        val configDir = Path.of(home, ".config", "melody-sync")
        Files.createDirectories(configDir)
        return configDir.resolve("library.db").toString()
    }
}
