# Data Model: Review Publish

## Status

Draft. Extends the Phase 2 metadata model for publish behavior.

## Entity Relationship Summary

```text
space 1:N file_item
space 1:N wiki_page
file_item 1:N source_chunk
file_item 1:N review_record
wiki_page N:1 space
wiki_page N:N source documents via source_document_ids
```

## Reused Entities

### `file_item`

Relevant fields:

- `id`
- `batch_id`
- `source_path` (relative only)
- `status`
- `confidence`
- `review_status`
- `markdown_path`
- `error_message`

Publish reads `file_item`; it may update `review_status` only as part of the explicit publish transition to `PUBLISHED` when the implementation chooses file-level publication status. Raw source path and artifact paths remain unchanged.

### `source_chunk`

Relevant fields:

- `id`
- `file_item_id`
- `source_file`
- `page`
- `section`
- `confidence`
- `review_status`

Publish reads source chunks to verify trace coverage. It does not mutate source chunks.

### `review_record`

Append-only fields:

- `id`
- `target_type`
- `target_id`
- `action`
- `reviewer`
- `comment`
- `affected_chunks`
- `created_at`

This slice does not add update or delete behavior.

### `wiki_page`

Relevant fields:

- `id`
- `space_id`
- `title`
- `markdown_path`
- `source_document_ids`
- `confidence`
- `review_status`
- `owner`
- `last_updated`

Publish creates or updates this entity and sets `review_status=PUBLISHED`.

## Enums

```text
review_status: REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED
review_action: APPROVE | NEED_FIX | OCR_REQUIRED
```

## State Rules

- Generated or LLM-modified content defaults to `REVIEW_REQUIRED`.
- `APPROVE` results in `APPROVED`; publish is a separate operation.
- `NEED_FIX` and `OCR_REQUIRED` block publish.
- `PUBLISHED` is only valid for content that passed eligibility.

## Validation Rules

| Field | Rule |
|---|---|
| `markdown_path` | Required for publish; relative; traversal-free. |
| `source_document_ids` | At least one id for publish. |
| `confidence` | Required for publish; 0 to 1. |
| `review_status` | Must be `APPROVED` before publish. |
| `source_trace` | At least one source chunk or equivalent trace record must exist. |

## Deferred Model Work

- Dedicated publish audit entity is deferred unless implementation review finds review history insufficient.
- Production user identity and RBAC tables are deferred to a security/auth slice.
- Graph nodes/edges and Ask indexes are not changed by this slice.
