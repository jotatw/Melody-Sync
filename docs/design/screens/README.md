# Screen Interaction Documents

> Interaction contracts for every screen. One document per screen; each file defines what the screen is responsible for, how it interacts with the user, what it may update, and what it must not do.

## Purpose

The screen documents are the source of truth for interaction design. They describe behavior, navigation rules, and screen boundaries before implementation details.

## Relationship to other design docs

- `docs/design/app-design.md` defines the global navigation model and cross-screen rules.
- `docs/standards/DesignSystem.md` defines the visual language, tokens, and reusable components.
- Files in this directory define screen-specific interaction contracts.

## Navigation structure

The primary navigation contains six structural destinations: Library, Statistics, Health, Organize, Settings, About. `Review` and `Duplicates` are intentionally **not** primary destinations — they are contextual workflows reached through Health (and Library) and are documented here as their own screen contracts (see `docs/design/app-design.md` "Primary Navigation").

## Screen documents

- [library.md](library.md) — primary workspace: inspect, search, select, act.
- [health.md](health.md) — diagnostic hub: issue categories and review entry.
- [statistics.md](statistics.md) — observation surface: aggregate library information.
- [organize.md](organize.md) — plan-first, apply-second filesystem workflow.
- [settings.md](settings.md) — configuration surface.
- [about.md](about.md) — informational surface.
- [review.md](review.md) — contextual song-level attention workflow (via Health).
- [duplicates.md](duplicates.md) — contextual duplicate review workflow (via Health).

## Contract template

Every screen document answers the same questions (mirroring the template in `docs/design/app-design.md`):

- Purpose
- User question
- Responsibilities
- Non-responsibilities
- Entry points
- What the user sees
- Primary actions
- States and empty states
- Contextual interactions
- Navigation rules
- Data interaction (consumes / can change)
- UX rules
- Accessibility notes
- Decision rules