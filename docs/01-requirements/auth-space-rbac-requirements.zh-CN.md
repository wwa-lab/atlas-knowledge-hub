# 需求：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05
切片：`auth-space-rbac`
Wave：Wave 3 / Trust And Governance

## 目的

Atlas 在进入受控内部 beta 前，需要后端强制执行的认证与空间级 RBAC 底座。本切片把当前 internal/mock-safe API 姿态收敛为统一的 auth context、role matrix、membership model 和 authorization guard 方案，但不实现外部公司 SSO 调用或生产账号管理。

## 已核实现状

| 证据 | 已验证现状 |
|---|---|
| `backend/pom.xml:37` | 后端使用 Spring Web、JPA、validation、Flyway、PostgreSQL 和测试依赖；当前依赖列表未包含 Spring Security。 |
| `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:35` | Graph 已有基于本地 header 的 `VIEWER`、`REVIEWER`、`ADMIN` 角色 guard。 |
| `backend/src/main/java/com/atlas/metadata/controller/SpaceController.java:35` | Space list/detail/create 端点当前不需要 authenticated user context。 |
| `backend/src/main/java/com/atlas/metadata/controller/ReviewPublishController.java:37` | Review queue 与 Wiki 端点当前不需要 authenticated user context。 |
| `backend/src/main/java/com/atlas/metadata/controller/AskController.java:29` | Ask run 创建当前不需要 authenticated user context。 |
| `backend/src/main/java/com/atlas/metadata/domain/Space.java:37` | `Space` 只有 `owner` 字符串，没有持久化 membership model。 |
| `backend/src/main/resources/db/migration/V1__init_schema.sql:3` | 基础 metadata schema 包含 `space`、`batch`、`file_item`、`source_chunk`、`review_record`、`wiki_page`、`graph_node`、`graph_edge`；V1 中没有 user 或 membership 表。 |
| `frontend/src/api.ts:37` | 前端当前只为 graph read/admin 调用注入 demo headers。 |

## 需求

| ID | 优先级 | 需求 |
|---|---|---|
| REQ-AUTH-SPACE-RBAC-001 | Must | 后端必须为每个受保护 API 请求解析当前 Atlas user context。 |
| REQ-AUTH-SPACE-RBAC-002 | Must | Auth 层必须支持用于开发、测试和 E2E 的本地 mock auth provider，且不产生外部网络调用。 |
| REQ-AUTH-SPACE-RBAC-003 | Must | Auth 层必须提供公司 SSO/OIDC adapter boundary，便于未来生产集成，controller 不得绑定某个身份供应商。 |
| REQ-AUTH-SPACE-RBAC-004 | Must | 当前 user context 必须包含 user id、display name、email、global roles、space memberships、active space 和 effective space roles。 |
| REQ-AUTH-SPACE-RBAC-005 | Must | Atlas 必须通过 additive Flyway schema 持久化 users 和 space memberships。 |
| REQ-AUTH-SPACE-RBAC-006 | Must | Role matrix 必须包含 `VIEWER`、`EDITOR`、`KNOWLEDGE_MANAGER`、`SPACE_OWNER`、`AUDITOR` 和 `PLATFORM_ADMIN`。 |
| REQ-AUTH-SPACE-RBAC-007 | Must | 后端 RBAC guards 必须覆盖 space、batch、file、chunk、review/publish、Wiki、graph、Ask 以及 model/storage/vector/parser settings APIs。 |
| REQ-AUTH-SPACE-RBAC-008 | Must | 前端可以做 permission-aware 的隐藏或禁用控件，但不得被视为安全边界。 |
| REQ-AUTH-SPACE-RBAC-009 | Must | 未认证的受保护 API 请求必须通过 Atlas API envelope 返回 `401`。 |
| REQ-AUTH-SPACE-RBAC-010 | Must | 已认证但无权限的受保护 API 请求必须通过 Atlas API envelope 返回 `403`，且不泄露敏感细节。 |
| REQ-AUTH-SPACE-RBAC-011 | Must | 跨空间资源访问不得泄露调用者无权限空间中的资源是否存在。 |
| REQ-AUTH-SPACE-RBAC-012 | Must | Space membership 写操作必须限制为 `SPACE_OWNER` 或 `PLATFORM_ADMIN`，并保护最后一个 active space owner。 |
| REQ-AUTH-SPACE-RBAC-013 | Should | 本切片可只产生最小审计事件，但 auth 与 RBAC 决策必须携带 actor、space、action、target 和 result 字段，以便后续 `audit-log-foundation` 使用。 |
| REQ-AUTH-SPACE-RBAC-014 | Must | 本切片只能使用 mock/sample identities，不得提交真实公司用户、域名、token、password、private path 或 provider log。 |

## 验收标准

| ID | Requirement | 标准 |
|---|---|---|
| AC-AUTH-SPACE-RBAC-001 | REQ-AUTH-SPACE-RBAC-001 | 受保护端点在没有 current user context 时返回 `401`，响应为 `success=false` 且 error body 对用户安全。 |
| AC-AUTH-SPACE-RBAC-002 | REQ-AUTH-SPACE-RBAC-007 | `VIEWER` 调用写 API，包括 batch create、review action、publish、graph projection、Ask create 和 settings write 时收到 `403`。 |
| AC-AUTH-SPACE-RBAC-003 | REQ-AUTH-SPACE-RBAC-011 | 用户请求自己没有 membership 的空间资源时，收到通用拒绝响应，且不披露资源 metadata。 |
| AC-AUTH-SPACE-RBAC-004 | REQ-AUTH-SPACE-RBAC-006 | 测试至少覆盖普通用户、`KNOWLEDGE_MANAGER` 和 `SPACE_OWNER` 的关键权限差异。 |
| AC-AUTH-SPACE-RBAC-005 | REQ-AUTH-SPACE-RBAC-008 | 前端 role-aware UI state 来自 `/api/auth/me`，不再硬编码 graph-only demo headers。 |
| AC-AUTH-SPACE-RBAC-006 | REQ-AUTH-SPACE-RBAC-014 | secret/private-path 扫描与 dependency/network 扫描证明没有新增 raw credential、private path、真实身份或外部云调用。 |

## 范围

范围内：

- Auth context abstraction 与本地 mock auth provider。
- Additive user 和 space membership 数据模型。
- Role matrix 与 permission policy。
- 现有 API domains 的后端 guard 集成。
- `/api/auth/me` 与 membership management API contracts。
- 使用 API capabilities 的前端 permission-aware state。
- 覆盖 `VIEWER`、`KNOWLEDGE_MANAGER`、`SPACE_OWNER` 的测试与 E2E。

范围外：

- 生产 SSO/OIDC provider 配置和真实身份供应商调用。
- Password registration/login、account recovery、MFA、SCIM/directory sync。
- 超出最小 event shape 的完整 audit retention 与 audit UI。
- Secret manager integration、rate limiting、deployment hardening 或 production operations。
- 真实公司身份、凭证、域名或日志。

## 约束

- 必须以后端 enforcement 为准；UI-only authorization 不可接受。
- 所有 schema change 必须通过 Flyway migration。
- Auth provider 与未来 SSO/OIDC integration 必须置于 adapter-style boundaries 后。
- 现有 parser/converter/model/vector/storage adapter boundaries 不得被破坏。
- 默认开发和 CI 路径必须保持 mock/sample-safe 与离线。
