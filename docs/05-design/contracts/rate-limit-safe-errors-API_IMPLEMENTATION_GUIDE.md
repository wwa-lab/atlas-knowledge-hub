# rate-limit-safe-errors API Implementation Guide

Date: 2026-07-07
Base path: `/api`
Backend stack: Spring Boot
Auth model: Existing mock-safe current-user and RBAC boundary

## Overview

This guide defines the safe error contract for REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012. It extends existing Atlas API envelopes and does not introduce production auth, Redis, gateway throttling, or external providers.

## Error Envelope

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RATE_LIMITED",
    "message": "Too many requests. Please try again later.",
    "fields": null,
    "timestamp": 1783390000000,
    "path": "/api/spaces/ibm-i-modernization/wiki-pages",
    "correlationId": "safe-correlation-id",
    "retryAfterSeconds": 60
  },
  "meta": null
}
```

## Error Code Table

| Code | HTTP | Requirements |
|---|---:|---|
| AUTHENTICATION_REQUIRED | 401 | REQ-RATE-LIMIT-SAFE-ERRORS-003 |
| PERMISSION_DENIED | 403 | REQ-RATE-LIMIT-SAFE-ERRORS-003 |
| VALIDATION_FAILED | 400 | REQ-RATE-LIMIT-SAFE-ERRORS-002 |
| NOT_FOUND | 404 | REQ-RATE-LIMIT-SAFE-ERRORS-004 |
| CONFLICT | 409 | REQ-RATE-LIMIT-SAFE-ERRORS-001 |
| RATE_LIMITED | 429 | REQ-RATE-LIMIT-SAFE-ERRORS-005 |
| SAFE_SYSTEM_ERROR | 500 | REQ-RATE-LIMIT-SAFE-ERRORS-006 |

## Contract Rules

- AC-RATE-LIMIT-SAFE-ERRORS-001: All error responses use the same envelope shape.
- AC-RATE-LIMIT-SAFE-ERRORS-002: No field may expose raw stack traces, raw exceptions, secrets, private paths, internal endpoints, raw credentials, or raw source content.
- AC-RATE-LIMIT-SAFE-ERRORS-003: HTTP status and code must distinguish user-relevant failure categories.
- AC-RATE-LIMIT-SAFE-ERRORS-004: Local limiter decisions must be deterministic under test reset.
- AC-RATE-LIMIT-SAFE-ERRORS-006: `SAFE_SYSTEM_ERROR` includes a correlation id and logs server-side safe context.

## Rate Limit Behavior

- Applies to `/api/**` except `OPTIONS`.
- Caller key comes from existing mock user header when available, then remote address fallback.
- Default local limit is intentionally small enough for tests to configure and large enough not to disrupt normal local UI flows.
- Test headers may override limit/window in integration tests only when the implementation keeps them safe and local.
- No Redis, gateway, service mesh, external service, or production quota store.

## Frontend Contract

Frontend `ApiError` must expose:

- `status`
- `code`
- `safeCategory`
- `retryAfterSeconds`
- `correlationId`

The product shell must render AC-RATE-LIMIT-SAFE-ERRORS-005 states without displaying raw server internals.

## Test Contract

- T-RATE-LIMIT-SAFE-ERRORS-006: backend unit and integration tests.
- T-RATE-LIMIT-SAFE-ERRORS-007: frontend tests.
- T-RATE-LIMIT-SAFE-ERRORS-008: closeout scans and docs.

## Security Notes

This guide explicitly excludes production SSO/OIDC, production rate-limit infrastructure, production observability, real secret manager, external cloud calls, real company data, raw credentials, and private absolute paths.
