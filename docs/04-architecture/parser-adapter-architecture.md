# Architecture: Parser Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/parser-adapter-spec.md`.

## Overview

Parser-adapter extends the existing metadata control plane with a replaceable parser integration seam. The backend owns parser run state, validation, reports, and metadata write-back; parser execution is isolated behind a product-facing adapter contract. `document-normalize` is the first named adapter, but the architecture keeps MinerU, Docling, PaddleOCR, internal OCR, and future parser engines replaceable.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Adapter neutrality | Product services resolve parser adapters through registry/capability contracts. |
| Trace preservation | Markdown, file metadata, and source chunks carry source path, PDF path, confidence, and review status. |
| Mock-only verification | Tests use mock/fake parser engines and do not require real documents or parser binaries. |
| Secret/path safety | Paths are relative; errors and capability summaries are sanitized. |
| Phase discipline | Parser-adapter produces Markdown/assets/chunks but does not publish Wiki pages or run OCR/LLM/graph/Ask. |

## Existing Metadata Context

The current metadata control plane already owns file items, source chunks, review status, confidence, artifact path fields, path safety rules, and adapter seam guarding. Parser-adapter should reuse those product concepts instead of creating a separate parser-owned metadata island. Code-level grounding anchors are recorded in the design and traceability artifacts, where implementation-facing detail belongs.

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend | Out of scope for this slice. Existing UI may later consume parser run APIs, but no frontend changes are part of parser-adapter. |
| Backend API / metadata control plane | Owns parser capability endpoints, parser run lifecycle, validation, metadata write-back, and reports. |
| Parser adapter seam | Encapsulates parser engine-specific execution and returns Atlas product concepts. |
| Parser engine / worker | External to product workflow. `document-normalize` is behind adapter contract only. |
| PostgreSQL metadata | Stores parser run evidence, per-file results, file path/status updates, and source chunks. |

## High-Level Architecture

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Admin, delivery lead, SME reviewer, Codex implementation    |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Parser capability endpoint, parser run endpoint, reports    |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Parser application service                                 |
| Target validation, adapter resolution, status mapping,       |
| safe error handling, metadata write-back                    |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Parser adapter registry      |        | Parser adapters      |
| capability + default policy  |------->| document-normalize   |
+------------------------------+        | mock parser in tests |
                                        +----------+----------+
                                                   |
                                                   | engine/worker boundary
                                                   v
                                        +---------------------+
                                        | Parser engine        |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                         |
| file_item, source_chunk, parser_run, parser_file_result      |
+------------------------------------------------------------+
```

## Component Breakdown

### Backend API

- **Parser adapter capability API:** lists parser capabilities and masked configuration.
- **Parser run API:** creates parser runs for batches and returns run reports.
- **File/source chunk API reuse:** existing file and chunk APIs remain the read surface for file/chunk metadata.

### Application Services

- **Parser run service:** validates target batch/files, resolves adapter, executes mock/configured mode, maps parser results, persists run evidence, and builds reports.
- **Parser summary calculator:** derives totals from parser file results.
- **Parser adapter registry:** owns default adapter resolution and unavailable/misconfigured behavior.
- **Safety helpers:** reuse relative path validation and safe error masking patterns.

### Integration Adapters

- **ParserAdapter contract:** accepts Atlas file metadata and returns Atlas parser results.
- **MockDocumentNormalizeParserAdapter:** deterministic fake implementation for CI and integration tests.
- **DocumentNormalizeParserAdapter:** real adapter boundary placeholder or configured implementation. It may know engine details, but product layers must not.

### Persistence

- Reuse `file_item` for status, `markdown_path`, `assets_path`, confidence, and sanitized errors.
- Reuse `source_chunk` for parser trace chunks.
- Add `parser_run` and `parser_file_result` logical entities for execution evidence and reports.
- Do not create `wiki_page` rows in this slice.

## State And Status Strategy

Parser run status:

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

File status mapping:

- `PDF_CONVERTED` + success confidence `>= 0.80` -> `MARKDOWN_GENERATED`
- `PDF_CONVERTED` + success confidence `< 0.80` -> `LOW_CONFIDENCE`
- parser says OCR required -> `OCR_REQUIRED`
- parser failure -> `FAILED`
- not eligible -> unchanged and reported as skipped/ineligible

Generated and low-confidence content remains `REVIEW_REQUIRED`.

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/parser-adapters` | Admin / implementation tests | List masked parser capabilities. |
| `POST /api/batches/{batchId}/parser-runs` | Internal workflow / future UI | Start a parser run for eligible PDF file metadata. |
| `GET /api/parser-runs/{runId}` | Delivery lead / future UI | Read parser report and per-file outcomes. |
| `ParserAdapter` | Parser service | Execute parser work behind product-facing interface. |

## Security / Reliability / Observability

- Capability responses are masked/status-only.
- Parser errors are bounded and sanitized before persistence or response.
- Parser result paths are validated as safe relative paths.
- Source chunks and Markdown trace preserve evidence without embedding raw confidential document content in logs.
- Mock engines are mandatory for automated verification.
- Seam guard scans non-adapter product layers for direct parser engine, command runner, and outbound client references.

## Risks / Tradeoffs

| ID | Risk / Tradeoff | Mitigation |
|---|---|---|
| R-PA-001 | Real parser runtime topology is not finalized. | Keep real execution behind adapter; mock contract remains stable. |
| R-PA-002 | Parser output may contain raw paths or logs. | Validate paths and sanitize safe messages before persistence. |
| R-PA-003 | Creating Wiki pages too early would blur review/publish boundaries. | Parser-adapter writes Markdown/chunks only; publish owns `wiki_page`. |
| R-PA-004 | Low-confidence threshold may need tuning. | Commit `< 0.80` in SDD and expose it in capability metadata for review. |

## Open Questions

- OQ-PA-001: Real `document-normalize` execution topology.
- OQ-PA-002: Future ownership of draft `wiki_page` rows.
- OQ-PA-003: Product confirmation of `< 0.80` low-confidence threshold.
