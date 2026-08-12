# ADR-0002 — Programming Language (Kotlin)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0002               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync will be developed in **Kotlin** (JVM target), replacing the initial Python prototype.

This decision is justified by Kotlin's balance between developer productivity, performance, type safety, and the possibility of sharing code across desktop and mobile platforms in the future through Kotlin Multiplatform.

---

## Context

Melody Sync began as a personal Python script for scanning a local music library and exporting metadata. As the project's scope evolved — including CLI improvements, a future GUI, and potential mobile support — the limitations of Python for this specific use case became more apparent.

### Current Situation

- The prototype is implemented in Python 3.14 using Mutagen, Typer, and Rich.
- The codebase is well-organized into layered architecture (scanner, models, UI).
- 54 automated tests cover the core functionality.
- All planned features (desktop GUI, mobile, configuration system) require a more robust foundation.

### Constraints

- The project must remain **simple to develop and maintain** as a personal tool.
- The language should provide **type safety** to prevent common bugs with optional metadata.
- **Cross-platform** support (initially Linux, possibly Windows/macOS, possibly mobile) is desirable.
- The language ecosystem must offer **mature libraries for audio metadata extraction**.
- The developer is willing to learn new languages, but the learning curve must be reasonable.

### Requirements

- Static typing with good inference.
- A mature CLI framework.
- A modern GUI framework for desktop.
- A real path toward mobile sharing (Android/iOS) without rewriting the core.
- Access to high-quality audio metadata libraries.
- Productivity similar to or greater than Python for this domain.

---

## Decision

Melody Sync will be written in **Kotlin** (version 2.1+), compiled to JVM bytecode, with a long-term goal of becoming a **Kotlin Multiplatform** project.

Kotlin is chosen because it satisfies all technical requirements while offering:

- A gentle learning curve for developers who already know Java or Python.
- Modern language features (null safety, data classes, coroutines, sealed classes).
- Full access to the Java ecosystem (libraries, tools, community).
- A mature path toward code sharing via Kotlin Multiplatform.
- Type safety that prevents runtime errors common in metadata processing.

The migration will be **incremental**: the new Kotlin code will coexist with the existing Python prototype until the core functionality is validated, after which the Python code may be archived.

---

## Alternatives Considered

### Python (Keep Current Language)

**Advantages**

- Already implemented with 54 passing tests.
- Mutagen is the gold standard for audio metadata.
- Simple to develop and iterate quickly.
- Excellent developer experience for scripts and small tools.

**Disadvantages**

- No static typing — bugs only surface at runtime.
- GIL limits true parallelism for large library scans.
- GUI options (PySide6, Tkinter) feel dated or have licensing concerns.
- Mobile via Kivy or BeeWare is immature compared to native solutions.
- No clear path to code sharing across desktop and mobile.

**Why rejected:** Type safety and the multiplatform path are essential for the project's long-term evolution. Staying in Python would require accepting these limitations.

---

### Java (Direct JVM Language)

**Advantages**

- Mature and stable platform.
- Vast ecosystem and community.
- Excellent tooling (IntelliJ IDEA, Maven, Gradle).
- Virtual Threads (Project Loom) since Java 21.

**Disadvantages**

- Verbose syntax requires more code for the same functionality.
- No null safety — `NullPointerException` is a constant risk with optional metadata.
- No built-in equivalents for Kotlin's data classes, sealed classes, or coroutines.
- No native path to iOS (only through commercial Gluon Mobile).

**Why rejected:** While Java meets all technical requirements, Kotlin offers the same ecosystem with significantly better ergonomics. Java would not be a wrong choice, but Kotlin is the more modern option for new projects.

---

### Rust

**Advantages**

- Excellent performance (native compilation).
- Single static binary (~15-30 MB) — ideal for CLI distribution.
- Memory safety without garbage collection.
- Strong type system and pattern matching.
- Lofty crate is a mature alternative to Mutagen.

