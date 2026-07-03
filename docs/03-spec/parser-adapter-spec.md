# Specification: Parser Adapter

## Status

Draft. Phase 3 adapter slice. Behavior source of truth for `parser-adapter`. Derived from `docs/02-user-stories/parser-adapter-stories.md`.

## Source Documents

- `docs/01-requirements/parser-adapter-requirements.md`
- `docs/02-user-stories/parser-adapter-stories.md`
- `docs/03-spec/converter-adapter-spec.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/markdown-standard.md`
- `docs/batch-processing-design.md`
- `docs/architecture.md`

## Scope

Atlas must support a PDF-to-Markdown/images parser slice that routes parsing through a product-facing parser adapter contract. The slice includes parser capability behavior, parser run behavior, Markdown/assets/source chunk output contracts, metadata write-back, low-confidence/OCR/failure status mapping, mock-engine verification, and adapter seam guards. It does not implement real OCR, LLM enrichment, graph extraction, Ask/RAG, publish-to-Wiki, frontend UI, production auth/RBAC, or storage-adapter object operations.

## Constraints

- **Adapter only:** product workflow code depends on parser interfaces and registry contracts, not on `document-normalize` or other parser engines directly (REQ-PA-001).
- **No single hardcoded implementation:** `document-normalize` is the first target but must be replaceable through adapter configuration and capability metadata (REQ-PA-002).
- **Mock-engine verification:** automated tests must use mock/fake parser engines and must not require real document corpora or a local parser binary (REQ-PA-012).
- **Secret/path safety:** all persisted or returned paths are relative and traversal-free; no raw secrets, private endpoints, stack traces, raw parser logs, or private absolute paths are returned or logged (REQ-PA-008, REQ-PA-011).
- **Trace/review preservation:** parser results preserve source path, PDF path, converter metadata, confidence, source chunks, parser name, and review status; generated content never auto-approves (REQ-PA-009, REQ-PA-010, REQ-PA-014).
- **No external cloud calls:** this slice does not introduce external network dependencies or cloud services.

## Actors

| Actor | Role |
|---|---|
| Knowledge base administrator | Triggers or monitors parser runs after conversion. |
| Platform administrator | Reviews parser adapter availability and masked configuration. |
| Delivery lead | Uses parser reports to understand review workload and blocked files. |
| SME reviewer | Reviews generated Markdown and source chunks against PDF evidence. |
| Codex implementation agent | Implements strictly against this spec and task checklist after SDD acceptance. |

## Functional Requirements

### Adapter Boundary

- **FR-PA-001:** The parser workflow must resolve a parser adapter through a registry/capability contract before any parsing starts. (US-PA-001)
- **FR-PA-002:** Non-adapter product layers must not reference `document-normalize`, MinerU, Docling, PaddleOCR, direct command execution APIs, or outbound HTTP clients for parsing. (US-PA-001, US-PA-005)
- **FR-PA-003:** The adapter registry must support at least one configured default parser adapter and expose unavailable/misconfigured states safely. (US-PA-002)

### Capability Metadata

- **FR-PA-004:** Capability metadata must include adapter key, display name, version, input type `pdf`, output types `markdown` and `assets`, default marker, health/status, low-confidence threshold, and masked configuration summary. (US-PA-002)
- **FR-PA-005:** Capability metadata must not expose raw command strings, environment values, credentials, hostnames, raw local paths, or private endpoints. (US-PA-002)

### Parser Run

- **FR-PA-006:** A parser run must accept a batch id, adapter key or default marker, requested actor, mode, and a set of target file ids or all eligible batch files. (US-PA-001)
- **FR-PA-007:** Eligible targets are file items in the same batch with status `PDF_CONVERTED` and a safe relative `pdfPath`. (US-PA-001, US-PA-005)
- **FR-PA-008:** Ineligible files must be skipped with a safe report entry and must not be passed to the parser adapter. (US-PA-004)
- **FR-PA-009:** Parser run records must preserve adapter key/name, input count, output counts, started/completed timestamps, result status, and user-safe summary. (US-PA-004)

### Markdown, Assets, And Chunks

