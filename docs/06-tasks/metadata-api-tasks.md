# Tasks: Metadata API

## Status

Draft. Phase 2 (Backend metadata API + persistence). Slice `metadata-api`. Executable checklist derived from `docs/05-design/metadata-api-design.md`, `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`, and `docs/03-spec/metadata-api-spec.md` (behavior source of truth).

## Preconditions (gate — REQ-PROD-073)

- [ ] Data model (`docs/04-architecture/metadata-api-data-model.md`) accepted.
- [ ] API guide (`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`) accepted.
- [ ] Phase 1 FE baseline enum/field shapes reconciled (`frontend/src/types.ts`, `docs/batch-processing-design.md`).

Do not start implementation until all three are checked.

## Global Constraints (apply to every task)

- **Phase 2 backend.** Java + Spring Boot; PostgreSQL + Flyway; `ddl-auto=validate`.
- **Metadata-only / adapter-neutral:** no converter/parser/model/vector/storage engine call; no file bytes read; `adapter/` stays empty (REQ-MA-013).
- **No hardcoded DB / no raw secrets:** datasource from externalized config; no credentials, private endpoints, hostnames, or real company data in source, config, logs, or seed (REQ-MA-012).
- **Trace preserved:** relative `source_path` only; `confidence` + `review_status` always present; generated content defaults `REVIEW_REQUIRED`, never `APPROVED` (REQ-MA-011).
- **Scope surgical:** touch only `backend/**` and the SDD docs; do not modify FE runtime code in this slice (FE cutover is deferred).

## Tasks

