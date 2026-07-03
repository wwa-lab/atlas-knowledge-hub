# Requirements: Metadata API

## Status

Draft. Phase 2 (Backend metadata API + persistence). Slice `metadata-api`. Backend + DB owner boundary.

## Purpose

Define the first real Atlas backend slice: a Spring Boot **metadata control plane** that persists Knowledge Space, batch, file, source-chunk, and review metadata in PostgreSQL (via Flyway), and exposes an internal REST API so the frontend can read/write this metadata instead of holding it in memory. This slice makes the metadata **mock-free at the contract level** while still shipping mock/sample seed data — no real company content.

This slice records *metadata and status only*. It does not execute conversion, parsing, model, vector, or storage work — those remain Phase 3 adapter slices.

## Source Documents

- `docs/00-context/slice-roadmap.md` — Phase 2 API row (verification, constraints, "API guide required").
- `docs/01-requirements/requirement.md` — Phase 2 product requirements: REQ-PROD-008, 012, 014, 030, 031, 057, 061, 068–073, 076.
- `docs/04-architecture/knowledge-space-data-model.md` — canonical entity/field shapes for space, batch, file_item, wiki_page, source_chunk, graph_node, graph_edge.
- `docs/04-architecture/folder-upload-data-model.md` — `FileStatus` / `ReviewStatus` enums and metrics shape the FE already uses.
- `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` — candidate endpoint naming this slice formalizes.
- `docs/batch-processing-design.md` — authoritative `FileStatus` set.
- `docs/markdown-standard.md` — source-trace / confidence / review-status fields.
- FE baseline (consumer of the API): `frontend/public/atlas-prototype.html`, `frontend/src/types.ts`, `frontend/src/data/atlasMock.ts`.

## Scope

### In scope

- Spring Boot service skeleton, layered per `DEVELOPMENT_STANDARDS.md` (controller → application service → repository → Flyway migration), with an empty adapter seam reserved.
- PostgreSQL schema via Flyway migrations for the **initial entity set** (REQ-PROD-071): `space`, `batch`, `file_item`, `source_chunk`, `wiki_page`, `review_record`, `graph_node`, `graph_edge`.
- Read + write REST endpoints for the metadata the FE already renders: Knowledge Spaces (list / detail / create), Batches (list-by-space / detail / create), File Items (list-by-batch / detail), Source Chunks (list-by-file), Review Records (list-by-target / append).
- Consistent API response envelope; pagination + filtering on list endpoints; DTO validation at the boundary; user-safe error envelope.
- Flyway seed of mock/sample metadata matching the FE baseline so the FE can switch from in-memory mock to the API without visual drift.
- Preserve source trace, confidence, and review status on every persisted and returned record.

### Out of scope (deferred; recorded in traceability)

- Converter/parser/model/vector/storage **execution** — Phase 3 adapter slices. This service only stores metadata/status.
- Real byte upload, decompression, streaming, or object storage — future storage adapter. `POST /batches` accepts pre-computed inventory metadata only.
- Graph extraction + graph query API (`knowledge-graph`), Ask/RAG (`ask-rag`), and full wiki-body rendering/publish state machine (`review-publish`) — only their **metadata tables** are created here.
- Authentication, RBAC enforcement, and audit hardening — Phase 4.
- Model/engine config, member, and registration management APIs — their own later slices.

## Requirements

