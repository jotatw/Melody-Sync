# ADR-0004 — Local Database (SQLite via Exposed)

> Architecture Decision Record.

---

## Document Information

| Item             | Value                  |
|------------------|------------------------|
| Document ID      | ADR-0004               |
| Category         | Architecture           |
| Audience         | Developers             |
| Status           | Accepted               |
| Project Version  | v0.2.0-dev             |
| Template Version | 1.0                    |
| Last Updated     | 2026-07-31             |
| Maintainer       | João                   |

---

## Summary

Melody Sync will use **SQLite** as its local database, accessed through the **Exposed** ORM framework (JetBrains).

SQLite provides a zero-configuration, embedded relational database that stores the music library's metadata locally, eliminating the need to re-scan the entire library on every startup. Exposed provides a type-safe Kotlin DSL for queries without the overhead of a full JPA/Hibernate ORM.

---

## Context

The Python prototype reads metadata from audio files on every scan. For a small library this is acceptable, but as the library grows, a persistent cache becomes necessary for responsive searches, filtering, and sorting.

### Current Situation

- The scanner reads metadata from files every time the application starts.
- No persistent storage exists — statistics are calculated in-memory and discarded.
- The Python version used `pathlib` for file traversal and in-memory Python objects for data.

### Constraints

- Zero configuration for the end user (no server to install, no connection strings).
- Must work offline (the entire music library is local).
- The database file should live alongside the application or in a standard config directory (`~/.config/melody-sync/`).
- Must support cross-platform access (Linux primary, Windows/macOS secondary).

### Requirements

- Embedded database (no server process).
- ACID-compliant.
- Good performance for read-heavy workloads (searches, filters).
- Type-safe SQL access from Kotlin.
- Support for complex queries (joins, aggregations, full-text search for music titles).
- Portable across platforms.

---

## Decision

