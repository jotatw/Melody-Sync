# Metadata Formats

> Read/write capability matrix for every supported audio format, based on measured behavior against real fixtures (Phase E of the metadata foundation).

---

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | planning/metadata-formats |
| Category         | Planning |
| Audience         | Core and desktop developers |
| Status           | Active |
| Project Version  | v0.13.0-dev |
| Template Version | BaseDocument v1.0 |
| Last Updated     | 2026-08-09 |
| Maintainer       | Melody Sync |

---

## Purpose

Document which formats Melody Sync can read and write, backed by fixtures and automated tests — not assumptions. Add or change a row only after the corresponding `FixtureCapabilityTest` passes.

## Capability Matrix

| Format | Read | Tag read | Write | Provider | Notes |
|--------|------|----------|-------|----------|-------|
| MP3 | ✓ | ✓ | ✓ | JAudioTagger | ID3 |
| M4A | ✓ | ✓ | ✓ | JAudioTagger | MP4 atoms; per-file write can fail on unusual layouts |
| FLAC | ✓ | ✓ | ✓ | JAudioTagger | Vorbis comments |
| WAV | ✓ | ✓ | ✗ | JAudioTagger | read-only: JAudioTagger's WAV writer silently drops LIST/INFO tags |
| OGG | ✓ | ✓ | ✓ | JAudioTagger | Vorbis |
| Opus | ✓ | ✓ | ✓ | OpusProvider | built-in Ogg/OpusTags reader/writer (JAudioTagger has no Opus) |
| AAC | ✗ | ✗ | ✗ | — | JAudioTagger has no AAC reader; removed from supported formats |

- **Read**: the file parses (provider can open it).
- **Tag read**: title/artist/album are extracted correctly.
- **Write**: a safe write test on a temporary copy succeeds **and** round-trips the written values (a write that reports success without persisting tags counts as failed).
- Individual files may still fail (e.g. an unusual M4A container); use `melody-sync metadata --write-test <file>` for the per-file truth.

## Fixtures

Fixtures live in `melody-sync-core/src/test/resources/fixtures/audio/<format>/` and are copied into temporary directories before any destructive test.

```text
fixtures/audio/
├── mp3/   with_tags.mp3, no_tags.mp3
├── m4a/   with_tags.m4a, no_tags.m4a
├── flac/  with_tags.flac, no_tags.flac
├── wav/   with_tags.wav, no_tags.wav
├── ogg/   with_tags.ogg, no_tags.ogg
├── opus/  with_tags.opus, no_tags.opus
└── aac/   no_tags.aac (kept to prove it is unsupported)
```

Tagged fixtures carry `title="Fixture Song"`, `artist="Fixture Artist"`, `album="Fixture Album"`.
Untagged fixtures must expose no title/artist/album (title falls back to the file name).

`FixtureCapabilityTest` verifies, per format: capability detection, read, tag extraction, safe write (on a copy), re-read and failure classification.

## Known Limitations

- **WAV**: read-only. JAudioTagger's WAV writer removes the LIST/INFO chunk and writes nothing back, reporting success — silent tag loss. Writes are refused (`TagWriteError.Unsupported`); reads work and trailing NUL values are normalized by the read layer. Write support should only be re-enabled behind a writer that genuinely persists LIST/INFO tags.
- **M4A**: some container layouts can fail to write even though the format is supported — `metadata --write-test` reports the per-file result.
- **AAC**: no tag container is read by the current stack; `.aac` files are treated as non-audio.

## Related Documents

- [Metadata Foundation](metadata-foundation.md)
- [Error Log](../project/ErrorLog.md)
- [Quick-Fix HUD research](../research/quick-fix-hud.md)
- [Documentation Index](../INDEX.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**
