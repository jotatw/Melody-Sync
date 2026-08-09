# Melody Sync Documentation

Documentation is organized by responsibility: project overview, architecture, design, planning, development standards, integrations, and history.

The [`README.md`](../README.md) is the public entry point. This index is the entry point for detailed project documentation.

---

## Getting Started

| Document | Purpose |
|---|---|
| [README](../README.md) | Project overview, current status, installation and quick start |
| [Documentation Index](INDEX.md) | Map of the detailed documentation |
| [Development Methodology](standards/handbook/DevelopmentMethodology.md) | Official development cycle and planning rules |

---

## Architecture

Architecture documents describe how the application is structured and why important technical decisions were made.

### Architecture Decision Records

| Document | Purpose |
|---|---|
| [ADR-0001 — Project Vision](architecture/ADR/ADR-0001-ProjectVision.md) | Project vision and scope |
| [ADR-0002 — Kotlin](architecture/ADR/ADR-0002-Python.md) | Programming-language decision record |
| [ADR-0003 — Compose Desktop](architecture/ADR/ADR-0003-PySide6.md) | Desktop UI decision record |
| [ADR-0004 — SQLite](architecture/ADR/ADR-0004-SQLite.md) | Local database decision |
| [ADR-0005 — Audio Metadata](architecture/ADR/ADR-0005-Mutagen.md) | Audio metadata decision |
| [ADR-0006 — Documentation Structure](architecture/ADR/ADR-0006-DocumentationStructure.md) | Documentation architecture |
| [ADR-0007 — clikt](architecture/ADR/ADR-0007-clikt.md) | CLI framework decision |
| [ADR-0008 — Gradle Kotlin DSL](architecture/ADR/ADR-0008-GradleKotlinDSL.md) | Build-system decision |
| [ADR-0009 — Platform Layer](architecture/ADR/ADR-0009-PlatformLayer.md) | Installation, shell and system boundary |

> ADR titles are preserved from the repository. Some historical filenames retain names from earlier project stages.

### General Architecture

| Document | Purpose | Status |
|---|---|---|
| [Core Services](architecture/core-services.md) | Current Core capability map and responsibility boundaries | Draft |
| [Music Library Domain](architecture/music-library-domain.md) | Domain model and library concepts | Active |
| [Multiplatform Portability Guide](architecture/MultiplatformPortabilityGuide.md) | Future portability considerations | Reference |
| [Security & Resilience Guide](architecture/SecurityAndResilienceGuide.md) | Defensive coding, data integrity and resilience | Active |
| [Architecture Reviews](architecture/reviews/) | Architectural reviews and evaluations | Active |
| Provider Architecture | Boundary for optional external providers | To be documented |

---

## Design

Design documentation defines how the application should look, behave and guide the user. Screen documents describe responsibilities and interaction contracts; they are not implementation specifications.

### General Design

| Document | Purpose | Status |
|---|---|---|
| [Application Design](design/app-design.md) | Application-wide navigation, hierarchy and interaction model | Target design |
| [Navigation](design/navigation.md) | Official navigation contract, context preservation and screen transitions | Defined / target navigation |
| [Design System](standards/DesignSystem.md) | Visual identity, colors, typography, shapes and reusable components | Implemented / evolving |
| [Screen Specifications](design/screens/) | One document per application screen | Active |

### Navigation State

The current implementation and approved target navigation are intentionally documented separately until the navigation consolidation is implemented.

**Current implementation:**

```text
Library
Statistics
Health
Review
Duplicates
Organize
Settings
About
```

**Target navigation:**

```text
Library
Statistics
Health
Organize
Settings
About
```

Review is intended to become a contextual review workflow connected to Health/Library. Duplicates is intended to become a contextual workflow rather than a permanent primary destination.

### Screens

| Screen | Purpose | Status |
|---|---|---|
| [Library](design/screens/library.md) | Main workspace for browsing, inspecting and curating songs | Implemented / refining |
| [Health](design/screens/health.md) | Identify library issues and guide the user to review them | Implemented / refining |
| [Statistics](design/screens/statistics.md) | Explore library data and navigate to contextual views | Implemented / refining |
| [Review](design/screens/review.md) | Current review workspace for inspecting issues and opening Quick Fix | Implemented / navigation consolidation pending |
| [Duplicates](design/screens/duplicates.md) | Current duplicate-management workspace | Implemented / navigation consolidation pending |
| [Organize](design/screens/organize.md) | Plan and apply filesystem organization | Implemented / refining |
| [Settings](design/screens/settings.md) | Application, installation and update configuration | Implemented / refining |
| [About](design/screens/about.md) | Project and application information | Implemented |

### Design Research & Feature History

