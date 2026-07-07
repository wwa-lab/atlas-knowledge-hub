# Traceability: worker-retry-dead-letter

Status: Implemented and verified
Last updated: 2026-07-07
Manifest: `docs/00-context/execution-manifests/worker-retry-dead-letter-20260707.yaml`
Maturity target: Local deterministic retry/dead-letter foundation; not production queue or worker operations readiness.

## Source Context

- User goal: deliver `worker-retry-dead-letter` full single-slice delivery for Wave 5 / Connector And Operations.
- Required repo context: `AGENTS.md`, `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, SDD bootstrap docs, SDD profile, goal-loop docs, roadmap docs, closeout checklist.
- Related slices referenced: folder upload, batch processing, connector sync v0, rate-limit-safe-errors, secret-manager-integration, review publish, markdown/source trace, adapter boundary docs.
- Missing required context noted: `docs/00-context/repo-status-roadmap.md` was not present when the goal started; the Chinese canonical overview existed at `docs/00-context/repo-status-roadmap.zh-CN.md`.

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
- Global `agentic-sdlc-orchestrator`, `execution-manifest`, `tasks-to-implementation`, and `tdd-workflow` skills were read for execution discipline.

Review-doc-quality result: Ready for implementation under the explicit preauthorization boundary; no critical or major findings in the generated SDD set.

## Verification Plan

- SDD gate: `npm run agent:check-sdd -- --slice worker-retry-dead-letter --require-api-guide --report docs/00-context/worker-retry-dead-letter-sdd-completion-report.md`
- Backend: `cd backend && mvn verify`
- Frontend: `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`
- Closeout: `npm run agent:closeout`
- Hygiene: `git status --short`, `git diff --check`, secret/private path scan, network dependency scan.

## Implementation Evidence

Backend implementation:

- Worker job, attempt, retry policy, and dead-letter domain model.
- Additive Flyway migration `V19__worker_retry_dead_letter.sql` with mock-safe fixtures.
- Worker recovery service and API controller for list/detail/retry/acknowledge.
- Unit and API contract tests for deterministic retry, terminal dead-letter, redaction, source trace, and safe transitions.

Frontend implementation:

- Worker/dead-letter API types and API client bindings.
- Processing Center worker recovery panel with list, detail, safe error, source trace, attempts, retry, and acknowledge controls.
- UI test for safe rendering and retry/acknowledge transitions.

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

- SDD and implementation changes remain inside the user-provided Goal / Scope / Exclusions / Acceptance boundary.
- Acceptance by preauthorization: yes.
- No real provider, credential, external network, company data, production queue, or scheduled worker is introduced by the implementation.
- Worktree contains unrelated pre-existing uncommitted changes; implementation and staging must remain scoped.

## Residual Risks

- v0 only proves deterministic local metadata transitions.
- Manual retry does not dispatch real worker execution.
- Operator action metadata is not production audit.
- Production queue, scheduler, alerting, SLO, and deployment monitoring remain future work.
