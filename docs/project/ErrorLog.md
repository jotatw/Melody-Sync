# Error Log

> Running record of bugs and issues found during development, their root cause and how they were fixed.

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | project/error-log |
| Category         | Project |
| Audience         | Developers |
| Status           | Active |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-19 |
| Maintainer       | Melody Sync |

---

## Purpose

Keep a searchable record of the problems we hit so the same class of error is not diagnosed from scratch twice. Each entry captures the symptom, the root cause, the fix and how it was verified. Unresolved issues are listed separately so they are not forgotten.

## Summary

| ID | Area | Issue | Status |
|----|------|-------|--------|
| E-01 | Desktop · Sidebar | Rail expanded to fill the whole window | ✅ Fixed |
| E-02 | Desktop · Fonts | App froze loading fonts | ✅ Fixed |
| E-03 | Desktop · State | Crash on fullscreen + tab switch during scan | ✅ Fixed |
| E-04 | Desktop · Preferences | Theme toggle reset letter grouping | ✅ Fixed |
| E-05 | Core · Kotlin | KDoc with `/*` produced "Unclosed comment" | ✅ Fixed |
| E-06 | Desktop · State | `setUpdateChannel` JVM signature clash | ✅ Fixed |
| E-07 | Platform · Release | Download jar threw `IOException` | ✅ Fixed |
| E-08 | Core · Compile | `ByteArrayOutputStream.write(Byte)` | ✅ Fixed |
| E-09 | Core · Opus | Opus parser read vendor length incorrectly | ✅ Fixed |
| E-10 | Core · Opus | Opus writer included the next page header | ✅ Fixed |
| E-11 | Core · Model | `Song.directory` NPE for parentless paths | ✅ Fixed |
| E-12 | Build · RPM | RPM rejects `-` in the version | ✅ Fixed |
| E-13 | Metadata · Apply | Apply failed on some original files | ✅ Fixed |
| E-14 | Build · Version | Version defined in three places | ✅ Fixed |
| E-15 | Database | Ad-hoc DB connects in seven places; no write serialization | ✅ Fixed |
| E-16 | Metadata · AAC | `.aac` had no JAudioTagger reader but was listed as supported | ✅ Fixed |

## Fixed Errors

### E-01 — Sidebar rail filled the whole window

- **Area:** Desktop · Sidebar
- **Symptom:** In windowed mode the navigation rail expanded to occupy the full layout.
- **Root cause:** Material 3 `NavigationRail` measures with `widthIn(min = 80.dp)` and grows to fit its labels; inside an unbounded `Row` there was nothing to stop it.
- **Fix:** Pin the rail width: `Modifier.width(if (expanded) 200.dp else 80.dp)` on the `Sidebar`.
- **Verification:** Build + GUI boot; rail no longer dominates.

### E-02 — App froze loading fonts

- **Area:** Desktop · Fonts
- **Symptom:** The app hung at startup while resolving typography.
- **Root cause:** `FontFamily(path)` was interpreted as a system font name; the lookup never returned for the bundled TTFs.
- **Fix:** Load fonts from the classpath with `androidx.compose.ui.text.platform.Font(resource, weight)`.
- **Verification:** GUI boot; fonts render.

### E-03 — Crash on fullscreen + tab switch during scan

- **Area:** Desktop · State
- **Symptom:** Concurrent snapshot corruption when switching sections during a scan.
- **Root cause:** State was being written from a background thread.
- **Fix:** `AppState` uses `uiScope = Dispatchers.Main.immediate` for state writes and `ioScope = Dispatchers.Default` for background work; heavy work runs in `withContext(Dispatchers.Default)`.
- **Verification:** Scan + section switch under fullscreen; no crash.

### E-04 — Theme toggle reset letter grouping

