# Detailed Design: Parser Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/parser-adapter-spec.md` and `docs/04-architecture/parser-adapter-architecture.md`.

## Source Architecture

Parser-adapter is a backend/API + adapter contract slice. It adds parser capability/run behavior to the metadata control plane while keeping parser engine details inside adapter implementations. The design intentionally mirrors the converter-adapter run/capability/report shape so Codex can implement the next adapter slice with minimal new product concepts.

## Grounded Existing Code Context

These anchors were verified before design:

| Existing element | Verified anchor | Parser-adapter use |
|---|---|---|
| File artifact fields | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` | Reuse `pdfPath`, `markdownPath`, `assetsPath`, `errorMessage`, confidence, and review status. |
| Review preservation pattern | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:92` | Parser result writes should preserve review status like conversion writes. |
| Source chunk entity | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` | Persist parser-created source trace chunks. |
| File statuses | `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` | Reuse `MARKDOWN_GENERATED`, `LOW_CONFIDENCE`, `OCR_REQUIRED`, `FAILED`, `UNSUPPORTED`. |
| Relative path validator | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | Reuse for Markdown/assets/PDF path validation. |
| File/chunk read API | `backend/src/main/java/com/atlas/metadata/controller/FileController.java:48` | Existing chunk list endpoint remains the read side for source chunks. |
| Converter API pattern | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:30` | Parser controller should follow capability/create/get shape. |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | Extend guard expectations for parser adapter implementation. |

## Design Scope

### In Scope

- Parser adapter domain contracts, capability metadata, request/result shapes.
- Parser run persistence and per-file result persistence.
- Parser service behavior: target selection, adapter resolution, execution, validation, status mapping, summary calculation, safe error handling.
- Parser API guide and contract tests.
- Source chunk creation and Markdown/assets metadata write-back. Markdown byte materialization and object storage writes are deferred to the real runtime/storage integration.
- Mock parser adapter for deterministic tests.

### Out of Scope

- Real parser process topology, Markdown byte materialization, OCR execution, LLM enrichment, frontend UI, storage object operations, graph/Ask, Wiki publication, production auth/RBAC.

## Module Design

### Parser Adapter Contract

Conceptual contract:

```text
ParserAdapter
  capability() -> ParserCapability
  parse(ParserRequest) -> ParserResult
```

`ParserCapability` contains:

- adapter key and display name
- safe version/status
- supported input types: `pdf`
- output types: `markdown`, `assets`
- default marker
- low-confidence threshold, default `0.800`
- masked configuration summary

`ParserRequest` contains:

- run id
- batch id
- artifact root hint, default `generated/markdown`
- low-confidence threshold
- file descriptors: file id, source path, source type, current status, PDF path, converter adapter key when available

`ParserResult` contains:

- adapter key
- safe message
- per-file results
- source chunks per file

### Parser Service

Responsibilities:

- Validate batch and requested file ids.
- Select eligible target files: `PDF_CONVERTED` and safe `pdfPath`.
- Record skipped/ineligible files in report without passing them to the adapter.
- Resolve adapter by explicit key or default marker.
- Create parser run, mark running, execute adapter, persist validated results.
- Map status using the committed threshold: confidence `< 0.800` -> `LOW_CONFIDENCE`; otherwise successful Markdown -> `MARKDOWN_GENERATED`.
- Preserve review status and source metadata.
- Sanitize safe messages and safe errors before persistence/response.

### Parser Summary Calculator

The summary is derived from persisted parser file results:

| Count | Rule |
|---|---|
| `total` | All requested target files plus skipped/ineligible report rows. |
| `markdownGenerated` | Status `MARKDOWN_GENERATED`. |
| `lowConfidence` | Status `LOW_CONFIDENCE`. |
| `ocrRequired` | Status `OCR_REQUIRED`. |
| `failed` | Status `FAILED`. |
| `skipped` | Ineligible targets not sent to adapter. |
| `unsupported` | Status `UNSUPPORTED`, if adapter returns it safely. |

### Parser Persistence

Add logical domain entities equivalent to the data model:

- `ParserRun`
- `ParserFileResult`
- `ParserRunStatus`
- `ParserAdapterStatus` may reuse the converter status naming pattern or share a neutral adapter status if introduced deliberately.

Use repositories only for parser run/result persistence and existing file/source chunk persistence. `wiki_page` remains untouched.

### Parser Mapper / DTOs

DTOs should match the API guide:

- `ParserCapabilityResponse`
- `CreateParserRunRequest`
- `ParserRunResponse`
- `ParserFileResultResponse`
- `ParserRunSummaryResponse`
- `ParserChunkResponse` or source chunk reuse response where appropriate

