# 数据模型：Wiki 数据模型

## 状态

待用户接受的草案。用于未来实现的逻辑模型；确切 SQL 在用户接受后进入 Flyway。

## 概述

本切片扩展现有 `atlas.wiki_page` 表，并新增四个支撑表。模型必须 additive，并保留现有 V1-V9 契约、seeded mock data 和 review-publish 行为。

## 实体关系图

```text
┌──────────────┐ 1:N ┌──────────────┐
│ space        │────▶│ wiki_folder  │
└──────────────┘     └──────┬───────┘
        │ 1:N               │ 1:N optional parent
        ▼                   ▼
┌─────────────────────────────────┐
│ wiki_page                       │
│ folder_id nullable              │
└──────┬───────────┬──────────────┘
       │ 1:N       │ 1:N
       ▼           ▼
┌──────────────┐ ┌──────────────────┐
│ wiki_log     │ │ wiki_page_issue  │
└──────────────┘ └──────────────────┘

┌──────────────────────┐
│ wiki_generation_run  │ N:1 space, optional page
└──────────────────────┘
```

## 实体定义

### `wiki_page`

现有表做 additive 扩展。

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | 稳定 page id。 |
| `space_id` | String | No | 所属 Knowledge Space。 |
| `folder_id` | String | Yes | 可选 folder。 |
| `title` | String | No | 展示标题。 |
| `slug` | String | No | 按 `space_id` scoped 的稳定 URL/lookup key。 |
| `page_type` | String enum | No | `INDEX`, `TOPIC`, `SOURCE_SUMMARY`, `ENTITY`, `CONCEPT`, `MANUAL`。 |
| `markdown_path` | String | Yes | 安全相对 Markdown artifact path。 |
| `source_document_ids` | String array | Yes | 现有兼容字段。 |
| `aliases` | String array | No | 用于 lookup/display 的别名。 |
| `source_refs` | JSON | No | 安全 source references，例如 file ids、labels、path labels、page/section hints。 |
| `chunk_refs` | JSON | No | 按 id 和可选 label 表示的安全 chunk references。 |
| `in_links` | String array | No | 入站 Wiki page slugs/ids。 |
| `out_links` | String array | No | 出站 Wiki page slugs/ids。 |
| `version` | Integer | No | 从 1 开始；未来 refresh/update 时递增。 |
| `source_mode` | String enum | No | `PUBLISHED_FILE`, `AUTO_GENERATED`, `MANUAL`, `HYBRID`。 |
| `refresh_policy` | String enum | No | `MANUAL`, `ON_SOURCE_CHANGE`, `SCHEDULED`, `LOCKED`。 |
| `confidence` | Decimal | Yes | 0 到 1。 |
| `review_status` | String enum | No | 现有 review status values。 |
| `owner` | String | Yes | 安全 owner label。 |
| `last_updated` | Timestamp | Yes | 最后 metadata 更新时间。 |

约束和索引：

- Primary key: `id`。
- Foreign keys: `space_id -> space(id)`, `folder_id -> wiki_folder(id)`。
- Unique: `(space_id, slug)`。
- Index: `(space_id, review_status, title)` 支持当前 list 行为。
- Index: `(space_id, page_type)`。
- Check: `version >= 1`。
- Check: 上方枚举值。

现有 rows 的 migration defaults：

| Field | Default rule |
|---|---|
| `slug` | 从 `markdown_path` 派生 deterministic slug，否则用 `title`，再否则用 `id`；按 space 解决冲突。 |
| `page_type` | `SOURCE_SUMMARY`。 |
| `aliases` | 空数组。 |
| `source_refs` | 从 `source_document_ids` 派生安全 id refs。 |
| `chunk_refs` | 空 JSON array，除非实现时能安全派生 source chunks。 |
| `in_links` / `out_links` | 空数组。 |
| `version` | `1`。 |
| `source_mode` | `PUBLISHED_FILE`。 |
| `refresh_policy` | `MANUAL`。 |

