# Data Model: Answer Review Governance

Last updated: 2026-07-07

## Overview

This slice extends `atlas.ask_run` with answer governance metadata. `atlas.ask_evidence` remains unchanged and continues to hold citation/source-trace snapshots.

## Entity Relationship

```text
┌──────────────────────────┐       1:N       ┌──────────────────────────┐
│ atlas.ask_run            │────────────────▶│ atlas.ask_evidence       │
│ answer governance fields │                 │ immutable evidence refs  │
└──────────────────────────┘                 └──────────────────────────┘
```

## `atlas.ask_run`

| Field | Type | Nullable | Description |
|---|---|---:|---|
| `answer_review_status` | text enum | no | `REVIEW_REQUIRED`, `APPROVED`, `REJECTED`, `NEEDS_REVISION`. |
| `answer_review_reason` | text | yes | Safe reviewer reason or governance note. |
| `answer_reviewed_by` | text | yes | Safe reviewer identifier/display value. |
| `answer_reviewed_at` | timestamptz | yes | Review action timestamp. |

## Status Rules

| Status | Reusable | Notes |
|---|---:|---|
| `REVIEW_REQUIRED` | false | Default for generated answers. |
| `APPROVED` | true only with answer text and eligible evidence | Eligible for future answer reuse. |
| `REJECTED` | false | Must include reason. |
| `NEEDS_REVISION` | false | Must include reason. |

## Migration

Add Flyway migration `V16__answer_review_governance.sql` to:

- Drop the older ask answer review check constraint.
- Add metadata columns.
- Add a new status check for answer governance values.
- Add an index for space/status governance queries.

## API Type Mapping

| Domain | Backend DTO | Frontend Type |
|---|---|---|
| `AnswerReviewStatus` | `AskRunResponse.answerReviewStatus` | `ApiAnswerReviewStatus` |
| metadata fields | `AskRunResponse` additive fields | `ApiAskRun` additive fields |
| review request | `ReviewAskAnswerRequest` | `reviewAskAnswer()` payload |
