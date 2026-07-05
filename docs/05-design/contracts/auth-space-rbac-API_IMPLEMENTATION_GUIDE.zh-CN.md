# auth-space-rbac — API 实现指南

日期：2026-07-05
状态：Draft SDD
Base path：`/api`
Backend stack：Spring Boot metadata API
Auth model：当前为 backend-enforced mock auth；未来为 SSO/OIDC boundary；本切片不进行 live external provider calls。

## 概览

本指南定义 REQ-AUTH-SPACE-RBAC-001 到 REQ-AUTH-SPACE-RBAC-014 的 auth 与 membership API contracts，以及现有 Atlas APIs 的 guard expectations。

## Authentication

Local/test：

- Provider：`mock`。
- User context 来自 safe mock headers 或 configured local/test default user。
- Mock identities 必须 sample-safe，且只能作为 fixtures 或 seed data 提交。

Future production：

- Provider boundary：`oidc`。
- Live SSO/OIDC setup、secrets、group mapping 和 provider calls 不在范围内。

## Error Response Format

所有错误使用现有 Atlas envelope：

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "FORBIDDEN",
    "message": "Action is not allowed.",
    "fields": null,
    "timestamp": 1783180800000,
    "path": "/api/spaces/space-ibmi/batches"
  },
  "meta": null
}
```

Error table：

| Status | Code | When |
|---|---|---|
| 401 | `UNAUTHORIZED` | Protected API 缺少 current user context。 |
| 403 | `FORBIDDEN` | 用户已认证但缺少 capability。 |
| 400 | `VALIDATION_ERROR` | role、status、email 或 request shape 无效。 |
| 409 | `CONFLICT` | Membership change 违反 owner invariant。 |
| 404 | `NOT_FOUND` | permitted scope 内的 safe not-found。 |

## API Endpoints Summary

| Operation | Method | Endpoint | Auth |
|---|---|---|---|
| Current user context | GET | `/api/auth/me` | Authenticated mock/local context |
| List members | GET | `/api/spaces/{spaceId}/members` | `SPACE_OWNER`, `PLATFORM_ADMIN`; `AUDITOR` read 可后续增加 |
| Create member/invitation | POST | `/api/spaces/{spaceId}/members` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Update member | PATCH | `/api/spaces/{spaceId}/members/{membershipId}` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Remove member | DELETE | `/api/spaces/{spaceId}/members/{membershipId}` | `SPACE_OWNER`, `PLATFORM_ADMIN` |

## Endpoint Reference

### GET `/api/auth/me`

Purpose：返回 current user context、memberships、active space、roles 与 capabilities。

Example response：

```json
{
  "success": true,
  "data": {
    "user": {
      "id": "mock-owner",
      "email": "owner@example.test",
      "displayName": "Atlas Owner",
      "status": "ACTIVE",
      "globalRoles": []
    },
    "activeSpaceId": "space-ibmi",
    "memberships": [
      {
        "id": "membership-owner-ibmi",
        "spaceId": "space-ibmi",
        "spaceName": "IBM i Modernization",
        "role": "SPACE_OWNER",
        "status": "ACTIVE"
      }
    ],
    "capabilities": [
      "SPACE_READ",
      "BATCH_CREATE",
      "WIKI_PUBLISH",
      "MEMBER_MANAGE"
    ]
  },
  "error": null,
  "meta": null
}
```

Validation：

- User 必须 active。
- Disabled user 返回 `403`。

### GET `/api/spaces/{spaceId}/members`

Purpose：列出 Knowledge Space 的 safe members。

Response fields：

| Field | Type | Required |
|---|---|---|
| id | string | Yes |
| userId | string | Yes |
| email | string | Yes |
| displayName | string | Yes |
| role | enum | Yes |
| status | enum | Yes |
| createdAt | timestamp | Yes |
| updatedAt | timestamp | Yes |

### POST `/api/spaces/{spaceId}/members`

Purpose：创建 mock-safe invitation 或 active membership。

Example request：

```json
{
  "email": "reviewer@example.test",
  "displayName": "Atlas Reviewer",
  "role": "KNOWLEDGE_MANAGER",
  "status": "INVITED"
}
```

Rules：

- committed fixtures 中的 email 必须 sample-safe。
- Role 必须来自 accepted role matrix。
- Caller 必须是 target space 的 `SPACE_OWNER` 或 `PLATFORM_ADMIN`。

### PATCH `/api/spaces/{spaceId}/members/{membershipId}`

Purpose：更新 role 或 status。

Example request：

```json
{
  "role": "VIEWER",
  "status": "ACTIVE"
}
```

Rules：

- 不能移除或降级最后一个 active `SPACE_OWNER`。
- `PLATFORM_ADMIN` 可以跨 spaces 更新。

### DELETE `/api/spaces/{spaceId}/members/{membershipId}`

Purpose：将 membership 标记为 `REMOVED`。

Rules：

- 不要求物理删除。
- 适用 last-owner invariant。

## Protected Existing APIs

| Domain | Representative endpoints | Required behavior |
|---|---|---|
| Space | `/api/spaces`, `/api/spaces/{spaceId}` | 读取要求 membership/global admin；创建要求 `PLATFORM_ADMIN` 或 accepted local owner setup。 |
| Batch/file/chunk | `/api/spaces/{spaceId}/batches`, `/api/batches/{batchId}/files`, `/api/files/{fileId}/chunks` | 读取要求 membership；写入要求 `EDITOR`+。 |
| Review/publish/Wiki | `/api/spaces/{spaceId}/review-queues`, `/api/files/{fileId}/publish`, `/api/spaces/{spaceId}/wiki-pages` | 读取要求 membership；写入要求 `KNOWLEDGE_MANAGER`+。 |
| Graph/Ask | `/api/spaces/{spaceId}/graph`, `/api/spaces/{spaceId}/ask` | 读取要求 membership；graph projection 与 Ask create 要求 `KNOWLEDGE_MANAGER`+。 |
| Settings/adapters | `/api/model-configurations/deepseek`, `/api/storage-adapters`, `/api/vector-adapters`, `/api/parser-adapters` | 读写按 role matrix；绝不返回 raw secrets。 |

## State Reference

```text
INVITED -> ACTIVE -> SUSPENDED -> ACTIVE
INVITED/ACTIVE/SUSPENDED -> REMOVED
```

Role values：

- `VIEWER`
- `EDITOR`
- `KNOWLEDGE_MANAGER`
- `SPACE_OWNER`
- `AUDITOR`
- `PLATFORM_ADMIN`

## Concurrency

- Membership updates 应拒绝 stale 或破坏 invariant 的变化。
- Last-owner validation 必须与 membership status/role update 在同一 transaction 中执行。

## Integration Dependencies

- 现有 Atlas API envelope 与 global exception handling。
- `atlas_user` 与 `space_membership` 的 Flyway migration。
- 本切片不依赖外部 SSO/OIDC、SMTP、directory 或 secret-manager。

## Testing Contracts

- `VIEWER` write calls 返回 `403`。
- Missing auth 返回 `401`。
- `SPACE_OWNER` 可以 list/update members。
- Last owner removal 返回 `409`。
- `/api/auth/me` 返回 capabilities 且不含 raw secrets。
