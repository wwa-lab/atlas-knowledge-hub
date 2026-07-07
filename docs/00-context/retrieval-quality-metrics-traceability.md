# Traceability: retrieval-quality-metrics

## Status

Implemented and verified. The slice now exposes deterministic, mock-safe retrieval quality metrics for Trusted Ask runs and safe frontend quality signals without changing provider/model/vector/auth/rate-limit/audit semantics.

## Slice Contract

| Field | Value |
|---|---|
| Goal mode | autonomous-single-slice |
| Autonomy level | standard-preauthorized |
| Slice | `retrieval-quality-metrics` |
| Wave | Wave 4 / Ask And Graph Productization |
| Branch | `develop-leo` |
| Goal | Add safe deterministic retrieval quality metrics for Trusted Ask and adjacent knowledge retrieval surfaces. |
| SDD accepted by preauthorization | yes |

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
- Related Ask, review/publish, graph, wiki, and safe-error traceability files listed in the goal prompt.
- Existing backend and frontend Ask, Graph, Wiki, review/publish, safe error, API envelope, repository, service, controller, and TypeScript surfaces.

## SDD Skill Chain Evidence

SDD skill chain used: yes

| Skill | File read |
|---|---|
| `atlas-sdd-generate-all` | `.agents/skills/atlas-sdd-generate-all/SKILL.md` |
| `req-to-user-story` | `.agents/skills/req-to-user-story/SKILL.md` |
| `user-story-to-spec` | `.agents/skills/user-story-to-spec/SKILL.md` |
| `spec-to-architecture` | `.agents/skills/spec-to-architecture/SKILL.md` |
| `architecture-to-design` | `.agents/skills/architecture-to-design/SKILL.md` |
| `design-to-tasks` | `.agents/skills/design-to-tasks/SKILL.md` |
| `review-doc-quality` | `.agents/skills/review-doc-quality/SKILL.md` |

