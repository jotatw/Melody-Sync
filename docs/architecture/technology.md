# Technology & Module Structure

> Technical stack, module map, and current validation status of Melody Sync.

## Document Information

| Item | Value |
|---|---|
| Category | Architecture |
| Audience | Developers |
| Status | Current reference |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-19 |

---

## Purpose

Holds the technical details of the project so the public README stays focused on what the project does. Behavioral status lives in [`docs/ROADMAP.md`](../ROADMAP.md); design details live in [`docs/design/`](../design/).

---

## Modules

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

## Technology Stack

| Component | Technology |
|---|---|
| Language | **Kotlin 2.4.10** |
| Runtime | **JVM 21** |
| Build | **Gradle 9.6.1 / Kotlin DSL** |
| Metadata | **JAudioTagger 2.2.7** + built-in Opus metadata support |
| CLI | **clikt 5.1.0** |
| Desktop UI | **Compose Desktop 1.11.1** |
| Responsive layout | `WindowSizeClass` (compact/medium/expanded via `LocalWindowSizeClass`) |
| Database | **SQLite / Exposed 0.61.0** |
| Testing | **JUnit 5** |

## Requirements

- JDK 21+
- Linux is the primary tested platform
- Windows and macOS are not currently the primary development targets

## Validation Status

- Automated tests: **268 passing** (Core, CLI, Desktop)
- CI and release automation in place
- The suite includes real audio fixtures for metadata behavior; writes are verified by round-trip persistence (see [`docs/planning/metadata-formats.md`](../planning/metadata-formats.md))

## Capability Status

| Capability | Status |
|---|---|
| Core | Stable and actively used |
| Desktop GUI | Functional and under UX refinement |
| CLI | Functional |
| Database | SQLite via Exposed |
| Review | Implemented |
| Quick Fix | Implemented |
| Health | Implemented and actionable |
| Statistics | Implemented foundation |
| Organization | Implemented |
| Duplicate detection | Implemented |
| Installation | Implemented |
| Update channels | Stable / Beta / Nightly implemented |
| Auto-update | Implemented for release installs; relaunch deferred |
| Platform layer | Frozen under [ADR-0009](ADR/ADR-0009-PlatformLayer.md) |

## Related Documents

- [Core Services](core-services.md)
- [Music Library Domain](music-library-domain.md)
- [Security and Resilience Guide](SecurityAndResilienceGuide.md)
- [ADR index](../architecture/)