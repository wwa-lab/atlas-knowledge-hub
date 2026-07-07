# Requirements: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07
Workflow tier: Tier 3 / High-Risk Governance

## Goal

Add a mock-safe foundation for deterministic local rate limiting and stable safe API errors across Atlas core APIs. The slice must make authentication, authorization, validation, not found, rate limited, and unexpected failures distinguishable without exposing stack traces, raw exceptions, secrets, credentials, private paths, internal endpoints, or raw source content.

## Scope

- Backend API error responses use one stable safe envelope.
- Backend exception handling maps validation, auth/RBAC, not found, conflict, rate limit, and unexpected errors to safe codes and HTTP statuses.
- Deterministic local rate limiting protects core `/api/**` endpoints without Redis, gateway, service mesh, or external providers.
- Server logs retain correlation and safe troubleshooting context.
- Frontend API and product shell expose user-safe states for permission denied, validation failed, rate limited, not found, and safe system errors.
- Tests prove redaction, status mapping, rate limit behavior, and representative frontend states.

## Exclusions

- No external rate-limit service, Redis quota store, gateway, service mesh, production SSO/OIDC, real secret manager, external cloud/provider calls, production observability, SIEM export, alerting, or SLO dashboard.
- No change to accepted auth-space-rbac permission semantics.
- No change to accepted audit-log-foundation audit semantics.
- No real company data, raw secret, token, password, private endpoint, private absolute path, raw source content, or credential fixture.

## Requirements

| ID | Priority | Requirement |
|---|---|---|
| REQ-RATE-LIMIT-SAFE-ERRORS-001 | Must | API failures must use `success=false` with a safe `error` object that contains a stable code, safe message, timestamp, path, and non-sensitive metadata. |
| REQ-RATE-LIMIT-SAFE-ERRORS-002 | Must | Validation failures must return `400` with `VALIDATION_FAILED` and field messages that are safe, bounded, and free of raw request payloads. |
| REQ-RATE-LIMIT-SAFE-ERRORS-003 | Must | Authentication and authorization failures must return `401` or `403` with `AUTHENTICATION_REQUIRED` or `PERMISSION_DENIED` without exposing protected resource metadata. |
| REQ-RATE-LIMIT-SAFE-ERRORS-004 | Must | Not found failures must return `404` with `NOT_FOUND` and a generic safe message. |
| REQ-RATE-LIMIT-SAFE-ERRORS-005 | Must | Rate limited requests must return `429` with `RATE_LIMITED`, safe retry metadata, and deterministic local behavior for tests. |
| REQ-RATE-LIMIT-SAFE-ERRORS-006 | Must | Unexpected exceptions must return `500` with `SAFE_SYSTEM_ERROR` and a correlation id while logging server-side context safely. |
| REQ-RATE-LIMIT-SAFE-ERRORS-007 | Must | Safe error generation must redact secrets, credentials, private paths, internal endpoints, raw stack traces, raw exception class names, and raw source content from API responses. |
| REQ-RATE-LIMIT-SAFE-ERRORS-008 | Must | Frontend API errors must expose typed safe status categories for permission denied, validation failed, rate limited, not found, and safe system errors. |
| REQ-RATE-LIMIT-SAFE-ERRORS-009 | Must | The product shell must render representative user-safe states without leaking internal implementation details. |
| REQ-RATE-LIMIT-SAFE-ERRORS-010 | Must | Tests must cover backend redaction, status mapping, deterministic rate limit behavior, API contract responses, and frontend state rendering. |
| REQ-RATE-LIMIT-SAFE-ERRORS-011 | Must | Traceability, slice roadmap, and repo status docs must record implementation status, verification evidence, and residual risks. |
| REQ-RATE-LIMIT-SAFE-ERRORS-012 | Must | The implementation must preserve auth-space-rbac, audit-log-foundation, and secret-manager-integration boundaries. |

## Acceptance Criteria

| ID | Criteria |
|---|---|
| AC-RATE-LIMIT-SAFE-ERRORS-001 | Core API errors use the stable safe envelope. |
| AC-RATE-LIMIT-SAFE-ERRORS-002 | Responses never expose raw stack traces, raw exceptions, secrets, private paths, internal endpoints, raw credentials, or raw source content. |
| AC-RATE-LIMIT-SAFE-ERRORS-003 | Permission denied, validation failed, rate limited, not found, and unexpected errors are distinguishable by safe code/status. |
| AC-RATE-LIMIT-SAFE-ERRORS-004 | Local rate limiting is deterministic and testable. |
| AC-RATE-LIMIT-SAFE-ERRORS-005 | Frontend displays user-safe error states. |
| AC-RATE-LIMIT-SAFE-ERRORS-006 | Server logs/audit hooks retain safe troubleshooting context. |
| AC-RATE-LIMIT-SAFE-ERRORS-007 | Verification commands and scans pass or are explicitly reported. |

## Assumptions

- Existing `ApiEnvelope`, `ErrorBody`, `GlobalExceptionHandler`, `AtlasAuthInterceptor`, and frontend `ApiError` are extension points.
- Existing audit behavior is not redefined; this slice only emits safe denial/error context where already owned by auth/error boundaries.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
