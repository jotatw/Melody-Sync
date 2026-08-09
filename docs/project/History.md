# Melody Sync Project History

> High-level historical record of the project's major milestones, architectural evolution, and documented state.

---

## Document Information

| Item | Value |
|---|---|
| Document ID | HISTORY-001 |
| Category | History |
| Audience | Everyone |
| Status | Active |
| Project Version | v0.13.0-dev |
| Template Version | HistoryTemplate v1.0 |
| Last Updated | 2026-08-09 |
| Maintainer | Melody Sync Project |

---

## Purpose

Record the evolution of Melody Sync at a milestone level without replacing planning, implementation documentation, or architecture decisions.

This document records what happened. Current and future work belongs in [`docs/ROADMAP.md`](../ROADMAP.md) and the relevant planning documents.

---

## Timeline

### Project Foundation

Melody Sync began as a personal music-library organization project. The early implementation established the core domain around scanning a local music directory, extracting metadata, and presenting library information.

The project later migrated from the Python prototype to Kotlin, establishing the current JVM architecture and removing the legacy implementation after the Kotlin version became sufficiently stable.

### Core Library and Scanner

The core evolved into a reusable library scanner and model layer capable of discovering audio files, reading metadata, and persisting library information in SQLite.

JAudioTagger was adopted for audio metadata, with defensive handling for missing and malformed metadata. This decision is recorded in ADR-0005.

### CLI

The CLI grew around report-oriented operations including:

- `scan`;
- `health`;
- `duplicates`;
- `organize`;
- `export`;
- `enrich`;
- `update`;
- `doctor`.

The project consistently favored explicit reports and validation before destructive actions.

### Library Health and Analysis

Health analysis was introduced to report missing metadata, zero duration, orphaned entries, and non-audio content without modifying files.

Duplicate detection followed with report-only grouping and recommendations.

Folder organization was introduced as a dry-run planning operation, with application remaining explicit.

### File Watcher

A recursive filesystem watcher was added to keep the database synchronized with library changes. The GUI updates library data and statistics after watcher events.

### Desktop GUI

The Compose Desktop application introduced the main application areas for Library, Statistics, Health, Review, Duplicates, Organize, Settings, and About.

A collapsible sidebar and persistent preferences established the navigation model used during the subsequent UX refinement work.

### UX Foundation

The desktop UI received a Hi-Fi Editorial design direction followed by a consistency pass covering semantic colors, reusable status pills, statistics cards, empty states, sidebar tooltips, Settings grouping, and Health review actions.

UX-1 was completed as a consistency and refinement milestone.

### Installation and Platform Layer

Installation evolved from the local source-based Fedora installer into a dedicated platform layer covering installation, shell execution, and system integration.

ADR-0009 froze the platform boundary so that domain and Core code remain independent of platform implementation details.

A single version source was introduced through `gradle.properties`, and installation metadata is recorded through `VERSION` and `INSTALLATION.json`.

### Release Installer and Updates

The release installer was completed with:

- published release download;
- SHA-256 verification;
- atomic installation and rollback support;
- launcher and symlink creation;
- desktop entry;
- Stable / Beta / Nightly channel support.

The CLI and GUI update flows can choose between rebuilding from a source checkout and installing a published release.

Automatic update-on-startup was subsequently implemented for release installations. Relaunch orchestration remained a separate deferred concern.

### Metadata Quick Fix

The Quick Fix foundation introduced:

- `TagWriter`;
- `SongDiagnostics`;
- `SongMatcher`;
- `QuickFixService`;
- local suggestions;
- optional YouTube suggestions;
- explicit user-approved Apply.

The desktop Quick Fix HUD was then integrated into Library as a split pane. Every edit remains explicit and user-validated.

Opus metadata support was added because JAudioTagger did not provide the required Opus behavior.

A lyrics source was added to the Quick Fix panel as a view-only fetch through the Lyrics.ovh API, alongside local and YouTube suggestion sources.

Quick Fix suggestions are now reviewed before Apply: the panel opens an editable review showing the current values separately from the suggestion, with the source visible. The user can edit or reject; nothing is written without the explicit Apply action (metadata workflow §5, §7).

YouTube suggestions now present candidate results with explicit selection: the user picks the candidate that best matches the file, then reviews/edits and applies it (youtube-identification.md §9, §16).

The Review screen was added after Health and Quick Fix, creating a direct Health → Review → Quick Fix flow for songs with issues. The Review screen computes per-song diagnostics, provides filters, and opens the Quick Fix panel for the selected song.

Navigation consolidation (Block 01 + Block 02): contextual navigation now distinguishes filter vs. selection contexts (Health → Library; Statistics → Library; Library → Quick Fix), and the sidebar reached its target primary navigation — Review and Duplicates were removed as primary destinations and are reached contextually through Health (issue context and duplicate groups).

Health consolidation (Block 03): Health now shows a duplicate-groups summary card and opens the full duplicate workflow from there, keeping duplicate findings within the Health/review experience while preserving inspection, confirmation, trash and progress.

