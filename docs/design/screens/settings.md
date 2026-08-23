# Settings

> Interaction model for configuring Melody Sync without mixing configuration with library workflows.

## Document Information

| Item | Value |
|---|---|
| Category | Design / UX |
| Audience | Developers |
| Status | Implemented / refining |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## 1. Purpose

Settings controls how Melody Sync behaves and how the application environment is configured.

It is a configuration surface, not part of the normal library workflow.

The user should be able to change a preference, understand its current value, and know when a change takes effect without needing to understand internal implementation details.

## 2. User Question

> **"How do I configure Melody Sync to work the way I want?"**

## 3. Responsibilities

Settings is responsible for:

- application preferences;
- library configuration that affects scanning or organization;
- appearance preferences;
- update and installation controls;
- advanced configuration that should not appear in the main library workflow;
- displaying relevant installation information where useful;
- communicating whether a setting takes effect immediately or requires a restart/reload.

## 4. Non-Responsibilities

Settings must not:

- become a second Library screen;
- display routine library statistics;
- provide song-level metadata editing;
- perform Quick Fix operations;
- silently execute organization plans;
- expose internal implementation details unless they are useful for diagnostics or advanced configuration.

## 5. Entry Points

Primary entry point:

- Sidebar → Settings.

Settings should not be opened automatically as part of normal Library, Health, Statistics, or Organize workflows unless a required configuration is missing and the application explicitly explains why the user needs to configure it.

## 6. Information Architecture

Settings should be grouped by user intent rather than by implementation module.

Recommended structure:

```text
SETTINGS

Application
  General application behavior
  Startup behavior

Library
  Music directory
  Watch behavior
  Scan behavior
  Organization rules

Appearance
  Theme
  Visual preferences

Updates
  Update channel
  Check for updates
  Automatic/unattended updates
  Installation information

Advanced
  Diagnostics or advanced options
```

The exact groups should reflect settings actually supported by the application. Empty groups should not be displayed merely to complete a template.

## 7. Application

Application settings control general program behavior.

Examples may include:

- startup behavior;
- general application preferences.

Each setting must communicate its current value and, when necessary, its effect.

## 8. Library

Library settings contain configuration that affects how the application accesses or processes the music collection.

Possible settings include:

- music directory;
- file watching;
- scan behavior;
- organization rules.

Changing a library setting should not silently perform a scan or organization operation unless that behavior is explicitly defined for that setting.

If a setting changes the meaning of a future scan, the UI should make that relationship clear.

## 9. Appearance

Appearance controls the visual presentation of the application.

The existing light/dark theme behavior belongs here rather than in the primary navigation or Library toolbar.

Appearance changes should be previewed immediately when technically possible.

The visual design should remain consistent with the Studio Editorial Design System regardless of theme.

## 10. Updates

Updates are configuration and maintenance functionality and therefore belong in Settings rather than the main Library workflow.

The update area may contain:

- current installed version;
- update channel;
- check for updates;
- automatic/unattended update configuration when implemented;
- source-based rebuild/install information where applicable.

### Update interaction

The update UI must distinguish between:

```text
Already up to date
Update available
Checking
Updating / rebuilding
Completed
Error
Not installed from source
```

A check must not be presented as an installation.

A rebuild/install operation must communicate that it is changing the installed application rather than the music library.

If a restart is required, the UI must state this explicitly. Automatic relaunch after update is deferred (see ROADMAP §6); the current UI communicates that the new build takes effect on the next launch.

## 11. Installation Information

When installation information is exposed, it should answer practical questions such as:

- installed version;
- installation path;
- Java/runtime information where relevant;
- installation status.

Technical information should remain secondary to the user's task and should not dominate the Settings screen.

## 12. States

### Ready

Settings displays the current configuration values.

### Saving

If a setting requires asynchronous persistence, communicate that it is being saved.

### Saved

The updated value should be reflected immediately when possible.

### Invalid value

Explain what is invalid and how to correct it. Do not silently replace the user's value with a different value.

### Update checking

Show that the update check is in progress and prevent duplicate checks when appropriate.

### Update available

Explain what is available and provide the appropriate explicit action.

### Updating / rebuilding

Show progress and distinguish application update activity from library scanning activity.

### Update completed

Report the installed version and whether a restart is required.

### Update error

Provide a concise explanation and preserve enough context for the user to retry or diagnose the problem.

### Not installed from source

For source-based rebuild functionality, clearly explain that automatic rebuild is unavailable when the current installation is not source-based. Provide the supported alternative rather than exposing a broken action.

## 13. Contextual Interactions

Settings normally has no forward workflow into Library.

However, configuration changes may affect later Library behavior.

Examples:

```text
Settings → Music directory
       ↓
Saved
       ↓
Library uses the new directory on the next scan
```

and:

```text
Settings → Organization rules
       ↓
Saved
       ↓
Organize uses the new rules on the next analysis
```

Settings should not automatically redirect the user into those screens unless the setting explicitly requires a follow-up action and the application explains it.

## 14. Navigation Rules

- Settings is a stable top-level destination.
- Opening Settings does not trigger library analysis.
- Leaving Settings does not discard successfully saved settings.
- Unsaved changes must be handled explicitly according to the setting type: immediate save or explicit save.
- Configuration changes should not unexpectedly mutate the library.
- Update operations must not be confused with library operations.

## 15. Data Interaction

Settings reads and writes application configuration through the existing configuration mechanisms.

It may read installation information for display.

It must not directly own:

- metadata writing;
- library scanning implementation;
- file organization execution;
- duplicate detection implementation.

Those capabilities remain owned by their respective services.

## 16. UX Rules

- Group settings by user intent.
- Avoid exposing implementation structure as the information architecture.
- Show current values clearly.
- Explain side effects before the user commits to a setting that can materially change behavior.
- Do not put routine operational actions in Settings when they belong to Library, Health, or Organize.
- Keep technical installation information available without making it the visual focus.
- Separate update checking from update execution.
- Never imply that an application update is a music-library operation.

## 17. Accessibility

- Every control needs a meaningful accessible label.
- Group headings must establish a logical reading order.
- Switches, selectors, buttons, and text fields must be keyboard reachable.
- Current values must not rely only on color or position.
- Validation errors must be associated with the relevant control.
- Progress and update states require textual feedback.
- Focus should remain predictable after saving or changing a setting.

## 18. Visual Notes

Settings should be calmer and denser than Library.

The Studio Editorial language should be present through:

- strong section hierarchy;
- restrained dividers;
- technical typography for paths/versions;
- semantic status indicators;
- controlled spacing.

Avoid turning every setting into a large decorative card. Group related controls into clear sections and use cards only where they materially improve grouping or state visibility.

Recommended hierarchy:

```text
SETTINGS

Application
  controls

Library
  controls

Appearance
  controls

Updates
  controls + status

Advanced
  controls
```

## 19. Decision Rules

- Settings configures behavior; it does not perform the normal library workflow.
- Repository/update configuration belongs in Settings rather than Library.
- Update functionality belongs in Settings and must remain visually distinct from library scanning.
- A configuration change does not implicitly perform a destructive or expensive operation.
- Source-based rebuild must explain when it is unavailable.
- Technical installation details are secondary information.
- Settings groups should exist only when they contain supported functionality.
- New settings must be placed according to user intent, not according to the package/class where they are implemented.
