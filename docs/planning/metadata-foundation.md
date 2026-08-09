# Metadata Foundation

> Foundation record for reliable, diagnosable and testable metadata read/write operations behind Quick Fix.

---

## Document Information

| Item | Value |
|---|---|
| Document ID | PLAN-METADATA-001 |
| Category | Planning / Foundation Record |
| Audience | Core and desktop developers |
| Status | **Implemented / Refinement** |
| Project Version | v0.13.0-dev |
| Template Version | BaseDocument v1.0 |
| Last Updated | 2026-08-09 |
| Maintainer | Melody Sync Project |

---

## Purpose

Record the foundation that makes metadata read/write operations explicit about format capabilities, failure reasons, persistence and testability.

The foundation described here is implemented. Remaining work belongs to reliability refinement, additional verified format coverage, and future metadata capabilities rather than rebuilding this architecture.

The scope remains intentionally limited to the existing title, artist and album write path. Genre, year and album artwork are outside this foundation.

---

## Implemented Foundation

Quick Fix and the metadata subsystem now provide:

- `TagWriter` for title/artist/album writes;
- `SongDiagnostics` for missing fields and quality flags;
- `SongMatcher` for local path/filename suggestions;
- `QuickFixService` for diagnosis, suggestions and Apply;
- local and optional YouTube suggestion sources;
- Review screen integration;
- Opus metadata reading and writing;
- `MetadataProvider` abstraction;
- `MetadataFormatRegistry`;
- `JAudioTaggerProvider`;
- `OpusProvider`;
- typed `WriteResult` / `TagWriteError` handling;
- metadata diagnostics and safe write tests;
- provider capability fixtures and verified format records;
- controlled database connection behavior for the Apply path.

`TagWriter` routes supported formats through the metadata provider registry and re-reads the file after a successful write. The user must explicitly confirm Apply.

---

# Step 0 — Metadata Diagnostic

> **Status: Implemented** (`melody-sync metadata [--write-test] <file>`).

The diagnostic inspects the target without changing the original file and reports the detected format, provider, read/write capability and failure reason where applicable.

Write tests operate against a temporary copy.

---

# Phase A — Metadata Provider Abstraction

> **Status: Implemented.**

The metadata subsystem uses:

```text
MetadataProvider
├── JAudioTaggerProvider
├── OpusProvider
└── MetadataFormatRegistry
```

Higher-level code resolves a provider through the registry rather than maintaining format-specific branches.

Providers expose the capabilities and fields they support and keep format-specific behavior inside the provider boundary.

The current write scope remains title, artist and album.

---

# Phase B — Typed Write Results

> **Status: Implemented.**

Metadata writes return explicit results and typed failures rather than exposing raw library exceptions as the application contract.

The error model distinguishes categories such as unsupported format/capability, parsing, I/O, locking, missing files and permission failures where applicable.

Quick Fix can therefore present a meaningful result without parsing exception strings.

Successful Apply re-reads the file before the application updates its database/cache representation.

---

# Phase C — Doctor and Integration Testing

> **Status: Implemented.**

The CLI doctor flow includes metadata-related checks, and the Apply path has headless integration coverage using an injectable/testable database path.

The integration path verifies the relationship between:

```text
suggestion
    ↓
QuickFixService.apply
    ↓
TagWriter / MetadataProvider
    ↓
read back
    ↓
MusicRepository
    ↓
application state
```

---

# Phase D — Database Connection Discipline

> **Status: Implemented.**

The application uses the controlled `DatabaseConnection` abstraction for the relevant persistence path rather than opening independent ad-hoc connections during Apply.

Database writes are coordinated to avoid inconsistent concurrent state.

Existing SQLite behavior and database tests remain part of the validation surface.

---

# Phase E — Documentation and Fixtures

> **Status: Implemented.**

The metadata capability matrix is recorded in [`metadata-formats.md`](metadata-formats.md), and real per-format fixtures are used for metadata validation.

The verified matrix and fixtures are the source of truth for current format behavior; assumptions about third-party library support should not be treated as capabilities.

Tests cover, where supported:

1. read;
2. diagnose;
3. capability detection;
4. write;
5. re-read;
6. database update;
7. failure classification.

---

# Current Refinement Scope

The foundation is complete. Remaining metadata work should be handled as focused refinement rather than reopening the architecture.

Current areas include:

- improve reliability of writes for individual formats;
- expand or correct fixtures when a real format edge case is discovered;
- improve user-facing error messages when a provider reports a specific failure;
- validate new metadata fields only when their domain/database requirements are explicitly approved;
- keep provider capabilities synchronized with the verified format matrix.

---

# Future Metadata Extensions

These are deliberately outside the current foundation:

- genre field/database migration;
- year field/database migration;
- album cover extraction and caching;
- broader metadata fields;
- bulk destructive tag rewriting.

Any of these should receive a separate plan when a concrete requirement exists.

---

# Non-Goals

This foundation does not include:

- automatic metadata application;
- automatic YouTube acceptance;
- automatic lyrics tagging;
- replacing JAudioTagger wholesale;
- turning Quick Fix into a generic automatic enrichment engine;
- redesigning the entire Quick Fix UI.

Quick Fix remains explicitly user-approved: suggestions are reports, and Apply is an explicit user action.

---

## Related Documents

- [Roadmap](../ROADMAP.md)
- [Quick-Fix HUD research](../research/quick-fix-hud.md)
- [Security & Resilience Guide](../architecture/SecurityAndResilienceGuide.md)
- [Metadata Formats](metadata-formats.md)
- [Documentation Index](../INDEX.md)

---

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-08-08 | Initial metadata foundation plan |
| 1.1 | 2026-08-09 | Reclassified the foundation as implemented and separated remaining reliability refinement from the completed architecture |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
