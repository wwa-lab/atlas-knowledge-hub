# 数据流：manual-url-knowledge-ingest

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
| `url` | `display_url`, `host`, `url_hash` | `displayUrl`, `host` | Query/fragment/userinfo 不存储。 |
| `title` | `title` | `title` | 可选、脱敏、有长度边界。 |
| `description` | `description` | `description` | 可选、脱敏、有长度边界。 |
| `fetchIntent` | `fetch_intent` | `fetchIntent` | 仅 metadata。 |
| implied | `fetch_policy` | `fetchPolicy` | 本切片固定为 metadata-only。 |
| implied | `source_trace` | `sourceTrace` | 人可读安全 trace summary。 |

## Acceptance Coverage

本数据流覆盖 `AC-MANUAL-URL-KNOWLEDGE-INGEST-001`、`AC-MANUAL-URL-KNOWLEDGE-INGEST-002`、`AC-MANUAL-URL-KNOWLEDGE-INGEST-003` 与 `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`。
