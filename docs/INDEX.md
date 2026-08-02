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

---

# 💻 Development

Guidelines for contributors and future development.

| Document | Purpose |
|----------|---------|
| Documentation Standard | Documentation standards and conventions |
| Architecture Review | Architectural reasoning and lessons learned |
| Application Design Research | Design and UX best practices applied to the app |
| Journal | Project history and context |

---

# 📊 Current Status

| Item | Status |
|------|--------|
| Version | **v0.12.0-dev** |
| Language | **Kotlin** (migrated from Python) |
| Core (model + scanner) | ✅ Completed |
| CLI (`melody-sync scan` / `health` / `duplicates` / `organize` / `export` / `enrich`) | ✅ Completed |
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
| Automated Tests | 🎉 **133 Passing** (107 core + 16 CLI + 10 desktop) |
| Collapsible Sidebar | ✅ Completed |
| Cleanup (legacy Python, empty docs) | ✅ Completed |

---

# 📂 Documentation Structure

```text
docs/
│
├── INDEX.md
│
├── architecture/
│   ├── ADR/                      # Architecture Decision Records
│   ├── music-library-domain.md   # Domain models specification
│   └── reviews/                  # Architecture reviews
│
├── journal/                      # Project journal and context
│
├── project/                      # Project notes
│
├── standards/
│   ├── handbook/                 # Documentation standard
│   └── templates/                # Document templates
│
└── research/                     # Design and technology research
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

2026-08-02 — Milestones 11 & 12 + cleanup + design research