| ID | Requirement | Priority | Traces to |
|---|---|---|---|
| REQ-MA-001 | Provide a Spring Boot internal REST metadata service that becomes the source of truth for Knowledge Space, batch, file, source-chunk, and review metadata, replacing the FE in-memory mock at the contract level. | Must | REQ-PROD-068 |
| REQ-MA-002 | Persist Knowledge Space metadata: `id`, `name`, `description`, `type` (`document`/`faq`), `index_strategy` (`rag`/`wiki`), `owner`, `status`, `document_count`, `wiki_page_count`, `review_count`, `created_at`, `updated_at`. | Must | REQ-PROD-008, 001, 005–007 |
| REQ-MA-003 | Persist Batch metadata: `id`, `space_id`, `name`, `source_kind` (`folder`/`zip`), `owner`, `uploaded_at`, derived metrics, and processing status. | Must | REQ-PROD-009, 011 |
| REQ-MA-004 | Persist File Item metadata using the exact allowed `FileStatus` set from `docs/batch-processing-design.md`, with `source_path` (relative only), `source_type`, artifact-path metadata, `confidence`, `review_status`, and user-safe `error_message`. | Must | REQ-PROD-012, 014 |
| REQ-MA-005 | Persist Source Chunk trace records (`source_file`, `page`/`section`, `confidence`, `review_status`) linked to a file item, so trace survives persistence. | Must | REQ-PROD-014, 023 |
| REQ-MA-006 | Persist an append-only Review Record log (`reviewer`, `action`, `timestamp`, `comment`, `affected_chunks`) and the allowed review status set. | Must | REQ-PROD-030, 031 |
| REQ-MA-007 | Create the initial schema via Flyway migrations for all initial entities: `space`, `batch`, `file_item`, `source_chunk`, `wiki_page`, `review_record`, `graph_node`, `graph_edge`. | Must | REQ-PROD-070, 071 |
| REQ-MA-008 | Layer the backend: controller → application service → repository → migration, keeping workflow orchestration out of controllers and reserving an empty adapter seam for Phase 3. | Must | REQ-PROD-072 |
| REQ-MA-009 | Validate every inbound DTO at the API boundary; reject invalid input with a consistent, user-safe error envelope (no stack traces, secrets, or private paths). | Must | REQ-PROD-076, 077 |
| REQ-MA-010 | Return a consistent success/error response envelope; list endpoints support pagination and status/space filtering. | Must | API Standards |
| REQ-MA-011 | Preserve source trace, confidence, and review status on every persisted and returned file/chunk record; never default generated content to `APPROVED`. | Must | REQ-PROD-024, 026, 032; Trace & Review |
| REQ-MA-012 | Do not hardcode a single database as the only implementation: datasource is config-driven; no raw secrets, credentials, private endpoints, or real company data in source, config, or seed data (status-only fields allowed). | Must | REQ-PROD-074, 075; Data Safety |
| REQ-MA-013 | The service must not call or embed any converter/parser/model/vector/storage engine; it records metadata/status only, keeping those behind future Phase 3 adapters. | Must | REQ-PROD-015–017; Adapter gate |
| REQ-MA-014 | Graph query, Ask, wiki-body rendering, and publish state-machine endpoints are out of scope; only their metadata tables are created here. The omission is recorded in traceability. | Must | Scope boundary |
| REQ-MA-015 | Ship a Flyway seed migration of mock/sample metadata aligned to the FE baseline mock so the FE can switch from in-memory mock to the API with no visual drift. | Should | REQ-PROD-074; mock-only |

## Constraints

- **Verification (Phase 2 API):** `mvn verify` · Flyway migration validation · API contract tests. Plus the baseline `git diff --check`, new-network/new-dependency scan, and secret/private-path scan.
- **Hard constraints:** no hardcoded single DB as the only impl; secrets masked/status-only, never raw; no real company data; no external cloud calls.
- **API guide is required** and must be accepted (with the data model) before implementation starts (REQ-PROD-073).

## Open Questions

- Should `POST /spaces` and `POST /batches` be enabled write paths in this slice, or read-only mirrors of seed data with writes deferred? (Default: enable create for space/batch/review; keep file-item writes internal/seed-only until the ingestion adapter exists.)
- Does the FE cut over to the API in this slice, or does API cutover land in a follow-up FE integration slice? (Default: this slice delivers the API + contract tests; FE cutover is a separate task, noted in traceability.)
- Single Postgres schema vs. per-domain schemas. (Default: single `atlas` schema, one migration set.)
