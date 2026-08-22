# Melody Sync Documentation

Documentation is organized by responsibility: project overview, architecture, design, planning, development standards, integrations, and history.

The [`README.md`](../README.md) is the public entry point. This index is the entry point for detailed project documentation.

---

## Getting Started

| Document | Purpose |
|---|---|
| [README](../README.md) | Project overview, current status, installation and quick start |
| [Documentation Index](INDEX.md) | Map of detailed documentation |
| [Development Methodology](standards/handbook/DevelopmentMethodology.md) | Official development cycle and planning rules |

---

## Architecture

| Document | Purpose | Status |
|---|---|---|
| [ADR-0001 — Project Vision](architecture/ADR/ADR-0001-ProjectVision.md) | Project vision and scope | Reference |
| [ADR-0002 — Kotlin](architecture/ADR/ADR-0002-Python.md) | Programming-language decision | Reference |
| [ADR-0003 — Compose Desktop](architecture/ADR/ADR-0003-PySide6.md) | Desktop UI decision | Reference |
| [ADR-0004 — SQLite](architecture/ADR/ADR-0004-SQLite.md) | Local database decision | Reference |
| [ADR-0005 — Audio Metadata](architecture/ADR/ADR-0005-Mutagen.md) | Audio metadata decision | Reference |
| [ADR-0006 — Documentation Structure](architecture/ADR/ADR-0006-DocumentationStructure.md) | Documentation architecture | Reference |
| [ADR-0007 — clikt](architecture/ADR/ADR-0007-clikt.md) | CLI framework decision | Reference |
| [ADR-0008 — Gradle Kotlin DSL](architecture/ADR/ADR-0008-GradleKotlinDSL.md) | Build-system decision | Reference |
| [ADR-0009 — Platform Layer](architecture/ADR/ADR-0009-PlatformLayer.md) | Installation, shell and system boundary | Reference |
| [Core Services](architecture/core-services.md) | Current Core capability map and responsibility boundaries | Draft |
| [Technology & Modules](architecture/technology.md) | Technical stack, module map, validation status | Reference |
| [Music Library Domain](architecture/music-library-domain.md) | Domain model and library concepts | Active |
| [Multiplatform Portability Guide](architecture/MultiplatformPortabilityGuide.md) | Future portability considerations | Reference |
| [Security & Resilience Guide](architecture/SecurityAndResilienceGuide.md) | Defensive coding, data integrity and resilience | Active |

### Architecture Reviews

| Document | Purpose | Date |
|---|---|---|
| [Architecture Review](architecture/reviews/ArchitectureReview.md) | Consolidated architecture review | Reference |
| [Navigation & Metadata Consistency Review](architecture/reviews/navigation-metadata-consistency-2026-08.md) | Cross-check of navigation and metadata consistency | 2026-08 |

---

## Design

### General Design

| Document | Purpose | Status |
|---|---|---|
| [Application Design](design/app-design.md) | Application-wide UX and interaction model | Target design |
| [Navigation](design/navigation.md) | Navigation contract, context preservation and screen transitions | Defined / target |
| [Design System](standards/DesignSystem.md) | Visual identity and reusable components | Implemented / evolving |
| [Branding Assets](assets/branding/BRANDING.md) | Logo direction and visual assets | Draft (MS monogram selected) |

### Screens

| Screen | Purpose | Status |
|---|---|---|
| [Screen Documents Index](design/screens/README.md) | Overview of the screen interaction documents | Reference |
| [Library](design/screens/library.md) | Browse, inspect and curate songs | Implemented / refining |
| [Health](design/screens/health.md) | Identify library issues and guide review | Implemented / refining |
| [Statistics](design/screens/statistics.md) | Explore library data and navigate to contextual views | Implemented / refining |
| [Review](design/screens/review.md) | Issue-review workflow, reached contextually through Health | Implemented / contextual via Health |
| [Duplicates](design/screens/duplicates.md) | Duplicate-management workflow, reached contextually through Health | Implemented / contextual via Health |
| [Organize](design/screens/organize.md) | Plan and apply filesystem organization | Implemented / refining |
| [Settings](design/screens/settings.md) | Application, installation and update configuration | Implemented / refining |
| [About](design/screens/about.md) | Project and application information | Implemented |

### Target Navigation

```text
Library
Statistics
Health
Organize
────────────
Settings
About
```

`Review` and `Duplicates` are removed from the permanent primary navigation; they remain functional contexts reached through Health (Health → issue context → Library/Quick Fix; Health → duplicate groups).

### Design Research

