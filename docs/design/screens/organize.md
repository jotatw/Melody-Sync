# Organize

> Interaction model for planning and applying controlled changes to the physical organization of the music library.

## Document Information

| Item | Value |
|---|---|
| Category | Design / UX |
| Audience | Developers |
| Status | Implemented / refining |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## 1. Purpose

Organize helps the user identify files that do not match the library's organization rules, review the proposed destinations, and explicitly apply the resulting plan.

It is an operational workflow. Unlike Statistics and Health, Organize may eventually change the filesystem, so the interface must make the transition from analysis to execution explicit.

## 2. User Question

> **"How should I organize these files, and what will happen if I apply the plan?"**

The user must be able to understand the proposed changes before any file is moved.

## 3. Responsibilities

Organize is responsible for:

- analyzing the current library against the configured organization rules;
- presenting the resulting plan;
- showing current path, destination, reason, and status for planned moves;
- distinguishing files that need action from files already organized;
- allowing the user to review the plan before applying it;
- applying an explicitly confirmed plan when the implementation supports execution;
- reporting successful, skipped, and failed operations clearly;
- returning the user to a consistent library state after changes are applied.

## 4. Non-Responsibilities

Organize must not:

- silently move files immediately after analysis;
- edit metadata as a side effect of planning;
- replace Health's diagnostic role;
- become a general-purpose file manager;
- hide the destination or reason for a proposed move;
- treat a proposed move as successful before execution completes.

## 5. Entry Points

Primary entry point:

- Sidebar → Organize.

Contextual entry points may include:

- Health → organization-related action, if introduced later;
- Library → organize selected songs, if the workflow is eventually scoped to a selection.

The initial workflow is library-wide unless the existing implementation explicitly supports a selection-based plan.

## 6. Primary Workflow

Organize follows a plan-first model:

```text
ORGANIZE
   ↓
ANALYZE
   ↓
PLAN CREATED
   ↓
REVIEW PLAN
   ↓
APPLY
   ↓
RESULT
   ↓
LIBRARY
```

The user should always be able to identify which stage they are in.

## 7. Analyze

Analysis determines which files require organization according to the current rules.

The result should summarize:

- files to move;
- files already organized;
- skipped items where applicable;
- any analysis errors.

Example:

```text
Organization plan

690 to move
0 already organized
0 skipped
```

The summary is informational. It does not mean any filesystem operation has occurred.

## 8. Plan Review

The plan is the central interaction surface of Organize.

Each planned item should make at least these values understandable:

- current path;
- destination path;
- reason;
- status.

Example:

```text
Current                         Destination                    Reason       Status
Queen/Track01.m4a       →      Queen/A Night.../Track01       mismatch     Needs move
```

Long paths should be visually truncated where necessary, with the complete value available through an appropriate accessible tooltip or detail interaction.

The user must be able to inspect the destination before applying the plan.

## 9. Apply

Applying a plan is an explicit user action.

The interface should make clear that applying will modify the filesystem.

A confirmation step is appropriate when the operation is destructive, irreversible, or otherwise risky according to the implementation.

During execution:

- show progress;
- identify that files are being moved;
- prevent duplicate submissions while the operation is active;
- report failures individually where possible.

The UI must not claim success before the operation actually completes.

## 10. Result States

### Initial

No current plan exists.

The screen should explain what Organize does and offer the analysis action.

```text
ORGANIZE

Keep your music arranged according to your library rules.

[Analyze library]
```

### Analyzing

Show that the library is being evaluated.

Do not present an incomplete plan as final.

### Plan ready

The user sees the summary and planned moves.

Primary action:

```text
[Review plan]
```

or, if the plan is already visible:

```text
[Apply plan]
```

### Nothing to organize

A successful empty result should be explicit:

```text
Everything is organized.

0 files need to move.
```

This is a success state, not an error or generic empty state.

### Applying

The plan is being executed.

The interface should expose progress and prevent accidental repeated execution.

### Completed

Show:

- moved count;
- skipped count;
- failed count;
- any relevant summary.

Then provide a clear path back to Library.

### Partial failure

A partially successful operation must not be represented as complete success.

The result should distinguish:

```text
Moved
Skipped
Failed
```

and provide enough information for the user to understand what remains.

### Error

An analysis error and an execution error are different states and should be described separately.

## 11. Contextual Interactions

### Organize → Library

After successful application, Library should reflect the new paths and current database state.

The user should be able to return to Library without manually restarting a scan solely to see the result, provided the existing application state can be refreshed reliably.

### Organize → Health

If organization changes affect health diagnostics, Health may be refreshed after completion. This is a secondary consistency update, not a required navigation step.

### Organize → Quick Fix

Organize must not silently invoke metadata correction. If a future workflow requires metadata preparation before organization, it must be an explicit prerequisite communicated to the user.

## 12. Navigation Rules

- Organize is a top-level destination because it represents a distinct operational goal.
- Entering Organize does not automatically move files.
- Analysis and application are separate stages.
- Leaving the screen before applying a plan must not apply it implicitly.
- A completed plan may be revisited before application.
- After application, returning to Library should show the resulting state.
- If execution fails, the user remains in a result state where failed items can be understood rather than being silently returned to Library.

## 13. Data Interaction

Organize reads:

- current library paths;
- organization rules/configuration;
- metadata required to determine destinations;
- planned move information.

Organize may update:

- filesystem paths when the user explicitly applies the plan;
- corresponding library/database state after successful filesystem operations.

Organize does not own metadata-writing logic. Metadata corrections belong to the metadata/Quick Fix workflow.

## 14. UX Rules

- **Plan before action.**
- The destination must be visible before execution.
- The reason for a proposed move must be understandable.
- No filesystem mutation occurs merely by opening or analyzing the screen.
- Apply is always explicit.
- Execution state must be visible.
- Success, partial success, skipped items, and failures are distinct outcomes.
- The interface should never make a move appear successful before it is confirmed.
- Empty results should communicate successful organization, not lack of functionality.
- The user should be able to recover from partial failure without losing the plan context.

## 15. Accessibility

- Apply and other operational controls must be keyboard reachable.
- Destructive or filesystem-changing actions require clear accessible labels.
- Status must not depend on color alone.
- Long paths need accessible full-text alternatives.
- Progress must be represented textually as well as visually.
- Focus must remain predictable during analysis and execution.
- Confirmation dialogs, when used, must identify the consequence of the action clearly.

## 16. Visual Notes

Organize should use the Studio Hi-Fi language primarily for operational feedback and the Editorial language for hierarchy.

Recommended hierarchy:

```text
ORGANIZE

Plan summary
        ↓
Current → Destination
        ↓
Reason / Status
        ↓
Execution controls
        ↓
Result
```

The table should feel precise and technical without becoming a raw filesystem console.

`StatusPill` is appropriate for states such as:

- Needs move;
- Already organized;
- Skipped;
- Moved;
- Failed.

## 17. Decision Rules

- Organize is a plan-first workflow.
- Opening Organize never changes files.
- Analysis never implies execution.
- Apply is explicit and user initiated.
- The user must be able to inspect proposed destinations before applying them.
- Metadata writing and filesystem organization remain separate responsibilities.
- Execution results must distinguish success, skip, and failure.
- A partial failure remains visible until the user understands what happened.
- After successful execution, Library becomes the primary context for the resulting collection state.
