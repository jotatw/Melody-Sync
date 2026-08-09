# Metadata Identification and Enrichment

> Planning document for the relationship between YouTube, external metadata providers, Quick Fix, and final library metadata.

## Document Information

| Item | Value |
|---|---|
| Category | Planning / Metadata Workflow |
| Audience | Developers / UX |
| Status | Defined / Target |
| Primary screens | Library, Quick Fix, Health |
| Related integrations | YouTube, Lyrics.ovh, future metadata providers |
| Last Updated | 2026-08-09 |

---

## 1. Objective

Melody Sync should help identify and organize music without treating any external service as the unquestionable authority over the library.

The intended workflow is:

```text
YouTube
  ↓
Simple identification / discovery
  ↓
Possible metadata
  ↓
More robust metadata source
  ↓
User review
  ↓
Explicit Apply
  ↓
Final library metadata
```

The goal is not full automatic tagging. The goal is to reduce manual work while keeping the user in control of the final metadata.

---

## 2. YouTube Role

YouTube is an **identification and discovery source**.

It may provide useful information such as:

- video title;
- video URL;
- channel/uploader;
- description, when available;
- other public information exposed by the integration.

This information can help answer:

> What music is this probably referring to?

YouTube data must not automatically become the final music metadata.

### Important rule

The YouTube channel/uploader must not be interpreted automatically as the music `Artist`.

A reupload can contain titles such as:

```text
Uploader Name - Artist Name - Song Name [Reupload]
```

The uploader may be unrelated to the artist. Titles can also contain remix labels, version information, upload notes, playlists, or other text that should not be copied blindly into metadata.

---

## 3. YouTube as an Identifier

For a music item, YouTube should initially be treated as a pointer toward a possible identity rather than as a metadata authority.

Conceptually:

```text
Local file
  ↓
YouTube reference
  ↓
Possible song identity
```

The integration may extract or suggest:

```text
Possible artist
Possible title
Possible version
Source URL
Source channel
```

These remain suggestions until reviewed.

The application should prefer simple, transparent identification over aggressive parsing heuristics.

---

## 4. Reuploads and Ambiguous Titles

YouTube reuploads are expected to be ambiguous.

Examples of potentially unreliable title information include:

```text
Artist - Song - Reupload
Song Name | Artist Name | Lyrics
Uploader - Song Name
Artist Name (Remix) [Official Reupload]
```

Melody Sync must not assume that every segment separated by `-`, `|`, parentheses, brackets, or similar delimiters represents a specific metadata field.

Parsing may generate candidates, but the candidates must remain editable and must not be silently applied.

When confidence is low, the interface should prefer showing the raw source information and letting the user decide.

---

## 5. Robust Metadata Sources

After simple identification, a more specialized metadata provider may be used to obtain or verify structured music metadata.

The provider may supply fields such as:

```text
Artist
Title
Album
Album Artist
Track Number
Disc Number
Release
Year
Genre
Artwork
```

The exact supported fields depend on the provider and on the Melody Sync metadata model.

A provider is a source of suggestions or verified data for the application workflow; it does not bypass the user's final confirmation.

---

## 6. Source Independence

External services should remain replaceable.

Conceptually:

```text
                    ┌── YouTube
                    ├── Metadata Provider A
Melody Sync ────────┼── Metadata Provider B
                    ├── Local/manual source
                    └── Future providers
```

The UI and core workflow should not depend directly on one provider's terminology or response format.

A future provider can be added or replaced without redesigning the whole Quick Fix workflow.

---

## 7. Suggestion Model

External data should be represented as a suggestion before it becomes library metadata.

Conceptually:

```text
Current
Artist: —
Title: —

Suggestion
Artist: Artist Name
Title: Song Name

Source: YouTube / metadata provider
Confidence: Medium

[Apply]
```

The user must be able to inspect and edit the suggested values before applying them.

No external result should overwrite existing metadata automatically merely because it was returned by a provider.

---

## 8. Confidence

