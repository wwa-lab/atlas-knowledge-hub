# User Stories: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## User Story US-RATE-LIMIT-SAFE-ERRORS-001

**Title:** Receive safe API errors

**Story:** As a knowledge user, I want API failures to explain what category failed without exposing internals, so that I can recover or report the issue safely.

### Acceptance Criteria

1. **Given** a validation error **When** the API responds **Then** the response contains AC-RATE-LIMIT-SAFE-ERRORS-001, AC-RATE-LIMIT-SAFE-ERRORS-002, and code `VALIDATION_FAILED`.
2. **Given** an unknown resource **When** the API responds **Then** the response contains AC-RATE-LIMIT-SAFE-ERRORS-003 and code `NOT_FOUND`.
3. **Given** an unexpected error **When** the API responds **Then** the response contains AC-RATE-LIMIT-SAFE-ERRORS-002 and code `SAFE_SYSTEM_ERROR`.

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-001, REQ-RATE-LIMIT-SAFE-ERRORS-002, REQ-RATE-LIMIT-SAFE-ERRORS-004, REQ-RATE-LIMIT-SAFE-ERRORS-006, REQ-RATE-LIMIT-SAFE-ERRORS-007

## User Story US-RATE-LIMIT-SAFE-ERRORS-002

**Title:** Preserve safe auth failure boundaries

**Story:** As a platform administrator, I want auth and permission failures to use stable safe codes, so that RBAC behavior remains explainable without leaking protected metadata.

### Acceptance Criteria

1. **Given** no current user **When** a protected API is called **Then** the response is `401 AUTHENTICATION_REQUIRED`.
2. **Given** a user without permission **When** a protected API is called **Then** the response is `403 PERMISSION_DENIED`.
3. **Given** either failure **When** the response is inspected **Then** it does not expose protected ids beyond the request path or internal decision details.

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-003, REQ-RATE-LIMIT-SAFE-ERRORS-007, REQ-RATE-LIMIT-SAFE-ERRORS-012

## User Story US-RATE-LIMIT-SAFE-ERRORS-003

**Title:** Throttle local mock-safe traffic

**Story:** As an operator, I want deterministic local rate limiting on API requests, so that abusive loops can be represented and tested without production infrastructure.

### Acceptance Criteria

1. **Given** a caller exceeds the configured local budget **When** another request is made **Then** the response is `429 RATE_LIMITED`.
2. **Given** the local limiter is reset in tests **When** the same request sequence is replayed **Then** the same decision sequence occurs.
3. **Given** a rate-limited response **When** it is inspected **Then** it includes safe retry metadata and no raw implementation state.

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-005, REQ-RATE-LIMIT-SAFE-ERRORS-007, REQ-RATE-LIMIT-SAFE-ERRORS-010

## User Story US-RATE-LIMIT-SAFE-ERRORS-004

**Title:** Show safe frontend error states

**Story:** As a frontend user, I want permission, validation, rate limit, not found, and system failures to show safe state messages, so that I can understand the outcome without seeing internal details.

### Acceptance Criteria

1. **Given** the frontend receives `PERMISSION_DENIED` **When** state is rendered **Then** it shows a permission-safe message.
2. **Given** the frontend receives `RATE_LIMITED` **When** state is rendered **Then** it shows a retry-safe message.
3. **Given** the frontend receives `SAFE_SYSTEM_ERROR` **When** state is rendered **Then** it shows a generic system message and does not display raw server text.

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-008, REQ-RATE-LIMIT-SAFE-ERRORS-009, REQ-RATE-LIMIT-SAFE-ERRORS-010

## User Story US-RATE-LIMIT-SAFE-ERRORS-005

**Title:** Close the governance slice with evidence

**Story:** As a project maintainer, I want documentation, tests, scans, and roadmap status updated, so that this governance slice can be reviewed without relying on chat memory.

### Acceptance Criteria

1. **Given** implementation is complete **When** closeout runs **Then** AC-RATE-LIMIT-SAFE-ERRORS-007 is recorded.
2. **Given** docs are inspected **When** traceability is read **Then** it maps REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 to tasks.

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-010, REQ-RATE-LIMIT-SAFE-ERRORS-011

## Dependencies

- Accepted auth-space-rbac and audit-log-foundation behavior.
- Existing Spring Boot API envelope and Vue API client.

## Out of Scope

Real rate-limit infrastructure, production SSO/OIDC, production observability, real secret manager, and external cloud calls.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
