# Data Model: Model Adapter

## Status

Draft. Phase 3 adapter slice. Logical persistence contract for model run evidence. Physical SQL is created only during implementation through Flyway.

## Overview

The model adapter stores model run evidence and safe output descriptors. It does not store raw prompts, raw provider payloads, raw vectors, credentials, private endpoints, or confidential source text.

## Enums

```text
model_type          : CHAT | EMBEDDING | RERANK | VISION | SPEECH
model_operation     : CHAT | EMBEDDING | RERANK | VISION | SPEECH
model_adapter_status: AVAILABLE | DISABLED | MISCONFIGURED
model_run_status    : REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
model_output_kind   : TEXT_SUMMARY | EMBEDDING_METADATA | RERANK_SCORES |
                      VISION_SUMMARY | SPEECH_SUMMARY | ERROR
review_status       : REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED
source_ref_type     : FILE_ITEM | SOURCE_CHUNK | WIKI_PAGE | GRAPH_NODE | ASK_CONTEXT
```

Generated outputs default to `REVIEW_REQUIRED`. `APPROVED` and `PUBLISHED` are not set by this slice.

## Entity Relationship Diagram

```text
+-----------------+       1:N       +--------------------+
| model_run        |---------------->| model_run_output   |
+-----------------+                 +--------------------+
        |
        | 1:N
        v
+----------------------------+
| model_run_source_reference |
+----------------------------+
```

## Tables

### `model_run`

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Server-created run id. |
| `adapter_key` | text NOT NULL | Adapter that executed or attempted the run. |
| `model_key` | text NOT NULL | Selected model capability. |
| `model_type` | model_type NOT NULL | Capability type. |
| `operation` | model_operation NOT NULL | Requested operation. |
| `status` | model_run_status NOT NULL | Lifecycle state. |
| `mode` | text NOT NULL | `mock` or `configured`; tests use `mock`. |
| `purpose` | text NOT NULL | Safe business purpose label. |
| `requested_by` | text | Mock/internal requester identity. |
| `input_reference` | text | Product reference, not raw prompt. |
| `safe_input_summary` | text | Optional bounded mock-safe summary only. |
| `safe_message` | text | User-safe run summary or error. |
| `prompt_units` | integer | Non-negative usage count when available. |
| `completion_units` | integer | Non-negative usage count when available. |
| `started_at` | timestamp | Server-set. |
| `completed_at` | timestamp | Server-set when terminal. |

### `model_run_output`

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Stable output id. |
| `run_id` | text FK -> `model_run.id` | Owning run. |
| `kind` | model_output_kind NOT NULL | Output descriptor kind. |
| `output_reference` | text | Relative product reference to generated artifact or mock descriptor. |
| `safe_summary` | text | Bounded safe summary; no raw confidential text. |
| `ranked_item_ids` | text array | Rerank output ids when applicable. |
| `embedding_dimension` | integer | Embedding dimension metadata only. |
| `embedding_item_count` | integer | Count of embedded items; no vectors. |
| `confidence` | decimal | Optional `[0,1]`. |
| `review_status` | review_status NOT NULL | Defaults to `REVIEW_REQUIRED`. |
| `safe_error` | text | Sanitized error when this output failed. |

### `model_run_source_reference`

| Column | Type | Notes |
|---|---|---|
| `id` | text PK | Source reference id. |
| `run_id` | text FK -> `model_run.id` | Owning run. |
| `ref_type` | source_ref_type NOT NULL | Source reference type. |
| `ref_id` | text NOT NULL | Product id, not path or URL. |
| `label` | text | Safe display label. |
| `confidence` | decimal | Optional source/evidence confidence. |
| `review_status` | review_status | Source review status when known. |

## Capability Metadata Shape

Capabilities may be computed from configured adapters rather than persisted:

| Field | Notes |
|---|---|
| `adapterKey` | Stable adapter id, e.g. `mock-model`. |
| `modelKey` | Stable model capability id. |
| `displayName` | User-safe display name. |
| `providerFamily` | Safe family label, no endpoint. |
| `modelType` | `CHAT`, `EMBEDDING`, `RERANK`, `VISION`, or `SPEECH`. |
| `supportedOperations` | Operation set supported by the capability. |
| `defaultModel` | Default marker per type. |
| `status` | `AVAILABLE`, `DISABLED`, `MISCONFIGURED`. |
| `contextLimit` | Safe numeric limit if known. |
| `maskedConfigSummary` | Status-only fields such as `credential: configured`. |

## Invariants

- No raw secret, endpoint, hostname, organization id, private path, raw prompt, raw provider payload, raw vector, or confidential document text is persisted.
- Model outputs default to `REVIEW_REQUIRED`.
- Embedding outputs store metadata only; vector persistence belongs to `vector-adapter`.
- Source references store product ids and safe labels, not raw source text.
- Usage counts are non-negative.
- Confidence values are nullable or within `[0,1]`.
- Terminal runs cannot move back to `RUNNING`.

## Deferred Data

- Provider credential storage and rotation.
- Cost/quota/account policy tables.
- Ask answer records.
- Vector index records.
- Graph extraction records.
- Wiki publication records.
