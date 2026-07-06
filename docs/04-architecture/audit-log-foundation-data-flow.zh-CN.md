# 数据流：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06

## Flow 1: Denied Protected Request

```text
Client request
  -> AtlasAuthInterceptor
  -> CurrentUserService resolves user or missing auth
  -> AuthorizationService evaluates requirement
  -> AuthDecision is UNAUTHENTICATED / FORBIDDEN / SAFE_NOT_FOUND
  -> AuditLogService builds safe denied event when safe scope exists
  -> AuditEventRepository persists append-only event
  -> ApiEnvelope error response returns safe code/message
```

规则：

- 如果 target space 不能安全解析，则跳过 persisted event，只保留 safe server logging。
- Event metadata 可包含 request method、normalized route pattern、reason code 与 request id。
- Event metadata 不得包含 query text、source content、raw body、tokens 或 private paths。

## Flow 2: Membership Change

```text
Space owner request
  -> RBAC permits MEMBER_MANAGE
  -> SpaceMembershipService validates role/status and last-owner invariant
  -> Domain result is SUCCEEDED or CONFLICT
  -> AuditLogService records MEMBERSHIP event
  -> API returns created/updated/deleted/conflict response
```

规则：

- Target user 只用 safe user id 与 display label 表示。
- Last-owner conflict 记录 result `CONFLICT` 与 safe reason code。

## Flow 3: Knowledge Operation

```text
Knowledge manager action
  -> Domain service executes review/publish/Wiki/graph/Ask/adapter operation
  -> Service computes safe counts and target ids
  -> AuditLogService records category-specific event
  -> Domain API returns existing response DTO
```

规则：

- Review/publish 中 affected chunk ids 是 safe references；comments 必须 sanitize 或不进入 audit summaries。
- Wiki/graph operations 中 run ids 与 counts 是 safe；generated text 不保存到 audit。
- Ask/model operations 中 prompt、answer text、provider payload 与 token secrets 不保存到 audit。

## Flow 4: Audit Query

```text
Authorized user
  -> GET /api/spaces/{spaceId}/audit-events
  -> RBAC requires GOVERNANCE_READ
  -> AuditLogService validates filters and page limits
  -> Repository queries by space/time and optional filters
  -> Controller returns ApiEnvelope<List<AuditEventResponse>> with PageMeta
```

规则：

- Viewer/editor users 默认收到 safe denial。
- Cross-space target filters 不得泄露 existence。
- Page size 有上限。

## State Model

```text
EVENT_CREATED (terminal)
```

Audit events 是 append-only。Product APIs 不暴露 update/delete transitions。

## Error Cascade

| Source | Behavior |
|---|---|
| Invalid filter | `400` validation error with safe field messages。 |
| Missing auth | 通过 auth boundary 返回 `401`。 |
| Missing governance permission | 根据 target disclosure rules 返回 `403` 或 safe `404`。 |
| Governed action 的 audit write failure | Return behavior 必须匹配 category policy 并有测试。 |
| Informational event 的 audit write failure | Original operation 可继续，但 safe operational log 记录 failure。 |

## Traceability

- Flow 1 覆盖 REQ-AUDIT-LOG-FOUNDATION-003、REQ-AUDIT-LOG-FOUNDATION-009、T-AUDIT-LOG-FOUNDATION-004 与 AC-AUDIT-LOG-FOUNDATION-002。
- Flow 2 覆盖 REQ-AUDIT-LOG-FOUNDATION-004 与 T-AUDIT-LOG-FOUNDATION-004。
- Flow 3 覆盖 REQ-AUDIT-LOG-FOUNDATION-005、REQ-AUDIT-LOG-FOUNDATION-006 与 T-AUDIT-LOG-FOUNDATION-005。
- Flow 4 覆盖 REQ-AUDIT-LOG-FOUNDATION-007、REQ-AUDIT-LOG-FOUNDATION-008、T-AUDIT-LOG-FOUNDATION-006 与 AC-AUDIT-LOG-FOUNDATION-004。
