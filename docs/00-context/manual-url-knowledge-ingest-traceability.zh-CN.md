# 溯源：manual-url-knowledge-ingest

状态：已在预授权 SDD 边界内完成实现并通过 verification。
最后更新：2026-07-07
成熟度：Metadata-only manual URL ingest foundation；不是 connector-sync-v0 或 production crawl readiness。

## Source Documents

- User goal：`manual-url-knowledge-ingest` autonomous single-slice full-delivery prompt。
- Execution manifest：`docs/00-context/execution-manifests/manual-url-knowledge-ingest-20260707.yaml`。
- SDD profile：`docs/00-context/sdd-profile.md`。
- Workflow docs：`docs/00-context/agent-goal-loop-workflow.md`、`docs/00-context/agent-goal-loop-workflow.zh-CN.md`、`docs/00-context/agent-goal-loop-quickstart.md`、`docs/00-context/agent-goal-loop-quickstart.zh-CN.md`。
- Existing anchors：batch/file/source chunk metadata、Wiki ingest、review/publish、safe errors、rate limiting、frontend Knowledge Space 与 Processing Center surfaces。

## Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/manual-url-knowledge-ingest-requirements.md` | `docs/01-requirements/manual-url-knowledge-ingest-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/manual-url-knowledge-ingest-stories.md` | `docs/02-user-stories/manual-url-knowledge-ingest-stories.zh-CN.md` |
| Specification | `docs/03-spec/manual-url-knowledge-ingest-spec.md` | `docs/03-spec/manual-url-knowledge-ingest-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/manual-url-knowledge-ingest-architecture.md` | `docs/04-architecture/manual-url-knowledge-ingest-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/manual-url-knowledge-ingest-data-flow.md` | `docs/04-architecture/manual-url-knowledge-ingest-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/manual-url-knowledge-ingest-data-model.md` | `docs/04-architecture/manual-url-knowledge-ingest-data-model.zh-CN.md` |
| Design | `docs/05-design/manual-url-knowledge-ingest-design.md` | `docs/05-design/manual-url-knowledge-ingest-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/manual-url-knowledge-ingest-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/manual-url-knowledge-ingest-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/manual-url-knowledge-ingest-tasks.md` | `docs/06-tasks/manual-url-knowledge-ingest-tasks.zh-CN.md` |

## Requirement To Task Map

| Requirement | Stories | Acceptance | Tasks |
|---|---|---|---|
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001`, `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` | `T-MANUAL-URL-KNOWLEDGE-INGEST-004`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` | `T-MANUAL-URL-KNOWLEDGE-INGEST-002`, `T-MANUAL-URL-KNOWLEDGE-INGEST-004` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003`, `T-MANUAL-URL-KNOWLEDGE-INGEST-005` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` | `US-MANUAL-URL-KNOWLEDGE-INGEST-002` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `T-MANUAL-URL-KNOWLEDGE-INGEST-002`, `T-MANUAL-URL-KNOWLEDGE-INGEST-003` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | `US-MANUAL-URL-KNOWLEDGE-INGEST-002` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003`, `T-MANUAL-URL-KNOWLEDGE-INGEST-006` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` | `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-005`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` | `US-MANUAL-URL-KNOWLEDGE-INGEST-004` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` | `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` | `T-MANUAL-URL-KNOWLEDGE-INGEST-006`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` | `US-MANUAL-URL-KNOWLEDGE-INGEST-004` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003` through `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-011` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-012` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |

## SDD Skill Chain Evidence

SDD skill chain used: yes

Skill files read:

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`
- `.agents/skills/architecture-review/SKILL.md`
- `.agents/skills/_shared/grounding-rules.md`
- `.agents/skills/review-doc-quality/references/completeness-criteria.md`
- `.agents/skills/review-doc-quality/references/phase-scope-guide.md`

Review-doc-quality result：在显式预授权边界内可进入实现；无 critical 或 major findings。

## Implementation Evidence

- 后端新增 metadata-only manual URL source registration APIs，覆盖 create、list 与 get。
- 后端将安全 URL metadata 存入 `manual_url_source`，并连接现有 batch/file/source chunk metadata；file 与 review state 保持 `REVIEW_REQUIRED`。
- 后端 validation 拒绝 non-HTTPS URL、userinfo、query string、fragment、local/private/internal host，以及 title/description/path 中的 secret-like 值，同时不回显 raw unsafe inputs。
- 前端在 Knowledge Space metadata surface 新增 API-backed manual URL ingest form、status/source-trace 展示，并在 Processing Center 展示 review-required manual URL count。
- 前端与 E2E mock 仅使用 `https://example.com/...` sample-safe fixtures。
- 未引入真实 URL fetch、crawl、connector sync、browser automation、external provider call、auth/RBAC/audit/secret/rate-limit semantic change 或 production compliance behavior。

## Completed Task IDs

`T-MANUAL-URL-KNOWLEDGE-INGEST-001` through `T-MANUAL-URL-KNOWLEDGE-INGEST-008`。

## Verification Evidence

- `npm run agent:check-sdd -- --slice manual-url-knowledge-ingest --require-api-guide --report docs/00-context/manual-url-knowledge-ingest-sdd-completion-report.md`
- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `cd frontend && npx playwright test tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`
- `npm run agent:closeout`
- `git diff --check`
- Focused secret/private-path/real-data scan。

## Residual Risks

- Real URL fetch、crawl、connector sync、compliance policy 与 connector secret management 延后到未来 slices。
- 本切片 URL-derived metadata 保持 review-required，不是 approved knowledge。
