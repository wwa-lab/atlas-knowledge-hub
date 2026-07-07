# 数据模型：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07

## 概览

数据模型增加三个本地可靠性实体：worker job、worker job attempt 和 dead-letter entry。所有 schema changes 都是 additive，并且只使用 mock/sample-safe metadata。

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
| `id` | String | Yes | Stable job id。 |
| `jobType` | Enum | Yes | `BATCH_INGEST`, `CONNECTOR_SYNC`, `ASYNC_PROCESSING`。 |
| `subjectType` | String | Yes | 来源对象类型，如 `batch`、`connector-sync-run` 或 `future-async`。 |
| `subjectId` | String | Yes | 来源对象 id。 |
| `status` | Enum | Yes | Spec 中的 worker job status。 |
| `attemptCount` | Integer | Yes | 已记录 attempt 数。 |
| `maxAttempts` | Integer | Yes | Policy max attempts，默认 3。 |
| `retryDelaySeconds` | Integer | No | 最近一次 scheduled retry delay。 |
| `nextRetryAt` | Timestamp | No | 下一次本地 retry eligibility time。 |
| `sourceTrace` | Text/JSON | No | Relative/mock-safe source trace snapshot。 |
| `reviewEligible` | Boolean | Yes | 恢复后 downstream review 是否可检查输出。 |
| `safeErrorCode` | String | No | 最新 safe error code。 |
| `safeErrorCategory` | Enum | No | 最新 safe error category。 |
| `safeErrorMessage` | String | No | 已脱敏 operator-facing message。 |
| `createdAt` | Timestamp | Yes | 创建时间。 |
| `updatedAt` | Timestamp | Yes | 最近 transition 时间。 |

## WorkerJobAttempt

Logical table: `atlas.worker_job_attempt`

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | String | Yes | Stable attempt id。 |
| `workerJobId` | String | Yes | 所属 worker job。 |
| `attemptNumber` | Integer | Yes | 1-based attempt number。 |
| `status` | Enum | Yes | Attempt status。 |
| `retryable` | Boolean | Yes | Failure 是否可 retry。 |
| `safeErrorCode` | String | No | 已脱敏 error code。 |
| `safeErrorCategory` | Enum | No | 已脱敏 category。 |
| `safeErrorMessage` | String | No | 已脱敏 message。 |
| `sourceTrace` | Text/JSON | No | Attempt-level trace snapshot。 |
| `startedAt` | Timestamp | Yes | Attempt start。 |
| `completedAt` | Timestamp | No | Attempt end。 |

## DeadLetterEntry

Logical table: `atlas.dead_letter_entry`

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | String | Yes | Stable entry id。 |
| `workerJobId` | String | Yes | Terminal worker job。 |
| `status` | Enum | Yes | `OPEN`, `RETRIED`, `ACKNOWLEDGED`。 |
| `jobType` | Enum | Yes | 从 job 复制，便于 list。 |
| `subjectType` | String | Yes | 复制来源对象类型。 |
| `subjectId` | String | Yes | 复制来源对象 id。 |
| `attemptSummary` | String/Text | Yes | 安全摘要，如 `3/3 attempts failed`。 |
| `safeErrorCode` | String | Yes | 最终 safe error code。 |
| `safeErrorCategory` | Enum | Yes | 最终 safe category。 |
| `safeErrorMessage` | String | Yes | 已脱敏 message。 |
| `sourceTrace` | Text/JSON | No | 复制的 relative/mock-safe source trace。 |
| `reviewEligible` | Boolean | Yes | 恢复输出是否可进入 review。 |
| `operatorActionBy` | String | No | 本地 operator id/name。 |
| `operatorActionAt` | Timestamp | No | 操作时间。 |
| `createdAt` | Timestamp | Yes | Dead-letter 创建时间。 |

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

所有 source trace 和 error fields 都必须脱敏、限定长度，并保持 mock/sample-safe。不存储 raw payload、raw source document content、private absolute path、raw exception、stack trace、token、cookie、API key 或 internal endpoint。
