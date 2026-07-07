# 数据模型：Ask Session Citations

## 概述

数据模型新增 `ask_session`，在 `ask_run` 上增加 nullable `session_id` ownership，并扩展 `ask_evidence` 作为 answer citation snapshot 表。

## 实体关系

```text
ask_session 1 ── N ask_run 1 ── N ask_evidence
space       1 ── N ask_session
source_chunk 1 ── N ask_evidence
file_item    1 ── N ask_evidence
```

## 实体定义

### ask_session

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | 稳定 session id。 |
| space_id | String | Yes | 所属 Knowledge Space。 |
| title | String | Yes | 安全展示标题。 |
| created_by | String | Yes | 安全 actor label。 |
| created_at | Timestamp | Yes | 创建时间。 |
| updated_at | Timestamp | Yes | 最近 run/session 更新时间。 |

### ask_run

新增字段：

| Field | Type | Required | Description |
|---|---|---|---|
| session_id | String | legacy rows 可为空；new rows 必填 | 所属 Ask session。 |

现有字段保持不变：question、status、review policy、mode、requested by、answer、answer confidence、answer review status、model run id、safe message、created/completed timestamps。

### ask_evidence

新增字段：

| Field | Type | Required | Description |
|---|---|---|---|
| citation_id | String | Yes | 暴露给客户端的稳定 citation id；兼容情况下默认 evidence id。 |
| evidence_label | String | Yes | 从 source file/page/section 派生的安全展示 label。 |
| source_locator | String | Yes | 安全 page/section/chunk locator。 |
| citation_status | String | Yes | `ELIGIBLE`、`REVIEW_REQUIRED`、`LOW_CONFIDENCE` 或 `MISSING_SOURCE_TRACE`。 |
| review_eligible | Boolean | Yes | 仅当 evidence 可支持可信检查时为 true。 |
| excluded_reason | String | No | 不 eligible 时的安全原因。 |

现有字段保持不变：source chunk id、file item id、source file、page、section、review status、confidence、vector item key、score、created at。

## 状态规则

| Condition | citationStatus | reviewEligible | excludedReason |
|---|---|---|---|
| Review status 为 APPROVED 或 PUBLISHED 且 source trace 存在 | ELIGIBLE | true | null |
| Review status 为 REVIEW_REQUIRED | REVIEW_REQUIRED | false | `Evidence requires review.` |
| 可用 confidence 低于 Ask 接受阈值 | LOW_CONFIDENCE | false | `Evidence confidence is low.` |
| Source chunk/file trace 缺失 | MISSING_SOURCE_TRACE | false | `Source trace is missing.` |

## Migration

使用增量 Flyway migration `V15__ask_session_citations.sql`。

## Compatibility

- 现有 `ask_run` rows 的 `session_id` 可以为空；migration 后 service 新建 rows 必须有 session。
- 现有 `ask_evidence` rows 通过 migration 获得安全默认值。
