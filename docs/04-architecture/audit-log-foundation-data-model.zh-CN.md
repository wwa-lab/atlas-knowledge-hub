# 数据模型：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06

## Overview

本切片增加通用 append-only audit event model。它通过 safe ids 引用现有 Atlas users、spaces 与 target resources，只保存 bounded safe summaries 与 metadata。

## Entity Relationship Diagram

```text
┌──────────────────────┐       1:N       ┌──────────────────────────┐
│ atlas.atlas_user      │────────────────►│ atlas.audit_event         │
│ id PK                 │ actor_user_id   │ id PK                     │
└──────────────────────┘                  │ actor_user_id             │
                                          │ space_id                  │
┌──────────────────────┐       1:N        │ action/category/result    │
│ atlas.space           │────────────────►│ target_type/target_id     │
│ id PK                 │ space_id        │ safe_summary/metadata     │
└──────────────────────┘                  └──────────────────────────┘
```

## Entity Definitions

### AuditEvent

- **Table name:** `atlas.audit_event`
- **Purpose:** Append-only governance/security event。

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | Stable audit event id。 |
| created_at | Timestamp | No | Event creation time。 |
| actor_user_id | String | Yes | Resolved Atlas user id；anonymous/system-safe events 可为 null。 |
| actor_display | String | Yes | Safe display label，不是 raw provider claim。 |
| space_id | String | Yes | 安全已知时的 target space id。 |
| category | String | No | Event category enum。 |
| action | String | No | Stable action enum/string。 |
| result | String | No | `SUCCEEDED`、`DENIED`、`FAILED`、`CONFLICT`、`SKIPPED` 或 `SAFE_NOT_FOUND`。 |
| severity | String | No | `INFO`、`NOTICE`、`WARNING` 或 `SECURITY`。 |
| target_type | String | Yes | Safe target type。 |
| target_id | String | Yes | Safe target id。 |
| request_id | String | Yes | Safe correlation id。 |
| safe_summary | String | Yes | Human-readable safe summary。 |
| metadata | JSON | No | Bounded safe metadata object。 |

约束：

- 无 update/delete product API。
- Metadata allowlist 仅允许 counts、ids、enum states、reason codes、route patterns 与 booleans。
- Index `(space_id, created_at desc)`、`(target_type, target_id, created_at desc)`、`(actor_user_id, created_at desc)` 与 `(category, result, created_at desc)`。

## Enums

### AuditCategory

`AUTH`、`MEMBERSHIP`、`REVIEW`、`PUBLISH`、`WIKI`、`GRAPH`、`ASK`、`MODEL`、`ADAPTER`、`SETTINGS`

### AuditResult

`SUCCEEDED`、`DENIED`、`FAILED`、`CONFLICT`、`SKIPPED`、`SAFE_NOT_FOUND`

### AuditSeverity

`INFO`、`NOTICE`、`WARNING`、`SECURITY`

## Safe Metadata Examples

| Operation | Allowed metadata |
|---|---|
| Auth denied | method, routePattern, reasonCode, statusCode |
| Membership change | targetUserId, previousRole, nextRole, previousStatus, nextStatus |
| Wiki ingest | runId, createdCount, updatedCount, skippedCount, failedCount |
| Graph projection | runId, nodeCount, edgeCount, skippedCount |
| Ask create | askRunId, citationCount, reviewPolicy, resultStatus |
| Model configuration | providerKey, configuredFlagChanged, adapterStatus |

Forbidden metadata：

- Raw request or response bodies。
- Raw source content or generated content。
- Raw prompt/answer/provider payload。
- API keys、tokens、passwords、provider payloads。
- Private paths、internal hostnames、stack traces、raw runtime logs。

## Relationship To Existing Graph Audit

`atlas.graph_audit_record` 已存在为 graph-specific governance evidence。本切片应保留它，并增加 documented bridge 或 dual-write path，而不是直接删除。未来 cleanup slice 可在接受后统一 graph audit storage。

## Traceability

- `AuditEvent` 对应 REQ-AUDIT-LOG-FOUNDATION-001、REQ-AUDIT-LOG-FOUNDATION-002、T-AUDIT-LOG-FOUNDATION-001 与 T-AUDIT-LOG-FOUNDATION-002。
- Audit enums 与 indexes 对应 REQ-AUDIT-LOG-FOUNDATION-007、REQ-AUDIT-LOG-FOUNDATION-011 与 T-AUDIT-LOG-FOUNDATION-006。
- Graph audit bridge note 对应 REQ-AUDIT-LOG-FOUNDATION-006 与 T-AUDIT-LOG-FOUNDATION-005。
