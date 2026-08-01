# ADR-0003 — Desktop GUI Framework (Compose Desktop)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0003               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync's desktop graphical interface will be built with **Jetpack Compose for Desktop** (a.k.a. Compose Multiplatform Desktop) instead of PySide6 (originally considered in the Python prototype).

This decision is justified by Compose Desktop's modern declarative API, first-class Kotlin integration, hardware-accelerated rendering, and the possibility of sharing UI components with mobile platforms through Kotlin Multiplatform.

---

## Context

The project plans a desktop GUI as part of Milestone 3 (originally). A graphical interface provides a visual way to explore the music library, search by metadata, and view statistics — replacing the need to navigate command-line output for everyday use.

### Current Situation

- The Python prototype considered PySide6 (Qt for Python) as the GUI framework.
- The Kotlin migration (see ADR-0002) opens new GUI possibilities.
- The project targets Linux primarily, with potential expansion to Windows and macOS.

### Constraints

- The framework must integrate well with Kotlin (the chosen language).
- It should support **Linux, Windows, and macOS** with native performance.
- The UI must be **responsive** even with libraries containing thousands of songs.
- A clear path toward **mobile UI sharing** (Android, possibly iOS) is desirable.
- The developer wants to learn a modern UI paradigm.

### Requirements

- Declarative UI API.
- Hardware-accelerated rendering.
- Theming support (dark/light mode).
- Good developer experience with hot reload / preview.
- Active maintenance and a stable release cycle.

---

## Decision

The desktop GUI will use **Compose for Desktop**, the JetBrains port of Jetpack Compose for desktop operating systems.

Compose for Desktop is selected because it provides:

- A **declarative API** (`@Composable` functions) that is more expressive than imperative UI frameworks.
- **First-class Kotlin integration** — the same language used throughout the project.
- **Hardware-accelerated rendering** via Skia (GPU on all major platforms).
- **Native window management** — menus, keyboard shortcuts, system tray, notifications.
- **Material 3 support** out of the box, with a modern appearance.
- A **direct path to mobile** via Compose Multiplatform, allowing UI components to be shared with Android (and eventually iOS) in the future.

The UI will follow the layered architecture: pure Composables for presentation, ViewModels for state, and calls into the `melody-sync-core` module for data access.

---

## Alternatives Considered

### PySide6 (Qt for Python)

**Advantages**

- Mature and widely used.
- Native appearance on each platform.
- Excellent tooling (Qt Designer, Qt Creator).
- Rich widget library.

**Disadvantages**

- Python only — does not survive the migration to Kotlin.
- Imperative API feels dated compared to modern declarative frameworks.
- License considerations (GPL/commercial for some scenarios).
- No path to mobile sharing with the rest of the Kotlin codebase.

**Why rejected:** Not viable after the language migration to Kotlin. The Kotlin ecosystem has better options.

---

### JavaFX

**Advantages**

- Long history in the Java ecosystem.
- FXML allows declarative UI with separation of concerns.
- CSS-based styling.
- Active releases (JavaFX 25 LTS, JavaFX 26 GA in 2026).

**Disadvantages**

- Imperative feel — XML + controllers, similar to old Android development.
- API verbose compared to Compose.
- No direct path to mobile sharing.
- Smaller ecosystem than Compose Desktop in 2026.

**Why rejected:** JavaFX is a viable choice for Java projects, but Compose Desktop offers a more modern API and better integration with Kotlin Multiplatform for future mobile work.

---

### TornadoFX (Kotlin wrapper for JavaFX)

**Advantages**

- Idiomatic Kotlin API over JavaFX.
- Type-safe builders.

**Disadvantages**

- Depends on JavaFX, inheriting its limitations.
- Smaller community than Compose Desktop.
- No path to mobile sharing.

**Why rejected:** Inherits JavaFX's architectural limitations; Compose Desktop is the more strategic choice.

---

### Iced (Rust GUI)

**Advantages**

- Elm-inspired architecture.
- Pure Rust, GPU-accelerated.
- Cross-platform.

**Disadvantages**

- Requires switching the project to Rust.
- Smaller widget library than Compose Desktop.
- No mobile sharing path.

**Why rejected:** Already decided Kotlin over Rust (see ADR-0002). Would require abandoning the Kotlin codebase.

---

### GTK 4 via GTK-rs or Java bindings

**Advantages**

- Native appearance on GNOME.
- Very mature.

**Disadvantages**

- Imperative C-style API.
- System dependency (GTK must be installed on Linux).
- Not portable to mobile.

**Why rejected:** Imperative API and system dependency make it less attractive than Compose Desktop.

---

### Electron / Tauri (Web-based)

**Advantages**

- Familiar web technologies (HTML/CSS/JS).
- Large ecosystem of UI components.

**Disadvantages**

- Heavy memory footprint (~100-300 MB for a typical app).
- Distribution size of ~100 MB+.
- Less native feel.
- Not aligned with Kotlin-first strategy.

**Why rejected:** Contradicts the project's goal of a lightweight, native desktop tool.

---

## Consequences

### Positive

- **Modern API:** Declarative Composables feel natural with Kotlin.
- **Code reuse:** Future mobile UI can share Composables with the desktop app.
- **Hardware acceleration:** Smooth UI even with large music lists.
- **Easy theming:** Material 3 with built-in dark/light support.
- **Type-safe navigation:** Compose Navigation with type-safe routes.
- **Preview support:** IntelliJ IDEA shows UI previews without launching the app.

### Negative

- **Distribution size:** ~50-70 MB for a desktop bundle (includes JVM runtime). Acceptable but larger than native frameworks.
- **Startup time:** ~1-3 seconds for a Compose Desktop app (vs. ~500ms for native Qt). Acceptable for a personal tool.
- **Ecosystem maturity:** Compose Desktop is newer than JavaFX; some advanced components may need custom implementations.
- **Documentation gaps:** Some edge cases are less documented than mature alternatives.

### Risks

- **Compose Desktop API stability:** Compose APIs have evolved rapidly. **Mitigation:** Pin to a specific Compose version; review before upgrades.
- **Performance with large lists:** Lazy components (`LazyColumn`) must be used correctly. **Mitigation:** Follow Compose performance best practices; add performance tests.
- **Distribution complexity:** Packaging for Linux (DEB/RPM/AppImage) requires careful Gradle configuration. **Mitigation:** Use the official `compose.desktop` Gradle plugin with `nativeDistributions`.

---

## Implementation Notes

- The UI layer depends on `melody-sync-core` for data access; it must not contain business logic.
- ViewModels (`androidx.lifecycle.ViewModel`) manage UI state and survive configuration changes.
- Navigation uses **Compose Navigation** with type-safe routes (introduced in Navigation 2.8+).
- Theming uses **Material 3** with explicit dark/light color schemes.
- Internationalization (i18n) will use `composeResources` for string management.
- The desktop module will be a separate Gradle module: `melody-sync-desktop`.

---

## References

- [Compose Multiplatform Documentation](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Jetpack Compose for Desktop](https://github.com/JetBrains/compose-multiplatform)
- [Material 3 Design](https://m3.material.io/)
- ADR-0002 — Programming Language (Kotlin)
- ADR-0008 — Gradle Kotlin DSL

---

## Related Documents

- `docs/INDEX.md`

---

## Revision History

| Version   | Date       | Description                                  |
|-----------|------------|----------------------------------------------|
| v0.1.0    | 2026-07-15 | Initial placeholder (PySide6 considered)     |
| v0.2.0    | 2026-07-31 | Decision revised: Compose for Desktop        |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
