# Melody Sync

<p align="center">
  <img alt="License" src="https://img.shields.io/github/license/jotatw/Melody-Sync">
  <img alt="CI" src="https://img.shields.io/github/actions/workflow/status/jotatw/Melody-Sync/ci.yml?branch=main">
  <img alt="Language" src="https://img.shields.io/github/languages/top/jotatw/Melody-Sync">
  <img alt="Version" src="https://img.shields.io/github/v/release/jotatw/Melody-Sync?include_prereleases&label=latest">
</p>

> A desktop workstation for curating a personal music library — analyze it, fix it, and keep it organized.

Melody Sync is a personal, open-source tool built around **local files** and **explicit decisions**. It reports problems first, suggests what to do, and only changes metadata or files when you approve.

**Report-first, apply-second.** Nothing is modified silently: analyses produce plans and suggestions, and every change to metadata or files is a deliberate action you confirm.

---

## About

Melody Sync helps you bring order to a personal music library. It scans your files, builds a searchable database, detects real problems (missing metadata, zero-duration files, orphaned entries, duplicates), and gives you a structured workflow to fix and organize — all with your explicit approval at every step.

It is **not** a commercial product, not a Spotify competitor, and not a cloud service. Your files stay on your machine; your data stays in a local SQLite database.

---

## Objective

I built Melody Sync because my own library was a mess: `.mp3`, `.flac`, `.mp4`, `.png`, `.vtt`, `.txt` all mixed together. Existing tools (MusicBrainz Picard, Kid3, LRCGET, Strawberry) each solve one piece of the puzzle, but none offers a **cohesive end-to-end workflow** for cleaning up and organizing a library from start to finish.

The project also serves as a practice ground for software engineering: architecture before implementation, documentation as code, ADR-driven decisions, and incremental evolution.

---

## Features

- **Library** — Browse, search, filter, sort, and inspect your collection.
- **Health** — Analyze the library for real problems: missing metadata, zero-duration files, orphaned database entries.
- **Metadata & Quick Fix** — Review issues and apply suggested tag corrections with your approval.
- **Statistics** — Explore how the collection is composed (formats, artists, albums, duration, size).
- **Duplicates** — Detect likely duplicate groups and review them before moving anything to trash.
- **Organize** — Plan an Artist/Album folder structure as a dry-run, then apply it explicitly.
- **CLI** — The same capabilities for scripting and direct use (`melody-sync scan`, `melody-sync fix`, etc.).
- **Updates** — Check and install releases; optional auto-update on startup.

---

## The Workflow

```text
Incoming / Existing Library
            │
            ▼
         Review
            │
      ┌─────┴─────┐
      ▼           ▼
    Fix         Validate
      │           │
      └─────┬─────┘
            ▼
         Organize
            │
            ▼
          Library
```

---

## Technologies

| Area | Stack |
|------|-------|
| **Language** | Kotlin 2.4 (JVM 21) |
| **Desktop UI** | Compose Desktop 1.11 (Material 3) |
| **CLI Framework** | clikt 5.1 |
| **Database** | SQLite via Exposed 0.61 (DAO + JDBC) + HikariCP |
| **Audio Metadata** | JAudioTagger 2.2.7 |
| **Logging** | SLF4J 2.0 + Logback 1.6 |
| **Concurrency** | Kotlinx Coroutines 1.10 |
| **Build** | Gradle 8 (Kotlin DSL + Version Catalog) |
| **Testing** | JUnit 5.12, Kotlinx Coroutines Test |
| **Terminal Colors** | Mordant 3.0 |

---

## Project Structure

```
melody-sync/
├── melody-sync-core/      # Domain model, scanner, database, health, fix, organize
├── melody-sync-desktop/   # Compose Desktop application (UI, screens, navigation)
├── melody-sync-cli/       # CLI entry points (scan, fix, organize, stats, etc.)
├── docs/                  # Technical documentation (INDEX.md, ADRs, handbook, testing)
├── scripts/               # Installation and utility scripts
├── .github/               # CI/CD workflows
├── gradle/                # Gradle wrapper
├── gradle/libs.versions.toml  # Version catalog
├── settings.gradle.kts    # Multi-module configuration
└── README.md              # This file
```

**Modules:**
- `melody-sync-core` — Pure Kotlin, no UI dependencies. Contains domain models, file scanner, SQLite repository (Exposed), health analysis, metadata fix engine, organize planner, and CLI-compatible services.
- `melody-sync-desktop` — Compose Desktop app depending on `core`. Implements screens, navigation, theming (Material 3, dark-first), and window-size responsiveness.
- `melody-sync-cli` — Thin CLI layer using clikt, delegates to `core` services.

---

## Installation

**Requirements:** JDK 21+. Linux is the primary tested platform (Fedora, Debian-based).

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync

./gradlew build                        # compile + run the full test suite

./gradlew :melody-sync-desktop:run     # run the desktop application
./gradlew :melody-sync-cli:run --args="scan /path/to/music"   # run the CLI
```

**Fedora/Linux installer (creates .desktop entry, adds to menu):**

```bash
./scripts/install.sh
```

---

## Documentation

The detailed documentation lives in [`docs/INDEX.md`](docs/INDEX.md), which maps every area:

| Document | Purpose |
|----------|---------|
| [`docs/INDEX.md`](docs/INDEX.md) | Documentation hub and getting started |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Current planning, priorities, and scope boundaries |
| [`docs/architecture/technology.md`](docs/architecture/technology.md) | Technical stack, modules, validation status |
| [`docs/architecture/`](docs/architecture/) | Architecture, core services, and decisions (ADRs) |
| [`docs/design/`](docs/design/) | UX, screens, navigation, and the visual design system |
| [`docs/planning/`](docs/planning/) | Detailed plans for larger changes |
| [`docs/project/History.md`](docs/project/History.md) | Project history and milestones |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Contribution guidelines |

---

## Status

**v0.13.0-dev** — Stable refinement phase. The core workflow is consolidated; focus is reliability and UX polish rather than new feature areas. Metadata writes are verified by re-read persistence, the interface adapts to compact/medium/expanded windows, and status feedback is semantic and consistent.

See [`docs/INDEX.md`](docs/INDEX.md#-current-status) for the live status table, [`docs/project/History.md`](docs/project/History.md) for milestones, and [`docs/project/SprintBoard.md`](docs/project/SprintBoard.md) for current priorities.

---

## Principles

- **Start small** — Prefer a small, coherent implementation over premature expansion.
- **Report first** — Operations that can change files or metadata present the result before applying changes.
- **Explicit approval** — Never modify metadata or reorganize the library without a user decision.
- **External providers are replaceable** — YouTube and lyrics are optional supporting sources, never the foundation.
- **Desktop-first** — The primary workflow is a desktop environment where curation is practical.
- **Documentation as code** — ADRs, sprint journals, and handbook live alongside the code they describe.

---

## License

Melody Sync is released under the [MIT License](LICENSE).