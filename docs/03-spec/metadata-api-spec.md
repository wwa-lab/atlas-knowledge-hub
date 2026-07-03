# Specification: Metadata API

## Status

Draft. Phase 2 (Backend metadata API + persistence). Behavior source of truth for the `metadata-api` slice. Derived from `docs/02-user-stories/metadata-api-stories.md`.

## Source Documents

- `docs/01-requirements/metadata-api-requirements.md`
- `docs/02-user-stories/metadata-api-stories.md`
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` — request/response contract (companion to this spec).
- `docs/04-architecture/metadata-api-data-model.md` — persisted schema and enums.
- `docs/batch-processing-design.md` — authoritative `FileStatus` set.
- `docs/markdown-standard.md` — trace / confidence / review metadata.
- FE consumer baseline: `frontend/src/types.ts`, `frontend/src/data/atlasMock.ts`, `frontend/public/atlas-prototype.html`.

## Scope

An internal Spring Boot REST metadata service backed by PostgreSQL/Flyway. It persists and serves Knowledge Space, batch, file-item, source-chunk, and review metadata, and creates (but does not serve) the wiki-page/graph tables. Metadata-only: **no** conversion, parsing, model, vector, storage, upload-bytes, auth, or graph/Ask behavior in this slice.

## Constraints

- **Metadata-only / adapter-neutral:** the service must not call or embed any converter/parser/model/vector/storage engine; no file bytes are read (REQ-MA-013).
- **No hardcoded DB / no raw secrets:** datasource is config-driven; no raw credentials, private endpoints, or real company data in source, config, logs, or seed (REQ-MA-012).
- **Trace preserved:** every persisted/returned file and chunk carries `source_path` (relative), `confidence`, and `review_status`; generated content is never `APPROVED` by default (REQ-MA-011).
- **Layered:** controller → application service → repository → migration; orchestration out of controllers; empty adapter seam reserved (REQ-MA-008).
- **Contract-first:** the API guide and data model must be accepted before implementation (REQ-PROD-073).

## API Surface (behavioral summary)

Full request/response shapes live in the API implementation guide. This spec fixes the observable behavior.

| Method + Path | Behavior | Story |
|---|---|---|
| `GET /api/spaces` | Paginated list of Knowledge Space cards; supports `page`, `size`, `status` filter. | US-MA-001 |
| `GET /api/spaces/{spaceId}` | Full space detail; `404` envelope if unknown. | US-MA-001 |
| `POST /api/spaces` | Create space; server sets `id`, `status` default, timestamps; `400` on invalid DTO. | US-MA-002 |
| `GET /api/spaces/{spaceId}/batches` | Paginated batches for a space with derived metrics. | US-MA-003 |
| `GET /api/batches/{batchId}` | Batch detail with metrics + processing status. | US-MA-003 |
| `POST /api/spaces/{spaceId}/batches` | Create batch + file items from inventory metadata; no bytes/engines touched. | US-MA-004 |
| `GET /api/batches/{batchId}/files` | Paginated file items with status/confidence/review/trace; supports `status` filter. | US-MA-005 |
| `GET /api/files/{fileId}` | File-item detail. | US-MA-005 |
| `GET /api/files/{fileId}/chunks` | Source chunks for a file (trace records). | US-MA-005 |
| `POST /api/files/{fileId}/reviews` | Append a review record; update target `review_status`. | US-MA-006 |
| `GET /api/files/{fileId}/reviews` | Append-only review history in chronological order. | US-MA-006 |

Wiki-page, graph-node, and graph-edge tables exist (migrated + seeded) but expose **no endpoints** in this slice (REQ-MA-014).

## Response Envelope

All responses use one envelope:

```json
{ "success": true, "data": { }, "error": null, "meta": { "page": 0, "size": 20, "total": 42 } }
```

- `success`: boolean.
- `data`: payload on success, `null` on error.
- `error`: `{ "code": "VALIDATION_ERROR", "message": "user-safe text", "fields": { "name": "must not be blank" }, "timestamp": 1750000000000, "path": "/api/spaces" }` on failure, else `null`. `timestamp` (epoch millis) and `path` appear on error responses only, mirroring Spring's default error body; success responses stay lean.
- `meta`: present on list endpoints (pagination). `total` reflects the full filtered count.

## Validation Rules

- `POST /spaces`: `name` required (non-blank, ≤200); `type` ∈ {`document`,`faq`}; `index_strategy` ∈ {`rag`,`wiki`}; unknown fields rejected or ignored per DTO.
- `POST /batches`: `source_kind` ∈ {`folder`,`zip`}; each inventory file needs `source_path` (relative), `source_type`, `status` ∈ allowed `FileStatus`, `confidence` ∈ [0,1].
- `POST /reviews`: `action` ∈ {`APPROVE`,`NEED_FIX`,`OCR_REQUIRED`}; `reviewer` required; `affected_chunks` optional list of chunk ids.
- Any `source_path` containing an absolute path, `..` traversal, or a drive/host prefix is rejected as `400`.
- Invalid input never reaches persistence; the response is a `400` `VALIDATION_ERROR` envelope with field details.

## State Model

### File status (persisted)

Exactly the allowed set from `docs/batch-processing-design.md`:
`NEW`, `UPLOADED`, `PDF_CONVERTED`, `PDF_CONVERT_FAILED`, `MARKDOWN_GENERATED`, `OCR_REQUIRED`, `LOW_CONFIDENCE`, `REVIEW_REQUIRED`, `APPROVED`, `PUBLISHED`, `FAILED`, `UNSUPPORTED`. No new statuses may be introduced by this slice.

### Review status (persisted, per target)

`REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`, `PUBLISHED` (REQ-PROD-030).

Review action → resulting target `review_status`:

```
APPROVE       → APPROVED
NEED_FIX      → NEED_FIX
OCR_REQUIRED  → OCR_REQUIRED
```
`PUBLISHED` is set only by a later publish slice, never by this metadata service. Generated/low-confidence items persist as `REVIEW_REQUIRED` and are never auto-`APPROVED`.

### Batch metrics (derived)

`total`, `pdfConverted`, `markdownGenerated`, `reviewRequired`, `failed`, `unsupported` are computed from the batch's file items on read — not stored as an independent, drift-prone column set (single source of truth).

## Error Behavior

| Situation | HTTP | Envelope error code |
|---|---|---|
| Unknown space/batch/file id | 404 | `NOT_FOUND` |
| Invalid/malformed DTO or path | 400 | `VALIDATION_ERROR` (with `fields`) |
| Duplicate/conflicting create | 409 | `CONFLICT` |
| Unexpected server fault | 500 | `INTERNAL_ERROR` (generic message; details logged server-side only, no stack/secret leak) |

Error messages are user-safe: no stack traces, SQL, secrets, internal hostnames, or private absolute paths (REQ-MA-009, REQ-PROD-077).

## Persistence & Migration Behavior

- Schema is created only by versioned Flyway migrations; no runtime auto-DDL for shared environments (`spring.jpa.hibernate.ddl-auto=validate`).
- One seed migration inserts mock/sample rows aligned to the FE baseline mock (spaces, one+ batch, file items, chunks, a review record, plus sample wiki-page/graph rows). No real company data or secrets.
- Migrations are immutable and version-ordered; `mvn verify` runs Flyway validation.

## Adapter & Future-Real Notes (documented, not implemented)

- An empty adapter package/interface seam is reserved (e.g. `adapter/` with no concrete engine). Real conversion/parsing/storage arrives in Phase 3 adapter slices and will populate file/chunk metadata through this service, not bypass it.
- Deterministic metadata precedes any future LLM enrichment; LLM-derived content stays `REVIEW_REQUIRED`.

## Acceptance Matrix

| Check | Requirement | Observable result |
|---|---|---|
| AC-MA-01 | REQ-MA-001, 002, 010 | `GET /api/spaces` returns a paginated envelope of seeded spaces; detail returns full metadata; unknown id → 404 envelope. |
| AC-MA-02 | REQ-MA-002, 009 | `POST /api/spaces` persists a space with server timestamps + default status; invalid payload → 400 with field errors. |
| AC-MA-03 | REQ-MA-003, 010 | `GET /api/spaces/{id}/batches` and `GET /api/batches/{id}` return batches with metrics derived from file items. |
| AC-MA-04 | REQ-MA-003, 004, 013 | `POST /api/spaces/{id}/batches` persists batch + file items from inventory with no engine call and no byte read. |
| AC-MA-05 | REQ-MA-004, 011 | File items persist/return exact `FileStatus` values, relative `source_path`, `confidence`, `review_status`; generated items are `REVIEW_REQUIRED`. |
| AC-MA-06 | REQ-MA-005, 011 | `GET /api/files/{id}/chunks` returns source chunks with trace + confidence + review status. |
| AC-MA-07 | REQ-MA-006, 011 | `POST /api/files/{id}/reviews` appends an immutable record and updates target review status; history is chronological. |
| AC-MA-08 | REQ-MA-007, 015 | Flyway creates all 8 initial tables and seeds mock rows; `mvn verify` passes Flyway validation. |
| AC-MA-09 | REQ-MA-009, 077 | All error envelopes are user-safe (no stack/SQL/secret/private path); path traversal in `source_path` → 400. |
| AC-MA-10 | REQ-MA-008, 012, 013 | Layered structure holds; datasource is config-driven; no converter/parser/model/vector/storage engine is called; no raw secret in source/config/logs. |
| AC-MA-11 | REQ-MA-014 | Wiki-page/graph tables exist and are seeded but expose no endpoint; the omission is recorded in traceability. |

## Open Questions

- Write paths (`POST`) enabled here vs. read-only + seed only (default: enable space/batch/review create).
- FE cutover to the API in this slice vs. a follow-up (default: follow-up; this slice ships API + contract tests).
- `review_status` divergence: the persisted set follows REQ-PROD-030 (`NEED_FIX`/`OCR_REQUIRED`/`PUBLISHED`) and is wider than the FE `ReviewStatus` (which has `REJECTED`). Default mapping: legacy FE `REJECTED` → `NEED_FIX`; realigning the FE type is deferred to T-MA-014.
