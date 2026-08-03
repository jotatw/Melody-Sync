# Application Design Research

> Design research and best practices applied to the Melody Sync desktop application.

> **Note:** The visual identity is now defined by `docs/standards/DesignSystem.md` (Hi-Fi Editorial). This document records the research that led to the current UX decisions.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Category         | Architecture / Design  |
| Audience         | Developers             |
| Status           | Draft                  |
| Project Version  | v0.12.0-dev            |
| Last Updated     | 2026-08-03             |

---

## Overview

This document records the research done on how to structure, design and improve the user experience of a Compose Desktop application. It focuses on practical conclusions applied to Melody Sync, not generic theory.

---

## 1. Navigation

### Findings

- Material 3 provides a **Navigation Rail** (`NavigationRail`, `WideNavigationRail`) designed for medium-to-large screens — ideal for desktop apps with 3-7 top-level destinations.
- A rail is more compact than a full sidebar and collapses gracefully.
- Destinations should be **mutually exclusive sections**, each with a clear icon + label.
- The active destination is indicated with a pill-shaped indicator (`NavigationRailItem`).

### Applied to Melody Sync

Current implementation uses a custom `Sidebar` with fixed width (180dp expanded / 48dp collapsed) and an expand/collapse toggle.

**Recommendation for evolution:**

- Adopt Material 3 `NavigationRail` as the base (it already provides the selected indicator, hover states and consistent sizing).
- Keep the collapse behavior: `WideNavigationRail` (labels) when expanded, `NavigationRail` (icons only) when collapsed.
- Top-level destinations (confirmed): Library, Statistics, Health, Duplicates, Organize.
- Move the expand/collapse toggle to the rail itself or the top app bar.

---

## 2. Layout & Scaffolding

### Findings

- Material 3 `Scaffold` provides the standard app structure: top bar, content, optional navigation rail / drawer.
- `TopAppBar` should host: title, primary actions (icons), and overflow menu for secondary actions.
- Keep **one primary action** per screen; group the rest in menus to avoid clutter.
- Consistent spacing: use Material 3 spacing scale (4dp base) rather than arbitrary values.

### Applied to Melody Sync

Current layout: custom `Row` with `Sidebar` + `Column` (TopBar, DirectoryBar, SearchBar, content).

**Recommendation for evolution:**

- Use `Scaffold` with:
  - `topBar = TopAppBar` (title + theme toggle + sidebar toggle + overflow menu)
  - `navigationRail = { ... }` (or the custom sidebar while it exists)
  - content area
- Move "Scan" and "Watch" actions into a **toolbar row** below the top bar (they are directory-scoped, not app-wide).
- Move per-section actions (Analyze Health, Detect Duplicates, Plan Organize) into their section headers with `Button`/`FilledTonalButton`.

---

## 3. State Management

### Findings

- Keep UI state in a **single state holder** (ViewModel-style) per screen; testable and lifecycle-aware.
- Separate **pure logic** (filters, sorting, calculations) from **side effects** (DB, network, file system).
- Prefer immutable state + explicit events over mutable globals.
- Persist user preferences (last directory, theme, sort, section) — done via `AppPreferences`.

### Applied to Melody Sync

- `AppState` is the single state holder (already in place).
- Pure logic (`filteredSongs`, `comparatorFor`) is computed from state — good.
- Side effects run in coroutines on `Dispatchers.Default` — good.

**Recommendation for evolution:**

- Extract pure functions into the core module for unit testing (e.g., `SongFilter`, `SongSorter`).
- Consider `androidx.lifecycle.ViewModel` for desktop when the app grows; not required now.

---

## 4. Song List UX

### Findings

- A music library list benefits from: column headers, sorting, search, and a **letter index** (jump to a letter) — common in large lists.
- Feedback states matter: **empty state** (no directory), **loading**, **results**, **no results for search**.
- Long titles must truncate with `maxLines = 1` + ellipsis.
- Keyboard navigation (arrow keys) is expected in lists.

### Applied to Melody Sync

Implemented: sortable headers (▲/▼), `LetterIndex` A–Z with scroll-to-letter, search filter.

**Recommendation for evolution:**

- Add explicit **empty states** with guidance text + icon (not just text).
- Add a "no results for query" state distinct from "no songs loaded".
- Consider keyboard navigation over the list.
- Group by letter visually (sticky headers) as an alternative to the index.

---

## 5. Feedback & Progress

### Findings

- Long-running operations (scan, health, duplicates) need **visible progress** (indeterminate bar is fine) and a clear **completion result**.
- Errors should be shown inline (not only in a dialog) with a recoverable message.
- All destructive operations (move, delete) must be **explicitly confirmed** or, for a personal tool, **report-first** (dry-run) — Melody Sync already follows report-first everywhere.

### Applied to Melody Sync

- Scan/Health/Duplicates/Organize show `LinearProgressIndicator` while running and a summary after.
- Errors surface in the section body.

**Recommendation for evolution:**

- Standardize a `ProgressCard`/`ResultCard` component reused by all sections.
- Add a **Snackbar** for transient notifications (e.g., "3 songs added").

---

## 6. Theming

### Findings

- Material 3 color schemes: light/dark. Dark should follow the system (KDE `kdeglobals` luminance) — implemented.
- Typography and spacing should come from the theme, not hardcoded.
- Icons: use Material Icons; ensure `contentDescription` for accessibility.

### Applied to Melody Sync

- `AppTheme` detects system theme (KDE luminance + GNOME fallback), toggle persists.
- Components use `MaterialTheme.typography` and `colorScheme`.

**Recommendation for evolution:**

- Consider a custom accent color tied to the music theme (e.g., the icon purple/pink).
- Keep the theme toggle; optionally persist in preferences (already done).

---

## 7. Project Structure (Desktop Module)

### Findings

- Group files by **feature/concern**, not by UI layer only:
  ```
  desktop/
    Main.kt            # entry point
    theme/             # AppTheme
    state/             # AppState, AppPreferences, enums
    ui/
      LibraryScreen.kt # top-level screen composition
      components/      # reusable UI components
  ```
- Keep components small and single-responsibility; extract repeated markup.

### Applied to Melody Sync

Current structure matches this already (theme/, state/, ui/components/).

**Recommendation for evolution:**

- As the app grows, split `ui/components/` into `ui/library/`, `ui/health/`, `ui/settings/` by section.
- Move section screens to `ui/sections/` (LibrarySection, HealthSection, ...).

---

## 8. UX Principles Checklist (Melody Sync)

- [x] Report-first for any file modification (organize, duplicates, enrichment).
- [x] Persistent preferences (directory, theme, section, sort, sidebar).
- [x] Search with clear button.
- [x] Sortable columns with direction indicator.
- [x] Letter index for large lists.
- [x] Progress indicators for long operations.
- [x] System dark mode detection (KDE + GNOME).
- [x] Navigation Rail (Material 3) — replaces the custom sidebar.
- [x] Distinct empty states (no directory / no search results).
- [x] Snackbar for transient messages.
- [ ] Keyboard navigation in lists.
- [ ] Reusable ProgressCard/ResultCard component.
- [ ] Inline error messages with recovery hints.

---

## References

- Material 3 — Navigation Rail: https://m3.material.io/components/navigation-rail
- Material 3 — Top App Bar: https://m3.material.io/components/top-app-bar
- Compose Desktop docs: https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-desktop.html
- ADR-0003 — Desktop GUI Framework (Compose Desktop)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**