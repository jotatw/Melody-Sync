# Update Channels & Release Installer — Vision

> Future evolution path for the `platform.installation` layer.
> **Not implemented** — recorded so the architecture stays frozen
> while still leaving room to grow.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | research/update-channels |
| Category         | Research               |
| Audience         | Developers             |
| Status           | Vision                 |
| Project Version  | v0.13.0-dev            |
| Last Updated     | 2026-08-06             |
| Maintainer       | Melody Sync            |

---

## Status

**Implemented (core).** Release-mode updates are available: the CLI
(`melody-sync update --channel stable|beta|nightly`) and the GUI
Settings → Updates download the latest published jar, verify the sha256
(zip-integrity fallback) and install it with launcher + symlink +
desktop entry. The release workflow publishes `.sha256` files so
releases can be verified.

What remains open (registered, not yet done):

- Channel selection UI in Settings (the GUI currently uses STABLE; the
  CLI accepts `--channel`).
- `UpdateChannel` pre-selection persists to `AppPreferences`.
- Publishing stable vs beta/nightly releases as a routine workflow
  (release.sh bumps the version and tags; the workflow marks
  `-dev`/`-beta`/`-rc` tags as pre-releases).

## What this is

`InstallationService` (ADR-0009) is currently single-channel: it
recompiles and reinstalls from a Melody Sync source checkout, writing
`INSTALLATION.json` with `sourceBased=true`.

Future versions may offer additional **update channels** so users who
do not build from source can still get updates:

```
stable   — published GitHub Release, signed jar
beta     — pre-release GitHub Release
nightly  — built from the latest main commit
```

The implementation should hide the channel selection behind a
`ReleaseInstaller` strategy that produces the same `InstallationResult`
as the current source installer.

## Flow (future)

1. `InstallationService.update(...)` is parameterized by an
   `Installer` strategy (default: `SourceInstaller`,
   future: `ReleaseInstaller(channel = Stable/Beta/Nightly)`).
2. `ReleaseInstaller` queries the GitHub Releases API
   (`https://api.github.com/repos/jotatw/Melody-Sync/releases/latest`),
   picks the matching jar asset, downloads it to a temp directory,
   verifies the SHA-256 included in the release, then atomically swaps
   the jar in `~/.local/share/melody-sync/`.
3. The GUI's "Check for updates" already returns
   `UpdateCheck(updateAvailable = true/false)`; the channel only changes
   where the bytes come from.
4. INSTALLATION.json gains a `channel` field (set to "source" today,
   "release:stable/beta/nightly" later) and `installerVersion` is
   bumped when its format changes.

## Why deferred

- Requires a release publishing pipeline (build + upload jar to GitHub
  Releases), which is out of scope for the "rebuild and install" loop.
- Requires authentication (Personal Access Token or `gh` CLI) for
  publishing, which is not currently configured.
- The source-based flow already serves the developer-and-immediate-user
  case fully, which is the project's actual audience today.

## Prerequisites to implement later

1. A release pipeline (`scripts/release.sh` or a CI workflow) that:
   - builds the uber jar
   - signs it
   - uploads the asset + checksum + version metadata to a GitHub
     Release tagged `vX.Y.Z`.
2. A new `Installer` abstraction with `SourceInstaller` (current
   behavior) and `ReleaseInstaller` (new behavior) implementations.
3. Channel configuration in Settings (`AppPreferences.channel =
   "source" | "release:stable" | "release:beta" | "release:nightly"`).
4. End-to-end test that exercises both installers against a stub
   release server.

## How the frozen `platform` layer supports this

Because `InstallationService` already exposes `detect()` and `update()`
as the only public methods, and `ShellExecutor` is the single point
that spawns processes, adding `ReleaseInstaller` requires:

- one new file under `platform/installation/`
- no changes to the GUI, CLI, domain code or shell layer

The layer separation documented in ADR-0009 is what makes this
additive.