- **FR-PA-010:** Successful parsing must write a relative `markdownPath`, optional relative `assetsPath`, parser key, completion timestamp, confidence, and `MARKDOWN_GENERATED` status. (US-PA-003)
- **FR-PA-011:** When a parser runtime materializes Markdown bytes, the generated Markdown must include the required front matter fields from `docs/markdown-standard.md`, including workspace, batch id, source file/path/type, PDF file, converter, parser, conversion status, review status, confidence, last updated, and owner. This metadata/API slice records `markdownPath` and source chunk evidence; byte-level object writes remain outside this slice. (US-PA-003)
- **FR-PA-012:** When Markdown bytes are materialized, they must include source trace blocks for major sections or chunks with source file, PDF file, page, section, chunk id, confidence, and review status. This slice persists equivalent source chunk metadata for report/review flows. (US-PA-003)
- **FR-PA-013:** Parser results must create source chunk records with file item id, source file, page, section, confidence, and review status. (US-PA-003)
- **FR-PA-014:** Parser-adapter must not create published Wiki pages; `wiki_page` creation remains deferred to a later publish/review slice unless that slice changes the contract. (US-PA-003)

### Status Mapping And Failure Behavior

- **FR-PA-015:** Parser confidence below `0.80` must map the file to `LOW_CONFIDENCE` and preserve `reviewStatus=REVIEW_REQUIRED`. (US-PA-004)
- **FR-PA-016:** Parser output that requires OCR must map the file to `OCR_REQUIRED`, include a safe reason, and not execute OCR in this slice. (US-PA-004)
- **FR-PA-017:** Parser failure for an eligible PDF must map the file to `FAILED` with a bounded user-safe error summary. (US-PA-004)
- **FR-PA-018:** Unexpected adapter faults must return user-safe errors and leave file status unchanged unless a safe per-file failure result exists. (US-PA-005)
- **FR-PA-019:** Parser reports must summarize total files, Markdown generated, low-confidence, OCR-required, failed, skipped/ineligible, and unsupported outcomes. (US-PA-004)

### Metadata Write-Back And Validation

- **FR-PA-020:** Parser result writes must reject absolute paths, URI-prefixed paths, traversal paths, unknown file ids, files outside the run batch, and out-of-range confidence. (US-PA-005)
- **FR-PA-021:** Parser error summaries must be sanitized to remove raw parser logs, stack traces, secrets, private endpoints, hostnames, and absolute/private paths. (US-PA-005)
- **FR-PA-022:** Parser result writes must preserve source path, source type, PDF path, converter evidence, and review status unless an explicit later review action changes review status. (US-PA-003, US-PA-005)

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw secrets, command credentials, hostnames, private endpoints, private paths, stack traces, or raw parser logs in responses or persisted safe messages. |
| Reliability | Mock/fake engine tests cover success, low-confidence, OCR-required, failed, skipped/ineligible, unsafe path rejection, unknown file result, unavailable adapter, and adapter fault cases. |
| Extensibility | Parser adapter interface allows future parser implementations without changing product workflow callers. |
| Auditability | Parser run and per-file result metadata are traceable to batch/file ids, adapter identity, Markdown/assets paths, and source chunks. |
| Data safety | Use mock/sample metadata only; no real company documents, raw document content, private paths, or external cloud calls in test fixtures. |

## Workflow

```text
--------------------------+
| Batch/file metadata     |
| PDF_CONVERTED targets   |
+------------+-------------+
             |
             v
+--------------------------+       unavailable/misconfigured
| Resolve parser adapter   |------------------------------+
+------------+-------------+                              |
             | available                                  v
             v                                    +----------------+
+--------------------------+                      | Safe run error |
| Validate eligible files  |                      | no unsafe leak |
+------------+-------------+                      +----------------+
             |
             v
+--------------------------+
| Execute parser adapter   |
| mock/fake in CI          |
+------------+-------------+
             |
             v
+--------------------------+
| Map statuses + chunks    |
| markdown/assets paths    |
+------------+-------------+
             |
             v
+--------------------------+
| Persist metadata +       |
| parser run report        |
+--------------------------+
```

## State Model

### Parser Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`: adapter resolved and input validation passed.
- `RUNNING -> SUCCEEDED`: all eligible files generated Markdown safely.
- `RUNNING -> PARTIAL_FAILED`: at least one file generated Markdown and at least one file was low-confidence, OCR-required, failed, skipped, or unsupported.
- `RUNNING -> FAILED`: no target file produced usable Markdown, or adapter-level failure prevented per-file results.

