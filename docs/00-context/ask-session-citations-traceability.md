# Traceability: Ask Session Citations

## Status

Implemented and verified for autonomous single-slice delivery on 2026-07-07.

## Slice Contract

| Field | Value |
|---|---|
| Goal | Add session-scoped Trusted Ask answer citation snapshots with safe source trace and review-aware evidence. |
| Slice | `ask-session-citations` |
| Wave | Wave 4 / Ask And Graph Productization |
| Workflow tier | Tier 1 / Standard Single Slice |
| Autonomy boundary | SDD and implementation are preauthorized only inside the prompt Goal, Scope, Exclusions, Acceptance, and Stop conditions. |
| API guide | Included because backend/API/persistence work is in scope. |

## Sources Read

- `AGENTS.md`
- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/repo-status-roadmap.zh-CN.md`
- `docs/00-context/agent-goal-loop-workflow.md`
- `docs/00-context/agent-goal-loop-workflow.zh-CN.md`
- `docs/00-context/agent-goal-loop-quickstart.md`
- `docs/00-context/agent-goal-loop-quickstart.zh-CN.md`
- `docs/00-context/codex-goal-prompts.zh-CN.md`
- `docs/00-context/sdd-profile.md`
- `docs/SDD-BOOTSTRAP.md`
- `docs/SDD-BOOTSTRAP.zh-CN.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- `docs/00-context/ask-rag-traceability.md`
- `docs/00-context/ask-rag-traceability.zh-CN.md`
- `docs/00-context/review-publish-traceability.md`
- `docs/00-context/review-publish-traceability.zh-CN.md`
- `docs/00-context/wiki-ingest-v0-traceability.md`
- `docs/00-context/wiki-ingest-v0-traceability.zh-CN.md`
- `docs/00-context/wiki-linkify-lint-traceability.md`
- `docs/00-context/wiki-linkify-lint-traceability.zh-CN.md`
- `docs/01-requirements/requirement.md`
- Existing Ask backend and frontend files identified during grounding.

## SDD Skill Chain Evidence

| Skill | File read |
|---|---|
| `atlas-sdd-generate-all` | `.agents/skills/atlas-sdd-generate-all/SKILL.md` |
| `req-to-user-story` | `.agents/skills/req-to-user-story/SKILL.md` |
| `user-story-to-spec` | `.agents/skills/user-story-to-spec/SKILL.md` |
| `spec-to-architecture` | `.agents/skills/spec-to-architecture/SKILL.md` |
| `architecture-to-design` | `.agents/skills/architecture-to-design/SKILL.md` |
| `design-to-tasks` | `.agents/skills/design-to-tasks/SKILL.md` |
| `review-doc-quality` | `.agents/skills/review-doc-quality/SKILL.md` |
| `architecture-review` | `.agents/skills/architecture-review/SKILL.md` |

Additional references read: `.agents/skills/_shared/grounding-rules.md`, `.agents/skills/review-doc-quality/references/completeness-criteria.md`, `.agents/skills/review-doc-quality/references/phase-scope-guide.md`, and `.agents/skills/architecture-to-design/references/design-patterns.md`.

SDD skill chain used: yes.

## Grounded Existing Implementation

| Area | Grounding |
|---|---|
| Existing Ask create/read API | `AskController` exposes `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}`. |
| Existing Ask answer record | `AskRun` stores question, status, review policy, mode, requestedBy, answer, confidence, review status, model run id, safe message, and timestamps. |
| Existing citation foundation | `AskEvidence` stores source chunk id, file item id, source file, page, section, review status, confidence, vector item key, score, and creation time. |
| Existing UI | `frontend/src/App.vue` renders Ask answer evidence and review status; `frontend/src/api.ts` calls create/read Ask endpoints. |
| Existing migration | Flyway chain includes `V8__ask_rag.sql`; current latest migration before this slice is `V14__audit_log_foundation.sql`. |

