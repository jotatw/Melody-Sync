# Library — Interaction Model

> Primary workspace for inspecting, searching, and acting on the music collection.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Screen Specification |
| Audience | Developers / UX |
| Status | Defined / refining |
| Project Version | v0.13.0-dev |
| Primary navigation | Library |
| Related screens | Health, Statistics, Organize, Settings, Review, Duplicates |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

Library is the primary workspace of Melody Sync. It presents the current music collection and gives the user a direct place to search, inspect, select, and act on individual songs.

The screen should make the current state of the library immediately understandable without turning the Library into a general-purpose dashboard.

---

## 2. User Question

> **What do I have, and what can I do with it now?**

The user should be able to answer:

- How large is my library?
- Is the displayed library synchronized with the filesystem?
- Which songs are present?
- Where is a particular song, artist, or album?
- Does a selected song need attention?
- What action can I safely take on the selected song?

---

## 3. Responsibilities

Library is responsible for:

- displaying the current library snapshot;
- showing high-level collection metrics relevant to the workspace;
- showing the current music directory;
- starting an explicit library scan/synchronization;
- exposing the optional Watch behavior;
- searching songs by title, artist, or album;
- filtering the visible collection;
- sorting the song list;
- selecting a song;
- exposing contextual actions for the selected song;
- hosting Quick Fix when a selected song can benefit from it;
- reflecting changes made by other workflows after returning to Library.

---

## 4. Non-Responsibilities

Library must not:

- become the main statistics dashboard;
- duplicate the detailed issue analysis performed by Health;
- silently modify metadata;
- silently move or delete files;
- perform organization without an explicit plan and confirmation;
- require the user to understand internal database states or implementation details;
- become a separate destination for every contextual action.

---

## 5. Entry Points

### Primary entry

- Sidebar → Library.

### Contextual entry

Health and Statistics may return the user to Library with context preserved.

Examples:

```text
Health → Review issue → Library with issue filter applied
Health → Review song → Library with song selected
Statistics → View in Library → Library with filter applied
Organize → Apply → Library refreshed
```

The destination should preserve useful context instead of opening an unrelated default state.

---

## 6. Screen Structure

The exact visual layout remains governed by the Design System. The interaction hierarchy should remain approximately:

```text
Library
│
├── Collection overview
│
├── Library synchronization
│   ├── Music directory
│   ├── Scan Library
│   └── Watch
│
├── Search and filters
│
└── Song list
    └── Selected song
        └── Quick Fix / contextual actions
```

The collection overview should support orientation, not compete with the song list for attention.

---

## 7. Primary Actions

### Scan Library

The primary operational action is an explicit library scan/synchronization.

Expected behavior:

1. User starts the scan.
2. The interface enters a visible loading state.
3. Progress communicates what the application is doing.
4. Completion clearly states the result.
5. Library data and statistics are refreshed.

The completion message should describe the operation in user terms, for example:

> `Library synchronized · 690 songs analyzed`

This is different from a database-load message such as:

> `Loaded 690 songs from database`

The first describes an operation performed by the user; the second describes application state.

### Search

Search is a direct exploration tool. It should accept title, artist, and album terms without requiring the user to choose a search mode first.

### Filter

Filters narrow the visible collection without changing the underlying library.

Current filter dimensions include:

- artist;
- album;
- format;
- search text.

The toolbar keeps **search always visible**; the artist/album/format fields live behind a compact **Filters** disclosure to preserve a low-density default. Active filters surface as removable chips with a **Clear all** shortcut, and the disclosure shows the active-filter count. When a contextual issue/statistics navigation applies a filter, it renders inline and can be cleared without losing the library state.

Additional filters may be introduced later, but each should have a clear user-facing purpose.

### Select Song

Selecting a song establishes the context for song-level actions. Selection must not modify the file or metadata.

---

## 8. Song Selection and Quick Fix

A selected song may expose the Quick Fix experience.

The conceptual flow is:

```text
Select song
    ↓
Inspect diagnostics
    ↓
Review suggestion
    ↓
Apply explicitly
    ↓
Persist metadata
    ↓
Refresh Library state
```

Quick Fix is contextual to the selected song and must not become a separate primary navigation destination.

Applying a suggestion always requires an explicit user action.

The user should be able to understand what will be changed before applying it.

---

## 9. States

### Initial / Database Loaded

The application may load the current library from the database on startup.

The interface should clearly distinguish this from a new filesystem scan.

### Empty

When no songs are available, explain why and provide the next useful action where possible.

Example direction:

```text
No songs in this library

Choose a music directory or scan the selected directory.
```

### Loading / Scanning

During a scan:

- the active operation must be visually obvious;
- the primary scan action must not allow accidental concurrent scans;
- progress should remain understandable without exposing raw implementation logs;
- Watch should not create a confusing second simultaneous operation.

### Ready

The normal state displays the current collection, search/filter controls, and available song actions.

### Filtered

