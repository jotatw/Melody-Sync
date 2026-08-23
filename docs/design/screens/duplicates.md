# Duplicates — Interaction Model

> Contextual workflow for duplicate detection and review. Reached through Health; not a primary navigation destination.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Screen Specification |
| Audience | Developers / UX |
| Status | Implemented / contextual via Health (removed from sidebar) |
| Project Version | v0.13.0-dev |
| Primary navigation | None — contextual (Health → Duplicates) |
| Related screens | Health, Library |
| Related documents | [app-design.md](../app-design.md) |

---

## 1. Purpose

Duplicates identifies groups of songs that are likely duplicates and gives the user a reviewable context before any file is moved to trash.

The screen is intentionally conservative: detection is a candidate-finding operation, not permission to delete files.

---

## 2. User Question

> **Are there duplicate songs, and which copy should stay?**

The user should be able to answer:

- Are there duplicate groups?
- What songs belong to each group?
- Which copy should be kept?
- Which file will be moved to trash?
- Did the action succeed?

---

## 3. Responsibilities

Duplicates currently provides a workflow for:

- displaying detected duplicate groups;
- showing the songs that belong to each group;
- selecting/reviewing duplicate candidates;
- presenting an explicit action to move a selected file to trash;
- showing operation progress and result state;
- requiring confirmation before a destructive filesystem action;
- refreshing the duplicate analysis after a file is removed.

---

## 4. Non-Responsibilities

Duplicates must not:

- automatically delete files;
- silently move files to trash;
- modify metadata;
- reorganize the library;
- decide which copy is authoritative without user input;
- implement a second duplicate-detection algorithm in the UI.

The UI marks one candidate per group as **KEEP** (the file with the most complete metadata, then the largest size) as a *suggestion only* — the user can still trash it. The primary is not selectable for trash to prevent accidental removal of the recommended copy.

---

## 5. Entry Points

- **Health → Duplicates**: the Health screen shows a duplicate-groups summary card (group count + action); the full workflow opens from there.
- There is no sidebar destination for Duplicates.

---

## 6. Screen Structure

What the user sees:

- duplicate groups (one per detected set);
- the songs in each group;
- per-group selection/reviewable candidates;
- an explicit action for the selected candidates (move to trash);
- progress and result feedback for the operation;
- confirmation before any destructive action.

---

## 7. Primary Actions

- select/review duplicate candidates;
- confirm and move a selected file to trash;
- refresh/rerun duplicate detection;
- recover from an error state.

---

## 8. States and Empty States

The workflow reports `Idle`, `Running`, `Done`, and `Error` for the detection operation:

- **Idle** — no analysis in progress.
- **Running** — detection in progress (re-running is guarded against concurrent runs).
- **Done** — groups are displayed for review.
- **Error** — detection failed; the error is surfaced without pretending the analysis happened.

### Empty State

When no duplicate groups are found, the screen should communicate this as a successful analysis result rather than as an empty technical list.

A useful state is conceptually:

```text
No duplicates found

The analyzed library currently contains no detected duplicate groups.
```

If a last-analysis timestamp is available, it may be shown as supporting information.

### User Flow

```text
Duplicates
    │
    ▼
Duplicate groups
    │
    ▼
Inspect candidates
    │
    ▼
Select action
    │
    ▼
Confirm
    │
    ▼
Move to trash
    │
    ▼
Refresh library state
```

The confirmation step is mandatory for filesystem removal/trash actions.

---

## 9. Contextual Interactions

- **Health → Duplicates**: Health hosts the duplicate-groups summary card and opens the workflow contextually.
- **After trash**: the library and duplicate analysis are refreshed; remaining groups are re-derived. The workflow displays a local success/error outcome with the number of groups remaining; this result must not depend solely on a transient notification.

---

## 10. Navigation Rules

Duplicates is not a primary navigation destination. Health shows a duplicate-groups summary card (group count + action), and the full workflow — group inspection, explicit confirmation, trash/recovery behavior and progress feedback — opens from there.

---

## 11. Data Interaction

- **Consumes:** the current library song list; duplicate groups produced by the Core `DuplicateDetectionService`.
- **Can change:** the filesystem only via an explicit, confirmed trash action. The heuristic is based on normalized title and artist together with a duration tolerance; the UI must present the result as a **likely duplicate**, not as mathematical proof that two files are identical.

---

## 12. UX Rules

- Detection result must be labeled as candidates, never as certainty.
- Destructive actions always require explicit confirmation.
- Progress and result state must be visible during and after the operation.
- The empty state reads as a positive analysis result.

---

## 13. Accessibility Notes

- Group and candidate lists must be keyboard-navigable.
- Confirmation dialogs must be fully operable without a mouse.
- State changes (running/done/error) should be announced, not only shown by color.

---

## 14. Decision Rules

- No file is moved to trash without confirmation.
- Duplicate analysis is deterministic per library snapshot and re-runs after removal.
- Do not promote Duplicates to a sidebar destination while the contextual workflow remains simple enough for Health (see `app-design.md`).

---

## Related Documents

- [Health](health.md)
- [Library](library.md)
- [Application Design](../app-design.md)
- [Roadmap](../../ROADMAP.md)