| Document | Purpose | Status |
|---|---|---|
| [Application Design Research](research/app-design.md) | Design and UX research | Reference |
| [Quick Fix HUD](research/quick-fix-hud.md) | Assisted curation interaction and implementation history | Implemented |
| [Update Channels](research/update-channels.md) | Installer and update-channel architecture | Implemented / backlog remains |

---

## Integrations

External services remain separate from Core responsibilities and support specific workflows.

| Document | Purpose | Status |
|---|---|---|
| [YouTube Identification](integrations/youtube-identification.md) | Lightweight song identification and discovery | Defined / target boundary |
| [Metadata Providers](integrations/metadata-providers.md) | Replaceable structured metadata suggestion boundary | Defined / target |
| [Lyrics Policy](integrations/lyrics-policy.md) | Informational lyrics lookup; never automatically written to tags | Defined / current policy |
| Syncthing | External file synchronization between devices | Workflow / external tool |
| Playback | Lightweight local playback for inspection | Planned |

---

## Planning

| Document | Purpose | Status |
|---|---|---|
| [Product Roadmap](project/product-roadmap.md) | Complete objective map: consolidation, UX refinement, workflow refinement, maintenance, future expansion and deliberate non-goals | Active — current stage: Product Validation |
| [ROADMAP](ROADMAP.md) | Current project direction, priorities and near-term implementation state | Active |
| [Implementation Block 01 — Navigation Context](planning/implementation-block-01-navigation-context.md) | Bounded contextual-navigation implementation | Implemented / validation ongoing |
| [Product Validation](planning/product-validation.md) | End-to-end workflow validation (automated + guided GUI) with prioritized findings | In execution |
| [Product Validation Report](planning/product-validation-report.md) | Automated results and classified findings; GUI checklist | Findings registered |
| [Product Hardening](planning/product-hardening.md) | Robustness review: fail comprehensibly and safely on error/edge cases | In progress |
| [Product Hardening Report](planning/product-hardening-report.md) | Probe results — no critical findings | Probes complete |
| [Metadata Workflow](planning/metadata-workflow.md) | End-to-end identification, enrichment, review and explicit metadata application | Defined / target workflow |
| [Metadata Foundation](planning/metadata-foundation.md) | Metadata providers, diagnostics, safe writes, persistence and fixtures | Implemented / consolidated (read-path validated) |
| [Metadata Formats](planning/metadata-formats.md) | Verified read/write capability matrix | Reference |
| [Metadata Identification & Enrichment](planning/metadata-identification-and-enrichment.md) | Identification and enrichment boundaries and flow | Reference |

Planning documents define purpose, scope, non-goals, dependencies and validation before implementation. Foundation records may remain here after implementation. The Product Roadmap provides the broader objective map; `ROADMAP.md` tracks current priorities and near-term implementation state.

---

## Development Standards

| Document | Purpose | Status |
|---|---|---|
| [Development Methodology](standards/handbook/DevelopmentMethodology.md) | Need → Planning → Architecture → Review → Freeze → Implementation → Validation → Documentation → Approval → Maintenance | Accepted |
| Documentation Standards | Documentation conventions and templates | Active |
| Design System | Visual design rules | Active / evolving |

---

## Project History

| Document | Purpose |
|---|---|
| [History](project/History.md) | Milestones and architectural evolution |
| [Error Log](project/ErrorLog.md) | Development bugs and resolutions |
| [Documentation Notes](project/DocumentationNotes.md) | Documentation decisions and working notes |
| [Repository Audit — 2026-08](project/audit-2026-08.md) | Historical audit of repository security, structure, documentation and code state | Completed snapshot |

---

## Current Status

| Area | Status |
|---|---|
| Core | Working |
| CLI | Working |
| Desktop GUI | Working |
| Library / Health / Statistics | Working |
| Organization / Duplicates | Working |
| Quick Fix | Working |
| Installation / Updates | Working |
| UX consistency | Completed (Consolidation & UX Refinement cycle) |
| Metadata foundation | Implemented; read-path validated (2026-08) |
| Metadata workflow | Defined; provider selection/enrichment remain future work |

---

## Documentation Principles

- Each document has a single responsibility.
- README is the public entry point; detailed information belongs in `docs/`.
- Architecture documents explain structure and decisions.
- Design documents define user behavior and interaction boundaries.
- Planning documents describe future work or preserve foundation decisions.
- History records completed evolution, not active backlog.
- External integrations remain separate from Core responsibilities.
- Significant features define scope and non-goals before implementation.
- Documentation distinguishes **Current**, **Target**, and **Planned** states.
- Target behavior is not presented as implemented until code changes are validated.

---

**Last Updated:** 2026-08-21
