# Melody Sync Product Roadmap

## Purpose

This document maps the intended evolution of Melody Sync without turning every useful idea into an immediate implementation task.

The roadmap separates:

- work required to consolidate the current product;
- near-term refinements to existing workflows;
- future expansions that solve identified needs;
- ideas deliberately kept out of scope.

The roadmap is directional rather than a fixed release schedule. A future item should only move forward when the current product state and real usage justify it.

---

## Guiding Principles

Melody Sync should continue to evolve according to the following rules:

1. Consolidate before expanding.
2. Measure before optimizing further.
3. Reuse the existing architecture whenever possible.
4. Prefer explicit user confirmation over unsafe automation.
5. Add metadata fields and integrations only when they provide practical value.
6. Do not build a feature merely because it could exist.
7. Keep external services replaceable and outside the core domain.
8. Keep the project focused on music-library review, correction, and organization.

The preferred progression is:

```text
Reliable data
    ↓
Scalable interaction
    ↓
Resilient desktop UX
    ↓
Workflow refinement
    ↓
Expansion when justified
```

---

## Cycle Status (2026-08)

The **Consolidation & UX Refinement** cycle is complete. The following items from the NOW/NEXT sections are delivered and validated; this map keeps them as reference rather than removing the context behind each decision:

- Metadata Read-Path Validation — complete
- Large-Library Performance — implemented
- Responsive Desktop Behavior — complete
- Library Information Density — complete
- Semantic Status Colors — complete
- Health → Library → Quick Fix refinement — complete
- Statistics workflow validation — complete
- Organize workflow validation — complete
- Keyboard and Accessibility review — complete

The current stage is **Product Validation** (see `docs/planning/product-validation.md`), followed by Product Hardening, real use, and only then an expansion decision. The FUTURE sections below remain the reference for expansion candidates.

---

# NOW — Product Consolidation

## Metadata Read-Path Validation

**Status:** Complete (2026-08)

### Goal

Finish validating the metadata read path so that the metadata foundation can be considered consolidated before further feature expansion.

### Scope

- Validate the `readMetadata()` pipeline as the read-path hub.
- Confirm supported formats behave according to documented limitations.
- Confirm read and write behavior remain coherent.
- Preserve read-back verification after metadata writes.
- Document real format limitations instead of hiding unsupported behavior.

### Out of scope

- New metadata providers.
- Automatic metadata enrichment.
- New metadata fields without a demonstrated need.
- Automatic tag application.

### Completion condition

The supported read path is validated, known limitations are documented, and no unresolved metadata reliability issue blocks normal use.

---

## Large-Library Performance

**Status:** Implemented — validation and maintenance as needed

### Goal

Keep the existing application responsive when working with larger music libraries without introducing unnecessary infrastructure.

### Current direction

The implementation should avoid additional complexity unless a real bottleneck is measured.

Possible areas to observe:

- Library loading;
- filtering;
- searching;
- sorting;
- Health calculations;
- Statistics aggregation;
- selection changes;
- list rendering.

### Rule

Do not introduce paging, complex caches, additional data layers, or concurrency mechanisms without evidence that the current implementation requires them.

---

# NEXT — UX Refinement

## 1. Responsive Desktop Behavior

**Status:** Complete (2026-08) — `WindowSizeClass` compact/medium/expanded

### Goal

Keep Melody Sync usable across different desktop and notebook window sizes.

This is desktop layout resilience, not mobile support.

### Scope

Review behavior for:

- Sidebar;
- Library;
- Health;
- Statistics;
- Review;
- Quick Fix;
- Organize;
- Settings;
- tables and lists;
- overflow and scrolling;
- action areas with limited space.

### Expected window states

```text
Large window
→ use available space effectively

Normal window
→ primary desktop layout

Narrow window
→ preserve usability through adaptation

Reduced height
→ preserve access through scrolling and layout priorities
```

### Out of scope

- Mobile application;
- touch-first redesign;
- mobile-specific navigation;
- a separate UI architecture for phones.

---

## 2. Library Information Density

**Status:** Complete (2026-08) — Filters disclosure, compact columns, result count

### Goal

Ensure the Library exposes useful information without becoming visually overloaded as the library grows.

### Review

Each song representation should distinguish:

```text
Primary information
Secondary information
Useful status indicators
Selection/context
Relevant actions
```

Questions to answer:

- Which information must always be visible?
- Which information is useful only on demand?
- Which indicators help the user make a decision?
- Which information is redundant?
- Does scanning remain practical with a large library?

### Rule

Do not add columns or persistent indicators without a concrete decision-making benefit.

---

## 3. Semantic Status Colors

**Status:** Complete (2026-08) — `colorRoles()` + `ChartPalette` (DesignSystem §4.5)