- **Area:** Desktop · Preferences
- **Symptom:** Toggling the theme silently reset "Group songs by first letter" to off.
- **Root cause:** `Main.kt` `onToggleTheme` rebuilt `AppPreferences` without `groupByLetter`.
- **Fix:** Include `groupByLetter` (and later `updateChannel`) in the theme save; moved to a shared `savePrefs()`.
- **Verification:** Toggle theme, grouping persists.

### E-05 — "Unclosed comment" from a KDoc

- **Area:** Core · Kotlin
- **Symptom:** `TrashService.kt` failed to compile with `Syntax error: Unclosed comment` at EOF.
- **Root cause:** Kotlin comments nest; the KDoc text `info/*.trashinfo` opened an inner `/*` so the outer `*/` closed the inner comment.
- **Fix:** Reworded the comment to avoid the `/*` sequence.
- **Verification:** Clean compile.

### E-06 — `setUpdateChannel` JVM signature clash

- **Area:** Desktop · State
- **Symptom:** Compile error "Platform declaration clash".
- **Root cause:** `var updateChannel` (with a private setter) generates `setUpdateChannel`; an explicit `fun setUpdateChannel` collided at the JVM level.
- **Fix:** Renamed the function to `selectUpdateChannel`.
- **Verification:** Clean compile + tests.

### E-07 — Download jar threw `IOException`

- **Area:** Platform · Release installer
- **Symptom:** `BodyHandlers.ofFile(..., CREATE_NEW)` failed with `IOException: <temp path>` during release downloads.
- **Root cause:** The file-sink BodyHandler surfaced a bare `IOException` with no useful detail.
- **Fix:** Download via `BodyHandlers.ofInputStream()` and copy manually, wrapping errors with a clear message.
- **Verification:** `ReleaseInstallerTest` against an in-process GitHub stub.

### E-08 — `ByteArrayOutputStream.write(Byte)` does not compile

- **Area:** Core · Compile
- **Symptom:** `write(it.size.toByte())` failed — no `write(Byte)` overload.
- **Root cause:** Kotlin does not widen `Byte` to `Int` for the `write(Int)` overload.
- **Fix:** `write(it.size)` (the size is already an `Int`).
- **Verification:** Clean compile.

### E-09 — Opus parser read the comment count from the wrong offset

- **Area:** Core · Opus
- **Symptom:** Real `.opus` files returned no tags.
- **Root cause:** After reading the vendor-length field, the cursor was advanced by the value but the vendor *string* bytes were not skipped, so the comment count was read from the wrong position.
- **Fix:** `cursor += 4 + vendorLength`.
- **Verification:** `OpusMetadataTest`; read a real user `.opus` file (`The Shore` → title/artist/album).

### E-10 — Opus writer included the next page header in the packet

- **Area:** Core · Opus
- **Symptom:** `OpusMetadata.writeTags` returned `false` on valid files.
- **Root cause:** The packet slice started at the page boundary, so the first bytes were the *next* page's Ogg header, not the `OpusTags` packet content.
- **Fix:** Track `packet1ContentStart` (after the page header) separately from the drop start.
- **Verification:** `OpusMetadataTest` write cases; real-file copy write + re-read.

### E-11 — `Song.directory` NPE for parentless paths

- **Area:** Core · Model
- **Symptom:** `NullPointerException: getParent(...) must not be null` for paths without a parent.
- **Root cause:** `Song.directory` was declared non-null `Path` but returned `path.parent` (null for relative paths).
- **Fix:** `val directory: Path?`.
- **Verification:** `SongMatcherTest`; callers already safe-call.

### E-12 — RPM rejects `-` in the version

- **Area:** Build · Packaging
- **Symptom:** Configuration failed: `'0.12.0-dev' is not a valid version` for RPM.
- **Root cause:** RPM package versions cannot contain `-`.
- **Fix:** Sanitized `rpmPackageVersion = version.substringBefore('-')`; the uber jar keeps the full dev version.
- **Verification:** `./gradlew` configure + package.

### E-13 — Apply failed on some original files

