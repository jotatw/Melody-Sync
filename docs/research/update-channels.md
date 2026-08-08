# Update Channels & Release Installer — Implemented

> Implemented release installer and Stable/Beta/Nightly update-channel architecture; unattended updates remain backlog.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | research/update-channels |
| Category         | Research               |
| Audience         | Developers             |
| Status           | Implemented            |
| Project Version  | v0.13.0-dev            |
| Last Updated     | 2026-08-08             |
| Maintainer       | Melody Sync            |

---

## Status

**Implemented (core + GUI + CLI).** Release-mode updates are available:

- CLI: `melody-sync update --channel stable|beta|nightly`.
- GUI: Settings → Updates has a channel selector (Stable/Beta/Nightly)
  persisted in `AppPreferences.updateChannel`; the update flow uses it.
- The release workflow publishes `.sha256` files so releases can be
  verified (zip-integrity fallback when missing).
- The release installer downloads, verifies and installs published builds
  with rollback support.

### Remaining backlog

The installed update system is not yet an unattended automatic-update
system. The following remain intentionally deferred:

- routine unattended background update checks;
- automatic installation policy;
- restart/relaunch orchestration after an unattended update.

These items are backlog work and do not change the implemented release
installer or channel architecture.

## What this is

The `platform.installation` layer provides the installation and update path
for both source-based development installations and published releases.

The implemented release channels are:

```text
stable   — published GitHub Release
beta     — pre-release GitHub Release
nightly  — latest development release channel
```

The channel selection is exposed through the same installation/update flow,
while the platform layer remains isolated according to ADR-0009.

## Implemented Flow

1. `InstallationService` determines the installation/update strategy.
2. Release-mode installation selects the requested Stable/Beta/Nightly
   channel.
3. `ReleaseInstaller` obtains the published jar and checksum, verifies the
   download and swaps the installed artifact atomically with rollback support.
4. The GUI and CLI expose the same channel-aware update behavior.
5. Installation metadata records the active installation information.

## Frozen Platform Boundary

The update implementation remains inside the existing `platform.installation`
layer. The architecture defined by ADR-0009 remains frozen: core/domain code
does not depend on platform implementation details.

Unattended updates are therefore treated as an incremental extension of the
existing installation flow rather than a reason to redesign the platform
layer.

## Related

- ADR-0009 — Platform Layer
- `docs/ROADMAP.md`
- `docs/INDEX.md`

This document follows the Melody Sync Documentation Standard.

**End of Document**
