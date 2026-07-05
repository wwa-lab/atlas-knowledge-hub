# 系统架构：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05
Source specification：`docs/03-spec/auth-space-rbac-spec.md`

## 概览

- **架构摘要：** 本切片为现有 Atlas Spring Boot metadata API 增加横切 auth/RBAC 层。它引入 current-user resolution、membership-backed permission checks、safe denied responses 和 frontend capabilities，同时把 production SSO/OIDC 保持在未来 provider boundary 后。
- **设计目标：** 为受控内部 beta 提供后端强制的 space authorization，且不产生外部 identity-provider 调用。
- **架构风格：** Layered Spring Boot control-plane，包含 auth provider boundary、centralized authorization policy、domain controllers、service/repository persistence 与 Vue permission-aware UI。

## Source Specification

- **Feature / System Name:** `auth-space-rbac`
- **Scope Summary:** 实现 current user context、local mock auth、role matrix、additive user/membership persistence、核心 APIs 的 backend guards 和 frontend permission awareness。

## 架构驱动

### 关键功能驱动

- REQ-AUTH-SPACE-RBAC-001 要求 protected requests 具备 current user context。
- REQ-AUTH-SPACE-RBAC-006 定义六角色矩阵。
- REQ-AUTH-SPACE-RBAC-007 要求 guards 覆盖现有 API domains。
- REQ-AUTH-SPACE-RBAC-012 保护 membership administration 与 owner invariants。

### 关键非功能驱动

- Denied responses 不得泄露资源存在性或敏感细节。
- 默认 local/test behavior 必须保持 offline 与 mock-safe。
- 未来 SSO/OIDC 必须保持在 provider boundary 后。
- 必须保留现有 API envelope 与 safe error style。

### 约束与假设

- 已验证当前 graph authorization 是 controller-local，位于 `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:35`。
- 已验证多数核心 controllers 当前不解析 user context，包括 `SpaceController.java:35`、`ReviewPublishController.java:37` 与 `AskController.java:29`。
- 已验证 base schema 有 `space.owner`，但没有 persisted membership table，见 `backend/src/main/resources/db/migration/V1__init_schema.sql:3`。
- [ASSUMPTION] 如果实现保留 local mock provider 和现有 API envelope semantics，引入 Spring Security 是可接受的。

## System Context

| Actor/System | Role |
|---|---|
| Vue frontend | 加载 `/api/auth/me`、渲染 permissions、调用 Atlas APIs。 |
| Atlas metadata API | 拥有 backend auth context、authorization、resource APIs 与 membership persistence。 |
| Local mock auth provider | 为 dev/test/E2E 提供确定性 mock users。 |
| Future SSO/OIDC provider | 位于 boundary 后的未来外部身份供应商；本切片不调用。 |
| PostgreSQL/Flyway | 通过 additive migrations 持久化 users 与 memberships。 |

系统边界：本切片位于 Atlas product surfaces 与 metadata API 内部。不创建外部 identity service，不调用公司 directory，不引入 production SSO secrets。

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│  Internal Users                                               │
│  Viewer · Editor · Knowledge Manager · Space Owner · Auditor  │
└─────────────────────────┬────────────────────────────────────┘
                          │ HTTPS / local dev
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Vue App                                                      │
│  /api/auth/me · capability-aware controls · safe denial states│
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│  Spring Boot Metadata API                                     │
│  Auth Filter/Resolver · Authorization Policy · Error Envelope  │
├──────────────────────────────────────────────────────────────┤
│  Space/Batch/File · Review/Wiki · Graph/Ask · Settings APIs   │
├──────────────────────────────────────────────────────────────┤
│  User + Membership Services · Existing Domain Services        │
└──────────────┬──────────────────────────────┬────────────────┘
               │ JPA/Flyway                   │ provider boundary
               ▼                              ▼
