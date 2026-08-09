# Statistics

> Interaction model for exploring the composition and characteristics of the music library.

## Document Information

| Item | Value |
|---|---|
| Category | Design / UX |
| Audience | Developers |
| Status | Defined |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## 1. Purpose

Statistics provides an overview of how the music collection is composed.

It is an exploratory and observational screen. Its purpose is to help the user understand patterns in the library without turning those patterns into a required workflow.

## 2. User Question

> **"How is my music collection composed?"**

The screen should make the library's size, composition, and distribution understandable at a glance and allow deeper inspection when useful.

## 3. Responsibilities

Statistics is responsible for:

- presenting meaningful aggregate library metrics;
- showing the distribution of supported audio formats;
- showing the artists most represented in the library;
- showing the albums most represented in the library;
- providing clear relationships between totals and visualizations;
- allowing contextual navigation back to Library when an aggregate item is actionable.

Statistics should prioritize information that can be derived reliably from the current library data.

## 4. Non-Responsibilities

Statistics must not:

- modify metadata;
- move, rename, or delete files;
- perform library organization;
- become a duplicate of Health;
- present unsupported metrics merely because they would make the dashboard look more complete;
- require the user to interpret charts without readable labels or values.

Metrics that require backend/model data not currently available should remain deferred rather than being inferred unreliably.

## 5. Entry Points

Primary entry point:

- Sidebar → Statistics.

Contextual entry points may include:

- Library → statistics shortcut, if introduced later;
- a future dashboard summary that links to detailed statistics.

Statistics should not be the mandatory destination after a scan. The scan result belongs to Library, while Statistics is available when the user wants to explore the collection.

## 6. Information Hierarchy

The screen should follow this order:

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

The exact visual composition may evolve, but the hierarchy should remain understandable without requiring the user to read every chart.

## 7. Core Metrics

Current supported overview metrics include:

- songs;
- artists;
- albums;
- total duration;
- library size;
- average bitrate where available.

These values should have a consistent relationship with the library currently being analyzed.

The interface should distinguish a current database/library result from a scan that is actively running.

## 8. Supported Visualizations

### Formats

Format distribution is a first-class visualization because format data already exists in the current statistics model.

It may be represented as a pie/donut chart or another compact distribution visualization, provided that:

- every visible segment has a readable label or legend;
- small categories remain discoverable;
- the total is understandable;
- colors are not the only means of differentiation.

### Top Artists

Artists may be ranked by song count.

A horizontal bar visualization is preferred when comparing multiple artists because the labels remain readable.

The number of displayed artists should be bounded for readability, with a way to indicate that the visualization is a top-N view rather than the complete artist list.

### Top Albums

Albums may be ranked by song count.

A proportional bar/list treatment is appropriate when it communicates ranking without requiring a large charting surface.

### Timeline

A year-based timeline is **deferred** while the `Song` model does not provide a reliable year field.

The application must not derive year heuristically from arbitrary file paths merely to populate a chart unless that behavior is explicitly specified and validated later.

## 9. States

### Initial / No data

If no library data is available, Statistics should explain why there is nothing to display and direct the user toward Library rather than presenting empty charts.

Example:

```text
STATISTICS

No library data available.

Scan a music directory to build your library.

[Go to Library]
```

### Loading

When statistics are being calculated or refreshed, communicate that the values are being updated.

Existing data may remain visible if the implementation can do so without presenting stale values as current.

### Ready

Show the overview metrics followed by the supported analysis.

### Empty category

A category with no meaningful data should have a concise explanation rather than an empty chart frame.

### Error

If statistics cannot be calculated, distinguish calculation failure from a valid library containing zero items.

Do not replace failed data with fabricated zeros.

## 10. Contextual Interactions

Statistics is primarily observational, but aggregate information can lead naturally to Library.

### Artist → Library

If an artist is actionable, selecting an artist may open Library filtered to that artist.

```text
Statistics
  ↓
Top Artists
  ↓
Nirvana
  ↓
Library
  ↓
Artist = Nirvana
```

### Album → Library

Similarly, an album may open Library filtered to that album.

### Format → Library

A format category may open Library filtered to that format when the implementation supports it.

These are contextual exploration actions, not required workflows.

## 11. Navigation Rules

- Statistics is a top-level destination because collection exploration is a distinct user goal.
- Opening Statistics should not automatically trigger a scan.
- Leaving Statistics should not discard Library filters or selections unless the user explicitly changes them elsewhere.
- Contextual navigation to Library should preserve the selected dimension as a filter where possible.
- Returning from Library should not imply that a modification was performed by Statistics.

## 12. Data Interaction

Statistics reads aggregate information from the current library/database state.

Current reliable inputs include:

- song count;
- artist count;
- album count;
- duration;
- library size;
- formats;
- average bitrate where available;
- song-level artist/album information for ranking.

Statistics must not require new metadata fields merely to complete the first dashboard implementation.

Deferred data includes:

- year-based analysis;
- genre analysis if genre is not currently represented in the model;
- cover-art based analysis.

## 13. UX Rules

- Every number should answer a useful question.
- Charts must communicate data, not merely decorate the screen.
- The same metric should not be repeated in multiple places without a reason.
- Aggregate values should remain consistent with Library and Health.
- A visualization must have readable labels and a textual fallback.
- Statistics should never become an operational workflow.
- Unsupported or unreliable data should be deferred rather than guessed.
- The user should be able to move from an interesting aggregate to the corresponding songs in Library when that interaction is meaningful.

## 14. Accessibility

- Charts must not rely on color alone.
- Every chart must have a textual representation of its important values.
- Labels and values must remain readable at supported window sizes.
- Interactive chart/list items must be keyboard reachable where interaction exists.
- Focus state must be visible.
- Large numerical metrics must have accessible labels explaining their meaning.

## 15. Visual Notes

Statistics should use the Studio Editorial language primarily for hierarchy and composition, not decoration.

Recommended structure:

```text
STATISTICS

Large collection summary
        ↓
Clear numerical metrics
        ↓
Editorial section heading
        ↓
Focused visualization
        ↓
Next analysis section
```

The page should have enough negative space to make each analytical section distinct, while avoiding the appearance of a generic SaaS dashboard made entirely from rounded cards.

Use typography to establish hierarchy and restrained dividers to separate analytical sections. Technical values may use the monospace face defined by the Design System.

## 16. Decision Rules

- Statistics describes the collection; it does not manage it.
- The first implementation uses only data already available in the current model/database.
- Formats, top artists, and top albums are valid first-class analyses.
- Timeline by year remains deferred until reliable year data exists.
- Genre and cover-art analysis remain deferred until their underlying data model and ingestion behavior are defined.
- Statistics does not duplicate Health's issue diagnosis.
- Statistics does not duplicate Library's song-level browsing.
- Contextual navigation to Library should preserve the dimension that caused the navigation.
- No chart is added solely to fill empty space.
