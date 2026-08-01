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
| Development Guide | Development workflow |
| Coding Standards | Coding conventions and project standards |
| Testing Guidelines | Testing methodology and best practices |

---

# 🧪 Testing

Documentation related to the testing process.

| Document | Purpose |
|----------|---------|
| Test Plan | Testing strategy |
| Test Report | Current testing results |

---

# 📋 Project Management

Project planning and development history.

| Document | Purpose |
|----------|---------|
| Sprint Board | Sprint planning and backlog |
| Sprint Journal | Detailed record of every sprint |

---

# 📖 Project History

High-level project evolution.

| Document | Purpose |
|----------|---------|
| Project History | Timeline and major milestones |

---

# 📊 Current Status

| Item | Status |
|------|--------|
| Version | **v0.10.0-dev** |
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
| Automated Tests | 🎉 **129 Passing** (107 core + 16 CLI + 6 desktop) |

---

# 📂 Documentation Structure

```text
docs/
│
├── INDEX.md
│
├── ADR/
│
├── handbook/
│   ├── DevelopmentGuide.md
│   ├── CodingStandards.md
│   └── TestingGuidelines.md
│
├── history/
│   └── ProjectHistory.md
│
├── project/
│   ├── SprintBoard.md
│   └── SprintJournal.md
│
└── testing/
    ├── TEST_PLAN.md
    └── TEST_REPORT.md
```

---

# 📌 Documentation Principles

The documentation follows the same philosophy as the source code:

- Each document has a single responsibility.
- Documentation should reflect the current state of the project.
- Architectural decisions are recorded using ADRs.
- Sprint Journal records the development process.
- Handbook contains permanent development guidelines.
- Project History summarizes the evolution of the project.

---

**Last Updated**

2026-08-01 — Milestone 10 (YouTube Enrichment) completed