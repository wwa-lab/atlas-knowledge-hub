# 详细设计：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05

## 概览

本设计把 REQ-AUTH-SPACE-RBAC-001 到 REQ-AUTH-SPACE-RBAC-014 转化为可实现的 backend、frontend、API、data 与 verification 决策。它引入 backend enforcement，同时保持 local/test mock auth，且不进行 live SSO/OIDC 调用。

## Source Architecture

来源：`docs/04-architecture/auth-space-rbac-architecture.md`

Grounding anchors：

- 现有 graph guard 在 `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:54` 读取 `X-Atlas-User` 和 `X-Atlas-Role`。
- 现有 API envelope 是 `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`。
- 现有 safe error body 是 `backend/src/main/java/com/atlas/metadata/dto/ErrorBody.java:6`。
- 现有前端 graph demo headers 位于 `frontend/src/api.ts:37`。

## 设计假设

- [ASSUMPTION] 如果测试保留 Atlas envelope 与 local mock profile，可引入 Spring Security 作为 request authentication/authorization substrate。
- [ASSUMPTION] 现有 seeded `space.owner` 足以在 migration/seed 中创建初始 mock `SPACE_OWNER` memberships。
- [ASSUMPTION] 实现可以只把 headers 用于 mock/test auth；production SSO 不在范围内。

## 设计范围

范围内：

- Auth context resolver 与 provider boundary。
- Role enum、capability enum、authorization policy。
- User/membership persistence 与 service logic。
- 现有 API domains 的 guard integration。
- `/api/auth/me` 与 membership APIs。
- Vue auth state 与 permission-aware controls。
- Integration/E2E role coverage。

范围外：

- 真实 SSO/OIDC provider 调用。
- Password auth、account recovery、MFA、SCIM、directory sync。
- Secret manager、rate limiting、production audit UI。
- 大范围 UI redesign。

## Module Design

### Auth Provider Module

职责：

- 为 protected requests 解析 current user。
- 在 local/test 中支持 `mock` provider。
- 为未来 `oidc` 暴露稳定 provider interface。
- Protected APIs 没有 context 时 fail closed。

输入：

- Mock headers 或 local/test default user id。
- 未来 provider claims 只通过 boundary 进入。

输出：

- Current Atlas user id。
- 可选 active space hint。

### Current User Context Module

职责：

- 加载 `AtlasUser`。
- 加载 active memberships。
- 派生 global roles、active space、effective space roles 与 capabilities。
- 提供 `/api/auth/me` response DTO。

### Authorization Policy Module

职责：

- 将 API domain/action/resource scope 映射到 required capabilities。
- 使用 membership status 与 role 做 allow/deny。
- 将 denied results 转为 safe `401`、`403` 或 safe not-found behavior。
- 替换或包裹 graph-local role checks。

Permission decisions：

- `VIEWER`：read-only。
- `EDITOR`：content ingestion/update；不可 review/publish/settings/member writes。
- `KNOWLEDGE_MANAGER`：knowledge operations，包括 review、publish、graph、vector、Ask、Wiki maintenance。
- `SPACE_OWNER`：所有 space-scoped operations，包括 membership 与 settings。
- `AUDITOR`：只读 governance metadata。
- `PLATFORM_ADMIN`：platform-wide admin。

### Membership Module

职责：

- 列出 space members。
- 创建 invitation/membership records。
- 更新 role/status。
- 使用 `REMOVED` status 移除 membership。
- 保护最后一个 active `SPACE_OWNER`。

### Frontend Auth Module

职责：

- App startup 时加载 `/api/auth/me`。
- 保存 current user、memberships、active space 和 capabilities。
- 对 denied actions 渲染 disabled/hidden controls。
- 将 graph-only hardcoded role headers 从 primary product authorization 中移除。

## API / Interface Design

权威细节见 `docs/05-design/contracts/auth-space-rbac-API_IMPLEMENTATION_GUIDE.md`。

