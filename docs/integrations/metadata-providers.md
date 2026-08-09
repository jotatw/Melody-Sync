# Metadata Providers — Integration Model

> Integration model for external services that help identify or enrich music metadata.

## Status

Defined / Target

## Purpose

External metadata providers support the Melody Sync metadata workflow without becoming the owner of the library metadata.

The integration boundary should allow providers to be added, replaced, or removed without changing the core Library workflow.

## Provider Role

A provider receives an identification/search context and returns candidate information that Melody Sync can present as a suggestion.

```text
Library
  ↓
Quick Fix
  ↓
Provider search
  ↓
Candidate result
  ↓
Metadata suggestion
  ↓
User review
  ↓
Apply
```

## Provider Independence

Providers must not be coupled directly to UI fields or persistence operations.

Conceptually:

```text
Provider API
    ↓
Provider adapter
    ↓
Application-level candidate/suggestion
    ↓
Quick Fix
```

Provider-specific response formats should remain at the integration boundary.

## Matching

Provider results are candidates, not automatic truth.

Matching should consider the available identification context and may use:

- current filename;
- current metadata;
- YouTube identification data;
- user-provided search text;
- provider-specific identifiers.

The exact matching and ranking algorithm remains undefined until a concrete provider is selected.

## User Confirmation

No provider result should overwrite library metadata automatically.

The user must be able to:

- inspect the candidate;
- edit fields;
- reject the candidate;
- apply the accepted values explicitly.

## YouTube Relationship

YouTube is treated as a simple identification/discovery source, not as the final metadata provider.

```text
YouTube
  ↓
possible identity
  ↓
metadata provider
  ↓
structured candidate
```

The YouTube channel/uploader must not be automatically interpreted as the music artist.

Reuploads and ambiguous titles are expected and require user review.

## Lyrics Relationship

Lyrics.ovh is an informational lyrics source and is not part of the automatic metadata application path.

Lyrics must remain separate from authoritative metadata suggestions unless a future decision explicitly changes this rule.

## Non-Goals

This document does not select a specific future provider or define its API integration.

It also does not define a universal automatic tagging system, automatic provider acceptance, or a final confidence algorithm.

## Future Provider Document

When a concrete provider is selected, document separately:

1. provider capabilities;
2. authentication requirements;
3. search inputs;
4. response mapping;
5. candidate matching;
6. ranking/confidence;
7. rate limits and errors;
8. caching, if necessary;
9. user-facing source attribution;
10. explicit Apply behavior.
