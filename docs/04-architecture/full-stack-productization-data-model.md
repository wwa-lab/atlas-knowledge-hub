# Data Model: Full-Stack Productization

## Overview

This slice reuses existing Atlas metadata entities. It does not introduce new production tables unless implementation discovers a blocking contract gap.

## Entities

| Entity | Purpose | P0 Fields Used |
|---|---|---|
| Space | Knowledge Space container. | id, name, description, owner, status, counts, timestamps. |
| Batch | Metadata-only upload/package record. | id, spaceId, name, sourceKind, owner, uploadedAt, metrics. |
| FileItem | Source file metadata and processing status. | id, batchId, sourcePath, sourceType, status, confidence, reviewStatus, markdownPath, errorMessage. |
| SourceChunk | Source trace unit for review, graph, vector, and Ask. | id, fileItemId, sourceFile, page, section, confidence, reviewStatus. |
| ReviewRecord | Append-only SME review event. | targetType, targetId, action, reviewer, comment, affectedChunks, createdAt. |
| WikiPage | Published trusted Wiki metadata. | id, spaceId, title, markdownPath, sourceDocumentIds, confidence, reviewStatus, owner, lastUpdated. |
| GraphProjectionRun | Backend graph refresh run. | id, spaceId, status, summary, safeMessage. |
| GraphNode / GraphEdge | Trusted graph view. | id, spaceId, label/type, evidenceChunkIds, confidence, reviewStatus. |
| VectorRun / VectorItemResult | Adapter-backed vector index run. | id, spaceId, batchId, sourceChunkId, status, reviewPolicy. |
| AskRun / AskEvidence | Trusted Ask answer and citations. | runId, spaceId, status, answer, answerReviewStatus, modelRunId, evidence rows. |

## Relationships

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

## State Values

| Model | P0 States |
|---|---|
| File review | `REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`, `PUBLISHED` |
| File status | `MARKDOWN_GENERATED`, `LOW_CONFIDENCE`, `OCR_REQUIRED`, `PDF_CONVERT_FAILED`, `UNSUPPORTED`, `PUBLISHED` |
| Wiki review | `PUBLISHED` for trusted read list. |
| Ask run | `SUCCEEDED`, `NO_EVIDENCE`, `FAILED`, `PARTIAL_FAILED` as returned by backend. |
| Ask answer review | Always `REVIEW_REQUIRED` in this slice. |

## Sample Batch Shape

The browser sample upload must use safe relative paths such as `samples/p0/productization-<timestamp>.md`, review-required file status, `markdownPath`, confidence, and at least one source chunk. Chunk review status may be created as `APPROVED` only as source evidence metadata; file review status starts `REVIEW_REQUIRED` and is approved through the review API.