Use **SQLite** accessed through **Exposed** (JetBrains' Kotlin SQL framework).

### Why SQLite

- Zero configuration — a single file on disk.
- Embedded in the application process — no server to manage.
- ACID-compliant, production-ready.
- Excellent read performance.
- Cross-platform (runs on every OS).
- Used by millions of applications (Firefox, Chrome, iOS, Android).

### Why Exposed (not raw JDBC)

- **Type-safe DSL:** Queries are validated at compile time, not at runtime.
- **Kotlin-idiomatic:** Table definitions as Kotlin objects, column types as Kotlin types.
- **Lightweight:** No JPA-style entity management, no lazy-loading proxies, no session management.
- **Dual mode:** Supports both DSL (type-safe queries) and DAO (active record-like) patterns.
- **JetBrains-maintained:** Active development, Kotlin-first philosophy.

### Library Selection

| Component | Choice | Purpose |
|-----------|--------|---------|
| Database engine | SQLite 3.46+ | Embedded storage |
| SQL driver | `sqlite-jdbc` (Xerial) | JDBC driver for SQLite |
| ORM / DSL | Exposed 0.61.0 | Type-safe SQL DSL |
| Connection pool | HikariCP | Lightweight pooling |
| Migration | Exposed migrations | Schema versioning |

---

## Alternatives Considered

### Raw SQLite JDBC (without ORM)

**Advantages**

- Maximum control over queries.
- No abstraction overhead.
- Full SQL feature access.

**Disadvantages**

- **No type safety** — SQL strings assembled at runtime.
- **Manual mapping** — every query result must be manually mapped to Kotlin objects.
- **No migration system** — schema changes must be managed manually.

**Why rejected:** Exposed provides significant productivity gains without enough overhead to matter at Melody Sync's scale.

---

### H2 Database

**Advantages**

- Embedded Java SQL database.
- In-memory mode for testing.

**Disadvantages**

- Less widely used than SQLite for single-user desktop applications.
- No clear advantage over SQLite for this use case.
- Java-specific; Kotlin Multiplatform support is weaker.

**Why rejected:** SQLite is the standard for embedded databases on desktop and mobile. No reason to choose H2 over SQLite for Melody Sync.

---

### Room (Android's ORM)

**Advantages**

- Official Android ORM from Google.
- Compile-time query validation via `@Query` annotations.
- Good Jetpack Compose integration.

**Disadvantages**

- **Android-only** — does not run on desktop JVM without workarounds.
- Annotation-based — less idiomatic Kotlin than Exposed DSL.
- Requires KSP (Kotlin Symbol Processing) annotation processing.

**Why rejected:** Does not run on standard JVM, making it incompatible with the current desktop-focused architecture. Exposed works on both JVM and KMP targets.

---

### No Database (Keep In-Memory)

**Advantages**

- Zero configuration.
- No migration management.
- Simpler architecture.

**Disadvantages**

- **Full re-scan on every startup** — unacceptable for libraries with 10k+ songs.
- **No state persistence** — assessments, tags, playlists lost on restart.
- **No query performance** — filtering/sorting requires scanning all in-memory objects.

**Why rejected:** A persistent database is required for the project to evolve beyond a simple one-shot scanner.

---

## Consequences

### Positive

- **Instant startup:** Metadata is available from the database; full re-scans are optional.
- **Rich queries:** SQLite + Exposed enables complex queries (songs by genre, albums by year, full-text search).
- **Schema migrations:** Exposed's migration system lets the database evolve alongside the application.
- **Type-safe queries:** Compile-time validation prevents malformed SQL.

### Negative

- **Database maintenance:** Schema migrations must be written and tested.
- **Extra dependency:** Increases project complexity and binary size.
- **Stale data:** The database can become out of sync if files are modified outside Melody Sync. Must handle cache invalidation.

### Risks

- **Schema drift:** Database model diverges from code. **Mitigation:** Use Exposed migrations with versioned schema files.
- **File system changes:** Music files moved/deleted externally. **Mitigation:** Implement a "sync" mode that detects changes without full re-scan.
- **Corruption:** SQLite is robust, but unexpected crashes could corrupt the database. **Mitigation:** Enable WAL (Write-Ahead Logging) mode for crash safety.

---

## Implementation Notes

- Database file location: `~/.config/melody-sync/library.db` (Linux), `~/Library/Application Support/MelodySync/library.db` (macOS), `%APPDATA%/MelodySync/library.db` (Windows).
- Enable **WAL mode** for better concurrent read performance.
- Enable **foreign keys** for referential integrity.
- Schema migrations live in `melody-sync-core/src/commonMain/kotlin/com/melodysync/database/migrations/`.
- Full-text search (FTS5) for music title/artist/album searches.
- The database is optional for CLI `scan` command but required for the GUI.
- HikariCP pool size: 1 (single-user application).
- **Exposed version pinned to 0.61.0:** the 1.x line (`org.jetbrains.exposed.v1.*`) restructured its packages and has sparse documentation; the classic `org.jetbrains.exposed.sql.*` API is stable and well-documented. Revisit when 1.x documentation matures.
- **SQLite in-memory (`:memory:`) does not work with Exposed:** each connection gets a separate in-memory database, so schema created on one connection is invisible to others. Tests use a temporary file database instead.

---

## References

- [SQLite Documentation](https://www.sqlite.org/docs.html)
- [Exposed ORM](https://github.com/JetBrains/Exposed)
- [Xerial SQLite JDBC](https://github.com/xerial/sqlite-jdbc)
- ADR-0002 — Programming Language (Kotlin)

---

## Related Documents

- `docs/architecture/music-library-domain.md` — planned domain model document
- `docs/INDEX.md`

---

## Revision History

| Version   | Date       | Description                               |
|-----------|------------|-------------------------------------------|
| v0.1.0    | 2026-07-15 | Initial placeholder (SQLite considered)   |
| v0.2.0    | 2026-07-31 | Decision confirmed: SQLite via Exposed    |

Record only meaningful revisions.

---

This document follows the Melody Sync Documentation Standard.

**End of Document**