The list reflects the active search/filter criteria. The user should be able to understand that the collection is filtered and clear the filter without losing the library itself.

### Song Selected

The selected song becomes the contextual focus. Quick Fix and other song-level actions may appear.

### Scan Success

Show a concise result that distinguishes synchronization from database loading.

The library and dependent views should reflect the new state.

### Error

Errors should describe the operation that failed and provide a useful recovery path where possible. Avoid presenting raw exceptions as the primary user message.

---

## 10. Contextual Interactions

### Health → Library

Health identifies problems; Library is the place where the user can inspect an affected song or filtered set of songs.

When the user chooses to review an issue category:

- navigate to Library;
- apply the corresponding issue filter when supported;
- if the context identifies one song, select that song;
- preserve enough context for the user to understand why the library was opened;
- do not automatically modify anything.

When the issue is a multi-file duplicate group, Health/duplicates context remains the review surface until Library supports an appropriate multi-selection workflow.

### Statistics → Library

Statistics may provide actions such as `View in Library`.

The Library should open with the corresponding filter applied when the target is representable by an existing filter.

Examples:

```text
Top Artist → Library filtered by artist
Album → Library filtered by album
Format → Library filtered by format
```

### Organize → Library

After an organization operation is successfully applied, Library should refresh and reflect the resulting paths and collection state.

### Review / Duplicates → Library

Review and Duplicates are currently separate workspaces, but the approved navigation direction is contextual rather than permanent primary destinations.

When a contextual workflow points to one song, Library should open with that song selected. Multi-file contexts should remain in their dedicated workflow until an appropriate multi-selection interaction exists.

### Watch

Watch is a library synchronization preference, not a separate screen or workflow.

---

## 11. Navigation Rules

### Leaving Library

The user's search/filter context should not be discarded unnecessarily when moving to a contextual destination and returning.

### Returning to Library

A contextual return should restore the relevant target:

- selected song;
- active filter;
- refreshed library state after a mutation.

### Sidebar Navigation

Sidebar navigation changes the primary screen. It should not be overloaded with song-level operations.

### Review

Review is a contextual workflow reached from Health or song-level problem handling. Its current implementation remains documented separately until navigation consolidation is implemented.

### Duplicates

Duplicates is a contextual workflow for duplicate groups. Its current implementation remains documented separately until navigation consolidation is implemented.

---

## 12. Data Interaction

Library reads:

- songs stored in the local database;
- library statistics;
- configured music directory;
- scan/watch state;
- active search and filter state.

Library may update:

- synchronized song records after a scan;
- visible state after Quick Fix metadata changes;
- visible paths after organization;
- dependent statistics after successful mutations.

Library must not treat the UI list as the authoritative source of persistent data.

---

## 13. UX Rules

1. **The Library is the workspace.** Other screens should lead back here when the user needs to work on individual songs.
2. **Actions must have visible consequences.** Scanning, applying metadata, and organization should communicate what changed.
3. **No silent mutation.** Selecting, filtering, or navigating never modifies the library.
4. **Context follows the user.** Health and Statistics should be able to open Library at a meaningful target.
5. **Do not overload the user with diagnostics.** Health owns collection-level diagnosis; Quick Fix owns song-level correction.
6. **Do not expose implementation terminology unnecessarily.** The user should see synchronization results rather than database operation names.
7. **Primary action hierarchy matters.** Scanning is the main library operation; secondary controls should remain visually subordinate.
8. **Selection is explicit.** A song must be selected before song-level correction actions become active.
9. **Destructive operations remain outside the normal browsing flow.** File movement and similar changes belong to explicit workflows such as Organize or Duplicates.
10. **Empty and loading states are designed states, not missing content.**

---

## 14. Accessibility Notes

- Every interactive control must have a visible keyboard focus state.
- Search and filters must be reachable without a mouse.
- Song selection must be possible with keyboard navigation.
- The selected row must have a clear visual indication beyond color alone.
- Tooltips may supplement truncated paths, titles, and technical metadata.
- Status messages should not rely solely on color.
- Loading and completion states should be communicated textually as well as visually.

---

## 15. Decision Rules

These rules should be treated as the behavioral contract for implementation:

- Selecting a song never changes it.
- Opening Quick Fix never applies a suggestion automatically.
- Applying a metadata fix requires explicit confirmation through the Apply action.
- Scanning is an explicit operation initiated by the user unless Watch is enabled.
- Database loading on startup must not be presented as a new scan.
- Health may identify a song or issue set, but Library owns the song-level workspace.
- Statistics may provide filters into Library, but does not modify library data.
- Organize may return to Library after a successful apply.
- Library remains the canonical destination for inspecting the resulting collection after mutations.

---

## 16. Future Extensions

The following are intentionally not required by this document yet:

- album artwork / cover presentation;
- genre and year filters;
- advanced multi-selection workflows;
- richer playback controls;
- expanded metadata editing beyond the current Quick Fix scope.

Future features must preserve the responsibilities and non-responsibilities defined above.