Endpoint summary：

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/auth/me` | 返回 current user context 与 capabilities。 |
| GET | `/api/spaces/{spaceId}/members` | 列出 space 中的 safe members。 |
| POST | `/api/spaces/{spaceId}/members` | 创建 mock invitation/membership。 |
| PATCH | `/api/spaces/{spaceId}/members/{membershipId}` | 更新 role/status。 |
| DELETE | `/api/spaces/{spaceId}/members/{membershipId}` | 标记 membership removed。 |

Protected existing APIs：

- 所有 space、batch、file、chunk、review/publish、Wiki、graph、Ask、model、storage、vector、parser、conversion、ingestion 与 downstream-refresh APIs 都必须通过 authorization。

## Data Design

数据模型来源：`docs/04-architecture/auth-space-rbac-data-model.md`。

Persistent tables：

- `atlas.atlas_user`
- `atlas.space_membership`

Enum decisions：

- User status：`ACTIVE`、`DISABLED`。
- Membership status：`INVITED`、`ACTIVE`、`SUSPENDED`、`REMOVED`。
- Roles：`VIEWER`、`EDITOR`、`KNOWLEDGE_MANAGER`、`SPACE_OWNER`、`AUDITOR`、`PLATFORM_ADMIN`。

## UI / User Flow Design

1. 用户打开 Atlas。
2. Vue 加载 `/api/auth/me`。
3. 已认证时，shell 渲染 user identity 与 current-space capabilities。
4. 不允许的写动作被禁用或隐藏。
5. 如果 backend call 返回 `401` 或 `403`，UI 展示安全消息，不显示 protected resource metadata。
6. Space Owner 在 member management 中看到允许的 invite/update/remove controls。

## Workflow / Execution Design

Protected write example：

1. 用户点击 publish。
2. Frontend 为 UX 检查 `WIKI_PUBLISH` capability。
3. Backend 收到 publish request。
4. Auth layer 解析 current user。
5. Authorization policy 要求 `KNOWLEDGE_MANAGER`、`SPACE_OWNER` 或 `PLATFORM_ADMIN`。
6. 允许时，现有 publish service 运行。
7. 拒绝时，响应为 `403`，且除 safe error fields 外不包含 file title、source path 或 reviewer detail。

## Security / Audit / Reliability Design

- Backend 是 permissions 的唯一真相源。
- Mock auth data 必须 sample-safe。
- 所有 denied responses 使用现有 `ApiEnvelope` 与 `ErrorBody`。
- Auth decision event shape 为后续 audit persistence 生成；完整 audit retention 是后续切片。
- Membership changes 校验 last-owner invariant。
- 未来 provider failures fail closed。

## Validation and Error Handling

| Case | Response |
|---|---|
| Missing auth on protected API | `401 UNAUTHORIZED` envelope |
| Authenticated but missing capability | `403 FORBIDDEN` envelope |
| Invalid role/status | `400 VALIDATION_ERROR` |
| Last active owner removal | `409 CONFLICT` |
| Cross-space resource id | Generic `403` 或 safe `404`，不包含 protected details |

## Testing Considerations

- Unit tests：role matrix、capability mapping、membership invariant。
- Integration tests：`VIEWER`、`KNOWLEDGE_MANAGER`、`SPACE_OWNER` 的 representative endpoints。
- API contract tests：`/api/auth/me`、membership APIs、`401`、`403`、`409`。
- Frontend tests：auth store 与 permission-aware controls。
- E2E tests：role-specific path differences。
- Safety scans：无真实 identities、raw secrets、private paths 或 external cloud calls。

## Risks / Design Tradeoffs

| Risk | Tradeoff / mitigation |
|---|---|
| Spring Security 可能改变 envelope handling。 | 增加 exception/entrypoint handling，使 `401`/`403` 保持 Atlas envelope style。 |
| Broad guard coverage 会触碰许多 controllers。 | 使用 central policy 与 representative endpoint tests，而不是每个 controller 定制 guard。 |
| Production SSO 保持范围外。 | 保持 boundary 明确，并让 mock provider default 可预测。 |

## Open Questions

- OQ-AUTH-SPACE-RBAC-001：未来 `AUDITOR` audit-log visibility boundary。
- OQ-AUTH-SPACE-RBAC-002：未来 SSO group-to-role mapping policy。
