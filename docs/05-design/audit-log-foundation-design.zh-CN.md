# 详细设计：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06

## Overview

本设计在 backend 中引入 centralized audit module，并在 frontend 中增加 read-only audit panel。Backend 拥有 enforcement、event creation、sanitization、persistence 与 read filtering；frontend 只渲染 safe DTOs。

## Design Assumptions

- `auth-space-rbac` 可用，并提供 current-user context、role/capability decisions 与 safe denied responses。
- PostgreSQL/Flyway 仍是 persistence mechanism。
- `graph_audit_record` 保持不变，直到实现选择 safe bridge 或 dual-write strategy。

## Design Scope

范围内：

- Audit event entity、repository、service、DTOs、controller、migration 与 tests。
- Auth、membership 与代表性 knowledge operations 的 audit emitters。
- 只读 Vue audit surface。

范围外：

- Retention jobs、SIEM export、alerting、signing、secret manager、rate limiting 与 live identity provider integration。

## Module Design

### Audit Domain

- `AuditEvent` 表示 append-only records。
- Audit enums 定义 category、result 与 severity。
- DTOs 只暴露 safe response fields。

### AuditLogService

职责：

- 接收 auth 与 domain services 的 audit event commands。
- 校验 safe event shape。
- 应用 metadata allowlist。
- 通过 repository 持久化 events。
- 使用 RBAC-aware service methods 查询 events。

Service 应提供表达意图的方法，例如 record denied auth decision、record membership change 与 record representative knowledge operation。Settings/adapter operation methods 延后到 dedicated governance slices。实现可共享 internal builder，但 metadata 必须受 service-owned allowlist 约束。

### Audit Read API

职责：

- 解析 filters 与 pagination。
- 通过现有 auth boundary 强制 governance-read permissions。
- 返回 `ApiEnvelope` 与 page metadata。
- 避免 cross-space target lookups 泄露。

### Domain Emitters

代表性 emitters 添加到：

- Auth/RBAC denied outcomes。
- Membership service。
- Review/publish service 中的 Wiki publish path。
- Wiki ingest 与 linkify/lint services 是 deferred follow-on emitters。
- Graph service，同时保留 existing graph audit evidence。
- Ask operations。
- Model/vector/storage/parser/converter operation emitters 延后到 adapter/runtime governance slices。

### Frontend Audit Panel

职责：

- 通过 typed API helper 获取 safe audit events。
- 渲染 filters、list、loading、empty、denied 与 error states。
- 当 capabilities 不允许 governance audit reads 时隐藏或禁用入口。

## API / Interface Design

API guide 是 endpoint paths 与 JSON fields 的权威来源。必须提供：

- List events for a space。
- List events for a target within a space。
- Optional current-user recent governance events only if RBAC allows it。

Clients 不暴露 create/update/delete audit API。

## Data Design

使用 `atlas.audit_event`，含 append-only records 与以下 indexes：

- Space timeline。
- Target timeline。
- Actor timeline。
- Category/result reporting。

`metadata` 是 JSON object，但必须由 allowlist 构建。禁止从 request 直接传入 arbitrary map。

## UI / User Flow Design

推荐第一版 UI 位置：现有 space settings 或 governance surface 内的 Governance/Audit panel。如果当前 UI 没有 dedicated governance tab，第一版可在 settings 下新增 audit section，同时保持只读。

Rows 展示：

- Time。
- Actor label。
- Category/action。
- Result/severity。
- Target type and target id。
- Safe summary。

Filters：

- Category。
- Result。
- Actor。
- Target type/id。
- Time range。

## Validation And Error Handling

- Page size 必须有上限。
- Time range 必须拒绝非法区间。
- Unknown category/result/action filters 返回 safe validation errors。
- Denied audit reads 显示非敏感 UI messages。
- Audit write sanitization failures 应让 tests 失败，并拒绝 unsafe event creation。

## Testing Considerations

- Backend unit tests 覆盖 safe metadata builder 与 enum coverage。
- Backend integration tests 覆盖 migration、append-only behavior、RBAC、filters 与 cross-space denial。
- Service tests 覆盖代表性 emitters。
- Frontend unit tests 覆盖 rendering 与 denied/empty states。
- 若 UI surface 在本切片实现，则增加 Playwright E2E。
- Safety scans 覆盖 secrets、private paths、external network calls 与 dependency drift。

## Risks / Design Tradeoffs

- 审计许多 read events 噪音较大，因此 v0 聚焦 denied access 与 governance writes。
- Metadata allowlist 比 free-form map 安全，但新操作需要 audit context 时要小幅扩展。
- Graph audit bridge 可能短期重复数据，但避免 destructive migration。

## Open Questions

- OQ-AUDIT-LOG-FOUNDATION-001：Auditor category visibility by default。
- OQ-AUDIT-LOG-FOUNDATION-002：Graph audit backfill versus bridge。

## Traceability

- Audit domain 与 `AuditLogService` 覆盖 REQ-AUDIT-LOG-FOUNDATION-001、REQ-AUDIT-LOG-FOUNDATION-002、REQ-AUDIT-LOG-FOUNDATION-011、T-AUDIT-LOG-FOUNDATION-001、T-AUDIT-LOG-FOUNDATION-002 与 T-AUDIT-LOG-FOUNDATION-003。
- Domain emitters 覆盖 REQ-AUDIT-LOG-FOUNDATION-003 through REQ-AUDIT-LOG-FOUNDATION-006 与 T-AUDIT-LOG-FOUNDATION-004 through T-AUDIT-LOG-FOUNDATION-005。
- Audit read API 与 frontend panel 覆盖 REQ-AUDIT-LOG-FOUNDATION-007 through REQ-AUDIT-LOG-FOUNDATION-010 与 T-AUDIT-LOG-FOUNDATION-006 through T-AUDIT-LOG-FOUNDATION-007。
