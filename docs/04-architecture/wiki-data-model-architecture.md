# System Architecture: Wiki Data Model

## Status

Draft for user acceptance. Derived from `docs/03-spec/wiki-data-model-spec.md`.

## Overview

- **Architecture summary:** This slice extends the existing Atlas metadata control plane with Wiki Foundation entities and read contracts. It keeps Wiki metadata in the Spring Boot/PostgreSQL path, while parser, converter, model, vector, storage, and search execution remain outside this slice and behind existing adapters.
- **Design objective:** Provide a stable data foundation for future Auto Wiki features while preserving current review-publish, graph, Ask, and Vue Wiki behavior.
- **Architectural style:** Layered Spring Boot metadata service with Vue API-backed UI, Flyway-managed PostgreSQL schema, DTO boundaries, and adapter isolation.

## Source Specification

- **Feature name:** Wiki Data Model
- **Scope summary:** Add Wiki page metadata fields and minimal folder/run/log/issue records; expose read APIs; show visible metadata in Vue; do not implement ingest, linkify, lint, production RBAC, or provider execution.

## Architectural Drivers

### Key Functional Drivers

- Space-scoped slug lookup for future generated Wiki pages.
- Additive `wiki_page` extension preserving current publish/list/id-detail APIs.
- Minimal folders, generation runs, logs, and issues as persistent foundations for future Auto Wiki slices.
- Vue Wiki tab display of new API-backed metadata with sample-safe fallback.

### Key Non-Functional Drivers

- Preserve source trace, confidence, and review status.
- Keep migrations compatible with V1-V9 seeded data and existing E2E.
- Avoid raw secrets, raw document content, provider payloads, private paths, and external cloud calls.
- Keep infrastructure engines behind Atlas adapter boundaries.

### Constraints And Assumptions

- Verified existing baseline: `ReviewPublishController` already exposes `POST /api/files/{fileId}/publish`, `GET /api/spaces/{spaceId}/wiki-pages`, and `GET /api/wiki-pages/{wikiPageId}`.
- Verified existing baseline: `WikiPage` currently stores published metadata but lacks slug, page type, richer refs, link refs, version, source mode, and refresh policy.
- [ASSUMPTION] Initial folder, generation run, log, and issue write behavior can be limited to repository/service support and sample-safe seed/tests; public API scope is read-minimal.
- `WEKNORA_ANALYSIS_DIR` was not set during this SDD pass, so no WeKnora analysis files were used.

## System Context

| Actor/System | Role |
|---|---|
| Vue Wiki tab | Consumes Wiki page/folder/run/log/issue metadata from Atlas API. |
| Review Publish service | Creates or republishes `wiki_page` records from approved files. |
| Graph and Ask surfaces | Continue consuming trusted source trace and published/approved evidence. |
| PostgreSQL/Flyway | Owns additive schema evolution for Wiki metadata. |
| Adapter layer | Remains the boundary for parser/converter/model/vector/storage/search engines; not invoked by this slice. |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│  Users                                                       │
│  Knowledge User · SME Reviewer · Delivery Lead               │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / local dev
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Vue Product UI                                              │
│  Wiki tab · Processing Center · Graph · Ask fallback safety   │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON ApiEnvelope
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Atlas Metadata API                                          │
│  Review Publish API · Wiki Metadata API                      │
├──────────────────────────────────────────────────────────────┤
│  Wiki Domain Services                                        │
│  Page metadata · Folder reads · Run/log/issue reads          │
├──────────────────────────────────────────────────────────────┤
│  JPA Repositories · DTO Mappers · User-safe errors           │
└──────────────┬───────────────────────────────────────────────┘
               │ JDBC
               ▼
┌──────────────────────────────────────────────────────────────┐
│  PostgreSQL / Flyway                                         │
│  wiki_page extension · wiki_folder · run/log/issue tables    │
└──────────────────────────────────────────────────────────────┘

