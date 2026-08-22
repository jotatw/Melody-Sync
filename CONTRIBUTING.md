# Contributing to Melody Sync

Thanks for considering contributing to Melody Sync.

## Project structure

- `melody-sync-core` — domain models, library services, metadata, health, statistics, organization, duplicates, and external providers.
- `melody-sync-cli` — command-line interface for scripting and direct use.
- `melody-sync-desktop` — Compose Desktop application.

The official contribution and development methodology is documented in
[`docs/standards/handbook/DevelopmentMethodology.md`](docs/standards/handbook/DevelopmentMethodology.md).

## Workflow

- The project works directly on `main`. Apply changes as focused commits; non-trivial work is welcome as pull requests.
- Documentation-first rule: a significant feature should have a defined purpose, scope, behavior, and non-goals before implementation (see [`docs/ROADMAP.md`](docs/ROADMAP.md)).
- Keep the relevant planning/design documentation in sync within the same change.
- `docs/INDEX.md` is the map of the project documentation ([`docs/INDEX.md`](docs/INDEX.md)).

## Setup and checks

Requirements: **JDK 21+**.

```bash
./gradlew build        # compile everything and run the full test suite
./gradlew test         # run the test suite
```

The suite includes real audio fixtures for metadata behavior and currently has **267 passing tests** (Core, CLI, Desktop).

## Rules enforced by the project

- **Report first** — operations that change files or metadata present their result before applying changes whenever practical.
- **Explicit user approval** — the application must not silently modify metadata or reorganize the library.
- **External providers are replaceable** — the Core should not depend on a specific provider when it can avoid doing so.
- **No secrets in the repository** — never commit API keys, tokens, or personal paths.

## Questions

Open an issue for questions, bug reports, or suggestions.