### Per-File Status Mapping

| Input / Result | File Status | Notes |
|---|---|---|
| Eligible PDF success with confidence `>= 0.80` | `MARKDOWN_GENERATED` | Requires relative `markdownPath`; optional relative `assetsPath`. |
| Eligible PDF success with confidence `< 0.80` | `LOW_CONFIDENCE` | Review status remains `REVIEW_REQUIRED`. |
| Parser indicates OCR is required | `OCR_REQUIRED` | OCR is not executed in this slice. |
| Parser failure for eligible PDF | `FAILED` | User-safe error summary required. |
| Non-PDF or not `PDF_CONVERTED` target | unchanged / skipped | Not passed to parser adapter; included in report as skipped/ineligible. |
| Adapter-level unavailable before execution | unchanged | Run fails safely; file statuses unchanged. |

## Validation Rules

- Batch id and target file ids must reference existing metadata records.
- Target files must belong to the requested batch.
- Targets passed to the adapter must have status `PDF_CONVERTED` and a safe relative `pdfPath`.
- `markdownPath`, `assetsPath`, chunk asset references, and all artifact paths must be safe relative paths.
- Confidence values, when present, must be in `[0,1]`.
- Chunk page values must be positive integers when supplied.
- Chunk ids must be unique within a parser run.
- Error summaries must be bounded, user-safe, and stripped of raw parser output, stack traces, private paths, hostnames, and secrets.
- Adapter key must resolve to a registered adapter or fail with a user-safe unavailable/misconfigured status.

## API / Interface Surface

The full contract lives in `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `GET /api/parser-adapters` | Lists configured parser adapter capabilities with masked configuration. |
| `POST /api/batches/{batchId}/parser-runs` | Starts a parser run for eligible file metadata and records results. |
| `GET /api/parser-runs/{runId}` | Returns parser run summary and per-file/chunk report. |
| Internal parser adapter interface | Parses PDFs behind product-facing contract; mock/fake implementations are required for tests. |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-PA-01 | REQ-PA-001, REQ-PA-012 | Static guard tests fail on direct parser engine references outside adapter implementation. |
| AC-PA-02 | REQ-PA-002, REQ-PA-003 | Capability contract returns masked adapter metadata and replaceable default marker. |
| AC-PA-03 | REQ-PA-004, REQ-PA-005 | Mock parser run converts eligible PDF metadata to `MARKDOWN_GENERATED` with relative Markdown/assets paths. |
| AC-PA-04 | REQ-PA-006, REQ-PA-007, REQ-PA-008 | Low-confidence, OCR-required, and failed outputs map to existing statuses with safe report entries. |
| AC-PA-05 | REQ-PA-009, REQ-PA-010, REQ-PA-014 | Markdown/source chunk output preserves source trace, confidence, parser identity, PDF path, and review status. |
| AC-PA-06 | REQ-PA-011 | Unsafe paths, unknown files, cross-batch files, and out-of-range confidence are rejected before persistence. |
| AC-PA-07 | REQ-PA-012 | `cd backend && mvn verify` passes using mock/fake parser engines only. |
| AC-PA-08 | REQ-PA-013 | Parser run report includes all required outcome counts. |
| AC-PA-09 | REQ-PA-008 | Error responses/log assertions show no raw secrets, private paths, raw parser logs, or stack traces. |

## Out Of Scope

- Real `document-normalize` process/worker execution beyond adapter seam contract.
- OCR execution, LLM enrichment, graph derivation, Ask/RAG, and Wiki publication.
- Storage adapter object copy/upload/delete behavior.
- Markdown byte materialization, object storage writes, and byte-level front matter validation beyond mock path/chunk metadata.
- Frontend screens or settings UI updates.
- Production auth/RBAC and secret manager integration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-PA-001 | Local process wrapper vs external worker for real `document-normalize` execution, including Markdown byte materialization and storage writes. | Affects deployment topology, not current mock-engine contract. |
| OQ-PA-002 | Parser output creates no `wiki_page` rows by default; should a future publish slice or this slice own draft page rows? | Affects publish/review ownership, not parser result contract. |
| OQ-PA-003 | Low-confidence threshold is committed as `< 0.80` for this SDD. | Confirm before implementation if product wants a different threshold. |
