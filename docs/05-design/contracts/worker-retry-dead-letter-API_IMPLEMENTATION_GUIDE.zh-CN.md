# worker-retry-dead-letter API 实现指南

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07
Base path: `/api`
Backend stack: Spring Boot
Auth model：现有 mock-safe auth/RBAC boundary；本 slice 不改变 production auth。

## Endpoint Summary

| Operation | Method | Path | Response |
|---|---|---|---|
| List failed jobs | GET | `/api/worker-jobs/failed` | `ApiEnvelope<List<WorkerJobResponse>>` |
| Get job detail | GET | `/api/worker-jobs/{jobId}` | `ApiEnvelope<WorkerJobResponse>` |
| List dead-letter entries | GET | `/api/dead-letter-entries` | `ApiEnvelope<List<DeadLetterEntryResponse>>` |
| Get dead-letter detail | GET | `/api/dead-letter-entries/{entryId}` | `ApiEnvelope<DeadLetterEntryResponse>` |
| Retry dead-letter entry | POST | `/api/dead-letter-entries/{entryId}/retry` | `ApiEnvelope<DeadLetterEntryResponse>` |
| Acknowledge dead-letter entry | POST | `/api/dead-letter-entries/{entryId}/acknowledge` | `ApiEnvelope<DeadLetterEntryResponse>` |

## Worker Job Response

```json
{
  "id": "worker-job-connector-sync-001",
  "jobType": "CONNECTOR_SYNC",
  "subjectType": "connector-sync-run",
  "subjectId": "connector-run-20260707-0001",
  "status": "DEAD_LETTERED",
  "attemptCount": 3,
  "maxAttempts": 3,
  "retryDelaySeconds": null,
  "nextRetryAt": null,
  "sourceTrace": {
    "sourceName": "Mock Local Fixture",
    "sourceLocator": "fixture-modernization-overview",
    "section": "overview",
    "connectorKey": "mock-local-fixture"
  },
  "reviewEligible": false,
  "safeErrorCode": "SAFE_SYSTEM_ERROR",
  "safeErrorCategory": "SOURCE_UNREADABLE",
  "safeErrorMessage": "The source could not be processed safely.",
  "attempts": []
}
```

## Attempt Response

```json
{
  "id": "worker-attempt-001",
  "workerJobId": "worker-job-connector-sync-001",
  "attemptNumber": 1,
  "status": "FAILED_RETRYABLE",
  "retryable": true,
  "safeErrorCode": "SAFE_SYSTEM_ERROR",
  "safeErrorCategory": "SOURCE_UNREADABLE",
  "safeErrorMessage": "The source could not be processed safely.",
  "startedAt": "2026-07-07T00:00:00Z",
  "completedAt": "2026-07-07T00:00:01Z"
}
```

## Dead Letter Entry Response

```json
{
  "id": "dead-letter-connector-sync-001",
  "workerJobId": "worker-job-connector-sync-001",
  "status": "OPEN",
  "jobType": "CONNECTOR_SYNC",
  "subjectType": "connector-sync-run",
  "subjectId": "connector-run-20260707-0001",
  "attemptSummary": "3/3 attempts failed",
  "safeErrorCode": "SAFE_SYSTEM_ERROR",
  "safeErrorCategory": "SOURCE_UNREADABLE",
  "safeErrorMessage": "The source could not be processed safely.",
  "sourceTrace": {
    "sourceName": "Mock Local Fixture",
    "sourceLocator": "fixture-modernization-overview",
    "section": "overview",
    "connectorKey": "mock-local-fixture"
  },
  "reviewEligible": false,
  "operatorActionBy": null,
  "operatorActionAt": null,
  "createdAt": "2026-07-07T00:00:02Z",
  "job": {},
  "attempts": []
}
```

## Action Request

Manual action request 可包含可选 safe operator field：

```json
{
  "operator": "local-operator"
}
```

Validation:

- `operator` 可选。
- 如提供，`operator` 必须脱敏并限制长度。
- Retry 只允许 `OPEN`。
- Acknowledge 允许 `OPEN`，且对 `ACKNOWLEDGED` 幂等。

## Error Rules

- 所有 errors 使用现有 `ApiEnvelope` safe error body。
- 缺失 job/entry 返回 `404 NOT_FOUND`。
- 无效 action state 返回 `409 CONFLICT`。
- 无效 request body 返回 `400 VALIDATION_FAILED`。
- Unexpected failures 返回 `500 SAFE_SYSTEM_ERROR`，包含 correlation id。
- response 不得暴露 raw exception class、stack trace、secret、token、cookie、API key、private path、internal endpoint 或 real source content。

## Verification

- Backend integration tests 必须覆盖 list/detail/action endpoints。
- Service tests 必须覆盖 retryable transitions、terminal failure、dead-letter idempotency、redaction、source trace preservation、retry action 和 acknowledge action。
- Frontend typecheck/test/build 必须覆盖 mapping 和 safe display。
