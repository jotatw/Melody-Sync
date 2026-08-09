# Melody Sync Documentation

Documentation is organized by responsibility: project overview, architecture, design, planning, development standards, and history.

The [`README.md`](../README.md) is the public entry point. This index is the entry point for the detailed project documentation.

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
| [ADR-0001](architecture/ADR/ADR-0001.md) | Project Vision |
| [ADR-0002](architecture/ADR/ADR-0002.md) | Programming Language — Kotlin |
| [ADR-0003](architecture/ADR/ADR-0003.md) | Desktop GUI — Compose Desktop |
| [ADR-0004](architecture/ADR/ADR-0004.md) | Local Database — SQLite via Exposed |
| [ADR-0005](architecture/ADR/ADR-0005.md) | Audio Metadata — JAudioTagger |
| [ADR-0006](architecture/ADR/ADR-0006.md) | Documentation Structure |
| [ADR-0007](architecture/ADR/ADR-0007.md) | CLI Framework — clikt |
| [ADR-0008](architecture/ADR/ADR-0008.md) | Build System — Gradle Kotlin DSL |
| [ADR-0009](architecture/ADR/ADR-0009.md) | Platform Layer — installation, shell and system |

### General Architecture

| Document | Purpose |
|---|---|
| [Music Library Domain](architecture/music-library-domain.md) | Domain model and library concepts |
| [Multiplatform Portability Guide](architecture/MultiplatformPortabilityGuide.md) | Future portability considerations |
| [Security & Resilience Guide](architecture/SecurityAndResilienceGuide.md) | Defensive coding, data integrity and resilience |
| [Architecture Reviews](architecture/reviews/) | Architectural reviews and evaluations |

---

## Design

Design documentation defines how the application should look, behave and guide the user. Screen documents describe responsibilities and interaction contracts; they are not implementation specifications.

### General Design

| Document | Purpose | Status |
|---|---|---|
| [Application Design](design/app-design.md) | Application-wide navigation, hierarchy and interaction model | Defined |
| [Design System](standards/DesignSystem.md) | Visual identity, colors, typography, shapes and reusable components | Implemented / evolving |
| [Screen Specifications](design/screens/) | One document per application screen | Active |

### Screens

| Screen | Purpose |
|---|---|
| [Library](design/screens/library.md) | Main workspace for browsing, inspecting and curating songs |
| [Health](design/screens/health.md) | Identify library issues and guide the user to review them |
| [Statistics](design/screens/statistics.md) | Explore library data and navigate to contextual views |
| [Organize](design/screens/organize.md) | Plan and apply filesystem organization |
| [Settings](design/screens/settings.md) | Application, installation and update configuration |
| [About](design/screens/about.md) | Project and application information |

### Design Research & Feature History

| Document | Purpose | Status |
|---|---|---|
| [Application Design Research](research/app-design.md) | Design and UX research applied to the application | Reference |
| [Quick Fix HUD](research/quick-fix-hud.md) | Assisted curation interaction and implementation history | Implemented |
| [Update Channels](research/update-channels.md) | Release installer and update-channel architecture | Implemented; unattended updates remain backlog |

---

## Integrations

External tools and services are documented separately from the Core. They support specific workflows and must not become implicit requirements of the application.

| Integration | Purpose | Status |
|---|---|---|
| YouTube | Optional external source for song identification and metadata suggestions | Implemented |
| Lyrics provider | Informational lyrics lookup; never automatically written to tags | Implemented |
| Syncthing | External file synchronization between devices | Workflow / external tool |
| Playback | Lightweight local playback for inspection | Planned |

Detailed integration documents will be added or expanded as each boundary is defined.

---

## Planning

Planning documents describe work that is not yet fully implemented. A large feature should have a dedicated planning document before implementation begins.

| Document | Purpose | Status |
|---|---|---|
| [ROADMAP](ROADMAP.md) | Current project direction, priorities and deferred work | Active |
| [Metadata Foundation](planning/metadata-foundation.md) | Reliable metadata read/write foundation and Quick Fix support | Planned |
| [Metadata Formats](planning/metadata-formats.md) | Verified read/write capability matrix by format | Reference / Planning |

Planning documents should define purpose, scope, non-goals, dependencies and validation before implementation.

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

The README is the source for the concise public project status. This section intentionally remains short so status does not become duplicated across documents.

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
| Metadata foundation | Planned |
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
│   ├── music-library-domain.md
│   ├── MultiplatformPortabilityGuide.md
│   └── SecurityAndResilienceGuide.md
│
├── design/
│   ├── app-design.md           # Application-wide UX and navigation
│   └── screens/                # One document per screen
│
├── planning/                   # Detailed plans for future work
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
- Planning documents describe future implementation work.
- History records completed evolution and is not an active backlog.
- External integrations are documented separately from Core responsibilities.
- Significant features should define scope and non-goals before implementation.
- Documentation should reflect the current state and should not silently describe planned behavior as implemented.

---

**Last Updated**

2026-08-09 — Reorganized the documentation index around project overview, architecture, design, integrations, planning, development standards and history. Reduced duplicated status information and established screen specifications as the design reference for application behavior.
