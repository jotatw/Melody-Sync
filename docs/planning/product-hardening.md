# Plan: Product Hardening

> Short robustness review: verify Melody Sync fails in a **comprehensible and safe way** on error and edge cases, before real use and any expansion.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | PLAN-HARDENING-001 |
| Category         | Planning / Hardening Block |
| Status           | In progress |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## Purpose

The product now works end-to-end (Product Validation passed). This block asks one question:

> Does Melody Sync fail in a comprehensible and safe way?

It is **not** about building a large error-handling system. It tests representative failures and confirms each is surfaced clearly, does not corrupt data, and does not crash.

## Cases to probe

| Case | Expected behavior |
|------|-------------------|
| Empty library directory | Scan reports 0 songs gracefully; Health/Stats empty states |
| Nonexistent directory | CLI argument rejects with a clear message |
| Corrupt/invalid audio file | Read reports failure; write-test fails with typed reason; no crash |
| Write refused (WAV) | `Write: no`, `TagWriteError.Unsupported`, UI disables Apply (verified in validation) |
| Permission-denied file | Failure classified (Permission), no crash |
| File removed during operation | Health reports orphaned entries; safe |
| Organization conflicts (name collisions) | `resolveNameCollisions` dedupes; safe target path guard (core-tested) |
| Provider unavailable (no YouTube key) | YouTube disabled; app unaffected |
| Empty result (no duplicates) | Clear empty state, not a crash |
| Invalid metadata (garbage tags) | Read best-effort; no crash |

## Method

- Extend `scripts/validate-workflow.sh` with a hardening section (or a sibling script) for the CLI-probeable cases.
- Reuse core unit coverage for the paths already tested (organize collisions, safe target path, WAV refusal).
- Record anything that fails obscurely or risks data as a finding (same classification as Product Validation: critical/security/regression fix immediately; friction registers).

## Deliverable

`docs/planning/product-hardening-report.md` — probe results and any findings.

---

## Related Documents

- [Product Validation](product-validation.md)
- [Product Validation Report](product-validation-report.md)
- [Security & Resilience Guide](../architecture/SecurityAndResilienceGuide.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**