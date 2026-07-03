# 数据模型：Ask RAG

## 状态

`ask-rag` 的草稿逻辑模型。

## 概览

Ask RAG 新增 Ask run 与 Ask evidence 记录。这些记录审计 question、retrieval policy、answer status、evidence references 和 safe output，但不存 raw prompt、raw source text、raw vector、secret 或 provider diagnostics。

## 实体关系

```text
KnowledgeSpace 1 ── N AskRun 1 ── N AskEvidence
AskRun N ── 0..1 ModelRun
AskEvidence N ── 1 SourceChunk
SourceChunk N ── 1 FileItem
```

## 实体定义

### AskRun

| 字段 | 类型 | 必填 | 描述 |
|---|---|---|---|
| `id` | String | Yes | 不透明 Ask run id。 |
| `spaceId` | String | Yes | Knowledge Space scope。 |
| `status` | AskRunStatus | Yes | `REQUESTED`, `RETRIEVING`, `GENERATING`, `SUCCEEDED`, `NO_EVIDENCE`, `PARTIAL_FAILED`, `FAILED`。 |
| `questionSummary` | String | Yes | 有边界的 safe summary 或 normalized question label。 |
| `requestedBy` | String | Yes | Mock/internal identity。 |
| `reviewPolicy` | AskReviewPolicy | Yes | `APPROVED_ONLY` 或 `INCLUDE_REVIEW_REQUIRED`。 |
| `limit` | Integer | Yes | 有边界 evidence limit。 |
| `mode` | String | Yes | `mock` 或未来 configured mode。 |
| `modelRunId` | String | No | 发生 generation 时关联的 model run。 |
| `answerText` | String | No | Safe generated answer 或 no-answer message。 |
| `answerConfidence` | Decimal | No | 可用时为 `[0,1]`。 |
| `answerReviewStatus` | ReviewStatus | Yes | 本切片中生成回答始终为 `REVIEW_REQUIRED`。 |
| `safeMessage` | String | No | 用户安全状态消息。 |
| `safeError` | String | No | Sanitized failure summary。 |
| `createdAt` | Timestamp | Yes | 服务端时间。 |
| `completedAt` | Timestamp | No | 服务端时间。 |

### AskEvidence

| 字段 | 类型 | 必填 | 描述 |
|---|---|---|---|
| `id` | String | Yes | 不透明 evidence id。 |
| `askRunId` | String | Yes | 父 Ask run。 |
| `sourceChunkId` | String | Yes | Evidence source chunk。 |
| `fileItemId` | String | Yes | Source file item。 |
| `sourceFile` | String | Yes | 安全 source file label。 |
| `page` | Integer | No | 可用时为页码。 |
| `section` | String | No | 可用时为 section label。 |
| `score` | Decimal | No | Retrieval score `[0,1]`。 |
| `confidence` | Decimal | No | Source confidence `[0,1]`。 |
| `reviewStatus` | ReviewStatus | Yes | Evidence review state。 |
| `safeExcerptLabel` | String | No | 有边界非机密 label，不是 raw source text。 |

## 状态模型

### AskRunStatus

```text
REQUESTED -> RETRIEVING -> NO_EVIDENCE
REQUESTED -> RETRIEVING -> GENERATING -> SUCCEEDED
REQUESTED -> RETRIEVING -> GENERATING -> PARTIAL_FAILED
REQUESTED -> RETRIEVING -> FAILED
REQUESTED -> RETRIEVING -> GENERATING -> FAILED
```

### AskReviewPolicy

| 值 | 行为 |
|---|---|
| `APPROVED_ONLY` | 只使用 approved/published evidence。 |
| `INCLUDE_REVIEW_REQUIRED` | 允许 review-required evidence，并要求 warning display。 |

## 索引与查询说明

- 索引 `AskRun.spaceId`、`AskRun.status`、`AskRun.createdAt` 和 `AskEvidence.askRunId`。
- Ask evidence 引用 source chunks，而不是复制 source content。
- Safe summary 不能替代 source trace。

## 数据安全

- 不允许 raw prompt、raw provider payload、raw vector、private endpoint、secret、private absolute path、stack trace 或 confidential source text 字段。
- `answerText` 是 generated output，保持 `REVIEW_REQUIRED`。
