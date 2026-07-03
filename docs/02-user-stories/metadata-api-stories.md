# User Stories: Metadata API

## Status

Draft. Phase 2. Slice `metadata-api`. Derived from `docs/01-requirements/metadata-api-requirements.md`.

## Actors

- **Frontend app** — the Vue client that today reads in-memory mock and will read/write metadata over HTTP.
- **Knowledge base admin** — creates spaces and batches; expects persistence and consistent reads.
- **SME reviewer** — appends review records against files/chunks.
- **Backend engineer (Codex)** — implements against a stable contract and migrations.
- **Platform operator** — points the service at a config-driven datasource without code changes.

## Stories

### US-MA-001 — Read Knowledge Spaces from the API
As the **frontend app**, I want to fetch the list and detail of Knowledge Spaces from a stable endpoint so that the home library and space shell render from persisted metadata instead of in-memory mock.

**Acceptance (Given/When/Then)**
- Given seeded spaces, When `GET /api/spaces` is called, Then it returns a paginated envelope of space cards with `name`, `description`, `owner`, `status`, and counts.
- Given a valid `spaceId`, When `GET /api/spaces/{spaceId}` is called, Then it returns full space detail metadata.
- Given an unknown `spaceId`, When the detail endpoint is called, Then it returns a `404` user-safe error envelope (no stack trace, no internal path).

Traces: REQ-MA-001, REQ-MA-002, REQ-MA-010.

### US-MA-002 — Create a Knowledge Space
As a **knowledge base admin**, I want to create a Knowledge Space with type and index strategy so that new spaces persist with server-assigned timestamps and status.

**Acceptance**
- Given a valid create payload (`name`, `type`, `index_strategy`), When `POST /api/spaces` is called, Then a space is persisted with `created_at`/`updated_at` set server-side and `status` defaulted, returned in the success envelope.
- Given an invalid payload (missing `name` or bad `type`), When the endpoint is called, Then it returns `400` with field-level, user-safe validation errors.

Traces: REQ-MA-002, REQ-MA-009, REQ-MA-010.

### US-MA-003 — Read batches for a space with metrics
As the **frontend app**, I want to list a space's batches and open a batch detail so that the Documents tab shows persisted batch metrics and status.

**Acceptance**
- Given a space with batches, When `GET /api/spaces/{spaceId}/batches` is called, Then it returns paginated batches with derived metrics (total/pdfConverted/markdownGenerated/reviewRequired/failed/unsupported).
- Given a `batchId`, When `GET /api/batches/{batchId}` is called, Then it returns batch detail with metrics and processing status.
- Given batch metrics, Then they are computed from persisted file items, not stored as an independent, drift-prone copy.

Traces: REQ-MA-003, REQ-MA-004, REQ-MA-010, REQ-MA-011.

### US-MA-004 — Create a batch from pre-computed inventory
As a **knowledge base admin**, I want to create a batch by submitting inventory metadata so that a batch and its file items persist without the service touching real bytes or parsers.

**Acceptance**
- Given a `POST /api/spaces/{spaceId}/batches` payload with `source_kind` and inventory file metadata, When called, Then a batch and file items persist with statuses from the allowed `FileStatus` set.
- Given the payload, When persisted, Then no converter/parser/storage engine is invoked (metadata-only) and no file bytes are read.
- Given a file item with generated/low-confidence status, Then its `review_status` persists as `REVIEW_REQUIRED`, never `APPROVED` by default.

Traces: REQ-MA-003, REQ-MA-004, REQ-MA-011, REQ-MA-013.

### US-MA-005 — Read file items and their source-chunk trace
As a **developer/reviewer**, I want to read a batch's file items and each file's source chunks so that source trace and confidence survive persistence and are visible in the UI.

**Acceptance**
- Given a batch, When `GET /api/batches/{batchId}/files` is called, Then it returns file items with `source_path` (relative), `status`, `confidence`, `review_status`, and artifact-path metadata.
- Given a file item, When `GET /api/files/{fileId}/chunks` is called, Then it returns source chunks with `source_file`, `page`/`section`, `confidence`, and `review_status`.
- Given any returned path, Then it is relative — never a private absolute path.

Traces: REQ-MA-004, REQ-MA-005, REQ-MA-011, REQ-MA-012.

### US-MA-006 — Append a review record
As an **SME reviewer**, I want to append a review action against a file or chunk so that review history is auditable and the target's review status reflects the latest decision.

**Acceptance**
- Given a valid review payload (`action` in Approve/Need Fix/OCR Required, `reviewer`, optional `comment`, `affected_chunks`), When `POST /api/files/{fileId}/reviews` is called, Then a review record is appended immutably with a server timestamp.
- Given the append, Then the target's `review_status` updates per the allowed review status set.
- Given `GET /api/files/{fileId}/reviews`, Then it returns the append-only history in chronological order.

Traces: REQ-MA-006, REQ-MA-009, REQ-MA-011.

### US-MA-007 — Migrate and seed the schema deterministically
As a **backend engineer**, I want Flyway to create the initial schema for all initial entities and seed mock/sample metadata so that a fresh database matches the FE baseline without manual steps.

**Acceptance**
- Given a clean database, When the app starts (or `mvn flyway:migrate` runs), Then all initial tables (`space`, `batch`, `file_item`, `source_chunk`, `wiki_page`, `review_record`, `graph_node`, `graph_edge`) are created by versioned migrations.
- Given the seed migration, Then it inserts mock/sample rows aligned with the FE baseline mock — no real company data, no secrets.
- Given `mvn verify`, Then Flyway migration validation passes and migrations are immutable/versioned.

Traces: REQ-MA-007, REQ-MA-012, REQ-MA-015.

### US-MA-008 — Config-driven datasource, no leaked secrets
As a **platform operator**, I want the datasource configured via environment/config so that no database is hardcoded as the only impl and no raw credentials live in source.

**Acceptance**
- Given the service config, When inspected, Then the datasource URL/credentials come from externalized config (env/profile), not hardcoded literals.
- Given any response or log, Then it never contains raw secrets, credentials, private endpoints, or internal stack traces.
- Given the codebase, Then no converter/parser/model/vector/storage engine is called directly (adapter seam only).

Traces: REQ-MA-008, REQ-MA-012, REQ-MA-013.

## Out of Scope (this slice)

- Graph query, Ask/RAG, wiki-body rendering, publish state machine (only metadata tables created).
- Real upload/parse/convert; authentication/RBAC; model/engine/member/registration APIs.

## Dependencies

- Accepted `metadata-api` data model and API implementation guide (REQ-PROD-073 gate).
- Enum/field alignment with `docs/batch-processing-design.md` and the FE `frontend/src/types.ts`.

## Open Questions

- Are `POST` write paths enabled in this slice or read-only + seed only? (See requirements Open Questions.)
- Does FE cut over to the API here or in a follow-up slice? (Default: follow-up.)
