# Documentation Templates

> Standard templates used to create and maintain Melody Sync documentation.

---

## Document Information

| Item             | Value        |
|------------------|--------------|
| Document ID      | TPL-000      |
| Category         | Template     |
| Audience         | Contributors |
| Status           | Approved     |
| Template Version | 1.0          |
| Last Updated     | YYYY-MM-DD   |
| Maintainer       | jotatw       |

---

## Purpose

This directory contains the official templates used to create and maintain every documentation file in the Melody Sync project.

The template system provides:

- A consistent documentation structure.
- Standardized document metadata.
- Reduced duplication and maintenance effort.
- A unified writing style across the project.

Every document inside the `docs/` directory should be based on one of these templates.

---

# Documentation Philosophy

The documentation follows the same principles adopted by the source code:

- Single Responsibility
- Consistency
- Simplicity
- Maintainability
- Incremental evolution
- Documentation reflects the current state of the project

Each document should have a single, well-defined purpose.

---

# Available Templates

| Template         | Category     | Purpose              |
|------------------|--------------|----------------------|
| BaseDocument     | Base         | Parent template      |
| HandbookTemplate | Handbook     | Development guides   |
| ADRTemplate      | Architecture | ADR documents        |
| SprintTemplate   | Project      | Sprint documentation |
| TestTemplate     | Testing      | Test documentation   |
| HistoryTemplate  | History      | Project history      |


---

## Template Naming Convention

Templates follow a standardized naming convention.

| Prefix | Category                      |
|--------|-------------------------------|
| TPL    | Documentation Templates       |
| HB     | Handbook Documents            |
| ADR    | Architecture Decision Records |
| PRJ    | Project Management            |
| HIS    | Project History               |
| TEST   | Testing Documentation         |

---

# Template Hierarchy

```text
BaseDocument
│
├── HandbookTemplate
│
├── ADRTemplate
│
├── SprintTemplate
│
├── TestTemplate
│
└── HistoryTemplate
```

```text
BaseDocument
│
├── HandbookTemplate
│      ├── DevelopmentGuide
│      ├── CodingStandards
│      ├── TestingGuidelines
│      └── DocumentationStandard
│
├── ADRTemplate
│      ├── ADR-0001
│      └── ADR-0002
│
├── SprintTemplate
│      ├── SprintBoard
│      └── SprintJournal
│
├── TestTemplate
│      ├── TEST_PLAN
│      └── TEST_REPORT
│
└── HistoryTemplate
       └── ProjectHistory
```

The `BaseDocument` defines the common structure shared by every document.

Each specialized template extends this base according to its specific purpose.

---

# Template Versions

| Template         | Version |
|------------------|---------|
| BaseDocument     | 1.0     |
| HandbookTemplate | 1.0     |
| ADRTemplate      | 1.0     |
| SprintTemplate   | 1.0     |
| TestTemplate     | 1.0     |
| HistoryTemplate  | 1.0     |

---

## How to Use

1. Select the appropriate template.
2. Copy it into the destination directory.
3. Rename the file.
4. Replace all placeholders.
5. Remove sections that are not applicable.
6. Update the revision history.
7. Review the document before publishing.
8. Submit the document for review.

---

## Placeholder Convention

Templates use placeholders enclosed by double curly braces.

Supported placeholders:

```text
{{TITLE}}
{{DESCRIPTION}}
{{DOCUMENT_ID}}
{{CATEGORY}}
{{AUDIENCE}}
{{STATUS}}
{{PROJECT_VERSION}}
{{TEMPLATE_VERSION}}
{{DATE}}
{{AUTHOR}}
```

Every placeholder must be replaced before publishing the document.

These placeholders are part of the official Documentation Standard.

Do not rename or remove placeholders unless the template version changes.

---

## Supported Status Values

- Draft
- In Review
- Approved
- Deprecated

---

## Supported Categories

- Template
- Handbook
- Architecture
- Project
- History
- Testing

---

# Template Status

Documentation templates are independent from the Melody Sync version.

Instead, they follow their own versioning.

Example:

| Template       | Version |
|----------------|---------|
| BaseDocument   | 1.0     |
| SprintTemplate | 1.0     |

---

## Related Documents

- DocumentationStandard.md *(to be created)*
- INDEX.md

---

# Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | YYYY-MM-DD | Initial template system |

Record only significant document revisions.

Do not register formatting or typo corrections.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**