# Data Model: Wiki Ingest v0

## Status

Draft for user acceptance. Logical model only; exact migration changes belong to implementation after acceptance.

## Overview

`wiki-ingest-v0` reuses the completed Wiki data model and adds only the minimum fields or records needed to track Auto Wiki ingest candidates. The preferred implementation is additive and should avoid reshaping existing `wiki_page`, `wiki_generation_run`, `wiki_log_entry`, and `wiki_page_issue` contracts unless tests prove a gap.

## Entity Relationship Diagram

```text
┌──────────────┐ 1:N ┌──────────────┐ 1:N ┌──────────────┐
│ space        │────▶│ file_item    │────▶│ source_chunk │
└──────┬───────┘     └──────────────┘     └──────┬───────┘
       │                                         │ refs
       │ 1:N                                     ▼
       │                                  ┌──────────────┐
       ├─────────────────────────────────▶│ wiki_page    │
       │                                  └──────┬───────┘
       │                                         │
       ▼                                         ▼
┌──────────────────────┐ 1:N ┌──────────────┐ ┌─────────────────┐
│ wiki_generation_run  │────▶│ wiki_log     │ │ wiki_page_issue │
└──────────────────────┘     └──────────────┘ └─────────────────┘
```

## Existing Entities Reused

### `source_chunk`

| Field | Use in this slice |
|---|---|
| `id` | Candidate `chunkRefs` evidence id. |
| `file_item_id` | Links chunk to file and space through batch. |
| `page` / `section` | Safe locator for source trace. |
| `confidence` | Candidate confidence input. |
| `review_status` | Must be `APPROVED` to be eligible. |

### `wiki_page`

| Field | v0 rule |
|---|---|
| `slug` | Deterministic, unique per space. |
| `page_type` | `TOPIC` only in v0. |
| `source_refs` | Safe file refs collected from source chunks. |
| `chunk_refs` | Safe chunk refs collected from source chunks. |
| `source_mode` | `AUTO_GENERATED` for generated candidates. |
| `refresh_policy` | `ON_SOURCE_CHANGE` for generated candidates. |
| `review_status` | `REVIEW_REQUIRED` for all generated candidates in v0. |
| `confidence` | Aggregated from eligible chunk confidence. |

### `wiki_generation_run`

| Field | v0 rule |
|---|---|
| `status` | Run state from spec. |
| `source_mode` | `AUTO_GENERATED`. |
| `refresh_policy` | `ON_SOURCE_CHANGE`. |
| `input_source_refs` | Safe source/chunk refs and counts. |
| `created_page_ids` | Pages created during run. |
| `updated_page_ids` | Pages merged during run. |
| `issue_ids` | Conflict or exclusion issue ids. |
| `safe_summary` | Counts and safe labels only. |
| `safe_error` | Sanitized message only. |

### `wiki_log_entry`

| Event | Meaning |
|---|---|
| `RUN_STARTED` | Ingest run accepted. |
| `METADATA_UPDATED` | Candidate created or merged. |
| `ISSUE_RECORDED` | Conflict or excluded evidence issue recorded. |
| `RUN_FINISHED` | Run completed, partially failed, or failed. |

### `wiki_page_issue`

| Issue | Meaning |
|---|---|
| `REVIEW_REQUIRED` | Generated candidate needs SME review. |
| `MISSING_SOURCE_REF` | Evidence could not form a safe trace. |
| `THIN_CONTENT` | Deterministic candidate had insufficient content. |
| `STALE_SOURCE` | Reserved for later refresh/retract; not emitted by default in v0. |

## Candidate Identity Rules

| Rule | Decision |
|---|---|
| Space scope | Candidate uniqueness is `(spaceId, slug)`. |
| Slug source | Deterministic normalized title/topic key. |
| Rerun behavior | Same evidence key maps to same slug. |
| Trusted collision | Preserve trusted page and record issue. |
| Draft collision | Merge refs into existing generated review-required candidate. |

## Confidence Aggregation

Default v0 rule: candidate confidence is the minimum confidence of included chunks. This is conservative and prevents a high average from hiding a weak source. Edge-case trace:

- Chunks `0.95, 0.91, 0.88` -> candidate confidence `0.88`.
- Single chunk `0.72` -> candidate confidence `0.72`.
- Missing confidence -> excluded from confidence math and recorded in safe summary; if all are missing, candidate confidence is null.

## Persistence Responsibilities

- Backend repositories persist candidate pages, run summaries, logs, and issues.
- Generated candidate body is persisted as a generated Markdown artifact with deterministic safe summary text and safe source/chunk labels.
- No raw source text, prompt, provider response, secret, stack trace, or private absolute path is persisted.
- Any new schema change must be additive and Flyway-managed.
