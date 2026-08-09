# Statistics — Interaction Model

> Exploratory view for understanding the composition and characteristics of the music library.

## Document Information

| Item | Value |
|---|---|
| Category | Design / Screen Specification |
| Audience | Developers / UX |
| Status | Implemented / refining |
| Project Version | v0.13.0-dev |
| Primary navigation | Statistics |
| Related screens | Library, Health |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

Statistics answers a different question from Library and Health:

> **How is my music collection composed?**

It is an exploratory screen. Its purpose is to make the size, composition, and distribution of the library understandable without turning aggregate information into a management workflow.

Statistics should help the user notice something interesting and, when useful, move from that aggregate context into the corresponding songs in Library.

---

## 2. Responsibilities

Statistics is responsible for:

- presenting meaningful aggregate library metrics;
- showing the distribution of supported audio formats;
- ranking artists by available song data;
- ranking albums by available song data;
- making relationships between totals and visualizations understandable;
- providing contextual navigation to Library when an aggregate item is actionable;
- keeping displayed information consistent with the current library/database state.

Statistics should use data that already exists reliably in the current model and database.

## 3. Non-Responsibilities

Statistics must not:

- modify metadata;
- move, rename, or delete files;
- perform organization;
- diagnose library problems already handled by Health;
- become a second Library;
- invent unsupported metrics merely to fill the dashboard;
- require unreliable heuristics to produce a chart;
- make the user interpret charts without readable labels or values.

---

## 4. Entry Point

Primary entry point:

```text
Sidebar → Statistics
```

Statistics should not become the mandatory destination after scanning. Scan results belong to Library; Statistics is available when the user wants to explore the collection.

Future contextual entry points may come from Library or a summary component, but they should preserve the user's context rather than reset the application unnecessarily.

---

## 5. Information Hierarchy

The screen should follow this conceptual order:

```text
STATISTICS
Short description
        ↓
Library overview
        ↓
Format distribution
        ↓
Top artists
        ↓
Top albums
        ↓
Additional supported analysis
```

The exact layout can evolve with the visual design system, but the hierarchy should remain understandable without requiring the user to inspect every visualization.

The screen should not become a wall of independent cards. Sections need a clear relationship to the overall collection.

---

## 6. Core Metrics

The current statistics model reliably provides:

- songs;
- artists;
- albums;
- total duration;
- library size;
- format distribution;
- average bitrate where available.

These metrics can form the overview/header of the Statistics screen.

The same values should agree with the corresponding Library state. If the database is being updated or a scan is active, the UI should distinguish current values from values being recalculated.

---

## 7. Supported Analysis

### Formats

Format distribution is a first-class analysis because format data already exists in the statistics model.

A pie/donut chart is appropriate if it remains readable. An alternative distribution visualization is acceptable if it communicates the same information more clearly.

Requirements:

- readable labels or legend;
- small categories remain discoverable;
- total is understandable;
- color is not the only differentiator;
- textual values remain available for accessibility.

### Top Artists

Artists can be ranked by song count using the song data already available in the library.

A horizontal bar chart is preferred for comparison because artist names remain readable.

The visualization should communicate that it is a **top-N** view rather than the complete artist list.

Selecting an artist can become a contextual navigation action to Library.

### Top Albums

Albums can be ranked by song count from the current song data.

A proportional bar/list treatment is appropriate when it communicates ranking without consuming the entire screen.

Selecting an album can become a contextual navigation action to Library.

### Timeline

A year-based timeline is **deferred**.

The current `Song` model does not provide a reliable year field. The application should not extract years heuristically from arbitrary file paths simply to fill a chart.

Timeline becomes valid when reliable year metadata is part of the model and ingestion pipeline.

### Genre

Genre analysis is also deferred if genre is not currently represented reliably in the model/database.

It should not be simulated from filenames, folder names, or external guesses.

### Cover Art

Cover-art-based statistics or visualizations are deferred until artwork storage, retrieval, and caching are defined.

---

## 8. States

### No data

If no library data exists:

```text
STATISTICS

No library data available.

Scan a music directory to build your library.

[Go to Library]
```

Do not show empty chart frames that imply data exists.

### Loading / Refreshing

When statistics are being calculated or refreshed, communicate that values are being updated.

Existing values may remain visible during refresh only when the UI clearly distinguishes them from the pending result.

