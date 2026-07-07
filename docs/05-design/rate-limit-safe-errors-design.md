# Design: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Design Scope

Implement REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 using current backend and frontend extension points.

## Backend Design

- Safe error factory creates every API error body.
- Sanitizer bounds and redacts all fields before serialization.
- Global exception handler maps validation, not found, conflict, and unexpected errors.
- Auth interceptor writes safe auth errors using the same factory.
- Local rate-limit interceptor uses an in-memory per-caller fixed window. Default values remain local/mock-safe and can be overridden in tests through headers or configuration.
- Test support can reset limiter state without adding a production endpoint.

## Frontend Design

- `ApiError` includes safe code, status, safe category, optional retry metadata, and correlation id.
- API client uses the backend safe error object and never displays raw server text for unsafe categories.
- Product shell renders representative safe error previews in the administration/API area so permission denied, validation, rate-limited, not found, and system error states are covered.

## Error Copy

| Code | Frontend state |
|---|---|
| AUTHENTICATION_REQUIRED | Sign in or choose an approved mock user. |
| PERMISSION_DENIED | Current role cannot perform this action. |
| VALIDATION_FAILED | Check the highlighted request fields. |
| NOT_FOUND | The requested Atlas item is unavailable. |
| RATE_LIMITED | Too many requests; try again after the safe retry hint. |
| SAFE_SYSTEM_ERROR | Atlas hit a safe system error; provide correlation id to support. |

## Testing Design

- Backend unit tests cover sanitizer and deterministic limiter.
- Backend integration tests cover representative validation, permission, not found, rate limit, and unexpected errors.
- Frontend tests cover `ApiError` classification and visible safe states.
- Scans cover forbidden secret/private-path/real-data patterns.

## Traceability

T-RATE-LIMIT-SAFE-ERRORS-001, T-RATE-LIMIT-SAFE-ERRORS-002, T-RATE-LIMIT-SAFE-ERRORS-003, T-RATE-LIMIT-SAFE-ERRORS-004, T-RATE-LIMIT-SAFE-ERRORS-005, T-RATE-LIMIT-SAFE-ERRORS-006, T-RATE-LIMIT-SAFE-ERRORS-007, and T-RATE-LIMIT-SAFE-ERRORS-008 implement this design and verify AC-RATE-LIMIT-SAFE-ERRORS-001 through AC-RATE-LIMIT-SAFE-ERRORS-007.