### Goal

Use consistent semantic meaning for application status instead of assigning colors independently in each screen.

### Initial semantic roles

```text
Success
Warning
Error
Info
Neutral
```

These roles should only be expanded when the application has a real state requiring a distinct meaning.

Possible mappings include:

- metadata complete;
- metadata missing;
- write verified;
- write failed;
- attention required;
- external service unavailable;
- informational state.

### Rule

Color must reinforce meaning, not become the only way to communicate a state.

---

# THEN — Existing Workflow Refinement

## 4. Health → Library → Quick Fix

### Goal

Refine the existing contextual workflow rather than creating another diagnostic or correction system.

### Target flow

```text
Problem detected
    ↓
Problem understood
    ↓
Library provides context
    ↓
Song or issue selected
    ↓
Quick Fix
    ↓
Explicit Apply
    ↓
Result verified
```

### Areas to validate

- context preservation;
- clear problem descriptions;
- empty states;
- individual versus multi-song behavior;
- feedback after correction;
- return to Library;
- result visibility.

---

## 5. Statistics

### Goal

Confirm that Statistics provides useful exploration and leads naturally back to the Library.

### Existing interaction model

```text
Artist
→ filtered Library

Album
→ filtered Library

Format
→ filtered Library
```

### Rule

Do not add dimensions such as Year, Genre, Decade, Composer, or Label merely to make Statistics broader.

New dimensions require reliable data and a demonstrated use case.

---

## 6. Organize

### Goal

Validate the real organization workflow before adding automation.

### Expected flow

```text
File received
    ↓
Metadata reviewed
    ↓
Destination determined
    ↓
Organize
    ↓
Result verified
```

Future improvements should come from real friction observed during use.

### Out of scope for now

- complex rule engines;
- invisible background organization;
- large automatic organization systems.

---

# USABILITY AND ACCESSIBILITY

## 7. Keyboard and Focus Review

Review:

- keyboard navigation;
- focus order;
- existing shortcuts;
- focus after section changes;
- focus after dialogs;
- selection behavior in lists.

The objective is predictable interaction, not a separate input architecture.

---

## 8. Accessibility Review

Review:

- contrast;
- states not communicated only by color;
- sufficient textual context;
- understandable error messages;
- meaningful status indicators.

This work should align with Semantic Status Colors rather than duplicating its responsibilities.

---

# MAINTENANCE

Maintenance should remain proportional to the project.

## Tests

Add tests when they protect:

- a regression;
- a critical rule;
- metadata behavior;
- filesystem behavior;
- database behavior;
- navigation context;
- an important integration boundary.

Test count is not itself a product goal.

---

## Dependencies

Review dependencies when justified by:

- security fixes;
- compatibility requirements;
- important releases;
- obsolete dependencies.

Avoid upgrades solely to keep version numbers current.

---

## Documentation

Documentation should evolve with meaningful changes:

```text
Architectural decision
→ architecture / ADR documentation

New or changed behavior
→ relevant design or planning documentation

Important resolved issue
→ project history or appropriate record

Significant implementation change
→ update affected current-state documentation
```

Do not create documentation without a clear maintenance or navigation purpose.

---

## Repository Hygiene

Repeat repository-wide audits when justified, for example:

- before a significant release;
- after major structural changes;
- when sensitive-data exposure is suspected;
- after substantial repository growth.

The existing audit remains a historical snapshot of the state it reviewed.

---

# FUTURE — Expansion When Justified

## 9. Metadata Enrichment

**Strategic future expansion**

This is the first major expansion currently considered strongly aligned with Melody Sync's purpose, but it should begin only when the manual workflow demonstrates a real need for faster identification.

### Planned flow

```text
File
  ↓
YouTube identification
  ↓
Simple identification context
  ↓
Metadata provider
  ↓
Candidate / suggestion
  ↓
Quick Fix review
  ↓
User confirmation
  ↓
Apply
  ↓
Write + read-back verification
```

### Responsibilities

```text
YouTube
→ discovery / identification support

Metadata provider
→ structured metadata suggestions

Lyrics
→ complementary information only

Quick Fix
→ review and explicit application

User
→ final authority
```

### Rules

- YouTube does not define final metadata.
- Channel/uploader information must not automatically become `Artist`.
- Reuploads and ambiguous titles are expected uncertainty cases.
- Lyrics are informative and must not automatically populate tags.
- Providers remain replaceable.
- Suggestions remain editable.
- No provider writes directly to the library.

---

## 10. Additional Metadata Fields

Possible future fields include:

- Year;
- Genre;
- Track;
- Disc;
- Album Artist;
- Composer;
- others justified by use.

