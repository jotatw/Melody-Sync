# Implementation Block 01 — Contextual Navigation

> First implementation block of the navigation consolidation. Defines the behavior to implement before changing the primary sidebar.

## Document Information

| Item | Value |
|---|---|
| Category | Planning / Implementation Block |
| Status | Implemented / validation ongoing |
| Project Version | v0.13.0-dev |
| Scope | Desktop navigation context |
| Related | `docs/design/navigation.md`, screen specifications |
| Last Updated | 2026-08-19 |

---

## 1. Objective

Make the first contextual navigation flows work consistently without changing the primary sidebar yet.

The block establishes a reusable distinction between **filter context** and **selection context** and makes contextual navigation preserve the reason the user left the current screen.

This block does not redesign the UI and does not remove existing sections.

---

## 2. Why This Block Comes First

The target navigation removes `Review` and `Duplicates` from the primary sidebar, but those capabilities must remain reachable.

Before changing the sidebar, the application must prove that contextual workflows can replace the current primary destinations without creating dead ends.

The current implementation already contains part of this behavior: Health can send an affected song to Library and select it. The new work should extend and normalize this pattern rather than introduce a second navigation mechanism.

---

## 3. Scope

### Included

1. Health → Library for issue review.
2. Statistics → Library for supported aggregate exploration.
3. Library → Quick Fix context preservation.
4. Explicit distinction between filtering and selection.
5. Preservation of relevant context when returning to Library.
6. Minimal state/navigation support required by the above flows.

### Excluded

- Removing `Review` from the sidebar.
- Removing `Duplicates` from the sidebar.
- Redesigning the Sidebar.
- Replacing the existing section/navigation architecture with a new routing system.
- Implementing album filtering if the current state does not yet support it.
- Designing or implementing the Duplicates contextual workflow.
- Changing the Health analysis itself.
- Changing Statistics calculations.
- Changing Quick Fix business logic.
- Adding new backend/domain fields solely for navigation.
- Visual redesign beyond the minimum feedback required to make context understandable.

---

## 4. Context Model

Navigation caused by data should carry a reason/context into the destination.

The conceptual model is:

```text
LibraryContext
├── None
├── Artist
├── Album
├── Format
├── HealthIssue
└── DuplicateGroup
```

Not every context is implemented in this block. The model documents the intended direction; this block implements only the contexts required by the included flows and already supported by the current application.

### Selection vs Filter

**Filter** answers:

> Which songs should be displayed?

**Selection** answers:

> Which song am I currently working on?

Rules:

| Origin | Destination context |
|---|---|
| Health → one affected song | Selection |
| Health → multiple affected songs | Health issue filter/context |
| Statistics → artist | Artist filter |
| Statistics → format | Format filter |
| Statistics → album | Album filter only when supported by the current state |
| Library → Quick Fix | Selected song |

---

## 5. Flow A — Health → Library

### Goal

A user reviewing a Health issue must arrive at the relevant content without manually finding it again.

### Single affected song

```text
Health
  ↓
Issue
  ↓
Review
  ↓
Library
  ↓
Affected song selected
  ↓
Quick Fix available
```

The current implementation already supports the essential single-song behavior through `reviewIssue(...)`, which changes the section to Library and selects the affected song.

The implementation block should preserve this behavior while making its context explicit.

### Multiple affected songs

Target behavior:

```text
Health
  ↓
Issue category
  ↓
Review all
  ↓
Library
  ↓
Only affected songs are shown
```

This is a filter/context operation, not a single-song selection.

The existing implementation must not silently interpret `Review all` as "open the first affected song". If the current state cannot support the multi-song context yet, the implementation should expose that limitation rather than claim the flow is complete.

**Chosen implementation (Block 01):** `reviewIssue(paths, label)` uses a *selection* context for a single affected song and a *filter* context for multiple affected songs. The multi-song case sets `issueContext` (label + path set), clears the selection, and Library shows only the affected songs with a visible indicator and a way to clear it. Album navigation from Statistics is deferred because the state layer does not currently support an album filter.

### Completion condition

A user can move from a Health issue to Library and immediately work on the affected content without reconstructing the issue manually.

---

## 6. Flow B — Statistics → Library

### Goal

Allow aggregate exploration to become contextual Library browsing.

### Artist

```text
Statistics
  ↓
Top Artists
  ↓
Artist
  ↓
Library
  ↓
Artist filter preserved
```

### Format

```text
Statistics
  ↓
Formats
  ↓
Format
  ↓
Library
  ↓
Format filter preserved
```

### Album

