# Health

> Interaction model for library diagnostics, issue review, and navigation to corrective actions.

## Document Information

| Item | Value |
|---|---|
| Category | Design / UX |
| Audience | Developers |
| Status | Defined |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## 1. Purpose

Health explains the condition of the current music library and identifies items that need attention.

It is a diagnostic and decision-support screen. It should help the user understand whether the library is healthy, what is wrong when it is not, and where to go to resolve a specific issue.

Health is not the place where individual metadata edits or file moves are performed.

## 2. User Question

> **"What needs attention in my library?"**

The screen should answer this question without requiring the user to inspect the entire library manually.

## 3. Responsibilities

Health is responsible for:

- presenting an overall library health score or equivalent summary;
- showing the categories of detected issues;
- showing issue counts and concise explanations;
- distinguishing healthy, warning, and error states;
- providing contextual actions such as `Review` for issue categories;
- sending the user to the affected content in Library when the issue is song-level or filterable;
- hosting duplicate-group review when duplicate findings require a multi-song context;
- reflecting the result of completed corrective actions after the library is refreshed or re-analyzed.

## 4. Non-Responsibilities

Health must not:

- become a second music browser;
- perform silent metadata changes;
- move or rename files directly;
- replace the Quick Fix workspace;
- reproduce the complete Statistics dashboard;
- require the user to understand internal scanner or database terminology;
- become a permanent duplicate-management workspace when a dedicated duplicate workflow is not required.

## 5. Entry Points

Primary entry point:

- Sidebar → Health.

Contextual entry points may include:

- Library → health-related status or issue action;
- completion of a scan that identifies issues;
- completion of a corrective action that requires re-evaluation.

Health should remain a top-level destination even when there are no issues, because a healthy library is itself a meaningful state.

## 6. Primary Actions

### Review issue category

The primary action for a non-empty issue category is `Review`.

The action should preserve the issue context.

For a song-level issue:

```text
Health
  ↓
Missing metadata · 14 songs
  ↓
Review
  ↓
Library filtered to affected songs
```

If the category identifies exactly one song, Library may additionally select that song.

The user should not be sent to a generic Review destination and forced to reconstruct the context.

### Review duplicates

Duplicate findings are different from ordinary issue categories because a duplicate group contains multiple songs.

Until Library has a suitable multi-selection workflow, duplicate review remains contextual to Health:

```text
Health
  ↓
Duplicates
  ↓
Duplicate groups
  ↓
Inspect group
```

The user may then navigate to Library for individual songs when appropriate, but Health remains responsible for presenting the relationship between the files in the duplicate group.

### Re-analyze / refresh

When the implementation exposes an explicit health analysis action, it should communicate that analysis is being performed and show the resulting state when complete.

A refresh after a successful fix should update the health result rather than requiring the user to restart the application.

## 7. Issue Categories

The exact categories should follow the diagnostics already supported by the application. Current examples include:

- missing metadata;
- zero duration;
- low bitrate;
- duplicate groups;
- orphaned or filesystem-related problems when reported by the current health implementation.

New categories should only be added when the underlying data and diagnostic behavior exist.

Health must not invent a category merely because it would look useful in the interface.

## 8. States

### Initial / Not analyzed

The user has opened Health but there is no current diagnostic result.

The screen should explain that analysis is required and provide the appropriate action if one exists.

It should not present fabricated or stale health numbers as current.

### Loading / Analyzing

Show that analysis is in progress.

The user should be able to understand:

- that work is occurring;
- that the displayed result may not yet be final;
- when the operation completes.

Long-running analysis should not make the application appear frozen.

### Healthy

When no actionable issues are detected, Health should communicate success explicitly.

Example structure:

```text
LIBRARY HEALTH

100 / 100
HEALTHY

No issues found.

Last analysis
09 Aug 2026 · 12:00
```

The healthy state should use the semantic success treatment defined by the Design System.

### Issues found

When problems exist, the score/summary should be accompanied by actionable categories.

Example:

```text
LIBRARY HEALTH

96 / 100
NEEDS ATTENTION

ATTENTION

14  Missing metadata       Review →
 1  Zero duration          Review →
 0  Duplicates             —
```

The number alone is not sufficient. The user must be able to understand what produced the result.

### Error

If analysis itself fails, distinguish an analysis failure from a library health problem.

The UI should say that the health check could not be completed rather than reporting a low score.

