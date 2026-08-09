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
| Version | **v0.13.0-dev** |
| Language | **Kotlin** (JVM 21) |
| Core (scan, metadata, statistics) | ✅ Completed |
| CLI (`melody-sync scan` / `health` / `duplicates` / `organize` / `export` / `enrich` / `update` / `doctor`) | ✅ Working |
| Automated tests | 🎉 **253 passing** |
| Database (SQLite) | ✅ Working |
| GUI (Desktop) | ✅ Working (Compose, collapsible sidebar) |
| Library health check | ✅ Working |
| Review screen | ✅ Working |
| Quick Fix HUD | ✅ Working (diagnose + local/YouTube suggestions, explicit Apply) |
| Duplicate detection | ✅ Working (move to system trash) |
| File watcher (auto re-sync) | ✅ Working |
| Folder organization | ✅ Working |
| Export (JSON/CSV) | ✅ Working |
| YouTube enrichment | ✅ Working (needs API key) |
| Opus metadata | ✅ Working (read + write) |
| Release installer + update channels | ✅ Working |
| Installation (Fedora) | ✅ Script (`scripts/install.sh`) |

---

## Features

### Implemented

- ✅ Audio file discovery (`.mp3`, `.flac`, `.m4a`, `.ogg`, `.opus`, `.wav`)
- ✅ Metadata extraction (title, artist, album, duration, bitrate, sample rate, channels, codec) — including Opus via a built-in Ogg/OpusTags reader/writer
- ✅ Library statistics (total songs, unique artists/albums, size, duration, formats, avg bitrate)
- ✅ SQLite database — persistent metadata cache (`~/.config/melody-sync/library.db`)
- ✅ Library sync — scan a folder and persist/update/remove songs in the database
- ✅ CLI — `melody-sync scan`, `health`, `duplicates`, `organize`, `export`, `enrich`, `update`, `doctor`
- ✅ Cross-format detection (uppercase extensions, nested directories)
- ✅ Desktop GUI — scan, browse, search, filter and review songs (Compose Desktop, Material 3, dark/light toggle)
- ✅ Library health check — classify non-audio files, detect missing metadata, zero duration and orphaned entries (CLI + GUI)
- ✅ Review screen — every song with an issue, filterable, opens the Quick Fix panel
- ✅ Quick Fix HUD — per-song diagnosis with local (file/folder) and optional YouTube suggestions; every edit is user-approved
- ✅ Duplicate detection — group songs by normalized title/artist and similar duration; move extras to the system trash (CLI + GUI)
- ✅ File watcher — automatic re-sync when files change (GUI toggle, debounced)
- ✅ Folder organization — plan `Artist/Album/` structure, apply with `--apply` (report-first, never automatic)
- ✅ Export — library metadata to JSON or CSV (CLI)
- ✅ YouTube enrichment — search candidates for songs missing metadata (CLI + GUI, report-only)
- ✅ Navigation Rail (Material 3) — collapsible sidebar with tooltips when collapsed
- ✅ Sortable song list with letter grouping (opt-in) and a letter scrubber
- ✅ Persistent preferences (directory, theme, section, sort, sidebar, grouping, update channel)
- ✅ Single source of truth for the version + generated runtime version resource
- ✅ Release installer — download a published jar, verify SHA-256, atomic install with rollback
- ✅ Update channels — Stable / Beta / Nightly (CLI + GUI Settings)
- ✅ `melody-sync doctor` — installation and metadata diagnostics
- ✅ One-command installation on Fedora (`scripts/install.sh`)
- ✅ GitHub Actions CI + automated release publishing
- ✅ MIT license
- ✅ System theme detection (KDE Plasma + GNOME)
- ✅ 253 automated tests with real audio fixtures

### In Progress / Planned

- 🚧 Metadata foundation — explicit per-format providers and typed write errors ([plan](docs/planning/metadata-foundation.md))
- ⏳ YouTube enrichment: auto-apply metadata to songs (currently report-only)
- ⏳ Cover art and lyrics fetching from matched videos
- ⏳ AppImage / RPM packaging for easier distribution (currently install script)
- ⏳ Unattended automatic updates

---

## Architecture

```
melody-sync-core/          Business logic
├── model/                 Domain objects: Song, SongDiagnostics, TagSuggestion, LibraryStatistics, HealthReport, DuplicateGroup, OrganizationReport, YouTubeVideoResult
├── scanner/               Discovery, Metadata (JAudioTagger + OpusMetadata), TagWriter, Scanner, Statistics
├── database/              SongsTable, MusicDatabase, MusicRepository
├── service/               LibrarySyncService, LibraryHealthService, DuplicateDetectionService, LibraryWatcher, LibraryOrganizationService, LibraryExportService, YouTubeSearchService, SongEnrichmentService, QuickFixService, SongMatcher, FixSuggestionSource, TrashService
└── platform/              Frozen infrastructure layer (ADR-0009)
    ├── installation/      InstallationService, ReleaseInstaller, InstallationValidator, ReleaseClient
    ├── shell/             ShellExecutor, CommandResult
    └── system/            VersionInfo, VersionComparator

melody-sync-cli/           Command-line interface (clikt)
└── cli/                   Scan, Health, Duplicates, Organize, Export, Enrich, Version, Update, Doctor commands

melody-sync-desktop/       Desktop GUI (Compose Desktop)
├── desktop/               Main.kt (window, theme, window-geometry persistence)
├── theme/                 AppTheme, ColorRoles, Colors, Typography, Shapes, Dimensions, Tokens
├── state/                 AppState (state holder + prefs)
└── ui/                    LibraryScreen + components
    └── components/        Sidebar, TopBar, DirectoryBar, LibraryToolbar, SongList, LibraryHeader,
                           StatisticsSection, HealthSection, ReviewSection, DuplicatesSection,
                           OrganizeSection, SettingsSection, AboutSection, QuickFixPanel, SectionCard, StatusPill
```

