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

### V1 — Health does not surface files that only have a filename title

- **Severity:** Medium (friction / usefulness gap)
- **Class:** Friction → Product Validation Report (not fixed during validation)
- **Observation:** On a library of untagged files, Health reported `0 songs without title/artist`. The diagnosis only flags a file when **both** title and artist are missing. Untagged files get a filename fallback for title, so a whole library of untagged files shows "no metadata issues" even though artist/album are empty.
- **Suggested direction (later):** surface files whose title equals the filename fallback, or that are missing artist/album, so "needs attention" is meaningful for untagged collections.

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