Confidence is useful as a UX concept, especially when identification is based on incomplete or ambiguous external information.

Possible conceptual states:

```text
High
Medium
Low
Unknown
```

Confidence must not be presented as a guarantee of correctness.

Low-confidence results should require more user attention rather than being silently applied.

The exact confidence calculation is not defined yet and should not be invented as part of the current implementation.

---

## 9. Final Authority

The final authority for library metadata is the user through an explicit confirmation action.

```text
External source
      ↓
Suggestion
      ↓
User review
      ↓
Edit if necessary
      ↓
Apply
      ↓
Library metadata
```

This is especially important for reuploads, unofficial uploads, remixes, live versions, alternate edits, and other cases where a source title does not unambiguously describe the desired catalog entry.

---

## 10. Relationship with Quick Fix

Quick Fix is the primary place for applying this workflow.

The intended flow is:

```text
Library
  ↓
select song
  ↓
Quick Fix
  ↓
identify / retrieve suggestions
  ↓
compare current and suggested values
  ↓
edit if necessary
  ↓
Apply
```

Quick Fix should make the source of a suggestion visible enough for the user to understand where it came from.

YouTube and metadata providers should support Quick Fix rather than becoming separate metadata-management destinations.

---

## 11. Relationship with Health

Health identifies incomplete or suspicious metadata but does not decide the final values.

Example:

```text
Health
  ↓
Missing Artist
  ↓
Library with issue context
  ↓
select song
  ↓
Quick Fix
  ↓
YouTube identification
  ↓
metadata provider
  ↓
user confirmation
```

Health owns diagnosis.

Library owns selection and song-level work.

Quick Fix owns correction.

External providers supply information to that workflow.

---

## 12. Lyrics.ovh

Lyrics are informational support only.

The Lyrics.ovh integration must not automatically apply lyrics to music tags.

Its conceptual role remains:

```text
Quick Fix
  ↓
Lyrics
  ↓
Informational result
```

Lyrics are not part of the authoritative metadata enrichment path unless a future decision explicitly changes this rule.

---

## 13. Non-Goals

This planning document does not define:

- automatic full-library tagging;
- automatic acceptance of YouTube metadata;
- automatic artist extraction from channel names;
- a universal title parser;
- a specific future metadata provider;
- a final confidence algorithm;
- automatic lyrics tagging;
- a music player;
- mobile-specific metadata workflows.

These may be considered later, but they are outside the current implementation scope.

---

## 14. Implementation Principles

Before implementing an integration:

1. Reuse the current application and domain structures where possible.
2. Keep provider-specific data at the integration boundary.
3. Convert external results into application-level suggestions rather than writing directly to the library.
4. Keep suggestions editable.
5. Require explicit user confirmation before applying metadata.
6. Preserve the original source information when it helps explain a suggestion.
7. Avoid aggressive heuristics when the source is ambiguous.
8. Do not add a new abstraction unless the current implementation demonstrates that it is necessary.

---

## 15. Current Decision Summary

| Concern | Decision |
|---|---|
| YouTube | Simple identifier / discovery source |
| YouTube title | Suggestion, not authoritative metadata |
| YouTube uploader/channel | Never automatically treated as Artist |
| Reuploads | Expected ambiguous case; require review |
| Robust metadata provider | Used later for structured/verified suggestions |
| User | Final authority before Apply |
| Quick Fix | Main metadata enrichment workflow |
| Health | Detects issues; does not apply metadata |
| Library | Provides selection and context |
| Lyrics.ovh | Informational only; never automatic tagging |
| Multiple providers | Allowed through replaceable integration boundaries |

---

## 16. Future Extension

When a robust metadata provider is selected, the next documentation step should define:

```text
Provider
  ↓
Search input
  ↓
Candidate matching
  ↓
Candidate ranking
  ↓
Metadata suggestion
  ↓
User review
  ↓
Apply
```

That future document should define matching rules separately from the YouTube integration. The project should not make YouTube responsible for the complete metadata identification problem.
