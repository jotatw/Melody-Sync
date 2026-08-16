# Melody Sync Roadmap

> Central planning document for the current project state, active work, deferred initiatives, and scope boundaries.

---

## Document Information

| Item | Value |
|---|---|
| Document ID | ROADMAP-001 |
| Category | Planning |
| Audience | Everyone |
| Status | Active |
| Project Version | v0.13.0-dev |
| Template Version | BaseDocument v1.0 |
| Last Updated | 2026-08-11 |
| Maintainer | Melody Sync Project |

---

## Purpose

This document is the central entry point for active project planning.

It answers three questions:

1. What is already complete?
2. What are we working on now?
3. What is intentionally deferred or out of scope?

Detailed implementation plans belong in `docs/planning/`. Design behavior belongs in `docs/design/`. Architectural decisions belong in `docs/architecture/` and ADRs. Project history belongs in `docs/project/History.md`.

The roadmap should therefore remain concise and should not become a second implementation specification.

---

## Current State

| Item | Status |
|---|---|
| Project version | **v0.13.0-dev** |
| Automated tests | **262 passing** |
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
| Auto-update | Implemented for supported release installations; relaunch remains deferred |
| Platform layer | Frozen under ADR-0009 |
| Development Methodology | Accepted |

### Current Direction

The immediate priority is **consolidation and documentation**, not opening additional major feature areas.

The project is moving from broad feature construction toward a controlled cycle of:

```text
Document
  ↓
Define behavior
  ↓
Define boundaries
  ↓
Validate existing implementation
  ↓
Refine
  ↓
Only then expand
```

This is intended to prevent unnecessary functionality from being added before the existing workflow and its limits are clearly defined.

---

## Planning Areas

| Area | Priority | Status | Related documents |
|---|---:|---|---|
| Repository audit 2026-08 | **High** | ✅ Complete (A → B → D → C) | [Audit 2026-08](project/audit-2026-08.md) |
| Documentation consolidation | **High** | 🚧 Active | [Documentation Index](INDEX.md), [History](project/History.md) |
| Screen behavior & navigation | **High** | 🚧 Active | [Design](design/), [Application Design Research](research/app-design.md) |
| Metadata reliability | **High** | 🚧 Refinement | [Metadata Foundation](planning/metadata-foundation.md), [Metadata Formats](planning/metadata-formats.md) |
| Quick Fix | Medium | ✅ Implemented; reliability refinement remains | [Quick-Fix HUD](research/quick-fix-hud.md) |
| Review / Health | Medium | ✅ Implemented; interaction refinement ongoing | [Application Design Research](research/app-design.md) |
| UX / Design System | Medium | 🚧 Continuous refinement | [Design](design/) |
| Updates & Installation | Medium | ✅ Implemented; relaunch deferred | [Update Channels](research/update-channels.md) |
| Providers | Low | ✅ YouTube + Lyrics support exists; provider boundary being documented | [Providers](integrations/metadata-providers.md) |
| Platform | Low | 🔒 Frozen | [ADR-0009](architecture/ADR/ADR-0009-PlatformLayer.md) |
| Tests / CI | Medium | ✅ CI and release automation complete; targeted coverage ongoing | [Development Methodology](standards/handbook/DevelopmentMethodology.md) |

---

## 1. Documentation Consolidation

**Priority: High — Active**

The project currently contains a large amount of useful documentation, but several documents accumulated implementation history, current state, and future planning together.

The current objective is to make the documentation hierarchy explicit and prevent new implementation work from being driven by scattered notes.

### Sequence

1. Keep `README.md` as the public project entry point.
2. Keep `docs/INDEX.md` as the documentation map.
3. Keep this document as the active planning source.
4. Maintain `docs/project/History.md` as historical record.
5. Define general architecture and design documents.
6. Define one document per important screen.
7. Define provider and integration boundaries.
8. Keep detailed implementation plans under `docs/planning/`.

### Rule