All API responses use `ApiEnvelope`.

## API / Interface Design

The API implementation guide is authoritative for payloads:

- `GET /api/parser-adapters`
- `POST /api/batches/{batchId}/parser-runs`
- `GET /api/parser-runs/{runId}`

Validation failures use the existing API envelope/error handling style. Authentication remains deferred/internal-only for this slice.

## Data Design

Data model lives in `docs/04-architecture/parser-adapter-data-model.md`.

Important invariants:

- No new `FileStatus` values.
- `markdownPath`, `assetsPath`, and parser result paths must pass relative-path validation.
- This slice validates metadata paths and persists source chunk trace metadata; byte-level Markdown front matter validation is deferred to the runtime/storage slice that materializes Markdown bytes.
- `reviewStatus` remains `REVIEW_REQUIRED` for generated/low-confidence/OCR-required parser output.
- `source_chunk` rows are inserted only after the parser file result passes validation.
- Parser run summaries are derived from result rows.

## Workflow / Execution Design

### Successful Parse

1. API receives parser run request.
2. Service validates batch and file ids.
3. Service filters eligible files and records skipped/ineligible results.
4. Registry resolves parser adapter.
5. Service creates parser run and marks it `RUNNING`.
6. Adapter returns Markdown/assets/chunk result for each eligible file.
7. Service validates each result.
8. Service updates `file_item`, persists `source_chunk`, persists parser result.
9. Service computes summary and terminal status.
10. API returns parser run response.

### Adapter Unavailable

- Parser run becomes `FAILED`.
- File items remain unchanged.
- Response contains safe message only.

### Unsafe Result

- Validation fails before unsafe metadata persists.
- Response uses validation error shape.
- Safe result rows may be discarded for atomicity unless implementation explicitly supports per-file safe partial persistence; default: reject the run result as invalid.

## Validation And Error Handling

| Case | Expected handling |
|---|---|
| Unknown batch | 404 safe not found. |
| Unknown target file | 404 safe not found. |
| Cross-batch file id | 400 validation error or 404 safe not found; do not reveal unrelated batch membership. |
| No eligible targets | 400 validation error with safe field message. |
| Unsafe path | 400 validation error before persistence. |
| Confidence outside `[0,1]` | 400 validation error before persistence. |
| Adapter throws runtime exception | Parser run `FAILED`; file items unchanged; sanitized safe message. |
| Parser output contains raw logs/secrets/paths | Store only sanitized `safeError`/`safeMessage`. |

## Edge Case Trace

### Low-Confidence Threshold

Rule: confidence `< 0.800` maps to `LOW_CONFIDENCE`; confidence `>= 0.800` maps to `MARKDOWN_GENERATED`.

| Input | Result |
|---|---|
| `0.799` | `LOW_CONFIDENCE` |
| `0.800` | `MARKDOWN_GENERATED` |
| `null` with success | `MARKDOWN_GENERATED` plus report note that confidence was unavailable |

### Path Safety

Rule: artifact paths must be safe relative paths.

| Input | Result |
|---|---|
| `generated/markdown/a.md` | Accepted |
| `/tmp/a.md` | Rejected |
| `../private/a.md` | Rejected |

### Eligibility

Rule: adapter receives only files with `PDF_CONVERTED` and safe `pdfPath`.

| File | Result |
|---|---|
| `PDF_CONVERTED` + safe PDF path | Sent to adapter |
| `UPLOADED` + no PDF path | Skipped/ineligible |
| `PDF_CONVERT_FAILED` | Skipped/ineligible |

## Testing Considerations

Required implementation verification:

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

Test coverage must include:

- Adapter contract tests with mock parser.
- Summary calculator unit tests.
- Service validation and status mapping tests.
- API contract integration tests.
- Seam guard test for parser engine references.
- Secret/path sanitization tests.

## Risks / Design Tradeoffs

| ID | Risk / Tradeoff | Decision |
|---|---|---|
| DT-PA-001 | Low-confidence threshold could be product-tuned later. | Commit `< 0.800` now and expose it in capability metadata. |
| DT-PA-002 | Per-file invalid parser result could be partially persisted. | Default to rejecting unsafe run result before persistence to keep metadata clean. |
| DT-PA-003 | Wiki page creation could be useful for previews. | Defer `wiki_page` creation to publish/review slice to preserve trust boundary. |

## Open Questions

- OQ-PA-001: Real parser runtime topology.
- OQ-PA-002: Future ownership of `wiki_page` creation.
- OQ-PA-003: Product confirmation of low-confidence threshold.
