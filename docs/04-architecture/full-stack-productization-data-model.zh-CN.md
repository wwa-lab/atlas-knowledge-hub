# 数据模型：全栈产品化

## 概述

本切片复用现有 Atlas metadata entities。除非实现发现阻塞性契约缺口，否则不引入新的生产表。

## 实体

| Entity | Purpose | P0 Fields Used |
|---|---|---|
| Space | Knowledge Space 容器。 | id, name, description, owner, status, counts, timestamps. |
| Batch | Metadata-only upload/package record。 | id, spaceId, name, sourceKind, owner, uploadedAt, metrics. |
| FileItem | Source file metadata 与处理状态。 | id, batchId, sourcePath, sourceType, status, confidence, reviewStatus, markdownPath, errorMessage. |
| SourceChunk | Review、graph、vector、Ask 的 source trace 单元。 | id, fileItemId, sourceFile, page, section, confidence, reviewStatus. |
| ReviewRecord | Append-only SME review event。 | targetType, targetId, action, reviewer, comment, affectedChunks, createdAt. |
| WikiPage | 已发布可信 Wiki metadata。 | id, spaceId, title, markdownPath, sourceDocumentIds, confidence, reviewStatus, owner, lastUpdated. |
| GraphProjectionRun | 后端 graph refresh run。 | id, spaceId, status, summary, safeMessage. |
| GraphNode / GraphEdge | 可信 graph view。 | id, spaceId, label/type, evidenceChunkIds, confidence, reviewStatus. |
| VectorRun / VectorItemResult | Adapter-backed vector index run。 | id, spaceId, batchId, sourceChunkId, status, reviewPolicy. |
| AskRun / AskEvidence | Trusted Ask answer 与 citations。 | runId, spaceId, status, answer, answerReviewStatus, modelRunId, evidence rows. |

## 关系

```text
Space 1:N Batch
Batch 1:N FileItem
FileItem 1:N SourceChunk
FileItem 1:N ReviewRecord
FileItem 1:N WikiPage.sourceDocumentIds
SourceChunk 1:N GraphNode/GraphEdge evidence references
SourceChunk 1:N VectorItemResult
AskRun 1:N AskEvidence -> SourceChunk/FileItem
```

## 状态值

| Model | P0 States |
|---|---|
| File review | `REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`, `PUBLISHED` |
| File status | `MARKDOWN_GENERATED`, `LOW_CONFIDENCE`, `OCR_REQUIRED`, `PDF_CONVERT_FAILED`, `UNSUPPORTED`, `PUBLISHED` |
| Wiki review | trusted read list 中为 `PUBLISHED`。 |
| Ask run | 后端返回的 `SUCCEEDED`, `NO_EVIDENCE`, `FAILED`, `PARTIAL_FAILED`。 |
| Ask answer review | 本切片始终为 `REVIEW_REQUIRED`。 |

## Sample Batch Shape

浏览器 sample upload 必须使用安全相对路径，例如 `samples/p0/productization-<timestamp>.md`，文件 review status 为 review-required，包含 `markdownPath`、confidence 和至少一个 source chunk。Chunk review status 可作为 source evidence metadata 创建为 `APPROVED`；file review status 从 `REVIEW_REQUIRED` 开始，并通过 review API 批准。
