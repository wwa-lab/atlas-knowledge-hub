# Data Model: manual-url-knowledge-ingest

## Overview

This slice adds one persistent entity, `manual_url_source`, and extends existing source enums with URL metadata values. Supporting batch/file/chunk records remain in existing tables.

## Entity Relationship

```text
space 1:N manual_url_source
manual_url_source 1:1 batch
manual_url_source 1:1 file_item
file_item 1:N source_chunk
```

## `manual_url_source`

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | text | no | Stable source id. |
| `space_id` | text | no | Knowledge Space id. |
| `url_hash` | text | no | Deterministic hash of sanitized display URL for duplicate detection. |
| `display_url` | text | no | Safe URL without userinfo/query/fragment. |
| `host` | text | no | Normalized public host. |
| `title` | text | yes | Optional user-provided title. |
| `description` | text | yes | Optional user-provided description. |
| `fetch_intent` | text | no | User intent such as `METADATA_ONLY`. |
| `fetch_policy` | text | no | Fixed policy such as `NO_FETCH_METADATA_ONLY`. |
| `ingest_status` | text | no | `REGISTERED`, `FETCH_INTENT_RECORDED`, `REVIEW_REQUIRED`. |
| `review_status` | text | no | `REVIEW_REQUIRED` in this slice. |
| `eligibility_status` | text | no | `REVIEW_REQUIRED_ONLY` in this slice. |
| `confidence` | numeric(4,3) | no | Default `0.300` metadata confidence. |
| `source_trace` | text | no | Safe source trace summary. |
| `batch_id` | text | no | Linked batch metadata id. |
| `file_item_id` | text | no | Linked file item metadata id. |
| `created_by` | text | no | Safe user id/name. |
| `created_at` | timestamptz | no | Creation time. |
| `updated_at` | timestamptz | no | Last update time. |

## Existing Model Extensions

| Existing type | Additive value | Reason |
|---|---|---|
| `SourceKind` | `url` | Batch-level source kind for manual URL metadata. |
| `SourceType` | `url` | File/source item type for URL metadata. |

## State Model

```text
REGISTERED
  -> FETCH_INTENT_RECORDED
  -> REVIEW_REQUIRED
```

Rejected URLs do not create persistent records.

## Review And Eligibility Defaults

| Field | Value |
|---|---|
| `reviewStatus` | `REVIEW_REQUIRED` |
| `eligibilityStatus` | `REVIEW_REQUIRED_ONLY` |
| `confidence` | `0.300` |
| `fetchPolicy` | `NO_FETCH_METADATA_ONLY` |

These defaults enforce `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`.