### `wiki_folder`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | 稳定 folder id。 |
| `space_id` | String | No | 所属 space。 |
| `parent_folder_id` | String | Yes | 同一 space 内的 parent folder。 |
| `slug` | String | No | 按 space scoped 的 folder slug。 |
| `name` | String | No | 展示名称。 |
| `description` | String | Yes | 安全描述。 |
| `sort_order` | Integer | No | 确定性展示顺序。 |
| `created_at` | Timestamp | No | 创建时间。 |
| `updated_at` | Timestamp | No | 更新时间。 |

约束：

- Unique `(space_id, slug)`。
- Parent folder 存在时必须属于同一 space。
- 本切片不实现 permission inheritance。

### `wiki_generation_run`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | 稳定 run id。 |
| `space_id` | String | No | 所属 space。 |
| `page_id` | String | Yes | 可选目标 page。 |
| `status` | String enum | No | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`, `CANCELLED`。 |
| `source_mode` | String enum | No | 与 page `source_mode` 相同的值集。 |
| `refresh_policy` | String enum | No | 与 page `refresh_policy` 相同的值集。 |
| `requested_by` | String | Yes | 安全 actor label。 |
| `input_source_refs` | JSON | No | 安全 input references。 |
| `created_page_ids` | String array | No | 安全 page ids。 |
| `updated_page_ids` | String array | No | 安全 page ids。 |
| `issue_ids` | String array | No | 安全 issue ids。 |
| `safe_summary` | String | Yes | 安全摘要。 |
| `safe_error` | String | Yes | 脱敏错误。 |
| `started_at` | Timestamp | Yes | 开始时间。 |
| `finished_at` | Timestamp | Yes | 结束时间。 |

### `wiki_log_entry`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | 稳定 log id。 |
| `space_id` | String | No | 所属 space。 |
| `page_id` | String | Yes | 关联 page。 |
| `run_id` | String | Yes | 关联 generation run。 |
| `event_type` | String enum | No | `PUBLISHED`, `REPUBLISHED`, `METADATA_UPDATED`, `RUN_STARTED`, `RUN_FINISHED`, `ISSUE_RECORDED`。 |
| `actor` | String | Yes | 安全 actor label。 |
| `message` | String | Yes | 脱敏 message。 |
| `metadata` | JSON | No | 仅安全 metadata。 |
| `created_at` | Timestamp | No | 事件时间。 |

### `wiki_page_issue`

| Column | Logical type | Nullable | Description |
|---|---|---:|---|
| `id` | String | No | 稳定 issue id。 |
| `space_id` | String | No | 所属 space。 |
| `page_id` | String | No | 受影响 page。 |
| `issue_type` | String enum | No | `STALE_SOURCE`, `BROKEN_LINK`, `ORPHAN_PAGE`, `THIN_CONTENT`, `REVIEW_REQUIRED`, `MISSING_SOURCE_REF`。 |
| `severity` | String enum | No | `INFO`, `LOW`, `MEDIUM`, `HIGH`。 |
| `status` | String enum | No | `OPEN`, `ACKNOWLEDGED`, `RESOLVED`, `IGNORED`。 |
| `evidence_refs` | JSON | No | 安全 evidence ids/labels。 |
| `message` | String | Yes | 脱敏摘要。 |
| `created_at` | Timestamp | No | 创建时间。 |
| `resolved_at` | Timestamp | Yes | 解决时间。 |

## 状态模型

Generation run：

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

Issue：

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> IGNORED
ACKNOWLEDGED -> RESOLVED
```

## 字段映射

| Existing field/source | New field | Mapping |
|---|---|---|
| `source_document_ids` | `source_refs` | 每个 id 转为 `{ "type": "FILE", "id": "<id>" }`。 |
| `source_chunk.id` | `chunk_refs` | 可用时转为 `{ "type": "SOURCE_CHUNK", "id": "<id>" }`。 |
| `markdown_path` | `slug` | 没有显式 slug 时，从 filename/path 派生 stable slug。 |
| Publish operation | `wiki_log_entry` | 用户接受实现后可写入可选安全生命周期 entry。 |

## 安全规则

- JSON 字段只存安全 IDs、labels、enum values、counts 和 relative references。
- 不存 raw document text、private absolute paths、provider payloads、raw prompts、API keys 或 stack traces。
- 所有写行为都是本地 metadata 行为；没有 external cloud calls。
