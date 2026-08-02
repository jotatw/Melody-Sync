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
| Version | **v0.11.0-dev** |
| Language | **Kotlin** (JVM 21) |
| Core (scan, metadata, statistics) | ✅ Migrated from Python |
| CLI (`melody-sync scan` / `health` / `duplicates` / `organize` / `export` / `enrich`) | ✅ Working |
| Automated tests | 🎉 **133 passing** |
| Database (SQLite) | ✅ Working |
| GUI (Desktop) | ✅ Working (Compose, sidebar) |
| Library health check | ✅ Working |
| Duplicate detection | ✅ Working |
| File watcher (auto re-sync) | ✅ Working |
| Folder organization | ✅ Working |
| Export (JSON/CSV) | ✅ Working |
| YouTube enrichment | ✅ Working (needs API key) |
| Installation (Fedora) | ✅ Script (`scripts/install.sh`) |

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
- ✅ Desktop GUI — scan, browse, search and filter songs (Compose Desktop, Material 3, dark/light toggle)
- ✅ Library health check — classify non-audio files, detect missing metadata, zero duration and orphaned entries (CLI + GUI)
- ✅ Duplicate detection — group songs by normalized title/artist and similar duration (CLI + GUI, report-only)
- ✅ File watcher — automatic re-sync when files change (GUI toggle, debounced)
- ✅ Folder organization — plan `Artist/Album/` structure, apply with `--apply` (report-first, never automatic)
- ✅ Export — library metadata to JSON or CSV (CLI)
- ✅ YouTube enrichment — search candidates for songs missing metadata (CLI, report-only)
- ✅ Sidebar navigation (Library, Statistics, Health, Duplicates, Organize)
- ✅ Sortable song list with A–Z letter index
- ✅ Persistent preferences (directory, theme, section, sort)
- ✅ One-command installation on Fedora (`scripts/install.sh`)
- ✅ System theme detection (KDE Plasma + GNOME)
- ✅ 133 automated tests with real audio fixtures

### In Progress / Planned

- ⏳ YouTube enrichment: auto-apply metadata to songs (currently report-only)
- ⏳ Cover art and lyrics fetching from matched videos
- ⏳ AppImage / RPM packaging for easier distribution (currently install script)

---

## Architecture

