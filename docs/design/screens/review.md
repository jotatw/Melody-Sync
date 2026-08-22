# Review — Interaction Model

> Contextual workflow for inspecting songs that need attention before they return to the normal library workflow. Reached through Health and Library; not a primary navigation destination.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Screen Specification |
| Audience | Developers / UX |
| Status | Implemented / contextual via Health (removed from sidebar) |
| Project Version | v0.13.0-dev |
| Primary navigation | None — contextual (Health → Review) |
| Related screens | Health, Library, Quick Fix |
| Related documents | [app-design.md](../app-design.md), [quick-fix-hud.md](../../research/quick-fix-hud.md) |

---

## 1. Purpose

Review is a focused workspace for inspecting songs that require attention before they return to the normal library workflow.

The screen is not an independent source of truth. It consumes diagnostic/review information from the application state and routes corrective actions through existing Core capabilities.

---

## 2. User Question

> **Which songs need attention, and why?**

The user should be able to answer:

- Which songs have issues?
- What kind of issue each song has?
- Can I filter the review list?
- Can I inspect a selected song?
- Can I open the corrective workflow for it?

---

## 3. Responsibilities

Review currently provides a focused workflow for:

- listing review items;
- filtering review items;
- selecting a song;
- inspecting the selected song;
- opening the Quick Fix context for the selected song;
- applying an explicit metadata fix through Quick Fix;
- returning the updated song to the normal library state.

---

## 4. Non-Responsibilities

Review must not:

- implement a second metadata-writing system;
- decide metadata automatically;
- silently apply a suggestion;
- replace Health diagnostics;
- replace Library browsing;
- directly implement YouTube or lyrics access.

---

## 5. Entry Points

- **Health → Review**: the primary entry, when the user acts on an issue category.
- **Library contextual**: song-level actions may open Review for the selected context.

Review must preserve the reason the user entered (issue filter or selected song). See `app-design.md` "Context Preservation".

---

## 6. Screen Structure

What the user sees:

- the review list (songs with issues across the whole library);
- filter controls for issue categories;
- per-song issue indicators;
- a selected-song inspection area;
- the Quick Fix context when a song is selected.

---

## 7. Primary Actions

- filter the review list;
- select a song;
- inspect the selected song;
- open Quick Fix for the selected song;
- apply an explicit metadata fix.

---

## 8. States and Empty States

Review items are derived in memory from the current library (no file IO). The screen reflects:

- **Loading** — while the diagnosis pass runs (right after a scan/load), the screen shows a progress state rather than a premature "nothing to review".
- **Ready with items** — review list populated from `reviewItems`. When items are present but no song is selected, the right panel shows a short guidance line ("Select a song to review a suggested fix.") instead of empty space.
- **Empty** — no songs require attention; communicate that there is nothing requiring attention rather than displaying an empty technical table. The empty state should not create a new action if there is no useful action to perform.
- **Per-song Quick Fix states** — applying/loading feedback is owned by the Quick Fix context, not by Review itself. Apply failures surface as a local error pill inside the Quick Fix panel (not only a transient snackbar), and clear when another song is selected.

### User Flow

```text
Review
  │
  ├── inspect issue
  │
  ├── select song
  │       │
  │       ▼
  │   Quick Fix
  │       │
  │       ├── local suggestion
  │       └── optional YouTube suggestion
  │
  └── return to review/library state
```

The important boundary is that Review identifies the item that needs attention, while Quick Fix performs the assisted correction workflow.

---

## 9. Contextual Interactions

- **Health → Review**: Health actions navigate to Review with the affected issue context.
- **Review → Quick Fix**: Quick Fix is opened for the selected song; corrections apply explicitly.
- **Refresh**: the review list refreshes after library changes and tag application.

---

## 10. Navigation Rules

Review is not a primary navigation destination. It is reached contextually through Health and Library:

```text
Health
  ↓
review issue
  ↓
Library / Quick Fix
```

The review capability remains fully reachable through this contextual flow.

---

## 11. Data Interaction

- **Consumes:** `reviewItems` (songs with issues across the library), produced by the application state from Core diagnostics. It is an in-memory derivation; it does not read directly from the database.
- **Can change:** nothing by itself. Corrections flow exclusively through Quick Fix, which writes metadata back to files; the review state is refreshed afterwards.

---

## 12. UX Rules

- Surface the reason a song needs attention before offering actions.
- Preserve filter/selection state when crossing into Quick Fix and returning.
- Do not duplicate Health's broader diagnostics; Review narrows to actionable items.

---

## 13. Accessibility Notes

- Filters and the song list must be keyboard-navigable.
- Issue indicators must not rely on color alone (semantic text/label required).
- Quick Fix status changes should be announced, not only visually shown.

---

## 14. Decision Rules

- A song opens Quick Fix only when a corrective action is available.
- Review keeps items in sync with the library after any applicable state change.
- Do not promote Review to a sidebar destination while its contextual flow remains simple enough for Health.

---

## Related Documents

- [Library](library.md)
- [Health](health.md)
- [Quick Fix HUD](../../research/quick-fix-hud.md)
- [Application Design](../app-design.md)
- [Roadmap](../../ROADMAP.md)