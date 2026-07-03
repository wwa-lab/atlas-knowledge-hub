# Specification: Converter Adapter

## Status

Draft. Phase 3 adapter slice. Behavior source of truth for `converter-adapter`. Derived from `docs/02-user-stories/converter-adapter-stories.md`.

## Source Documents

- `docs/01-requirements/converter-adapter-requirements.md`
- `docs/02-user-stories/converter-adapter-stories.md`
- `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/batch-processing-design.md`
- `docs/markdown-standard.md`

## Scope

Atlas must support an Office-to-PDF conversion slice that routes all conversion through a product-facing converter adapter contract. The slice includes adapter registry/capability behavior, conversion run behavior, metadata write-back, status/error mapping, mock-engine verification, and seam guards. It does not implement parser, storage, OCR, vector/model, Wiki publish, graph, Ask, frontend UI, or production auth/RBAC.

## Constraints

- **Adapter only:** product workflow code depends on converter interfaces and registry contracts, not on `trinity-office` directly (REQ-CA-001).
- **No single hardcoded implementation:** `trinity-office` is the first target but must be replaceable through adapter configuration and capability metadata (REQ-CA-002).
- **Mock-engine verification:** automated tests must use mock/fake converter engines and must not require real document corpora or a local `trinity-office` binary (REQ-CA-010).
- **Secret/path safety:** all paths persisted or returned are relative and traversal-free; no raw secrets, private endpoints, command credentials, stack traces, or private absolute paths are returned or logged (REQ-CA-008, REQ-CA-009).
- **Trace/review preservation:** conversion results preserve source path, status, confidence when available, adapter name, and review status; conversion never auto-approves generated content (REQ-CA-007).
- **No external cloud calls:** this slice does not introduce external network dependencies or cloud services.

## Actors

| Actor | Role |
|---|---|
| Knowledge base administrator | Triggers or monitors batch conversion. |
| Platform administrator | Reviews converter adapter availability and masked configuration. |
| Delivery lead | Uses conversion reports to understand which files are ready for parser work. |
| SME reviewer | Uses failure/OCR/unsupported states to decide follow-up action. |
| Codex implementation agent | Implements strictly against this spec and task checklist. |

## Functional Requirements

### Adapter Boundary

- **FR-CA-001:** The converter workflow must resolve a converter adapter through a registry/capability contract before any conversion starts. (US-CA-001)
- **FR-CA-002:** Non-adapter product layers must not reference `trinity-office`, direct command execution APIs, or outbound HTTP clients for conversion. (US-CA-001, US-CA-005)
- **FR-CA-003:** The adapter registry must support at least one configured default converter adapter and must expose unavailable/misconfigured states safely. (US-CA-002)

### Capability Metadata

- **FR-CA-004:** Capability metadata must include adapter key, display name, version, output type `pdf`, supported source types, default marker, health/status, and masked configuration summary. (US-CA-002)
- **FR-CA-005:** Capability metadata must not expose raw command path, raw environment values, credentials, hostnames, or local absolute paths. (US-CA-002)

### Conversion Run

- **FR-CA-006:** A conversion run must accept a batch id, adapter key or default marker, and a set of target file ids or source metadata. (US-CA-001, US-CA-003)
- **FR-CA-007:** For `pptx`, `docx`, and `xlsx`, successful conversion must produce `PDF_CONVERTED`, a relative `pdfPath`, adapter name, and completion timestamp. (US-CA-001)
- **FR-CA-008:** Existing PDF inputs must be handled as pass-through candidates and must not invoke Office conversion. The exact artifact copy/reference policy remains OQ-CA-002. (US-CA-001)
- **FR-CA-009:** `image` inputs may be classified as `OCR_REQUIRED`; `unsupported` inputs must be classified as `UNSUPPORTED`. (US-CA-004)
- **FR-CA-010:** Conversion failure for supported Office inputs must be mapped to `PDF_CONVERT_FAILED` with a user-safe error summary. (US-CA-004)

### Metadata Write-Back

- **FR-CA-011:** Converter results must update file item status, `pdfPath`, confidence when available, and `errorMessage` when applicable through a metadata boundary. (US-CA-003)
- **FR-CA-012:** Converter result writes must reject absolute paths, traversal paths, raw private paths, and unsafe error payloads before persistence. (US-CA-003)
- **FR-CA-013:** Review status must remain `REVIEW_REQUIRED` for generated/derived conversion outputs unless a later explicit review action changes it. (US-CA-003)
- **FR-CA-014:** Conversion run records must preserve adapter key/name, input count, output counts, started/completed timestamps, result status, and user-safe summary. (US-CA-003, US-CA-004)

### Reporting And Failure Behavior

