# ADR-0006 — Documentation Structure

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0006               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.13.0-dev            |
| Template Version | 1.0                    |
| Last Updated     | 2026-08-11             |
| Maintainer       | Melody Sync            |

---

## Summary

Organize project documentation under a single `docs/` directory, split by
**responsibility** (`architecture/`, `design/`, `planning/`, `integrations/`,
`project/`, `research/`, `standards/`), with `docs/INDEX.md` as the entry map,
`docs/ROADMAP.md` as the active planning source, ADRs for architecture
decisions, and a Documentation Standard with reusable templates.

This record formalizes the structure already in use and gives the project an
explicit, stable reference for where new documentation belongs.

---

## Context

The project accumulated documentation faster than a single flat list could
organize. Notes, decisions, research and implementation plans were mixed, and
without an explicit layout each new document could be placed anywhere. The
problems to solve were:

1. **Navigation** — a reader needs a deterministic path from an entry point
   to any document.
2. **Separation of concerns** — decisions (ADRs), plans (planning), current
   behavior (design), historical record (project) and research notes must not
   collapse into one pile.
3. **Stable naming** — documents must have a single, unambiguous home so
   references (links and textual "see ADR-…" mentions) do not break.
4. **Enforcement** — the structure needs a written standard with templates so
   new documents follow the same shape.

Constraints:

- The repository is public and must remain easy to navigate for external
  readers.
- Architecture decisions must be recorded as ADRs under `architecture/ADR/`.
- The `README.md` stays the public entry point; `docs/INDEX.md` is the map of
  the detailed documentation.

---

## Decision

Documentation lives under `docs/`, organized by responsibility:

```text
docs/
├── INDEX.md              # map of the detailed documentation (entry point)
├── ROADMAP.md            # active planning: current state, priorities, scope
├── architecture/         # architecture decisions and guides
│   └── ADR/              # Architecture Decision Records (ADR-0001…)
├── design/               # visual identity, interaction model, screens/
├── planning/             # detailed implementation plans
├── integrations/         # external provider boundaries (YouTube, lyrics)
├── project/              # historical record, notes, audits
├── research/             # research notes behind UX/design decisions
└── standards/            # design system, handbooks, templates
    ├── handbook/         # methodology and documentation standard
    └── templates/        # BaseDocument, ADR, History, Sprint, Test templates
```

Rules:

1. **`README.md`** is the public entry point. `docs/INDEX.md` is the entry
   point for detailed documentation. Every document relevant to navigation
   must be reachable through a clear reference chain from one of these.
2. **Architecture decisions** live in `docs/architecture/ADR/` as numbered
   ADRs following the ADR template.
3. **Active planning** lives in `docs/ROADMAP.md`. Detailed implementation
   plans live in `docs/planning/` and are referenced, not duplicated, from the
   roadmap.
4. **Current behavior** (screen interaction, navigation, design system) lives
   in `docs/design/`. Screen behavior documents live in `docs/design/screens/`.
5. **Historical records** live in `docs/project/` (e.g. `History.md`) and are
   kept separate from current planning so completed work stays distinguishable
   from active work.
6. **Naming is explicit and unambiguous.** When two documents share a
   basename (e.g. `app-design.md` under `design/` and `research/`), they are
   distinct by purpose and must say so; ambiguous names should cross-reference
   each other.
7. **Layers and components are named explicitly.** ADR-0009 relies on this:
   the three-layer model (domain, platform, desktop) is documented under
   `architecture/` so future infrastructure has a named home.
8. **Templates and the Documentation Standard** in `docs/standards/` define
   the shape of documents and the contribution lifecycle.

---

## Alternatives Considered

- **Flat single directory** — every document in one place. Rejected: no
  separation by responsibility, no deterministic navigation, ambiguous names
  would collide.
- **Functional structure from an early draft of the Documentation Standard**
  (`foundation/`, `implementation/`, `governance/`, `templates/`, `assets/`).
  This matched the standard's Part II example but not the actual repository,
  which had grown a responsibility-based structure. Rejected in favor of the
  implemented structure; the Documentation Standard's directory example is a
  recognized inconsistency (see [Consequences](#consequences)).
- **Module-mirroring structure** (one doc tree per Gradle module). Rejected:
  documentation is cross-cutting; architecture and design concern multiple
  modules and would be duplicated.
- **No written structure** (status quo of informal placement). Rejected:
  provided no reference for new documents and no home for the platform layer.

---

## Consequences

### Positive

- Deterministic navigation: `README` → `INDEX` → category → document.
- ADRs have a stable home and numbering, referenced reliably from the index.
- Completed work (history) is separated from active work (roadmap/planning),
  supporting the accepted development methodology.
- ADR-0009 (Platform Layer) could name a new layer with a documented home.

### Negative

- Two documents legitimately share the basename `app-design.md`
  (`design/app-design.md` = formal interaction model; `research/app-design.md`
  = research notes). The name is ambiguous to readers; the documents should
  cross-reference each other and make their different roles explicit.
- The Documentation Standard (Part II, Directory Structure) proposes a
  category layout (`foundation/`, `implementation/`, `governance/`) that does
  not match the implemented responsibility-based structure. The standard's
  directory example should be reconciled with this ADR.

### Risks

- A future contributor may place a document in the wrong category. Mitigation:
  templates, the Documentation Standard, and review during the contribution
  lifecycle.

---

## Implementation Notes

This record is a retroactive formalization: the file existed as an empty
placeholder since the initial project structure, and the responsibility-based
layout described here is the structure already in use. No structural
migration is required. Open consistency work:

- Reconcile the Documentation Standard's directory example with this ADR
  (tracked in the 2026-08 repository audit).
- Resolve the `app-design.md` naming ambiguity (cross-references).

---

## References

- ADR-0009 — Platform Layer (cites this ADR for explicit layer naming).
- `docs/INDEX.md` — the documentation map.
- `docs/ROADMAP.md` — active planning source.
- `docs/standards/handbook/DocumentationStandard.md` — standard and templates.

---

## Related Documents

- [Documentation Index](../../INDEX.md)
- [Roadmap](../../ROADMAP.md)
- [Documentation Standard](../../standards/handbook/DocumentationStandard.md)
- [Project History](../../project/History.md)
- [Repository Audit 2026-08](../../project/audit-2026-08.md)

---

## Revision History

| Version             | Date       | Description                         |
|---------------------|------------|-------------------------------------|
| v0.13.0-dev         | 2026-08-11 | Initial version (retroactive formalization of the implemented structure) |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
