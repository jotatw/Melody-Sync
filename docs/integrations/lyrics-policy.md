# Lyrics Policy

> Boundary definition for lyrics lookup and future lyrics providers.

---

## Document Information

| Item | Value |
|---|---|
| Category | Integration / Informational Support |
| Audience | Core, desktop and integration developers |
| Status | Defined / Current Policy |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

Define the role of lyrics services in Melody Sync and prevent lyrics lookup from becoming an implicit metadata-writing workflow.

---

## 2. Primary Role

Lyrics are **informational**.

They may help the user:

- identify a song;
- confirm that a candidate is correct;
- inspect lyrics while reviewing a song.

They are not automatically written to audio tags.

---

## 3. Current Provider

Lyrics.ovh is currently used as an HTTP/API integration for testing and informational lookup.

The application should keep the provider boundary replaceable so another lyrics source can be added later.

---

## 4. Lookup Flow

```text
Current song / identification context
          ↓
     lyrics provider
          ↓
        result
          ↓
      display only
```

A lookup result does not modify the song file or database metadata by itself.

---

## 5. Relationship with Metadata

Lyrics may support identification but are not a metadata authority.

The following flow is intentionally forbidden:

```text
Lyrics
  ↓
automatic Artist/Title update
  ↓
Apply
```

The metadata workflow remains independent:

```text
Identification / metadata provider
  ↓
editable suggestion
  ↓
user approval
  ↓
metadata write
```

---

## 6. Relationship with YouTube

YouTube and lyrics can provide complementary evidence:

```text
YouTube
→ identification context

Lyrics
→ informational confirmation

Metadata provider
→ structured metadata suggestion
```

No single service automatically owns the final metadata decision.

---

## 7. Failure States

Lyrics lookup should distinguish:

- lyrics not found;
- provider unavailable;
- network/API error;
- invalid or incomplete lookup context;
- successful result.

A failed lyrics lookup must not affect the metadata state of the song.

---

## 8. User Experience

Lyrics should remain secondary to the metadata workflow.

The interface should make it clear that:

```text
Lyrics
Informational
```

rather than presenting lyrics as another automatic correction source.

---

## 9. Non-Goals

This policy does not define:

- automatic lyrics tagging;
- automatic synchronized lyrics embedding;
- lyrics file generation;
- lyrics caching strategy;
- selection of a permanent lyrics provider;
- lyric translation or transformation.

Those require separate decisions if they become real requirements.

---

## Related Documents

- [Metadata Workflow](../planning/metadata-workflow.md)
- [Metadata Foundation](../planning/metadata-foundation.md)
- [YouTube Identification](youtube-identification.md)
- [Quick Fix HUD](../research/quick-fix-hud.md)

---

**End of Document**