┌──────────────────────────────┐   ┌──────────────────────────┐
│  PostgreSQL atlas schema      │   │  Mock Auth / Future SSO  │
│  atlas_user · space_membership│   │  No live SSO in slice    │
└──────────────────────────────┘   └──────────────────────────┘
```

## Layer Summary

- **Presentation Layer:** Vue 获取 current context 与 capabilities，禁用或隐藏 denied controls，并仍以后端为权威。
- **API/Auth Layer:** 解析 user context，把 endpoint actions 映射到 capabilities，并通过现有 envelope 返回 `401`/`403`。
- **Domain Layer:** 授权成功后，现有 space、batch、review、wiki、graph、ask 和 settings services 继续拥有业务逻辑。
- **Persistence Layer:** Flyway-managed user 与 membership tables 提供 role data 与 active-owner invariants。
- **Provider Boundary:** local/test 使用 mock provider；未来 SSO/OIDC provider 是可替换 boundary。

## Component Breakdown

### Frontend Components

- **Auth Context Store:** 加载 `/api/auth/me`，暴露 current user、active space、roles 与 capabilities。
- **Permission-Aware Controls:** 对 upload、batch create、review、publish、graph、Ask、settings 和 members 可见动作做 gate。
- **Denied/Error States:** 展示不含敏感信息的 `401`/`403`，不暴露 protected metadata。

### Backend Services

- **Current User Resolver:** 从 mock auth 或未来 provider claims 构建 Atlas user context。
- **Authorization Policy:** 把 endpoint/action/resource 映射到 required roles 与 capabilities。
- **Membership Service:** 读取和更新 user memberships、role assignments 与 owner invariants。
- **Safe Error Handling:** 返回 envelope-compatible `401`、`403`、conflict 与 validation responses。

### Configuration / Administration Modules

- **Mock Auth Configuration:** 定义安全 test identities 与 defaults。
- **Provider Boundary Configuration:** 记录 provider type 与 configured state，不返回 raw secrets。

### Monitoring / Audit Modules

- **Auth Decision Event Shape:** 为后续 audit slice 输出安全 actor、role、space、action、target、result 与 request id 字段。

### Integration Adapters

- **Auth Provider Boundary:** 当前为 local mock；未来为 SSO/OIDC。不改变 parser/converter/model/vector/storage adapter boundaries。

## Data Architecture

| Entity | Description | Key Attributes |
|---|---|---|
| AtlasUser | 安全内部用户 profile | id, email, displayName, status, globalRoles, createdAt, updatedAt |
| SpaceMembership | 用户在 Knowledge Space 中的 membership | id, userId, spaceId, role, status, invitedBy, createdAt, updatedAt |
| AuthDecision | 安全 transient event shape | requestId, actor, role, spaceId, action, targetType, targetId, result |

State models：

- User status：`ACTIVE`、`DISABLED`。
- Membership status：`INVITED`、`ACTIVE`、`SUSPENDED`、`REMOVED`。
- Denied result：`UNAUTHENTICATED`、`FORBIDDEN`、`SAFE_NOT_FOUND`、`CONFLICT`。

## Integration Architecture

### Local Mock Auth Provider

- **Interaction Pattern:** 使用 safe headers/test defaults 的 in-process resolver。
- **Triggered by:** 每个 protected request。
- **Data exchanged:** Mock user id 与 active space preference；无 secrets。

### Future SSO/OIDC Provider

- **Interaction Pattern:** 本切片仅定义 boundary。
- **Triggered by:** 未来 production auth configuration。
- **Data exchanged:** 本切片不交换。

## Workflow / Runtime Architecture

Request flow：

1. 请求进入 backend auth layer。
2. Current user resolver 完成认证或返回 `401`。
3. Authorization policy 解析 action 与 required capability。
4. Membership service 检查 target space 的 active role 或 global platform role。
5. 允许的请求进入现有 controller/service logic。
6. 拒绝的请求返回 safe `403` 或指定场景下的 safe not-found。

## Security Architecture

- Protected APIs 必须由后端授权。
- Frontend capabilities 只是 UX hints。
- Cross-space denied responses 避免 resource metadata disclosure。
- 禁止 provider payloads、raw tokens、passwords、private paths 和真实公司身份。
- Member write operations 必须保护最后一个 active `SPACE_OWNER`。

## Risks / Tradeoffs

| Risk | Impact | Mitigation |
|---|---|---|
| 引入 central guards 可能暴露缺少测试的 endpoints。 | High | 按 domain 和 role 增加代表性 integration tests。 |
| Spring Security setup 可能影响 local dev。 | Medium | 提供 mock provider defaults 并记录 test headers。 |
| 现有 graph-local guard duplication 可能漂移。 | Medium | 用 common authorization policy 替换或包裹它。 |

## Open Questions

- OQ-AUTH-SPACE-RBAC-001：`AUDITOR` 未来应跨空间读取 audit logs，还是只读 space-scoped governance metadata？
- OQ-AUTH-SPACE-RBAC-002：production SSO 应自动映射 groups 到 roles，还是要求显式 Atlas memberships？