## Artifact Set

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/retrieval-quality-metrics-requirements.md` | `docs/01-requirements/retrieval-quality-metrics-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/retrieval-quality-metrics-stories.md` | `docs/02-user-stories/retrieval-quality-metrics-stories.zh-CN.md` |
| Spec | `docs/03-spec/retrieval-quality-metrics-spec.md` | `docs/03-spec/retrieval-quality-metrics-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/retrieval-quality-metrics-architecture.md` | `docs/04-architecture/retrieval-quality-metrics-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/retrieval-quality-metrics-data-flow.md` | `docs/04-architecture/retrieval-quality-metrics-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/retrieval-quality-metrics-data-model.md` | `docs/04-architecture/retrieval-quality-metrics-data-model.zh-CN.md` |
| Design | `docs/05-design/retrieval-quality-metrics-design.md` | `docs/05-design/retrieval-quality-metrics-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/retrieval-quality-metrics-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/retrieval-quality-metrics-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/retrieval-quality-metrics-tasks.md` | `docs/06-tasks/retrieval-quality-metrics-tasks.zh-CN.md` |
| Traceability | `docs/00-context/retrieval-quality-metrics-traceability.md` | `docs/00-context/retrieval-quality-metrics-traceability.zh-CN.md` |

## Requirement Trace

| Requirement | Stories | Spec | Tasks |
|---|---|---|---|
| REQ-RETRIEVAL-QUALITY-METRICS-001 | US-RETRIEVAL-QUALITY-METRICS-001, US-RETRIEVAL-QUALITY-METRICS-002 | FR-RETRIEVAL-QUALITY-METRICS-001, FR-RETRIEVAL-QUALITY-METRICS-003 | T-RETRIEVAL-QUALITY-METRICS-002, T-RETRIEVAL-QUALITY-METRICS-003 |
| REQ-RETRIEVAL-QUALITY-METRICS-002 | US-RETRIEVAL-QUALITY-METRICS-001, US-RETRIEVAL-QUALITY-METRICS-003 | FR-RETRIEVAL-QUALITY-METRICS-002 | T-RETRIEVAL-QUALITY-METRICS-002, T-RETRIEVAL-QUALITY-METRICS-005 |
| REQ-RETRIEVAL-QUALITY-METRICS-003 | US-RETRIEVAL-QUALITY-METRICS-002, US-RETRIEVAL-QUALITY-METRICS-003 | FR-RETRIEVAL-QUALITY-METRICS-003 | T-RETRIEVAL-QUALITY-METRICS-003 |
| REQ-RETRIEVAL-QUALITY-METRICS-004 | US-RETRIEVAL-QUALITY-METRICS-001, US-RETRIEVAL-QUALITY-METRICS-002 | FR-RETRIEVAL-QUALITY-METRICS-004 | T-RETRIEVAL-QUALITY-METRICS-002, T-RETRIEVAL-QUALITY-METRICS-003 |
| REQ-RETRIEVAL-QUALITY-METRICS-005 | US-RETRIEVAL-QUALITY-METRICS-004 | FR-RETRIEVAL-QUALITY-METRICS-005 | T-RETRIEVAL-QUALITY-METRICS-004 |
| REQ-RETRIEVAL-QUALITY-METRICS-006 | US-RETRIEVAL-QUALITY-METRICS-003 | FR-RETRIEVAL-QUALITY-METRICS-006 | T-RETRIEVAL-QUALITY-METRICS-005, T-RETRIEVAL-QUALITY-METRICS-006 |
| REQ-RETRIEVAL-QUALITY-METRICS-007 | US-RETRIEVAL-QUALITY-METRICS-004 | FR-RETRIEVAL-QUALITY-METRICS-007 | T-RETRIEVAL-QUALITY-METRICS-004, T-RETRIEVAL-QUALITY-METRICS-005, T-RETRIEVAL-QUALITY-METRICS-006, T-RETRIEVAL-QUALITY-METRICS-007 |
| REQ-RETRIEVAL-QUALITY-METRICS-008 | US-RETRIEVAL-QUALITY-METRICS-005 | FR-RETRIEVAL-QUALITY-METRICS-008 | T-RETRIEVAL-QUALITY-METRICS-001, T-RETRIEVAL-QUALITY-METRICS-007, T-RETRIEVAL-QUALITY-METRICS-008 |

## SDD Quality Gate

| Gate | Result |
|---|---|
| English and Chinese files exist | Pass |
| IDs match across language pairs | Pass |
| Requirements map to stories/spec/tasks | Pass |
| API guide included | Pass |
| Adapter boundaries and no-network constraints explicit | Pass |
| SDD accepted by preauthorization | yes |

## Implementation Evidence

| Area | Evidence |
|---|---|
| Backend calculator | `RetrievalQualityMetricsCalculator` computes evidence coverage, citation health, confidence band, conservative review eligibility, no-evidence refusal, and aggregate summaries from stored Ask metadata. |
| Backend API | `RetrievalQualityMetricsController` exposes `GET /api/ask-runs/{runId}/quality-metrics` and `GET /api/spaces/{spaceId}/retrieval-quality-metrics` through `ApiEnvelope`. |
| Backend safety | Metrics DTOs omit raw questions, answers, source content, prompts, provider payloads, stack traces, secrets, private paths, and internal endpoints. |
| Frontend API | `frontend/src/api.ts` adds run and space metrics client calls with typed DTOs in `frontend/src/types.ts`. |
| Frontend UI | `frontend/src/App.vue` renders safe quality chips in API-backed Trusted Ask and falls back to safe unavailable messaging if metrics cannot load. |
| E2E | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts` verifies visible quality signals and the new metrics endpoint call. |

## Verification Evidence

| Check | Result | Notes |
|---|---|---|
| SDD gate | Passed | `npm run agent:check-sdd -- --slice retrieval-quality-metrics --require-api-guide --report docs/00-context/retrieval-quality-metrics-sdd-completion-report.md` |
| Focused backend | Passed after scoped fixes | `cd backend && mvn -q -Dtest=RetrievalQualityMetricsCalculatorTest -Dit.test=RetrievalQualityMetricsApiContractIT verify` |
| Backend full verify | Passed | `cd backend && mvn verify` |
| Frontend typecheck | Passed | `cd frontend && npm run typecheck` |
| Frontend tests | Passed | `cd frontend && npm run test` |
| Frontend build | Passed | `cd frontend && npm run build` |
| Frontend E2E | Passed | `cd frontend && npm run e2e` |
| Diff hygiene | Passed | `git diff --check` |
| Secret/private-path scan | Passed | Focused changed-file scan over changed docs, backend, frontend, and tests |
| Network/dependency scan | Passed | Focused changed-file scan found no new external provider, analytics, observability, or cloud calls |
| Closeout gate | Passed | `npm run agent:closeout` |

## Residual Risks

- Metrics are local quality signals, not production retrieval governance.
- Existing Ask session citation metadata is reused as grounding; this slice does not extend answer governance.
- Aggregate metrics intentionally avoid raw question and answer text; existing Ask run API still returns the existing Ask contract.
