# 实现任务拆解：audit-log-foundation

状态：用户接受后已实现
最后更新：2026-07-07
Source spec: `docs/03-spec/audit-log-foundation-spec.md`
Source design: `docs/05-design/audit-log-foundation-design.md`

## Overview

实现面向核心 Atlas governance events 的 safe append-only audit log foundation。用户已接受 SDD，本任务集已在 prototype stack 中实现。

## Workstreams

- Backend audit domain and persistence。
- Auth、membership 与代表性 knowledge operations 中的 audit capture。
- 带 RBAC 与 pagination 的 read-only audit APIs。
- Frontend audit panel 与 typed API integration。
- Verification、safety scans、traceability 与 closeout。

## Implementation Evidence

- T-AUDIT-LOG-FOUNDATION-001 至 T-AUDIT-LOG-FOUNDATION-003：通过 `AuditEvent`、audit enums、DTO/mapper、`AuditEventRepository`、`AuditLogService` 与 `V14__audit_log_foundation.sql` 实现。
- T-AUDIT-LOG-FOUNDATION-004：已覆盖 auth denied 与 membership create/update/remove/last-owner conflict flows。
- T-AUDIT-LOG-FOUNDATION-005：已覆盖 graph projection/review/access-denied bridge events、Wiki publish events 与 Trusted Ask run outcomes。Wiki ingest/linkify-lint 与 adapter/runtime emitters 明确延后到后续覆盖。既有 `graph_audit_record` 行为保留，并并行写入通用 audit event。
- T-AUDIT-LOG-FOUNDATION-006：已实现 `GET /api/spaces/{spaceId}/audit-events` 与 `GET /api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}`。
- T-AUDIT-LOG-FOUNDATION-007：已实现由 typed API 驱动的只读 Vue settings audit panel。
- T-AUDIT-LOG-FOUNDATION-008 与 T-AUDIT-LOG-FOUNDATION-009：验证与溯源证据记录在 `docs/00-context/audit-log-foundation-traceability.zh-CN.md`。

## Task Details

### T-AUDIT-LOG-FOUNDATION-001: Add audit domain model and enums

- **Objective:** 定义 audit event entity、category/result/severity enums、safe metadata rules、DTOs 与 mapper。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 002, 011。
- **Dependencies:** Accepted SDD。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Unit tests 覆盖 enum values、mapper output 与 safe metadata allowlist。

### T-AUDIT-LOG-FOUNDATION-002: Add Flyway migration and repository

- **Objective:** 创建 `atlas.audit_event`、indexes 与 repository query methods。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 007, 012。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-001。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** `cd backend && mvn verify` 验证 Flyway 与 repository tests。

### T-AUDIT-LOG-FOUNDATION-003: Implement AuditLogService

- **Objective:** 增加 centralized append-only event recording、safe command validation、metadata allowlist 与 query methods。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 002, 007, 009。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-002。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Service tests 覆盖 safe event creation、forbidden metadata rejection、pagination 与 filters。

### T-AUDIT-LOG-FOUNDATION-004: Record auth and membership audit events

- **Objective:** 为 denied auth decisions 与 membership create/update/remove/conflict flows 产生 events。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-003, 004, 008, 009。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Integration tests 覆盖 `401`、`403`、safe `404`、membership success 与 last-owner conflict audit events。

### T-AUDIT-LOG-FOUNDATION-005: Record representative knowledge-operation events

- **Objective:** 为 Wiki publish、graph projection/review/access-denied 与 Trusted Ask run outcomes 产生 safe foundation events，并将 Wiki ingest/linkify-lint 与 adapter/runtime emitters 记录为 deferred follow-on coverage。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-005, 006。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Service/integration tests 验证 representative publish、graph 与 Ask events；traceability 记录 deferred emitters，且不声称 production audit coverage。

### T-AUDIT-LOG-FOUNDATION-006: Add audit read APIs

- **Objective:** 增加按 space 与 target 查询 audit events 的只读 endpoints，支持 filters 与 pagination。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-007, 008, 009, 012。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** API contract tests 覆盖 filters、pagination、RBAC、invalid filters 与 cross-space non-disclosure。

### T-AUDIT-LOG-FOUNDATION-007: Add frontend audit panel

- **Objective:** 增加 typed API calls、domain types 与由 backend capabilities gate 的只读 audit UI。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-010。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-006。
- **Owner type:** frontend。
- **Priority:** Should。
- **Verification:** `npm --prefix frontend run typecheck`、unit tests、build，以及 UI 进入用户路径时的 E2E。

### T-AUDIT-LOG-FOUNDATION-008: Run verification and safety scans

- **Objective:** 运行 backend、frontend、workflow、diff、secret/private-path 与 network/dependency checks。
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-002, 012。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-001 through T-AUDIT-LOG-FOUNDATION-007。
- **Owner type:** QA/security。
- **Priority:** Must。
- **Verification:** `npm run agent:check-sdd -- --slice audit-log-foundation --require-api-guide`、`cd backend && mvn verify`、若 touched frontend 则跑 frontend checks、`git diff --check`、focused scans 与 `npm run agent:closeout`。

### T-AUDIT-LOG-FOUNDATION-009: Update traceability and roadmap status

- **Objective:** 记录 completed tasks、verification evidence、residual risks、graph audit decision 与 closeout status。
- **Scope:** Goal-driven SDD closeout。
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-008。
- **Owner type:** backend/frontend。
- **Priority:** Must。
- **Verification:** Traceability 与 roadmap files 匹配 implementation maturity，且不声称 production readiness。

## Dependency Plan

Critical path: T-001 -> T-002 -> T-003 -> T-004/T-005/T-006 -> T-007 -> T-008 -> T-009。

Parallel work：

- T-004 与 T-005 可在 AuditLogService 存在后并行。
- T-007 可在 API response shape 稳定后开始。

## Risks / Blockers

- 当前实现是 prototype audit foundation，不是生产 retention/SIEM/export/compliance system。
- 历史 `graph_audit_record` 会保留，但不会 backfill 到 `audit_event`。
- Graph API contract verification 已在校准 controller-slice auth test 边界后通过。

## Definition Of Done

- 所有 Must tasks 已完成。
- SDD gate 与 implementation verification evidence 已记录。
- 不引入 raw secrets、private paths、raw content、provider payloads 或 external cloud calls。
- Traceability 记录 evidence、residual risks 与 deferred historical graph audit backfill。
