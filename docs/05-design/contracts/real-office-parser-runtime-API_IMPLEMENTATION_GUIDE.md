# Real Office Parser Runtime - API Implementation Guide

## Status

Draft for user review. Backend implementation is blocked until user acceptance.

## Overview

This slice reuses the existing converter and parser API surfaces. The API contract change is behavioral: configured runtime mode may execute internal runtime adapters when available, while all responses remain safe, masked, and envelope-based.

## Authentication

Production authentication and RBAC are out of scope. Existing internal/mock-safe API behavior remains unchanged for this slice.

## Error Response Format

Use the existing Atlas API envelope and global error handling. Runtime errors must be converted to safe validation, conflict, not-found, or failed-run responses without raw runtime output.

## API Endpoints Summary

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| List converter adapters | GET | `/api/converter-adapters` | Inspect safe converter capability metadata. |
| Create conversion run | POST | `/api/batches/{batchId}/conversion-runs` | Start mock or configured conversion run. |
| Get conversion run | GET | `/api/conversion-runs/{runId}` | Retrieve conversion run report. |
| List parser adapters | GET | `/api/parser-adapters` | Inspect safe parser capability metadata. |
| Create parser run | POST | `/api/batches/{batchId}/parser-runs` | Start mock or configured parser run. |
| Get parser run | GET | `/api/parser-runs/{runId}` | Retrieve parser run report. |

## Capability Metadata Rules

Capability responses may include:

```json
{
  "adapterKey": "trinity-office",
  "displayName": "Trinity Office Converter",
  "version": "configured",
  "defaultAdapter": false,
  "status": "AVAILABLE",
  "maskedConfig": {
    "command": "configured",
    "externalNetwork": "disabled"
  }
}
```

Capability responses must not include:

- raw command path
- raw endpoint
- hostname
- token, password, API key, or credential value
- private absolute path
- raw environment variable value

## Create Conversion Run

### Request

```json
{
  "adapterKey": "trinity-office",
  "requestedBy": "knowledge-manager",
  "mode": "configured",
  "fileIds": ["file-001", "file-002"]
}
```

### Behavior

- `mode=mock` or blank uses mock-safe behavior.
- `mode=configured` may use configured runtime only when the resolved adapter reports `AVAILABLE`.
- Unknown or unavailable adapters fail safely.
- Runtime results are validated by existing conversion result rules before persistence.

### Error Cases

| Case | Response behavior |
|---|---|
| Unknown batch | Safe not-found envelope. |
| Unknown file id | Safe not-found or validation envelope. |
| Adapter unavailable/misconfigured | Failed run when possible; safe summary only. |
| Runtime timeout | Failed or partial-failed run with safe timeout summary. |
| Unsafe `pdfPath` | Validation error; unsafe path not persisted. |

## Create Parser Run

### Request

```json
{
  "adapterKey": "document-normalize",
  "requestedBy": "knowledge-manager",
  "mode": "configured",
  "fileIds": ["file-pdf-001"]
}
```

### Behavior

- Only eligible `PDF_CONVERTED` files with safe `pdfPath` are sent to parser runtime.
- Ineligible files are skipped or rejected according to existing parser service behavior.
- Runtime Markdown/assets paths and chunks must pass validation before persistence.
- Generated content remains review-required.

### Error Cases

| Case | Response behavior |
|---|---|
| No eligible target | Validation error. |
| Adapter unavailable/misconfigured | Failed run when possible; safe summary only. |
| Runtime timeout | Failed or partial-failed run with safe timeout summary. |
| Unsafe Markdown/assets path | Validation error; unsafe path not persisted. |
| Duplicate chunk id | Validation error before unsafe chunk persistence. |

## State Reference

```text
Run:
  REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED

Capability:
  AVAILABLE | DISABLED | MISCONFIGURED
```

## Concurrency

Existing active-run rules continue to apply. A batch must not have conflicting active conversion or parser runs for the same run type.

## Integration Dependencies

- Runtime command/worker config is server-side only.
- Timeout default is 120 seconds per adapter invocation.
- Captured output limit is 65536 bytes per invocation.
- Runtime smoke tests are opt-in and must self-skip when config is absent.

## Testing Contract

- API contract tests for capability masking and configured mode failure/success paths with fake runtime executor.
- Service tests for safe status mapping, timeout, invalid manifest, unsafe path, and sanitized messages.
- Seam guard tests for adapter boundary.
- No default test requires real `trinity-office` or `document-normalize`.