### Ready

Show the overview metrics followed by the supported analysis sections.

### Empty category

If a supported category contains no useful data, explain it briefly instead of rendering an empty chart.

### Error

If calculation fails, report a calculation error rather than replacing missing data with zeros.

---

## 9. Contextual Navigation to Library

Statistics is observational, but it should provide useful paths from aggregate information to individual songs.

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
Artist filter applied
```

### Album

```text
Statistics
  ↓
Top Albums
  ↓
Album
  ↓
Library
  ↓
Album filter applied
```

### Format

```text
Statistics
  ↓
Format distribution
  ↓
Format
  ↓
Library
  ↓
Format filter applied
```

These are contextual exploration actions, not required workflows.

The important rule is that the dimension that caused the navigation is preserved.

---

## 10. Navigation Rules

- Statistics is a top-level destination because collection exploration is a distinct user goal.
- Opening Statistics does not automatically trigger a scan.
- Statistics does not own Library filters.
- When Statistics opens Library contextually, the selected artist/album/format becomes a Library filter where supported.
- Returning to Statistics should not imply that a modification occurred.
- Statistics should not redirect the user to Health merely because a metric looks unusual; Health owns diagnosis.

---

## 11. Data Boundary

Statistics reads aggregate information from the current library/database state.

Reliable current inputs include:

```text
Song count
Artist count
Album count
Duration
Library size
Formats
Average bitrate
Song-level artist/album values
```

The first dashboard implementation should not require changes to the Core merely to provide a visually complete page.

Deferred data:

```text
Year
Genre
Cover art
```

These require reliable underlying data and belong to future Core work when explicitly approved.

---

## 12. Relationship with Other Screens

### Library

```text
Library
→ works on individual songs

Statistics
→ understands the collection as a whole
```

Statistics may lead to Library with context, but it should not reproduce Library's browsing and editing controls.

### Health

```text
Health
→ What needs attention?

Statistics
→ How is the collection composed?
```

An unusual statistic is not automatically an issue.

### Organize

Statistics does not decide how files should be organized.

### Review / Quick Fix

Statistics does not initiate metadata correction directly. If a future workflow needs to move from an aggregate observation to a correction, it should first arrive in Library with the appropriate context.

---

## 13. UX Rules

- Every number should answer a useful question.
- Charts must communicate data, not decorate empty space.
- Avoid repeating the same metric without a clear purpose.
- Aggregate values must remain consistent with Library and the current database state.
- Every visualization needs readable labels and a textual interpretation.
- Top-N charts must communicate that they are partial views.
- Unsupported data is deferred rather than guessed.
- A chart should be added because it provides insight, not because the screen has available space.
- Interactive aggregate items should lead naturally to Library when that interaction is useful.
- Statistics should feel analytical rather than operational.

---

## 14. Accessibility

- Charts must not rely on color alone.
- Important chart values must have textual representations.
- Interactive chart/list items must be keyboard reachable.
- Focus must be visible.
- Large numerical metrics require accessible labels explaining their meaning.
- Tooltips may supplement labels but must not be the only way to understand data.
- The information hierarchy should remain understandable with reduced motion and without color perception.

---

## 15. Visual Direction

Statistics should use the project's **Studio Editorial** language primarily for hierarchy and composition, not decoration.

Recommended structure:

```text
STATISTICS

Collection summary
        ↓
Large, clear metrics
        ↓
Editorial section heading
        ↓
Focused visualization
        ↓
Supporting analysis
```

The page should use meaningful negative space without becoming an empty dashboard.

Avoid a generic collection of rounded SaaS cards. Use typography, restrained dividers, strong section hierarchy, and a small number of purposeful visualizations.

Technical values such as bitrate, formats, and file sizes may use the Design System's monospace typeface.

---

## 16. Decision Rules

- Statistics describes the collection; it does not manage it.
- The first implementation uses only reliable data already available in the current model/database.
- Formats, top artists, and top albums are valid first-class analyses.
- Year-based timeline remains deferred until reliable year data exists.
- Genre remains deferred until reliable genre data exists.
- Cover-art analysis remains deferred until artwork handling is defined.
- Statistics does not duplicate Health's diagnostic role.
- Statistics does not duplicate Library's song-level workflow.
- Contextual navigation to Library preserves the selected dimension.
- No visualization is introduced solely to fill space.