## 9. Review Workflow

`Review` is a workflow, not a primary navigation destination.

For ordinary song-level issues, the expected flow is:

```text
Health
  ↓
Issue category
  ↓
Review
  ↓
Library
  ↓
Affected song(s)
  ↓
Quick Fix / contextual action
  ↓
User confirms an action
  ↓
Library updated
  ↓
Health recalculated
```

For category-level review, Library opens with an appropriate issue filter rather than pretending that every issue can be represented as a single selection.

For a single affected song, the song should be selected so that its contextual actions are immediately available.

For duplicate groups, Health retains the group-level review context until a dedicated multi-selection experience exists in Library.

## 10. Contextual Interactions

### Health → Library

When the user chooses `Review`, Library should receive enough context to show the relevant songs.

Examples:

- missing metadata → filter/select songs with missing metadata;
- zero duration → filter/select affected songs;
- low bitrate → filter/select affected songs;
- orphaned/file issue → filter/select affected songs where supported.

The exact mechanism (filter or selection) depends on whether the target is one song or a set of songs.

### Health → Duplicate Review

Duplicate groups are reviewed in Health while multi-song Library selection is unavailable.

A future dedicated duplicate workspace may be introduced only if the interaction grows beyond what Health can reasonably contain.

### Health → Quick Fix

Quick Fix is not a Health editing surface.

Health identifies the problem; the Library/Quick Fix context performs the user-confirmed correction.

### Health → Statistics

Health may link to Statistics only when aggregate context helps explain a result. This is secondary and should not distract from issue resolution.

## 11. Navigation Rules

- Entering Health from the sidebar starts at the current library-wide health state.
- Entering Library through `Review` preserves the selected issue context.
- Returning to Health after a correction should show the updated state when recalculation has completed.
- Health should not force navigation to another screen when the library is healthy.
- Duplicate review remains within Health unless a future dedicated workflow is explicitly introduced.
- Back/navigation should not discard an in-progress analysis without communicating the state to the user.

## 12. Data Interaction

Health reads diagnostic information from the current library state and health analysis.

It may depend on:

- song metadata;
- duration;
- bitrate;
- filesystem/library state;
- duplicate information where supported by the existing health model.

Health may request a refresh/recalculation after a completed operation.

Health must not directly own metadata-writing or file-moving logic.

## 13. UX Rules

- A score must always have an explanation.
- An issue count should lead to an actionable next step when possible.
- Healthy state is a meaningful result, not an empty page.
- `Review` should preserve context instead of opening a generic screen.
- Health diagnoses; Library and Quick Fix act.
- Duplicate groups require relationship-aware review rather than a simple flat song list.
- Do not use technical database/scanner language when a user-facing explanation is possible.
- Do not treat a failed health analysis as a health problem.
- Do not make automatic corrections from the Health screen.

## 14. Accessibility

- Issue categories must not rely on color alone.
- Success/warning/error indicators require text or icon support.
- `Review` actions must be keyboard reachable and visibly focused.
- The health score must have a textual representation in addition to visual treatment.
- Loading and completion states must be announced through accessible text where supported.
- The order of keyboard focus should follow the visual and decision order: summary → issues → actions.
- Duplicate groups must expose the relationship between files textually, not only through spatial grouping.

## 15. Visual Notes

Health should use the Studio Editorial language without becoming a decorative dashboard.

Recommended hierarchy:

```text
HEALTH

Large health result
        ↓
Short explanation
        ↓
Issues requiring attention
        ↓
Review actions
        ↓
Supporting diagnostics
```

Semantic colors should communicate state:

- success → healthy;
- warning/error → issues requiring attention;
- info → explanatory context;
- primary accent → actionable controls.

The interface should favor clear dividers, strong typography, restrained cards, and meaningful whitespace over decorative instrumentation.

## 16. Decision Rules

- Health is the diagnostic hub, not the editing workspace.
- Review is contextual and is not a sidebar destination.
- A detected issue must explain what is wrong before asking the user to act.
- Review actions should lead to the affected songs in Library when the issue is song-level.
- Duplicate groups remain reviewable in Health until Library supports a suitable multi-selection interaction.
- Quick Fix remains responsible for explicit metadata correction.
- A correction is not considered complete until the affected library state and health result can be refreshed.
- No health score is shown as current when the underlying analysis has failed.
- No new diagnostic category is introduced without corresponding backend/data support.
