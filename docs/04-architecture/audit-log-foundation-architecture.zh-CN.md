# 系统架构：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06

## Overview

- **Architecture summary:** 本切片在现有 Spring Boot metadata service 中增加集中式 audit domain。Domain services 与 auth interceptor 通过 `AuditLogService` 产生 safe `AuditEvent` records；read APIs 向 Vue frontend 暴露分页、RBAC-protected audit DTOs。
- **Design objective:** 捕获高价值治理证据，同时不保存 raw sensitive payloads。
- **Architectural style:** Layered Spring Boot service，使用 PostgreSQL/Flyway persistence 与 Vue read-only UI integration。

## Architectural Drivers

- 面向 auth、membership、review/publish、Wiki、graph、Ask、settings、model 与 adapter operations 的统一 event shape。
- RBAC-protected read APIs。
- Safe summary 与 metadata allowlist。
- 保留当前 graph audit evidence。
- 无 raw secrets、raw content、private paths、provider payloads、stack traces 或 raw logs。

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Auditor · Space Owner · Knowledge Manager · Platform Admin   │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTPS
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Frontend                                                  │
│ Audit panel · filters · empty/error/denied states             │
└────────────────────────────┬─────────────────────────────────┘
                             │ REST / JSON ApiEnvelope
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                      │
│ Auth interceptor · Audit controller · Domain controllers       │
├──────────────────────────────────────────────────────────────┤
│ AuditLogService                                                │
│ Safe event builder · metadata allowlist · read policy checks    │
├──────────────────────────────────────────────────────────────┤
│ Domain services emit safe events                               │
│ Membership · Review/Publish · Wiki · Graph · Ask · Adapters    │
└────────────────────────────┬─────────────────────────────────┘
                             │ JPA / Flyway
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ PostgreSQL atlas.audit_event                                  │
│ Append-only records · space/time and target/time indexes       │
└──────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### Backend Components

- **Audit event domain:** audit action、category、result、severity 的 immutable entity 与 enums。
- **AuditLogService:** 单一写入边界，接收 safe event commands 并持久化 append-only events。
- **AuditEventRepository:** 支持按 space/time、target/time、actor/action/result/category 与 pagination 查询。
- **AuditLogController:** 面向 space 与 target audit queries 的只读 API。
- **Auth audit hook:** 在可安全解析 scope 时记录现有 `AtlasAuthInterceptor` 的 denied decisions。
- **Domain audit hooks:** 在 membership、review/publish、Wiki、graph、Ask、model/settings 与 adapter services 中记录代表性事件。

### Frontend Components

- **Audit panel:** 只读列表与 filters。
- **Audit API client/types:** typed DTOs 与 query parameters。
- **Capability gate:** 使用 `/api/auth/me` capabilities 与 safe API errors，而不是硬编码本地角色判断。

## Data Architecture

| Entity | Responsibility |
|---|---|
| AuditEvent | Append-only governance record。 |
| AuditEventMetadata | 面向 counts、reason codes 与 correlation ids 的 safe bounded JSON。 |
| AuditEventQuery | Read model filter 与 pagination contract。 |

Existing `graph_audit_record` 是窄范围 graph-governance record。新实现应为 graph actions 选择 dual-write 到 legacy + general audit events，或提供 documented bridge，直到未来 cleanup slice。本文不要求 destructive migration。

## Integration Architecture

- **Auth/RBAC:** 复用 `auth-space-rbac` 的 `CurrentUserContext`、`AuthDecision` 与 role/capability checks。
- **Domain services:** 通过 centralized audit service 产生 events；product logic 不直接写 repository。
- **Adapters:** Parser/converter/model/vector/storage 保持在现有 adapter boundaries 后；audit 只保存 safe operation summaries。

## Risks And Tradeoffs

- 捕获所有 read 操作会产生噪音；v0 聚焦 denied access 与 high-value writes。
- Graph audit bridge 可避免 destructive migration，但可能短期重复 graph evidence。
- 若某些 audit write 非阻塞，operational safe logs 必须仍能暴露 write failures。

## Traceability

- REQ-AUDIT-LOG-FOUNDATION-001、REQ-AUDIT-LOG-FOUNDATION-002 与 T-AUDIT-LOG-FOUNDATION-001 through T-AUDIT-LOG-FOUNDATION-003 对应 audit domain、service、repository 与 migration architecture。
- REQ-AUDIT-LOG-FOUNDATION-003 through REQ-AUDIT-LOG-FOUNDATION-006 与 T-AUDIT-LOG-FOUNDATION-004 through T-AUDIT-LOG-FOUNDATION-005 对应 auth/domain audit emitters 与 graph audit preservation。
- REQ-AUDIT-LOG-FOUNDATION-007 through REQ-AUDIT-LOG-FOUNDATION-010 与 T-AUDIT-LOG-FOUNDATION-006 through T-AUDIT-LOG-FOUNDATION-007 对应 read APIs、RBAC checks 与 frontend audit inspection。
