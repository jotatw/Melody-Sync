# ADR-0007 — CLI Framework (clikt)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0007               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync's command-line interface will be built with **clikt** (`com.github.ajalt.clikt:clikt`), a Kotlin-first CLI framework.

clikt provides an idiomatic Kotlin API for building complex command-line applications with subcommands, typed options, and rich help output, while being fully multiplatform-capable.

---

## Context

The project began as a simple Python CLI (`music scan <folder>`) and the command-line interface remains important for power users and scripting. The CLI allows scanning libraries, exporting statistics, and future automation.

### Current Situation

- The Python prototype uses Typer (Click-based) for CLI argument parsing.
- Rich (Python) provides terminal formatting (tables, progress bars, panels).
- The Kotlin migration (ADR-0002) requires a new CLI framework.

### Constraints

- Must be a Kotlin/Java library (not Python).
- Must support subcommands (`melody-sync scan`, `melody-sync version`, future `melody-sync tag`).
- Must handle typed options and arguments.
- Must generate high-quality help output automatically.
- Should support colored output for a pleasant terminal experience.

---

## Decision

Use **clikt** version 5.x as the CLI framework.

### Why clikt

- **Kotlin-first design:** The API is idiomatic Kotlin — options are declared as properties, not annotations.
- **Multiplatform:** Runs on JVM, Native, and JS targets — aligned with the KMP strategy.
- **Rich feature set:** Nested subcommands, typed options, validation, autocomplete, prompt support, and automatic help generation.
- **Maintained by ajalt:** Same author as the Mordant library (terminal colorization), ensuring consistent quality.
- **No code generation:** Everything is plain Kotlin code with sensible defaults.

---

## Alternatives Considered

### kotlinx-cli (JetBrains)

**Advantages**

- Official JetBrains library.
- Multiplatform.

**Disadvantages**

- **Officially obsolete** — the README explicitly states: "This library is obsolete. It is effectively unmaintained."
- Missing autocomplete, colors, and prompt features.
- API marked `@ExperimentalCli`.

**Why rejected:** Unmaintained. The project explicitly recommends using other libraries.

---

### picocli

**Advantages**

- Popular Java CLI framework (5k+ stars).
- Mature, rich feature set.
- GraalVM native-image support.

**Disadvantages**

- **Java-first:** Annotation-based API feels less idiomatic in Kotlin.
- Not Kotlin Multiplatform (JVM only).
- More verbose for simple commands.

**Why rejected:** While solid, the annotation-based API and JVM-only support make it less aligned with the Kotlin-first, multiplatform strategy.

---

### JCommander

**Advantages**

- Simple, annotation-based.
- Popular (1.5k stars).

**Disadvantages**

- Java-only.
- Fewer features (no autocomplete, no ANSI colors).
- Less actively maintained than clikt.

**Why rejected:** Less feature-rich and less Kotlin-idiomatic than clikt.

---

### Command-Line Parsing By Hand

**Advantages**

- Zero dependencies.
- Full control.

**Disadvantages**

- Error-prone parsing logic.
- No automatic help generation.
- No type conversion.
- Time-consuming to maintain.

**Why rejected:** The project should focus on library organization features, not reimplementing argument parsing.

---

## Consequences

### Positive

- **Kotlin-idiomatic API:** Subcommands are just `CliktCommand` subclasses with properties.
- **Type safety:** Options are typed (Int, Path, Choice, Enum) at compile time.
- **Multiplatform alignment:** clikt runs on JVM and Native, compatible with future KMP targets.
- **Rich help output:** Automatic help generation with usage, options, and subcommands.
- **Autocomplete:** Built-in support for bash/zsh/fish autocompletion scripts.

### Negative

- **Learning curve:** clikt's command-based API differs from Typer's decorator style.
- **Smaller ecosystem:** Fewer third-party integrations than Click/Typer (Python) or picocli (Java).

### Risks

- **Version churn:** clikt 5.x introduced breaking changes from 4.x. **Mitigation:** Pin the version in Gradle and review before upgrades.

---

## Implementation Notes

- CLI lives in a separate module: `melody-sync-cli`.
- CLI commands delegate to `melody-sync-core` for business logic.
- Use **Mordant** (same author as clikt) for terminal colorization and table output, matching Rich's capabilities in Python.
- Structure commands as:

```
melody-sync
├── scan <directory>   # Scan a music library and show statistics
├── export <format>    # Export library metadata (JSON/CSV/YAML)
├── tag <file>         # Edit tags (future)
└── version            # Show version
```

Command sketch:

```kotlin
class ScanCommand : CliktCommand(help = "Scan a music library directory") {
    val directory by argument("directory", help = "Music library to scan")
        .path().check { it.isDirectory() }

    val recursive by option("-r", "--recursive").flag()

    override fun run() {
        val songs = LibraryScanner(directory).scan()
        StatisticsPrinter(songs).print()
    }
}
```

---

## References

- [clikt Documentation](https://ajalt.github.io/clikt/)
- [Mordant (terminal formatting)](https://github.com/ajalt/mordant)
- ADR-0002 — Programming Language (Kotlin)
- ADR-0008 — Gradle Kotlin DSL

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