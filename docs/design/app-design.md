# Melody Sync App Design

> Global navigation model, screen responsibilities, and cross-screen interaction rules.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Architecture |
| Audience | Developers |
| Status | Draft |
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

## Screen Relationships

```text
Library
  ├─ selects a song
  ├─ opens Quick Fix for that song
  └─ receives filters from Health or Statistics

Health
  ├─ shows issues
  ├─ sends the user to Library with a song selected
  └─ may expose a review flow for a category of issues

Statistics
  ├─ shows aggregates
  └─ may open Library filtered by an artist, album, or format

Organize
  ├─ analyzes the library
  ├─ produces a plan
  ├─ reviews the plan
  └─ applies file moves and returns to Library

Settings
  └─ stores application preferences, update channel, and installation information
```

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

## Screen Interaction Rules

### Library
The Library answers: "What do I have, and what can I do with it now?"

Responsibilities:
- display the current library snapshot
- allow search and filtering
- allow selecting a single song
- expose contextual actions for the selected song
- host the Quick Fix panel when applicable

Non-responsibilities:
- summarize the entire collection as a dashboard
- perform silent modifications
- duplicate Health or Statistics content

### Health
The Health screen answers: "What needs attention?"

Responsibilities:
- show health score and issue categories
- explain what is wrong
- lead the user to the affected songs in Library

Non-responsibilities:
- act as the editing workspace itself
- perform changes without explicit user action
- repeat all Statistics information

### Statistics
The Statistics screen answers: "How is my collection composed?"

Responsibilities:
- show aggregate counts and distributions
- reveal patterns in formats, artists, and albums
- allow optional navigation into the Library with a filter applied

Non-responsibilities:
- edit metadata
- move files
- diagnose file-level issues in detail

### Organize
The Organize screen answers: "How should the files be arranged?"

Responsibilities:
- analyze current and target paths
- present a dry-run plan before applying changes
- show reasons for each move
- return the user to Library after apply

Non-responsibilities:
- silently move files
- hide the plan behind a generic dialog
- replace the Health workflow

### Settings
The Settings screen answers: "How should Melody Sync behave?"

Responsibilities:
- store preferences
- configure library behavior
- manage update channel and installation status
- keep the user informed about update state

Non-responsibilities:
- manage songs directly
- show health diagnostics
- show statistics dashboards

### About
The About screen answers: "What is this application?"

Responsibilities:
- identify the application
- present version and licensing information
- provide a concise product description

Non-responsibilities:
- participate in the main workflow
- expose operational controls

## Navigation States

Screens should be able to express at least the following states:

- empty
- loading
- ready
- error
- success / completed

The state displayed should reflect the current operation instead of forcing the user to infer what happened.

## Implementation Guidance

- Keep the sidebar as the structural navigation layer.
- Keep screen actions inside the screen.
- Prefer contextual actions over separate top-level destinations when a feature belongs to an existing workflow.
- If a screen becomes mostly a sub-state of another screen, prefer embedding it rather than promoting it to a sidebar item.

## Planned Screen Documents

- `docs/design/screens/library.md`
- `docs/design/screens/health.md`
- `docs/design/screens/statistics.md`
- `docs/design/screens/organize.md`
- `docs/design/screens/settings.md`
- `docs/design/screens/about.md`
