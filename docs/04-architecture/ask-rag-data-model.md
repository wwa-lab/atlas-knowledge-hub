# Data Model: Ask RAG

## Status

Draft logical model for `ask-rag`.

## Overview

Ask RAG adds Ask run and Ask evidence records. These records audit the question, retrieval policy, answer status, evidence references, and safe output without storing raw prompts, raw source text, raw vectors, secrets, or provider diagnostics.

## Entity Relationship

```text
KnowledgeSpace 1 ── N AskRun 1 ── N AskEvidence
AskRun N ── 0..1 ModelRun
AskEvidence N ── 1 SourceChunk
SourceChunk N ── 1 FileItem
```

## Entity Definitions

### AskRun

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque Ask run id. |
| `spaceId` | String | Yes | Knowledge Space scope. |
| `status` | AskRunStatus | Yes | `REQUESTED`, `RETRIEVING`, `GENERATING`, `SUCCEEDED`, `NO_EVIDENCE`, `PARTIAL_FAILED`, `FAILED`. |
| `questionSummary` | String | Yes | Bounded safe summary or normalized question label. |
| `requestedBy` | String | Yes | Mock/internal identity. |
| `reviewPolicy` | AskReviewPolicy | Yes | `APPROVED_ONLY` or `INCLUDE_REVIEW_REQUIRED`. |
| `limit` | Integer | Yes | Bounded evidence limit. |
| `mode` | String | Yes | `mock` or configured future mode. |
| `modelRunId` | String | No | Linked model run when generation occurs. |
| `answerText` | String | No | Safe generated answer or no-answer message. |
| `answerConfidence` | Decimal | No | Bounded `[0,1]` when available. |
| `answerReviewStatus` | ReviewStatus | Yes | Always `REVIEW_REQUIRED` for generated answer output in this slice. |
| `safeMessage` | String | No | User-safe status message. |
| `safeError` | String | No | Sanitized failure summary. |
| `createdAt` | Timestamp | Yes | Server timestamp. |
| `completedAt` | Timestamp | No | Server timestamp. |

### AskEvidence

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque evidence id. |
| `askRunId` | String | Yes | Parent Ask run. |
| `sourceChunkId` | String | Yes | Evidence source chunk. |
| `fileItemId` | String | Yes | Source file item. |
| `sourceFile` | String | Yes | Safe source file label. |
| `page` | Integer | No | Page number when available. |
| `section` | String | No | Section label when available. |
| `score` | Decimal | No | Retrieval score `[0,1]`. |
| `confidence` | Decimal | No | Source confidence `[0,1]`. |
| `reviewStatus` | ReviewStatus | Yes | Evidence review state. |
| `safeExcerptLabel` | String | No | Bounded non-confidential label, not raw source text. |

## State Models

### AskRunStatus

```text
REQUESTED -> RETRIEVING -> NO_EVIDENCE
REQUESTED -> RETRIEVING -> GENERATING -> SUCCEEDED
REQUESTED -> RETRIEVING -> GENERATING -> PARTIAL_FAILED
REQUESTED -> RETRIEVING -> FAILED
REQUESTED -> RETRIEVING -> GENERATING -> FAILED
```

### AskReviewPolicy

| Value | Behavior |
|---|---|
| `APPROVED_ONLY` | Uses only approved/published evidence. |
| `INCLUDE_REVIEW_REQUIRED` | Allows review-required evidence and requires warning display. |

## Indexing And Query Notes

- Index `AskRun.spaceId`, `AskRun.status`, `AskRun.createdAt`, and `AskEvidence.askRunId`.
- Ask evidence references source chunks rather than duplicating source content.
- Safe summaries are not a substitute for source trace.

## Data Safety

- No raw prompt, raw provider payload, raw vector, private endpoint, secret, private absolute path, stack trace, or confidential source text fields are allowed.
- `answerText` is generated output and remains `REVIEW_REQUIRED`.
