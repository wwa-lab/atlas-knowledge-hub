# 数据流：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07

## Request Flow

1. Client sends a request to `/api/**`。
2. Local rate limiter 从 safe request metadata 识别 caller key，并更新 in-memory bucket。
3. 如果 bucket exhausted，limiter 返回 AC-RATE-LIMIT-SAFE-ERRORS-004 与 `429 RATE_LIMITED`。
4. 如果 allowed，auth/RBAC evaluates the request。
5. 如果 auth fails，auth boundary 返回 AC-RATE-LIMIT-SAFE-ERRORS-003 与 `AUTHENTICATION_REQUIRED` 或 `PERMISSION_DENIED`。
6. Controller/service validation or domain errors 由 global exception boundary 处理。
7. Unexpected errors 使用 correlation id 记录日志，并返回 `SAFE_SYSTEM_ERROR`。
8. Frontend API client classifies safe code，product shell renders AC-RATE-LIMIT-SAFE-ERRORS-005。

## Safe Error State Machine

```
REQUEST_RECEIVED
  -> RATE_LIMITED
  -> AUTHENTICATION_REQUIRED
  -> PERMISSION_DENIED
  -> VALIDATION_FAILED
  -> NOT_FOUND
  -> CONFLICT
  -> SAFE_SYSTEM_ERROR
  -> SUCCESS
```

## Redaction Flow

Raw exception 或 validation input 不直接进入 API response。每个 error response 都通过：

1. Safe code selection。
2. Safe message selection。
3. Field/detail sanitization。
4. Path sanitization。
5. Optional correlation/retry metadata attachment。
6. `ApiEnvelope.fail(...)` serialization。

## Edge Cases

- REQ-RATE-LIMIT-SAFE-ERRORS-002: malformed JSON 使用 generic body validation field。
- REQ-RATE-LIMIT-SAFE-ERRORS-005: `OPTIONS` requests 不被 throttled。
- REQ-RATE-LIMIT-SAFE-ERRORS-006: runtime faults include correlation id but no raw exception text。
- REQ-RATE-LIMIT-SAFE-ERRORS-007: redaction covers path-like、credential-like、endpoint-like、stack-trace-like 与 source-content-like strings。
- REQ-RATE-LIMIT-SAFE-ERRORS-012: audit and auth semantics remain owned by current boundaries。

## Verification Mapping

- AC-RATE-LIMIT-SAFE-ERRORS-001: API contract tests。
- AC-RATE-LIMIT-SAFE-ERRORS-002: redaction unit tests and scans。
- AC-RATE-LIMIT-SAFE-ERRORS-003: auth and exception contract tests。
- AC-RATE-LIMIT-SAFE-ERRORS-004: deterministic rate limiter tests。
- AC-RATE-LIMIT-SAFE-ERRORS-005: frontend component tests。
- AC-RATE-LIMIT-SAFE-ERRORS-006: safe logging/correlation tests。
- AC-RATE-LIMIT-SAFE-ERRORS-007: closeout evidence。

Task IDs: T-RATE-LIMIT-SAFE-ERRORS-001, T-RATE-LIMIT-SAFE-ERRORS-002, T-RATE-LIMIT-SAFE-ERRORS-003, T-RATE-LIMIT-SAFE-ERRORS-004, T-RATE-LIMIT-SAFE-ERRORS-005, T-RATE-LIMIT-SAFE-ERRORS-006, T-RATE-LIMIT-SAFE-ERRORS-007, T-RATE-LIMIT-SAFE-ERRORS-008.
