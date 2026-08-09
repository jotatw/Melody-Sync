# Navigation — Interaction Model

> Official navigation contract for Melody Sync.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Navigation |
| Audience | Developers / UX |
| Status | Defined / Target Navigation |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

This document defines how users move through Melody Sync and how context is preserved between screens.

The navigation model separates **primary destinations** from **contextual workflows**.

Primary navigation represents stable user goals. Contextual workflows represent actions that arise from those goals and should not automatically become permanent sidebar destinations.

---

## 2. Primary Navigation

The target primary navigation is:

```text
Library
Statistics
Health
Organize
────────────
Settings
About
```

### Primary destinations

| Destination | User goal |
|---|---|
| Library | Work with individual songs |
| Statistics | Understand the composition of the library |
| Health | Find what needs attention |
| Organize | Plan and execute file organization |
| Settings | Configure application behavior |
| About | Learn about the application/project |

The current implementation may still expose Review and Duplicates as sidebar items. Those are treated as **navigation-consolidation work**, not as additional long-term primary goals.

---

## 3. Contextual Workflows

Contextual workflows are entered from a primary destination and preserve the reason the user started the action.

Current examples:

```text
Health
  ├── Review issue
  └── Review duplicates

Library
  └── Quick Fix

Statistics
  └── Explore in Library
```

These workflows should not require the user to manually reconstruct context after navigation.

---

## 4. Core Navigation Model

```text
                    ┌─────────────┐
                    │   Library   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
         Quick Fix      Health       Organize
                           │
                    ┌──────┴──────┐
                    ▼             ▼
                 Review       Duplicates

Statistics ───────────────────────► Library

Settings / About
      │
      └── independent informational/configuration flow
```

The Library is the central operational workspace. Health and Statistics may route the user into Library with context.

---

## 5. Context Preservation

Whenever navigation is caused by an action on data, the destination must preserve the relevant context.

### Health → Library

```text
Health
  ↓
Missing metadata · 14 songs
  ↓
Review
  ↓
Library
  ↓
issue filter = missing metadata
```

If only one song is affected, that song may also be selected and the Quick Fix context may be opened.

### Statistics → Library

```text
Statistics
  ↓
Top Artists
  ↓
Queen
  ↓
Library
  ↓
artist filter = Queen
```

The same rule applies to album and format exploration.

### Duplicates → Library

When a duplicate group is inspected, navigation to Library should identify the relevant song rather than opening an unrelated default list.

---

## 6. Navigation by Screen

### Library

Entry:

- sidebar;
- Health review action;
- Statistics contextual exploration;
- duplicate inspection;
- other future contextual links.

Actions:

- select song;
- search/filter;
- open Quick Fix;
- scan;
- open relevant workflows.

Primary return:

- remains in Library after contextual actions unless the user explicitly navigates elsewhere.

### Statistics

Entry:

- sidebar.

Actions:

- inspect aggregate information;
- select artist/album/format when interactive.

Destination:

- Library with the selected context.

Statistics does not open Health merely because a value looks unusual.

### Health

Entry:

- sidebar.

Actions:

- inspect issue categories;
- review affected songs;
- inspect duplicate groups;
- refresh/re-analyze when supported.

Destinations:

- Library with issue context;
- contextual duplicate review.

### Organize

Entry:

- sidebar;
- future contextual action from Library where appropriate.

Actions:

- generate organization plan;
- inspect planned moves;
- explicitly confirm execution.

Destination:

- Library after successful execution or cancellation where appropriate.

Organization rules are defined separately and are not expanded by this navigation document.

### Settings

Entry:

- sidebar.

Actions:

- configure application behavior;
- configure library behavior;
- configure appearance;
- configure integrations when available;
- manage updates/installation settings.

Settings does not become a destination for operational library actions.

### About

Entry:

- sidebar.

Actions:

- inspect application information;
- open project resources.

About does not change application state.

---

## 7. Review and Duplicates Transition

### Current state

The application may expose:

```text
Library
Statistics
Health
Review
Duplicates
Organize
Settings
About
```

