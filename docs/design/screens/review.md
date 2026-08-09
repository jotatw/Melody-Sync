# Review

> Current screen contract for the Review workflow.

## Document Information

| Item | Value |
|---|---|
| Category | Screen Specification |
| Status | Implemented / contextual via Health (removed from sidebar) |
| Scope | Current Review workflow |
| Related | Health, Library, Quick Fix |

---

## Purpose

Review is a focused workspace for inspecting songs that require attention before they return to the normal library workflow.

The screen is not an independent source of truth. It consumes diagnostic/review information from the application state and routes corrective actions through existing Core capabilities.

## Current Responsibilities

Review currently provides a focused workflow for:

- listing review items;
- filtering review items;
- selecting a song;
- inspecting the selected song;
- opening the Quick Fix context for the selected song;
- applying an explicit metadata fix through Quick Fix;
- returning the updated song to the normal library state.

## Does Not

Review must not:

- implement a second metadata-writing system;
- decide metadata automatically;
- silently apply a suggestion;
- replace Health diagnostics;
- replace Library browsing;
- directly implement YouTube or lyrics access.

## User Flow

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

## Empty State

When there are no review items, the screen should communicate that there is nothing requiring attention rather than displaying an empty technical table.

The empty state should not create a new action if there is no useful action to perform.

## Navigation Status

Review is no longer a primary navigation destination. It is reached contextually through Health and Library:

```text
Health
  ↓
review issue
  ↓
Library / Quick Fix
```

The review capability remains fully reachable through this contextual flow.

## Future Consolidation

Before removing Review from the sidebar, verify that the following capabilities remain reachable:

- identify why a song needs attention;
- inspect the song;
- open Quick Fix;
- apply a correction explicitly;
- return to the library context;
- preserve filtering/selection state where useful.

The migration should remove a navigation destination, not remove the underlying review capability.

## Related Documents

- [Library](library.md)
- [Health](health.md)
- [Quick Fix HUD](../../research/quick-fix-hud.md)
- [Application Design](../app-design.md)
- [Roadmap](../../ROADMAP.md)