| ID | Task | Owner | Pri | Deps | Maps to (REQ / spec) | Verification |
|---|---|---|---|---|---|---|
| T-MA-001 | Promote `backend/` to a buildable Spring Boot Maven project: `pom.xml` (Spring Web, Spring Data JPA, Validation, Flyway, PostgreSQL driver, Testcontainers), `MetadataApiApplication`, **package-by-feature** layout (`space/ batch/ file/ review/ wiki/ graph/ common/ adapter/`) per design + `docs/BACKEND_CODING_STANDARD.md`. | Codex | High | — | REQ-MA-001, REQ-MA-008 / Design "Module Layout" | `cd backend && mvn -q compile` succeeds; package structure is feature-first and matches design. |
| T-MA-002 | Add config-driven `application.yml`: `spring.datasource.*` from `${ATLAS_DB_*}` placeholders (no literals), `ddl-auto=validate`, Flyway enabled, `test`/`local` profiles. Add `SECURITY.md`/class note: no auth, internal-only, RBAC deferred to Phase 4. | Codex | High | T-MA-001 | REQ-MA-012, REQ-MA-008 / Design "Configuration" | `grep -RnE '(jdbc:|password:\s*\S)' backend/src/main/resources` finds no literal secret; app context loads under `test` profile. |
| T-MA-003 | Author `V1__init_schema.sql`: enum-as-text+CHECK (or PG enums), all 8 tables (`space`, `batch`, `file_item`, `source_chunk`, `review_record`, `wiki_page`, `graph_node`, `graph_edge`), FKs, indexes per data model. | Codex | High | T-MA-001 | REQ-MA-007 / Data model "Tables" | Flyway migrate on Testcontainers PG succeeds; `ddl-auto=validate` passes against entities. |
| T-MA-004 | Author `V2__seed_mock_metadata.sql`: mock/sample rows aligned to FE baseline (spaces, ≥1 batch, file items across representative statuses, chunks, one review record, sample wiki-page/graph rows). Relative paths only, no real data, no secrets. | Codex | High | T-MA-003 | REQ-MA-015, REQ-MA-012 / Data model "Migrations" | Integration test asserts seeded counts/enum values match FE mock; secret/path scan clean. |
| T-MA-005 | Implement entities + enums co-located in their feature packages (`Space` in `space/`, `Batch` in `batch/`, `FileItem`/`SourceChunk` in `file/`, `ReviewRecord` in `review/`, `WikiPage` in `wiki/`, `GraphNode`/`GraphEdge` in `graph/`); cross-feature enums (`FileStatus`, `ReviewStatus`, `SourceKind`, `SourceType`) in `common/enums`, feature-local enums (`SpaceType`, `IndexStrategy`, `SpaceStatus`, `ReviewAction`, `GraphNodeType`, `GraphEdgeType`) with their feature. Enum values identical to `frontend/src/types.ts` / `docs/batch-processing-design.md`. Add invariant helpers. | Codex | High | T-MA-001 | REQ-MA-002–007, REQ-MA-011 / Data model "Enums" | Unit test asserts `FileStatus` set == allowed set (no extras); enum value strings match FE. |
| T-MA-006 | Implement each feature's Spring Data JPA repository (`space/SpaceRepository`, `batch/BatchRepository`, `file/FileItemRepository`+`SourceChunkRepository`, `review/ReviewRecordRepository`) with paged/filter queries (`findBySpaceId`, `findByBatchIdAndStatus`, `findByFileItemId`, chronological review history). | Codex | High | T-MA-003, T-MA-005 | REQ-MA-010 / Design "Repositories" | Integration test (Testcontainers) covers CRUD + paging + filter. |
| T-MA-007 | Implement `common/` cross-cutting: `common/web` envelope + DTOs (`ApiEnvelope`, `ErrorBody`, `PageMeta`) + `GlobalExceptionHandler`; `common/validation` custom `@RelativePath` validator; per-feature request records with Bean Validation; exception→`{400,404,409,500}` user-safe mapping. | Codex | High | T-MA-001 | REQ-MA-009, REQ-MA-010 / Spec "Response Envelope","Error Behavior" | Contract test: bad DTO → 400 with `fields`; unknown id → 404; error body carries no stack/SQL/secret/absolute path. |
| T-MA-008 | Implement Space endpoints (`GET /api/spaces`, `GET /api/spaces/{id}`, `POST /api/spaces`) + `SpaceService`: server-set id/status/timestamps, validation. | Codex | High | T-MA-006, T-MA-007 | REQ-MA-002 / API guide "Knowledge Spaces"; AC-MA-01, AC-MA-02 | Contract tests for list/detail/create incl. 400 + 404. |
| T-MA-009 | Implement Batch endpoints (`GET /api/spaces/{id}/batches`, `GET /api/batches/{id}`, `POST /api/spaces/{id}/batches`) + `BatchService` + `MetricsCalculator` (metrics derived from file items). Create batch persists file items + optional chunks; no engine/byte access. | Codex | High | T-MA-008 | REQ-MA-003, REQ-MA-004, REQ-MA-013 / API guide "Batches"; AC-MA-03, AC-MA-04 | Unit test: `MetricsCalculator` derivation. Contract test: create-from-inventory persists items; generated item stays `REVIEW_REQUIRED`; adapter-seam guard test passes. |
| T-MA-010 | Implement File endpoints (`GET /api/batches/{id}/files`, `GET /api/files/{id}`, `GET /api/files/{id}/chunks`) + `FileService`: relative paths, trace/confidence/review in every response. | Codex | High | T-MA-009 | REQ-MA-004, REQ-MA-005, REQ-MA-011 / API guide "File Items"; AC-MA-05, AC-MA-06 | Contract test asserts relative paths, trace fields present; `?status=` filter works. |
| T-MA-011 | Implement Review endpoints (`POST /api/files/{id}/reviews`, `GET /api/files/{id}/reviews`) + `ReviewService`: append-only record, action→review_status mapping (`APPROVE→APPROVED`, `NEED_FIX→NEED_FIX`, `OCR_REQUIRED→OCR_REQUIRED`; never `PUBLISHED`). | Codex | High | T-MA-010 | REQ-MA-006, REQ-MA-011 / API guide "Reviews"; AC-MA-07 | Contract test: append updates target status; history chronological; records immutable. |
| T-MA-012 | Add adapter-seam guard test + confirm no wiki/graph/ask endpoints exist: assert `adapter/` has no engine/network dependency and no controller maps deferred routes. | Codex | Med | T-MA-011 | REQ-MA-013, REQ-MA-014 / Architecture "Adapter Boundary"; AC-MA-10, AC-MA-11 | Test scans for outbound HTTP clients / engine imports in `adapter/`; route table excludes wiki/graph/ask. |
| T-MA-013 | Full verification pass + evidence: run `mvn verify` (Flyway validate + unit + contract + integration), `git diff --check`, new-dependency/network scan, secret/private-path scan. Record results in traceability. | Codex | High | T-MA-001→012 | Phase 2 verification row; all AC-MA | `cd backend && mvn verify` green; scans clean; evidence written to `docs/00-context/metadata-api-traceability.md`. |
| T-MA-014 | (Deferred, documented) FE cutover: replace FE mock provider with an HTTP client against these endpoints. **Not implemented this slice** — recorded as a follow-up FE integration task. | — | Low | T-MA-013 | REQ-MA-015 / Design "Consumer Integration" | N/A this slice; tracked in traceability Open Questions. |

## Verification Commands (Phase 2 API row)

```bash
cd backend
mvn verify                 # compile + Flyway validate + unit + contract + integration (Testcontainers)
mvn -q flyway:migrate      # optional explicit migration check against a local/test datasource
git diff --check           # whitespace/conflict hygiene
# secret / private-path scan (no raw creds, no absolute paths, no real company data)
grep -RnE '(BEGIN.*PRIVATE KEY|password\s*=\s*[^$]|/Users/|/home/|C:\\\\)' backend/src || echo "clean"
```

Name any skipped check and why; never imply an unrun check passed.

## Definition of Done

- All T-MA-001→013 complete (T-MA-014 explicitly deferred).
- `mvn verify` green: Flyway validation, unit, contract, and integration tests pass.
- Every acceptance check AC-MA-01→11 demonstrated by a test.
- No engine call, no byte read, empty `adapter/`, config-driven datasource, no raw secrets, relative paths only, generated content `REVIEW_REQUIRED`.
- Evidence + residual risks recorded in `docs/00-context/metadata-api-traceability.md`.
