# Metadata Foundation

> Foundation plan for reliable, diagnosable and testable metadata read/write operations behind Quick Fix.

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | PLAN-METADATA-001 |
| Category         | Planning |
| Audience         | Core and desktop developers |
| Status           | Planned |
| Project Version  | v0.13.0-dev |
| Template Version | BaseDocument v1.0 |
| Last Updated     | 2026-08-08 |
| Maintainer       | Melody Sync Project |

---

## Purpose

Define the next foundation work for metadata writing without expanding the domain model unnecessarily.

The goal is not to add more metadata fields immediately. The goal is to make the existing title, artist and album write path explicit about format capabilities, failure reasons, persistence and testability.

---

## Current Context

Quick Fix already provides:

- `TagWriter` for title/artist/album writes.
- `SongDiagnostics` for missing fields and quality flags.
- `SongMatcher` for local path/filename suggestions.
- `QuickFixService` for diagnosis, suggestions and Apply.
- Local and optional YouTube suggestion sources.
- Review screen integration.
- Opus metadata reading and writing.

`TagWriter` currently routes Opus through a dedicated writer and other formats through JAudioTagger. The writer re-reads the file after a successful write. This is useful, but format capability and failure semantics still need to be made explicit.

The project also has a real-world requirement: an Apply operation must never silently report success when the original file could not be modified.

---

# Step 0 — Metadata Diagnostic

> **Status: Implemented** (`melody-sync metadata [--write-test] <file>`).

## Objective

Create a diagnostic operation exposed by the CLI as:

```text
melody-sync metadata --write-test <file>
```

The operation must inspect the target without changing the original file.

## Output

The diagnostic should report, at minimum:

- detected format;
- metadata reader/provider selected;
- read capability;
- write capability;
- requested write fields supported by the provider;
- whether a safe write test can be performed;
- typed reason when it cannot.

Example:

```text
Format: m4a
Provider: JAudioTagger
Read: yes
Write: limited
Write test: unavailable
Reason: UnsupportedContainer
```

For a supported fixture:

```text
Format: opus
Provider: OpusProvider
Read: yes
Write: yes
Write test: passed
```

The diagnostic must never modify the user's original file.

## Acceptance

- The command exits successfully when diagnosis itself succeeds.
- Unsupported formats produce a typed reason rather than a generic exception.
- The original file remains unchanged.
- The operation can be exercised in automated tests against temporary fixtures.

---

# Phase A — Metadata Provider Abstraction

## Objective

Replace format-specific branching in higher-level metadata code with a provider abstraction.

### Components

```text
MetadataProvider
├── JAudioTaggerProvider
├── OpusProvider
└── MetadataFormatRegistry
```

`MetadataFormatRegistry` is responsible for selecting the provider for a file format.

Higher-level code should ask the registry for a provider instead of maintaining conditions such as:

```text
if extension == "opus"
```

### Provider responsibilities

Each provider should explicitly describe:

- supported formats;
- read capability;
- write capability;
- supported fields;
- read operation;
- write operation.

The provider should preserve the current title/artist/album scope. Genre is intentionally not part of this foundation.

### Acceptance

- Quick Fix no longer owns format-specific selection logic.
- Each supported format has one clear provider.
- Unsupported formats produce a typed capability result.
- Existing Opus behavior remains covered by tests.
- Existing JAudioTagger behavior remains covered by tests.

---

# Phase B — Typed Write Results

> **Status: Implemented** (`WriteResult` + sealed `TagWriteError`).

## Objective

Make metadata write failures explicit and user-readable.

Introduce:

```text
WriteResult
```

and a sealed error hierarchy such as:

```text
TagWriteError
├── Unsupported
├── Parse
├── Io
├── Locked
├── NotFound
└── Permission
```

The exact Kotlin representation can be refined during implementation, but callers must be able to distinguish capability, parsing, filesystem and permission failures.

## UI behavior

Before Apply, the UI should know whether the selected provider can write the requested fields.

After a failed Apply, the user should receive a message that explains the actual class of failure rather than a raw library exception.

Examples:

```text
Cannot write tags: format is not supported.
```

```text
Cannot write tags: file is locked or unavailable.
```

```text
Cannot write tags: permission denied.
```

```text
Cannot write tags: metadata could not be parsed.
```

## Acceptance

