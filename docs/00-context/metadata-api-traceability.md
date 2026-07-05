# Traceability: Metadata API

## Status

Implemented. Phase 2 (Backend metadata API + persistence). Slice `metadata-api`.

Current state:

- Full bilingual SDD artifact set is complete (EN + `.zh-CN.md`).
- **API guide is INCLUDED** (Phase 2 backend slice — required, REQ-PROD-073).
- Product code implemented in `backend/` for T-MA-001→013; T-MA-014 remains deferred FE cutover.
- Slice status in `docs/00-context/slice-roadmap.md`: ✅ implemented.

## Slice

`metadata-api` — Spring Boot metadata control plane persisting Knowledge Space, batch, file-item, source-chunk, and review metadata in PostgreSQL (Flyway), served over an internal REST API. Metadata-only; no engine execution.

## Source Inputs

| Source | Role |
|---|---|
| `docs/00-context/slice-roadmap.md` | Phase 2 API row: verification (`mvn verify` · Flyway validation · API contract tests), constraints (no hardcoded single DB, secrets masked/status-only, no real data), "API guide required". |
| `docs/01-requirements/requirement.md` | Phase 2 product requirements: REQ-PROD-008, 012, 014, 030, 031, 068–073, 076, 077. |
| `docs/04-architecture/knowledge-space-data-model.md` | Canonical entity/field shapes for the 8 initial entities. |
| `docs/04-architecture/folder-upload-data-model.md` | `FileStatus`/`ReviewStatus` enums + metrics shape used by FE. |
| `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` | Candidate endpoint naming formalized here. |
| `docs/batch-processing-design.md` | Authoritative `FileStatus` set. |
| `docs/markdown-standard.md` | Trace/confidence/review fields. |
| `frontend/src/types.ts`, `frontend/src/data/atlasMock.ts`, `frontend/public/atlas-prototype.html` | FE consumer baseline for enum-value / shape parity. |

## Artifact Map

