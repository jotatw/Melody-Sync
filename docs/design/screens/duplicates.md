# Duplicates

> Current screen contract for duplicate detection and review.

## Document Information

| Item | Value |
|---|---|
| Category | Screen Specification |
| Status | Implemented / navigation consolidation pending |
| Scope | Current duplicate workflow |
| Related | Health, Library, Trash |

---

## Purpose

Duplicates identifies groups of songs that are likely duplicates and gives the user a reviewable context before any file is moved to trash.

The screen is intentionally conservative: detection is a candidate-finding operation, not permission to delete files.

## Current Responsibilities

Duplicates currently provides a workflow for:

- displaying detected duplicate groups;
- showing the songs that belong to each group;
- selecting/reviewing duplicate candidates;
- presenting an explicit action to move a selected file to trash;
- showing operation progress and result state;
- requiring confirmation before a destructive filesystem action.

## Detection Boundary

Duplicate detection is provided by the Core duplicate service.

The current heuristic is based on normalized title and artist together with a duration tolerance. The UI must present the result as a **likely duplicate**, not as mathematical proof that two files are identical.

## Does Not

Duplicates must not:

- automatically delete files;
- silently move files to trash;
- modify metadata;
- reorganize the library;
- decide which copy is authoritative without user input;
- implement a second duplicate-detection algorithm in the UI.

## User Flow

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

## Empty State

When no duplicate groups are found, the screen should communicate this as a successful analysis result rather than as an empty technical list.

A useful state is conceptually:

```text
No duplicates found

The analyzed library currently contains no detected duplicate groups.
```

If a last-analysis timestamp is available, it may be shown as supporting information.

## Navigation Status

The current implementation exposes Duplicates as a primary navigation destination.

The approved target navigation model proposes treating duplicate findings as part of the broader Health/review experience rather than requiring a permanent top-level destination.

This document describes the **current implemented workflow**. It does not by itself authorize removing Duplicates from the sidebar.

## Future Consolidation

If Duplicates becomes contextual to Health, the migration must preserve:

- access to duplicate groups;
- group-level inspection;
- explicit confirmation;
- trash/recovery behavior;
- progress and result feedback;
- a clear distinction between detection and deletion.

The goal is to reduce navigation complexity, not to hide or remove the duplicate-management capability.

## Related Documents

- [Health](health.md)
- [Library](library.md)
- [Application Design](../app-design.md)
- [Roadmap](../../ROADMAP.md)
