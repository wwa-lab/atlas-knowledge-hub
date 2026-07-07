# Traceability：worker-retry-dead-letter

状态：已实现并通过验证
最后更新：2026-07-07
Manifest: `docs/00-context/execution-manifests/worker-retry-dead-letter-20260707.yaml`
成熟度目标：本地确定性 retry/dead-letter foundation；不是 production queue 或 worker operations readiness。

## Source Context

- User goal：交付 Wave 5 / Connector And Operations 的 `worker-retry-dead-letter` full single-slice delivery。
- Required repo context：`AGENTS.md`、`README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、SDD bootstrap docs、SDD profile、goal-loop docs、roadmap docs、closeout checklist。
- Related slices referenced：folder upload、batch processing、connector sync v0、rate-limit-safe-errors、secret-manager-integration、review publish、markdown/source trace、adapter boundary docs。
- 缺失 required context 记录：goal 开始时 `docs/00-context/repo-status-roadmap.md` 不存在；中文 canonical overview 位于 `docs/00-context/repo-status-roadmap.zh-CN.md`。

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/worker-retry-dead-letter-requirements.md` | `docs/01-requirements/worker-retry-dead-letter-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/worker-retry-dead-letter-stories.md` | `docs/02-user-stories/worker-retry-dead-letter-stories.zh-CN.md` |
| Specification | `docs/03-spec/worker-retry-dead-letter-spec.md` | `docs/03-spec/worker-retry-dead-letter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/worker-retry-dead-letter-architecture.md` | `docs/04-architecture/worker-retry-dead-letter-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/worker-retry-dead-letter-data-flow.md` | `docs/04-architecture/worker-retry-dead-letter-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/worker-retry-dead-letter-data-model.md` | `docs/04-architecture/worker-retry-dead-letter-data-model.zh-CN.md` |
| Design | `docs/05-design/worker-retry-dead-letter-design.md` | `docs/05-design/worker-retry-dead-letter-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/worker-retry-dead-letter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/worker-retry-dead-letter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/worker-retry-dead-letter-tasks.md` | `docs/06-tasks/worker-retry-dead-letter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/worker-retry-dead-letter-traceability.md` | `docs/00-context/worker-retry-dead-letter-traceability.zh-CN.md` |

## Requirement Mapping

| Requirement | Stories | Spec | Tasks |
|---|---|---|---|
| REQ-WORKER-RETRY-DEAD-LETTER-001 | US-WORKER-RETRY-DEAD-LETTER-002 | FR-WORKER-RETRY-DEAD-LETTER-001 | T-WORKER-RETRY-DEAD-LETTER-002 |
| REQ-WORKER-RETRY-DEAD-LETTER-002 | US-WORKER-RETRY-DEAD-LETTER-002, US-WORKER-RETRY-DEAD-LETTER-003 | FR-WORKER-RETRY-DEAD-LETTER-002 | T-WORKER-RETRY-DEAD-LETTER-002 |
| REQ-WORKER-RETRY-DEAD-LETTER-003 | US-WORKER-RETRY-DEAD-LETTER-002 | FR-WORKER-RETRY-DEAD-LETTER-003 | T-WORKER-RETRY-DEAD-LETTER-003 |
| REQ-WORKER-RETRY-DEAD-LETTER-004 | US-WORKER-RETRY-DEAD-LETTER-002 | FR-WORKER-RETRY-DEAD-LETTER-004 | T-WORKER-RETRY-DEAD-LETTER-003 |
| REQ-WORKER-RETRY-DEAD-LETTER-005 | US-WORKER-RETRY-DEAD-LETTER-002 | FR-WORKER-RETRY-DEAD-LETTER-005 | T-WORKER-RETRY-DEAD-LETTER-003, T-WORKER-RETRY-DEAD-LETTER-004 |
| REQ-WORKER-RETRY-DEAD-LETTER-006 | US-WORKER-RETRY-DEAD-LETTER-001 | FR-WORKER-RETRY-DEAD-LETTER-006 | T-WORKER-RETRY-DEAD-LETTER-002, T-WORKER-RETRY-DEAD-LETTER-005 |
| REQ-WORKER-RETRY-DEAD-LETTER-007 | US-WORKER-RETRY-DEAD-LETTER-001 | FR-WORKER-RETRY-DEAD-LETTER-007 | T-WORKER-RETRY-DEAD-LETTER-004, T-WORKER-RETRY-DEAD-LETTER-005 |
| REQ-WORKER-RETRY-DEAD-LETTER-008 | US-WORKER-RETRY-DEAD-LETTER-003 | FR-WORKER-RETRY-DEAD-LETTER-008 | T-WORKER-RETRY-DEAD-LETTER-004 |
| REQ-WORKER-RETRY-DEAD-LETTER-009 | US-WORKER-RETRY-DEAD-LETTER-001 | FR-WORKER-RETRY-DEAD-LETTER-009 | T-WORKER-RETRY-DEAD-LETTER-005 |
| REQ-WORKER-RETRY-DEAD-LETTER-010 | US-WORKER-RETRY-DEAD-LETTER-004 | FR-WORKER-RETRY-DEAD-LETTER-010 | T-WORKER-RETRY-DEAD-LETTER-005 |
| REQ-WORKER-RETRY-DEAD-LETTER-011 | US-WORKER-RETRY-DEAD-LETTER-001, US-WORKER-RETRY-DEAD-LETTER-004 | FR-WORKER-RETRY-DEAD-LETTER-011 | T-WORKER-RETRY-DEAD-LETTER-007, T-WORKER-RETRY-DEAD-LETTER-008 |
| REQ-WORKER-RETRY-DEAD-LETTER-012 | US-WORKER-RETRY-DEAD-LETTER-002, US-WORKER-RETRY-DEAD-LETTER-004 | FR-WORKER-RETRY-DEAD-LETTER-012 | T-WORKER-RETRY-DEAD-LETTER-006, T-WORKER-RETRY-DEAD-LETTER-008 |
| REQ-WORKER-RETRY-DEAD-LETTER-013 | US-WORKER-RETRY-DEAD-LETTER-005 | FR-WORKER-RETRY-DEAD-LETTER-013 | T-WORKER-RETRY-DEAD-LETTER-001, T-WORKER-RETRY-DEAD-LETTER-009 |

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
- Global `agentic-sdlc-orchestrator`、`execution-manifest`、`tasks-to-implementation`、`tdd-workflow` skills 已用于执行纪律。

