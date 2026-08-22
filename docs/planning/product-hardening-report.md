# Product Hardening Report

> Robustness probe results: does Melody Sync fail in a comprehensible and safe way?

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | HARDENING-REPORT-001 |
| Category         | Planning / Hardening |
| Status           | Probes complete — no critical findings |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## Probe results

| Case | Probed behavior | Outcome |
|------|-----------------|---------|
| Empty library directory | `scan <empty>` | ✅ 0 songs added, graceful (`Database now holds 0 songs`) |
| Nonexistent directory | `scan <missing>` | ✅ clikt rejects: "Directory must exist and be a valid directory" |
| Corrupt audio file | `metadata <fake.mp3>` | ✅ `Read result: failed`; no crash |
| Corrupt file write-test | `metadata --write-test <fake.mp3>` | ✅ `Write test: failed` — "metadata could not be parsed (No audio header found within fake.mp3)" (typed `TagWriteError.Parse`) |
| Write refused (WAV) | `metadata --write-test <wav>` | ✅ `Write: no` (`TagWriteError.Unsupported`); UI Apply disabled (verified in Product Validation) |
| File removed after scan | delete file, then `health --db` | ✅ `1 orphaned entries` + suggestion "Remove 1 orphaned database entries" |
| Duplicates empty result | distinct-songs library | ✅ empty state covered by UI (EmptyState "No duplicates found"); detection runs |
| Organization name collisions | core `resolveNameCollisions` | ✅ covered by `LibraryOrganizationServiceTest` (suffixes `(2)`, `(3)`, …) |
| Safe target path (traversal) | core `safeTargetPath` | ✅ guarded + `SecurityException` (covered by tests) |
| Provider unavailable (no YouTube key) | app startup | ✅ `youtubeEnabled == false`; app unaffected |
| Permission-denied file | `AccessDeniedException` path | ✅ `classify` returns `TagWriteError.Permission` (typed, unit-covered) |

## Findings

**No critical, security, or data-loss findings.** All probed failures are:

- **comprehensible** (typed `TagWriteError`, clear CLI messages, explicit empty states);
- **safe** (no partial writes that corrupt data, no crashes, destructive actions gated by confirmation);
- **consistent** with the metadata reliability review and Security & Resilience Guide.

### Friction (registered, not blocking)

- **H1 (low):** the "no duplicates" empty result was not directly probed via CLI on a truly distinct library (the shared-tag fixtures group together by design). UI empty state exists; optional CLI wording check later.
- **H2 (informational):** corrupt-file write-test error text includes the technical phrase "No audio header found" — acceptable for a diagnostic tool; keep as-is.

## Next

The product fails safely and clearly on the probed cases. The recommended next stage is **real use** of the library to collect actual friction, then decide the expansion (Metadata Enrichment most likely).

---

## Related Documents

- [Product Hardening](product-hardening.md)
- [Product Validation Report](product-validation-report.md)
- [Security & Resilience Guide](../architecture/SecurityAndResilienceGuide.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**