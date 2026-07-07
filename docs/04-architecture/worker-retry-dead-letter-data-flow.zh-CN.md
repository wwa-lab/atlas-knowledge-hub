# 数据流：worker-retry-dead-letter

状态：已由 autonomous single-slice prompt 预授权接受
最后更新：2026-07-07

## Flow 1: Retryable Failure

```text
Job RUNNING
  │
  ▼
Record attempt failure (retryable=true)
  │
  ├─ attempt number < maxAttempts
  │     ▼
  │  sanitize error snapshot
  │     ▼
  │  persist attempt FAILED_RETRYABLE
  │     ▼
  │  update job WAITING_RETRY
  │     ▼
  │  set retryDelaySeconds + nextRetryAt
  │
  └─ attempt number >= maxAttempts
        ▼
     terminal dead-letter flow
```

## Flow 2: Terminal Failure

```text
Record terminal failure
  │
  ▼
sanitize safe error snapshot
  │
  ▼
persist attempt FAILED_TERMINAL
  │
  ▼
update job DEAD_LETTERED
  │
  ▼
create or return one dead-letter entry
  │
  ▼
expose via ApiEnvelope + frontend Processing Center
```

## Flow 3: Manual Retry

```text
Operator clicks Retry
  │
  ▼
POST /api/dead-letter-entries/{entryId}/retry
  │
  ├─ entry OPEN
  │     ▼
  │  mark entry RETRIED
  │     ▼
  │  set job WAITING_RETRY with deterministic metadata
  │     ▼
  │  return dead-letter detail
  │
  └─ entry ACKNOWLEDGED
        ▼
     safe CONFLICT
```

## Flow 4: Manual Acknowledge

```text
Operator clicks Acknowledge
  │
  ▼
POST /api/dead-letter-entries/{entryId}/acknowledge
  │
  ▼
mark entry ACKNOWLEDGED (idempotent)
  │
  ▼
mark job ACKNOWLEDGED
  │
  ▼
return dead-letter detail
```

## Field Preservation

| Input | Job | Attempt | Dead letter | UI |
|---|---|---|---|---|
| `jobType` | stored | referenced | exposed | badge |
| `sourceTrace` | stored | copied | copied | source panel |
| `safeErrorCode` | latest | stored | stored | safe category |
| `safeErrorMessage` | latest | stored | stored | safe message |
| `attemptNumber` | derived | stored | summarized | timeline |
| `reviewEligible` | stored | copied | copied | review state |

## Safety Rules

- Unsafe error input 在 persistence 前脱敏。
- Source trace 只使用 relative/mock-safe identifiers。
- API responses 复用 `ApiEnvelope`。
- 这些流程不会 dispatch production worker，也不会触发 external network action。
