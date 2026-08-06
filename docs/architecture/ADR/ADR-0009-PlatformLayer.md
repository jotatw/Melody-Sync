# ADR-0009 — Platform Layer

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0009               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.13.0-dev            |
| Template Version | 1.0                    |
| Last Updated     | 2026-08-06             |
| Maintainer       | Melody Sync            |

---

## Summary

Introduce a `com.melodysync.platform` package inside `melody-sync-core`
as a third architectural layer alongside Core and Desktop. It owns the
infrastructure that does not belong to the music domain: shell
execution, installation/update flow, and runtime version discovery.
After this ADR, the layer is frozen until a future ADR explicitly
extends it.

## Context

`melody-sync-core` had grown to mix three concerns:

1. **Domain** — models (`Song`, `DuplicateGroup`, `HealthReport`),
   services that read/write audio files (`LibrarySyncService`,
   `LibraryHealthService`, `DuplicateDetectionService`,
   `LibraryOrganizationService`, `LibraryExportService`).
2. **Persistence** — Exposed/SQLite wiring under `com.melodysync.database`.
3. **Infrastructure** — environment interactions: `LibraryWatcher`,
   `FilenameSanitizer`, and the version-aware components.

Adding the auto-update feature (CLI + GUI) would have forced the third
concern into a new fourth layer of mixed code (process spawning,
installation paths, version handling, INSTALLATION.json, environment
validation). Without a dedicated home, this would drift into either
`core` (polluting domain) or `desktop` (making CLI depend on desktop
classes, which already happens today because `melody-sync-cli` does
not depend on `melody-sync-desktop` and must re-own version logic).

## Decision

Create a new package `com.melodysync.platform` in `melody-sync-core`
that owns everything related to the process running Melody Sync and
the artifacts it produces on disk outside the music library:

```
melody-sync-core
└── com/melodysync/platform/
    ├── installation/   # install/update/repair/validate/detect
    ├── shell/          # ProcessBuilder wrapper for any external command
    └── system/         # runtime version discovery
```

Subpackages:

- **`installation`** — `InstallationPaths`, `InstallationInfo`
  (`INSTALLATION.json`, `@Serializable`, `installerVersion`),
  `InstallationValidator` (env vs. project checks),
  `InstallationResult`, `UpdateCheck`, `InstallationService`
  (the orchestrator). Discovers and rebuilds the install when run
  from a source checkout.
- **`shell`** — `ShellExecutor` (open class, line streaming) and
  `CommandResult` (stdout/stderr/exitCode/duration/command). The only
  abstraction over `ProcessBuilder`; everything that spawns processes
  must use it so logging, tests and future features can share it.
- **`system`** — `VersionInfo`, the runtime access point that reads the
  `melody-sync-version.properties` resource generated from
  `gradle.properties`. Used by Desktop, CLI, Doctor and Settings.

Layer rules:

1. `platform` **may** depend on domain and persistence (it knows about
   nothing musical yet, but must be allowed to read the running JVM,
   `user.dir`, the install directory, etc.).
2. `platform` **must not** depend on `desktop` or `cli` — it stays a
   leaf module so both consumers stay equal.
3. Domain code in `com.melodysync` **must not** depend on
   `com.melodysync.platform`. The boundary is one-way.
4. New infrastructure capabilities (`BackupService`, `LoggingService`,
   `NotificationService`, `FileAssociationService`) are added under
   `platform.<concern>`, not under domain.

## Consequences

Positive:

- CLI and Desktop share the same installer, version and shell
  infrastructure. One fix benefits both.
- The install/update feature is now scoped: any future "download a
  release" mode plugs into `InstallationService` without touching the
  GUI, the CLI or domain code.
- Tests for shell execution, path resolution and installation flows
  run in `melody-sync-core` — no compose-test dependency required.

Negative / trade-offs:

- Adds a third conceptual layer to the project. Mitigated by ADR-0006
  (Documentation Structure) which already calls for explicit layer
  naming.
- `InstallationService` shells out to `bash scripts/install.sh`,
  coupling the platform layer to the project's own bash script. The
  script is intentionally the single place that knows how to publish a
  build, so this coupling is correct; any future "release installer"
  is additive.

## Alternatives Considered

- **Module separation (`melody-sync-platform`)** — would force a
  circular module dependency because `platform` needs core's logging
  (`slf4j`) and serialization, and core needs to stay JVM-friendly.
  Package-level separation inside core achieves the same isolation
  with less ceremony.
- **Put infrastructure inside `desktop`** — would break the CLI's
  access to the installer and force the desktop module to expose
  internals. Rejected.
- **Inline install logic in the GUI** — would duplicate the same
  shell calls in CLI; rejected (this is the situation we are leaving
  behind).

## Status

Accepted and implemented in v0.13.0-dev. The layer is considered
**frozen**: any future addition (e.g. `platform.backup`,
`platform.notifications`) requires its own ADR.

## Related

- `docs/research/update-channels.md` — evolution path that keeps the
  layer frozen while still adding features.
- `melody-sync-core/src/main/kotlin/com/melodysync/platform/` — the
  implementation.
