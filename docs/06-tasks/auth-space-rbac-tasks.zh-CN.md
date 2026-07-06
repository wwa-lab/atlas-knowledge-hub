# 实现任务拆解：auth-space-rbac

状态：已按已接受的 auth-space-rbac 切片完成
最后更新：2026-07-07
Source spec：`docs/03-spec/auth-space-rbac-spec.md`
Source design：`docs/05-design/auth-space-rbac-design.md`

## 概览

本任务计划为受控内部 beta 实现后端强制认证和空间 RBAC。SDD 被接受后，第一轮实现已于 2026-07-06 完成，并于 2026-07-07 完成 closeout verification。

## Implementation Status

| Task | Status | Evidence |
|---|---|---|
| T-AUTH-SPACE-RBAC-001 | Done | 已添加 auth/RBAC enums、current-user context、auth decisions 与 authorization matrix。 |
| T-AUTH-SPACE-RBAC-002 | Done | `V13__auth_space_rbac.sql` 增加 users、memberships、constraints、indexes 与 sample-safe seed users。 |
| T-AUTH-SPACE-RBAC-003 | Done | 已添加 user/membership repositories 与 `SpaceMembershipService`，包含 last-owner protection。 |
| T-AUTH-SPACE-RBAC-004 | Done | 已通过 `X-Atlas-User` 实现 mock-local current user resolver；missing/disabled user 被拒绝。 |
| T-AUTH-SPACE-RBAC-005 | Done | MVC interceptor 与 path policy 已实现 `401`、`403` 与 safe `404`。 |
| T-AUTH-SPACE-RBAC-006 | Done | 已添加 `/api/auth/me` 与 `/api/spaces/{spaceId}/members` endpoints。 |
| T-AUTH-SPACE-RBAC-007 | Done | 现有核心 API domains 通过 centralized path policy 受保护；graph-specific header guard 已移除。 |
| T-AUTH-SPACE-RBAC-008 | Done | 前端启动加载 `/api/auth/me`，发送 mock auth header，并基于 capabilities 禁用代表性写操作。 |
| T-AUTH-SPACE-RBAC-009 | Done | `frontend/tests/e2e/auth-space-rbac.spec.ts` 覆盖 `VIEWER`、`KNOWLEDGE_MANAGER` 与 `SPACE_OWNER`，包括 manipulated viewer write `403`。 |
| T-AUTH-SPACE-RBAC-010 | Done | `mvn verify`、frontend typecheck、frontend unit tests、frontend build 与完整 frontend E2E 已通过。 |
| T-AUTH-SPACE-RBAC-011 | Done | 已运行 `git diff --check`、focused secret scan 与 network/dependency scan；命中项为既有 mock/test fixture 或既有 adapter URL。 |
| T-AUTH-SPACE-RBAC-012 | Done | Traceability 已更新 implementation evidence 与 residual risks。 |

## Workstreams

- Backend auth foundation：provider boundary、current user context、policy、envelope errors。
- Persistence：additive user and membership schema plus repositories/services。
- API coverage：`/api/auth/me`、membership APIs，以及 existing domains 的 guards。
- Frontend awareness：auth state 与 capability-driven UI。
- Verification：unit、integration、E2E 与 safety scans。

推荐顺序：

1. T-AUTH-SPACE-RBAC-001 到 T-AUTH-SPACE-RBAC-005 建立 backend data 与 policy。
2. T-AUTH-SPACE-RBAC-006 到 T-AUTH-SPACE-RBAC-008 集成 APIs 与 frontend。
3. T-AUTH-SPACE-RBAC-009 到 T-AUTH-SPACE-RBAC-012 完成验证、扫描和收尾。

## Task Details

### T-AUTH-SPACE-RBAC-001: Add auth and RBAC domain types

- **Objective:** 定义 role、capability、user status、membership status、current user context 与 auth decision result types。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-004、REQ-AUTH-SPACE-RBAC-006、REQ-AUTH-SPACE-RBAC-013。
- **Dependencies:** None。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** 为 enum parsing 与 role/capability coverage 添加 unit tests。

### T-AUTH-SPACE-RBAC-002: Add additive Flyway user and membership schema

- **Objective:** 创建 `atlas_user` 与 `space_membership` schema，并添加 mock/sample-safe seed memberships。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-005、REQ-AUTH-SPACE-RBAC-014。
- **Dependencies:** T-AUTH-SPACE-RBAC-001。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Flyway migration 在 `mvn verify` 中校验；seed data 只包含 sample-safe identities。

### T-AUTH-SPACE-RBAC-003: Implement repositories and membership service

- **Objective:** 增加 user/membership repositories 与 list、create、update、remove、last-owner validation service methods。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-005、REQ-AUTH-SPACE-RBAC-012。
- **Dependencies:** T-AUTH-SPACE-RBAC-002。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Unit/integration tests 覆盖 role/status validation 与最后一个 active `SPACE_OWNER` protection。

### T-AUTH-SPACE-RBAC-004: Implement mock auth provider and current user resolver

- **Objective:** 从 local/test mock auth 解析 current user context，且不进行外部网络调用。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-001、REQ-AUTH-SPACE-RBAC-002、REQ-AUTH-SPACE-RBAC-003、REQ-AUTH-SPACE-RBAC-004。
- **Dependencies:** T-AUTH-SPACE-RBAC-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Integration tests 覆盖 missing auth `401`、mock user success、disabled user denial 与无外部依赖。

### T-AUTH-SPACE-RBAC-005: Implement authorization policy and safe denied responses

- **Objective:** 集中 action-to-capability mapping 与 envelope-compatible `401`/`403` handling。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-007、REQ-AUTH-SPACE-RBAC-009、REQ-AUTH-SPACE-RBAC-010、REQ-AUTH-SPACE-RBAC-011。
- **Dependencies:** T-AUTH-SPACE-RBAC-004。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Role matrix unit tests；`VIEWER` write denial 与 cross-space non-disclosure integration tests。

