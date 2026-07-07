# Data Model: worker-retry-dead-letter

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07

## Overview

The data model adds three local reliability entities: worker job, worker job attempt, and dead-letter entry. All schema changes are additive and use mock/sample-safe metadata only.

## Entity Relationship

```text
┌──────────────────────┐ 1       N ┌──────────────────────────┐
│ WorkerJob            │──────────▶│ WorkerJobAttempt         │
│ id                   │           │ id                       │
│ jobType              │           │ workerJobId              │
│ status               │           │ attemptNumber            │
└──────────────────────┘           └──────────────────────────┘
          │ 1
          │
          │ 0..1
          ▼
┌──────────────────────┐
│ DeadLetterEntry      │
│ id                   │
│ workerJobId          │
│ status               │
└──────────────────────┘
```

## WorkerJob

Logical table: `atlas.worker_job`

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | String | Yes | Stable job id. |
| `jobType` | Enum | Yes | `BATCH_INGEST`, `CONNECTOR_SYNC`, `ASYNC_PROCESSING`. |
| `subjectType` | String | Yes | Source object type such as `batch`, `connector-sync-run`, or `future-async`. |
| `subjectId` | String | Yes | Source object id. |
| `status` | Enum | Yes | Worker job status from the spec. |
| `attemptCount` | Integer | Yes | Number of recorded attempts. |
| `maxAttempts` | Integer | Yes | Policy max attempts, default 3. |
| `retryDelaySeconds` | Integer | No | Last scheduled retry delay. |
| `nextRetryAt` | Timestamp | No | Next local retry eligibility time. |
| `sourceTrace` | Text/JSON | No | Relative/mock-safe source trace snapshot. |
| `reviewEligible` | Boolean | Yes | Whether downstream review can inspect the output once recovered. |
| `safeErrorCode` | String | No | Latest safe error code. |
| `safeErrorCategory` | Enum | No | Latest safe error category. |
| `safeErrorMessage` | String | No | Sanitized operator-facing message. |
| `createdAt` | Timestamp | Yes | Creation time. |
| `updatedAt` | Timestamp | Yes | Last transition time. |

## WorkerJobAttempt

Logical table: `atlas.worker_job_attempt`

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | String | Yes | Stable attempt id. |
| `workerJobId` | String | Yes | Owning worker job. |
| `attemptNumber` | Integer | Yes | 1-based attempt number. |
| `status` | Enum | Yes | Attempt status. |
| `retryable` | Boolean | Yes | Whether failure may retry. |
| `safeErrorCode` | String | No | Sanitized error code. |
| `safeErrorCategory` | Enum | No | Sanitized category. |
| `safeErrorMessage` | String | No | Sanitized message. |
| `sourceTrace` | Text/JSON | No | Attempt-level trace snapshot. |
| `startedAt` | Timestamp | Yes | Attempt start. |
| `completedAt` | Timestamp | No | Attempt end. |

## DeadLetterEntry

Logical table: `atlas.dead_letter_entry`

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | String | Yes | Stable entry id. |
| `workerJobId` | String | Yes | Terminal worker job. |
| `status` | Enum | Yes | `OPEN`, `RETRIED`, `ACKNOWLEDGED`. |
| `jobType` | Enum | Yes | Copied from job for listing. |
| `subjectType` | String | Yes | Copied source object type. |
| `subjectId` | String | Yes | Copied source object id. |
| `attemptSummary` | String/Text | Yes | Safe summary such as `3/3 attempts failed`. |
| `safeErrorCode` | String | Yes | Final safe error code. |
| `safeErrorCategory` | Enum | Yes | Final safe category. |
| `safeErrorMessage` | String | Yes | Sanitized message. |
| `sourceTrace` | Text/JSON | No | Copied relative/mock-safe source trace. |
| `reviewEligible` | Boolean | Yes | Whether recovered output may enter review. |
| `operatorActionBy` | String | No | Local operator id/name. |
| `operatorActionAt` | Timestamp | No | Action time. |
| `createdAt` | Timestamp | Yes | Dead-letter creation time. |

## State Transitions

```text
QUEUED -> RUNNING -> SUCCEEDED
                 └-> WAITING_RETRY -> RUNNING
                 └-> DEAD_LETTERED -> ACKNOWLEDGED

DeadLetterEntry:
OPEN -> RETRIED
OPEN -> ACKNOWLEDGED
ACKNOWLEDGED -> ACKNOWLEDGED
```

## Indexes

- `worker_job(status, job_type)`
- `worker_job(subject_type, subject_id)`
- `worker_job_attempt(worker_job_id, attempt_number)`
- `dead_letter_entry(status, created_at)`

## Data Safety

All source trace and error fields are sanitized, bounded, and mock/sample-safe. No raw payload, raw source document content, private absolute path, raw exception, stack trace, token, cookie, API key, or internal endpoint is stored.
