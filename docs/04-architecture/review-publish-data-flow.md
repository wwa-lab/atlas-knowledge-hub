# Data Flow: Review Publish

## Status

Draft.

## Flow 1: Review Queue Read

```text
User opens Processing Center
  -> Frontend requests /api/spaces/{spaceId}/review-queues
  -> API derives queue counts from file/chunk/wiki metadata
  -> API returns blocked categories and publish-ready candidates
  -> Frontend renders counts, actions, and downstream blocking copy
```

## Flow 2: SME Review Decision

```text
Reviewer submits action
  -> POST /api/files/{fileId}/reviews
  -> Validate action/comment/affectedChunks
  -> Append review_record
  -> Update file_item.review_status to APPROVED / NEED_FIX / OCR_REQUIRED
  -> Return created review record
```

## Flow 3: Publish Approved Markdown

```text
Admin selects publish
  -> POST /api/files/{fileId}/publish
  -> Load file item, source chunks, and existing wiki page if any
  -> Check APPROVED, relative markdown_path, confidence, source documents, source trace
  -> If ineligible: return user-safe 400/409 and mutate nothing
  -> If eligible: create/update wiki_page with review_status=PUBLISHED
  -> Return published wiki page metadata
```

## Flow 4: Downstream Consumption

```text
Wiki reads published pages
  -> GET /api/spaces/{spaceId}/wiki-pages
  -> Graph and Ask future slices consume only published/approved/source-traced records
  -> Blocked Processing Center items remain excluded
```

## Error Cascades

| Condition | Result |
|---|---|
| Missing source trace | Publish blocked; no wiki mutation. |
| `reviewStatus != APPROVED` | Publish blocked; reviewer action required. |
| Missing or absolute Markdown path | Publish blocked; validation error. |
| Missing confidence | Publish blocked; quality metadata incomplete. |
| Unknown file/space/page id | `404 NOT_FOUND`. |
| Concurrent publish conflict | `409 CONFLICT`; no partial update. |

## Verification Hooks

- Unit tests for state transitions and eligibility.
- Integration tests for review queue, publish success, and publish failure.
- E2E tests for Processing Center to published Wiki state.
- Static scans for new network calls, direct adapter calls, secrets, and private paths.
