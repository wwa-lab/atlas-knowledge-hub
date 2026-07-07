# 需求：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07
Workflow tier：Tier 3 / High-Risk Governance

## Goal

为 Atlas core APIs 增加 mock-safe 的 deterministic local rate limiting 与稳定 safe API error foundation。该切片必须让 authentication、authorization、validation、not found、rate limited 与 unexpected failures 可区分，同时不暴露 stack trace、raw exception、secret、credential、private path、internal endpoint 或 raw source content。

## Scope

- Backend API error responses 使用统一 stable safe envelope。
- Backend exception handling 将 validation、auth/RBAC、not found、conflict、rate limit 与 unexpected errors 映射到 safe code 和 HTTP status。
- Deterministic local rate limiting 保护核心 `/api/**` endpoints，不使用 Redis、gateway、service mesh 或外部 provider。
- Server logs 保留 correlation 与 safe troubleshooting context。
- Frontend API 和产品 shell 展示 permission denied、validation failed、rate limited、not found 与 safe system error 的用户安全状态。
- Tests 证明 redaction、status mapping、rate limit behavior 与代表性 frontend states。

## Exclusions

- 不接入 external rate-limit service、Redis quota store、gateway、service mesh、production SSO/OIDC、real secret manager、external cloud/provider calls、production observability、SIEM export、alerting 或 SLO dashboard。
- 不改变已接受的 auth-space-rbac permission semantics。
- 不改变已接受的 audit-log-foundation audit semantics。
- 不提交 real company data、raw secret、token、password、private endpoint、private absolute path、raw source content 或 credential fixture。

## Requirements

| ID | Priority | Requirement |
|---|---|---|
| REQ-RATE-LIMIT-SAFE-ERRORS-001 | Must | API failures 必须使用 `success=false` 和 safe `error` object，包含 stable code、safe message、timestamp、path 与非敏感 metadata。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-002 | Must | Validation failures 必须返回 `400` 与 `VALIDATION_FAILED`，field messages 必须 safe、bounded，且不包含 raw request payload。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-003 | Must | Authentication 和 authorization failures 必须返回 `401` 或 `403`，code 为 `AUTHENTICATION_REQUIRED` 或 `PERMISSION_DENIED`，且不暴露 protected resource metadata。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-004 | Must | Not found failures 必须返回 `404` 与 `NOT_FOUND`，message 为 generic safe message。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-005 | Must | Rate limited requests 必须返回 `429` 与 `RATE_LIMITED`、safe retry metadata，并在 tests 中具备 deterministic local behavior。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-006 | Must | Unexpected exceptions 必须返回 `500` 与 `SAFE_SYSTEM_ERROR`、correlation id，同时在 server-side 安全记录 context。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-007 | Must | Safe error generation 必须从 API responses 中 redacts secrets、credentials、private paths、internal endpoints、raw stack traces、raw exception class names 与 raw source content。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-008 | Must | Frontend API errors 必须提供 typed safe status categories，覆盖 permission denied、validation failed、rate limited、not found 与 safe system errors。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-009 | Must | Product shell 必须渲染代表性 user-safe states，且不泄露 internal implementation details。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-010 | Must | Tests 必须覆盖 backend redaction、status mapping、deterministic rate limit behavior、API contract responses 与 frontend state rendering。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-011 | Must | Traceability、slice roadmap 与 repo status docs 必须记录 implementation status、verification evidence 与 residual risks。 |
| REQ-RATE-LIMIT-SAFE-ERRORS-012 | Must | Implementation 必须保持 auth-space-rbac、audit-log-foundation 与 secret-manager-integration boundaries。 |

## Acceptance Criteria

| ID | Criteria |
|---|---|
| AC-RATE-LIMIT-SAFE-ERRORS-001 | Core API errors 使用 stable safe envelope。 |
| AC-RATE-LIMIT-SAFE-ERRORS-002 | Responses 绝不暴露 raw stack traces、raw exceptions、secrets、private paths、internal endpoints、raw credentials 或 raw source content。 |
| AC-RATE-LIMIT-SAFE-ERRORS-003 | Permission denied、validation failed、rate limited、not found 与 unexpected errors 可通过 safe code/status 区分。 |
| AC-RATE-LIMIT-SAFE-ERRORS-004 | Local rate limiting deterministic 且可测试。 |
| AC-RATE-LIMIT-SAFE-ERRORS-005 | Frontend 显示 user-safe error states。 |
| AC-RATE-LIMIT-SAFE-ERRORS-006 | Server logs/audit hooks 保留 safe troubleshooting context。 |
| AC-RATE-LIMIT-SAFE-ERRORS-007 | Verification commands 与 scans 通过，或被明确报告。 |

## Assumptions

- 现有 `ApiEnvelope`、`ErrorBody`、`GlobalExceptionHandler`、`AtlasAuthInterceptor` 与 frontend `ApiError` 是扩展点。
- Existing audit behavior 不被重新定义；本切片只在 auth/error boundaries 已拥有的位置记录 safe denial/error context。

## Open Questions

在 attached goal 的预授权边界内没有 blocking open question。