- **Area:** Metadata · Quick Fix Apply
- **Symptom:** "incapaz de determinar o começo do audio no arquivo" (JAudioTagger `CannotReadException`/`CannotWriteException`) when applying to certain songs.
- **Root cause:** Two compounding causes: (1) JAudioTagger has no Opus support at all, so the single `.opus` file in the library could not be read/written; (2) the installed build was older than the Opus fix, so even previously-working formats appeared broken.
- **Fix:** Built an Ogg/OpusTags reader (`OpusMetadata.read`) and writer (`OpusMetadata.writeTags`) and routed Opus through `OpusProvider`; reinstalled the app from current source.
- **Verification:** `melody-sync metadata --write-test` reports `Write test: passed` for the real `.opus`, `.m4a` and `.mp3` files; `TagWriterTest` + `OpusMetadataTest`.

### E-14 — Version defined in three places

- **Area:** Build · Versioning
- **Symptom:** `scripts/install.sh`, the Compose `packageVersion` and the CLI all reported different versions (`0.10.0`, `0.12.0-dev`, `v0.6.0-dev`).
- **Root cause:** No single source of truth.
- **Fix:** `melodySyncVersion` in `gradle.properties`, consumed by the build (package version), a generated runtime resource (`VersionInfo`) and `install.sh`.
- **Verification:** `melody-sync version` and `doctor` report the same version.

### E-15 — Ad-hoc database connections and unserialized writes

- **Area:** Database
- **Symptom:** `MusicDatabase.connect()` was called in seven places from background threads; concurrent tag application and watcher resyncs could interleave writes.
- **Root cause:** No single connection lifecycle; every operation reconnected and replaced the default Exposed database; writes were not serialized.
- **Fix:** Introduced `DatabaseConnection` — idempotent `connect` per URL (connect once, reuse; tests may switch files) and a `ReentrantLock` write lock. `MusicRepository` write methods serialize through it; `AppState` connects through the single entry point.
- **Verification:** `DatabaseConnectionTest` (idempotency, file switching, lock) + full suite; `doctor` reports the connection discipline.

## Known Issues (unresolved)

| ID | Area | Issue | Plan |
|----|------|-------|------|
| K-02 | Metadata | Some M4A/MP4 files can still fail to write depending on the container layout (JAudioTagger limitation). | Phases A–B (done) expose typed capabilities and `WriteResult`; `metadata --write-test` reports the per-file truth — monitor for format-specific fixtures |
| K-03 | Metadata · WAV | JAudioTagger reads WAV LIST/INFO values with a trailing NUL byte (e.g. `Fixture Song\u0000`). | Documented in the format matrix; callers trim the NUL when displaying |
| K-04 | Metadata · Write verification | Write path reports success on re-read without verifying the written values actually persisted. The WAV silent-drop bug (K-03) was only caught because round-trip tests explicitly check values. Other providers may silently drop tags. | Add post-write verification hook in `TagWriter` that re-reads and compares written values; convert silent drops to typed `WriteResult` errors. Register as M-01. |
| K-05 | Metadata · M4A write failures | Some M4A/MP4 files fail to write depending on container layout (JAudioTagger limitation). The write-test does not distinguish "unsupported format" from "write failed silently". | Add M4A fixture to test suite; verify `metadata --write-test` exposes the failure. Tag as K-02 (already tracked) but add format-specific fixture. |
| K-06 | Metadata · Write-test gap | Current write-test only re-reads to check for exceptions, not for value persistence. Silent drops (like WAV K-03) pass as "passed" because no exception is thrown. | Extend `MetadataDiagnosticService.inspect` write-test to compare written values against input; update `MetadataRoundTripTest` to cover all providers. Tag as M-02. |

## Related Documents

- [Metadata Foundation](../planning/metadata-foundation.md)
- [History](History.md)
- [Development Methodology](../standards/handbook/DevelopmentMethodology.md)
- [Security & Resilience Guide](../architecture/SecurityAndResilienceGuide.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