Data flow:

```
Directory
    │
    ▼
 discover()          → list of audio files
    │
    ▼
 readMetadata()      → enriched Song objects (per-format provider)
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
| Testing | **JUnit 5** (253 tests, real audio fixtures) |

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
./gradlew :melody-sync-cli:run --args="update"                          # rebuild source or download release
./gradlew :melody-sync-cli:run --args="doctor"                          # installation diagnostics

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

**253 tests, all passing:**

| Module | Tests | Area |
|--------|-------|------|
| `core` | 220 | Models, Discovery, Metadata, Opus, TagWriter, Scanner, Statistics, Database, Sync, Health, Duplicates, Watcher, Organization, Export, Enrichment, Quick Fix, Suggestion sources, Platform (installation/shell/system) |
| `cli` | 17 | Version, Scan, Health, Duplicates, Organize, Export, Enrich, Update, Doctor, Metadata commands |
| `desktop` | 16 | Theme, preferences, state |

```bash
./gradlew test
```

Test fixtures include real audio files for every supported format (with and without tags).

---

## Documentation

Project documentation lives in the `docs/` directory. Key documents:

| Document | Purpose |
|----------|---------|
| `INDEX.md` | Documentation entry point |
| `ROADMAP.md` | Current project state, priorities and deferred work |
| `planning/metadata-foundation.md` | Foundation plan for reliable metadata read/write |
| `project/History.md` | Major milestones and architectural evolution |
| `architecture/ADR/ADR-0001` | Project Vision |
| `architecture/ADR/ADR-0002` | Programming Language (Kotlin) |
| `architecture/ADR/ADR-0003` | Desktop GUI (Compose Desktop) |
| `architecture/ADR/ADR-0004` | Database (SQLite via Exposed) |
| `architecture/ADR/ADR-0005` | Audio Metadata (JAudioTagger) |
| `architecture/ADR/ADR-0007` | CLI Framework (clikt) |
| `architecture/ADR/ADR-0008` | Build System (Gradle Kotlin DSL) |
| `architecture/ADR/ADR-0009` | Platform Layer (installation / shell / system) |
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
- [x] Letter scrubber showing the current letter (replaced the A–Z index)
- [x] Persistent preferences (`~/.config/melody-sync/settings.properties`)

### Milestone 12 — Installation ✅
- [x] Self-contained JAR (uberJar) with main class
- [x] `scripts/install.sh` — build, install to `~/.local`, create launcher + menu entry
- [x] `scripts/uninstall.sh`
- [ ] AppImage / RPM packaging (future)

### Milestone 13 — Enrichment ⏳
- [ ] YouTube enrichment: auto-apply metadata to songs (currently report-only)
- [ ] Cover art and lyrics fetching from matched videos

### Milestone 14 — UX Foundation ✅
- [x] Hi-Fi Editorial design system (colors, typography, shapes, tokens)
- [x] Semantic color roles (ColorRoles) — accent for actions, green/amber/blue for status
- [x] StatusPill, StatCard with icons, EmptyState with success variant, ProgressCard/ResultCard
- [x] Sidebar tooltips when collapsed; Settings grouped into cards
- [x] Letter grouping (opt-in) with letter scrubber

### Milestone 15 — Quick Fix HUD ✅
- [x] `TagWriter` (JAudioTagger + Opus) with re-read after write
- [x] `SongDiagnostics`, `SongMatcher`, `QuickFixService`
- [x] Pluggable suggestion sources (Local + YouTube)
- [x] Library split-pane Quick Fix panel with explicit Apply
- [x] Review screen — all songs with issues, filterable, opens Quick Fix
- [x] Opus metadata read/write

### Milestone 16 — Platform & Updates ✅
- [x] Platform layer (installation / shell / system) frozen under ADR-0009
- [x] Single version source + runtime version resource
- [x] Release installer (download, SHA-256 verify, atomic install, rollback)
- [x] Update channels Stable / Beta / Nightly (CLI + GUI)
- [x] `melody-sync doctor`
- [ ] Unattended automatic updates (deferred)

### Milestone 17 — Metadata Foundation 🚧
- [x] `melody-sync metadata --write-test` diagnostic (Step 0)
- [x] Per-format `MetadataProvider` abstraction (Phase A)
- [x] Typed write errors (Phase B)
- [x] Doctor metadata section + integration tests (Phase C)
- [x] Database connection discipline (Phase D)
- [x] Format fixtures + capability matrix docs (Phase E)

See [docs/planning/metadata-foundation.md](docs/planning/metadata-foundation.md).

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