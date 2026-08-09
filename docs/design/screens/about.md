# About

> Interaction model for application identity, version information, project context, and supporting links.

## Document Information

| Item | Value |
|---|---|
| Category | Design / UX |
| Audience | Developers |
| Status | Defined |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

## 1. Purpose

About provides information about Melody Sync as an application and project.

It is informational rather than operational. The screen should help the user identify the application, understand the installed version, and access relevant project information without becoming another settings or diagnostic screen.

## 2. User Question

> **"What is this application, which version am I running, and where can I learn more?"**

## 3. Responsibilities

About is responsible for:

- application name and identity;
- current installed version;
- project or copyright information where appropriate;
- links to relevant project resources;
- concise attribution/license information where required;
- identifying the project without overwhelming the user with technical implementation details.

## 4. Non-Responsibilities

About must not:

- contain application configuration;
- perform updates;
- diagnose the installation;
- provide routine library statistics;
- expose the full developer/debugging environment;
- become a changelog browser unless a dedicated, intentionally scoped history view is introduced later.

## 5. Entry Points

Primary entry point:

- Sidebar → About.

About should not be used as a required intermediate step in any normal application workflow.

## 6. Information Hierarchy

Recommended order:

```text
MELODY SYNC

Short project description

Version

Project links

Credits / license
```

The application identity should be the most prominent element. Technical details remain secondary.

## 7. Version Information

The displayed version should come from the application's canonical version source rather than being hardcoded independently in the UI.

The version shown in About should therefore remain consistent with the version reported by the CLI and installation information.

Example:

```text
Melody Sync
Personal music curation workstation

v0.13.0-dev
```

If additional build information is useful, it should be visually secondary to the user-facing version.

## 8. Project Links

Links may include resources that are genuinely useful to users, such as:

- project repository;
- documentation;
- issue tracker or support resource when appropriate.

Links should open their intended destination without altering application state.

## 9. States

### Ready

Display the normal About content.

### Link unavailable

If an external link cannot be opened, communicate the failure without treating it as an application error.

The user should still be able to view the local About information.

## 10. Contextual Interactions

About normally has no interactions with Library, Health, Statistics, Organize, or Quick Fix.

The only expected application-level interaction is access to project resources and version information.

## 11. Navigation Rules

- About is a stable informational destination.
- Opening About does not trigger scans, health checks, statistics calculations, organization analysis, or update checks.
- Leaving About has no effect on application state.
- External links must not modify application data.

## 12. Data Interaction

About reads:

- canonical application version;
- static project identity information;
- static attribution/license information where required.

About must not own or modify application configuration, library data, metadata, or filesystem state.

## 13. UX Rules

- Keep the screen concise.
- Make the application identity immediately recognizable.
- Keep version information easy to find.
- Do not duplicate Settings or Doctor functionality.
- Do not turn About into a technical dashboard.
- External links should be explicit and understandable.
- Information that changes frequently belongs in the appropriate project documentation or update/history mechanism rather than being copied into About.

## 14. Accessibility

- All external links must have descriptive labels.
- Version information must be selectable/readable by assistive technologies.
- Keyboard focus must be visible and follow the visual order.
- The application name and purpose must not depend on typography or color alone.

## 15. Visual Notes

About is the quietest screen in the application.

It should use the Studio Editorial language through:

- strong application typography;
- restrained composition;
- generous but intentional whitespace;
- subtle dividers;
- technical typography for version/build information.

It should not attempt to demonstrate every visual feature of the design system.

## 16. Decision Rules

- About is informational only.
- Version comes from the canonical application version source.
- Updates belong to Settings, not About.
- Diagnostics belong to Health/Doctor, not About.
- Library data does not belong on About.
- External links are navigation out of the application, not application workflows.
