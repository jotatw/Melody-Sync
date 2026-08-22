# Product Validation Report

> Outcome of the Product Validation block (see `product-validation.md`). Automated results are verified; the guided GUI checklist is provided for a human pass over the preserved library.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | VALIDATION-REPORT-001 |
| Category         | Planning / Validation |
| Status           | Findings registered; GUI checklist pending human completion |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## 1. Automated Results

Harness: `scripts/validate-workflow.sh` — **12/12 checks passing** on a realistic library (fixtures for mp3/flac/m4a/ogg/opus/wav + duplicate pair + messy filename + non-audio file).

| Step | Result |
|------|--------|
| Scan discovers audio and persists to DB | ✅ (15 audio files) |
| Health analyzes and reports counts | ✅ |
| Write-test persists for mp3 / flac / m4a / ogg / opus | ✅ |
| WAV write refused (read-only) | ✅ |
| Duplicates detection runs | ✅ |
| Organize produces a dry-run plan | ✅ |
| Doctor healthy | ✅ |
| Desktop GUI boot (smoke, ~18s) | ✅ no crash/exceptions |

Preserved library for the GUI pass: `--keep` output (temp dir reported by the harness); database included.

## 2. Findings (classified)

Per the block rules, only critical/security/regressions are fixed immediately; the rest are registered for prioritization.

### V1 — Health DOES surface untagged files (original report was a harness artifact)

- **Severity:** Resolved (validation-harness bug, fixed)
- **Class:** Fixed during validation (the report's own tooling was wrong, not the product)
- **Observation:** An early run reported `0 songs without title/artist` on a library of untagged files. Root cause: the harness invoked `health` **without `--db`**, so it analyzed the default (empty) database instead of the scanned test database. With the correct `--db`, Health surfaces the untagged files correctly (`hasMetadata` = title **and** artist present; filename fallback for title counts as a title, but a missing artist flags the file). The harness now passes `--db` and asserts untagged files are surfaced (7 in the realistic library). **13/13 checks pass.**

### V1b — Health does not surface album-only gaps or filename-fallback titles

- **Severity:** Low (friction / refinement)
- **Class:** Friction → register and prioritize later (not fixed during validation)
- **Observation:** `hasMetadata` requires only title and artist. A file with real title + artist but **missing album**, or a file whose title is only the filename fallback but has an artist tag, is not flagged by Health. Quick Fix's `diagnose` already tracks `MissingField.ALBUM`; Health's aggregate count lags behind it.
- **Suggested direction (later):** align Health's aggregate metadata issue with `QuickFixService.diagnose` (title/artist/album), so "needs attention" is consistent between Health and Review.

### V2 — Duplicate heuristic groups by title+artist+duration (documented)

- **Severity:** Informational
- **Class:** Expected behavior (fixture artifact)
- **Observation:** Fixtures that intentionally share tags ("Fixture Song" / "Fixture Artist", similar durations) group together across formats. This matches the documented heuristic; real libraries would differ. Not a defect.

### V3 — AAC treated as non-audio (documented)

- **Severity:** Informational
- **Class:** Expected behavior
- **Observation:** `.aac` counted as non-audio (2 non-audio = `no_tags.aac` + `readme.txt`), consistent with the format matrix.

### V4 — Duplicate pair detected correctly

- **Severity:** Informational
- **Class:** Verified behavior
- **Observation:** the byte-identical `copy1.mp3`/`copy2.mp3` appear in the duplicate group.

### V5 — Local suggestion coverage on real YouTube-derived filenames

- **Severity:** Informational → input to the Metadata Enrichment decision
- **Class:** Measured behavior (locked by `SongMatcherRealNamesTest`)
- **Observation (real library, 13 no-artist files):** the local `SongMatcher` recovers the correct artist for **8/13** via the `Artist - Title` separator heuristic. **5/13 fail** because the heuristic does not handle: en-dash separators (`Hiroyuki Sawano – …`), underscores-as-spaces (`JoJo_s Bizarre Adventure…`), `[ORIGINAL] X - Y` inversions, and `"… by X"` phrasing. These are systematic, not one-offs.
- **Implication:** ~62% of this library's untagged MP3s are fixable without leaving Quick Fix; the remainder are exactly the case for **Metadata Enrichment** (YouTube identification) or a matcher improvement (en-dash/underscore normalization).

## 3. Guided GUI Checklist (for a human pass)

Use the preserved `--keep` library. Answer each item; register observations under "GUI findings" below.

- [ ] Scan runs from the UI and shows progress/result clearly
- [ ] Health result is understandable (score, issue lists, next actions)
- [ ] Opening Review shows loading state, then items; selecting a song shows the guidance hint first, then Quick Fix
- [ ] Quick Fix provides enough context (diagnosis, suggestion source, values to write)
- [ ] Apply writes, read-back verifies, and the issue disappears after refresh
- [ ] Library updates after the fix and preserves the current filter/context
- [ ] Statistics → Library drill-down opens with a single filter chip and "X of Y songs"
- [ ] Duplicates flow: groups visible, confirmation required, result evident
- [ ] Organize: plan review → apply → result → library resync
- [ ] No screen shows too much or too little information for the task
- [ ] Status colors are never the only signal (label/icon present)
- [ ] Compact/medium/expanded window behavior is intact

### GUI findings

(To be completed by the human pass.)

## 4. Next Steps

1. Complete the guided GUI checklist on the preserved library.
2. Prioritize V1 (Health usefulness for untagged libraries) against other friction found.
3. Proceed to **Product Hardening** (safe failure behavior) once the GUI pass is done.
4. Real use → expansion decision.

---

## Related Documents

- [Product Validation](product-validation.md)
- [Product Roadmap](../project/product-roadmap.md)
- [Metadata Reliability Review](metadata-reliability-review.md)

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-22 | Automated results + findings V1–V4; GUI checklist prepared |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**