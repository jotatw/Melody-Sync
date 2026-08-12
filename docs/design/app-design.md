# Melody Sync App Design

> Global navigation model, screen responsibilities, and cross-screen interaction rules.

> **Note:** this document defines the target interaction model. The research
> behind these decisions is recorded in
> [Application Design Research](../research/app-design.md).

## Document Information

| Item | Value |
|---|---|
| Category | Design / Architecture |
| Audience | Developers |
| Status | Defined |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## Purpose

This document defines the interaction model of Melody Sync at the application level. It explains how the major screens relate to each other, what each screen is responsible for, and which actions are allowed to flow between them.

## Navigation Philosophy

Melody Sync is a personal music curation workstation. Its navigation should help the user move through a simple mental model:

- **Library**: inspect and work with the collection.
- **Health**: identify problems and review issues.
- **Statistics**: understand the shape of the collection.
- **Organize**: plan and apply file moves.
- **Settings**: configure application behavior.
- **About**: read project information.

The sidebar is structural navigation. Screen buttons inside a screen are contextual actions.

## Global Rules

1. The Library is the primary workspace.
2. Health identifies issues; it does not replace Library editing.
3. Statistics is observational and should not modify data.
4. Organize is plan-first and apply-second.
5. Settings changes application behavior and preferences.
6. About has no operational workflow.
7. Review is not a primary destination; it is a workflow/state used by Health and contextual actions in the Library.
8. Quick Fix is contextual to a selected song.
9. Duplicates are a Health concern, not a primary navigation destination while their workflow can remain contextual to Health.
10. Contextual navigation must preserve the reason the user moved between screens.
11. A screen should not own a workflow that belongs to another screen merely because it can display the same data.

## Primary Navigation

The structural navigation should contain only these destinations:

```text
Library
Statistics
Health
Organize

Settings
About
```

`Review` and `Duplicates` are intentionally absent from the primary navigation. They remain contextual workflows owned by the screen where they make sense.

## Screen Relationships

```text
                         LIBRARY
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
           HEALTH       STATISTICS     ORGANIZE
              │             │             │
              │             │             │
              ▼             ▼             ▼
       issue context     filters        plan/apply
              │             │             │
              └───────┬─────┘             │
                      ▼                   ▼
                    LIBRARY ◄─────────────┘

                  SETTINGS
                      │
                      └── configures future behavior

                    ABOUT
                      │
                      └── information / external links
```

### Library

Library is the canonical workspace for song-level inspection and action.

- selects songs;
- searches and filters;
- hosts Quick Fix;
- receives contextual issue/filter state from Health and Statistics;
- reflects the result of Organize and Quick Fix operations.

### Health

Health is the diagnostic hub.

- shows library health;
- explains issue categories;
- sends song-level issues to Library;
- retains duplicate-group review context while Library lacks an appropriate multi-selection workflow.

### Statistics

Statistics is the observation/exploration surface.

- shows aggregate library information;
- may send an artist, album, or format context to Library;
- never modifies library data.

### Organize

Organize is the controlled filesystem workflow.

- analyzes;
- creates a plan;
- exposes the plan before execution;
- applies only after explicit user action;
- returns the resulting collection state to Library.

### Settings

Settings is the configuration surface.

- stores application/library/appearance/update preferences;
- exposes installation information;
- does not perform normal library workflows.

### About

About is informational only.

- identifies the application;
- shows canonical version information;
- provides project links and attribution.

## Cross-Screen Contracts

### Health → Library

```text
Health issue
   ↓
Review
   ↓
Library
   ↓
Issue filter or selected song
```

The user must retain the reason for entering Library.

If one song is targeted, it may be selected. If multiple songs are targeted, a compatible filter should be applied. The application must not imply multi-selection if that capability does not exist.

### Health → Quick Fix

Quick Fix is reached through Library context, not as a separate Health editing surface.

```text
Health
  ↓
Review issue
  ↓
Library
  ↓
Select song
  ↓
Quick Fix
```

### Health → Duplicates

Duplicate groups are reviewed contextually in Health while there is no suitable multi-selection workflow in Library.

A dedicated Duplicates destination should only be introduced if this interaction becomes too complex for Health.

### Statistics → Library

```text
Statistics
   ↓
Artist / Album / Format
   ↓
Library filtered by that dimension
```

The selected dimension is preserved.

### Organize → Library

```text
Organize
   ↓
Analyze
   ↓
Review plan
   ↓
Apply
   ↓
Library refreshed
```

No file move occurs before Apply.

### Settings → Future Screens

Settings normally does not navigate directly into operational screens. Configuration changes affect subsequent Library, Health, Statistics, or Organize behavior.

If a setting requires an immediate follow-up action, that relationship must be explicitly explained before navigation occurs.

### About → External Resources

About may open external project resources. These links do not alter application state.

## State Model

Screens should distinguish between application state and operation state.

At minimum, operational screens should be able to represent:

```text
Idle / Ready
Loading / Running
Success / Completed
Error
Empty
```

The UI must not confuse:

- database loading with library scanning;
- health-analysis failure with poor library health;
- update/rebuild activity with music-library activity;
- a generated Organize plan with an executed move.

## Context Preservation

Context preservation is a global navigation rule.

When a contextual action moves the user to another screen, the destination should receive enough state to answer:

> **"Why did I end up here?"**

Examples:

- Health → Library preserves issue filter or selected song.
- Statistics → Library preserves artist/album/format filter.
- Organize → Library reflects the resulting paths after Apply.

Unrelated navigation through the sidebar may reset contextual state where appropriate.

## Responsibilities Matrix

| Screen | Primary question | Reads | Can modify | Leads to |
|---|---|---|---|---|
| Library | What do I have / what can I do? | Library state | Explicit song-level actions | Quick Fix, contextual workflows |
| Health | What needs attention? | Diagnostic state | No direct mutation | Library, contextual duplicate review |
| Statistics | How is my collection composed? | Aggregate state | None | Library filters |
| Organize | How should files be arranged? | Paths + rules | Explicit filesystem operations | Library |
| Settings | How should Melody Sync behave? | Configuration | Configuration | None normally |
| About | What is this application? | Static/app info | None | External resources |

## Screen Contract Template

Every screen document under `docs/design/screens/` should answer the same questions:

- Purpose
- User question
- Responsibilities
- Non-responsibilities
- Entry points
- Primary actions
- States
- Contextual interactions
- Navigation rules
- Data interaction
- UX rules
- Accessibility notes
- Decision rules

## Implementation Guidance

- Keep the sidebar as the structural navigation layer.
- Keep screen actions inside the screen.
- Prefer contextual actions over separate top-level destinations when a feature belongs to an existing workflow.
- If a screen becomes mostly a sub-state of another screen, prefer embedding it rather than promoting it to a sidebar item.
- Preserve contextual state when crossing screen boundaries.
- Do not implement a visual navigation element until its interaction contract is defined here or in the corresponding screen document.

## Related Screen Documents

- `docs/design/screens/library.md`
- `docs/design/screens/health.md`
- `docs/design/screens/statistics.md`
- `docs/design/screens/organize.md`
- `docs/design/screens/settings.md`
- `docs/design/screens/about.md`
