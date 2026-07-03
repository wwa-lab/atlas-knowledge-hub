# Traceability: Review Publish

## Status

Implemented. Full bilingual SDD set generated and implementation verified for Phase 4 hardening. Last updated: 2026-07-03.

## Slice Contract

- **Goal:** Enable SMEs and delivery leads to publish only approved, source-traced Markdown into trusted Wiki metadata.
- **Slice:** `review-publish`
- **Phase:** 4 hardening
- **Scope:** Review queues, SME review state machine, publish eligibility, Wiki metadata publishing, API/UI contracts, verification tasks.
- **Exclusions:** Production auth/RBAC, graph extraction, Ask/RAG, real external adapter execution, real company data, production secret rollout.
- **Verification:** Full unit + integration + E2E for touched layer; plus `git diff --check`, network/dependency scan, secret/private-path scan.
- **Constraints:** Preserve source trace, confidence, and review status; LLM output stays review-required until verified; adapter boundaries; no raw secrets or private paths.

## Sources

| Source | Use |
|---|---|
| `PROJECT_RULES.md` | SDD, phase, adapter, security, bilingual rules. |
| `AGENTS.md` | Project-local agent execution rules. |
| `DEVELOPMENT_STANDARDS.md` | Phase 4 verification and quality gates. |
| `docs/00-context/sdd-profile.md` | Required document chain and ID rules. |
| `docs/01-requirements/requirement.md` | Product-level review, Wiki, Graph, Ask, security requirements. |
| `docs/review-workflow.md` | Review states and review actions. |
| `docs/markdown-standard.md` | Front matter, source trace, confidence, review status. |
| `docs/knowledge-graph-design.md` | Downstream evidence constraints. |
| `docs/00-context/slice-roadmap.md` | Phase 4 verification and constraints row. |
| `frontend/public/atlas-prototype.html`, `prototypes/index.html` | FE Processing Center, Wiki, Graph, and Ask baseline. |
| `docs/04-architecture/metadata-api-data-model.md` | Existing metadata entities and deferred Wiki table. |
| `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | Existing metadata API and deferred review-publish endpoints. |

## Artifact Set

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/review-publish-requirements.md` | `docs/01-requirements/review-publish-requirements.zh-CN.md` |
| Stories | `docs/02-user-stories/review-publish-stories.md` | `docs/02-user-stories/review-publish-stories.zh-CN.md` |
| Spec | `docs/03-spec/review-publish-spec.md` | `docs/03-spec/review-publish-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/review-publish-architecture.md` | `docs/04-architecture/review-publish-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/review-publish-data-flow.md` | `docs/04-architecture/review-publish-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/review-publish-data-model.md` | `docs/04-architecture/review-publish-data-model.zh-CN.md` |
| Design | `docs/05-design/review-publish-design.md` | `docs/05-design/review-publish-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/review-publish-tasks.md` | `docs/06-tasks/review-publish-tasks.zh-CN.md` |

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-REVIEW-PUBLISH-001 | US-REVIEW-PUBLISH-001 | Review Queue And Processing Center | T-REVIEW-PUBLISH-001, T-REVIEW-PUBLISH-006 |
| REQ-REVIEW-PUBLISH-002 | US-REVIEW-PUBLISH-002 | SME Review State Machine | T-REVIEW-PUBLISH-002 |
| REQ-REVIEW-PUBLISH-003 | US-REVIEW-PUBLISH-002 | SME Review State Machine | T-REVIEW-PUBLISH-002 |
| REQ-REVIEW-PUBLISH-004 | US-REVIEW-PUBLISH-003 | Publish Eligibility | T-REVIEW-PUBLISH-003 |
| REQ-REVIEW-PUBLISH-005 | US-REVIEW-PUBLISH-003 | Publish Result | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-005, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-006 | US-REVIEW-PUBLISH-004 | Publish Result | T-REVIEW-PUBLISH-003, T-REVIEW-PUBLISH-005 |
| REQ-REVIEW-PUBLISH-007 | US-REVIEW-PUBLISH-003, US-REVIEW-PUBLISH-004 | API Behavior | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-009 |
| REQ-REVIEW-PUBLISH-008 | US-REVIEW-PUBLISH-001 | Review Queue And Processing Center | T-REVIEW-PUBLISH-006, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-009 | US-REVIEW-PUBLISH-001, US-REVIEW-PUBLISH-003 | Downstream Trust Boundary | T-REVIEW-PUBLISH-006, T-REVIEW-PUBLISH-007 |
| REQ-REVIEW-PUBLISH-010 | US-REVIEW-PUBLISH-004 | Non-Functional Requirements | T-REVIEW-PUBLISH-003, T-REVIEW-PUBLISH-009 |
| REQ-REVIEW-PUBLISH-011 | US-REVIEW-PUBLISH-004 | Acceptance Matrix | T-REVIEW-PUBLISH-008, T-REVIEW-PUBLISH-009 |

## API Guide Decision

API guide included. This is a full-stack Phase 4 hardening slice and introduces new endpoints.

## Quality Gate: review-doc-quality

- **Document type:** Full SDD set.
- **Readiness verdict:** Ready with minor fixes.
- **Strengths:** Complete bilingual artifact set, stable IDs across languages, API guide included, tasks map to REQ IDs and spec sections, exact verification commands included.
- **Minor residual risks:** Duplicate publish behavior and file vs Wiki `PUBLISHED` ownership were resolved during implementation; remaining risk is that full-suite verification is currently affected by unrelated ask/knowledge-graph worktree changes.
- **No critical blockers found:** The SDD set is ready for human review before implementation.

## Recommended Codex Handoff Command

```text
Implement the review-publish slice strictly against docs/03-spec/review-publish-spec.md and docs/06-tasks/review-publish-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## Implementation Evidence

Updated on 2026-07-03 after Codex implementation verification.

- Backend added review queue and Wiki publish metadata endpoints without direct parser/converter/model/vector/storage/search calls.
- Review queue responses include bounded representative item metadata needed by the spec, limited to safe fields and covered by backend contract tests.
- Publish duplicate behavior is idempotent.
- Publish sets `wiki_page.review_status=PUBLISHED`; file and source chunk review metadata remain unchanged.
- No schema migration was required because `atlas.wiki_page` already contained the required metadata fields.
- Frontend typed mocks and prototype surfaces now show ready-to-publish, blocked missing-trace, and published Wiki metadata states.
- Verification passed: `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT,DomainInvariantTest'`; `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test -- --run`; `cd frontend && npm run build`; `cd frontend && npm run e2e`; prototype JavaScript syntax extraction; `git diff --check`.
- Previously blocked checks are now resolved: ask/knowledge-graph migration/route/projection expectations were aligned, and `frontend/src/App.vue` lint errors were fixed.
- Network/secret scan note: scans still report pre-existing mock provider URLs and masked password/secret copy in the prototype; no new runtime network call or raw secret was introduced by this slice.
