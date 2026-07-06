# Data Flow: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06

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

Rules:

- If the target space cannot be resolved safely, skip persisted event and use safe server logging only.
- Event metadata may include request method, normalized route pattern, reason code, and request id.
- Event metadata must not include query text, source content, raw body, tokens, or private paths.

## Flow 2: Membership Change

```text
Space owner request
  -> RBAC permits MEMBER_MANAGE
  -> SpaceMembershipService validates role/status and last-owner invariant
  -> Domain result is SUCCEEDED or CONFLICT
  -> AuditLogService records MEMBERSHIP event
  -> API returns created/updated/deleted/conflict response
```

Rules:

- Target user is represented by safe user id and display label only.
- Last-owner conflict records result `CONFLICT` and a safe reason code.

## Flow 3: Knowledge Operation

```text
Knowledge manager action
  -> Domain service executes review/publish/Wiki/graph/Ask/adapter operation
  -> Service computes safe counts and target ids
  -> AuditLogService records category-specific event
  -> Domain API returns existing response DTO
```

Rules:

- For review/publish, affected chunk ids are safe references; comments must be sanitized or omitted from audit summaries.
- For Wiki and graph operations, run ids and counts are safe; generated text is not stored in audit.
- For Ask/model operations, prompt, answer text, provider payload, and token secrets are not stored in audit.

## Flow 4: Audit Query

```text
Authorized user
  -> GET /api/spaces/{spaceId}/audit-events
  -> RBAC requires GOVERNANCE_READ
  -> AuditLogService validates filters and page limits
  -> Repository queries by space/time and optional filters
  -> Controller returns ApiEnvelope<List<AuditEventResponse>> with PageMeta
```

Rules:

- Viewer/editor users receive safe denial by default.
- Cross-space target filters cannot leak existence.
- Page size is bounded.

## State Model

```text
EVENT_CREATED (terminal)
```

Audit events are append-only. Product APIs do not expose update/delete transitions.

## Error Cascade

| Source | Behavior |
|---|---|
| Invalid filter | `400` validation error with safe field messages. |
| Missing auth | `401` through auth boundary. |
| Missing governance permission | `403` or safe `404` according to target disclosure rules. |
| Audit write failure on governed action | Return behavior must match category policy and be tested. |
| Audit write failure on informational event | Original operation may continue, but safe operational log records failure. |

## Traceability

- Flow 1 covers REQ-AUDIT-LOG-FOUNDATION-003, REQ-AUDIT-LOG-FOUNDATION-009, T-AUDIT-LOG-FOUNDATION-004, and AC-AUDIT-LOG-FOUNDATION-002.
- Flow 2 covers REQ-AUDIT-LOG-FOUNDATION-004 and T-AUDIT-LOG-FOUNDATION-004.
- Flow 3 covers REQ-AUDIT-LOG-FOUNDATION-005, REQ-AUDIT-LOG-FOUNDATION-006, and T-AUDIT-LOG-FOUNDATION-005.
- Flow 4 covers REQ-AUDIT-LOG-FOUNDATION-007, REQ-AUDIT-LOG-FOUNDATION-008, T-AUDIT-LOG-FOUNDATION-006, and AC-AUDIT-LOG-FOUNDATION-004.
