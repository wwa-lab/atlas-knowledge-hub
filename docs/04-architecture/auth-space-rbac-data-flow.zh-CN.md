# 数据流：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05

## 概览

本文描述 REQ-AUTH-SPACE-RBAC-001 到 REQ-AUTH-SPACE-RBAC-014 的 current-user resolution、protected API authorization、membership management 与 frontend permission awareness 运行时数据流。

## Flow 1：Frontend Boot And Current User Context

```text
Vue App
  │
  │ GET /api/auth/me
  ▼
Auth Controller
  │
  │ resolve current user
  ▼
Current User Resolver
  │
  ├── local/test → Mock Auth Provider
  │
  └── future prod → SSO/OIDC Provider Boundary
  │
  ▼
Membership Service
  │
  │ load active memberships and capabilities
  ▼
Auth Me Response
  │
  ▼
Vue Auth Context Store
```

处理的数据：

- Safe profile：id、displayName、email。
- Safe membership list：space id/name、role、status。
- Capabilities：`SPACE_READ`、`BATCH_CREATE`、`WIKI_PUBLISH`、`MEMBER_MANAGE` 等 action names。

失败行为：

- Missing context 对 protected APIs 返回 `401`。
- `/api/auth/me` 在 local/test mode 返回 configured mock user。

## Flow 2：Protected API Authorization

```text
Client Request
  │
  ▼
Auth Filter / Interceptor
  │
  ├─ no current user ───────────────► 401 UNAUTHORIZED envelope
  │
  ▼
Authorization Policy
  │
  ├─ no membership / no capability ─► 403 FORBIDDEN envelope
  │
  ▼
Existing Controller / Service
  │
  ▼
ApiEnvelope success or domain validation error
```

Permission examples：

| Domain | Read role | Write role |
|---|---|---|
| Space metadata | `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, `PLATFORM_ADMIN` | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Batch/file/chunk | `VIEWER`+ | `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Review/publish/Wiki operations | `VIEWER`+ 读取 | `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Graph/Ask | `VIEWER`+ 读取 reports | `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Model/storage/vector/parser settings | `SPACE_OWNER`, `PLATFORM_ADMIN` 读取 | `SPACE_OWNER`, `PLATFORM_ADMIN` |
| Membership | `SPACE_OWNER`, `PLATFORM_ADMIN` 读写 | `SPACE_OWNER`, `PLATFORM_ADMIN` |

## Flow 3：Membership Change

```text
PATCH /api/spaces/{spaceId}/members/{membershipId}
  │
  ▼
Auth Guard: require SPACE_OWNER or PLATFORM_ADMIN
  │
  ├─ denied ───────────────────────► 403
  │
  ▼
Membership Service
  │
  ├─ would remove last SPACE_OWNER ─► 409 CONFLICT
  │
  ├─ invalid role/status ───────────► 400 VALIDATION_ERROR
  │
  ▼
Persist membership update
  │
  ▼
Return safe membership response
```

State transitions：

```text
INVITED ──accept/admin activate──► ACTIVE
ACTIVE ──suspend───────────────► SUSPENDED
SUSPENDED ──reactivate─────────► ACTIVE
INVITED/ACTIVE/SUSPENDED ──────► REMOVED
REMOVED ──new invitation only──► INVITED
```

Invariant：

- 每个 space 必须始终保留至少一个 active `SPACE_OWNER`。

## Flow 4：Cross-Space Resource Protection

```text
Request resource id
  │
  ▼
Resolve caller permissions first
  │
  ├─ no access to resource's space ─► generic denied response
  │
  ▼
Load resource within permitted scope
  │
  ├─ not found in permitted scope ──► safe not found
  │
  ▼
Return resource
```

Non-disclosure rule：

- 对 caller permitted spaces 之外的资源，不得泄露 protected titles、source paths、owner emails、graph labels、Ask question text、model endpoints 或 settings state。

## Error Cascade

| Condition | Status | Code | Body constraints |
|---|---|---|---|
| Missing auth context | `401` | `UNAUTHORIZED` | 不包含 provider details 或 raw token hints |
| Authenticated but role denied | `403` | `FORBIDDEN` | 通用 action denial |
| Invalid membership mutation | `400` | `VALIDATION_ERROR` | Field-level safe validation |
| Last owner violation | `409` | `CONFLICT` | 安全 owner-invariant message |
| Resource outside membership scope | `403` 或 safe `404` | `FORBIDDEN` 或 `NOT_FOUND` | 不包含 protected resource detail |

## Refresh Strategy

- `/api/auth/me` 在 app boot 以及 active-space 或 membership-changing actions 后加载。
- Backend authorization 永远不依赖 stale frontend cache。
- E2E tests 必须通过 mock users 或 test headers 进行 role switching，而不是 hardcoded graph-only headers。

## Traceability

- REQ-AUTH-SPACE-RBAC-001、REQ-AUTH-SPACE-RBAC-004 对应 Flow 1。
- REQ-AUTH-SPACE-RBAC-007、REQ-AUTH-SPACE-RBAC-009、REQ-AUTH-SPACE-RBAC-010 对应 Flow 2。
- REQ-AUTH-SPACE-RBAC-012 对应 Flow 3。
- REQ-AUTH-SPACE-RBAC-011 对应 Flow 4。
