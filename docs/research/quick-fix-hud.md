# Quick-Fix HUD — Vision Document

> Registered vision for the assisted curation interaction model.
> **Not implemented yet.** Depends on backend changes (tag writing).

---

## Status

| Item | Value |
|------|-------|
| Status | **Vision / Registered** |
| Requires | Backend work (tag writing via JAudioTagger) |
| Backend freeze | ❌ Conflicts — deferred until the UX phase is complete |
| Source | `docs/standards/DesignSystem.md` §5 |

---

## What it is

The **Quick-Fix HUD** (Heads-Up Display) is the flagship assisted-curation interaction:
when the user selects a song with issues, a split-screen panel slides in from the right
showing what's wrong and offering one-click fixes.

```
┌───────────────────────┬────────────────────────────────┐
│ Song List Table       │ Quick-Fix HUD (Right Panel)     │
│                       │                                │
│ [■] Smells Like...    │ SELECTED:                      │
│ [ ] Come As You Are   │  "Smells Like Teen Spirit.mp3" │
│                       │                                │
│                       │ ⚠ Missing: Album, Genre         │
│                       │                                │
│                       │ 💡 SUGGESTED FIXES:             │
│                       │  [YouTube] Apply suggestion      │
│                       │  [Local]    Apply suggestion     │
└───────────────────────┴────────────────────────────────┘
```

### Flow

1. **Diagnosis area** — crisp summary of what's wrong (no metadata, low bitrate, folder mismatch).
2. **Local heuristic suggestions** — regex matching on folder/filename yields title/artist
   (e.g. `Nirvana/Nevermind/01-Smells Like Teen Spirit.mp3`).
3. **External YouTube suggestions** — when `YOUTUBE_API_KEY` is present, show top candidate
   with title, channel, duration and thumbnail.
4. **Accept/merge** — "Apply" populates the file tags, updates the SQLite cache in background,
   and shows an LED-green success toast. The user validates each edit personally.

---

## Why deferred

- The backend is **frozen** (10% backend / 90% UX priority).
- Applying suggestions requires **writing tags to audio files** (JAudioTagger write support),
  which is a backend change.
- Local regex heuristics and per-file diagnosis also belong in the core module.

---

## Prerequisites to implement later

- [ ] Tag writing capability in `melody-sync-core` (Metadata writer via JAudioTagger).
- [ ] A `SongDiagnostics` model (missing fields, quality flags).
- [ ] Local filename/folder regex matcher (`SongMatcher`).
- [ ] GUI split-pane layout + Apply flow + success toast.
- [ ] Update database cache after apply.

---

## Related

- `docs/standards/DesignSystem.md` §5 (original spec)
- `docs/research/app-design.md`
- ADR-0001 — Project Vision (assisted curation)
