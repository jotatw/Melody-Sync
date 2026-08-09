# Melody Sync Roadmap

> Central planning document for the current project state, active work and deferred initiatives.

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | ROADMAP-001 |
| Category         | Planning |
| Audience         | Everyone |
| Status           | Active |
| Project Version  | v0.13.0-dev |
| Template Version | BaseDocument v1.0 |
| Last Updated     | 2026-08-08 |
| Maintainer       | Melody Sync Project |

---

## Purpose

Provide a single point of entry for project planning.

This document separates completed work from active planning and deferred work so that new implementation does not open unnecessary parallel directions.

Large features should have a dedicated document under `docs/planning/` before implementation begins.

---

## Current State

| Item | Status |
|------|--------|
| Project version | **v0.13.0-dev** |
| Automated tests | **253 passing** |
| Core | Stable and actively used |
| Desktop GUI | Functional and in UX refinement |
| CLI | Functional |
| Installation | Release installer completed |
| Update channels | Stable / Beta / Nightly implemented |
| Platform layer | Frozen under ADR-0009 |
| UX-1 Consistency | Completed |
| UX-2 Quick Fix | Completed |
| Review screen | Completed |
| Development Methodology | Accepted |

The current priority is stabilization and consolidation rather than opening additional large UI features.

---

## Planning Areas

| Area | Priority | Status | Planning / Research |
|------|----------|--------|---------------------|
| Metadata & Quick Fix | High | ✅ Foundation complete (Steps 0, A–E done) | [Metadata Foundation](planning/metadata-foundation.md) · [Formats](planning/metadata-formats.md) · [Quick-Fix HUD](research/quick-fix-hud.md) |
| Review / Analysis | Medium | ✅ Review implemented; Health actionable | [Application Design Research](research/app-design.md) |
| Updates & Installation | Medium | 🚧 Automatic unattended update backlog; release installer and channels completed | [Update Channels](research/update-channels.md) |
| Platform | Low | 🔒 Frozen | [ADR-0009](architecture/ADR/ADR-0009-PlatformLayer.md) |
| UX / Design | Medium | 🚧 Continuous refinement | [Application Design Research](research/app-design.md) |
| Docs / Tests | Medium | 🚧 CI completed; methodology accepted | [Development Methodology](standards/handbook/DevelopmentMethodology.md) |

---

## 1. Metadata & Quick Fix

**Priority: High**

The Quick Fix flow is implemented across core and desktop. The remaining work is to make metadata writing reliable and diagnosable across supported formats.

Current implementation includes:

- `TagWriter` for tag writing and re-reading.
- `SongDiagnostics` for missing metadata and quality flags.
- `SongMatcher` for local filename/path suggestions.
- `QuickFixService` orchestration.
- Local and optional YouTube suggestion sources.
- Review screen connected to the Health → song → fix flow.
- Opus metadata read/write support.

The foundation work is tracked in [metadata-foundation.md](planning/metadata-foundation.md) and is **complete (Steps 0, A–E)**: diagnostic CLI, MetadataProvider registry, typed write errors, doctor metadata section, headless Apply integration test, single serialized DatabaseConnection, and per-format fixtures with the verified capability matrix ([metadata-formats.md](planning/metadata-formats.md)).

Primary open problem:

> Applying metadata to some original files can still fail because format capabilities differ, particularly for formats handled through JAudioTagger.

The foundation plan must therefore establish explicit format capabilities and typed write failures before expanding Quick Fix further.

The implemented interaction model is documented in [Quick-Fix HUD research](research/quick-fix-hud.md).

---

## 2. Review / Analysis

**Priority: Medium**

Completed:

- Review screen listing songs with issues.
- Filters for metadata, zero duration and low bitrate.
- Per-song issue indicators.
- Selection opens Quick Fix.
- Health can navigate directly to affected songs.
- Review state refreshes after scan, load, watcher changes and tag application.

Future work is refinement of the analysis experience rather than another parallel analysis system.

---

## 3. Updates & Installation

**Priority: Medium**

Completed:

- Single source of truth for versioning.
- Installation information and `INSTALLATION.json`.
- Source checkout validation.
- CLI update command.
- `doctor` command.
- Release installer with checksum verification and rollback support.
- Stable / Beta / Nightly channel selection.
- GUI update flow using source rebuild or release installation as appropriate.

Deferred:

- Fully automatic unattended update behavior.
- Automatic background checking/install policy.
- Restart/relaunch orchestration after an update.

The current implementation and the real unattended-update backlog are documented in [Update Channels research](research/update-channels.md).

---

## 4. Platform

**Priority: Low**

The platform layer is frozen according to [ADR-0009](architecture/ADR/ADR-0009-PlatformLayer.md).

Its responsibilities include installation, shell execution and system-level integration. Core/domain code must not depend on platform implementation details.

No new platform abstraction should be introduced unless a concrete requirement appears.

---

## 5. UX / Design

**Priority: Medium**

Completed:

- UX-1 Consistency: semantic colors, StatusPill, StatCard refinement, EmptyState improvements, sidebar tooltips, Settings cards and Health review actions.
- UX-2 Quick Fix: Library split-pane with diagnostics, local/YouTube suggestions and explicit Apply.
- Shared ProgressCard / ResultCard usage.
- Library filters and column visibility.
- Statistics dashboard foundation.

Future work:

- Accessibility refinement.
- Keyboard navigation.
- Responsive behavior for small windows.
- Loading/skeleton refinement.
- Tooltips and truncation polish.
- Large-library performance validation.

These are refinement tasks, not reasons to redesign the application architecture.

---

## 6. Documentation / Tests

**Priority: Medium**

Completed:

- GitHub Actions CI.
- Release automation.
- Documentation standard and templates.
- Architecture Decision Records.
- Research and project documentation structure.
- Official development methodology.

Active / ongoing:

- Continued expansion of integration tests where real filesystem/database behavior matters.
- Metadata format fixtures and capability tests as part of the metadata foundation.

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

Practical roadmap rules:

1. Stabilize existing functionality before opening a new feature area.
2. A large feature receives a planning document under `docs/planning/` before implementation.
3. Do not duplicate detailed implementation plans in this roadmap.
4. Completed work remains recorded here so the roadmap does not become a second backlog.
5. Deferred work must have an explicit reason.
6. A frozen architectural layer is not changed merely to simplify a feature implementation.

---

## Out of Scope for the Current Cycle

The following remain intentionally deferred:

- Genre as a new database field.
- Year as a new database field.
- Album cover extraction/cache.
- Timeline by year based on a real `year` field.
- Large-scale metadata schema expansion without a foundation review.
- Unattended automatic updates.
- New major desktop sections unrelated to the current review/metadata flow.

---

## Related Documents

- [Documentation Index](INDEX.md)
- [Metadata Foundation](planning/metadata-foundation.md)
- [Metadata Formats](planning/metadata-formats.md)
- [Quick-Fix HUD research](research/quick-fix-hud.md)
- [Application Design Research](research/app-design.md)
- [Update Channels research](research/update-channels.md)
- [Development Methodology](standards/handbook/DevelopmentMethodology.md)
- [ADR-0009 — Platform Layer](architecture/ADR/ADR-0009-PlatformLayer.md)
- [History](project/History.md)
- [Error Log](project/ErrorLog.md)

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-08 | Initial roadmap consolidating current implementation and planning |
| 1.1 | 2026-08-08 | Consolidated research links and adopted the official development methodology |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