Review-doc-quality result：在显式预授权边界内 Ready for implementation；生成的 SDD set 无 critical 或 major findings。

## Verification Plan

- SDD gate: `npm run agent:check-sdd -- --slice worker-retry-dead-letter --require-api-guide --report docs/00-context/worker-retry-dead-letter-sdd-completion-report.md`
- Backend: `cd backend && mvn verify`
- Frontend: `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`
- Closeout: `npm run agent:closeout`
- Hygiene: `git status --short`, `git diff --check`, secret/private path scan, network dependency scan。

## Implementation Evidence

Backend implementation:

- Worker job、attempt、retry policy 与 dead-letter domain model。
- Additive Flyway migration `V19__worker_retry_dead_letter.sql`，仅包含 mock-safe fixtures。
- Worker recovery service 与 API controller，支持 list/detail/retry/acknowledge。
- Unit 与 API contract tests 覆盖 deterministic retry、terminal dead-letter、redaction、source trace 与 safe transitions。

Frontend implementation:

- Worker/dead-letter API types 与 API client bindings。
- Processing Center worker recovery panel，支持 list、detail、safe error、source trace、attempts、retry 与 acknowledge controls。
- UI test 覆盖 safe rendering 与 retry/acknowledge transitions。

Focused verification already passed:

- `cd backend && mvn -Dtest=WorkerJobServiceTest,WorkerRetryDeadLetterApiContractIT test`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`

Full verification passed:

- `npm run agent:check-sdd -- --slice worker-retry-dead-letter --require-api-guide --report docs/00-context/worker-retry-dead-letter-sdd-completion-report.md`
- `cd backend && mvn verify`
- `cd frontend && npm run typecheck && npm run test && npm run build`
- `git diff --check`
- Focused secret/private-path/network dependency scan over touched slice files
- `npm run agent:closeout`

## Status Notes

- SDD 与 implementation changes 保持在用户提供的 Goal / Scope / Exclusions / Acceptance 边界内。
- Acceptance by preauthorization: yes。
- Implementation 不引入 real provider、credential、external network、company data、production queue 或 scheduled worker。
- Worktree 存在无关的 pre-existing uncommitted changes；implementation 和 staging 必须保持 scoped。

## Residual Risks

- v0 只证明 deterministic local metadata transitions。
- Manual retry 不 dispatch real worker execution。
- Operator action metadata 不是 production audit。
- Production queue、scheduler、alerting、SLO 和 deployment monitoring 保留为未来工作。