| Document | Purpose | Status |
|---|---|---|
| [Application Design Research](research/app-design.md) | Design and UX research applied to the application | Reference |
| [Quick Fix HUD](research/quick-fix-hud.md) | Assisted curation interaction and implementation history | Implemented |
| [Update Channels](research/update-channels.md) | Release installer and update-channel architecture | Implemented; unattended updates remain backlog |

---

## Integrations

External tools and services are documented separately from Core responsibilities. They support specific workflows and must not become implicit requirements of the application.

| Integration | Purpose | Status |
|---|---|---|
| YouTube | Optional external source for song identification and metadata suggestions | Implemented |
| Lyrics provider | Informational lyrics lookup; never automatically written to tags | Implemented |
| Syncthing | External file synchronization between devices | Workflow / external tool |
| Playback | Lightweight local playback for inspection | Planned |

Detailed integration documents will be added or expanded when each boundary is formally defined.

---

## Planning

Planning documents describe work that is not yet fully implemented. A large feature should have a dedicated planning document before implementation begins.

| Document | Purpose | Status |
|---|---|---|
| [ROADMAP](ROADMAP.md) | Current project direction, priorities and deferred work | Active |
| [Implementation Block 01 — Navigation Context](planning/implementation-block-01-navigation-context.md) | First bounded implementation block for contextual navigation and context preservation | Approved for implementation |
| [Metadata Foundation](planning/metadata-foundation.md) | Metadata provider, diagnostics, typed write results, persistence discipline and fixtures | Implemented / refinement |
| [Metadata Formats](planning/metadata-formats.md) | Verified read/write capability matrix by format | Reference |

Planning documents should define purpose, scope, non-goals, dependencies and validation before implementation. A planning document may remain in `planning/` after implementation when it records the design and validation history of a foundation.

---

## Development Standards

| Document | Purpose | Status |
|---|---|---|
| [Development Methodology](standards/handbook/DevelopmentMethodology.md) | Official cycle: Need → Planning → Architecture → Review → Freeze → Implementation → Validation → Documentation → Approval → Maintenance | Accepted |
| Documentation Standards | Documentation conventions and templates | Active |
| Architecture Review | Architectural reasoning and lessons learned | Active |

Templates are maintained under `docs/standards/templates/`.

---

## Project History

History records what has already happened. Completed milestones should not remain in the active planning backlog.

| Document | Purpose |
|---|---|
| [History](project/History.md) | Major milestones, architectural evolution and current state |
| [Error Log](project/ErrorLog.md) | Development bugs and their resolutions |
| [Documentation Notes](project/DocumentationNotes.md) | Documentation decisions and working notes |

---

## Current Status

The README is the source for concise public project status. This section intentionally remains short so status does not become duplicated across documents.

| Area | Status |
|---|---|
| Core | Working |
| CLI | Working |
| Desktop GUI | Working |
| Library / Health / Statistics | Working |
| Organization / Duplicates | Working |
| Quick Fix | Working |
| Installation / Updates | Working |
| UX consistency | Implemented; refinement ongoing |
| Metadata foundation | Implemented; reliability refinement ongoing |
| Unattended source rebuild updates | Backlog |

For the current version and test count, see the [README](../README.md) and [ROADMAP](ROADMAP.md).

---

## Documentation Structure

```text
docs/
├── INDEX.md
├── ROADMAP.md
│
├── architecture/
│   ├── ADR/                    # Architecture Decision Records
│   ├── reviews/                # Architecture reviews
│   ├── core-services.md        # Current Core capability map
│   ├── music-library-domain.md
│   ├── MultiplatformPortabilityGuide.md
│   └── SecurityAndResilienceGuide.md
│
├── design/
│   ├── app-design.md           # Application-wide UX and navigation
│   ├── navigation.md           # Navigation contract and context rules
│   └── screens/                # One document per screen
│
├── planning/                   # Detailed plans and foundation records
│
├── project/                    # History, notes and error records
│
├── research/                   # Research and implementation history
│
└── standards/
    ├── handbook/               # Development and documentation handbooks
    ├── templates/              # Document templates
    └── DesignSystem.md         # Visual design system
```

---

## Documentation Principles

- Each document has a single responsibility.
- The README is the public project entry point; detailed information belongs in `docs/`.
- `INDEX.md` maps the documentation; it should not become a second roadmap.
- Architecture documents explain structure and decisions.
- Design documents define user behavior and interaction boundaries.
- Planning documents describe future implementation work or preserve the implementation record of a foundation.
- History records completed evolution and is not an active backlog.
- External integrations are documented separately from Core responsibilities.
- Significant features should define scope and non-goals before implementation.
- Documentation must distinguish **Current**, **Target**, and **Planned** states.
- A target design must not be presented as the current implementation until the code has been changed and validated.

---

**Last Updated**

2026-08-09 — Added the navigation contract and current/target navigation distinction while preserving the existing screen specifications.