A significant feature should not move directly from an idea into implementation. Its purpose, scope, interaction, dependencies, and non-goals should be clear first.

---

## 2. Screen Behavior & Navigation

**Priority: High — Active**

The next design effort is to document how each screen works before making additional interface changes.

Each screen document should define:

- purpose;
- what the user can see;
- primary actions;
- secondary actions;
- states and empty states;
- what information it consumes;
- what it can change;
- where its actions lead;
- interactions with other screens;
- explicit non-goals.

The initial screen model centers the application around Library, with Health/Review, Statistics, Organize, Settings, and About supporting the main workflow.

The exact navigation structure should be finalized in the design documentation before further navigation changes are implemented.

### Current state (2026-08-11)

The navigation structure is now finalized in the design documentation:

- `docs/design/app-design.md` defines the global model: six structural destinations (Library, Statistics, Health, Organize, Settings, About); `Review` and `Duplicates` are contextual workflows reached through Health and documented as their own screen contracts.
- Every screen has an interaction contract in `docs/design/screens/` (`library`, `health`, `statistics`, `organize`, `settings`, `about`, `review`, `duplicates`, plus a directory `README.md`).
- Remaining work under this area is interaction/visual refinement (keyboard accessibility, responsive behavior, loading states, large-library performance) — not navigation restructure.

---

## 3. Metadata & Quick Fix

**Priority: High — Refinement**

The Quick Fix workflow is implemented across Core and Desktop.

Implemented capabilities include:

- `TagWriter` for tag writing and re-reading;
- `SongDiagnostics` for missing metadata and quality flags;
- `SongMatcher` for local path/filename suggestions;
- `QuickFixService` orchestration;
- local suggestions;
- optional YouTube suggestions;
- informational lyrics support;
- Review → song → fix flow;
- Opus metadata read/write support;
- metadata provider and format capability infrastructure;
- typed write failure handling;
- metadata diagnostics and fixtures.

The remaining concern is **reliable application of metadata across real-world formats and files**, particularly where format capabilities differ.

The goal is not to add more enrichment sources before the existing write path is dependable.

Detailed work belongs in:

- [Metadata Foundation](planning/metadata-foundation.md)
- [Metadata Formats](planning/metadata-formats.md)
- [Quick-Fix HUD](research/quick-fix-hud.md)

---

## 4. Review / Health

**Priority: Medium — Refinement**

The Review experience is implemented and connected to Health and Quick Fix.

Implemented behavior includes:

- listing songs with issues;
- issue filters;
- per-song indicators;
- opening Quick Fix for a selected song;
- Health actions that navigate to affected songs;
- state refresh after library changes and tag application.

The remaining work is primarily interaction and visual refinement. A separate parallel analysis system should not be introduced without a concrete need.

---

## 5. UX / Design

**Priority: Medium — Continuous refinement**

The application already has the Hi-Fi Editorial visual direction and the first UX consistency/Quick Fix iterations implemented.

Current design work is focused on defining behavior and limits before further visual expansion.

### Current refinement areas

- screen hierarchy and navigation;
- semantic status colors;
- consistent base components;
- Library density and information hierarchy;
- Statistics presentation;
- Health and Review relationship;
- Organize presentation;
- Settings grouping (data-driven section model implemented; ongoing refinement);
- keyboard accessibility;
- responsive behavior;
- loading and feedback states;
- large-library performance validation.

These are refinement tasks. They should not trigger a new application architecture unless a concrete requirement proves that the current structure cannot support them.

Detailed behavior belongs under [`docs/design/`](design/).

---

## 6. Updates & Installation

**Priority: Medium — Implemented / Deferred refinement**

Implemented:

- centralized version information;
- installation information;
- source checkout validation;
- CLI update command;
- `doctor` command;
- release installer;
- checksum verification and rollback support;
- Stable / Beta / Nightly channels;
- GUI update flow;
- automatic update on startup for supported release installations.

Deferred:

- automatic relaunch into the new build after an update.

The current boundary is intentional: update installation and channel selection are implemented, while relaunch orchestration remains a separate problem.

See [Update Channels](research/update-channels.md).

---

## 7. External Providers & Integrations

**Priority: Low — Boundary documentation**

YouTube and Lyrics.ovh are treated as **supporting providers**, not as core application dependencies.

### YouTube

Provides optional search/suggestion information during review and enrichment workflows.

It must not silently modify metadata or files.

### Lyrics

Provides informational lyrics for inspection. Lyrics are not automatically written to tags.

### Syncthing

May be used externally to transfer music between devices. Melody Sync does not implement or require Syncthing.

### Future providers

Provider interfaces should remain replaceable. A new provider should only be added when it solves a concrete user need.

No general-purpose external API layer is planned at this stage. The Core should remain integration-ready without introducing infrastructure before a real second consumer requires it.

---

## 8. Platform

**Priority: Low — Frozen**

The platform layer is frozen according to [ADR-0009](architecture/ADR/ADR-0009-PlatformLayer.md).

Its responsibilities include installation, shell execution, and system-level integration.

Core/domain code must not depend directly on platform implementation details.

No new platform abstraction should be introduced unless a concrete requirement appears.

---

## 9. Tests & Validation

**Priority: Medium — Ongoing**

The project has CI and release automation in place and currently reports **262 passing automated tests**.

Ongoing validation should prioritize areas where behavior depends on real filesystem, database, metadata, or external-process behavior.

The current emphasis is not increasing the test count for its own sake. Tests should protect meaningful behavior and prevent regressions while the existing workflow is consolidated.

---

## Planning Rules

The project follows the official development cycle defined in [`DevelopmentMethodology.md`](standards/handbook/DevelopmentMethodology.md):

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

Practical rules:

1. Stabilize existing functionality before opening a new feature area.
2. A significant feature receives a planning document before implementation.
3. Define what the feature does **and does not do**.
4. Reuse existing capabilities before introducing new abstractions.
5. Do not add infrastructure solely for hypothetical future consumers.
6. Keep external providers optional and replaceable where practical.
7. Do not duplicate detailed implementation plans in this roadmap.
8. Keep completed work distinguishable from active work.
9. Keep deferred work explicit instead of allowing it to silently become scope.
10. A frozen architectural layer is not changed merely to simplify a feature.

---

## Out of Scope for the Current Cycle

These items are intentionally deferred unless a concrete need changes their priority:

- full-featured music player;
- dedicated mobile Melody Sync application;
- built-in file synchronization protocol;
- general-purpose public HTTP API;
- YouTube downloader implementation inside Melody Sync;
- automatic lyrics-to-tag writing;
- genre/year schema expansion without a new metadata foundation decision;
- album cover extraction/cache and a cover-driven library view;
- automatic relaunch after update;
- major new desktop sections unrelated to the current Library/Review/Health workflow.

This list is a scope boundary, not a rejection of future possibilities.

---

## Related Documents

- [Documentation Index](INDEX.md)
- [Project History](project/History.md)
- [Repository Audit 2026-08](project/audit-2026-08.md)
- [Metadata Foundation](planning/metadata-foundation.md)
- [Metadata Formats](planning/metadata-formats.md)
- [Quick-Fix HUD](research/quick-fix-hud.md)
- [Application Design Research](research/app-design.md)
- [Update Channels](research/update-channels.md)
- [Development Methodology](standards/handbook/DevelopmentMethodology.md)
- [ADR-0009 — Platform Layer](architecture/ADR/ADR-0009-PlatformLayer.md)

---

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | 2026-08-08 | Initial roadmap consolidating current implementation and planning |
| 1.1 | 2026-08-08 | Consolidated research links and adopted the official development methodology |
| 1.2 | 2026-08-09 | Reframed roadmap around documentation consolidation, screen behavior, existing capabilities, and explicit scope boundaries |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
