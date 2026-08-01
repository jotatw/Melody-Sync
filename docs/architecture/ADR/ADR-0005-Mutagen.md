# ADR-0005 — Audio Metadata Library (JAudioTagger)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0005               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync will use **JAudioTagger** for reading and writing audio file metadata. It is the most comprehensive audio tagging library available in the Java/Kotlin ecosystem, supporting all major audio formats.

---

## Context

Melody Sync's core functionality depends on extracting metadata from audio files: title, artist, album, duration, bitrate, sample rate, codec, and embedded cover art.

### Current Situation

- The Python prototype uses Mutagen, the gold standard for audio metadata in Python.
- The Kotlin migration (ADR-0002) requires a JVM-compatible replacement.
- The library must support the same audio formats as the Python prototype: MP3, FLAC, Ogg Vorbis, Opus, M4A/AAC, WAV.

### Constraints

- Must be available in the JVM ecosystem (Java or Kotlin).
- Must support reading metadata from all common audio formats.
- Should support writing metadata (tag editing) for future features.
- Must handle files without tags gracefully (common in personal libraries).
- Should be stable and reliable — the library will read potentially thousands of files.

---

## Decision

Use **JAudioTagger** (specifically the `com.github.goxr3plus:jaudiotagger:2.2.7` fork) as the metadata extraction library.

### Why JAudioTagger

- The **most complete** audio tagging library in the JVM ecosystem.
- Supports all formats needed: MP3, FLAC, Ogg Vorbis, MP4/M4A, WAV, WMA.
- Handles ID3v1, ID3v2.2, ID3v2.3, ID3v2.4, Vorbis Comments, and MP4 metadata atoms.
- Supports reading embedded cover art (album art extraction).
- Mature codebase with years of real-world use.

### Library Selection

| Component | Version | Purpose |
|-----------|---------|---------|
| JAudioTagger | 2.2.7 | Metadata reading/writing |

The fork from `goxr3plus` is used instead of the original Bitbucket repository because it is published on JitPack/Maven Central and includes Java 11+ module support.

---

## Alternatives Considered

### mp3agic

**Advantages**

- Clean API.
- Lightweight.
- Well-known (1.2k stars on GitHub).

**Disadvantages**

- **MP3 only** — does not support FLAC, Ogg, M4A, or WAV.
- **No longer actively maintained** — the author explicitly states the repo is no longer maintained.

**Why rejected:** Format support is insufficient for a library organizer that needs to handle multiple audio formats.

---

### Apache Tika

**Advantages**

- Detects metadata from hundreds of file formats.
- Mature Apache project.
- Comprehensive metadata extraction.

**Disadvantages**

- **Heavy dependency** — designed for document analysis, not audio-focused.
- **No tag writing** — read-only metadata extraction.
- **Over-engineered** — pulls in many transitive dependencies.

**Why rejected:** Too heavy for the specific need of reading audio tags; designed for a much broader use case.

---

### FFmpeg / FFprobe (Subprocess)

**Advantages**

- Comprehensive metadata support.
- Handles virtually every audio format.
- Extremely well-tested.

**Disadvantages**

- **External binary dependency** — ffprobe must be installed on the user's system.
- **Process spawning overhead** — each file requires a subprocess invocation.
- **Fragile** — parsing ffprobe JSON output is brittle.
- **No tag writing** — would require a separate tool.

**Why rejected:** The external dependency and process overhead make this unsuitable for a self-contained desktop application.

---

### Custom ID3/Vorbis Parser

**Advantages**

- Full control over parsing.
- No external dependencies.
- Educational value.

**Disadvantages**

- **Extremely time-consuming** — implementing ID3v2.4 alone is a significant undertaking.
- **Error-prone** — edge cases in tag formats are notoriously difficult.
- **Format proliferation** — would need separate parsers for MP3, FLAC, Ogg, MP4.

**Why rejected:** Not a productive use of time for a personal project. The project should focus on library organization features, not reimplementing tag parsing.

---

## Consequences

### Positive

- **Comprehensive format support:** All common audio formats covered.
- **Read and write:** Future tag editing features are supported.
- **Embedded images:** Album art extraction is supported.
- **Battle-tested:** The library has been used in many projects over many years.

### Negative

- **Semi-maintained:** The original project is abandoned; the fork receives minimal updates.
- **No Kotlin-first API:** Java API requires null handling for optional metadata.
- **Potential bugs:** Edge cases in exotic tag configurations may surface.

### Risks

- **Library abandonment:** If JAudioTagger stops working with future JDK versions. **Mitigation:** The fork is small and could be maintained in-house if needed; consider wrapping all calls in an `AudioMetadataReader` interface to allow swapping implementations.
- **Unexpected format behavior:** Some files may have non-standard tags. **Mitigation:** Wrap the library in defensive code with fallbacks for missing or malformed metadata.
- **Performance with large files:** Reading tags from very large FLAC files may be slow. **Mitigation:** Read only the header portion of the file; do not scan the entire file.

---

## Implementation Notes

- Wrap JAudioTagger in a **MetadataReader** interface to allow future replacement of the underlying library without affecting the rest of the codebase.
- The scanner should parse metadata concurrently using coroutines (`Dispatchers.IO`).
- Handle null metadata gracefully — files without tags should still appear in the library with placeholder values (filename as title, "Unknown Artist", etc.).
- Cache parsed metadata in the database to avoid re-parsing on every startup (see ADR-0004).

Interface sketch:

```kotlin
interface MetadataReader {
    suspend fun read(filePath: Path): MetadataResult
}

data class MetadataResult(
    val title: String?,
    val artist: String?,
    val album: String?,
    val duration: Double,
    val bitrate: Int?,
    val sampleRate: Int?,
    val channels: Int?,
    val codec: String?,
    val coverArt: ByteArray?
)
```

---

## References

- [JAudioTagger (goxr3plus fork)](https://github.com/goxr3plus/jaudiotagger)
- [JAudioTagger (original Bitbucket)](https://bitbucket.org/ijabz/jaudiotagger)
- [Mutagen (Python reference)](https://mutagen.readthedocs.io/)
- ADR-0002 — Programming Language (Kotlin)

---

## Related Documents

- `docs/INDEX.md`

---

## Revision History

| Version   | Date       | Description                               |
|-----------|------------|-------------------------------------------|
| v0.1.0    | 2026-07-15 | Initial placeholder (Mutagen considered)  |
| v0.2.0    | 2026-07-31 | Decision revised: JAudioTagger            |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**