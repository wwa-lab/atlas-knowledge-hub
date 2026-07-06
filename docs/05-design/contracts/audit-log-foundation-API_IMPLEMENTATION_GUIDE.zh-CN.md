# audit-log-foundation API 实现指南

状态：待人工审阅的草案
最后更新：2026-07-06
Base path: `/api`
Auth model: `auth-space-rbac` current-user 与 capability enforcement。

## Overview

本文定义只读 audit APIs 与 internal audit event contracts。Clients 不能 create、update 或 delete audit events。

## Authentication And Authorization

Audit read endpoints 要求目标 space 的 governance-read capability。`PLATFORM_ADMIN` 仅在显式支持时可读取 mock/internal global scopes。`VIEWER` 与 `EDITOR` 默认无 audit-read access。

Denied responses 使用标准 `ApiEnvelope` error shape。

## Error Response Format

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "The current user is not allowed to perform this action.",
    "fields": null,
    "timestamp": 1783372800000,
    "path": "/api/spaces/ibm-i-modernization/audit-events"
  },
  "meta": null
}
```

## API Endpoints Summary

| Operation | Method | Endpoint | Auth |
|---|---|---|---|
| List space audit events | GET | `/api/spaces/{spaceId}/audit-events` | Governance read for space |
| List target audit events | GET | `/api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}` | Governance read for space |

## Endpoint Reference

### List Space Audit Events

`GET /api/spaces/{spaceId}/audit-events`

Query parameters:

| Name | Type | Required | Notes |
|---|---|---|---|
| actorUserId | string | No | Safe Atlas user id。 |
| category | string | No | Audit category enum。 |
| action | string | No | Stable action string。 |
| result | string | No | Audit result enum。 |
| targetType | string | No | Safe target type。 |
| targetId | string | No | Safe target id。 |
| createdFrom | ISO timestamp | No | Inclusive lower bound。 |
| createdTo | ISO timestamp | No | Exclusive upper bound。 |
| page | integer | No | Default `0`。 |
| size | integer | No | 实现需设置上限。 |

Example response:

```json
{
  "success": true,
  "data": [
    {
      "id": "audit-001",
      "createdAt": "2026-07-06T00:00:00Z",
      "actorUserId": "mock-owner",
      "actorDisplay": "Atlas Owner",
      "spaceId": "ibm-i-modernization",
      "category": "MEMBERSHIP",
      "action": "MEMBERSHIP_UPDATE",
      "result": "SUCCEEDED",
      "severity": "NOTICE",
      "targetType": "space_membership",
      "targetId": "mock-viewer",
      "requestId": "req-001",
      "safeSummary": "Space membership role updated.",
      "metadata": {
        "previousRole": "VIEWER",
        "nextRole": "AUDITOR"
      }
    }
  ],
  "error": null,
  "meta": {
    "page": 0,
    "size": 25,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### List Target Audit Events

`GET /api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}`

Purpose：列出一个 permitted target 的 audit history。

Validation：

- `targetType` 与 `targetId` 必填。
- Cross-space targets 不得披露 existence。
- Pagination 与 time filters 与 space audit list endpoint 一致。

## Internal Event Command Contract

Internal code 应传入以下 command：

| Field | Required | Notes |
|---|---|---|
| actorUserId | No | Anonymous/system-safe events 可为 null。 |
| actorDisplay | No | Safe display label。 |
| spaceId | No | Space-scoped event 在安全已知时必填。 |
| category | Yes | Enum。 |
| action | Yes | Stable action。 |
| result | Yes | Enum。 |
| severity | Yes | Enum。 |
| targetType | No | Safe target type。 |
| targetId | No | Safe target id。 |
| requestId | No | Correlation id。 |
| safeSummary | No | 不含敏感内容。 |
| metadata | No | Allowlisted safe JSON only。 |

Forbidden in commands：

- Raw request/response bodies。
- Raw source content or generated content。
- Raw prompt/answer/provider payload。
- Tokens、credentials、API keys、passwords。
- Private paths、internal hostnames、stack traces、raw runtime logs。

## Testing Contracts

- Unauthorized read returns `401`。
- Viewer/editor audit read returns `403` or safe `404`。
- Auditor/owner/manager permitted read returns safe DTOs。
- Invalid filter returns `400`。
- Cross-space target query does not leak event existence。
- Stored events do not contain forbidden sensitive strings。

## Traceability

- Space 与 target list endpoints 覆盖 REQ-AUDIT-LOG-FOUNDATION-007、REQ-AUDIT-LOG-FOUNDATION-008、REQ-AUDIT-LOG-FOUNDATION-009、T-AUDIT-LOG-FOUNDATION-006 与 AC-AUDIT-LOG-FOUNDATION-004。
- Internal event command contract 覆盖 REQ-AUDIT-LOG-FOUNDATION-001、REQ-AUDIT-LOG-FOUNDATION-002、REQ-AUDIT-LOG-FOUNDATION-005、T-AUDIT-LOG-FOUNDATION-003 与 T-AUDIT-LOG-FOUNDATION-005。