- **FR-CA-015:** Conversion reports must summarize total files, converted PDFs, unsupported files, failed files, OCR-required files, and skipped/pass-through files. (US-CA-004)
- **FR-CA-016:** Raw engine stderr/stdout must not be persisted or returned. A sanitized summary may be recorded. (US-CA-004)
- **FR-CA-017:** Unexpected adapter faults must return user-safe errors and leave file status unchanged unless a safe per-file failure result exists. (US-CA-004)

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw secrets, command credentials, private endpoints, private paths, or stack traces in responses/logs. |
| Reliability | Mock/fake engine tests must cover success, unsupported, failed, OCR-required, path rejection, and unavailable adapter cases. |
| Extensibility | Adapter interface must allow future converter implementations without changing product workflow callers. |
| Auditability | Conversion run and per-file result metadata must be traceable to batch/file ids and adapter identity. |
| Data safety | Use mock/sample metadata only; no real company documents or local absolute paths in seed/test data. |

## Workflow

```text
------------------+
| Batch/file data |
+--------+---------+
         |
         v
+------------------+      unavailable/misconfigured
| Resolve adapter  |------------------------------+
+--------+---------+                              |
         | available                              v
         v                                +----------------+
+------------------+                      | Safe error     |
| Validate targets |                      | no file change |
+--------+---------+                      +----------------+
         |
         v
+------------------+
| Execute adapter  |
| mock/fake in CI  |
+--------+---------+
         |
         v
+------------------+
| Map per-file     |
| results/statuses |
+--------+---------+
         |
         v
+------------------+
| Persist metadata |
| + run report     |
+------------------+
```

## State Model

### Conversion Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`: adapter resolved and input validation passed.
- `RUNNING -> SUCCEEDED`: all eligible files converted or pass-through/skipped safely.
- `RUNNING -> PARTIAL_FAILED`: at least one file converted and at least one file failed/unsupported/OCR-required.
- `RUNNING -> FAILED`: no target file produced a usable conversion result, or adapter-level failure prevented per-file results.

### Per-File Status Mapping

| Input / Result | File Status | Notes |
|---|---|---|
| `pptx`/`docx`/`xlsx` success | `PDF_CONVERTED` | Requires relative `pdfPath`. |
| Existing `pdf` pass-through | `PDF_CONVERTED` | Does not call Office converter; artifact policy OQ-CA-002. |
| Supported Office conversion failure | `PDF_CONVERT_FAILED` | User-safe error summary required. |
| Image requiring OCR | `OCR_REQUIRED` | OCR is not executed in this slice. |
| Unsupported source type | `UNSUPPORTED` | User-safe reason required. |
| Adapter-level unavailable before execution | unchanged | Run fails safely; file statuses unchanged. |

## Validation Rules

- Batch id and target file ids must reference existing metadata records.
- Target source types must be one of existing `SourceType` values: `pptx`, `docx`, `pdf`, `xlsx`, `image`, `unsupported`.
- `pdfPath` and all artifact paths must pass the same relative-path safety rule used by metadata API.
- Confidence values, when present, must be in `[0,1]`.
- Error summaries must be bounded, user-safe, and stripped of raw command output, stack traces, private paths, and secrets.
- Adapter key must resolve to a registered adapter or fail with a user-safe unavailable/misconfigured status.

## API / Interface Surface

The full contract lives in `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `GET /api/converter-adapters` | Lists configured converter adapter capabilities with masked configuration. |
| `POST /api/batches/{batchId}/conversion-runs` | Starts a conversion run for file metadata and records results. |
| `GET /api/conversion-runs/{runId}` | Returns conversion run summary and per-file report. |
| Internal converter adapter interface | Executes conversion behind product-facing contract; mock/fake implementations are required for tests. |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-CA-01 | REQ-CA-001, REQ-CA-011 | Static guard tests fail on direct engine references outside adapter implementation. |
| AC-CA-02 | REQ-CA-002, REQ-CA-003 | Capability endpoint/contract returns masked adapter metadata and replaceable default marker. |
| AC-CA-03 | REQ-CA-004, REQ-CA-005 | Mock run converts Office metadata to `PDF_CONVERTED` with relative `pdfPath`. |
| AC-CA-04 | REQ-CA-006 | Unsupported/failed/OCR-needed inputs map to existing statuses with safe report entries. |
| AC-CA-05 | REQ-CA-007, REQ-CA-008 | Result writes preserve source/review metadata and reject unsafe paths. |
| AC-CA-06 | REQ-CA-009 | Error responses/log assertions show no raw secrets, private paths, or stack traces. |
| AC-CA-07 | REQ-CA-010 | `cd backend && mvn verify` passes using mock/fake engines only. |
| AC-CA-08 | REQ-CA-012 | API/adapter guide and bilingual SDD docs exist before implementation. |

## Out Of Scope

- Parser adapter, Markdown generation, source chunk extraction, OCR execution.
- Storage adapter or object storage upload/copy implementation.
- Vector/model adapters, graph derivation, Ask/RAG.
- Frontend screens or settings UI updates.
- Production auth/RBAC and secret manager integration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-CA-001 | Local process wrapper vs external worker for real `trinity-office` execution. | Affects deployment topology, not current mock-engine contract. |
| OQ-CA-002 | PDF pass-through copy vs relative reference policy before storage-adapter exists. | Affects artifact path semantics for existing PDFs. |
| OQ-CA-003 | Exact target `trinity-office` command-line contract. | Affects real adapter implementation details, not interface or mock tests. |
