# ADR-0008 — Build System (Gradle Kotlin DSL)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0008               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync will use **Gradle** with the **Kotlin DSL** as its build system, following the standard for Kotlin/Kotlin Multiplatform projects.

---

## Context

The project needs a build system that supports the planned architecture: a shared core module, a CLI module, a desktop GUI module, and a potential mobile module in the future.

### Current Situation

- The Python prototype uses `setuptools` (via `pyproject.toml`) and `pip`.
- The Kotlin migration (ADR-0002) requires a JVM-capable build tool.
- IntelliJ IDEA is the primary IDE.

### Constraints

- Must support Kotlin (JVM target initially, Multiplatform in the future).
- Must support multi-module projects (core, cli, desktop).
- Must integrate well with IntelliJ IDEA.
- Must handle dependency management (Maven Central, JitPack).
- Should support test execution with JUnit.

---

## Decision

Use **Gradle** with the **Kotlin DSL** (`build.gradle.kts`).

### Why Gradle

- **Standard for Kotlin:** Official Kotlin Gradle Plugin (KGP) and Kotlin Multiplatform Plugin are built for Gradle.
- **Multi-module support:** First-class support for the planned module structure.
- **Performance:** Incremental builds, build cache, daemon, configuration cache.
- **Flexibility:** Extensible via plugins (Compose Desktop plugin, serialization plugin, etc.).
- **Kotlin DSL:** Type-safe build scripts with IDE autocompletion — the same language as the project code.

### Why not Maven

Maven supports Kotlin but:
- Its XML syntax is less readable than Gradle's Kotlin DSL.
- Kotlin Multiplatform support is weaker (requires third-party plugins).
- Compose Desktop plugin is not supported by Maven.

---

## Alternatives Considered

### Maven

**Advantages**

- Very mature and widely used in Java ecosystem.
- Predictable convention-over-configuration.
- Stable dependency resolution.

**Disadvantages**

- **No official Kotlin Multiplatform support** — requires `kotlin-maven-plugin`.
- **No Compose Desktop plugin** — GUI builds would require manual configuration.
- **XML verbosity** — `pom.xml` files are verbose.
- **Slower builds** — no daemon, weaker incremental compilation.

**Why rejected:** Cannot support the full Kotlin ecosystem (KMP, Compose) as well as Gradle.

---

### Bazel / Buck

**Advantages**

- Extremely fast incremental builds.
- Reproducible builds.

**Disadvantages**

- **Complex configuration** — steep learning curve.
- **Overkill** for a personal project.
- Smaller community and fewer Kotlin integrations.

**Why rejected:** Unnecessary complexity for a personal project; Gradle handles the scale perfectly.

---

### Plain Kotlin Compiler + Custom Scripts

**Advantages**

- Zero build tooling.
- Full control.

**Disadvantages**

- No dependency management.
- No module system.
- Manual test execution.
- No KMP or Compose support.

**Why rejected:** Dependency management and multi-module support are essential.

---

## Consequences

### Positive

- **Type-safe builds:** Kotlin DSL validates build scripts at compile time.
- **Multi-module ready:** `melody-sync-core`, `melody-sync-cli`, `melody-sync-desktop` can be built independently or together.
- **Plugin ecosystem:** Access to Kotlin, Compose, and serialization plugins.
- **IDE integration:** IntelliJ IDEA has first-class Gradle support.

### Negative

- **Learning curve:** Gradle's DSL is complex; errors can be cryptic.
- **Build speed:** First build downloads dependencies and warms up; subsequent builds benefit from the daemon.
- **Configuration drift:** Complex Gradle configurations can become hard to maintain without discipline.

### Risks

- **Plugin version compatibility:** Kotlin/Compose plugin versions must be aligned. **Mitigation:** Pin versions in a `libs.versions.toml` (Gradle Version Catalog); test upgrades in isolation.
- **Configuration cache issues:** Some plugins may not support Gradle's configuration cache. **Mitigation:** Enable it gradually and verify with `--configuration-cache`.

---

## Implementation Notes

- Use the **Gradle Version Catalog** (`gradle/libs.versions.toml`) to centralize dependency versions.
- Use `settings.gradle.kts` with `include` declarations for each module.
- Configure JVM target 21 for all modules.
- Enable the Gradle **build cache** and **configuration cache** where possible.
- Distribution for desktop via the Compose Gradle plugin's `nativeDistributions`.

Module structure:

```kotlin
// settings.gradle.kts
rootProject.name = "melody-sync"

include(":melody-sync-core")
include(":melody-sync-cli")
include(":melody-sync-desktop")
```

---

## References

- [Gradle Documentation](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Kotlin Gradle Plugin](https://kotlinlang.org/docs/gradle-configure-project.html)
- ADR-0002 — Programming Language (Kotlin)

---

## Related Documents

- `docs/INDEX.md`

---

## Revision History

| Version   | Date       | Description                     |
|-----------|------------|---------------------------------|
| v0.2.0    | 2026-07-31 | Initial version                 |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**