# 用户故事：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07

## User Story US-RATE-LIMIT-SAFE-ERRORS-001

**Title:** Receive safe API errors

**Story:** 作为知识用户，我希望 API failures 能解释失败类别但不暴露内部细节，以便我可以安全恢复或报告问题。

### Acceptance Criteria

1. **Given** validation error **When** API responds **Then** response 包含 AC-RATE-LIMIT-SAFE-ERRORS-001、AC-RATE-LIMIT-SAFE-ERRORS-002 与 code `VALIDATION_FAILED`。
2. **Given** unknown resource **When** API responds **Then** response 包含 AC-RATE-LIMIT-SAFE-ERRORS-003 与 code `NOT_FOUND`。
3. **Given** unexpected error **When** API responds **Then** response 包含 AC-RATE-LIMIT-SAFE-ERRORS-002 与 code `SAFE_SYSTEM_ERROR`。

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-001, REQ-RATE-LIMIT-SAFE-ERRORS-002, REQ-RATE-LIMIT-SAFE-ERRORS-004, REQ-RATE-LIMIT-SAFE-ERRORS-006, REQ-RATE-LIMIT-SAFE-ERRORS-007

## User Story US-RATE-LIMIT-SAFE-ERRORS-002

**Title:** Preserve safe auth failure boundaries

**Story:** 作为平台管理员，我希望 auth 与 permission failures 使用 stable safe codes，以便 RBAC 行为可解释且不泄露 protected metadata。

### Acceptance Criteria

1. **Given** no current user **When** protected API is called **Then** response 为 `401 AUTHENTICATION_REQUIRED`。
2. **Given** user without permission **When** protected API is called **Then** response 为 `403 PERMISSION_DENIED`。
3. **Given** either failure **When** response is inspected **Then** it does not expose protected ids beyond the request path or internal decision details。

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-003, REQ-RATE-LIMIT-SAFE-ERRORS-007, REQ-RATE-LIMIT-SAFE-ERRORS-012

## User Story US-RATE-LIMIT-SAFE-ERRORS-003

**Title:** Throttle local mock-safe traffic

**Story:** 作为 operator，我希望 API requests 有 deterministic local rate limiting，以便无需 production infrastructure 也能表示并测试 abusive loops。

### Acceptance Criteria

1. **Given** caller exceeds configured local budget **When** another request is made **Then** response 为 `429 RATE_LIMITED`。
2. **Given** local limiter is reset in tests **When** same request sequence is replayed **Then** same decision sequence occurs。
3. **Given** rate-limited response **When** inspected **Then** it includes safe retry metadata and no raw implementation state。

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-005, REQ-RATE-LIMIT-SAFE-ERRORS-007, REQ-RATE-LIMIT-SAFE-ERRORS-010

## User Story US-RATE-LIMIT-SAFE-ERRORS-004

**Title:** Show safe frontend error states

**Story:** 作为 frontend user，我希望 permission、validation、rate limit、not found 与 system failures 显示安全状态消息，以便理解结果而不看到内部细节。

### Acceptance Criteria

1. **Given** frontend receives `PERMISSION_DENIED` **When** state is rendered **Then** it shows a permission-safe message。
2. **Given** frontend receives `RATE_LIMITED` **When** state is rendered **Then** it shows a retry-safe message。
3. **Given** frontend receives `SAFE_SYSTEM_ERROR` **When** state is rendered **Then** it shows a generic system message and does not display raw server text。

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-008, REQ-RATE-LIMIT-SAFE-ERRORS-009, REQ-RATE-LIMIT-SAFE-ERRORS-010

## User Story US-RATE-LIMIT-SAFE-ERRORS-005

**Title:** Close the governance slice with evidence

**Story:** 作为项目维护者，我希望 documentation、tests、scans 与 roadmap status 被更新，以便该治理切片可 review 且不依赖 chat memory。

### Acceptance Criteria

1. **Given** implementation is complete **When** closeout runs **Then** AC-RATE-LIMIT-SAFE-ERRORS-007 is recorded。
2. **Given** docs are inspected **When** traceability is read **Then** it maps REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 to tasks。

Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-010, REQ-RATE-LIMIT-SAFE-ERRORS-011

## Dependencies

- 已接受的 auth-space-rbac 与 audit-log-foundation 行为。
- 现有 Spring Boot API envelope 与 Vue API client。

## Out of Scope

真实 rate-limit infrastructure、production SSO/OIDC、production observability、real secret manager 与 external cloud calls。

## Open Questions

在 attached goal 的预授权边界内没有 blocking open question。
