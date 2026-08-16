# Quick-Fix HUD — Implemented

> Implemented assisted-curation interaction model for diagnosing and correcting song metadata.

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | research/quick-fix-hud |
| Category         | Research |
| Audience         | Developers |
| Status           | Implemented |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-08 |
| Maintainer       | Melody Sync |

---

## What it is

The **Quick-Fix HUD** is the assisted-curation interaction used when a song
has metadata or quality issues. Selecting a song opens a split-pane panel
showing the diagnosis and available suggestions. The user explicitly chooses
whether to apply a suggestion; fixes are not applied automatically.

```text
┌───────────────────────┬────────────────────────────────┐
│ Song List / Review    │ Quick-Fix Panel                │
│                       │                                │
│ [■] Song with issues  │ SELECTED:                     │
│ [ ] Another song      │  "Song.mp3"                   │
│                       │                                │
│                       │ Missing / quality issues      │
│                       │                                │
│                       │ Local suggestion   [Apply]    │
│                       │ YouTube suggestion [Apply]    │
└───────────────────────┴────────────────────────────────┘
```

### Implemented flow

1. **Diagnosis** — `SongDiagnostics` identifies missing title, artist or album metadata and quality flags such as zero duration or low bitrate.
2. **Local suggestions** — `SongMatcher` derives title/artist/album suggestions from the file path and filename.
3. **External suggestions** — `YouTubeFixSource` can provide candidate fixes when YouTube access is configured.
4. **Explicit Apply** — `QuickFixPanel` lets the user review and apply a selected suggestion through `QuickFixService`.
5. **Persistence and refresh** — tag changes are written to the file, the metadata is re-read, the database-backed library state is refreshed and the Review state is recalculated.
6. **Review flow** — the Review screen lists affected songs, provides issue filters and opens the Quick-Fix panel for the selected song.

---

## Implemented Components

### Core

- `TagWriter` — writes title, artist and album tags and re-reads the resulting metadata.
- `SongDiagnostics` — models missing metadata and quality issues.
- `SongMatcher` — derives local metadata suggestions from path and filename patterns.
- `QuickFixService` — coordinates diagnosis, suggestions and application.
- `FixSuggestionSource` — abstraction for extensible suggestion providers.
- `LocalFixSource` — local filename/path-based suggestions.
- `YoutubeFixSource` — optional YouTube candidates.
- `WriteResult` / `TagWriteError` — typed tag-write outcomes that surface failures to the UI.

### Desktop

- `QuickFixPanel` — split-pane diagnosis and explicit Apply interaction.
- `ReviewSection` — library-wide review list with issue filters.
- Health → Review → Quick Fix navigation.
- Refresh of Review state after scan, database load, watcher changes and tag application.

### Format support

- JAudioTagger remains the general metadata backend.
- Opus has a dedicated lightweight reader/writer for Ogg/OpusTags because JAudioTagger does not provide the required Opus support.
- Opus title/artist/album read and write have been validated against real and synthetic files.

---

## Current Boundary

The Quick-Fix interaction and its foundation are implemented, including:

- `MetadataProvider` abstraction and format registry;
- typed tag-write errors (`WriteResult` / `TagWriteError`);
- more robust Apply behavior;
- injectable database state for headless integration tests;
- serialized database access;
- format-specific fixtures and capability coverage.

Remaining work is the focused reliability refinement described in
[`docs/planning/metadata-foundation.md`](../planning/metadata-foundation.md)
(per-format write reliability, real-world edge cases, and error-message polish).

The detailed implementation plan is maintained in
[`docs/planning/metadata-foundation.md`](../planning/metadata-foundation.md).

---

## Related

- [`docs/planning/metadata-foundation.md`](../planning/metadata-foundation.md)
- `docs/standards/DesignSystem.md` §5
- `docs/research/app-design.md`
- ADR-0001 — Project Vision

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
