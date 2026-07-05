# 数据模型：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05

## 概览

本切片为 REQ-AUTH-SPACE-RBAC-001 到 REQ-AUTH-SPACE-RBAC-014 增加 additive user 与 membership model。现有 `atlas.space` 仍是 space root；membership 成为 authorization truth source。

## Entity Relationship Diagram

```text
┌──────────────────────┐        1:N        ┌──────────────────────────┐
│ atlas.atlas_user      │──────────────────►│ atlas.space_membership   │
│ id PK                 │                   │ id PK                    │
│ email                 │                   │ user_id FK               │
│ display_name          │                   │ space_id FK              │
│ status                │                   │ role                     │
│ global_roles          │                   │ status                   │
└──────────────────────┘                   └────────────┬─────────────┘
                                                         │ N:1
                                                         ▼
                                             ┌──────────────────────────┐
                                             │ atlas.space              │
                                             │ id PK                    │
                                             │ owner                    │
                                             │ status                   │
                                             └──────────────────────────┘
```

## Entity Definitions

### AtlasUser

- **Table name:** `atlas.atlas_user`
- **Purpose:** 用于 Atlas authorization 的安全内部用户 profile。

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | 稳定 Atlas user id。 |
| email | String | No | Mock/sample-safe email；唯一。 |
| display_name | String | No | 面向用户的显示名。 |
| status | String | No | `ACTIVE` 或 `DISABLED`。 |
| global_roles | String array | No | 如 `PLATFORM_ADMIN` 的 global roles；无角色时为空数组。 |
| auth_subject | String | Yes | 未来 provider subject reference；本切片只允许 mock-safe。 |
| created_at | Timestamp | No | 创建时间。 |
| updated_at | Timestamp | No | 最近更新时间。 |

Constraints：

- email 唯一。
- `status` in `ACTIVE`, `DISABLED`。
- 不保存 raw tokens、passwords、provider payloads 或真实公司数据。

### SpaceMembership

- **Table name:** `atlas.space_membership`
- **Purpose:** Space-scoped role assignment 与 membership state。

| Column | Type | Nullable | Description |
|---|---|---|---|
| id | String | No | 稳定 membership id。 |
| space_id | String | No | FK 到 `atlas.space.id`。 |
| user_id | String | No | FK 到 `atlas.atlas_user.id`。 |
| role | String | No | role matrix 中的一个角色。 |
| status | String | No | `INVITED`、`ACTIVE`、`SUSPENDED` 或 `REMOVED`。 |
| invited_by_user_id | String | Yes | 创建 membership/invitation 的 user id。 |
| created_at | Timestamp | No | 创建时间。 |
| updated_at | Timestamp | No | 最近更新时间。 |

Constraints：

- 每个 `space_id` 和 `user_id` 只能有一个 active-style membership。
- `role` in `VIEWER`, `EDITOR`, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`, `AUDITOR`, `PLATFORM_ADMIN`。
- `status` in `INVITED`, `ACTIVE`, `SUSPENDED`, `REMOVED`。
- Service-level invariant：每个 space 至少保留一个 active `SPACE_OWNER`。

### AuthDecision

- **Persistence:** 本切片为 transient event shape；完整持久化属于 `audit-log-foundation`。
- **Purpose:** 保留未来添加 audit persistence 所需的最小字段。

| Field | Type | Description |
|---|---|---|
| requestId | String | 安全 correlation id。 |
| actorUserId | String | Current user id 或 `anonymous`。 |
| role | String | 决策使用的 effective role。 |
| spaceId | String | 可用时为 target space id。 |
| action | String | Capability/action name。 |
| targetType | String | Target resource type。 |
| targetId | String | 可用时为 safe target id。 |
| result | String | `ALLOWED`, `UNAUTHENTICATED`, `FORBIDDEN`, `SAFE_NOT_FOUND`, `CONFLICT`。 |

## State Models

### User Status

```text
ACTIVE ──admin disable──► DISABLED
DISABLED ──admin enable──► ACTIVE
```

### Membership Status

```text
INVITED ──accept/admin activate──► ACTIVE
ACTIVE ──suspend───────────────► SUSPENDED
SUSPENDED ──reactivate─────────► ACTIVE
INVITED/ACTIVE/SUSPENDED ──────► REMOVED
REMOVED ──new invitation only──► INVITED
```

### Role Matrix

| Capability | VIEWER | EDITOR | KNOWLEDGE_MANAGER | SPACE_OWNER | AUDITOR | PLATFORM_ADMIN |
|---|---|---|---|---|---|---|
| Read space content | Yes | Yes | Yes | Yes | Yes | Yes |
| Create/update batch, file, parser/conversion/storage runs | No | Yes | Yes | Yes | No | Yes |
| Review, publish, Wiki ingest/linkify/lint | No | No | Yes | Yes | No | Yes |
| Graph projection/review and Ask create | No | No | Yes | Yes | No | Yes |
| Read governance metadata | No | No | Yes | Yes | Yes | Yes |
| Manage space membership | No | No | No | Yes | No | Yes |
| Manage provider/settings configuration | No | No | No | Yes | No | Yes |

## Configuration Entities

| Config | Type | Description |
|---|---|---|
| auth.provider | enum | 本切片为 `mock`；未来可为 `oidc`。 |
| auth.mock.default-user | string | local/test mode 使用的安全 mock user id。 |
| auth.mock.require-header | boolean | local/test requests 是否必须提供 mock user header。 |

## Field Mapping

| Source | Target |
|---|---|
| Mock auth header/default | `AtlasUser.id` lookup |
| `/api/auth/me` | Safe user profile + memberships + capabilities |
| Space member API request | `SpaceMembership.role` and `SpaceMembership.status` |
| Existing `space.owner` | 初始 `SPACE_OWNER` membership 的 migration seed hint |

## Traceability

- REQ-AUTH-SPACE-RBAC-004 对应 `AtlasUser` 与 `/api/auth/me` fields。
- REQ-AUTH-SPACE-RBAC-005 对应 `AtlasUser` 与 `SpaceMembership`。
- REQ-AUTH-SPACE-RBAC-006 对应 role enum 与 role matrix。
- REQ-AUTH-SPACE-RBAC-012 对应 last-owner invariant。
- REQ-AUTH-SPACE-RBAC-013 对应 `AuthDecision`。
