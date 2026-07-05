# 数据模型：Wiki Ingest v0

## 状态

供用户接受的草稿。仅为逻辑模型；确切 migration changes 在接受后的实现中处理。

## 概述

`wiki-ingest-v0` 复用已完成的 Wiki data model，只增加追踪 Auto Wiki ingest candidates 所需的最小字段或记录。首选实现应为 additive，并应避免重塑现有 `wiki_page`、`wiki_generation_run`、`wiki_log_entry` 与 `wiki_page_issue` contracts，除非测试证明存在缺口。

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
| `id` | Candidate `chunkRefs` evidence id。 |
| `file_item_id` | 通过 batch 将 chunk 连接到 file 与 space。 |
| `page` / `section` | Source trace 的 safe locator。 |
| `confidence` | Candidate confidence input。 |
| `review_status` | 必须为 `APPROVED` 才 eligible。 |

### `wiki_page`

| Field | v0 rule |
|---|---|
| `slug` | 确定性生成，per space unique。 |
| `page_type` | v0 只允许 `TOPIC`。 |
| `source_refs` | 从 source chunks 收集的 safe file refs。 |
| `chunk_refs` | 从 source chunks 收集的 safe chunk refs。 |
| `source_mode` | Generated candidates 使用 `AUTO_GENERATED`。 |
| `refresh_policy` | Generated candidates 使用 `ON_SOURCE_CHANGE`。 |
| `review_status` | v0 所有 generated candidates 使用 `REVIEW_REQUIRED`。 |
| `confidence` | 从 eligible chunk confidence 聚合。 |

### `wiki_generation_run`

| Field | v0 rule |
|---|---|
| `status` | Spec 中定义的 run state。 |
| `source_mode` | `AUTO_GENERATED`。 |
| `refresh_policy` | `ON_SOURCE_CHANGE`。 |
| `input_source_refs` | Safe source/chunk refs and counts。 |
| `created_page_ids` | Run 中创建的 pages。 |
| `updated_page_ids` | Run 中合并的 pages。 |
| `issue_ids` | Conflict 或 exclusion issue ids。 |
| `safe_summary` | 只包含 counts 和 safe labels。 |
| `safe_error` | 只包含 sanitized message。 |

### `wiki_log_entry`

| Event | Meaning |
|---|---|
| `RUN_STARTED` | Ingest run accepted。 |
| `METADATA_UPDATED` | Candidate created 或 merged。 |
| `ISSUE_RECORDED` | Conflict 或 excluded evidence issue recorded。 |
| `RUN_FINISHED` | Run completed、partially failed 或 failed。 |

### `wiki_page_issue`

| Issue | Meaning |
|---|---|
| `REVIEW_REQUIRED` | Generated candidate 需要 SME review。 |
| `MISSING_SOURCE_REF` | Evidence 无法形成 safe trace。 |
| `THIN_CONTENT` | Deterministic candidate 内容不足。 |
| `STALE_SOURCE` | 为后续 refresh/retract 保留；v0 默认不产生。 |

## Candidate Identity Rules

| Rule | Decision |
|---|---|
| Space scope | Candidate uniqueness 是 `(spaceId, slug)`。 |
| Slug source | 确定性 normalized title/topic key。 |
| Rerun behavior | Same evidence key 映射到 same slug。 |
| Trusted collision | 保留 trusted page 并记录 issue。 |
| Draft collision | 将 refs 合并到现有 generated review-required candidate。 |

## Confidence Aggregation

Default v0 rule：candidate confidence 使用 included chunks 的最低 confidence。该规则更保守，可避免高平均值掩盖弱证据。Edge-case trace：

- Chunks `0.95, 0.91, 0.88` -> candidate confidence `0.88`。
- Single chunk `0.72` -> candidate confidence `0.72`。
- Missing confidence -> 从 confidence math 中排除，并记录在 safe summary；如果全部缺失，则 candidate confidence 为 null。

## Persistence Responsibilities

- Backend repositories 持久化 candidate pages、run summaries、logs 和 issues。
- Generated candidate body 以 generated Markdown artifact 持久化，内容为确定性安全摘要与安全 source/chunk labels。
- 不持久化 raw source text、prompt、provider response、secret、stack trace 或 private absolute path。
- 任意新增 schema change 必须 additive 且 Flyway-managed。
