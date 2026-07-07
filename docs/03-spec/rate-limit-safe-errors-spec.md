# Feature Specification: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07
Source stories: US-RATE-LIMIT-SAFE-ERRORS-001 through US-RATE-LIMIT-SAFE-ERRORS-005

## Overview

Atlas must expose stable, safe API failures and deterministic local throttling for core APIs. This slice strengthens the existing `ApiEnvelope` and `GlobalExceptionHandler` path rather than creating a competing response format.

## Actors

- Knowledge user: sees safe UI states.
- Platform administrator: needs distinguishable governance failures.
- Operator: needs deterministic local throttling evidence.
- Developer: needs testable contracts and server-side correlation.

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
| AUTHENTICATION_REQUIRED | 401 | Sign-in or current-user context is missing. |
| PERMISSION_DENIED | 403 | Current user lacks the required capability. |
| VALIDATION_FAILED | 400 | Request shape or values failed validation. |
| NOT_FOUND | 404 | Requested resource is unavailable or not visible. |
| CONFLICT | 409 | Request conflicts with current state. |
| RATE_LIMITED | 429 | Local request budget was exceeded. |
| SAFE_SYSTEM_ERROR | 500 | Unexpected server failure with correlation id. |

## Response Rules

- AC-RATE-LIMIT-SAFE-ERRORS-001: Error responses must keep the existing envelope shape.
- AC-RATE-LIMIT-SAFE-ERRORS-002: Response message, fields, details, path, and metadata must be sanitized and bounded.
- AC-RATE-LIMIT-SAFE-ERRORS-003: Codes distinguish permission denied, validation failed, rate limited, not found, and unexpected errors.
- AC-RATE-LIMIT-SAFE-ERRORS-004: Rate limiting is deterministic in local tests.
- AC-RATE-LIMIT-SAFE-ERRORS-005: Frontend displays user-safe states.
- AC-RATE-LIMIT-SAFE-ERRORS-006: Server logs include safe troubleshooting context.
- AC-RATE-LIMIT-SAFE-ERRORS-007: Verification commands and scans are recorded.

## Workflows

1. Request enters `/api/**`.
2. Local rate limiter evaluates non-`OPTIONS` requests.
3. Auth/RBAC evaluates protected routes without changing accepted permission semantics.
4. Controller/service either returns success or raises a known/unknown exception.
5. The safe error mapper produces a sanitized error response.
6. Frontend API client classifies the safe code and renders the mapped state.

## Edge States

- Empty fields map to a generic validation message.
- Unknown paths or hidden resources use generic `NOT_FOUND`.
- Rate-limited callers receive safe retry seconds, not bucket internals.
- Unexpected exceptions never return raw class name, stack trace, private path, endpoint, credential, or source content.

## Non-Functional Requirements

- Security: no secret, credential, private path, internal endpoint, raw stack trace, raw exception, or raw source content in responses.
- Reliability: rate limiter uses deterministic in-memory state suitable for local/mock testing.
- Auditability: server logs include correlation id and safe request context.
- Environment: no external network dependency or production quota store.

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-001 through T-RATE-LIMIT-SAFE-ERRORS-008 implement FR-RATE-LIMIT-SAFE-ERRORS-001 through FR-RATE-LIMIT-SAFE-ERRORS-012.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
