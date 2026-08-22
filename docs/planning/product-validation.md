# Plan: Product Validation

> End-to-end validation of the Melody Sync workflow on a realistic library. This is not a new feature: it verifies the consolidated product as a whole and produces a prioritized list of real frictions before any expansion.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | PLAN-VALIDATION-001 |
| Category         | Planning / Validation Block |
| Audience         | Developers, UX, maintainer |
| Status           | Approved — ready to execute |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |
| Maintainer       | Melody Sync Project |

---

## Purpose

Answer two different questions about the current product:

```text
Automated validation
→ Does the system work correctly?

Guided GUI validation
→ Does the product work well for a person using it?
```

One does not substitute the other. The consolidated base (metadata reliability, performance, responsive behavior, semantic colors, workflow refinements) is the object under test. The block must reveal the *next* important work with more confidence than choosing a feature would.

---

## Relationship to the Roadmap

The `docs/project/product-roadmap.md` records the **Consolidation & UX Refinement** cycle as complete. This block is the next stage:

```text
Product Validation
    ↓
Product Hardening
    ↓
Real use
    ↓
Expansion decision (Metadata Enrichment most likely)
```

---

## Rules of the Block

- **Automation** validates repeatable, objective behavior.
- **Guided GUI checklist** validates experience, context, and clarity.
- **Frictions found are not fixed automatically during this block.** They are registered, classified, and prioritized.

### Classification of findings

| Class | Action |
|-------|--------|
| Critical bug | Fix immediately |
| Security / data-loss problem | Fix immediately |
| Regression | Fix immediately + add test |
| Friction / improvement | Register in the Product Validation Report → prioritize later |

This prevents the block from turning into an uncontrolled implementation effort:

```text
VALIDATE → REGISTER → CLASSIFY → (fix only critical/security/regression) → report
```

---

## Track 1 — Automated Validation

### 1.1 Build a realistic test library

Create a temporary library directory combining:

- existing fixtures under `melody-sync-core/src/test/resources/fixtures/audio/` (mp3, flac, m4a, ogg, opus, wav);
- synthetic cases:
  - files without metadata;
  - incomplete metadata (title only, artist only, missing album);
  - bad/ugly filenames (`01 - Track.MP3`, `_mixed_case_.Flac`, spaces and symbols);
  - duplicate groups (same title/artist, close duration);
  - a WAV file (read-only write path);
  - an unknown-format file (e.g. `.txt`) to confirm it is treated as non-audio.

### 1.2 Drive the CLI end-to-end

Use `melody-sync` CLI commands against the temporary library and assert outcomes:

| Step | Command | Verified outcome |
|------|---------|------------------|
| Scan | `scan <dir>` | songs discovered; DB populated; counts match |
| Health | `health <dir>` | issues found: missing metadata, zero duration where present |
| Metadata inspect | `metadata --write-test <file>` | per-format read/write truth, persistence verified |
| Quick Fix path | apply a suggestion to a copy | tags written, read-back matches (persistence) |
| Duplicates | `duplicates <dir>` | duplicate groups detected |
| Organize | `organize <dir>` then `--apply` on a copy tree | plan → apply → files moved, library refresh |
| Doctor | `doctor` | registry/write-capabilities checks pass (WAV read-only) |

Assertions are the same class already covered by core tests (TagWriter round-trip, write-test persistence, reorganize). The purpose here is the **full chain in one library**, not new unit coverage.

### 1.3 Regression checks

Run the full suite before and after (`./gradlew test`) — no regressions. Optionally record wall-time for scan/health on the test library for a coarse performance sanity note.

### 1.4 Deliverable

A repeatable harness script that builds the library, runs the chain, and prints a pass/fail summary. This keeps Track 1 re-runnable and CI-able.

**Implemented:** `scripts/validate-workflow.sh` — builds a realistic library (fixtures + duplicate pair + messy filename + non-audio file), runs scan → health → per-format write-test → duplicates → organize (dry-run) → doctor, and reports `[PASS]`/`[FAIL]` per step. Run with `--keep` to preserve the work dir for the guided GUI pass. Exit code 0 when all checks pass.

**Baseline (2026-08-22):** 12 checks passing (scan discovers files; health analyzes; write-test persists for mp3/flac/m4a/ogg/opus; WAV write refused; duplicates run; organize plan produced; doctor healthy).

---

## Track 2 — Guided GUI Validation

A human walks the desktop application over the same realistic library, answering questions per flow.

### Data cases to include

- real files;
- different formats (mp3, flac, m4a, ogg, opus, wav);
- files without metadata;
- incomplete metadata;
- bad filenames;
- duplicate files.

### Flow questions

For each step of:

```text
Scan → Health → find problem → Review → Quick Fix → Apply → verify → Health refresh → Library updated → Organize (when needed)
```

check:

- is the problem found?
- is it clear to the user?
- is the right action easy to find?
- does Quick Fix provide enough context?
- is the result evident?
- does the problem disappear after the correction?
- does navigation preserve context?

### Limits to observe

- what still requires manual work?
- where are steps repetitive?
- where does the interface show too much information?
- where is information missing?
- is there any implemented function that does not help the real flow?

### Context & navigation checks

- Statistics → Library drill-down (single filter chip, "X of Y songs");
- Health → Library issue context (selection vs filter);
- Review loading state and selection guidance;
- compact/medium/expanded window behavior;
- keyboard navigation and shortcuts;
- status colors not the only signal (semantic + label/icon).

---

## Deliverable

`docs/planning/product-validation-report.md`:

- summary of automated results (pass/fail per step);
- guided GUI checklist outcome;
- classified findings:
  - critical / security / regression (fixed immediately during execution);
  - friction items, ranked by severity and frequency;
- recommended next step (which expansion or targeted fix the frictions point to).

---

## Acceptance Criteria

| Criterion | Definition of done |
|-----------|--------------------|
| Automated chain runs | scan → health → metadata/write-test → duplicates → organize all execute on the realistic library with asserted outcomes |
| No regressions | full suite green before and after |
| GUI checklist executed | each flow question answered for at least one representative case |
| Report produced | `product-validation-report.md` lists findings classified and prioritized |
| No speculative expansion started | findings only registered/prioritized; only critical/security/regression fixes applied during the block |

---

## Out of Scope

- new features or expansions;
- building a large automated test suite (reuse existing coverage);
- implementing the frictions found (deferred to the report);
- Metadata Enrichment, Playback, new fields, or Artwork (future, gated on the report).

---

## Related Documents

- [Product Roadmap](../project/product-roadmap.md) — stage context
- [ROADMAP](../ROADMAP.md) — operational state
- [Metadata Reliability Review](metadata-reliability-review.md) — metadata baseline
- [Metadata Formats](metadata-formats.md) — format matrix
- [Repository Audit 2026-08](../project/audit-2026-08.md) — prior snapshot

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-22 | Initial Product Validation plan (automated + guided GUI, classification rules, deliverables) |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**