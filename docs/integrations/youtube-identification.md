# YouTube Identification

> Boundary definition for using YouTube as a lightweight song-identification and discovery source.

---

## Document Information

| Item | Value |
|---|---|
| Category | Integration / Identification |
| Audience | Core, desktop and integration developers |
| Status | Defined / Target Boundary |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

Define the limited role of YouTube in Melody Sync.

YouTube is useful because the user's real-world workflow may begin with finding a song on YouTube. The application therefore needs a practical way to use YouTube information for identification without treating YouTube metadata as the final cataloguing authority.

---

## 2. Primary Role

YouTube is an **identification and discovery source**.

Its main question is:

> What song might this file or user query represent?

It is not:

> What exact metadata must be written to this file?

---

## 3. Information That May Be Used

Depending on the available API and integration capabilities, YouTube may provide:

- video title;
- video URL / identifier;
- channel/uploader name;
- description or other public context when explicitly supported;
- other identification-oriented information available from the selected integration.

The integration must not assume that every field is present or reliable for every video.

---

## 4. Reuploads

Reuploads are expected and are not treated as exceptional failures.

Example:

```text
Uploader Channel - Artist Name - Song Name [Reupload]
```

The uploader/channel may be unrelated to the original artist.

Therefore:

```text
YouTube channel ≠ Artist
Uploader ≠ Artist
Video title ≠ Structured metadata
```

unless the user explicitly confirms the relationship.

---

## 5. Title Parsing

A title parser may be used to generate candidate information.

It must be considered heuristic.

For example:

```text
"Uploader - Artist - Song (Reupload)"
```

may produce:

```text
Possible Artist: Artist
Possible Title: Song
```

but the parser must not directly write these values.

The parser should be conservative and may return an unresolved/ambiguous result.

---

## 6. Identification vs. Enrichment

The recommended flow is:

```text
YouTube
  ↓
possible identification
  ↓
metadata provider
  ↓
structured suggestion
  ↓
Quick Fix
  ↓
user approval
```

YouTube does not need to solve the entire metadata problem.

A later provider can use the identified song as input to retrieve more reliable structured information.

---

## 7. Suggestions

YouTube-derived values are suggestions only.

They must remain:

- visible as suggestions;
- editable;
- distinguishable from current metadata;
- attributable to their source;
- subject to explicit user approval.

The UI should not imply that a YouTube value is already part of the song's metadata.

---

## 8. Source Attribution

When a YouTube-derived suggestion is displayed, the user should be able to understand that it came from YouTube.

Conceptually:

```text
Source: YouTube

Possible match
Artist: ...
Title: ...
```

A future provider may produce a separate suggestion beside it.

---

## 9. Multiple Results

If several videos are plausible matches, Melody Sync should present multiple candidates rather than silently accepting the first result.

The user selects the candidate that best represents the local file.

The integration may rank results, but ranking is not authorization to apply metadata.

---

## 10. Relationship with Local Context

YouTube may be combined with local information such as:

- current filename;
- path;
- existing tags;
- manually entered search terms.

Local context is also evidence, not automatic authority.

A filename such as:

```text
Artist - Song.mp3
```

may help search YouTube, but the resulting match still requires validation.

---

## 11. Relationship with Lyrics

Lyrics lookup is a separate informational workflow.

YouTube identification may help the user identify a song, while a lyrics provider may help confirm or inspect lyrics.

Neither source automatically writes tags.

---

## 12. Failure and Ambiguity

The integration must support at least these conceptual outcomes:

```text
No result
Single plausible result
Multiple plausible results
Unavailable service
Network/API error
Rate limited
Ambiguous result
```

Failure to identify a song is not the same as failure to write metadata.

These operations should remain separate in the application state and UI.

---

## 13. Provider Independence

The application must not make YouTube a mandatory dependency of the Core metadata system.

The integration should remain replaceable so another identification source can be added later without changing the metadata write architecture.

Conceptually:

```text
Identification source
├── YouTube
├── future source A
└── future source B

        ↓
identification result
        ↓
metadata workflow
```

---

## 14. Security and Privacy Boundary

The YouTube integration must only use information required by the approved identification workflow.

Authentication, API keys, rate limits and provider-specific operational details belong to the integration implementation and configuration documentation, not to the song metadata model.

Secrets must never become part of stored song metadata.

---

## 15. Non-Goals

YouTube integration does not define:

- automatic tag writing;
- automatic artist extraction as authoritative data;
- automatic album assignment;
- automatic lyrics tagging;
- permanent dependency on YouTube;
- a universal title-parsing algorithm;
- a complete music metadata catalogue.

---

## 16. Target Interaction

```text
User has an incompletely identified song
          ↓
      Quick Fix
          ↓
     YouTube search
          ↓
   candidate results
          ↓
   user selects match
          ↓
 structured metadata source
          ↓
   editable suggestion
          ↓
        Apply
```

The important boundary is:

> **YouTube identifies; a metadata provider enriches; the user approves; the metadata subsystem writes.**

---

## Related Documents

- [Metadata Workflow](../planning/metadata-workflow.md)
- [Metadata Foundation](../planning/metadata-foundation.md)
- [Metadata Providers](metadata-providers.md)
- [Quick Fix HUD](../research/quick-fix-hud.md)
- [Library Screen](../design/screens/library.md)

---

**End of Document**
