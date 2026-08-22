# Metadata Filename Conventions

> How real music filenames are structured — Japanese titles, remixes, symbols, source suffixes — and how Melody Sync should treat them in the local matcher and future enrichment.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | PLAN-FILENAME-CONV-001 |
| Category         | Planning / Metadata |
| Audience         | Core developers, UX |
| Status           | Reference (grounded on the real library 2026-08) |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## Purpose

Before improving the filename matcher or enabling Metadata Enrichment, we need a shared understanding of how real filenames are written. This document:

- catalogues the patterns actually present in the project's real library (690 audio files);
- explains Japanese/anime, remix, vocaloid, and source-suffix conventions;
- separates **simple, safe** normalization (apply now) from **complex** treatment (defer);
- gives rules so a future matcher/enrichment behaves predictably.

It is the companion of `SongMatcher` (current local heuristic) and the Metadata Enrichment plan.

---

## Why filenames matter

A filename is the only structured signal available before tags are read. It encodes `Artist`, `Title`, `Album`, `Remix/feat.`, `Version`, and `Source`. Good handling means:

- more Quick Fix suggestions with no external service;
- correct input for Metadata Enrichment (YouTube identification needs a clean title);
- consistent Organize output (`Artist/Album/Title.ext`).

Media-server guidance (Jellyfin/FileBot) reinforces the target: `Artist/Album (Year)/NN - Track Title.flac`, no problematic characters (`: ? * < > |`), cover art as `cover.jpg`.

---

## Real corpus patterns (690 audio files, 2026-08)

| Pattern | Files | Meaning |
|---------|------:|---------|
| `' - '` ASCII separator | 439 | `Artist - Title` |
| `[ … ]` brackets | 649 | YouTube IDs (`[uNi5Go5dth8]`), `[CC]`, `[ORIGINAL]`, `[vocal: X]`, `[Instrumental]`, `[Kami Cover]` |
| `ft.` / `feat.` | 123 | featured artist |
| underscore `_` | 135 | space **or** apostrophe (ambiguous) |
| CJK (Japanese/Chinese) | 192 | titles in kanji/kana |
| remix / `(mix)` | 36 | remix/cover/version tags |
| en-dash `–` / em-dash `—` | 17 / 27 | editorial separator (`Artist – Title`) |
| fullwidth hyphen `－` | 1 | CJK fullwidth punctuation |
| Japanese quotes `「」『』` | 20 | title quoting |
| `(MP3_320K)` | 24 | source/quality suffix (YouTube rip) |
| `OP` / `ED` | 5 | anime opening/ending theme |
| tilde `~` | 1 | wave dash (Japanese) |

Notes:

- 94% of files carry `[…]`; most are YouTube video IDs or qualifiers, not part of the title.
- Underscores are genuinely ambiguous: `don_t` = "don't" (apostrophe), `JoJo_s` = "JoJo's", `Golden Wind OST_` = trailing space. Blanket `_`→space corrupts contractions.

---

## Conventions by category

### Japanese / anime / vocaloid

- Titles are quoted with `「」` (e.g. `「KISS OF DEATH」`) or `『』`; `・` is the katakana middle dot; `～` is the wave dash.
- **OP/ED** (and insert songs) mark anime theme tracks: `Darling in the FranXX OP…「KISS OF DEATH - Mika Nakashima x Hyde」`.
- **Vocaloid** songs are usually identified by the producer (often suffixed `P`, e.g. `DECO*27`) and the vocal (`[vocal: gemie]`); dedicated services (VocaDB) exist for tagging.
- **Cover artists** use tags like `[Kami Cover]`, `歌ってみた` (sang it), or `(cover)`.
- Romaji and kanji may coexist; the "artist" may be a character/anime unit rather than a person.

### Remix / feat. / versions

- `Artist - Title (X Remix)` or `(X mix)` — the remixer is a separate credit, not the track artist.
- `Artist - Title (feat. Y)` / `ft. Y` — featured artist goes to a **feat.** field (not `artist`).
- `[Instrumental]`, `[Off Vocal]`, `Full ver.` / `Short ver.` are version tags.

### Source / download suffixes

- `(MP3_320K)`, `(FLAC_...)`, `(1080p)`, channel tags — artifacts of the source (YouTube/audio extraction). They belong to **no metadata field** and should be stripped before matching/enrichment.

### Separators and dashes

- ASCII `-` is the most common; `–`/`—`/`－` are the same separator in editorial/CJK text. A matcher should normalize them before splitting (already done for `SongMatcher`).

---

## Treatment strategy

### Apply now (simple, safe)

1. **Unicode-dash normalization**: `–`, `—`, `－` → `-` before the `Artist - Title` split. *Done in `SongMatcher` (2026-08).*
2. **Strip obvious source suffixes** before matching: trailing `(MP3_320K)` / `(FLAC…)` style tokens (uppercase/digit token groups only, so `(Remix)` / `(feat. X)` stay). *Done in `SongMatcher` (2026-08).*
3. **Whitespace/trim** around split parts (already done).

### Defer (complex — needs care)

4. **Underscore disambiguation**: `_` is space or apostrophe; deciding requires context (`don_t` → "don't" vs `Golden Wind OST_` → space). Not safe as a blanket rule.
5. **Bracket-tag stripping**: `[CC]`, `[ORIGINAL]`, `[vocal: X]`, `[Instrumental]`, YouTube IDs. Valuable (649 files), but each tag type has different meaning; a tag allowlist/blacklist is needed — design carefully.
6. **`[ORIGINAL] X - Y` inversion**: the bracketed prefix is not the artist; requires recognizing prefix qualifiers.
7. **`… by X` phrasing**: "by" indicates the artist/author; a pattern, but ambiguous (could be part of a title).
8. **Japanese quoting**: `「…」` — strip quotes to recover the true title; keep the quoted form for enrichment search.
9. **feat./remix parsing into dedicated fields**: requires new `Song` fields (deferred with metadata-field expansion).

---

## Rules for the future matcher / enrichment

- **Clean before matching**: apply normalization (dashes, source suffixes, brackets) to produce a *search title*, but keep the original filename for the user.
- **Never guess a blank field from a non-separator name** (e.g. `artist = folder name` for a flat library produced "Músicas" — a wrong suggestion is worse than none).
- **feat./remixer ≠ artist**: featured and remixer credits are separate data, not the primary `artist`.
- **Suggestions stay editable and reviewed** — matching is assisted, never automatic.
- **Japanese titles should be preserved in original script** for enrichment (YouTube matches kanji/kana better than romaji).

---

## Relationship to existing work

- `SongMatcher` — current local heuristic (`docs` referenced from Quick Fix HUD).
- Product Validation **V5** — measured coverage 9/13 on the real no-artist MP3s; the remaining 4 misses map to rules 4–8 above.
- Metadata Enrichment (future) — will consume the "clean search title" produced by these rules.
- `metadata-identification-and-enrichment.md` — the enrichment boundary this document supports.

---

## Related Documents

- [Product Validation Report](product-validation-report.md) (V5)
- [Metadata Identification & Enrichment](metadata-identification-and-enrichment.md)
- [Metadata Workflow](metadata-workflow.md)
- [Quick-Fix HUD research](../research/quick-fix-hud.md)

---

## Revision History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | 2026-08-22 | Initial catalogue grounded on the real library; simple vs deferred treatment; rules for matcher/enrichment |

---

This document follows the Melody Sync Documentation Standard.

**End of Document**