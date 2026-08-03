# Security and Resilience Guide

> Defensive coding guidelines for file management, sandboxing, and data integrity in Melody Sync.

---

## 1. Introduction

Melody Sync performs write operations directly on the user's local filesystem (renaming, organizing, and tagging files) and handles untrusted metadata from local files and third-party APIs (YouTube).

This guide outlines defensive coding standards to protect the user's data and system integrity across Linux, Windows, and Android.

---

## 2. Preventing Path Traversal Vulnerabilities

The most significant security risk in a local organizer is **Path Traversal**. This occurs when the application constructs file paths using untrusted strings (such as song tags like `Artist` or `Album`) without proper validation, allowing malicious files to escape the designated library root.

### 2.1 The Vulnerability Scenario
Suppose a user scans a file with the following ID3 tags:
* **Artist:** `../../../../etc`
* **Title:** `shadow`

If `LibraryOrganizationService` organizes this file using naive path concatenation:
```kotlin
// VULNERABLE CODE
val target = rootDir.resolve(artist).resolve("$title.mp3")
Files.move(source, target)
```
The target becomes: `/home/user/Music/../../../../etc/shadow.mp3` which translates to `/etc/shadow.mp3`. If the app runs with high privileges, this could overwrite system-critical configuration files.

### 2.2 Defensive Code Pattern (The Descendant Check)
To fully eliminate this risk, the application must **always normalize and verify** that the resolved target path is a strict descendant of the library's root directory.

#### Standard Implementation:
```kotlin
fun safeTargetPath(song: Song, root: Path): Path {
    val target = targetPath(song, root) ?: throw IllegalArgumentException("Invalid path structure")

    // Normalize both paths to resolve any ".." or "." segments
    val absoluteRoot = root.toAbsolutePath().normalize()
    val absoluteTarget = target.toAbsolutePath().normalize()

    // Verify that the target is strictly inside the root directory
    if (!absoluteTarget.startsWith(absoluteRoot)) {
        throw SecurityException(
            "Security violation: Target path '$absoluteTarget' escapes the library root '$absoluteRoot'!"
        )
    }

    return absoluteTarget
}
```

---

## 3. Cross-Platform Filename Sanitization

Each operating system has strict rules regarding which characters are allowed in filenames. Failing to sanitize tags before generating paths will cause crashes or data corruption.

### 3.1 Reserved Characters Map

| Platform | Forbidden Characters | Filename Length Limit |
|---|---|---|
| **Linux (ext4)** | `/`, `\0` (null byte) | 255 bytes |
| **Windows (NTFS)** | `/`, `\`, `:`, `*`, `?`, `"`, `<`, `>`, `|` | 255 characters |
| **Android (FAT32/ExFAT)**| `/`, `\`, `:`, `*`, `?`, `"`, `<`, `>`, `\|` | 255 characters |

### 3.2 Reserved Filenames (Windows Only)
On Windows, filenames cannot match reserved system device names, even if they have an extension (e.g., `CON.mp3`, `PRN.mp3`, `AUX.mp3`, `NUL.mp3`, `COM1.mp3`, `LPT1.mp3`).

### 3.3 Safe Sanitization Standard
When generating a filename from artist/album/title tags, apply this standard Kotlin sanitize function:

```kotlin
object FilenameSanitizer {
    private val FORBIDDEN_CHARS_REGEX = Regex("[/\\\\:*?\"<>|\\x00]")
    private val WINDOWS_RESERVED_NAMES = setOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    )

    fun sanitize(segment: String): String {
        val trimmed = segment.trim()
        if (trimmed.isEmpty()) return "Unknown"

        // 1. Replace forbidden characters with underscores
        var cleaned = trimmed.replace(FORBIDDEN_CHARS_REGEX, "_")

        // 2. Prevent Windows reserved names
        val baseName = cleaned.substringBefore('.').uppercase()
        if (baseName in WINDOWS_RESERVED_NAMES) {
            cleaned = "Prefix_$cleaned"
        }

        // 3. Prevent trailing dots or spaces (invalid in Windows/Android)
        while (cleaned.endsWith(".") || cleaned.endsWith(" ")) {
            cleaned = cleaned.dropLast(1)
        }

        return cleaned.ifBlank { "Unknown" }
    }
}
```

---

## 4. Secure API Credentials Management

To enable the YouTube metadata search feature, the app requires a Google API Key. Storing this key securely prevents API quota theft and key leakage.

### 4.1 Developer Guidelines
* **Never commit API Keys to GitHub:** Ensure no `.env`, `.properties`, or Kotlin source files containing hardcoded keys are tracked by git.
* **Environment-First Configuration:** Prioritize reading the key from the environment (`YOUTUBE_API_KEY`) or from a user-owned configuration file with strict permissions (e.g., `chmod 600 ~/.config/melody-sync/settings.properties`).

### 4.2 Key Isolation in Code
Isolate the API client from the key's storage mechanism. The client should simply ask for the key at instantiation:

```kotlin
class YouTubeClient(private val apiKeyProvider: () -> String) {
    fun search(query: String) {
        val key = apiKeyProvider()
        if (key.isBlank()) throw IllegalStateException("YouTube API key not configured.")
        // Proceed with request...
    }
}
```

---

## 5. Local Database Resilience (SQLite & Exposed)

SQLite is an exceptionally reliable embedded database. However, multi-threaded access (from GUI + File Watcher) or abrupt power failure can trigger "database is locked" errors or corrupt the journal.

### 5.1 Best Practices for Concurrency
1. **WAL Mode (Write-Ahead Logging):** Switch SQLite to WAL mode. This allows multiple concurrent readers to query the database while a background scan is writing data, completely eliminating 95% of lock contention issues.
   * *SQL execution during setup:* `PRAGMA journal_mode=WAL;`
2. **Busy Timeout:** Configure a busy timeout on the SQLite connection. If the database is busy, the driver will wait for the specified milliseconds instead of crashing immediately.
   * *SQL execution during setup:* `PRAGMA busy_timeout=5000;` (5 seconds timeout).
3. **Keep Transactions Short:** Never perform heavy I/O operations (like reading audio file tags from disk) inside an active database transaction. Read the tags first, and then persist them in a single fast, unified database transaction.
