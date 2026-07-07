# Data Flow: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Request Flow

1. Client sends a request to `/api/**`.
2. Local rate limiter identifies a caller key from safe request metadata and updates an in-memory bucket.
3. If the bucket is exhausted, the limiter returns AC-RATE-LIMIT-SAFE-ERRORS-004 with `429 RATE_LIMITED`.
4. If allowed, auth/RBAC evaluates the request.
5. If auth fails, the auth boundary returns AC-RATE-LIMIT-SAFE-ERRORS-003 with `AUTHENTICATION_REQUIRED` or `PERMISSION_DENIED`.
6. Controller/service validation or domain errors are handled by the global exception boundary.
7. Unexpected errors are logged with correlation id and return `SAFE_SYSTEM_ERROR`.
8. Frontend API client classifies the safe code and the product shell renders AC-RATE-LIMIT-SAFE-ERRORS-005.

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

Raw exception or validation input never flows directly to the API response. Every error response goes through:

1. Safe code selection.
2. Safe message selection.
3. Field/detail sanitization.
4. Path sanitization.
5. Optional correlation/retry metadata attachment.
6. `ApiEnvelope.fail(...)` serialization.

## Edge Cases

- REQ-RATE-LIMIT-SAFE-ERRORS-002: malformed JSON uses a generic body validation field.
- REQ-RATE-LIMIT-SAFE-ERRORS-005: `OPTIONS` requests are not throttled.
- REQ-RATE-LIMIT-SAFE-ERRORS-006: runtime faults include correlation id but no raw exception text.
- REQ-RATE-LIMIT-SAFE-ERRORS-007: redaction covers path-like, credential-like, endpoint-like, stack-trace-like, and source-content-like strings.
- REQ-RATE-LIMIT-SAFE-ERRORS-012: audit and auth semantics remain owned by their current boundaries.

## Verification Mapping

- AC-RATE-LIMIT-SAFE-ERRORS-001: API contract tests.
- AC-RATE-LIMIT-SAFE-ERRORS-002: redaction unit tests and scans.
- AC-RATE-LIMIT-SAFE-ERRORS-003: auth and exception contract tests.
- AC-RATE-LIMIT-SAFE-ERRORS-004: deterministic rate limiter tests.
- AC-RATE-LIMIT-SAFE-ERRORS-005: frontend component tests.
- AC-RATE-LIMIT-SAFE-ERRORS-006: safe logging/correlation tests.
- AC-RATE-LIMIT-SAFE-ERRORS-007: closeout evidence.

Task IDs: T-RATE-LIMIT-SAFE-ERRORS-001, T-RATE-LIMIT-SAFE-ERRORS-002, T-RATE-LIMIT-SAFE-ERRORS-003, T-RATE-LIMIT-SAFE-ERRORS-004, T-RATE-LIMIT-SAFE-ERRORS-005, T-RATE-LIMIT-SAFE-ERRORS-006, T-RATE-LIMIT-SAFE-ERRORS-007, T-RATE-LIMIT-SAFE-ERRORS-008.
