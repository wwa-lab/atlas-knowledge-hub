# Data Flow: Metadata API

## Status

Draft. Phase 2. Slice `metadata-api`. Derived from `docs/03-spec/metadata-api-spec.md` and `docs/04-architecture/metadata-api-architecture.md`.

## Request Lifecycle (all endpoints)

```
Client ─▶ Controller (web/)
             │  Bean Validation on request DTO
             │  invalid → GlobalExceptionHandler → 400 VALIDATION_ERROR envelope
             ▼
        Application Service (app/)
             │  orchestration, entity↔DTO mapping, derived metrics, invariants
             ▼
        Repository (repo/, Spring Data JPA)
             │
             ▼
        PostgreSQL (Flyway-managed schema `atlas`)
             ▲
             │  entities
        Application Service  ── assemble success envelope + meta (pagination)
             ▲
        Controller ─▶ Client (JSON envelope)
```

No converter/parser/model/vector/storage engine and no file bytes appear anywhere in this flow (REQ-MA-013).

## Flow 1 — Create Knowledge Space (`POST /api/spaces`)

```
DTO {name, description, type, index_strategy, owner}
  → validate (name non-blank; type∈document|faq; index_strategy∈rag|wiki)
  → app: build Space entity; server sets id, status=default, created_at/updated_at=now
  → repo.save
  → 201 envelope { data: SpaceDetail }
invalid → 400 VALIDATION_ERROR { fields }
```

## Flow 2 — Create Batch from inventory (`POST /api/spaces/{spaceId}/batches`)

```
DTO { source_kind, name, owner, files:[{source_path, source_type, status, confidence, review_status, chunks?[]}] }
  → validate spaceId exists (else 404); source_kind∈folder|zip
  → for each file: validate source_path relative & traversal-free; status∈FileStatus; confidence∈[0,1]
  → invariant: generated/low-confidence file ⇒ review_status defaults REVIEW_REQUIRED (never APPROVED)
  → app: persist Batch + FileItems (+ SourceChunks if provided) in one transaction
  → metrics NOT stored; derived on read from persisted file items
  → 201 envelope { data: BatchDetail with derived metrics }
no supported/invalid file paths → 400 VALIDATION_ERROR
```

Only metadata is written; the service performs no conversion, parsing, decompression, or byte read.

## Flow 3 — Read Batch with derived metrics (`GET /api/batches/{batchId}`)

```
GET batchId
  → repo.findBatch + repo.findFileItemsByBatch
  → app.computeMetrics(fileItems): { total, pdfConverted, markdownGenerated, reviewRequired, failed, unsupported }
  → 200 envelope { data: BatchDetail(metrics) }
unknown → 404 NOT_FOUND
```

Metrics are a pure function of file-item statuses — a single source of truth, never a stored counter that can drift.

## Flow 4 — Read file items + chunks (`GET /api/batches/{id}/files`, `GET /api/files/{id}/chunks`)

```
GET files?status=&page=&size=
  → repo.pageFileItemsByBatch(filter)
  → map each: {id, source_path(relative), source_type, status, confidence, review_status, artifact paths, error_message}
  → 200 envelope { data:[...], meta:{page,size,total} }

GET files/{id}/chunks
  → repo.findChunksByFile
  → map: {id, source_file, page/section, confidence, review_status}
  → 200 envelope { data:[...] }
```

Every returned path is relative; trace + confidence + review status always present (REQ-MA-011).

## Flow 5 — Append review record (`POST /api/files/{fileId}/reviews`)

```
DTO { action∈APPROVE|NEED_FIX|OCR_REQUIRED, reviewer, comment?, affected_chunks?[] }
  → validate fileId exists (else 404); action & reviewer required
  → app: append ReviewRecord (immutable, server timestamp)
  → app: update file_item.review_status per action mapping
        APPROVE→APPROVED, NEED_FIX→NEED_FIX, OCR_REQUIRED→OCR_REQUIRED
        (PUBLISHED never set here)
  → 201 envelope { data: ReviewRecord }

GET files/{fileId}/reviews → chronological append-only history
```

## Migration & Seed Flow (startup / `mvn flyway:migrate`)

```
Flyway on clean DB
  → V1__init_schema.sql        create space, batch, file_item, source_chunk,
                                wiki_page, review_record, graph_node, graph_edge (+ indexes)
  → V2__seed_mock_metadata.sql insert mock/sample rows aligned to FE baseline
                                (no real data, no secrets)
  → app boots with ddl-auto=validate (schema matches entities or startup fails)
mvn verify → Flyway validate + contract/integration tests (Testcontainers)
```

## Error Flow (cross-cutting)

```
any layer throws
  → GlobalExceptionHandler
      ValidationException/BindException → 400 VALIDATION_ERROR (+fields)
      NotFoundException                 → 404 NOT_FOUND
      ConflictException                 → 409 CONFLICT
      anything else                     → 500 INTERNAL_ERROR (generic message)
  → response envelope carries NO stack trace, SQL, secret, hostname, or private path
  → full detail logged server-side only
```

## Data-Safety Invariants Across Flows

- Metadata-only: no engine call, no byte read, no external network beyond JDBC.
- `source_path` always relative and traversal-checked before persistence.
- Generated content persists `REVIEW_REQUIRED`; only explicit review advances status.
- `review_record` is append-only; metrics are derived, not stored.
- Seed and responses never contain real company data, raw secrets, or private paths.
