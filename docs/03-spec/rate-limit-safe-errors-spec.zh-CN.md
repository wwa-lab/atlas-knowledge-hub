# 功能规格：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07
Source stories: US-RATE-LIMIT-SAFE-ERRORS-001 through US-RATE-LIMIT-SAFE-ERRORS-005

## Overview

Atlas 必须为 core APIs 提供 stable safe API failures 与 deterministic local throttling。本切片加强现有 `ApiEnvelope` 与 `GlobalExceptionHandler` 路径，不创建竞争性的 response format。

## Actors

- Knowledge user：看到 safe UI states。
- Platform administrator：需要可区分的 governance failures。
- Operator：需要 deterministic local throttling evidence。
- Developer：需要 testable contracts 与 server-side correlation。

## Functional Requirements

| ID | Spec Requirement |
|---|---|
| FR-RATE-LIMIT-SAFE-ERRORS-001 | REQ-RATE-LIMIT-SAFE-ERRORS-001: All handled API failures return `success=false`, `data=null`, and a safe `error`. |
| FR-RATE-LIMIT-SAFE-ERRORS-002 | REQ-RATE-LIMIT-SAFE-ERRORS-002: Validation failures return `400 VALIDATION_FAILED`. |
| FR-RATE-LIMIT-SAFE-ERRORS-003 | REQ-RATE-LIMIT-SAFE-ERRORS-003: Unauthenticated calls return `401 AUTHENTICATION_REQUIRED`; unauthorized calls return `403 PERMISSION_DENIED`. |
| FR-RATE-LIMIT-SAFE-ERRORS-004 | REQ-RATE-LIMIT-SAFE-ERRORS-004: Unknown resources return `404 NOT_FOUND` with a generic safe message. |
| FR-RATE-LIMIT-SAFE-ERRORS-005 | REQ-RATE-LIMIT-SAFE-ERRORS-005: Local rate limit denials return `429 RATE_LIMITED` with safe retry metadata. |
| FR-RATE-LIMIT-SAFE-ERRORS-006 | REQ-RATE-LIMIT-SAFE-ERRORS-006: Unexpected faults return `500 SAFE_SYSTEM_ERROR` and a correlation id. |
| FR-RATE-LIMIT-SAFE-ERRORS-007 | REQ-RATE-LIMIT-SAFE-ERRORS-007: API responses redact forbidden material before serialization. |
| FR-RATE-LIMIT-SAFE-ERRORS-008 | REQ-RATE-LIMIT-SAFE-ERRORS-008: Frontend classifies safe error codes into stable UI categories. |
| FR-RATE-LIMIT-SAFE-ERRORS-009 | REQ-RATE-LIMIT-SAFE-ERRORS-009: Product shell renders representative safe error states. |
| FR-RATE-LIMIT-SAFE-ERRORS-010 | REQ-RATE-LIMIT-SAFE-ERRORS-010: Tests cover redaction, status mapping, rate limit behavior, contract responses, and UI states. |
| FR-RATE-LIMIT-SAFE-ERRORS-011 | REQ-RATE-LIMIT-SAFE-ERRORS-011: Traceability and roadmaps record evidence and risks. |
| FR-RATE-LIMIT-SAFE-ERRORS-012 | REQ-RATE-LIMIT-SAFE-ERRORS-012: Existing auth, audit, and secret boundaries are preserved. |

## Safe Error Codes

| Code | HTTP | User-safe meaning |
|---|---:|---|
| AUTHENTICATION_REQUIRED | 401 | 缺少 sign-in 或 current-user context。 |
| PERMISSION_DENIED | 403 | Current user 缺少 required capability。 |
| VALIDATION_FAILED | 400 | Request shape 或 values 验证失败。 |
| NOT_FOUND | 404 | Requested resource unavailable 或 not visible。 |
| CONFLICT | 409 | Request conflicts with current state。 |
| RATE_LIMITED | 429 | Local request budget exceeded。 |
| SAFE_SYSTEM_ERROR | 500 | Unexpected server failure with correlation id。 |

## Response Rules

- AC-RATE-LIMIT-SAFE-ERRORS-001: Error responses 必须保持 existing envelope shape。
- AC-RATE-LIMIT-SAFE-ERRORS-002: Response message、fields、details、path 与 metadata 必须 sanitized and bounded。
- AC-RATE-LIMIT-SAFE-ERRORS-003: Codes distinguish permission denied、validation failed、rate limited、not found 与 unexpected errors。
- AC-RATE-LIMIT-SAFE-ERRORS-004: Rate limiting 在 local tests 中 deterministic。
- AC-RATE-LIMIT-SAFE-ERRORS-005: Frontend displays user-safe states。
- AC-RATE-LIMIT-SAFE-ERRORS-006: Server logs include safe troubleshooting context。
- AC-RATE-LIMIT-SAFE-ERRORS-007: Verification commands and scans are recorded。

## Workflows

1. Request enters `/api/**`。
2. Local rate limiter evaluates non-`OPTIONS` requests。
3. Auth/RBAC evaluates protected routes without changing accepted permission semantics。
4. Controller/service either returns success or raises a known/unknown exception。
5. Safe error mapper produces sanitized error response。
6. Frontend API client classifies safe code and renders mapped state。

## Edge States

- Empty fields map to a generic validation message。
- Unknown paths or hidden resources use generic `NOT_FOUND`。
- Rate-limited callers receive safe retry seconds, not bucket internals。
- Unexpected exceptions never return raw class name、stack trace、private path、endpoint、credential 或 source content。

## Non-Functional Requirements

- Security：responses 中无 secret、credential、private path、internal endpoint、raw stack trace、raw exception 或 raw source content。
- Reliability：rate limiter 使用 deterministic in-memory state，适合 local/mock testing。
- Auditability：server logs include correlation id and safe request context。
- Environment：无 external network dependency 或 production quota store。

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-001 through T-RATE-LIMIT-SAFE-ERRORS-008 implement FR-RATE-LIMIT-SAFE-ERRORS-001 through FR-RATE-LIMIT-SAFE-ERRORS-012.

## Open Questions

在 attached goal 的预授权边界内没有 blocking open question。