| Stage | EN | zh-CN |
|---|---|---|
| Requirements | `docs/01-requirements/metadata-api-requirements.md` | `…metadata-api-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/metadata-api-stories.md` | `…metadata-api-stories.zh-CN.md` |
| Specification | `docs/03-spec/metadata-api-spec.md` | `…metadata-api-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/metadata-api-architecture.md` | `…metadata-api-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/metadata-api-data-flow.md` | `…metadata-api-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/metadata-api-data-model.md` | `…metadata-api-data-model.zh-CN.md` |
| Design | `docs/05-design/metadata-api-design.md` | `…metadata-api-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | `…metadata-api-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/metadata-api-tasks.md` | `…metadata-api-tasks.zh-CN.md` |
| Traceability | `docs/00-context/metadata-api-traceability.md` | `…metadata-api-traceability.zh-CN.md` |

## API Guide Inclusion (recorded decision)

Unlike the Phase 1 FE slices (`knowledge-space`, `folder-upload`), this is a Phase 2 backend slice: the API implementation guide is **required and included**, and must be accepted with the data model before implementation starts (REQ-PROD-073, SDD profile "Gates").

## Requirement → Story → Spec → Task Links

| Requirement | Stories | Spec (Acceptance) | Tasks |
|---|---|---|---|
| REQ-MA-001 | US-MA-001 | AC-MA-01 | T-MA-001 |
| REQ-MA-002 | US-MA-001, US-MA-002 | AC-MA-01, AC-MA-02 | T-MA-005, T-MA-008 |
| REQ-MA-003 | US-MA-003, US-MA-004 | AC-MA-03, AC-MA-04 | T-MA-005, T-MA-009 |
| REQ-MA-004 | US-MA-004, US-MA-005 | AC-MA-04, AC-MA-05 | T-MA-005, T-MA-009, T-MA-010 |
| REQ-MA-005 | US-MA-005 | AC-MA-06 | T-MA-005, T-MA-010 |
| REQ-MA-006 | US-MA-006 | AC-MA-07 | T-MA-005, T-MA-011 |
| REQ-MA-007 | US-MA-007 | AC-MA-08 | T-MA-003, T-MA-004, T-MA-005 |
| REQ-MA-008 | US-MA-008 | AC-MA-10 | T-MA-001, T-MA-002, T-MA-007 |
| REQ-MA-009 | US-MA-002, US-MA-006 | AC-MA-02, AC-MA-09 | T-MA-007 |
| REQ-MA-010 | US-MA-001, US-MA-003 | AC-MA-01, AC-MA-03 | T-MA-006, T-MA-007 |
| REQ-MA-011 | US-MA-004, US-MA-005, US-MA-006 | AC-MA-05, AC-MA-06, AC-MA-07 | T-MA-005, T-MA-009, T-MA-010, T-MA-011 |
| REQ-MA-012 | US-MA-007, US-MA-008 | AC-MA-08, AC-MA-10 | T-MA-002, T-MA-004 |
| REQ-MA-013 | US-MA-004, US-MA-008 | AC-MA-04, AC-MA-10 | T-MA-009, T-MA-012 |
| REQ-MA-014 | (scope boundary) | AC-MA-11 | T-MA-012 |
| REQ-MA-015 | US-MA-007 | AC-MA-08 | T-MA-004, T-MA-014 (deferred) |

## Slice Boundary vs Other Slices

- `metadata-api` **owns:** Spring Boot service skeleton, PostgreSQL/Flyway schema for the 8 initial entities, and REST read/write endpoints for spaces, batches, files, chunks, and reviews.
- **Reserves (empty):** `adapter/` seam for Phase 3 converter/parser/storage/vector/model adapters — no engine call in this slice.
- **Creates tables but exposes no endpoints for:** `wiki_page` (→ `review-publish`), `graph_node`/`graph_edge` (→ `knowledge-graph`); Ask (→ `ask-rag`). Recorded per REQ-MA-014.
- **Does not touch:** FE runtime code — cutover from `frontend/src/data/atlasMock.ts` to the API is a deferred follow-up (T-MA-014).
- **Reconciles with, does not redefine:** `docs/04-architecture/knowledge-space-data-model.md`, `docs/batch-processing-design.md`, and `frontend/src/types.ts` enum values.

## Verification Evidence

Per the Phase 2 API row of `docs/00-context/slice-roadmap.md` (executed during implementation, T-MA-013):

- `cd backend && mvn verify` — **passed** on 2026-07-03. Evidence: 8 unit/static tests + 7 integration tests passed; Testcontainers 2.0.3 started `postgres:16-alpine`; Flyway applied `V1__init_schema.sql` and `V2__seed_mock_metadata.sql` against PostgreSQL 16.14; Hibernate `ddl-auto=validate` completed during Spring context startup.
- PostgreSQL integration note: integration tests use Testcontainers PostgreSQL directly, matching the SDD verification contract. Test DB credentials are runtime-only test container values and are not committed.
- `git diff --check` — **passed**.
- New-dependency / no-external-network scan — **passed** for product code. Findings limited to Maven schema URLs in `pom.xml`; no product HTTP client, engine import, or outbound network adapter exists.
- Secret / private-path / real-data scan — **passed** after cleanup. Runtime test DB secret is generated in memory; no raw credential, private absolute path, or real company data is committed in backend source or seed migrations.

Deferred work:

- T-MA-014 FE cutover remains out of scope and is tracked as a follow-up.
- `wiki_page`, `graph_node`, and `graph_edge` tables are migrated and seeded, but no wiki/graph/ask endpoints are mapped in this slice.

## Sub-Skills Used (generation pass)

`atlas-sdd-generate-all` orchestrated the chain: `req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality`.

## Key Assumptions Introduced

- Slice ID abbreviation is `MA` (matching the `FU`/`KS` convention); IDs `REQ-MA-###`, `US-MA-###`, `T-MA-###`, `AC-MA-###`.
- Data model covers all 8 initial entities (REQ-PROD-071) to satisfy the schema requirement, while the **API surface** is scoped to space/batch/file/chunk/review to stay a reviewable single implementation pass. Graph/wiki/ask APIs are deferred to their own slices.
- `POST` write paths (space/batch/review) are enabled; file-item writes arrive via batch creation from pre-computed inventory metadata (no bytes/engines).
- Enum values are persisted as identical strings to the FE types to enable drift-free FE cutover.
- No authentication in Phase 2 (internal-only); RBAC deferred to Phase 4.

## Open Questions

- Enable `POST` writes now vs. read-only + seed only. (Default taken: enable space/batch/review create.)
- FE cutover in this slice vs. a follow-up. (Default taken: follow-up — T-MA-014 deferred.)
- Enum persistence: PG native enum types vs. text + CHECK. (Default taken: text + CHECK.)
- Single `atlas` schema vs. per-domain schemas. (Default taken: single schema.)
- **`review_status` divergence (grounded in review):** persisted `review_status` follows REQ-PROD-030 (`REVIEW_REQUIRED|APPROVED|NEED_FIX|OCR_REQUIRED|PUBLISHED`) and is wider than `frontend/src/types.ts:44` `ReviewStatus` (`REVIEW_REQUIRED|APPROVED|REJECTED`). Persistence set is authoritative; DTO maps FE `REJECTED`→`NEED_FIX`; realigning the FE type is deferred to T-MA-014. Only `file_status` is a strict identical-value match with the FE.

## Deferred Translations

None. Every artifact has an EN and a `.zh-CN.md` copy with identical REQ/US/T/AC IDs.

## Product Goal Batch 5 FE Cutover Note

The deferred `T-MA-014` FE cutover is partially consumed by Product Goal Batch 5 for Phase I1-I2. The real Vue product path now reads existing space, batch, file, and source chunk endpoints and creates the safe sample batch through `POST /api/spaces/{spaceId}/batches`.

Evidence: `frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`; `docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png`; `cd backend && mvn verify` passed on 2026-07-05. No metadata API contract, schema, production upload behavior, auth/RBAC, or provider boundary changed in this batch.
