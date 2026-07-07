# Data Flow: manual-url-knowledge-ingest

## Registration Flow

```text
Frontend form
  -> POST /api/spaces/{spaceId}/manual-url-sources
  -> URL validation and sanitization
  -> manual_url_source row
  -> batch row with source_kind=url
  -> file_item row with source_type=url and review_status=REVIEW_REQUIRED
  -> source_chunk row with URL metadata trace and review_status=REVIEW_REQUIRED
  -> ApiEnvelope<ManualUrlSourceResponse>
```

## Validation Failure Flow

```text
Unsafe URL
  -> Manual URL service validation
  -> RequestValidationException
  -> existing GlobalExceptionHandler
  -> safe validation envelope
  -> UI error state without raw unsafe URL
```

## Review Flow

```text
manual_url_source
  -> review-required file_item/source_chunk
  -> existing review queue and Wiki ingest metadata
  -> SME review required
  -> no automatic Wiki publish, Ask eligibility, or Graph extraction
```

## Field Mapping

| Request field | Persisted field | Response field | Notes |
|---|---|---|---|
| `url` | `display_url`, `host`, `url_hash` | `displayUrl`, `host` | Query/fragment/userinfo not stored. |
| `title` | `title` | `title` | Optional, sanitized, bounded. |
| `description` | `description` | `description` | Optional, sanitized, bounded. |
| `fetchIntent` | `fetch_intent` | `fetchIntent` | Metadata only. |
| implied | `fetch_policy` | `fetchPolicy` | Fixed to metadata-only in this slice. |
| implied | `source_trace` | `sourceTrace` | Human-safe trace summary. |

## Acceptance Coverage

This flow covers `AC-MANUAL-URL-KNOWLEDGE-INGEST-001`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`, and `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`.
