# Traceability: Frontend Componentization

## Slice Contract

- Slice: `frontend-componentization`
- Goal: Behavior-preserving structural extraction of the monolithic Vue `App.vue`.
- Phase: 1 FE structural hardening.
- Status: SDD draft generated on 2026-07-08; implementation requires SDD acceptance.

## Source Documents

- `README.md`
- `AGENTS.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/03-spec/knowledge-space-spec.md`
- `docs/03-spec/full-stack-productization-spec.md`
- `docs/06-tasks/knowledge-space-tasks.md`
- `docs/06-tasks/full-stack-productization-tasks.md`
- `frontend/src/App.vue`
- `frontend/src/App.test.ts`
- `frontend/src/api.ts`
- `frontend/src/types.ts`
- `frontend/src/data/atlasMock.ts`

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-003 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-002 | US-FRONTEND-COMPONENTIZATION-002 | T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004 |
| REQ-FRONTEND-COMPONENTIZATION-003 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-001, T-FRONTEND-COMPONENTIZATION-003, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-004 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-006 |
| REQ-FRONTEND-COMPONENTIZATION-005 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005 |
| REQ-FRONTEND-COMPONENTIZATION-006 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-005, T-FRONTEND-COMPONENTIZATION-007 |
| REQ-FRONTEND-COMPONENTIZATION-007 | US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-002, T-FRONTEND-COMPONENTIZATION-006, T-FRONTEND-COMPONENTIZATION-007 |
| REQ-FRONTEND-COMPONENTIZATION-008 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-006, T-FRONTEND-COMPONENTIZATION-007 |

## API Guide Decision

API guide omitted. This slice is frontend-only and does not add or change backend endpoints, request/response payloads, persistence, adapter contracts, provider/runtime behavior, or deployment behavior.

## Verification Plan

- `npm run agent:check-sdd -- --slice frontend-componentization`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test -- --run`
- `cd frontend && npm run build`
- `cd frontend && npm run e2e`
- `git diff --check`
- Focused secret/private-path scan over changed files.
- Focused new-network/dependency scan over changed files.
- `npm run agent:closeout`

## SDD Quality Gate

- English and Chinese companions exist for every new SDD artifact.
- IDs match across both languages.
- API guide omission is recorded in requirements, data model, tasks, and traceability.
- `vue-router` is explicitly excluded unless URL semantics, deep links, and browser back/forward become accepted product capabilities.
- `review-doc-quality` checklist was applied as a self-review; no critical SDD blockers are known before user acceptance.

## Deferred Work

- URL semantics, deep links, browser back/forward behavior, and route guards.
- Pinia/Vuex/global store.
- Visual redesign.
- New backend/API contracts.
- Production auth/RBAC, provider/runtime, storage/vector/model changes.
- Real company data ingestion.