- No metadata write failure is silently swallowed.
- Quick Fix can render a meaningful error without parsing exception strings.
- Capabilities are available before the user confirms Apply.
- Successful Apply still re-reads the file before updating the database cache.

---

# Phase C — Doctor and Integration Testing

> **Status: Implemented** (`doctor` Metadata section + headless `ApplyIntegrationTest`).

## Objective

Expose metadata health in `melody-sync doctor` and make Apply integration-testable without depending on the production database singleton.

### Doctor

Add a Metadata section containing checks such as:

```text
Metadata
✓ Registry available
✓ JAudioTagger provider available
✓ Opus provider available
✓ Required write capabilities registered
```

The exact checks should reflect the providers actually compiled into the application.

### AppState / database injection

The desktop state should be able to receive a test database/repository dependency so the Apply flow can be exercised headlessly:

```text
suggestion
    ↓
QuickFixService.apply
    ↓
TagWriter / MetadataProvider
    ↓
read back
    ↓
MusicRepository.updateByPath
    ↓
AppState refresh
```

The integration test must verify both the filesystem result and the database result.

---

# Phase D — Database Connection Discipline

> **Status: Implemented** (`DatabaseConnection`: idempotent single connection + serialized writes).

## Objective

Remove ad-hoc database connections from the metadata Apply path and establish one controlled connection lifecycle.

Introduce a single `DatabaseConnection` abstraction/factory for the application.

The current plan is to remove the seven ad-hoc `connect()` call sites identified during review.

Writes should be serialized where necessary using a `Mutex` so concurrent tag application cannot produce inconsistent database state.

## Doctor

`melody-sync doctor` should detect multiple/legacy connection paths when practical and report the installation as unhealthy if the expected connection discipline is violated.

## Acceptance

- One controlled database connection strategy exists.
- Apply does not create arbitrary independent connections.
- Concurrent writes are serialized where required.
- Existing SQLite WAL/busy-timeout behavior remains intact.
- Existing database tests continue to pass.

---

# Phase E — Documentation and Fixtures

> **Status: Implemented.** Real per-format fixtures generated with ffmpeg and the verified capability matrix documented in [metadata-formats.md](metadata-formats.md).

## Objective

Document the metadata subsystem and make format behavior reproducible through fixtures.

Create:

```text
MetadataFormats.md
```

with a read/write capability matrix.

Example:

| Format | Read | Write | Provider | Fields |
|--------|------|-------|----------|--------|
| M4A | ✓ | provider-dependent | JAudioTagger | title, artist, album |
| MP3 | ✓ | ✓ | JAudioTagger | title, artist, album |
| Opus | ✓ | ✓ | OpusProvider | title, artist, album |
```

The final matrix must be based on verified behavior, not assumptions.

### Fixtures

Maintain isolated fixtures for each supported format:

```text
fixtures/
├── mp3/
│   ├── with_tags
│   └── no_tags
├── m4a/
│   ├── with_tags
│   └── no_tags
└── opus/
    ├── with_tags
    └── no_tags
```

Fixtures should be copied into temporary directories before destructive/write tests.

Tests must verify:

1. read;
2. diagnose;
3. capability detection;
4. write;
5. re-read;
6. database update;
7. failure classification.

---

# Implementation Order

```text
Step 0 — Diagnostic
       ↓
Phase A — Providers
       ↓
Phase B — Typed Results
       ↓
Phase C — Doctor + Integration Tests
       ↓
Phase D — Database Connection Discipline
       ↓
Phase E — Documentation + Fixtures
```

Do not begin the next phase if the previous phase has unresolved correctness issues.

---

# Non-Goals

This foundation does not include:

- genre field/database migration;
- year field/database migration;
- album cover extraction;
- automatic metadata application;
- automatic YouTube acceptance;
- bulk destructive tag rewriting;
- replacing JAudioTagger wholesale;
- redesigning the Quick Fix UI.

Quick Fix remains explicitly user-approved: suggestions are reports, and Apply is an explicit user action.

---

## Related Documents

- [Roadmap](../ROADMAP.md)
- [Quick-Fix HUD research](../research/quick-fix-hud.md)
- [Security & Resilience Guide](../architecture/SecurityAndResilienceGuide.md)
- [ADR-0005 — Audio Metadata](../architecture/ADR/ADR-0005-Mutagen.md)
- [Documentation Index](../INDEX.md)

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-08 | Initial metadata foundation plan |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**