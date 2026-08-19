# Metadata Reliability Review

> Formal review of metadata write/read reliability across supported formats. Establishes the baseline for considering metadata reliability "consolidated" before moving to performance work.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | PLAN-METADATA-REVIEW-001 |
| Category         | Planning / Review |
| Audience         | Core developers |
| Status           | Active |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-19 |
| Maintainer       | Melody Sync Project |

---

## Purpose

Determine whether the metadata write/read pipeline is reliable enough to consider the metadata foundation "consolidated" (per ROADMAP metadata reliability priority). This review catalogs known issues, identifies gaps in verification, and defines the acceptance criteria for considering metadata reliability "consolidated".

---

## Scope

- **Supported formats**: MP3, FLAC, M4A, OGG, OPUS, WAV (read-only)
- **Operations**: Read (metadata extraction), Write (Quick Fix Apply), Write-test (CLI `metadata --write-test`)
- **Providers**: `JAudioTaggerProvider` (MP3, FLAC, M4A, OGG, WAV), `OpusProvider` (OPUS)
- **Verification**: `metadata --write-test` CLI, `MetadataRoundTripTest`, `FixtureCapabilityTest`

---

## Current State Summary

| Format | Read | Write | Write-test Verification | Status |
|--------|------|-------|------------------------|--------|
| MP3    | ✅   | ✅    | Re-read only           | ✅ Stable |
| FLAC   | ✅   | ✅    | Re-read only           | ✅ Stable |
| M4A    | ✅   | ⚠️    | Re-read only           | ⚠️ Intermittent (K-02) |
| OGG    | ✅   | ✅    | Re-read only           | ✅ Stable |
| OPUS   | ✅   | ✅    | Re-read only           | ✅ Stable |
| WAV    | ✅   | ❌    | N/A (read-only)        | 📖 Read-only (K-03) |

---

## Identified Issues

### K-02 (Pre-existing) — M4A/MP4 intermittent write failures
- **Area**: Metadata · M4A/MP4
- **Symptom**: Some M4A/MP4 files fail to write depending on container layout (JAudioTagger limitation)
- **Status**: Known limitation, documented in ErrorLog (K-02)
- **Current mitigation**: `metadata --write-test` exposes per-file truth; no format-specific fixture in test suite
- **Action needed**: Add M4A fixture to test suite; verify `--write-test` exposes failure

### K-03 (Pre-existing, resolved as read-only) — WAV NUL bytes on read
- **Area**: Metadata · WAV
- **Symptom**: JAudioTagger reads WAV LIST/INFO values with trailing NUL byte
- **Status**: Resolved by normalizing tags in `JAudioTaggerProvider.read` (trim NUL); writes refused (`supportsWrite = false`)
- **Status**: Resolved as read-only

### M-01 (New) — Write path reports success without verifying value persistence
- **Area**: Metadata · Write verification
- **Symptom**: Write path reports success on re-read without verifying the written values actually persisted. The WAV silent-drop bug (K-03) was only caught because round-trip tests explicitly check values. Other providers may silently drop tags.
- **Root cause**: `JAudioTaggerProvider.write` re-reads after write but does not compare the re-read values against the requested values. A "silent drop" (write succeeds but tags are not persisted) returns `WriteResult(updated = song)` with no error.
- **Impact**: Any provider that silently drops tags (like WAV LIST/INFO writer) reports success.
- **Fix applied**: Added `writePersistError` helper in `JAudioTaggerProvider` that compares re-read values against requested values and returns `TagWriteError.Parse` on mismatch.
- **Verification needed**: Add test case for silent-drop scenario; verify M4A/OPUS providers.

### M-02 (New) — Write-test only checks for exceptions, not value persistence
- **Area**: Metadata · Write-test
- **Symptom**: `MetadataDiagnosticService.inspect` write-test reports "passed" if no exception is thrown during write + re-read. Silent drops (like WAV K-03) pass as "passed" because no exception is thrown.
- **Impact**: `metadata --write-test` reports false positives for formats with silent-drop behavior.
- **Fix needed**: Extend `MetadataDiagnosticService.inspect` write-test to compare written values against input; update `MetadataRoundTripTest` to cover all providers.

### M-03 (New) — M4A fixture missing from test suite
- **Area**: Metadata · Test coverage
- **Symptom**: M4A format has intermittent write failures (K-02) but no dedicated fixture in `FixtureCapabilityTest` or `MetadataRoundTripTest` to verify `--write-test` exposes the failure.
- **Action needed**: Add M4A fixture to test fixtures; extend `FixtureCapabilityTest` and `MetadataRoundTripTest` to cover M4A write-test.

---

## Acceptance Criteria for "Metadata Reliability Consolidated"

| Criterion | Status | Evidence Required |
|-----------|--------|-------------------|
| All supported formats have write-test fixtures | ⚠️ Partial | M4A fixture missing (M-03) |
| Write-test verifies value persistence (not just exceptions) | ❌ No | M-02 implemented |
| Silent-drop detection in all providers | ⚠️ Partial | M-01 implemented for JAudioTagger; Opus pending |
| M4A fixture in test suite | ❌ No | M-03 implemented |
| Write-test verifies value persistence in CLI | ❌ No | M-02 implemented |
| No silent-drop bugs in production | ⚠️ Partial | WAV K-03 resolved as read-only; others unproven |

---

## Action Plan

| Task | ID | Priority | Owner | Estimate |
|------|----|----------|-------|----------|
| Add M4A fixture to test fixtures | M-03a | High | Core | 1h |
| Extend `MetadataRoundTripTest` for M4A | M-03b | High | Core | 1h |
| Extend write-test to verify value persistence | M-02 | High | Core | 2h |
| Add silent-drop test case for `JAudioTaggerProvider` | M-01a | High | Core | 1h |
| Verify Opus provider silent-drop behavior | M-01b | Medium | Core | 1h |
| Update `metadata-formats.md` with current status | Doc | Low | Docs | 30m |

---

## Related Documents

- [Error Log](../project/ErrorLog.md) — K-02, K-03, K-04, K-05, K-06
- [Metadata Foundation](metadata-foundation.md)
- [Metadata Formats](metadata-formats.md)
- [Quick-Fix HUD](../research/quick-fix-hud.md)
- [Error Log](../project/ErrorLog.md)

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-19 | Initial metadata reliability review |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**