## Artifact Set

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/ask-session-citations-requirements.md` | `docs/01-requirements/ask-session-citations-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/ask-session-citations-stories.md` | `docs/02-user-stories/ask-session-citations-stories.zh-CN.md` |
| Spec | `docs/03-spec/ask-session-citations-spec.md` | `docs/03-spec/ask-session-citations-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/ask-session-citations-architecture.md` | `docs/04-architecture/ask-session-citations-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/ask-session-citations-data-flow.md` | `docs/04-architecture/ask-session-citations-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/ask-session-citations-data-model.md` | `docs/04-architecture/ask-session-citations-data-model.zh-CN.md` |
| Design | `docs/05-design/ask-session-citations-design.md` | `docs/05-design/ask-session-citations-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/ask-session-citations-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/ask-session-citations-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/ask-session-citations-tasks.md` | `docs/06-tasks/ask-session-citations-tasks.zh-CN.md` |
| Traceability | `docs/00-context/ask-session-citations-traceability.md` | `docs/00-context/ask-session-citations-traceability.zh-CN.md` |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | US-ASK-SESSION-CITATIONS-001 | T-ASK-SESSION-CITATIONS-002, T-ASK-SESSION-CITATIONS-003 |
| REQ-ASK-SESSION-CITATIONS-002 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-003 | US-ASK-SESSION-CITATIONS-001 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-004 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-002, T-ASK-SESSION-CITATIONS-003 |
| REQ-ASK-SESSION-CITATIONS-005 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-006 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-007 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-008 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-009 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-010 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-006, T-ASK-SESSION-CITATIONS-007, T-ASK-SESSION-CITATIONS-008 |
| REQ-ASK-SESSION-CITATIONS-011 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-004, T-ASK-SESSION-CITATIONS-009 |
| REQ-ASK-SESSION-CITATIONS-012 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-010 |

## Review-Doc-Quality Result

| Gate | Result |
|---|---|
| Expected artifacts exist | Pass |
| Bilingual IDs match | Pass |
| API guide included | Pass |
| Tasks are executable | Pass |
| Adapter boundaries explicit | Pass |
| Open questions explicit | Pass |
| Deferred decision scan | Pass |

## SDD Gate Acceptance Note

- **SDD accepted by preauthorization:** yes, because the SDD remains inside the attached prompt Goal, Scope, Exclusions, Acceptance, and Autonomy boundary.
- **User acceptance required before code:** no additional acceptance required for this autonomous goal unless a stop condition appears.
- **Decision:** proceed to implementation in task ID order.

## Implementation Evidence

Implementation was completed in a clean detached worktree from `develop-leo` to avoid unrelated in-progress `answer-review-governance`, `graph-from-wiki-extraction`, and `retrieval-quality-metrics` work that appeared in the primary worktree during execution.

| Area | Evidence |
|---|---|
| Persistence | Added `AskSession`, `AskSessionRepository`, `AskRun.sessionId`, citation snapshot fields on `AskEvidence`, and Flyway migration `V15__ask_session_citations.sql`. |
| Backend service | `AskService` now creates or reuses sessions, touches session update time after terminal outcomes, derives safe citation labels/locators/status, and returns session list/detail responses. |
| API contract | `AskController` exposes `GET /api/spaces/{spaceId}/ask-sessions` and `GET /api/ask-sessions/{sessionId}`; `AskApiContractIT` verifies session identity, session history, and citation fields. |
| Frontend | `frontend/src/types.ts`, `frontend/src/api.ts`, and `frontend/src/App.vue` now handle Ask session summaries/details and display citation id, label, locator, eligibility status, and exclusion reason. |
| Fixtures/tests | Frontend mock data and P0 API mocks include session list/detail endpoints and richer citation metadata. |
| Scope control | No provider/model adapter changes, production auth/RBAC/audit/secret/rate-limit semantic changes, real data, cloud calls, answer governance, retrieval metrics, or graph extraction behavior were added. |

## Verification Evidence

| Check | Result |
|---|---|
| `npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide` | Pass; non-blocking generated-artifact warnings remain for architecture/design/API ID density and missing optional `--report`. |
| `cd backend && mvn verify` | Pass; 129 unit tests and 61 integration tests passed, with 2 configured runtime smoke tests skipped as designed. |
| `cd frontend && npm run typecheck` | Pass. |
| `cd frontend && npm run test` | Pass; 3 files / 21 tests. |
| `cd frontend && npm run build` | Pass; includes lint, typecheck, and Vite production build. |
| `npm run agent:closeout` | Pass; workflow YAML, workflow private-path/secret scans, `git diff --check`, and changed-slice SDD gates passed. |
| `git diff --check` | Pass. |
| Added-line safety scan | Pass; no new secrets, raw endpoints, JDBC URLs, or private paths in added slice lines. |

## Residual Risks

- Production answer approval and governance remain deferred to `answer-review-governance`.
- Retrieval metrics remain deferred to `retrieval-quality-metrics`.
- Citation eligibility is snapshot-based and review-aware, but production-grade citation quality scoring remains out of scope.
- The isolated delivery worktree intentionally excludes unrelated in-progress files from the primary worktree.
