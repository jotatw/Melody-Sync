# 📚 Melody Sync Documentation

Welcome to the Melody Sync documentation.

This directory contains the project's technical documentation, development guides, planning records and project history.

---

# 🚀 Getting Started

New to the project? Start here.

| Document | Purpose |
|----------|---------|
| README | Project overview, installation and usage |
| Development Guide | How the project is organized and developed |

---

# 🏗 Architecture

Documentation describing the system design and architectural decisions.

| Document | Purpose |
|----------|---------|
| ADR-0001 | Project Vision |
| ADR-0002 | Programming Language — Kotlin |
| ADR-0003 | Desktop GUI — Compose Desktop |
| ADR-0004 | Local Database — SQLite via Exposed |
| ADR-0005 | Audio Metadata — JAudioTagger |
| ADR-0006 | Documentation Structure |
| ADR-0007 | CLI Framework — clikt |
| ADR-0008 | Build System — Gradle Kotlin DSL |
| ADR-0009 | Platform Layer (`com.melodysync.platform`: installation, shell, system) |
| Multiplatform Portability Guide | Strategy for porting to Windows/Android (KMP) |
| Security & Resilience Guide | Defensive coding, sandboxing, data integrity |

---

# 🗺 Planning

Planning is the entry point for future implementation. Large features should receive a dedicated document under `docs/planning/`.

| Document | Purpose |
|----------|---------|
| [ROADMAP](ROADMAP.md) | Current project state, priorities and deferred work |
| [Metadata Foundation](planning/metadata-foundation.md) | Foundation plan for reliable metadata read/write and Quick Fix |
| [Metadata Formats](planning/metadata-formats.md) | Verified read/write capability matrix per format |

---

# 🎨 Design

Visual identity and interaction standards.

| Document | Purpose | Status |
|----------|---------|--------|
| Design System | Hi-Fi Editorial visual identity (colors, typography, layout) | ✅ Implemented |
| [Application Design Research](research/app-design.md) | Design and UX best practices applied to the app | 📚 Draft / Active reference |
| [Quick-Fix HUD](research/quick-fix-hud.md) | Assisted curation interaction model and implementation history | ✅ Implemented |
| [Update Channels](research/update-channels.md) | Release installer and update-channel architecture | ✅ Implemented; unattended updates backlog |

---

# 💻 Development

Guidelines for contributors and future development.

| Document | Purpose | Status |
|----------|---------|--------|
| Documentation Standard | Documentation standards and conventions | ✅ Active |
| Architecture Review | Architectural reasoning and lessons learned | ✅ Active |
| [Development Methodology](standards/handbook/DevelopmentMethodology.md) | Official development cycle and planning rules | ✅ Accepted |

---

# 📜 History

Historical project evolution is kept separately from planning so completed milestones do not become an active backlog.

| Document | Purpose |
|----------|---------|
| [History](project/History.md) | Major milestones, architectural evolution and current state |
| [Error Log](project/ErrorLog.md) | Bugs found during development and how they were fixed |

---

# 📊 Current Status

| Item | Status |
|------|--------|
| Version | **v0.13.0-dev** |
| Language | **Kotlin** (migrated from Python) |
| Core (model + scanner) | ✅ Completed |
| CLI (`melody-sync scan` / `health` / `duplicates` / `organize` / `export` / `enrich` / `update` / `doctor`) | ✅ Completed |
| Database (SQLite via Exposed) | ✅ Completed |
| Library Sync (scanner → DB) | ✅ Completed |
| Desktop GUI (Compose) | ✅ Completed |
| Library Health Check | ✅ Completed |
| Review screen | ✅ Completed |
| Duplicate Detection | ✅ Completed |
| File Watcher (auto re-sync) | ✅ Completed |
| Folder Organization | ✅ Completed |
| Export (JSON/CSV) | ✅ Completed |
| YouTube Enrichment | ✅ Completed (report-only) |
| Sidebar UI + Preferences | ✅ Completed |
| Installation (Fedora script) | ✅ Completed |
| Automated Tests | 🎉 **233 Passing** |
| Collapsible Sidebar | ✅ Completed |
| Cleanup (legacy Python, empty docs) | ✅ Completed |
| Hi-Fi Editorial Design System | ✅ Completed |
| UX-1 Consistency | ✅ Completed |
| UX-2 Quick Fix | ✅ Completed |
| Security hardening (path traversal, WAL) | ✅ Completed |
| Letter grouping (opt-in) | ✅ Completed |
| Move duplicates to system trash | ✅ Completed |
| Platform layer (installation / shell / system) | 🔒 Completed — frozen |
| Release installer (download + verify + install) | ✅ Completed |
| Update channels (Stable / Beta / Nightly) | ✅ Completed |
| GitHub Actions CI + release automation | ✅ Completed |
| Unattended automatic updates | ⏳ Backlog |
| Metadata foundation | 🚧 Planned |
| Development Methodology | ✅ Accepted |

---

# 📂 Documentation Structure

```text
docs/
│
├── INDEX.md
├── ROADMAP.md
│
├── architecture/
│   ├── ADR/                       # Architecture Decision Records
│   ├── music-library-domain.md    # Domain models specification
│   ├── MultiplatformPortabilityGuide.md  # KMP porting strategy
│   ├── SecurityAndResilienceGuide.md     # Defensive coding standards
│   └── reviews/                   # Architecture reviews
│
├── planning/                      # Detailed plans for large features
│   ├── metadata-foundation.md
│   └── metadata-formats.md
│
├── project/                       # Project notes, history and error log
│   ├── DocumentationNotes.md
│   ├── History.md
│   └── ErrorLog.md
│
├── standards/
│   ├── DesignSystem.md            # Hi-Fi Editorial visual identity
│   ├── handbook/                  # Documentation and development handbooks
│   └── templates/                 # Document templates
│
└── research/                      # Design and technology research
    ├── app-design.md              # UX research applied to Melody Sync
    ├── quick-fix-hud.md           # Quick Fix design and implementation history
    └── update-channels.md         # Release/update research
```

---

# 📌 Documentation Principles

The documentation follows the same philosophy as the source code:

- Each document has a single responsibility.
- Documentation should reflect the current state of the project.
- Architectural decisions are recorded using ADRs.
- Large features receive dedicated planning documents.
- History records completed evolution; it is not an implementation backlog.
- The roadmap points to detailed planning and research instead of duplicating them.
- Documentation is part of the project's architecture and evolves with it.

---

**Last Updated**

2026-08-08 — Consolidated Planning, Design and Development references. Quick-Fix HUD and Update Channels are recorded as implemented with their remaining backlog boundaries; Development Methodology is accepted and no longer a backlog item. Current status remains v0.13.0-dev with 233 tests passing.
