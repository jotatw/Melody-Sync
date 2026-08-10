# Navigation and Metadata Documentation Review — August 2026

> Cross-document consistency review for the current navigation, contextual workflows, and metadata-identification planning.

## Document Information

| Item | Value |
|---|---|
| Category | Architecture Review |
| Status | Reviewed / Actions identified |
| Scope | Navigation, Library context, Statistics, Health, Quick Fix, metadata integrations |
| Last Updated | 2026-08-10 |

---

## 1. Review Objective

Verify that the documentation produced during the navigation and metadata-planning work describes one coherent model before another implementation block is opened.

The review compares:

- `docs/design/navigation.md`;
- `docs/planning/implementation-block-01-navigation-context.md`;
- `docs/design/screens/statistics.md`;
- `docs/design/screens/health.md`;
- `docs/design/screens/library.md`;
- `docs/research/quick-fix-hud.md`;
- `docs/planning/metadata-workflow.md`;
- `docs/integrations/youtube-identification.md`;
- `docs/integrations/metadata-providers.md`;
- `docs/integrations/lyrics-policy.md`;
- `docs/planning/metadata-foundation.md`.

The review intentionally distinguishes documented target behavior from the current implementation.

---

## 2. Overall Result

**Architecture is coherent. No fundamental redesign is required.**

The documents agree on the central model:

```text
Health
  → identifies problems

Statistics
  → explores aggregate information

Library
  → is the central operational workspace

Quick Fix
  → assists correction of one selected song

YouTube
  → identifies / discovers

Metadata provider
  → supplies structured candidates

Lyrics
  → provides informational support

User
  → makes the final metadata decision
```

The main remaining documentation work is synchronization of a few **Current vs Target** statements, not a change to the architecture itself.

---

## 3. Navigation Consistency

### Confirmed

The navigation contract and Implementation Block 01 agree that:

```text
Primary
Library
Statistics
Health
Organize
Settings
About
```

while `Review` and `Duplicates` remain contextual workflows until the sidebar consolidation is validated.

The Library is the operational center, and Health/Statistics may enter Library with preserved context.

### Confirmed implementation direction

The current implementation now contains an explicit `IssueContext` for multi-song Health navigation, while single-song navigation remains selection-based. Artist, format and album filters are also represented in the current state layer.

This means the conceptual model in Block 01 is no longer merely hypothetical for these contexts.

### Remaining distinction

`Review` and `Duplicates` must still be treated as current implementation capabilities until the later sidebar block removes them from primary navigation.

---

## 4. Health → Library

The agreed behavior is:

```text
Single issue
  → Library
  → select affected song
  → Quick Fix available
```

and:

```text
Multiple issues
  → Library
  → issue filter/context
  → affected songs only
  → user selects one
  → Quick Fix
```

`Review all` therefore means **open the affected set in Library**, not "automatically edit every song" and not "silently open only the first song".

This is consistent with the current `IssueContext` implementation model.

---

## 5. Statistics → Library

The intended interaction is:

```text
Artist → Library + artist filter
Album  → Library + album filter
Format → Library + format filter
```

The current state now contains all three filter dimensions, including album.

Therefore, album exploration should no longer be described as architecturally unsupported. It may still require UI validation, but the state-layer prerequisite identified in the original Implementation Block has been satisfied.

### Important rule

A contextual filter must remain visible and removable in Library.

Filter and selection are independent:

```text
Filter   = which songs are shown
Selection = which song is being worked on
```

Both may coexist.

---

## 6. Statistics and Missing Metadata

Statistics must never manufacture aggregate values from uncertain context.

If 100 songs have `Artist = Artist A` and 20 songs have no Artist value, the aggregate should not silently count all 120 as Artist A because their filenames, folders or YouTube context suggest that artist.

The conceptual representation is:

```text
Artist A       100
Artist B        84
Missing Artist  20
```

`Missing Artist` is a semantic missing-field context, not an actual Artist value.

This keeps Statistics descriptive and leaves diagnosis to Health.

---

## 7. Quick Fix Consistency

Quick Fix remains the operational correction point for a selected song.

The current implementation/history documents the older `Health → Review → Quick Fix` path. The target navigation model changes the preferred path to:

```text
Health
  → Library
  → selected / filtered issue context
  → Quick Fix
```

The old Review flow remains a valid current capability until sidebar consolidation is complete. It should not be described as the long-term navigation target.

Quick Fix itself remains explicitly user-approved:

```text
Diagnosis
  → suggestion
  → user review
  → explicit Apply
  → write
  → read-back
  → library refresh
```

---

## 8. YouTube Consistency

All metadata documents agree that YouTube is an identification/discovery source rather than an authoritative metadata catalogue.

The following are explicitly not assumed:

```text
YouTube channel = Artist
Uploader = Artist
Video title = structured metadata
```

Reuploads and ambiguous titles are expected.

A parser may generate candidates, but candidates remain editable and require explicit approval.

The approved conceptual boundary is:

```text
YouTube
  → possible identity
  → metadata provider
  → structured suggestion
  → Quick Fix
  → user approval
  → metadata write
```

---

## 9. Metadata Provider Consistency

The provider model is consistent with the existing metadata foundation.

The provider boundary is:

```text
Provider API
  → adapter
  → application candidate/suggestion
  → Quick Fix
```

Providers do not write directly to files or the database.

The exact future provider, matching algorithm, confidence scoring and ranking rules remain intentionally undefined.

---

## 10. Lyrics Consistency

Lyrics.ovh is documented as an informational provider.

The agreed behavior is:

```text
lyrics lookup
  → display
```

and never:

```text
lyrics
  → automatic Artist/Title tags
```

This is consistent across the lyrics policy and metadata workflow.

---

## 11. Metadata Foundation Boundary

The existing metadata foundation is treated as implemented infrastructure rather than future architecture.

It already covers the current title/artist/album write path, provider abstraction, capability registry, typed write results, read-back validation, persistence coordination and test fixtures.

Future fields such as genre, year and artwork require separate domain/database decisions before being added to the workflow.

---

## 12. Documentation Corrections Identified

The following are documentation synchronization items rather than architectural changes:

1. The Implementation Block 01 status should eventually move from `Approved for implementation` to an implementation/validation state once the current implementation has completed its local validation.
2. The Statistics specification should treat album context as supported by the current state layer, while still requiring UI validation.
3. The Quick Fix historical document should distinguish its implemented legacy Review path from the target Health → Library → Quick Fix navigation.
4. The documentation index should reflect the implementation/validation state of Block 01 after the user confirms local tests.

These corrections should be made as documentation maintenance, not as code changes.

---

## 13. No New Architecture Required

The review does **not** justify introducing:

- a new routing framework;
- a generic navigation service;
- a universal automatic metadata engine;
- a YouTube-owned metadata model;
- a review queue;
- a separate player architecture inside Melody Sync;
- new database fields solely to support navigation.

Existing state and service boundaries are sufficient for the currently defined behavior.

---

## 14. Readiness

The navigation/metadata documentation is sufficiently defined to continue with bounded implementation work.

Before opening the next implementation block, the remaining requirement is validation of the already implemented Block 01 behavior rather than additional architectural planning.

The next block should not start until Block 01 is explicitly marked validated.

---

**End of Document**