The target design supports album exploration, but the current application state must be verified before implementation.

Do not create an album-filter architecture solely for this block if the current data/state layer does not already support it. If necessary, document album navigation as deferred to a later block.

### Completion condition

A supported Statistics interaction opens Library with the selected aggregate dimension preserved.

---

## 7. Flow C — Library → Quick Fix

### Goal

Opening Quick Fix must remain a contextual operation on the selected song.

Expected flow:

```text
Library
  ↓
Select song
  ↓
Quick Fix
  ↓
Diagnose / Suggest
  ↓
Explicit Apply
  ↓
Library state refreshed
  ↓
Same song remains contextual
```

Quick Fix business logic is out of scope. This block only concerns navigation/state preservation.

After a successful correction:

- the selected song remains identifiable;
- updated metadata is reflected in Library;
- existing statistics refresh behavior is preserved;
- the user does not lose the current workflow unnecessarily.

---

## 8. State Rules

The implementation should distinguish these states conceptually:

```text
No context
Filter context
Selection context
Filter + selection context
```

A context must not unexpectedly overwrite an unrelated user action.

Examples:

- Opening a specific Health issue should not clear the user's selected song before the destination is ready.
- Opening Statistics → Artist should not select an arbitrary song merely because the Library list is filtered.
- Applying Quick Fix should not clear the selected song unless the operation invalidates it.

If a corrective operation changes the underlying data, the destination may refresh while retaining meaningful context.

---

## 9. Navigation Return Rules

When returning from a contextual action:

```text
Health
  ↓
Library + issue context
  ↓
Quick Fix
  ↓
Back
  ↓
Library + same issue/selection context
```

The exact mechanism may reuse the existing Desktop state architecture. A new routing framework is not part of this block.

The implementation should prioritize predictable behavior over architectural abstraction.

---

## 10. Existing Capabilities to Reuse

The implementation should inspect and reuse existing capabilities before adding new abstractions.

Known relevant capabilities include:

- existing `Section` navigation;
- existing Library selection state;
- existing artist and format filter state;
- existing Health `reviewIssue(...)` behavior;
- existing Quick Fix selection flow;
- existing pending scroll/selection behavior where applicable.

The purpose of this block is to consolidate these behaviors, not replace them with parallel mechanisms.

---

## 11. Acceptance Criteria

The block is complete when all applicable criteria are satisfied:

### Health

- [ ] A single Health issue opens Library with the affected song selected.
- [ ] The affected song is visible/focused according to the existing Library behavior.
- [ ] A multi-song Health action does not falsely claim to review all songs if only one can be selected.
- [ ] The chosen implementation for multi-song context is documented and consistent.

### Statistics

- [ ] Supported artist navigation opens Library with artist context.
- [ ] Supported format navigation opens Library with format context.
- [ ] Album navigation is either implemented correctly or explicitly deferred; it is not simulated.

### Quick Fix

- [ ] Quick Fix opens for the selected Library song.
- [ ] Applying a fix keeps the user in a meaningful Library context.
- [ ] Existing update/statistics behavior continues to work.

### General

- [ ] Filter and selection are treated as different concepts.
- [ ] No new navigation framework is introduced.
- [ ] No primary sidebar item is removed in this block.
- [ ] No unrelated feature is added.
- [ ] Existing tests remain passing.

---

## 12. Validation

Validation should be performed locally before the implementation is considered complete.

Minimum manual scenarios:

1. Health → single issue → Library → selected song.
2. Health → multiple issues → verify the chosen multi-song behavior.
3. Statistics → artist → Library filtered by artist.
4. Statistics → format → Library filtered by format.
5. Library → select song → Quick Fix → Apply → Library remains contextual.
6. Back navigation from Quick Fix returns to the expected Library state.
7. Existing sidebar navigation still works because sidebar consolidation is not part of this block.

After implementation, the changes should be committed to GitHub and reviewed against this document before Block 02 begins.

---

## 13. Non-Goals / Future Blocks

The following remain outside this block:

```text
Block 02 — Sidebar + primary navigation
Block 03 — Health consolidation
Block 04 — Statistics interaction expansion
Block 05 — Library + Quick Fix refinement
Block 06 — Settings foundation
Block 07 — Visual refinement
Block 08 — Organize redesign/rules
```

In particular, Review and Duplicates must not be removed from the sidebar until their replacement contextual flows have been validated.

---

## 14. Decision

**Approved scope:** contextual navigation foundation only.

The implementation should be small enough to test independently and large enough to establish a reusable navigation pattern for the remaining screens.

No visual redesign or unrelated feature work should be included under this block.
