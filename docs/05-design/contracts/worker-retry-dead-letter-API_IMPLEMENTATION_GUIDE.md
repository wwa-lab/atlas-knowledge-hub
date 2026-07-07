# worker-retry-dead-letter API Implementation Guide

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Base path: `/api`
Backend stack: Spring Boot
Auth model: Existing mock-safe auth/RBAC boundary; this slice does not change production auth.

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

Manual action requests have an optional safe operator field:

```json
{
  "operator": "local-operator"
}
```

Validation:

- `operator` is optional.
- If present, `operator` must be sanitized and bounded.
- Retry is allowed only for `OPEN`.
- Acknowledge is allowed for `OPEN` and is idempotent for `ACKNOWLEDGED`.

## Error Rules

- All errors use existing `ApiEnvelope` safe error body.
- Missing job/entry returns `404 NOT_FOUND`.
- Invalid action state returns `409 CONFLICT`.
- Invalid request body returns `400 VALIDATION_FAILED`.
- Unexpected failures return `500 SAFE_SYSTEM_ERROR` with correlation id.
- No response may expose raw exception class, stack trace, secret, token, cookie, API key, private path, internal endpoint, or real source content.

## Verification

- Backend integration tests must cover list/detail/action endpoints.
- Service tests must cover retryable transitions, terminal failure, dead-letter idempotency, redaction, source trace preservation, retry action, and acknowledge action.
- Frontend typecheck/test/build must cover mapping and safe display.