### T-AUTH-SPACE-RBAC-006: Add `/api/auth/me` and membership endpoints

- **Objective:** 实现 current user 与 member management API contracts。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-004、REQ-AUTH-SPACE-RBAC-012、REQ-AUTH-SPACE-RBAC-014。
- **Dependencies:** T-AUTH-SPACE-RBAC-005。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** API contract tests 覆盖 `/api/auth/me`、member list/create/update/delete、`401`、`403` 与 `409`。

### T-AUTH-SPACE-RBAC-007: Apply guards to existing core API domains

- **Objective:** 保护 space、batch、file、chunk、review/publish、Wiki、graph、Ask、model、storage、vector、parser、conversion、ingestion 与 downstream-refresh APIs。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-007、REQ-AUTH-SPACE-RBAC-011。
- **Dependencies:** T-AUTH-SPACE-RBAC-005。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** 按 domain 的 representative integration tests 展示 allowed/denied outcomes；graph 不再把 graph-only demo headers 作为 primary policy。

### T-AUTH-SPACE-RBAC-008: Add frontend auth state and permission-aware controls

- **Objective:** 加载 `/api/auth/me`，保存 capabilities，并更新 UI controls 以反映 backend-provided permissions。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-008。
- **Dependencies:** T-AUTH-SPACE-RBAC-006、T-AUTH-SPACE-RBAC-007。
- **Owner type:** frontend。
- **Priority:** Must。
- **Verification:** Frontend tests 覆盖 viewer disabled/hidden writes 与 owner member controls。

### T-AUTH-SPACE-RBAC-009: Add role-specific E2E coverage

- **Objective:** 覆盖 ordinary user、`KNOWLEDGE_MANAGER` 与 `SPACE_OWNER` 的产品路径差异。
- **Scope:** 覆盖 AC-AUTH-SPACE-RBAC-004 与 REQ-AUTH-SPACE-RBAC-014。
- **Dependencies:** T-AUTH-SPACE-RBAC-008。
- **Owner type:** QA/frontend。
- **Priority:** Must。
- **Verification:** E2E 断言 role-aware controls，并验证 manipulated viewer write calls 被 backend `403` 拒绝。

### T-AUTH-SPACE-RBAC-010: Run backend and frontend verification

- **Objective:** 代码改动后运行 implementation-level verification。
- **Scope:** 覆盖所有 requirements。
- **Dependencies:** T-AUTH-SPACE-RBAC-001 到 T-AUTH-SPACE-RBAC-009。
- **Owner type:** QA。
- **Priority:** Must。
- **Verification:** `mvn verify`、与触及前端相关的 frontend typecheck/test/build/E2E commands，并报告所有 skipped checks 原因。

### T-AUTH-SPACE-RBAC-011: Run security, network, dependency, and diff scans

- **Objective:** 证明本切片没有新增真实数据、raw secrets、private paths、external cloud calls 或 unsafe dependency drift。
- **Scope:** 覆盖 REQ-AUTH-SPACE-RBAC-014。
- **Dependencies:** T-AUTH-SPACE-RBAC-010。
- **Owner type:** security。
- **Priority:** Must。
- **Verification:** `git diff --check`、focused secret/private-path scan 与 focused network/dependency scan 通过。

### T-AUTH-SPACE-RBAC-012: Update traceability and closeout evidence

- **Objective:** 更新 traceability、roadmap status、verification evidence、residual risks，必要时记录 lessons。
- **Scope:** 覆盖 SDD closeout gates。
- **Dependencies:** T-AUTH-SPACE-RBAC-010、T-AUTH-SPACE-RBAC-011。
- **Owner type:** backend/frontend。
- **Priority:** Must。
- **Verification:** Docs 更新后 `npm run agent:check-sdd -- --slice auth-space-rbac --require-api-guide` 通过。

## Dependency Plan

- Critical path：T-AUTH-SPACE-RBAC-001 → T-AUTH-SPACE-RBAC-002 → T-AUTH-SPACE-RBAC-003 → T-AUTH-SPACE-RBAC-004 → T-AUTH-SPACE-RBAC-005 → T-AUTH-SPACE-RBAC-006/T-AUTH-SPACE-RBAC-007 → T-AUTH-SPACE-RBAC-008 → T-AUTH-SPACE-RBAC-009 → T-AUTH-SPACE-RBAC-010 → T-AUTH-SPACE-RBAC-011 → T-AUTH-SPACE-RBAC-012。
- Parallel work：policy 存在后，T-AUTH-SPACE-RBAC-006 与 T-AUTH-SPACE-RBAC-007 可并行；frontend 在 `/api/auth/me` shape 稳定后开始。

## Risks / Blockers

- 跨许多 controllers 增加 auth guard coverage 范围较大；必须有 representative endpoint tests。
- Spring Security 或等价 filter changes 可能改变现有 integration test setup；mock profile 必须确定性。
- Production SSO/OIDC、audit retention、secret manager 与 rate limiting 保持未来切片。

## Open Questions

- OQ-AUTH-SPACE-RBAC-001：未来 `AUDITOR` 对 audit logs 的 visibility。
- OQ-AUTH-SPACE-RBAC-002：未来 SSO group-to-role mapping policy。

## Definition Of Done

- T-AUTH-SPACE-RBAC-001 到 T-AUTH-SPACE-RBAC-012 全部完成。
- AC-AUTH-SPACE-RBAC-001 到 AC-AUTH-SPACE-RBAC-006 全部验证或显式报告 blocked。
- Verification evidence 与 residual risks 已记录到 traceability。
