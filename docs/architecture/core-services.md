# Core Services

> Current capability map and responsibility boundaries for `melody-sync-core`.

## Document Information

| Item | Value |
|---|---|
| Category | Architecture |
| Scope | `melody-sync-core` |
| Status | Draft |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

---

## Purpose

This document describes the capabilities already provided by the Core and establishes what each capability is responsible for.

The purpose is to prevent duplicate business logic and to make future UI work reuse the existing Core instead of introducing parallel implementations.

---

## 1. Library Discovery and Synchronization

### Scanner

The scanner discovers supported audio files and reads their available metadata into `Song` objects.

**Responsible for:** filesystem discovery and song construction.

**Not responsible for:** database synchronization, organization, UI state, or automatic metadata correction.

### LibrarySyncService

Synchronizes the scanned filesystem state with the local database.

**Responsible for:** adding, updating and removing database records according to the scanned library.

**Not responsible for:** moving files, editing metadata, or presenting UI.

---

## 2. Metadata

The metadata subsystem is provider-based:

```text
MetadataFormatRegistry
        │
   ┌────┴────┐
   ▼         ▼
JAudioTagger  Opus
Provider     Provider
```

Current capabilities include:

- `MetadataProvider`;
- `MetadataFormatRegistry`;
- `JAudioTaggerProvider`;
- `OpusProvider`;
- typed tag-write errors/results;
- metadata diagnostics;
- temporary-copy write testing;
- `TagWriter`.

**Responsible for:** reading, validating and explicitly writing supported metadata.

**Not responsible for:** moving files, organizing directories, choosing metadata automatically, or contacting external providers.

---

## 3. Diagnostics and Health

### MetadataDiagnosticService

Verifies metadata-provider capabilities and reports read/write support and failure reasons. Write tests operate on temporary copies rather than the original file.

### LibraryHealthService

Analyzes library state and reports issues such as missing metadata, zero duration, orphaned records and other currently supported diagnostics.

**Boundary:** Health diagnoses; it does not silently repair files.

---

## 4. Statistics

Statistics are derived from current library/song data.

Current aggregate information includes:

- songs;
- artists;
- albums;
- total size;
- duration;
- formats;
- average bitrate.

The statistics capability does not own persistence and must not invent unsupported metadata fields merely to fill UI charts.

---

## 5. Duplicates

`DuplicateDetectionService` identifies likely duplicate groups using the current Core heuristic.

Detection is a candidate-finding operation.

It does not automatically delete files or decide which copy is authoritative.

`TrashService` is responsible for recoverable trash operations when an explicit user action authorizes them.

---

## 6. Organization

`LibraryOrganizationService` separates planning from execution:

```text
planOrganization(...)
        ↓
OrganizationReport
        ↓
reorganize(...)
```

**Responsible for:** calculating target paths and executing explicitly authorized filesystem moves.

**Not responsible for:** deciding metadata corrections or silently moving files without an explicit workflow.

Organization rules are documented separately because the physical library structure is a product decision rather than merely a UI concern.

---

## 7. Quick Fix and Matching

`QuickFixService` coordinates the song-level assisted correction workflow.

Current operations include:

```text
diagnose(song)
localSuggestion(song)
youtubeSuggestion(song, apiKey)
apply(song, suggestion)
```

`SongMatcher` provides local heuristic suggestions. `SongEnrichmentService` and the YouTube search service provide optional external candidates.

**Boundary:** suggestions are not authoritative metadata, and Apply is explicit.

The caller remains responsible for refreshing/persisting application state after a successful metadata write where required by the current workflow.

---

## 8. External Providers

Current external integrations include:

- YouTube for optional song-identification/metadata suggestions;
- Lyrics.ovh for informational lyrics lookup.

Providers are optional sources of information.

They must not become implicit requirements for normal offline library operation and must not automatically write metadata.

---

## 9. File Watching

`LibraryWatcher` reports filesystem changes such as added, removed and modified songs, with debouncing.

The watcher reports events; consumers decide how those events affect synchronization and application state.

It does not replace `LibrarySyncService`.

---

## 10. Persistence

The database layer contains the local library persistence boundary, including `DatabaseConnection`, `MusicDatabase`, `MusicRepository` and the songs table.

**Responsible for:** storing and retrieving application/library state.

The database is not the physical source of truth for file existence. Filesystem analysis and synchronization compare both representations.

---

## 11. Installation and Platform

The platform layer isolates operating-system concerns such as:

- installation;
- release installation;
- source validation;
- update handling;
- shell execution;
- version information;
- system-specific behavior.

These concerns must remain outside the music-domain services.

---

## 12. Responsibility Matrix

| Capability | Read | Diagnose | Suggest | Write Metadata | Move Files | Trash |
|---|---:|---:|---:|---:|---:|---:|
| Scanner | ✓ | | | | | |
| Library Sync | ✓ | | | | | |
| Metadata | ✓ | ✓ | | ✓ | | |
| Health | ✓ | ✓ | | | | |
| Statistics | ✓ | | | | | |
| Duplicates | ✓ | ✓ | | | | ✓* |
| Organization | ✓ | | | | ✓ | |
| Quick Fix | ✓ | ✓ | ✓ | ✓* | | |
| YouTube | | | ✓ | | | |
| Lyrics | ✓ | | ✓ | | | |
| Watcher | ✓ | | | | | |

`*` only as an explicit action in the corresponding workflow.

---

## 13. Cross-Service Workflow

The normal library lifecycle is:

```text
Filesystem
    ↓
Scanner
    ↓
Metadata
    ↓
LibrarySyncService
    ↓
Database
    ├── Statistics
    └── Health
          ↓
      contextual review
          ↓
       Library
          ↓
       Quick Fix
          ↓
       Metadata
```

Organization and duplicate handling remain explicit workflows rather than automatic consequences of scanning.

---

## 14. Core Non-Goals

The Core should not become:

- a full music player;
- a mobile application;
- a YouTube downloader;
- a cloud music service;
- an automatic metadata authority;
- a public API without a concrete current consumer.

These may be future projects or integrations, but they are not current Core responsibilities.

---

## 15. Reuse Before Creation

Before introducing a new Core service:

1. Check whether an existing capability already owns the responsibility.
2. Prefer extending an existing service when its boundary remains clear.
3. Check whether the request is actually a UI concern.
4. Check whether it belongs to an external provider.
5. Check whether the repository/model already exposes the required data.
6. Introduce a new abstraction only when an existing responsibility cannot accommodate the behavior cleanly.

This rule is intended to keep the Core small and prevent parallel implementations of the same behavior.

---

## Related Documents

- [Roadmap](../ROADMAP.md)
- [Metadata Foundation](../planning/metadata-foundation.md)
- [Application Design](../design/app-design.md)
- [Navigation](../design/navigation.md)
- [Development Methodology](../standards/handbook/DevelopmentMethodology.md)
- [Platform Layer ADR](ADR/ADR-0009-PlatformLayer.md)
