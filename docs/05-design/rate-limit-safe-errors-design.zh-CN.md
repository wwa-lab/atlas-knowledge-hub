# 设计：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07

## Design Scope

使用当前 backend 与 frontend extension points 实现 REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012。

## Backend Design

- Safe error factory 创建每个 API error body。
- Sanitizer 在 serialization 前 bounds and redacts all fields。
- Global exception handler 映射 validation、not found、conflict 与 unexpected errors。
- Auth interceptor 使用同一个 factory 写 safe auth errors。
- Local rate-limit interceptor 使用 in-memory per-caller fixed window。Default values 保持 local/mock-safe，并可通过 tests 的 headers 或 configuration 覆盖。
- Test support 可以 reset limiter state，不新增 production endpoint。

## Frontend Design

- `ApiError` 包含 safe code、status、safe category、optional retry metadata 与 correlation id。
- API client 使用 backend safe error object，unsafe categories 不显示 raw server text。
- Product shell 在 administration/API area 渲染 representative safe error previews，覆盖 permission denied、validation、rate-limited、not found 与 system error states。

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

- Backend unit tests cover sanitizer and deterministic limiter。
- Backend integration tests cover representative validation、permission、not found、rate limit 与 unexpected errors。
- Frontend tests cover `ApiError` classification and visible safe states。
- Scans cover forbidden secret/private-path/real-data patterns。

## Traceability

T-RATE-LIMIT-SAFE-ERRORS-001, T-RATE-LIMIT-SAFE-ERRORS-002, T-RATE-LIMIT-SAFE-ERRORS-003, T-RATE-LIMIT-SAFE-ERRORS-004, T-RATE-LIMIT-SAFE-ERRORS-005, T-RATE-LIMIT-SAFE-ERRORS-006, T-RATE-LIMIT-SAFE-ERRORS-007, and T-RATE-LIMIT-SAFE-ERRORS-008 implement this design and verify AC-RATE-LIMIT-SAFE-ERRORS-001 through AC-RATE-LIMIT-SAFE-ERRORS-007.
