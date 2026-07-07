# 数据模型：manual-url-knowledge-ingest

## Overview

本切片新增一个持久化实体 `manual_url_source`，并以 additive 方式扩展现有 source enums。Supporting batch/file/chunk records 继续使用现有表。

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
| `id` | text | no | Stable source id。 |
| `space_id` | text | no | Knowledge Space id。 |
| `url_hash` | text | no | Sanitized display URL 的 deterministic hash，用于 duplicate detection。 |
| `display_url` | text | no | 无 userinfo/query/fragment 的安全 URL。 |
| `host` | text | no | 归一化 public host。 |
| `title` | text | yes | 可选用户输入 title。 |
| `description` | text | yes | 可选用户输入 description。 |
| `fetch_intent` | text | no | 用户意图，例如 `METADATA_ONLY`。 |
| `fetch_policy` | text | no | 固定策略，例如 `NO_FETCH_METADATA_ONLY`。 |
| `ingest_status` | text | no | `REGISTERED`、`FETCH_INTENT_RECORDED`、`REVIEW_REQUIRED`。 |
| `review_status` | text | no | 本切片为 `REVIEW_REQUIRED`。 |
| `eligibility_status` | text | no | 本切片为 `REVIEW_REQUIRED_ONLY`。 |
| `confidence` | numeric(4,3) | no | 默认 metadata confidence `0.300`。 |
| `source_trace` | text | no | 安全 source trace summary。 |
| `batch_id` | text | no | 关联 batch metadata id。 |
| `file_item_id` | text | no | 关联 file item metadata id。 |
| `created_by` | text | no | 安全 user id/name。 |
| `created_at` | timestamptz | no | 创建时间。 |
| `updated_at` | timestamptz | no | 更新时间。 |

## Existing Model Extensions

| Existing type | Additive value | Reason |
|---|---|---|
| `SourceKind` | `url` | Manual URL metadata 的 batch-level source kind。 |
| `SourceType` | `url` | URL metadata 的 file/source item type。 |

## State Model

```text
REGISTERED
  -> FETCH_INTENT_RECORDED
  -> REVIEW_REQUIRED
```

被拒绝的 URL 不创建持久化记录。

## Review And Eligibility Defaults

| Field | Value |
|---|---|
| `reviewStatus` | `REVIEW_REQUIRED` |
| `eligibilityStatus` | `REVIEW_REQUIRED_ONLY` |
| `confidence` | `0.300` |
| `fetchPolicy` | `NO_FETCH_METADATA_ONLY` |

这些默认值强制满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`。
