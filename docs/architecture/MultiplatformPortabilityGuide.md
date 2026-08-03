# Multiplatform Portability Guide

> Strategic architecture guide for porting Melody Sync to Windows and Android.

---

## 1. Introduction & Context

Melody Sync began as a personal Linux-first music manager. However, its modular architecture makes it a prime candidate for expansion into a multiplatform ecosystem (covering **Linux, Windows, and Android**).

By structuring our transition strategy now, we ensure that current implementations do not introduce technical debt that would block future ports.

---

## 2. Target Architecture (Kotlin Multiplatform)

To support Windows and Android seamlessly, the project should migrate from a pure JVM multi-module structure to a **Kotlin Multiplatform (KMP)** architecture.

```text
                        ┌─────────────────────────────────┐
                        │     melody-sync-core            │
                        │     (Kotlin Multiplatform)      │
                        │                                 │
                        │     commonMain (Pure Logic)     │
                        │     jvmMain (JAudioTagger, NIO) │
                        │     androidMain (Media APIs)    │
                        └────────────────┬────────────────┘
                                         │
                 ┌───────────────────────┼───────────────────────┐
                 ▼                       ▼                       ▼
    ┌─────────────────────────┐  ┌────────────────────────┐  ┌───────────────────────┐
    │   melody-sync-cli       │  │  melody-sync-desktop   │  │  melody-sync-android  │
    │   (JVM / Native)        │  │  (Compose Desktop)     │  │  (Jetpack Compose)    │
    │   - Linux, Windows, macOS│  │  - Linux, Windows, macOS│  │  - Android App        │
    └─────────────────────────┘  └────────────────────────┘  └───────────────────────┘
```

### 2.1 Modular Responsibilities
1. **`commonMain` (The Core Engine):** Contains our domain models (`Song`, `LibraryStatistics`), core duplicate matching algorithms, health analysis structures, and YouTube enrichment logic. It is completely independent of Java or Android platforms.
2. **Platform-Specific Layers (`expect` / `actual`):** Used to abstract behaviors that differ between JVM (Desktop) and Android, such as:
   * **File System Operations** (reading directories, moving files).
   * **Audio Tag Reading** (metadata extraction).
   * **Local Database Storage** (SQLite driver configuration).

---

## 3. Storage & File System Portability

File system access differs dramatically between Desktop systems (Linux/Windows) and Mobile systems (Android).

### 3.1 Desktop (Linux & Windows) — Java NIO
On Desktop, we use `java.nio.file.Path` and `java.nio.file.Files`.
* **Compatibility:** Fully compatible with both Linux and Windows out of the box.
* **Path Differences:**
  * Linux uses forward slashes (e.g., `/home/user/Music`).
  * Windows uses backward slashes and drive letters (e.g., `C:\Users\User\Music`).
* **Heuristic:** Always use Java NIO's path resolution (`Path.resolve`, `Path.normalize`) and avoid hardcoded string concatenations with `/` or `\`.

### 3.2 Mobile (Android) — Storage Access Framework (SAF)
On Android 10 (API 29) and above, direct filesystem paths are highly restricted due to **Scoped Storage**.
* **The Android Challenge:** A music app cannot scan `/storage/emulated/0/Music` directly using standard file APIs unless it asks for the highly-restricted `MANAGE_EXTERNAL_STORAGE` permission (which Google Play rarely approves for non-utility file managers).
* **The Solution:** Use Android's **Storage Access Framework (SAF)** and the `ContentResolver` / `DocumentFile` APIs.
* **Migration Strategy:**
  * Define an abstract `FileDevice` interface in `commonMain`:
    ```kotlin
    interface FileDevice {
        fun listFiles(root: String): List<SongFile>
        fun moveFile(from: String, to: String)
    }
    ```
  * In `jvmMain`, implement it using Java NIO.
  * In `androidMain`, implement it using Android `DocumentFile` (Content URIs).

---

## 4. Audio Metadata Portability

Currently, `melody-sync-core` relies on **JAudioTagger**, which is a JVM-only Java library.

| Platform | Metadata Reader Strategy |
|---|---|
| **Desktop (JVM)** | Continue using **JAudioTagger** via `actual` implementation. It is highly optimized for flac, mp3, and m4a tags. |
| **Android** | Use Android's native **`MediaMetadataRetriever`** or the androidx **`Media3 Exoplayer MetadataExtractor`**, reducing app size and utilizing native hardware-accelerated OS decoders. |

* **Abstraction Pattern:**
  ```kotlin
  // In commonMain
  expect class AudioMetadataReader {
      fun readMetadata(file: SongFile): SongMetadata
  }
  ```

---

## 5. Database Portability: Exposed vs SQLDelight

Our current ORM is JetBrains **Exposed**, which is an excellent JVM-specific SQL library. However, Exposed relies heavily on JDBC drivers, which do not run natively on Android or non-JVM platforms.

### 5.1 Recommendation: SQLDelight or Room Multiplatform
To make the database layer 100% portable:
1. **SQLDelight:** Highly recommended for KMP. It compiles raw SQL queries into type-safe Kotlin APIs. It has native driver implementations for:
   * **JVM (Desktop):** `JdbcSqliteDriver` (uses the same SQLite library we use now).
   * **Android:** `AndroidSqliteDriver` (uses the Android OS built-in SQLite database, reducing app size).
2. **Room Multiplatform:** Recently released by Google, supporting both Android and JVM Desktop natively.

---

## 6. UI/UX Portability (Compose Multiplatform)

Since both Desktop and Android layers use **Jetpack Compose**, we can achieve nearly 90% UI code reuse.

### 6.1 Layout Adaptation Guidelines
* **Responsive Layouts:** Ensure screens use `BoxWithConstraints` or responsive grids. When screen width is `< 600dp` (Android Portrait), the navigation bar should transform from a **Navigation Rail** (Sidebar) into a **NavigationBar** (Bottom Bar).
* **Tactile Targets:** Since Android is touch-based, keep touchable elements (buttons, row items) at a minimum height of **48dp** (as defined by our Design System).

---

## 7. Strategic Action Plan

When the time comes to execute the multiplatform expansion, follow this incremental sequence:

```text
Step 1: Abstract Filesystem & Database Interfaces
                 │
                 ▼
Step 2: Replace Exposed with SQLDelight (Multiplatform SQLite)
                 │
                 ▼
Step 3: Convert core module to a Kotlin Multiplatform (KMP) module
                 │
                 ▼
Step 4: Create the melody-sync-android module (Jetpack Compose Mobile)
                 │
                 ▼
Step 5: Implement expect/actual metadata reading for Android (Media3)
```

By keeping these steps in mind during our Linux development, we write clean, abstracted code today that can scale to millions of devices tomorrow.
