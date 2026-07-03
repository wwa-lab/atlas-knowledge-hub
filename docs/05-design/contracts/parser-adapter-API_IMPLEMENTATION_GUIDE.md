# Parser Adapter — API / Adapter Implementation Guide

## Status

Draft. Required before Phase 3 parser-adapter implementation. Slice `parser-adapter`.

## Overview

This guide defines the internal API and adapter contracts for PDF-to-Markdown/images parsing. The API is internal to Atlas; it does not expose raw document bytes, raw parser logs, raw command configuration, external cloud calls, or private runtime paths.

## Base Conventions

- Base path: `/api`.
- Envelope: reuse `ApiEnvelope`.
- JSON only.
- Auth/RBAC: deferred; internal-only for this slice.
- Paths: relative only; reject traversal, drive prefixes, URI prefixes, host prefixes, and private absolute paths.
- Secrets: masked/status-only; never return raw command paths, tokens, credentials, hostnames, endpoints, or private paths.
- Mode: tests use `mock`; configured runtime behavior remains behind the adapter.

## Adapter Capability Contract

### `GET /api/parser-adapters`

Purpose: list configured parser adapters.

Response:

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "document-normalize",
      "displayName": "Document Normalize Parser",
      "version": "configured",
      "inputTypes": ["pdf"],
      "outputTypes": ["markdown", "assets"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "lowConfidenceThreshold": 0.8,
      "maskedConfigSummary": {
        "runtime": "configured",
        "workingDirectory": "configured",
        "externalNetwork": "disabled"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

`status` values: `AVAILABLE`, `DISABLED`, `MISCONFIGURED`.

Validation/security:

- Do not return raw command strings, absolute paths, endpoints, environment values, or credentials.
- `version` may be a safe adapter version or `configured`; do not call the parser engine only to discover a version during capability listing.

## Create Parser Run

### `POST /api/batches/{batchId}/parser-runs`

Purpose: execute parsing for selected eligible file items in a batch through the parser adapter contract.

Request:

```json
{
  "adapterKey": "document-normalize",
  "fileIds": ["file-001", "file-002"],
  "requestedBy": "delivery-lead",
  "mode": "mock"
}
```

Rules:

- `adapterKey` is optional; omitted means default parser adapter.
- `fileIds` is optional; omitted means all eligible files in the batch.
- `mode` is optional and must be `mock` or `configured`; tests use `mock`.
- The service validates target files before starting the run.
- Only files with `PDF_CONVERTED` and safe relative `pdfPath` are passed to the adapter.

Success response:

```json
{
  "success": true,
  "data": {
    "runId": "parse-run-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "document-normalize",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock parsing completed with review-required output.",
    "summary": {
      "total": 4,
      "markdownGenerated": 2,
      "lowConfidence": 1,
      "ocrRequired": 1,
      "failed": 0,
      "skipped": 0,
      "unsupported": 0
    },
    "results": [
      {
        "fileId": "file-001",
        "sourcePath": "Discovery/BRD.docx",
        "pdfPath": "generated/pdf/BRD.pdf",
        "status": "MARKDOWN_GENERATED",
        "markdownPath": "generated/markdown/BRD.md",
        "assetsPath": "generated/assets/BRD",
        "confidence": 0.91,
        "reviewStatus": "REVIEW_REQUIRED",
        "chunkCount": 2,
        "skipped": false,
        "safeError": null
      }
    ],
    "chunks": [
      {
        "chunkId": "chunk-file-001-001",
        "fileId": "file-001",
        "sourceFile": "Discovery/BRD.docx",
        "page": 3,
        "section": "Business Rules",
        "confidence": 0.91,
        "reviewStatus": "REVIEW_REQUIRED"
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:03Z"
  },
  "error": null,
  "meta": null
}
```

Error cases:

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter key, unsafe path, invalid mode, no eligible targets, invalid confidence, cross-batch target, invalid parser result. |
| 404 | `NOT_FOUND` | Batch or file id is unknown. |
| 409 | `CONFLICT` | Parser run cannot start because the batch already has an active parser run. |
| 500 | `INTERNAL_ERROR` | Unexpected adapter fault; details logged safely server-side only. |

## Get Parser Run

### `GET /api/parser-runs/{runId}`

Purpose: return parser run summary, per-file report, and chunk summary.

Response shape: same `data` body as create parser run.

## Internal Adapter Interface Contract

Conceptual interface:

```text
ParserAdapter
  capability() -> ParserCapability
  parse(ParserRequest) -> ParserResult
```

Required request fields:

| Field | Description |
|---|---|
| `runId` | Server-created parser run id. |
| `batchId` | Existing batch id. |
| `files[]` | File item descriptors: file id, source path, source type, current status, PDF path. |
| `artifactRoot` | Optional relative artifact root hint, default `generated/markdown`. |
| `lowConfidenceThreshold` | Decimal threshold, default `0.800`. |

Required result fields:

| Field | Description |
|---|---|
| `adapterKey` | Adapter that produced the result. |
| `files[]` | Per-file parser result. |
| `safeMessage` | Optional sanitized run-level summary. |

Per-file result fields:

| Field | Description |
|---|---|
| `fileId` | Existing file item id. |
| `status` | Existing `FileStatus` result. |
| `markdownPath` | Relative generated Markdown path when available. |
| `assetsPath` | Relative generated assets path when available. |
| `confidence` | Optional `[0,1]`. |
| `reviewStatus` | Current file review status. |
| `chunkCount` | Number of accepted source chunks. |
| `skipped` | `true` only for skipped/ineligible report rows; skipped rows preserve the file's unchanged current status. |
| `safeError` | Sanitized error summary. |
| `chunks[]` | Source trace chunks for the file. |

Chunk fields:

| Field | Description |
|---|---|
| `chunkId` | Stable chunk id unique in the parser run. |
| `page` | Positive page number when available. |
| `section` | User-safe section label. |
| `confidence` | Optional `[0,1]`. |
| `reviewStatus` | Defaults to `REVIEW_REQUIRED`. |

## Status Mapping

| Source / Outcome | Result Status |
|---|---|
| Eligible PDF success, confidence `>= 0.800` | `MARKDOWN_GENERATED` |
| Eligible PDF success, confidence `< 0.800` | `LOW_CONFIDENCE` |
| Parser indicates OCR required | `OCR_REQUIRED` |
| Parser failure | `FAILED` |
| Unsupported parser outcome | `UNSUPPORTED` |
| Ineligible selected file | skipped report entry, file unchanged |

No new `FileStatus` values are allowed in this slice.

## Contract Tests

Run before implementation completion:

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

The seam scan must return no matches in non-adapter product layers. Adapter implementation packages may contain parser engine names only when covered by guard tests and no outbound network clients leak into product layers.