**Disadvantages**

- **Steep learning curve** — ownership and lifetimes are a significant barrier.
- Slower development velocity for someone coming from Python.
- GUI frameworks (Iced, egui, Slint) are functional but not as mature as Qt/JavaFX/Compose.
- No native path to mobile sharing — Rust core + native UI required.
- Build times are noticeably longer than Kotlin/Java.

**Why rejected:** The project's priority is developer productivity and learning, combined with the pleasure of having something functional quickly. Rust's learning curve would slow this down considerably. The performance benefit does not justify the productivity cost for a personal library manager.

---

### Go

**Advantages**

- Simple, readable syntax.
- Excellent concurrency primitives (goroutines).
- Single static binary.
- Fast compilation.

**Disadvantages**

- Less mature ecosystem for audio metadata libraries.
- No Kotlin Multiplatform equivalent — no path to mobile sharing.
- Less expressive type system than Kotlin for domain modeling.
- No GUI framework as mature as Compose Desktop.

**Why rejected:** Go lacks the audio metadata ecosystem and the multiplatform path that make Kotlin attractive.

---

## Consequences

### Positive

- **Type safety:** Null safety catches entire categories of bugs at compile time.
- **Coroutines:** Native support for concurrent I/O without callback hell.
- **Data classes:** Models like `Song` and `LibraryStatistics` become one-liners instead of 50-line Python dataclasses with manual methods.
- **JVM ecosystem:** Access to JAudioTagger, SQLite JDBC, and thousands of other mature libraries.
- **KMP path:** Future possibility to share core with Android (and potentially iOS) without rewriting.
- **Tooling:** IntelliJ IDEA provides best-in-class Kotlin support.

### Negative

- **Steeper initial setup:** Gradle, JDK, IDE configuration required vs. Python's `pip install`.
- **Larger distribution:** CLI binary will be ~40-70 MB with embedded JRE vs. Python's script + dependencies.
- **Slower startup:** ~1-3 seconds vs. Python's instant startup (acceptable for a desktop tool).
- **Learning curve:** New language for the developer, even though Kotlin is one of the easiest non-Python options.

### Risks

- **Mature Kotlin version drift:** Kotlin evolves quickly; some APIs deprecated between versions. **Mitigation:** Pin to a specific Kotlin version in `gradle.properties` and review before major upgrades.
- **Migration cost:** Existing Python code is well-tested but must be reimplemented. **Mitigation:** Keep the Python version functional during migration; treat Kotlin as a parallel implementation until validated.
- **iOS compatibility:** Kotlin Multiplatform iOS support is improving but not yet fully stable. **Mitigation:** Not pursuing iOS in the current scope; revisit when the feature is actually needed.

---

## Implementation Notes

- Target JVM 21 (LTS) to benefit from Virtual Threads.
- Use Gradle Kotlin DSL as build system (see ADR-0008).
- Structure the project as a Kotlin Multiplatform module from the start, even if only `jvmMain` is active in v0.2.0. This makes future platform additions trivial.
- Port tests alongside code, aiming for equivalent coverage (54+ tests).
- Keep the Python implementation in a `legacy/` or `archive/` branch during migration for reference.

---

## References

- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Project Loom (Java Virtual Threads)](https://openjdk.org/projects/loom/)
- ADR-0001 — Project Vision
- ADR-0003 — Compose Desktop
- ADR-0005 — JAudioTagger
- ADR-0007 — clikt
- ADR-0008 — Gradle Kotlin DSL

---

## Related Documents

- `docs/INDEX.md`
- `docs/standards/handbook/DevelopmentMethodology.md`

---

## Revision History

| Version   | Date       | Description                              |
|-----------|------------|------------------------------------------|
| v0.1.0    | 2026-07-15 | Initial placeholder (Python selected)    |
| v0.2.0    | 2026-07-31 | Decision revised: migration to Kotlin    |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