Statistics expansion (Block 04): album navigation to Library with a preserved album filter (alongside artist/format), a Library album filter field, and a proper no-data empty state with a Go to Library action.

### Metadata Foundation

The metadata foundation was completed through the documented Steps 0–E. The work established diagnostic tooling, metadata-provider abstractions and registry support, typed write errors, metadata checks in `doctor`, headless Apply integration coverage, serialized database access, and per-format fixtures with a capability matrix.

The foundation established the boundary for further metadata reliability work rather than declaring all format-specific write behavior universally solved.

### Documentation Consolidation

The documentation was consolidated around a central roadmap, detailed planning documents, project history, and an official development methodology.

The development methodology formalized the cycle:

```text
Need
  ↓
Planning
  ↓
Architecture
  ↓
Review
  ↓
Freeze
  ↓
Implementation
  ↓
Validation
  ↓
Documentation
  ↓
Approval
  ↓
Maintenance
```

The methodology became an accepted handbook document rather than a backlog item.

---

## Current Milestone — v0.13.0-dev

The current documented project state is a consolidation and stabilization phase following the implementation of the Core, desktop workflow, installation system, Quick Fix, Review, and documentation foundation.

Recorded state:

- Version: **v0.13.0-dev**
- Automated tests: **257 passing**
- Core: functional and stable enough for continued development
- Desktop GUI: functional with UX refinement ongoing
- CLI: functional
- Installation and release update flow: implemented
- UX-1 Consistency: completed
- UX-2 Quick Fix: completed
- Review screen: completed
- Development methodology: accepted
- Metadata foundation: completed
- Auto-update on startup: completed; relaunch orchestration remains separate

The detailed current planning state is maintained in [`docs/ROADMAP.md`](../ROADMAP.md), rather than in this history document.

---

## Project Evolution

```text
Python Prototype
       │
       ▼
Kotlin Migration
       │
       ▼
Core + Scanner
       │
       ▼
SQLite + CLI
       │
       ▼
Health / Duplicates / Organize
       │
       ▼
Desktop GUI
       │
       ▼
Installation + Platform Layer
       │
       ▼
Release Installer + Updates
       │
       ▼
UX Foundation
       │
       ▼
Quick Fix + Review
       │
       ▼
Metadata Foundation
       │
       ▼
Documentation Consolidation
```

---

## Historical Project Metrics

| Metric | Recorded Value |
|---|---|
| Project Version | v0.13.0-dev |
| Automated Tests | 257 |
| Architecture Decision Records | 9 |
| Desktop Areas | Library, Statistics, Health, Review, Duplicates, Organize, Settings, About |
| Release Installer | Completed |
| Update Channels | Stable / Beta / Nightly |
| Platform Layer | Frozen |

These values describe the documented state at the current milestone and may change as the project evolves.

---

## Major Milestones

| Milestone | Status at v0.13.0-dev |
|---|---|
| Kotlin migration | ✅ Completed |
| Core scanner and model | ✅ Completed |
| SQLite persistence | ✅ Completed |
| CLI | ✅ Completed |
| Health analysis | ✅ Completed |
| Duplicate detection | ✅ Completed |
| Folder organization | ✅ Completed |
| File watcher | ✅ Completed |
| Desktop GUI | ✅ Completed |
| UX-1 Consistency | ✅ Completed |
| Installation | ✅ Completed |
| Platform layer / ADR-0009 | 🔒 Frozen |
| Release installer | ✅ Completed |
| Update channels | ✅ Completed |
| UX-2 Quick Fix | ✅ Completed |
| Review screen | ✅ Completed |
| Development methodology | ✅ Accepted |
| Metadata foundation | ✅ Completed |
| Auto-update on startup | ✅ Completed |

---

## Document Boundaries

This history intentionally does not contain an implementation backlog or detailed future plans.

- Current priorities and deferred work → [`docs/ROADMAP.md`](../ROADMAP.md)
- Detailed feature plans → [`docs/planning/`](../planning/)
- Architecture decisions → [`docs/architecture/ADR/`](../architecture/ADR/)
- UX and screen behavior → [`docs/design/`](../design/)

---

## Related Documents

- [Roadmap](../ROADMAP.md)
- [Documentation Index](../INDEX.md)
- [Development Methodology](../standards/handbook/DevelopmentMethodology.md)
- [ADR-0009 — Platform Layer](../architecture/ADR/ADR-0009-PlatformLayer.md)
- [ADR-0005 — Audio Metadata](../architecture/ADR/ADR-0005-Mutagen.md)
- [Metadata Foundation](../planning/metadata-foundation.md)
- [Quick-Fix HUD research](../research/quick-fix-hud.md)
- [History Template](../standards/templates/HistoryTemplate.md)

---

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-08-08 | Initial historical record consolidated from project milestones |
| 1.1 | 2026-08-08 | Recorded documentation consolidation and accepted development methodology |
| 1.2 | 2026-08-09 | Separated historical record from current planning and removed future-work sections |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
