# 溯源：Answer Review Governance

最后更新：2026-07-07
状态：已按接受的 prototype 范围完成实现并通过本地验证。

## Slice Contract

- Goal：为 Trusted Ask answers 添加 answer review governance states。
- Slice：`answer-review-governance`
- Wave：Wave 4 / Ask And Graph Productization
- Workflow tier：Tier 1 / Standard Single Slice
- Autonomy boundary：满足；SDD changes 保持在 prompt 的 Goal、Scope、Exclusions 和 Acceptance 内。
- SDD skill chain used：yes。
- SDD accepted by preauthorization：yes。

## 来源

| Source | Use |
|---|---|
| `README.md` | Product 和 stack direction。 |
| `PROJECT_RULES.md` | SDD、phase、safety、adapter、traceability rules。 |
| `DEVELOPMENT_STANDARDS.md` | Verification 和 implementation standards。 |
| `docs/00-context/sdd-profile.md` | Required SDD artifact set 和 ID rules。 |
| `docs/01-requirements/requirement.md` | Product Ask/review/source-trace requirements。 |
| `docs/review-workflow.md` | 既有 document review states 和 boundaries。 |
| `docs/markdown-standard.md` | Source trace、confidence、review status preservation。 |
| `docs/knowledge-graph-design.md` | Downstream evidence boundary。 |
| `frontend/public/atlas-prototype.html`, `prototypes/index.html` | Trusted Ask baseline。 |
| Existing Ask backend/frontend code | Grounded API、DTO、mapper、service 和 UI decisions。 |

## Artifact Set

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/answer-review-governance-requirements.md` | `docs/01-requirements/answer-review-governance-requirements.zh-CN.md` |
| Stories | `docs/02-user-stories/answer-review-governance-stories.md` | `docs/02-user-stories/answer-review-governance-stories.zh-CN.md` |
| Spec | `docs/03-spec/answer-review-governance-spec.md` | `docs/03-spec/answer-review-governance-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/answer-review-governance-architecture.md` | `docs/04-architecture/answer-review-governance-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/answer-review-governance-data-flow.md` | `docs/04-architecture/answer-review-governance-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/answer-review-governance-data-model.md` | `docs/04-architecture/answer-review-governance-data-model.zh-CN.md` |
| Design | `docs/05-design/answer-review-governance-design.md` | `docs/05-design/answer-review-governance-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/answer-review-governance-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/answer-review-governance-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/answer-review-governance-tasks.md` | `docs/06-tasks/answer-review-governance-tasks.zh-CN.md` |

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-ANSWER-REVIEW-GOVERNANCE-001 | US-ANSWER-REVIEW-GOVERNANCE-001 | Status Model | T-ANSWER-REVIEW-GOVERNANCE-001, T-ANSWER-REVIEW-GOVERNANCE-005 |
| REQ-ANSWER-REVIEW-GOVERNANCE-002 | US-ANSWER-REVIEW-GOVERNANCE-001 | Status Model | T-ANSWER-REVIEW-GOVERNANCE-001 |
| REQ-ANSWER-REVIEW-GOVERNANCE-003 | US-ANSWER-REVIEW-GOVERNANCE-002 | Review Actions | T-ANSWER-REVIEW-GOVERNANCE-002, T-ANSWER-REVIEW-GOVERNANCE-003 |
| REQ-ANSWER-REVIEW-GOVERNANCE-004 | US-ANSWER-REVIEW-GOVERNANCE-004 | Reuse Boundary | T-ANSWER-REVIEW-GOVERNANCE-003, T-ANSWER-REVIEW-GOVERNANCE-006 |
| REQ-ANSWER-REVIEW-GOVERNANCE-005 | US-ANSWER-REVIEW-GOVERNANCE-003 | Preservation Rule | T-ANSWER-REVIEW-GOVERNANCE-004 |
| REQ-ANSWER-REVIEW-GOVERNANCE-006 | US-ANSWER-REVIEW-GOVERNANCE-002 | Safe Display | T-ANSWER-REVIEW-GOVERNANCE-003 |
| REQ-ANSWER-REVIEW-GOVERNANCE-007 | US-ANSWER-REVIEW-GOVERNANCE-001 | UI Behavior | T-ANSWER-REVIEW-GOVERNANCE-005, T-ANSWER-REVIEW-GOVERNANCE-006 |
| REQ-ANSWER-REVIEW-GOVERNANCE-008 | US-ANSWER-REVIEW-GOVERNANCE-003 | Acceptance Matrix | T-ANSWER-REVIEW-GOVERNANCE-004, T-ANSWER-REVIEW-GOVERNANCE-007 |

## Quality Review

- Skill chain files read：`atlas-sdd-generate-all`、`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`、`review-doc-quality`、`architecture-review`、`tdd-workflow`。
- `review-doc-quality` result：Ready for implementation；无 critical blockers。轻微 residual risk 是 future answer reuse semantics 按设计延后。

## Implementation Summary

- Backend 增加 Ask answer 专用 `AnswerReviewStatus`、review metadata persistence、review-action request DTO、service validation，以及 `POST /api/spaces/{spaceId}/ask/{runId}/review-actions`。
- `AskRunResponse` 与 mapper 现在返回 reviewer-safe metadata、display label 和 `answerReusable`；只有 approved 且带 answer text 与 eligible evidence 的 answer 才可复用。
- Frontend types、API client helper、mock data 与 Trusted Ask UI 现在展示 governance label、reuse hint 和 reviewer-safe reason，同时保留 citations。
- 既有 Ask evidence/citation behavior 已保留；review action 只更新 answer governance fields。

## Verification Evidence

| Check | Result | Evidence |
|---|---|---|
| SDD readiness | Passed with warnings only | `npm run agent:check-sdd -- --slice answer-review-governance`；warnings 为 architecture/design pair 缺少 REQ/US/T/AC ID，以及没有单独 report evidence。 |
| Targeted backend RED/GREEN loop | Passed after implementation | `cd backend && mvn test -Dtest='AskDomainInvariantTest,AskServiceTest,AskApiContractIT'`；15 tests passed。 |
| Full backend verification | Passed | `cd backend && mvn verify`；Surefire 131 tests passed，Failsafe 63 tests passed，2 个 opt-in runtime smoke tests skipped。 |
| Frontend typecheck | Passed | `cd frontend && npm run typecheck`。 |
| Frontend unit tests | Passed | `cd frontend && npm run test`；3 files、21 tests passed。 |
| Frontend build | Passed | `cd frontend && npm run build`；包含 lint、typecheck 和 Vite build。 |
| Closeout gate | Passed | `npm run agent:closeout`；package/script/YAML checks、trailing-whitespace scan、private-path scan、secret-pattern scan、`git diff --check` 和 changed-slice SDD gates passed。 |
| Focused changed-file scans | Passed with expected hits only | Secret/private-path scan 只命中 safe-text regexes、既有 mock API key UI tests 与 docs guardrail wording。Network/dependency scan 只命中既有 local/mock API plumbing 和 negative assertions；未引入新的 external cloud call 或 dependency。 |

## Residual Risks

- Approved answer reuse indexing、retrieval-quality metrics、reviewer queues、notifications 和 production RBAC/audit workflow automation 仍是后续切片。
- 当前实现是 mock/sample-safe 与 local/API-backed，不是 production approval system。
- Delivery 正在 clean scoped worktree 中收尾，commit 可以只包含 answer-review-governance 变更。
