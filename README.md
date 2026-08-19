# Melody Sync

<p align="center">
  <img alt="License" src="https://img.shields.io/github/license/jotatw/Melody-Sync">
  <img alt="CI" src="https://img.shields.io/github/actions/workflow/status/jotatw/Melody-Sync/ci.yml?branch=main">
  <img alt="Language" src="https://img.shields.io/github/languages/top/jotatw/Melody-Sync">
</p>

> A desktop workstation for curating a personal music library — analyze it, fix it, and keep it organized.

Melody Sync is a personal, open-source tool built around **local files** and **explicit decisions**. It reports problems first, suggests what to do, and only changes metadata or files when you approve.

**Report-first, apply-second.** Nothing is modified silently: analyses produce plans and suggestions, and every change to metadata or files is a deliberate action you confirm.

## Features

- **Library** — browse, search, filter, sort, and inspect your collection.
- **Health** — analyze the library for real problems: missing metadata, zero-duration files, orphaned database entries.
- **Metadata & Quick Fix** — review issues and apply suggested tag corrections with your approval.
- **Statistics** — explore how the collection is composed (formats, artists, albums, duration, size).
- **Duplicates** — detect likely duplicate groups and review them before moving anything to trash.
- **Organize** — plan an Artist/Album folder structure as a dry-run, then apply it explicitly.
- **CLI** — the same capabilities for scripting and direct use.
- **Updates** — check and install releases; optional auto-update on startup.

## The workflow

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

## Quick Start

Requirements: **JDK 21+**. Linux is the primary tested platform.

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync

./gradlew build                        # compile + run the full test suite

./gradlew :melody-sync-desktop:run     # run the desktop application
./gradlew :melody-sync-cli:run --args="scan /path/to/music"   # run the CLI
```

The installer for Fedora/Linux:

```bash
./scripts/install.sh
```

## Documentation

The detailed documentation lives in [`docs/INDEX.md`](docs/INDEX.md), which maps every area:

| Document | Purpose |
|---|---|
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Current planning, priorities, and scope boundaries |
| [`docs/architecture/technology.md`](docs/architecture/technology.md) | Technical stack, modules, validation status |
| [`docs/architecture/`](docs/architecture/) | Architecture, core services, and decisions (ADRs) |
| [`docs/design/`](docs/design/) | UX, screens, navigation, and the visual design system |
| [`docs/planning/`](docs/planning/) | Detailed plans for larger changes |
| [`docs/project/History.md`](docs/project/History.md) | Project history and milestones |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Contribution guidelines |

## Principles

- **Start small** — prefer a small, coherent implementation over premature expansion.
- **Report first** — operations that can change files or metadata present the result before applying changes.
- **Explicit approval** — never modify metadata or reorganize the library without a user decision.
- **External providers are replaceable** — YouTube and lyrics are optional supporting sources, never the foundation.
- **Desktop-first** — the primary workflow is a desktop environment where curation is practical.

## License

Melody Sync is released under the [MIT License](LICENSE).