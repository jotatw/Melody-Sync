# Metadata Workflow

> End-to-end planning document for identifying, enriching, reviewing and applying music metadata.

---

## Document Information

| Item | Value |
|---|---|
| Category | Planning / Workflow Contract |
| Audience | Core, desktop and UX developers |
| Status | Defined / Target Workflow |
| Project Version | v0.13.0-dev |
| Last Updated | 2026-08-09 |

---

## 1. Purpose

Define how Melody Sync should move from an incompletely identified music file to user-approved final metadata without making any external source authoritative.

The workflow is intentionally assisted rather than fully automatic.

The user remains responsible for the final metadata decision.

---

## 2. Core Principle

```text
File
  ↓
Identification
  ↓
Enrichment / suggestions
  ↓
Review
  ↓
Explicit Apply
  ↓
Write metadata
  ↓
Read back
  ↓
Update library state
```

No external source may silently write final metadata.

---

## 3. Responsibility Boundaries

### Library

Provides the working context for individual songs.

### Health

Identifies missing or suspicious metadata and guides the user toward review.

### YouTube

Provides lightweight song identification and discovery context.

YouTube data is not authoritative metadata.

### Metadata Providers

Provide structured metadata suggestions when a reliable provider is available.

### Lyrics Provider

Provides informational lyrics only.

Lyrics are never automatically written to tags.

### Quick Fix

Combines diagnostics, suggestions and explicit user approval into a correction workflow.

### Metadata Foundation

Handles provider capabilities, safe writes, typed failures, read-back validation and persistence synchronization.

The existing foundation and provider boundaries are recorded in `metadata-foundation.md`.

---

## 4. Identification vs. Final Metadata

Identification answers:

> What song might this file represent?

Final metadata answers:

> How should this song be catalogued in my library?

These are related but different problems.

A YouTube title such as:

```text
Uploader Name - Artist Name - Song Name [Reupload]
```

may help identify the song but must not be interpreted as authoritative Artist/Title metadata.

---

## 5. Suggested Workflow

### Step 1 — Existing file data

Start with metadata already present in the file.

Do not replace valid user data merely because an external source provides another value.

### Step 2 — Diagnose gaps

Identify fields that are missing or otherwise require attention.

The current foundation intentionally focuses on title, artist and album writes.

### Step 3 — Lightweight identification

Use available local context and optional external identification sources.

Current examples include:

- filename/path context;
- YouTube video information;
- manually supplied information.

### Step 4 — Structured enrichment

When needed, query a metadata provider capable of returning structured music information.

The provider is a suggestion source, not an automatic writer.

### Step 5 — Review

Quick Fix presents the current values and proposed changes together.

The user must be able to edit or reject a suggestion.

### Step 6 — Explicit Apply

Only an explicit user action writes metadata.

### Step 7 — Read-back validation

After a successful write, the application reads the file again and verifies the persisted values before updating the application/database representation.

This behavior is part of the implemented metadata foundation.

---

## 6. Source Priority

There is no universal source priority that should blindly overwrite existing values.

Instead, sources have roles:

```text
Existing metadata
→ current state

YouTube
→ identification / discovery

Metadata provider
→ structured suggestion

Manual input
→ user correction
```

When sources disagree, Quick Fix should expose the disagreement instead of silently choosing a value.

---

## 7. Reuploads and Ambiguous YouTube Titles

Reuploads are an expected case.

The following must not be assumed automatically:

```text
YouTube channel = Artist
Uploader name = Artist
First token in title = Artist
Last token in title = Title
```

Parsing may generate candidate values, but candidates must remain editable and require explicit approval.

A channel/uploader can be useful as identification context without becoming an Artist field.

---

## 8. Confidence

Future suggestion systems may expose confidence levels, but confidence must describe a suggestion rather than authorize an automatic write.

Conceptually:

```text
High
Medium
Low
Unknown
```

A low-confidence suggestion remains useful if it helps the user identify the correct record, but it must not receive special write authority.

The exact scoring algorithm is intentionally deferred.

---

## 9. Multiple Candidate Results

If an identification or metadata provider returns several plausible matches, the UI should present them as alternatives rather than silently selecting one.

Conceptually:

```text
Possible match 1
Possible match 2
Possible match 3

[Select]
```

The matching system must be allowed to report ambiguity.

---

## 10. Missing Metadata Groups

Health may group multiple songs with the same missing field.

For example:

```text
Missing Artist · 37
```

`Review all` should open Library with the appropriate issue context rather than silently selecting only the first song.

The Library remains responsible for selecting an individual song and opening Quick Fix.

---

## 11. Statistics Relationship

Statistics must distinguish known metadata values from missing metadata.

For example:

```text
Artist A · 100
Artist B · 84
Missing Artist · 20
```

Songs without an Artist value must not be attributed to Artist A merely because their filename, folder or YouTube context suggests that artist.

Statistics may navigate to Library with a semantic missing-field context, but such a context is not the same as an actual metadata value.

---

## 12. Lyrics

Lyrics are informational only.

Current policy:

```text
Lyrics provider
  ↓
lookup
  ↓
display
```

There is no automatic path:

```text
lyrics
  ↓
Artist/Title tags
```

Lyrics may help the user identify a song, but they are not a metadata authority and are not part of the automatic Apply operation.

---

## 13. Safety Rules

The metadata workflow must:

- never silently overwrite user metadata;
- never treat YouTube uploader/channel identity as authoritative Artist data;
- never infer metadata from a filename/path and write it automatically;
- never accept an external suggestion without explicit user confirmation;
- never report a write as successful before persistence is verified;
- distinguish unsupported format/capability from other write failures;
- preserve the original file when performing diagnostics or test writes;
- keep external integrations behind replaceable provider boundaries.

---

## 14. Current Write Scope

The implemented foundation supports the current title, artist and album write path.

The following remain future extensions unless explicitly approved:

```text
Genre
Year
Artwork
Track number
Disc number
Additional release metadata
```

Adding a field requires both domain/database justification and provider/write capability validation.

---

## 15. Non-Goals

This workflow does not define:

- a fully automatic tagger;
- a single mandatory metadata provider;
- a permanent dependency on YouTube;
- automatic lyrics tagging;
- bulk destructive metadata rewriting;
- a universal confidence algorithm;
- automatic acceptance of the first provider result.

---

## 16. Target User Experience

The user should experience the process as:

```text
I found a song
    ↓
Melody Sync helps identify it
    ↓
Melody Sync shows possible metadata
    ↓
I check the result
    ↓
I decide what to apply
    ↓
Melody Sync writes and verifies it
```

The application assists the user's judgement rather than replacing it.

---

## Related Documents

- [Metadata Foundation](metadata-foundation.md)
- [Metadata Formats](metadata-formats.md)
- [Metadata Providers](../integrations/metadata-providers.md)
- [YouTube Identification](../integrations/youtube-identification.md)
- [Health Screen](../design/screens/health.md)
- [Library Screen](../design/screens/library.md)
- [Review Screen](../design/screens/review.md)

---

**End of Document**
