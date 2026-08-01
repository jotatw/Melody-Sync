# ADR-0001 — Project Vision

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0001               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync is a **personal tool** to organize and analyze a local music library on Linux.

It scans libraries where audio files and related media (images, subtitles, text) are mixed together, enriches metadata — including covers and lyrics — using the **YouTube API** as the identification source, and checks the **health** of the library (files missing metadata, orphan files, unexpected formats).

It is personal by nature, but it may be made publicly available in the future if it proves useful. It is also a vehicle for learning software engineering through good architecture and documentation.

---

## Context

### Origin

The developer listens to and downloads music from **YouTube**. The very first idea was a simple script that would scan the music library, identify each song through the **YouTube API**, and fetch its metadata from that source.

The project began as a personal necessity, not as an ambition to build a large application.

### Current State of the Library

The library is currently disorganized. Audio files and related files are mixed together in the same folders:

- **Audio:** `.mp3`, `.flac`, `.mp4`
- **Media:** `.png`
- **Subtitles:** `.vtt`
- **Text:** `.txt`

There is no consistent structure and no reliable source of truth about what each folder contains.

### Frustration with Existing Linux Tools

Several existing tools were tried, but each solves only part of the problem:

| Tool | Strength | Limitation |
|------|----------|------------|
| MusicBrainz Picard | Audio fingerprinting and tag identification | Focused on tagging; confusing for beginners |
| Kid3 | Complete tag editor | Technical; no library organization workflow |
| LRCGET | Fetches lyrics | Lyrics only |
| Strawberry | Music player | Features scattered; not an organizer |

The core problem: **features are scattered across different tools**. None of them offers a cohesive workflow for cleaning up and organizing a personal library from start to finish.

### Nature of the Project

Melody Sync is **personal**. It is not intended to serve a large audience from the start. However, if it works well and reaches a usable state, there is a clear intention to **make it publicly available** in the future.

The project also values planning and documentation: understanding *why* each decision is made is as important as making it.

---

## Decision

### Vision Statement

> Melody Sync is a personal tool that organizes, analyzes and explores a local music library on Linux. It brings together in one place what scattered tools do separately: scanning mixed files, enriching metadata from the YouTube API, and checking the health of the library — built with clean architecture and documentation from the start.

### Who

- **Primary user:** one person (the developer).
- **Future audience:** the project may be released publicly if it works well, but it will never be driven by commercial goals.

### What

Melody Sync will:

1. **Scan and classify** the library, dealing with mixed files (`.mp3`, `.flac`, `.mp4`, `.png`, `.vtt`, `.txt`).
2. **Enrich metadata** — title, artist, album, cover art and lyrics — using the **YouTube API** to identify songs and fetch their metadata.
3. **Check library health** — detect files missing metadata, orphan files, and unexpected formats.
4. **Provide a cohesive experience** — a single tool that replaces the fragmented workflow of using several tools together.

### Where

- **Initial platform:** Linux.
- **Future platforms:** other desktop systems (Windows, macOS) and possibly mobile, **only if a real need arises** — following the project's incremental philosophy. No speculative porting.

### How

- **Incremental development:** features are added only when they solve a real need.
- **Documentation before implementation:** architectural decisions are recorded in ADRs before code is written.
- **Simplicity:** the architecture stays as small as possible while satisfying current requirements.
- **Learning by doing:** the project is also a way to practice software engineering, so quality matters even for a personal tool.

---

## Non-Goals

The following are explicitly out of scope for the vision:

- **Not a commercial product:** Melody Sync is not intended to generate revenue or serve a mass audience immediately. Public release is conditional and future.
- **Not a music player:** players like Strawberry already exist and work well. Melody Sync focuses on *organization*, not playback.
- **Not a clone of existing tools:** it does not aim to duplicate every feature of Picard, Kid3 or others. It fills the gap they leave: a cohesive organization workflow.
- **Not a complex YouTube sync engine:** the YouTube integration starts simple — identify a song and fetch its metadata. A full sync service is speculative at this stage.
- **No speculative features:** nothing is implemented before it is needed (per the project's incremental philosophy).

---

## Alternatives Considered

### Keep Only the Original Python Script

**Advantages**

- Minimal effort; the script already scans and lists files.

**Disadvantages**

- No structure, no tests, no way to evolve into a real tool.
- Solves only the "list files" part, not organization or enrichment.

**Why rejected:** The need grew beyond a listing script. The vision requires a tool that organizes, enriches and checks health — which needs architecture.

---

### Use the Existing Tools Together (Picard + Kid3 + LRCGET + Strawberry)

**Advantages**

- Each tool is mature at what it does.
- No development needed.

**Disadvantages**

- **Features are scattered** — exactly the problem that motivated the project.
- No cohesive workflow; the user must switch between tools and manually keep them in sync.
- Existing tools are either confusing for beginners or too technical.

**Why rejected:** This is the problem Melody Sync exists to solve. Relying on the same fragmented set of tools does not solve it.

---

### Build on Top of an Existing Base (e.g., Beets)

**Advantages**

- Beets is a powerful, mature music library manager.
- Saves years of work.

**Disadvantages**

- **Steep learning curve** for the developer, who wants to understand every decision.
- **Overkill scope** — Beets does far more than this personal tool needs.
- The developer wants to *learn* software engineering, not just use a tool.

**Why rejected:** For a personal project focused on learning and understanding the "why" behind every choice, building on an existing complex base conflicts with the goal.

---

### Stay in Python Only

**Advantages**

- The prototype is already in Python with 54 passing tests.
- Mutagen is excellent for audio metadata.

**Disadvantages**

- Limits the future evolution of the project (GUI, mobile potential).
- Less type safety for a growing codebase.

**Why rejected:** The language decision was revisited and changed to Kotlin (see ADR-0002). The vision supports this: the project should be allowed to evolve.

---

## Consequences

### Positive

- **Clear scope:** The vision defines exactly what the project is and, just as importantly, what it is not.
- **Cohesive product:** One tool replaces the fragmented workflow of several.
- **Guided decisions:** Every technical decision (language, libraries, roadmap) can be checked against the vision.
- **Learning value:** The project serves both as a useful tool and as software engineering practice.

### Negative

- **Solo maintenance:** All development, documentation and maintenance fall on one person.
- **Slower progress:** The incremental, documentation-first approach is slower than a quick-and-dirty script.
- **YouTube dependency risk:** The enrichment feature depends on the YouTube API.

### Risks

- **Scope creep:** The vision could slowly expand beyond the personal tool. **Mitigation:** the incremental philosophy and the Non-Goals section act as a constant check.
- **YouTube API viability:** Rate limits, terms of service or API changes could affect the enrichment feature. **Mitigation:** keep the metadata enrichment behind an abstraction so the source can be swapped (e.g., local identification fallback).
- **Motivation in a solo project:** Long personal projects can stall. **Mitigation:** small validated increments keep progress visible and rewarding.

---

## Implementation Notes

- The vision guides the roadmap order:
  1. **Milestone 2 — CLI:** scan the library, show statistics, and check library health.
  2. **Milestone 3 — Enrichment:** identify songs via the YouTube API and fetch metadata, covers and lyrics.
  3. **Milestone 4 — GUI:** a graphical interface for everyday use.
- The future public release is already anticipated by the existing infrastructure: MIT license and GitHub PR template.
- The YouTube integration must be isolated behind an interface (see ADR-0005 for the same pattern applied to metadata reading).
- This ADR is the reference point for future ADRs: any decision that contradicts this vision must revisit it.

---

## References

- `docs/architecture/reviews/ArchitectureReview.md`
- `docs/journal/capitulos/ 01-a-primeira-fundacao.md`
- `README.md`
- ADR-0002 — Programming Language (Kotlin)

---

## Related Documents

- `docs/INDEX.md`
- `docs/architecture/ADR/ADR-0002-Python.md`

---

## Revision History

| Version   | Date       | Description                     |
|-----------|------------|---------------------------------|
| v0.2.0    | 2026-07-31 | Initial version                 |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**