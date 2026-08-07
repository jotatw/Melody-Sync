# 📚 Melody Sync Documentation

Welcome to the Melody Sync documentation.

This directory contains the project's technical documentation, development guides and project management records.

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

# 🎨 Design

Visual identity and interaction standards.

| Document | Purpose |
|----------|---------|
| Design System | Hi-Fi Editorial visual identity (colors, typography, layout) |
| Application Design Research | Design and UX best practices applied to the app |
| Quick-Fix HUD | Assisted curation vision (registered, not implemented) |
| Update Channels | Release installer implemented; channel UI registered |

---

# 💻 Development

Guidelines for contributors and future development.

| Document | Purpose |
|----------|---------|
| Documentation Standard | Documentation standards and conventions |
| Architecture Review | Architectural reasoning and lessons learned |

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
| Duplicate Detection | ✅ Completed |
| File Watcher (auto re-sync) | ✅ Completed |
| Folder Organization | ✅ Completed |
| Export (JSON/CSV) | ✅ Completed |
| YouTube Enrichment | ✅ Completed (report-only) |
| Sidebar UI + Preferences | ✅ Completed |
| Installation (Fedora script) | ✅ Completed |
| Automated Tests | 🎉 **207 Passing** (176 core + 17 CLI + 14 desktop) |
| Collapsible Sidebar | ✅ Completed |
| Cleanup (legacy Python, empty docs) | ✅ Completed |
| Hi-Fi Editorial Design System | ✅ Completed (subtle first pass) |
| Security hardening (path traversal, WAL) | ✅ Completed |
| Letter grouping (opt-in) | ✅ Completed |
| Move duplicates to system trash | ✅ Completed |
| Platform layer (installation / shell / system) | ✅ Completed — frozen |
| Release installer (download + verify + install) | ✅ Completed |
| GitHub Actions CI + release automation | ✅ Completed |

---

# 📂 Documentation Structure

```text
docs/
│
├── INDEX.md
│
├── architecture/
│   ├── ADR/                       # Architecture Decision Records
│   ├── music-library-domain.md    # Domain models specification
│   ├── MultiplatformPortabilityGuide.md  # KMP porting strategy
│   ├── SecurityAndResilienceGuide.md     # Defensive coding standards
│   └── reviews/                   # Architecture reviews
│
├── project/                       # Project notes
│
├── standards/
│   ├── DesignSystem.md            # Hi-Fi Editorial visual identity
│   ├── handbook/                  # Documentation standard
│   └── templates/                 # Document templates
│
└── research/                      # Design and technology research
    ├── app-design.md              # UX research applied to Melody Sync
    ├── quick-fix-hud.md           # Curation vision (deferred)
    └── update-channels.md         # Release installer vision (deferred)
```

---

# 📌 Documentation Principles

The documentation follows the same philosophy as the source code:

- Each document has a single responsibility.
- Documentation should reflect the current state of the project.
- Architectural decisions are recorded using ADRs.
- Documentation is part of the project's architecture and evolves with it.

---

**Last Updated**

2026-08-07 — Release installer (download + verify sha256 + install with
launcher); GitHub Actions CI + release automation; CLI/GUI release-mode
update; 207 tests passing.