### Target state

The application should expose:

```text
Library
Statistics
Health
Organize
Settings
About
```

Review and Duplicates remain available through contextual workflows.

This transition must preserve all current capabilities. Removing a sidebar destination must not remove the underlying review or duplicate functionality.

---

## 8. Back Navigation

Back navigation should return to the previous user context whenever possible.

Examples:

```text
Statistics
  ↓
Library filtered to Queen
  ↓
Back
  ↓
Statistics
```

```text
Health
  ↓
Library with issue context
  ↓
Quick Fix
  ↓
Back
  ↓
Library with the same context
```

Back must not silently reset filters, selection, or the user's current workflow unless the destination explicitly represents a new context.

---

## 9. Selection and Filtering

Selection and filtering are different concepts.

**Filter** answers:

> Which songs should be shown?

**Selection** answers:

> Which song am I currently working on?

Navigation should preserve the appropriate one.

Examples:

- Statistics → artist → Library: filter by artist.
- Health → one affected song → Library: select the song.
- Health → many affected songs → Library: filter to affected songs.
- Quick Fix → after Apply: keep the song selected and refresh its data.

---

## 10. State Preservation

Navigation should preserve relevant transient state when returning to a screen.

Relevant examples include:

- active search text;
- active filters;
- selected song;
- selected issue;
- duplicate group context;
- scroll position where practical.

State should not be preserved when doing so would expose stale data after a destructive or corrective operation. In that case the destination should refresh while retaining the user's meaningful context.

---

## 11. No Dead-End Navigation

A contextual action should always make clear what happened and where the user can continue.

Avoid flows such as:

```text
Health → Review → empty generic screen
```

Prefer:

```text
Health → Review → Library filtered to affected songs
```

Likewise:

```text
Statistics → Artist → generic Library
```

should become:

```text
Statistics → Artist → Library filtered to that artist
```

---

## 12. Navigation Rules for New Features

Before adding a new sidebar item, answer:

1. Does this represent a distinct, recurring user goal?
2. Does it need to be available independently of another workflow?
3. Does it have enough content to justify a primary destination?
4. Could it be a contextual workflow from an existing destination?
5. Would adding it make the primary navigation harder to understand?

A feature should remain contextual when it is naturally a step inside another user's goal.

A new primary destination requires an explicit design decision and documentation update.

---

## 13. Navigation and Core Responsibilities

Navigation must call existing capabilities rather than duplicate them.

```text
Health
  → LibraryHealthService

Statistics
  → statistics calculation

Quick Fix
  → QuickFixService

Organize
  → LibraryOrganizationService

Duplicates
  → DuplicateDetectionService / TrashService
```

The navigation layer coordinates user context. It does not reimplement Core business logic.

---

## 14. Accessibility

- All primary destinations must be keyboard reachable.
- Current location must be visibly indicated and exposed to assistive technologies.
- Contextual navigation must communicate the destination and preserved context.
- Focus should move to the relevant content after contextual navigation.
- Back navigation must be predictable.
- No navigation decision should depend solely on color or hover state.

---

## 15. Visual Direction

Primary navigation should remain visually quiet and easy to scan.

The application can use the Studio Editorial visual language through typography, spacing, dividers, and restrained accents without turning the sidebar into a decorative element.

The active destination should be immediately recognizable.

Contextual actions should look different from primary destinations so users can distinguish:

```text
Where am I?
```
from:

```text
What can I do here?
```

---

## 16. Decision Rules

- Primary navigation represents stable user goals.
- Contextual workflows represent actions derived from those goals.
- Library is the central operational workspace.
- Health diagnoses; Library/Quick Fix acts.
- Statistics explores; Library applies the resulting context.
- Settings configures; it does not perform library operations.
- About informs; it does not modify state.
- Removing Review or Duplicates from the sidebar must not remove their capabilities.
- Context must be preserved across navigation whenever the destination is caused by a data selection or issue.
- New primary navigation items require explicit design justification.
- Navigation coordinates existing Core services rather than creating duplicate business logic.