```
melody-sync-core/          Business logic
├── model/                 Domain objects: Song, LibraryStatistics, HealthReport, FileCategory, DuplicateGroup, OrganizationReport, YouTubeVideoResult
├── scanner/               Discovery, Metadata, Scanner, Statistics
├── database/              SongsTable, MusicDatabase, MusicRepository
└── service/               LibrarySyncService, LibraryHealthService, DuplicateDetectionService, LibraryWatcher, LibraryOrganizationService, LibraryExportService, YouTubeSearchService, SongEnrichmentService

melody-sync-cli/           Command-line interface (clikt)
└── cli/                   ScanCommand, VersionCommand

melody-sync-desktop/       Desktop GUI (Compose Desktop)
├── desktop/               Main.kt (window, theme state)
├── theme/                 AppTheme (light/dark, system detection)
├── state/                 AppState (state holder)
└── ui/                    LibraryScreen + components
    └── components/        TopBar, DirectoryBar, SearchBar, SongList
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
| GUI | **Compose Desktop** 1.11.1 |
| Database | **SQLite** via **Exposed** 0.61.0 |
| Testing | **JUnit 5** (133 tests, real audio fixtures) |

---

## Requirements

- JDK 21+ (JVM target 21; JDK 25+ also works)
- Linux (primary; Windows/macOS possible but not tested)

---

## Installation (Fedora / Linux)

Install the desktop GUI with a single command (builds, installs to `~/.local/share/melody-sync`, creates a launcher in `~/.local/bin` and a menu entry):

```bash
./scripts/install.sh
```

Then run `melody-sync` (or launch from your app menu). Uninstall with `./scripts/uninstall.sh` (keeps your `~/.config/melody-sync/library.db`).

The install script builds a self-contained JAR and requires Java 21+ at runtime.

---

## Quick Start

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync

# Build everything
./gradlew build

# Run the CLI
./gradlew :melody-sync-cli:run --args="scan /path/to/music"
./gradlew :melody-sync-cli:run --args="health /path/to/music"
./gradlew :melody-sync-cli:run --args="duplicates /path/to/music"
./gradlew :melody-sync-cli:run --args="organize /path/to/music"        # dry-run
./gradlew :melody-sync-cli:run --args="organize --apply /path/to/music" # move files
./gradlew :melody-sync-cli:run --args="export --format json /path/to/music"
./gradlew :melody-sync-cli:run --args="export --format csv -o library.csv /path/to/music"
./gradlew :melody-sync-cli:run --args="enrich --only-missing /path/to/music"  # needs YOUTUBE_API_KEY

# Run the Desktop GUI
./gradlew :melody-sync-desktop:run

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

**133 tests, all passing:**

| Module | Tests | Area |
|--------|-------|------|
| `core` | 107 | Models, Discovery, Metadata, Scanner, Statistics, Database, Sync, Health, Duplicates, Watcher, Organization, Export, Enrichment |
| `cli` | 16 | Version, Scan, Health, Duplicates, Organize, Export, Enrich commands |
| `desktop` | 10 | Theme, preferences, color schemes |

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

### Milestone 4 — Desktop GUI ✅
- [x] Compose Desktop window (Material 3)
- [x] System theme detection (KDE + GNOME) with dark/light toggle
- [x] Directory selection + scan with linear progress
- [x] Song list with search/filter (title, artist, album) and clear button
- [x] Compact library statistics in the top bar
- [x] 4-column song list (title, artist, album, duration)

### Milestone 5 — Library Health Check ✅
- [x] Classify non-audio files (image, subtitle, lyrics, metadata, playlist, video)
- [x] Detect songs without metadata, zero duration and orphaned entries
- [x] CLI command `melody-sync health <directory>`
- [x] GUI Health button (report-only, suggestions for the user)
- [ ] Report-only confirmed: never modifies files automatically

### Milestone 6 — Duplicate Detection ✅
- [x] Group duplicate songs (normalized title/artist + duration tolerance)
- [x] CLI command `melody-sync duplicates <directory>`
- [x] GUI Duplicates button (report-only, suggests which file to keep)

### Milestone 7 — File Watcher ✅
- [x] Watch directory recursively (create/delete/modify) via WatchService
- [x] Debounced automatic re-sync of the database
- [x] GUI Watch toggle button

### Milestone 8 — Folder Organization ✅
- [x] Plan `Artist/Album/` structure (dry-run, report-first)
- [x] Apply moves with `--apply` (never automatic)
- [x] Name collision handling (numeric suffix)
- [x] CLI command `melody-sync organize`
- [x] GUI Organize button (dry-run summary)

### Milestone 9 — Export ✅
- [x] Export library metadata to JSON (pretty-printed)
- [x] Export library metadata to CSV (with escaping)
- [x] CLI command `melody-sync export` (`--format json|csv`, `--output`)

### Milestone 11 — Sidebar UI & Preferences ✅
- [x] Sidebar navigation (Library, Statistics, Health, Duplicates, Organize)
- [x] Sortable song list (clickable column headers with ▲/▼)
- [x] A–Z letter index with scroll-to-letter
- [x] Persistent preferences (`~/.config/melody-sync/settings.properties`)

### Milestone 12 — Installation ✅
- [x] Self-contained JAR (uberJar) with main class
- [x] `scripts/install.sh` — build, install to `~/.local`, create launcher + menu entry
- [x] `scripts/uninstall.sh`
- [ ] AppImage / RPM packaging (future)

### Milestone 13 — Enrichment ⏳
- [ ] YouTube enrichment: auto-apply metadata to songs (currently report-only)
- [ ] Cover art and lyrics fetching from matched videos

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