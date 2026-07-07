# 溯源：retrieval-quality-metrics

## 状态

已实现并验证。该切片现在为 Trusted Ask runs 暴露确定性、mock-safe 的 retrieval quality metrics，并在前端显示安全 quality signals；未改变 provider/model/vector/auth/rate-limit/audit 语义。

## 切片契约

| Field | Value |
|---|---|
| Goal mode | autonomous-single-slice |
| Autonomy level | standard-preauthorized |
| Slice | `retrieval-quality-metrics` |
| Wave | Wave 4 / Ask And Graph Productization |
| Branch | `develop-leo` |
| Goal | 为 Trusted Ask 及相邻知识检索界面增加安全确定性 retrieval quality metrics。 |
| SDD accepted by preauthorization | yes |

## 已读来源

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
- goal prompt 中列出的相关 Ask、review/publish、graph、wiki 与 safe-error traceability files。
- 现有 backend 与 frontend Ask、Graph、Wiki、review/publish、safe error、API envelope、repository、service、controller 和 TypeScript surfaces。

## SDD 技能链证据

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

## 需求追踪

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

## SDD 质量门

| Gate | Result |
|---|---|
| English and Chinese files exist | Pass |
| IDs match across language pairs | Pass |
| Requirements map to stories/spec/tasks | Pass |
| API guide included | Pass |
| Adapter boundaries and no-network constraints explicit | Pass |
| SDD accepted by preauthorization | yes |

## 实现证据

| Area | Evidence |
|---|---|
| Backend calculator | `RetrievalQualityMetricsCalculator` 基于已存储 Ask metadata 计算 evidence coverage、citation health、confidence band、conservative review eligibility、no-evidence refusal 与 aggregate summaries。 |
| Backend API | `RetrievalQualityMetricsController` 通过 `ApiEnvelope` 暴露 `GET /api/ask-runs/{runId}/quality-metrics` 与 `GET /api/spaces/{spaceId}/retrieval-quality-metrics`。 |
| Backend safety | Metrics DTOs 排除 raw questions、answers、source content、prompts、provider payloads、stack traces、secrets、private paths 与 internal endpoints。 |
| Frontend API | `frontend/src/api.ts` 增加 run 与 space metrics client calls，并在 `frontend/src/types.ts` 中提供 typed DTOs。 |
| Frontend UI | `frontend/src/App.vue` 在 API-backed Trusted Ask 中渲染安全 quality chips；metrics 无法加载时显示安全 unavailable messaging。 |
| E2E | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts` 验证可见 quality signals 与新 metrics endpoint 调用。 |

## 验证证据

| Check | Result | Notes |
|---|---|---|
| SDD gate | Passed | `npm run agent:check-sdd -- --slice retrieval-quality-metrics --require-api-guide --report docs/00-context/retrieval-quality-metrics-sdd-completion-report.md` |
| Focused backend | Scoped fixes 后 Passed | `cd backend && mvn -q -Dtest=RetrievalQualityMetricsCalculatorTest -Dit.test=RetrievalQualityMetricsApiContractIT verify` |
| Backend full verify | Passed | `cd backend && mvn verify` |
| Frontend typecheck | Passed | `cd frontend && npm run typecheck` |
| Frontend tests | Passed | `cd frontend && npm run test` |
| Frontend build | Passed | `cd frontend && npm run build` |
| Frontend E2E | Passed | `cd frontend && npm run e2e` |
| Diff hygiene | Passed | `git diff --check` |
| Secret/private-path scan | Passed | 对已变更 docs、backend、frontend 与 tests 进行 focused changed-file scan |
| Network/dependency scan | Passed | Focused changed-file scan 未发现新的 external provider、analytics、observability 或 cloud calls |
| Closeout gate | Passed | `npm run agent:closeout` |

## 残留风险

- Metrics 是本地质量信号，不是 production retrieval governance。
- 复用已有 Ask session citation metadata 作为 grounding；本切片不扩展 answer governance。
- Aggregate metrics 有意避免 raw question 与 answer text；现有 Ask run API 仍返回既有 Ask contract。
