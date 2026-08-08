# Development Methodology

> Official development cycle for planning, implementing and maintaining Melody Sync.

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | standards/handbook/DevelopmentMethodology |
| Category         | Handbook |
| Audience         | Developers |
| Status           | Accepted |
| Project Version  | v0.13.0-dev |
| Template Version | 1.0 |
| Last Updated     | 2026-08-08 |
| Maintainer       | Melody Sync |

---

## Purpose

This document defines the official development cycle of Melody Sync.

It provides a common sequence for turning a real need into an implemented,
validated and documented project change. The methodology also establishes the
foundation for the planning rules recorded in `docs/ROADMAP.md`.

The methodology is intentionally incremental: a stage should provide enough
clarity for the next stage without introducing unnecessary process.

---

## Scope

This methodology applies to meaningful project changes, including features,
architectural changes, substantial UX work and changes that require new
planning or documentation.

Small maintenance changes may use the same principles without requiring a
separate document for every stage.

This document does not define implementation-specific coding rules, test
framework details or release procedures. Those belong to their respective
standards and project documents.

---

## Design Principles

- **Need before structure** — introduce work because a real need exists.
- **Planning before implementation** — clarify the intended change before writing code.
- **Architecture before commitment** — resolve structural responsibilities and constraints early.
- **Review before freeze** — inspect the proposed solution before treating its structure as stable.
- **Incremental growth** — solve the current problem without speculative expansion.
- **Validation as part of development** — tests and verification are part of the implementation cycle, not an afterthought.
- **Documentation follows reality** — documentation must reflect the implemented state.
- **Approval closes the change** — a change is complete only after its result is reviewed and accepted.
- **Maintenance remains part of the lifecycle** — completed work can continue to evolve when a new need appears.

---

## Guidelines

### Official Development Cycle

The Melody Sync development cycle is:

```text
Need
  ↓
Planning
  ↓
Architecture
  ↓
Review
  ↓
Freeze
  ↓
Implementation
  ↓
Validation
  ↓
Documentation
  ↓
Approval
  ↓
Maintenance
```

Each stage has a specific responsibility.

### 1. Need

Identify the real problem, requirement or opportunity that justifies the change.

The need should be concrete enough to explain why the project should spend
complexity, code or documentation effort on the change.

### 2. Planning

Define what should change, what is intentionally excluded, and where the
work belongs in the project roadmap.

Planning should establish the smallest coherent scope. Large features should
have a dedicated planning document under `docs/planning/` when additional
detail is necessary.

### 3. Architecture

Determine responsibilities, boundaries, dependencies and structural decisions
before implementation when the change affects architecture.

Existing ADRs and architectural constraints must be considered before creating
new structures.

### 4. Review

Review the proposed plan and architecture for consistency, scope, risks and
alignment with the current project state.

Review may identify missing work, unnecessary complexity or a need to revise
the plan before implementation begins.

### 5. Freeze

Once the plan and architecture are sufficiently reviewed, freeze the intended
structure for implementation.

A freeze does not mean the project can never change. It means implementation
should proceed against a known structure; changes discovered later should be
explicitly reviewed rather than introduced silently.

### 6. Implementation

Implement the approved scope while preserving the responsibilities and
boundaries established during planning and architecture.

Avoid adding unrelated improvements merely because they become convenient
during implementation.

### 7. Validation

Verify that the implementation works and that existing behavior remains
correct.

Validation may include automated tests, integration tests, manual checks,
fixtures or other evidence appropriate to the change.

A feature is not considered complete merely because its code compiles.

### 8. Documentation

Update documentation so it describes the resulting project state.

This includes the relevant index, roadmap, research documents, ADRs,
handbooks, history or planning documents when applicable.

Documentation should not preserve obsolete implementation assumptions simply
because they existed in an earlier plan.

### 9. Approval

Review the completed implementation and its documentation against the agreed
scope.

Approval confirms that the change is ready to become part of the maintained
project state.

### 10. Maintenance

After approval, the change becomes part of the project and is maintained as
new needs appear.

Maintenance may produce a new Need and therefore begin another cycle.

---

## Planning Rules

The methodology establishes the following practical rules for project
planning:

1. A real need precedes a new feature or structural document.
2. A large feature gets a dedicated planning document under `docs/planning/`.
3. Architecture is reviewed before an architectural change is frozen.
4. Implementation should stay within the approved scope.
5. Validation must provide evidence appropriate to the change.
6. Documentation is updated after the implementation reflects the new state.
7. Completed work is recorded in project history when it represents a meaningful milestone.
8. The roadmap records the current status and points to detailed planning or research documents instead of duplicating them.
9. Backlog items remain explicitly marked as future work until they are implemented.

---

## References

- `docs/ROADMAP.md`
- `docs/INDEX.md`
- `docs/project/DocumentationNotes.md`
- `docs/standards/handbook/DocumentationStandard.md`

---

## Related Documents

- `docs/research/app-design.md`
- `docs/project/History.md`
- `docs/planning/metadata-foundation.md`

---

## Revision History

| Version      | Date       | Description |
|--------------|------------|-------------|
| v0.13.0-dev  | 2026-08-08 | Initial official development methodology |

Record only meaningful document revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
