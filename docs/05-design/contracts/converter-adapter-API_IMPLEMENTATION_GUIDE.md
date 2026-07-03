# Converter Adapter — API / Adapter Implementation Guide

## Status

Draft. Required before Phase 3 implementation. Slice `converter-adapter`.

## Overview

This guide defines the internal API and adapter contracts for Office-to-PDF conversion. The API is internal to Atlas; it does not expose raw document bytes, raw command configuration, or external cloud calls.

## Base Conventions

- Base path: `/api`.
- Envelope: reuse `ApiEnvelope`.
- JSON only.
- Auth/RBAC: deferred; internal-only for this slice.
- Paths: relative only; reject traversal, drive prefixes, host prefixes, and private absolute paths.
- Secrets: masked/status-only; never return raw command paths, tokens, credentials, or private endpoints.

## Adapter Capability Contract

### `GET /api/converter-adapters`

Purpose: list configured converter adapters.

Response:

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "trinity-office",
      "displayName": "Trinity Office Converter",
      "version": "configured",
      "outputType": "pdf",
      "supportedSourceTypes": ["pptx", "docx", "xlsx", "pdf"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "maskedConfigSummary": {
        "command": "configured",
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

- Do not return raw command strings or absolute paths.
- `version` may be a safe adapter version or the word `configured`; do not shell out only to discover version during capability listing.

## Create Conversion Run

### `POST /api/batches/{batchId}/conversion-runs`

Purpose: execute conversion for selected file items in a batch through the converter adapter contract.

Request:

```json
{
  "adapterKey": "trinity-office",
  "fileIds": ["file-001", "file-002"],
  "requestedBy": "delivery-lead",
  "mode": "mock"
}
```

Rules:

- `adapterKey` optional; omitted means default converter adapter.
- `fileIds` optional; omitted means all eligible files in the batch.
- `mode` is optional and must be `mock` or `configured`; tests use `mock`.
- The service validates target files before starting the run.

Success response:

```json
{
  "success": true,
  "data": {
    "runId": "conv-run-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "trinity-office",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock conversion completed with partial failures.",
    "summary": {
      "total": 4,
      "pdfConverted": 2,
      "pdfConvertFailed": 1,
      "ocrRequired": 1,
      "unsupported": 0,
      "skipped": 0
    },
    "results": [
      {
        "fileId": "file-001",
        "sourcePath": "Discovery/BRD.docx",
        "sourceType": "docx",
        "status": "PDF_CONVERTED",
        "pdfPath": "generated/pdf/BRD.pdf",
        "confidence": 1.0,
        "reviewStatus": "REVIEW_REQUIRED",
        "safeError": null
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
| 400 | `VALIDATION_ERROR` | Unknown adapter key, unsafe path, invalid target list, unsupported request mode. |
| 404 | `NOT_FOUND` | Batch or file id is unknown. |
| 409 | `CONFLICT` | Conversion run cannot start because the batch is already locked by an active run. |
| 500 | `INTERNAL_ERROR` | Unexpected adapter fault; details logged safely server-side only. |

## Get Conversion Run

### `GET /api/conversion-runs/{runId}`

Purpose: return conversion run summary and per-file report.

Response shape: same `data` body as create conversion run.

## Internal Adapter Interface Contract

Conceptual interface:

```text
ConverterAdapter
  capability() -> ConverterCapability
  convert(ConverterRequest) -> ConverterResult
```

Required request fields:

| Field | Description |
|---|---|
| `runId` | Server-created conversion run id. |
| `batchId` | Existing batch id. |
| `files[]` | File item descriptors: file id, source path, source type, current status. |
| `artifactRoot` | Optional relative artifact root hint. |

Required result fields:

| Field | Description |
|---|---|
| `adapterKey` | Adapter that produced the result. |
| `files[]` | Per-file conversion result. |
| `safeMessage` | Optional sanitized run-level summary. |

Per-file result fields:

| Field | Description |
|---|---|
| `fileId` | Existing file item id. |
| `status` | Existing `FileStatus` result. |
| `pdfPath` | Relative generated/pass-through path when available. |
| `confidence` | Optional `[0,1]`. |
| `safeError` | Sanitized error summary. |

## Status Mapping

| Source Type / Outcome | Result Status |
|---|---|
| `pptx`, `docx`, `xlsx` success | `PDF_CONVERTED` |
| `pptx`, `docx`, `xlsx` failure | `PDF_CONVERT_FAILED` |
| `pdf` pass-through | `PDF_CONVERTED` |
| `image` requires OCR | `OCR_REQUIRED` |
| `unsupported` | `UNSUPPORTED` |

No new `FileStatus` values are allowed in this slice.

## Contract Tests

Run before completion:

```bash
cd backend && mvn verify
git diff --check
rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md
```

The `rg` command looking for command/network APIs should return no matches in non-adapter product layers. Adapter implementation packages may contain command-runner boundaries only when covered by seam guard tests.
