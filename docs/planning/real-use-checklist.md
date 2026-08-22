# Real-Use Friction Checklist

> Checklist to guide using Melody Sync on a real library and recording actual friction. Findings feed the expansion decision (Metadata Enrichment most likely).

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | REAL-USE-CHECKLIST-001 |
| Category         | Planning / Real Use |
| Status           | Ready to use |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## Real library baseline (2026-08-22, `/home/joao/Músicas`)

- Audio: **690** (643 m4a, 46 mp3, 1 opus)
- Non-audio: **1166** (1049 images/covers, 85 subtitles vtt, 21 lyrics lrc/txt, 11 meta)
- **Health:** 14 songs missing artist, 1 song with zero duration, 0 orphaned
- Syncthing folders (`.stfolder`, `.stversions`) present and handled as non-audio
- The 14 no-artist songs are YouTube-sourced `(MP3_320K).mp3` (title+album filled, artist embedded in the title)

## How to run the pass

1. Start the desktop app and scan `/home/joao/Músicas` (the GUI persists to the default database).
2. Follow the flow below; record friction as you go.
3. Do **not** apply destructive changes (Organize `--apply`) unless you intend to.

## Flow to walk

### 1. Library
- [ ] Scan completes with a clear progress/result
- [ ] 690 songs listed; search/filter/sort feel responsive
- [ ] Filters disclosure and result count ("X of Y songs") behave
- [ ] Compact window keeps the list usable

### 2. Health
- [ ] The 14 missing-artist songs are easy to find and understand
- [ ] The 1 zero-duration opus is clear
- [ ] The 1166 non-audio files are presented without overwhelming the audio issues
- [ ] Actions from Health lead somewhere useful (Review / Library context)

### 3. Review → Quick Fix
- [ ] Opening Review shows the issues; selecting a song opens Quick Fix
- [ ] For a no-artist song, a suggestion is available (local filename heuristics, e.g. "Artist - Title" from the filename)
- [ ] Apply writes, read-back verifies, and the issue disappears after refresh
- [ ] Is identifying the artist manually the main bottleneck? (this decides Metadata Enrichment)

### 4. Statistics
- [ ] Artist/Album/Format drill-down opens Library with a single filter chip
- [ ] Charts are readable and labeled

### 5. Duplicates
- [ ] Detection runs; groups are reviewable
- [ ] Empty result reads as a success

### 6. Organize (dry-run only)
- [ ] Plan is understandable; apply is explicit and confirmed
- [ ] Result is evident after apply

## Friction recording

For each friction, note:

- what you were trying to do;
- what happened (or didn't);
- how often it occurs;
- severity (annoying / blocking).

Record here:

```text
F1:
F2:
F3:
...
```

## Decision input

After the pass, answer:

- Was identifying/fixing metadata the biggest friction? → Metadata Enrichment
- Was listening to decide a bottleneck? → Lightweight Playback
- Was something missing to decide (year/genre/artwork)? → those fields
- Is the current flow enough? → maintenance-only

---

## Related Documents

- [Product Validation Report](product-validation-report.md)
- [Product Hardening Report](product-hardening-report.md)
- [Product Roadmap](../project/product-roadmap.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**