Parser / converter / model / vector / storage / search engines remain behind
existing Atlas adapters and are not called by this slice.
```

## Layer Summary

- **Presentation layer:** Vue renders API-backed Wiki metadata and retains deterministic sample fallback.
- **API layer:** Spring controllers expose page list/detail/slug and minimal folder/run/log/issue read contracts using `ApiEnvelope`.
- **Domain layer:** Wiki services validate scope, preserve compatibility defaults, and map entities to DTOs.
- **Persistence layer:** Flyway adds columns and tables; repositories query space-scoped Wiki records.
- **Adapter boundary:** No parser, converter, model, vector, storage, or search execution is introduced.

## Component Breakdown

### Frontend Components

- **Wiki tab metadata view:** Displays slug, type, aliases, refs, links, version, source mode, and refresh policy.
- **Fallback Wiki sample provider:** Remains deterministic and sample-safe when API data is empty or unavailable.
- **API client/types:** Adds typed Wiki page, folder, run, log, and issue shapes.

### Backend Services

- **Review Publish service:** Continues publishing approved files; supplies defaults for new `wiki_page` fields.
- **Wiki metadata service:** Reads pages by id/list/slug and reads folders, generation runs, logs, and issues.
- **DTO mappers:** Convert entities to safe response records without leaking raw content.

### Persistence

- **`wiki_page`:** Extended page metadata and compatibility fields.
- **`wiki_folder`:** Minimal hierarchy.
- **`wiki_generation_run`:** Future generation/refresh run metadata.
- **`wiki_log_entry`:** Safe lifecycle event log.
- **`wiki_page_issue`:** Future issue/lint finding records.

## Data Architecture

| Entity | Description | Key Attributes |
|---|---|---|
| WikiPage | Published or future generated Wiki page metadata. | slug, pageType, refs, links, version, sourceMode, refreshPolicy, reviewStatus |
| WikiFolder | Space-scoped Wiki grouping. | spaceId, parentFolderId, slug, name |
| WikiGenerationRun | Future generation/refresh run metadata. | status, sourceMode, refreshPolicy, counts, timestamps |
| WikiLogEntry | Safe lifecycle event. | eventType, actor, message, safe metadata |
| WikiPageIssue | Future maintenance issue. | issueType, severity, status, evidence refs |

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/spaces/{spaceId}/wiki-pages` | Vue, tests | Existing page list with new fields. |
| `GET /api/wiki-pages/{wikiPageId}` | Vue, tests | Existing page detail with new fields. |
| `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}` | Future Wiki navigation | Space-scoped lookup. |
| `GET /api/spaces/{spaceId}/wiki-folders` | Vue/future navigation | Minimal folder tree/list. |
| `GET /api/spaces/{spaceId}/wiki-generation-runs` | Delivery lead/admin UI | Safe run inspection. |
| `GET /api/wiki-pages/{wikiPageId}/logs` | SME/admin UI | Safe page lifecycle events. |
| `GET /api/wiki-pages/{wikiPageId}/issues` | SME/admin UI | Safe issue inspection. |

## Security / Reliability / Observability

- No production auth/RBAC is implemented; existing trusted internal/local API posture remains until a security slice.
- User-safe errors use existing global exception conventions.
- Logs/issues contain safe metadata only.
- Migration must be additive and preserve seeded mock data.
- Completion report must not claim Auto Wiki ingest or production readiness.

## Risks / Tradeoffs

| # | Risk / Tradeoff | Notes |
|---|---|---|
| 1 | Storing refs as structured JSON vs arrays | JSON gives future flexibility; DTOs must keep shapes stable. |
| 2 | New support tables before full workflows | Useful foundation, but docs must be clear that no generation/lint engine exists yet. |
| 3 | Slug defaults for legacy pages | Defaults must be deterministic and collision-safe per space. |

## Open Questions

- OQ-WIKI-DATA-MODEL-001: Future generated-page initial state.
- OQ-WIKI-DATA-MODEL-002: Future source freshness checksum shape.
- OQ-WIKI-DATA-MODEL-003: Future folder ordering model.
