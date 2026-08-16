# Melody Sync

<p align="center">
  <img alt="License" src="https://img.shields.io/github/license/jotatw/Melody-Sync">
  <img alt="CI" src="https://img.shields.io/github/actions/workflow/status/jotatw/Melody-Sync/ci.yml?branch=main">
  <img alt="Language" src="https://img.shields.io/github/languages/top/jotatw/Melody-Sync">
</p>

> A desktop tool for curating, analyzing, and organizing a local music library.

Melody Sync is a personal, open-source project focused on making music-library maintenance a clear and controlled workflow. It is designed around local files, explicit user decisions, and incremental organization rather than automatic changes.

The project currently focuses on desktop use. External services and synchronization tools can support the workflow, but they are not required by the core application.

---

## Current Status

| Item | Status |
|---|---|
| Version | **v0.13.0-dev** |
| Language | **Kotlin / JVM 21** |
| Core | Working |
| CLI | Working |
| Desktop GUI | Working |
| Database | SQLite |
| Automated tests | **265 passing** |
| Metadata read/write | Working, including Opus |
| Health analysis | Working |
| Duplicate detection | Working |
| Organization | Working |
| Quick Fix | Working |
| YouTube provider | Working, optional |
| Lyrics provider | Working as an informational source |
| Installation / updates | Working |
| CI | Working |

The project is under active development. Some areas are being consolidated and documented before further feature expansion.

---

## Core Workflow

The main workflow is centered on turning incoming or existing files into a reliable, organized library:

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

The application favors **report-first workflows** and explicit user approval for changes to metadata or files.

---

## Main Capabilities

### Library

Browse, search, filter, sort, and inspect songs stored in the local library.

### Health

Analyze the library for problems such as missing metadata, invalid or incomplete information, zero-duration files, and orphaned database entries.

### Metadata & Quick Fix

Inspect metadata problems and review possible corrections. Suggestions may come from local path heuristics or optional external providers.

Changes are explicitly applied by the user.

### Statistics

Explore library information such as song count, artists, albums, formats, duration, size, and other available statistics.

### Duplicates

Detect groups of potentially duplicated songs and review them before taking action.

### Organization

Plan and apply filesystem organization based on the library metadata. Organization follows a report-first approach and does not silently move files.

### CLI

The command-line interface exposes the main operational capabilities for scripting and direct use.

---

## External Support

External services are treated as **providers or supporting tools**, not as the foundation of the application.

### YouTube

Used as an optional source for identifying songs and providing metadata suggestions.

YouTube results do not automatically modify files or tags. The user must explicitly approve changes.

### Lyrics

Lyrics are an informational feature. Retrieved lyrics are displayed for reference and are **never automatically written to song tags**.

The provider implementation is replaceable so other lyric sources can be added later.

### Syncthing

Syncthing can be used externally to synchronize music files between devices.

A possible personal workflow is:

```text
Mobile
   │
   ▼
Discovery / acquisition
   │
   ▼
Syncthing
   │
   ▼
Incoming
   │
   ▼
Melody Sync
   │
   ├── Review
   ├── Metadata
   ├── Health
   └── Organize
   │
   ▼
Library
   │
   ▼
Syncthing
   │
   ├── Mobile
   └── Notebook
```

Syncthing is not a dependency of Melody Sync, and Melody Sync does not implement its own synchronization protocol.

---

## Architecture

At a high level, the project is divided into three main parts:

```text
melody-sync-core
        │
        ├── Domain / models
        ├── Library services
        ├── Metadata
        ├── Health
        ├── Statistics
        ├── Organization
        ├── Duplicates
        └── External providers
        │
        ├───────────────┐
        ▼               ▼
melody-sync-cli   melody-sync-desktop
```

The Core contains the application capabilities. The CLI and Desktop application consume those capabilities rather than implementing separate versions of the business logic.

Detailed architecture is documented under [`docs/architecture/`](docs/architecture/).

---

## Design Direction

The interface follows a desktop-first approach focused on:

- clear hierarchy;
- deliberate visual identity;
- compact but information-rich layouts;
- contextual actions;
- explicit status and feedback;
- keyboard and mouse interaction;
- dark and light themes.

The visual language combines a restrained **Studio Hi-Fi** influence with editorial music-design elements rather than attempting to reproduce a generic music player interface.

Detailed screen behavior and design rules are documented under [`docs/design/`](docs/design/).

---

## Technology

| Component | Technology |
|---|---|
| Language | **Kotlin 2.4.10** |
| Runtime | **JVM 21** |
| Build | **Gradle 9.6.1 / Kotlin DSL** |
| Metadata | **JAudioTagger 2.2.7** + built-in Opus metadata support |
| CLI | **clikt 5.1.0** |
| Desktop UI | **Compose Desktop 1.11.1** |
| Database | **SQLite / Exposed 0.61.0** |
| Testing | **JUnit 5** |

---

## Requirements

- JDK 21+
- Linux is the primary tested platform
- Windows and macOS are not currently the primary development targets

---

## Installation

For the current Fedora/Linux installation:

```bash
./scripts/install.sh
```

The installer builds the application, installs it under the user's local data directory, and creates the appropriate launcher.

Uninstall:

```bash
./scripts/uninstall.sh
```

---

## Quick Start

Clone the repository:

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync
```

Build:

```bash
./gradlew build
```

Run the Desktop application:

```bash
./gradlew :melody-sync-desktop:run
```

Run the CLI:

```bash
./gradlew :melody-sync-cli:run --args="scan /path/to/music"
```

Other CLI capabilities include:

```text
scan
health
duplicates
organize
export
enrich
update
doctor
```

Run all tests:

```bash
./gradlew test
```

---

## Testing

The project currently has **265 automated tests** covering the Core, CLI, and Desktop modules.

Run:

```bash
./gradlew test
```

The test suite includes real audio fixtures for metadata-related behavior.

---

## Documentation

The `docs/` directory is the source of detailed project documentation.

| Document | Purpose |
|---|---|
| [`docs/INDEX.md`](docs/INDEX.md) | Documentation entry point |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Current planning and priorities |
| [`docs/project/History.md`](docs/project/History.md) | Project history and milestones |
| [`docs/architecture/`](docs/architecture/) | Architecture and technical decisions |
| [`docs/design/`](docs/design/) | UX, screens, navigation, and visual design |
| [`docs/planning/`](docs/planning/) | Detailed plans for larger changes |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Contribution guidelines |

Architecture Decision Records are maintained under `docs/architecture/ADR/`.

---

## Project Principles

### Start small

Prefer a small, coherent implementation over premature expansion.

### Reuse before creating

Before introducing a new abstraction or service, check whether an existing capability can be reused safely.

### Report first

Operations that can change files or metadata should present their result before applying changes whenever practical.

### Explicit user approval

The application should not silently modify metadata or reorganize the user's library.

### External providers are replaceable

YouTube, lyrics providers, and other external services are supporting sources. The Core should not depend on a specific provider when it can avoid doing so.

### Desktop-first

The primary workflow is designed for a desktop environment where library organization, inspection, comparison, and editing are practical.

### Integration-ready, not integration-driven

The architecture should leave room for future integrations without implementing infrastructure before a real use case requires it.

### Documentation before expansion

Significant features should have a defined purpose, scope, behavior, and non-goals before implementation.

---

## Current Direction

The current focus is refinement and stabilization of the implemented workflow:

1. **Documentation consolidation** — ongoing hierarchy work across `docs/`;
2. **Screen behavior & navigation** — finalized (all eight screens have interaction contracts); remaining work is interaction refinement;
3. **Metadata reliability** — per-format write capability enforced; WAV writes are read-only until a reliable writer exists; value round-trip coverage for all writable formats;
4. **UX refinement** — keyboard accessibility, loading/feedback states, responsive behavior, and large-library performance validation;
5. **Updates & installation** — implemented; relaunch orchestration deferred.

The detailed roadmap is maintained in [`docs/ROADMAP.md`](docs/ROADMAP.md).

---

## License

Melody Sync is released under the [MIT License](LICENSE).
