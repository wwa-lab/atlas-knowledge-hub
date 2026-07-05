# Detailed Design: Wiki Data Model

## Status

Draft for user acceptance. Product code remains blocked until acceptance.

## Overview

This design turns the `wiki-data-model` specification into implementation-ready module, API, data, UI, validation, and testing decisions. It stays within metadata foundation scope and does not implement Auto Wiki ingest, linkify/lint, production RBAC, or provider execution.

## Source Architecture

- `docs/03-spec/wiki-data-model-spec.md`
- `docs/04-architecture/wiki-data-model-architecture.md`
- `docs/04-architecture/wiki-data-model-data-flow.md`
- `docs/04-architecture/wiki-data-model-data-model.md`

## Design Assumptions

- Existing review-publish endpoints and Vue API-backed Wiki flow remain the compatibility baseline.
- Flyway migration will be the only schema mutation path.
- DTOs should use immutable response records and safe collection copies.
- JSON reference fields should be represented as lists of small DTO records in API responses, not raw JSON strings.
- Initial implementation may expose read-only folder/run/log/issue APIs; write workflows for Auto Wiki are deferred.

## Design Scope

In scope:

- Additive migration and domain/repository/DTO/API changes.
- Backward-compatible publish/list/id-detail behavior.
- New slug lookup, folder list, generation run list, page logs, and page issues reads.
- Vue type/client updates and Wiki tab metadata rendering.
- Contract/unit/E2E regression coverage and safety scans.

Out of scope:

- Ingest pipeline, linkify/lint engine, refresh worker, folder editing UI, production auth/RBAC, external providers, real company documents.

## Module Design

### Backend Wiki Domain

- Extend `WikiPage` with fields from the data model.
- Add minimal entities for `WikiFolder`, `WikiGenerationRun`, `WikiLogEntry`, and `WikiPageIssue`.
- Keep entities scoped by `spaceId`.
- Use factory methods/defaulting helpers for publish-created Wiki pages:
  - slug derived from `markdownPath`, title, or id.
  - `pageType=SOURCE_SUMMARY`
  - `sourceMode=PUBLISHED_FILE`
  - `refreshPolicy=MANUAL`
  - `version=1`
  - empty arrays/lists for aliases and links.

### Backend Repositories

- Extend `WikiPageRepository` with space-scoped slug lookup.
- Add repositories for folders, generation runs, log entries, and issues.
- Queries must scope by `spaceId` or by a page that is verified to belong to the requested space.

### Backend Service

- Extend the existing review-publish service or introduce a narrow Wiki metadata service for read paths.
- Preserve existing publish behavior and response compatibility.
- Log/issue/run read methods return bounded results and safe DTOs.
- Slug lookup validates the space exists and returns not found when the slug belongs to another space.

### Backend DTOs And Mapping

- Extend `WikiPageResponse` with:
  - `slug`
  - `pageType`
  - `aliases`
  - `sourceRefs`
  - `chunkRefs`
  - `inLinks`
  - `outLinks`
  - `version`
  - `sourceMode`
  - `refreshPolicy`
  - optional `folderId`
- Add response records:
  - `WikiReferenceResponse(type, id, label, locator)`
  - `WikiFolderResponse(id, spaceId, parentFolderId, slug, name, description, sortOrder)`
  - `WikiGenerationRunResponse(...)`
  - `WikiLogEntryResponse(...)`
  - `WikiPageIssueResponse(...)`
- Do not expose raw JSON strings in public response bodies.

### Frontend API And UI

- Extend `ApiWikiPage` with all new page metadata fields.
- Add client helpers for folder/run/log/issue reads when UI needs them.
- Update Wiki tab to render:
  - slug and page type near the page title.
  - aliases as small labels or fallback "none".
  - source refs and chunk refs as safe IDs/labels.
  - in/out links as counts plus sample IDs when short.
  - version, source mode, and refresh policy in metadata badges.
- Preserve fallback sample pages when API pages are empty.

## API / Interface Design

The API guide at `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.md` is the implementation contract. Summary:

- Existing:
  - `POST /api/files/{fileId}/publish`
  - `GET /api/spaces/{spaceId}/wiki-pages`
  - `GET /api/wiki-pages/{wikiPageId}`
- New:
  - `GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}`
  - `GET /api/spaces/{spaceId}/wiki-folders`
  - `GET /api/spaces/{spaceId}/wiki-generation-runs`
  - `GET /api/wiki-pages/{wikiPageId}/logs`
  - `GET /api/wiki-pages/{wikiPageId}/issues`

## Data Design

- Use one additive migration after V9, expected as `V10__wiki_data_model.sql` unless another migration already exists at implementation time.
- `wiki_page` new columns must have safe defaults for existing rows.
- JSON columns store safe metadata and should default to empty arrays/objects as appropriate.
- Add indexes for `(space_id, slug)`, folder by space, run by space/time, log by page/time, and issue by page/status.

## UI / User Flow Design

1. User enters a Knowledge Space.
2. Vue loads API Wiki pages via existing flow.
3. If API pages exist, the Wiki page list and detail render new metadata.
4. If API pages are empty or unavailable, fallback sample-safe pages render.
5. Graph and Ask tabs continue using existing API-backed evidence flows.

Empty states:

- No API Wiki pages: show existing safe empty/fallback state.
- No aliases/links/refs: show concise "none" or count zero, not missing UI.

## Validation And Error Handling

| Input/Field | Rule |
|---|---|
| `slug` | Lowercase URL-safe segment; unique per space; no slash traversal. |
| `pageType` | Must be one of spec enum values. |
| `sourceMode` | Must be one of spec enum values. |
| `refreshPolicy` | Must be one of spec enum values. |
| `version` | Integer >= 1. |
| JSON refs | Safe IDs/labels only; no raw document text or private paths. |
| Folder parent | Parent must be in same space. |

Error behavior:

- Unknown resource -> safe `NOT_FOUND`.
- Duplicate slug -> safe `CONFLICT` for future writes; migration must avoid duplicates.
- Invalid enum/path -> safe `VALIDATION_ERROR`.

## Testing Considerations

- Backend API contract tests for:
  - page response new fields.
  - slug lookup scoped by space.
  - folder list.
  - generation run list.
  - page logs.
  - page issues.
  - existing publish/list/id-detail compatibility.
- Backend repository/migration tests for V1-V10 compatibility and defaults.
- Frontend unit tests for API page mapping and fallback.
- Playwright checks for Wiki metadata display and existing review-publish/graph/Ask flows.
- Safety scans for secrets/private paths and external network/provider calls.

## Risks / Design Tradeoffs

| Risk | Mitigation |
|---|---|
| Large JSON fields become unclear contracts | Public DTOs use typed reference records. |
| Supporting tables imply workflows are complete | UI/docs label this as foundation metadata only. |
| Slug backfill collisions | Migration must use deterministic conflict suffixing per space. |

## Open Questions

- OQ-WIKI-DATA-MODEL-001: Future generated-page initial status.
- OQ-WIKI-DATA-MODEL-002: Future checksum/freshness metadata.
- OQ-WIKI-DATA-MODEL-003: Future folder ordering behavior.
