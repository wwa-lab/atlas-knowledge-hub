# rate-limit-safe-errors API Implementation Guide

日期：2026-07-07
Base path: `/api`
Backend stack: Spring Boot
Auth model：现有 mock-safe current-user 与 RBAC boundary

## Overview

本 guide 定义 REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 的 safe error contract。它扩展现有 Atlas API envelopes，不引入 production auth、Redis、gateway throttling 或 external providers。

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

- AC-RATE-LIMIT-SAFE-ERRORS-001: All error responses use the same envelope shape。
- AC-RATE-LIMIT-SAFE-ERRORS-002: No field may expose raw stack traces、raw exceptions、secrets、private paths、internal endpoints、raw credentials 或 raw source content。
- AC-RATE-LIMIT-SAFE-ERRORS-003: HTTP status and code must distinguish user-relevant failure categories。
- AC-RATE-LIMIT-SAFE-ERRORS-004: Local limiter decisions must be deterministic under test reset。
- AC-RATE-LIMIT-SAFE-ERRORS-006: `SAFE_SYSTEM_ERROR` includes a correlation id and logs server-side safe context。

## Rate Limit Behavior

- Applies to `/api/**` except `OPTIONS`。
- Caller key comes from existing mock user header when available, then remote address fallback。
- Default local limit intentionally large enough not to disrupt normal local UI flows and configurable enough for tests。
- Test headers may override limit/window in integration tests only when implementation keeps them safe and local。
- 不使用 Redis、gateway、service mesh、external service 或 production quota store。

## Frontend Contract

Frontend `ApiError` 必须暴露：

- `status`
- `code`
- `safeCategory`
- `retryAfterSeconds`
- `correlationId`

Product shell 必须渲染 AC-RATE-LIMIT-SAFE-ERRORS-005 states，且不显示 raw server internals。

## Test Contract

- T-RATE-LIMIT-SAFE-ERRORS-006: backend unit and integration tests。
- T-RATE-LIMIT-SAFE-ERRORS-007: frontend tests。
- T-RATE-LIMIT-SAFE-ERRORS-008: closeout scans and docs。

## Security Notes

本 guide 明确排除 production SSO/OIDC、production rate-limit infrastructure、production observability、real secret manager、external cloud calls、real company data、raw credentials 与 private absolute paths。
