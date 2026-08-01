# Melody Sync

> A personal tool to organize, analyze and explore your local music library.

Melody Sync is a **personal project** — not a commercial product, not a Spotify competitor.

I built it because I listen to music from YouTube and my library is a mess: `.mp3`, `.flac`, `.mp4`, `.png`, `.vtt` and `.txt` all mixed together. Existing tools like MusicBrainz Picard, Kid3, LRCGET and Strawberry each solve one piece of the puzzle, but none of them offer a *cohesive* workflow for cleaning up and organizing a library from start to finish.

So I decided to build the tool I wanted. One place to scan, analyze and enrich my music library — with the option to identify songs and fetch metadata via the YouTube API in the future.

The project is also a way to practice software engineering: architecture before implementation, documentation as code, and incremental evolution.

---

## Project Status

| Item | Status |
|------|--------|
| Version | **v0.3.0-dev** |
| Language | **Kotlin** (JVM 21) |
| Core (scan, metadata, statistics) | ✅ Migrated from Python |
| CLI (`melody-sync scan`) | ✅ Working |
| Automated tests | 🎉 **73 passing** |
| Database (SQLite) | ✅ Working |
| GUI (Desktop) | ⏳ Planned |

---

## Features

### Implemented

- ✅ Audio file discovery (`.mp3`, `.flac`, `.m4a`, `.ogg`, `.opus`, `.wav`, `.aac`)
- ✅ Metadata extraction (title, artist, album, duration, bitrate, sample rate, channels, codec)
- ✅ Library statistics (total songs, unique artists/albums, size, duration, formats, avg bitrate)
- ✅ SQLite database — persistent metadata cache (`~/.config/melody-sync/library.db`)
- ✅ Library sync — scan a folder and persist/update/remove songs in the database
- ✅ CLI — `melody-sync scan <directory>`
- ✅ Cross-format detection (uppercase extensions, nested directories)
- ✅ 73 automated tests with real audio fixtures

### In Progress / Planned

- ⏳ YouTube API integration — enrich songs with metadata, covers and lyrics
- ⏳ Library health check (missing metadata, orphan files)
- ⏳ Graphical User Interface (Compose Desktop)
- ⏳ File organization (auto-sort into `Artist/Album/` structure)
- ⏳ Duplicate detection

---

## Architecture

```
melody-sync-core/          Business logic
├── model/                 Domain objects: Song, LibraryStatistics
├── scanner/               Discovery, Metadata, Scanner, Statistics
├── database/              SongsTable, MusicDatabase, MusicRepository
└── service/               LibrarySyncService (scanner + database)

melody-sync-cli/           Command-line interface (clikt)
└── cli/                   ScanCommand, VersionCommand

melody-sync-desktop/       Desktop GUI (Compose Desktop) — future
```

Data flow:

```
Directory
    │
    ▼
 discover()          → list of audio files
    │
    ▼
 readMetadata()      → enriched Song objects
    │
    ▼
 scan()              → orchestration
    │
    ▼
 LibrarySyncService  → persist to SQLite (insert/update/remove)
    │
    ▼
 calculateStatistics() → LibraryStatistics
    │
    ▼
 CLI / GUI           → display
```

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | **Kotlin** 2.4.10 (JVM 21) |
| Build | **Gradle** 9.6.1 (Kotlin DSL) |
| Audio metadata | **JAudioTagger** 2.2.7 |
| CLI framework | **clikt** 5.1.0 |
| GUI (future) | **Compose Desktop** 1.11.1 |
| Database | **SQLite** via **Exposed** 0.61.0 |
| Testing | **JUnit 5** (73 tests, real audio fixtures) |

---

## Requirements

- JDK 21+ (JVM target 21; JDK 25+ also works)
- Linux (primary; Windows/macOS possible but not tested)

---

## Quick Start

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync

# Build everything
./gradlew build

# Run the CLI
./gradlew :melody-sync-cli:run --args="scan /path/to/music"

# Or use the wrapper after build
./gradlew :melody-sync-cli:installDist
./melody-sync-cli/build/install/melody-sync-cli/bin/melody-sync-cli scan /path/to/music

# Run tests
./gradlew test

# Run specific module tests
./gradlew :melody-sync-core:test
./gradlew :melody-sync-cli:test
```

---

## Testing

**73 tests, all passing:**

| Module | Tests | Area |
|--------|-------|------|
| `core` | 69 | Models, Discovery, Metadata, Scanner, Statistics, Database, Sync |
| `cli` | 4 | Version command, Scan command, edge cases |

```bash
./gradlew test
```

Test fixtures include real `.mp3` files with and without metadata tags.

---

## Documentation

Project documentation lives in the `docs/` directory. Key documents:

| Document | Purpose |
|----------|---------|
| `INDEX.md` | Documentation entry point |
| `architecture/ADR/ADR-0001` | Project Vision |
| `architecture/ADR/ADR-0002` | Programming Language (Kotlin) |
| `architecture/ADR/ADR-0003` | Desktop GUI (Compose Desktop) |
| `architecture/ADR/ADR-0004` | Database (SQLite via Exposed) |
| `architecture/ADR/ADR-0005` | Audio Metadata (JAudioTagger) |
| `architecture/ADR/ADR-0007` | CLI Framework (clikt) |
| `architecture/ADR/ADR-0008` | Build System (Gradle Kotlin DSL) |
| `architecture/music-library-domain.md` | Domain model specification |

---

## Roadmap

### Milestone 1 — Core MVP ✅
- [x] Song model
- [x] LibraryStatistics model
- [x] Discovery (file scanning)
- [x] Metadata extraction
- [x] Scanner pipeline
- [x] Statistics calculation
- [x] 55 tests

### Milestone 2 — CLI ✅
- [x] `melody-sync scan <directory>` command
- [x] `melody-sync version` command
- [x] 4 CLI tests

### Milestone 3 — Database & Sync ✅
- [x] SQLite database (Exposed) for persistent metadata cache
- [x] Library sync (scan → insert/update/remove in DB)
- [ ] YouTube API metadata enrichment (covers, lyrics)
- [ ] Library health check (missing tags, orphan files)

### Milestone 4 — Enrichment & GUI ⏳
- [ ] YouTube API integration
- [ ] Library health check
- [ ] Desktop GUI (Compose Desktop)
- [ ] File organization (Artist/Album structure)
- [ ] Duplicate detection
- [ ] Export tools

---

## Contributing

This is a **personal project** built for my own music library. It is not open for general contributions at this stage.

That said, if you have ideas or suggestions, feel free to open an issue or discuss in the repository.

---

## License

MIT License — see [LICENSE](LICENSE).

---

## Why not just use existing tools?

I tried several Linux music library tools before starting this project:

- **MusicBrainz Picard** — excellent for tagging and audio fingerprinting, but confusing for beginners and doesn't handle library organization beyond tags.
- **Kid3** — a powerful tag editor, but purely technical — no library overview, no statistics.
- **LRCGET** — fetches lyrics only, nothing else.
- **Strawberry** — a great music player, but its organization features are scattered and not cohesive.

Each tool solves one problem well. None solves the *whole* problem in a simple, integrated way.

Melody Sync is my attempt to bring the pieces together in one place — not to compete with any of these tools, but to fill the gap between them for my own use.