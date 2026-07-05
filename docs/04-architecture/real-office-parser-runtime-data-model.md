# Data Model: Real Office Parser Runtime

## Status

Draft for user review.

## Overview

This slice primarily reuses existing converter/parser metadata models. It may add runtime configuration metadata or runtime-attempt evidence only if implementation needs it, but raw runtime config, credentials, command paths, and private endpoints are not persisted as product data.

## Entity Relationship Summary

```text
┌────────────┐ 1:N ┌────────────────┐ 1:N ┌────────────────────────┐
│ Batch      │────▶│ FileItem       │────▶│ ConversionFileResult   │
└────────────┘     └───────┬────────┘     └────────────────────────┘
                           │ 1:N
                           ▼
                    ┌────────────────┐ 1:N ┌─────────────┐
                    │ ParserFileResult│────▶│ SourceChunk │
                    └────────────────┘     └─────────────┘

┌───────────────┐ 1:N ┌────────────────────────┐
│ ConversionRun │────▶│ ConversionFileResult   │
└───────────────┘     └────────────────────────┘

┌───────────┐ 1:N ┌────────────────┐
│ ParserRun │────▶│ ParserFileResult│
└───────────┘     └────────────────┘
```

## Reused Entity Contracts

| Entity | Role In This Slice | Runtime-Specific Rule |
|---|---|---|
| `FileItem` | Latest source file status, confidence, PDF path, Markdown path, assets path, safe error. | Runtime updates must preserve review status and source identity. |
| `ConversionRun` | Conversion execution record. | Configured runtime attempts use existing run states and safe summaries. |
| `ConversionFileResult` | Per-file converter evidence. | Runtime result paths and errors must be safe before persistence. |
| `ParserRun` | Parser execution record. | Configured runtime attempts use existing mode/status/threshold evidence. |
| `ParserFileResult` | Per-file parser evidence. | Runtime output must use accepted file statuses only. |
| `SourceChunk` | Parser-emitted source trace. | Chunks remain review-required unless later review changes them. |

## Runtime Configuration Model

| Field | Type | Persistence | Rule |
|---|---|---|---|
| adapter key | string | request/config | Must resolve through existing registry. |
| runtime mode | enum-like string | request/run evidence | `mock` or `configured`; configured is opt-in. |
| command status | status-only string | capability response | `configured`, `missing`, `disabled`; raw path not exposed. |
| worker endpoint status | status-only string | capability response | `configured`, `missing`, `disabled`; raw endpoint not exposed. |
| timeout seconds | integer | config | Default must be documented before implementation. |
| max output bytes | integer | config | Default must be documented before implementation. |

## Status Models

### Capability Status

```text
DISABLED -> MISCONFIGURED -> AVAILABLE
```

### Conversion File Status Mapping

| Runtime result | File status |
|---|---|
| PDF output produced | `PDF_CONVERTED` |
| Runtime conversion failed for supported Office input | `PDF_CONVERT_FAILED` |
| Runtime reports OCR needed | `OCR_REQUIRED` |
| Runtime cannot process source type | `UNSUPPORTED` |
| Adapter-level fault without per-file results | File metadata unchanged; run `FAILED` |

### Parser File Status Mapping

| Runtime result | File status |
|---|---|
| Markdown output confidence >= threshold | `MARKDOWN_GENERATED` |
| Markdown output confidence < threshold | `LOW_CONFIDENCE` |
| Runtime reports OCR needed | `OCR_REQUIRED` |
| Runtime parse failed | `FAILED` |
| Runtime cannot process target | `UNSUPPORTED` or skipped before adapter |

## Validation Rules

- Paths must be relative and traversal-free.
- URI, host-prefixed, absolute, drive-prefixed, and private root paths are rejected.
- Confidence values must be in `[0,1]` when present.
- Runtime messages are bounded and sanitized.
- Runtime result adapter key must match the resolved adapter.
- Runtime-generated chunks must have unique ids within the run.

## No New Trust State

This slice does not add new review status or Wiki trust state. Generated or low-confidence runtime outputs remain review-required until a later explicit review or publish workflow changes them.
