# Music Library Domain

> Domain model documentation for Melody Sync.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Draft                  |
| Project Version  | v0.2.0-dev             |
| Last Updated     | 2026-07-31             |

---

## Overview

The music library domain consists of two primary models: **Song** and **LibraryStatistics**. These models represent the core data structures that flow through the entire application, from file discovery to metadata enrichment and statistics calculation.

---

## Song

A `Song` represents a single audio file in the user's music library. It holds file system information, extracted metadata, and technical audio properties.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `path` | `Path` | (required) | Full file system path to the audio file |
| `size` | `Long` | (required) | File size in bytes |
| `title` | `String?` | `null` | Track title from metadata |
| `artist` | `String?` | `null` | Artist name from metadata |
| `album` | `String?` | `null` | Album name from metadata |
| `duration` | `Double?` | `null` | Track duration in seconds |
| `bitrate` | `Int?` | `null` | Audio bitrate in bps |
| `sampleRate` | `Int?` | `null` | Audio sample rate in Hz |
| `channels` | `Int?` | `null` | Number of audio channels |
| `codec` | `String?` | `null` | Codec name (e.g., MPEG 1 Layer 3) |

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `filename` | `String` | File name with extension (from path) |
| `extension` | `String` | File extension without dot (e.g., "mp3") |
| `isLossless` | `Boolean` | `true` for flac, wav, aiff |
| `directory` | `Path` | Parent directory of the file |
| `sizeMb` | `Double` | Size in megabytes |
| `sizeGb` | `Double` | Size in gigabytes |
| `durationMinutes` | `Double` | Duration in minutes |
| `hasMetadata` | `Boolean` | `true` if both title and artist are present and non-blank |

### Validation

- Metadata fields are nullable because audio files may lack tags.
- `hasMetadata` checks for non-blank `title` and `artist` as the minimum viable metadata.
- The `extension` is extracted from the last dot in the filename, following the same rules defined in LAB-002.

---

## LibraryStatistics

A `LibraryStatistics` represents a summary of an entire music library scan, aggregating information from all songs.

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `totalSongs` | `Int` | (required) | Total number of songs in the library |
| `uniqueArtists` | `Int` | (required) | Number of distinct artists |
| `uniqueAlbums` | `Int` | (required) | Number of distinct albums |
| `totalSize` | `Long` | (required) | Total size of all audio files in bytes |
| `formats` | `Map<String, Int>` | (required) | Format distribution (e.g., `{"mp3": 80, "flac": 40}`) |
| `totalDuration` | `Double` | (required) | Total duration of all songs in seconds |
| `averageBitrate` | `Double` | (required) | Average bitrate across all songs in bps |

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `totalSizeMb` | `Double` | Total size in megabytes |
| `totalSizeGb` | `Double` | Total size in gigabytes |
| `totalDurationMinutes` | `Double` | Total duration in minutes |
| `totalDurationHours` | `Double` | Total duration in hours |
| `averageDuration` | `Double` | Average song duration in seconds (0.0 if empty) |
| `isEmpty` | `Boolean` | `true` if `totalSongs == 0` |
| `averageBitrateKbps` | `Double` | Average bitrate in kbps |

### Validation

- `averageDuration` returns `0.0` when the library is empty (avoids division by zero).
- Average bitrate in kbps is derived from bps by dividing by 1000.

---

## HealthReport

A `HealthReport` summarizes the health of a music library, produced by the `LibraryHealthService`.

| Field | Type | Description |
|-------|------|-------------|
| `directory` | `Path` | The analyzed directory |
| `totalFiles` | `Int` | All files found in the directory |
| `audioFiles` | `Int` | Supported audio files |
| `nonAudio` | `List<FileCategory>` | Non-audio files grouped by category |
| `unknownExtensions` | `List<String>` | Extensions not recognized by any category |
| `songsWithoutMetadata` | `List<Song>` | Songs missing title or artist |
| `songsWithZeroDuration` | `List<Song>` | Songs with null or zero duration |
| `orphanedEntries` | `List<String>` | Paths in the database that no longer exist on disk |

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `totalNonAudio` | `Int` | Sum of all non-audio file counts |
| `songsWithMetadataIssues` | `Int` | Total count of metadata problems (missing + zero duration + orphaned) |

### Design decisions

- The health check is **report-only**: it never modifies or deletes files. Actions are suggested to the user.
- Two analysis modes: `analyze()` (full disk scan, default) and `analyzeFromDatabase()` (fast, database-only).
- Database songs are filtered to those under the analyzed directory, so the report is coherent per directory.

---

## FileCategory

A `FileCategory` groups non-audio files by their purpose.

| Field | Type | Description |
|-------|------|-------------|
| `category` | `String` | Category key (e.g., "image", "subtitle", "lyrics") |
| `description` | `String` | Human-readable description |
| `extensions` | `Set<String>` | File extensions in this category |
| `count` | `Int` | Number of files |
| `totalSize` | `Long` | Total size in bytes |

### Known categories (`KNOWN_NON_AUDIO`)

| Category | Extensions | Description |
|----------|-----------|-------------|
| `image` | png, jpg, jpeg, gif, bmp, webp, svg | Cover art and images |
| `subtitle` | vtt, srt, ass, ssa, sub | Subtitle files |
| `lyrics` | lrc, txt | Lyrics and text files |
| `metadata` | meta, nfo, xml, cue | Metadata files |
| `playlist` | m3u, m3u8, pls, xspf | Playlist files |
| `video` | mp4, mkv, avi, webm, mov | Video files |

> `.lrc` (lyrics) and `.meta` (metadata) are classified here. Their *use* (e.g., displaying lyrics or reading extra metadata in the GUI) is a future feature, not part of the health check.

---

## Relationships

```
LibraryStatistics
    │
    └── aggregates → List<Song>
                        │
                        ├── file information (path, size)
                        ├── metadata tags (title, artist, album)
                        └── technical info (duration, bitrate, codec)

HealthReport
    ├── produced by LibraryHealthService
    ├── references → List<Song> (songs without metadata / zero duration)
    └── groups → List<FileCategory> (non-audio files)
```

The `Scanner` module produces a `List<Song>`, and the `StatisticsCalculator` consumes that list to produce a single `LibraryStatistics`. The database (SQLite via Exposed, ADR-0004) persists both individual songs and computed statistics. The `LibraryHealthService` combines a disk scan with database contents to produce a `HealthReport`.

---

## References

- ADR-0002 — Programming Language (Kotlin)
- ADR-0005 — Audio Metadata (JAudioTagger)
- `src/melody_sync/models/song.py` — Python reference implementation
- `melody-sync-core/src/main/kotlin/com/melodysync/model/Song.kt` — Current implementation
- `melody-sync-core/src/test/kotlin/com/melodysync/model/SongTest.kt` — Tests (8 passing)
- `melody-sync-core/src/test/kotlin/com/melodysync/model/LibraryStatisticsTest.kt` — Tests (7 passing)
- `melody-sync-core/src/main/kotlin/com/melodysync/model/HealthReport.kt` — Health report model
- `melody-sync-core/src/main/kotlin/com/melodysync/model/FileCategory.kt` — Non-audio file classification
- `melody-sync-core/src/main/kotlin/com/melodysync/service/LibraryHealthService.kt` — Health analysis service

---

This document follows the Melody Sync Documentation Standard.

**End of Document**