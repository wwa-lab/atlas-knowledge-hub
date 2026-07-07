# 数据模型：Answer Review Governance

最后更新：2026-07-07

## 概览

本切片扩展 `atlas.ask_run`，增加 answer governance metadata。`atlas.ask_evidence` 保持不变，继续保存 citation/source-trace snapshots。

## 实体关系

```text
┌──────────────────────────┐       1:N       ┌──────────────────────────┐
│ atlas.ask_run            │────────────────▶│ atlas.ask_evidence       │
│ answer governance fields │                 │ immutable evidence refs  │
└──────────────────────────┘                 └──────────────────────────┘
```

## `atlas.ask_run`

| Field | Type | Nullable | Description |
|---|---|---:|---|
| `answer_review_status` | text enum | no | `REVIEW_REQUIRED`、`APPROVED`、`REJECTED`、`NEEDS_REVISION`。 |
| `answer_review_reason` | text | yes | 安全 reviewer reason 或 governance note。 |
| `answer_reviewed_by` | text | yes | 安全 reviewer identifier/display value。 |
| `answer_reviewed_at` | timestamptz | yes | Review action timestamp。 |

## 状态规则

| Status | Reusable | Notes |
|---|---:|---|
| `REVIEW_REQUIRED` | false | Generated answers 默认值。 |
| `APPROVED` | true only with answer text and eligible evidence | 可供未来 answer reuse 使用。 |
| `REJECTED` | false | 必须包含 reason。 |
| `NEEDS_REVISION` | false | 必须包含 reason。 |

## Migration

添加 Flyway migration `V16__answer_review_governance.sql`：

- 删除旧的 ask answer review check constraint。
- 添加 metadata columns。
- 为 answer governance values 添加新的 status check。
- 添加按 space/status 查询治理状态的 index。

## API Type Mapping

| Domain | Backend DTO | Frontend Type |
|---|---|---|
| `AnswerReviewStatus` | `AskRunResponse.answerReviewStatus` | `ApiAnswerReviewStatus` |
| metadata fields | `AskRunResponse` additive fields | `ApiAskRun` additive fields |
| review request | `ReviewAskAnswerRequest` | `reviewAskAnswer()` payload |
