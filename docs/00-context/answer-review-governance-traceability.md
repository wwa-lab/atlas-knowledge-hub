# Traceability: Answer Review Governance

Last updated: 2026-07-07
Status: Implemented and locally verified for the accepted prototype scope.

## Slice Contract

- Goal: Add answer review governance states to Trusted Ask answers.
- Slice: `answer-review-governance`
- Wave: Wave 4 / Ask And Graph Productization
- Workflow tier: Tier 1 / Standard Single Slice
- Autonomy boundary: satisfied; SDD changes stay within the prompt's Goal, Scope, Exclusions, and Acceptance.
- SDD skill chain used: yes.
- SDD accepted by preauthorization: yes.

## Sources

| Source | Use |
|---|---|
| `README.md` | Product and stack direction. |
| `PROJECT_RULES.md` | SDD, phase, safety, adapter, traceability rules. |
| `DEVELOPMENT_STANDARDS.md` | Verification and implementation standards. |
| `docs/00-context/sdd-profile.md` | Required SDD artifact set and ID rules. |
| `docs/01-requirements/requirement.md` | Product Ask/review/source-trace requirements. |
| `docs/review-workflow.md` | Existing document review states and boundaries. |
| `docs/markdown-standard.md` | Source trace, confidence, review status preservation. |
| `docs/knowledge-graph-design.md` | Downstream evidence boundary. |
| `frontend/public/atlas-prototype.html`, `prototypes/index.html` | Trusted Ask baseline. |
| Existing Ask backend/frontend code | Grounded API, DTO, mapper, service, and UI decisions. |

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

- Skill chain files read: `atlas-sdd-generate-all`, `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, `review-doc-quality`, `architecture-review`, `tdd-workflow`.
- `review-doc-quality` result: Ready for implementation; no critical blockers. Minor residual risk is that future answer reuse semantics remain deferred by design.

## Implementation Summary

- Backend added Ask-answer-specific `AnswerReviewStatus` values, review metadata persistence, a review-action request DTO, service validation, and `POST /api/spaces/{spaceId}/ask/{runId}/review-actions`.
- `AskRunResponse` and mapper output now expose reviewer-safe metadata, a display label, and `answerReusable`, which is true only for approved answers with answer text and eligible evidence.
- Frontend types, API client helpers, mock data, and Trusted Ask UI surfaces now show the governance label, reuse hint, and reviewer-safe reason without hiding citations.
- Existing Ask evidence/citation behavior is preserved; review actions update only answer governance fields.

## Verification Evidence

| Check | Result | Evidence |
|---|---|---|
| SDD readiness | Passed with warnings only | `npm run agent:check-sdd -- --slice answer-review-governance`; warnings were missing REQ/US/T/AC IDs in architecture/design pairs and missing separate report evidence. |
| Targeted backend RED/GREEN loop | Passed after implementation | `cd backend && mvn test -Dtest='AskDomainInvariantTest,AskServiceTest,AskApiContractIT'`; 15 tests passed. |
| Full backend verification | Passed | `cd backend && mvn verify`; Surefire 131 tests passed, Failsafe 63 tests passed, 2 opt-in runtime smoke tests skipped. |
| Frontend typecheck | Passed | `cd frontend && npm run typecheck`. |
| Frontend unit tests | Passed | `cd frontend && npm run test`; 3 files and 21 tests passed. |
| Frontend build | Passed | `cd frontend && npm run build`; includes lint, typecheck, and Vite build. |
| Closeout gate | Passed | `npm run agent:closeout`; package/script/YAML checks, trailing-whitespace scan, private-path scan, secret-pattern scan, `git diff --check`, and changed-slice SDD gates passed. |
| Focused changed-file scans | Passed with expected hits only | Secret/private-path scan hit safe-text regexes, existing mock API key UI tests, and docs guardrail wording. Network/dependency scan hit existing local/mock API plumbing and negative assertions; no new external cloud call or dependency was introduced. |

## Residual Risks

- Approved answer reuse indexing, retrieval-quality metrics, reviewer queues, notifications, and production RBAC/audit workflow automation remain future slices.
- Current implementation is mock/sample-safe and local/API-backed; it is not a production approval system.
- Delivery is being finalized in a clean scoped worktree so the commit can contain only answer-review-governance changes.