Fields should be added incrementally when the workflow benefits from them.

---

## 11. Artwork

Potential future support:

- inspect existing artwork;
- review artwork;
- replace artwork;
- write supported artwork changes.

Do not begin artwork infrastructure until there is a practical need beyond metadata basics.

---

## 12. Lightweight Playback

Potential future purpose:

```text
Select
→ listen briefly
→ confirm or review
```

Melody Sync should not become a full music player as a result.

---

## 13. Additional Metadata Providers

Future providers may be added behind the existing integration model when a concrete provider offers useful coverage or reliability.

Possible provider types:

```text
Provider A
Provider B
Local/manual source
```

The project should not commit to a specific provider before the Metadata Enrichment phase begins.

---

# PERSONAL WORKFLOW FIT

The current project direction should continue supporting the real workflow it was designed around:

```text
MOBILE

Find music
↓
Download
↓
Synced folder

        ↓ Syncthing

NOTEBOOK

Library
↓
Health
↓
Review
↓
Quick Fix
↓
Metadata / organization
↓
Processed file

        ↓ Syncthing

MOBILE

Listen to processed music
```

Melody Sync is responsible for review, correction, and organization within this workflow.

It does not need to replace every surrounding tool.

---

# DELIBERATELY OUT OF SCOPE

The following ideas are not current roadmap goals:

- full music player;
- dedicated mobile Melody Sync application;
- built-in synchronization protocol;
- YouTube downloader;
- automatic lyrics tagging;
- public HTTP API;
- cloud account system;
- social features;
- streaming service;
- complex automatic organization engine;
- metadata automation without explicit review.

An item may return to consideration only when a concrete use case demonstrates that the current boundaries are insufficient.

---

# Priority Order

The intended order is:

```text
COMPLETED — Consolidation & UX Refinement (2026-08)

1. Metadata read-path validation            ✓
2. Large-library behavior                   ✓
3. Responsive Desktop Behavior              ✓
4. Library Information Density              ✓
5. Semantic Status Colors                   ✓
6. Health → Library → Quick Fix refinement  ✓
7. Statistics workflow validation           ✓
8. Organize workflow validation             ✓
9. Keyboard and accessibility review        ✓

COMPLETED — Product Validation & Hardening (2026-08)

10. End-to-end workflow validation            ✓ (13/13 checks, real-library baseline)
11. Guided GUI validation                     ✓ (real use in progress)
12. Product Hardening (safe failure behavior)  ✓ (no critical findings)

NOW — Real Use (Track A — active)

13. Use the real library; collect friction in planning/friction-register.md

PARALLEL — Maintenance (Track B)

    Documentation upkeep, test maintenance, dependency review, small improvements.
    Does not interrupt Track A.

PARALLEL — Future Design (Track C — design only, no implementation)

    Metadata Enrichment, Playback, fields, Artwork.
    Design and specify; execute only when Track A justifies.

FUTURE (gated on real-use friction — Track A evidence)

14. Metadata Enrichment when justified by real use
15. Additional metadata fields when needed
16. Artwork when justified
17. Lightweight playback if useful for review
18. Additional providers when the enrichment architecture needs them
```

This order is intentionally flexible inside each stage. A real reliability issue may always take priority over planned UX work. Product Validation findings are registered and prioritized — only critical bugs, security/data problems, and regressions are fixed immediately; friction items feed the register and a later decision.

---

## Expansion Decision Criteria

Each future expansion has explicit triggers that must be confirmed by real use before implementation begins.

### Metadata Enrichment

Implement when **all** of these are confirmed by real use:

- Many songs require manual identification (repetitive);
- The local matcher (`SongMatcher`) is insufficient (<70% coverage);
- A metadata provider can significantly improve the identification quality;
- The user is willing to configure an API key.

### Lightweight Playback

Implement when:

- Identifying or confirming metadata requires listening frequently;
- A brief preview (5–10s) would eliminate the need to open an external player.

### Additional Metadata Fields

Implement when:

- The absence of a specific field (Year, Genre, etc.) prevents a real decision or organization step;
- The field can be reliably read from existing tags or provided by enrichment.

### Artwork

Implement when:

- Missing or incorrect artwork becomes a recurring need during curation;
- The storage/retrieval/caching strategy is defined.

### None of the above

If real use shows the current workflow is sufficient, continue with maintenance only.

---

## Decision Rule for New Ideas

Before adding a new feature to the implementation roadmap, answer:

1. What real problem does it solve?
2. Can an existing feature or workflow solve it already?
3. Does it improve the project's main purpose?
4. What new complexity does it introduce?
5. Can it remain a future option instead of being implemented now?

If the value is unclear, the item belongs in a future or deferred category rather than the next implementation block.
