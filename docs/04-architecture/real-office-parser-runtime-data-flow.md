# Data Flow: Real Office Parser Runtime

## Status

Draft for user review.

## Overview

This document describes how configured runtime data moves through Atlas while runtime details remain behind adapter boundaries.

## Flow 1: Capability Inspection

```text
Admin UI/API client
  -> Atlas capability endpoint
  -> Adapter registry
  -> Adapter capability
  -> Masked capability response
```

| Step | Data In | Data Out | Safety Rule |
|---|---|---|---|
| Request capability | none | adapter capability list | No raw runtime config requested. |
| Resolve adapters | registered adapters | capability objects | Default marker sorted first. |
| Mask config | server-side config | status-only summary | Use `configured`, `missing`, `disabled`, `not-required`. |
| Return response | capability objects | API envelope | No command path, endpoint, host, secret, or private path. |

## Flow 2: Configured Conversion

```text
Conversion request
  -> Validate batch/files
  -> Resolve converter adapter
  -> Check capability status
  -> Execute runtime inside adapter
  -> Translate runtime output to converter result
  -> Validate status/path/error
  -> Persist run/result/file metadata
```

| Runtime Output | Product Mapping |
|---|---|
| PDF created | `PDF_CONVERTED` plus safe relative `pdfPath` |
| Office conversion failed | `PDF_CONVERT_FAILED` plus safe error |
| OCR needed | `OCR_REQUIRED` plus safe reason |
| Unsupported input | `UNSUPPORTED` plus safe reason |
| Adapter fault / timeout | run `FAILED` or `PARTIAL_FAILED`; safe summary only |

## Flow 3: Configured Parsing

```text
Parser request
  -> Validate batch/files
  -> Filter eligible PDF_CONVERTED files
  -> Resolve parser adapter
  -> Check capability status
  -> Execute runtime inside adapter
  -> Translate runtime manifest/output
  -> Validate markdown/assets/chunks
  -> Persist parser result + source chunks + file metadata
```

| Runtime Output | Product Mapping |
|---|---|
| Markdown generated with confidence >= threshold | `MARKDOWN_GENERATED` |
| Markdown generated with confidence < threshold | `LOW_CONFIDENCE` |
| No extractable content / OCR needed | `OCR_REQUIRED` |
| Parser failed | `FAILED` plus safe error |
| Unsupported parse target | `UNSUPPORTED` or skipped before adapter |

## Flow 4: Failure And Sanitization

```text
Runtime fault / unsafe output
  -> Capture bounded message
  -> Remove secrets, hosts, stack traces, private paths
  -> Reject unsafe path/status/confidence
  -> Persist safe run evidence when possible
  -> Return safe API envelope
```

## Data Lifecycle

| Data Object | Created By | Consumed By | Lifecycle |
|---|---|---|---|
| Runtime config | Server-side deployment config | Adapter capability/runtime executor | Not persisted in product DB as raw values. |
| Runtime stdout/stderr | Runtime process/worker | Adapter runtime implementation | Sanitized/truncated; raw value discarded. |
| PDF artifact path | Converter result | File metadata, parser target selection | Safe relative path only. |
| Markdown/assets path | Parser result | Review/Wiki ingest later | Safe relative path only. |
| Source chunk | Parser result | Review, Wiki, graph, Ask | Review-required unless later approved. |

## Edge Cases

| Case | Required Data Flow |
|---|---|
| Missing runtime config | Capability returns misconfigured/disabled; run fails safely if requested. |
| Runtime returns absolute path | Product validation rejects result before unsafe persistence. |
| Runtime returns secret in stderr | Sanitizer masks/truncates before safe message is persisted. |
| Partial file failure | Successful files may produce safe evidence; failed files get safe status/error according to existing run semantics. |
| No real binary in CI | Mock/fake adapter path runs; opt-in runtime smoke tests self-skip. |
