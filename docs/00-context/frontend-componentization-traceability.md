# Traceability: Frontend Componentization

## Slice Contract

- Slice: `frontend-componentization`
- Goal: Behavior-preserving structural extraction of the monolithic Vue `App.vue`.
- Phase: 1 FE structural hardening.
- Status: SDD accepted on 2026-07-08; T-FRONTEND-COMPONENTIZATION-001 through T-FRONTEND-COMPONENTIZATION-011 implemented and locally verified, including the `frontend-state-extraction` follow-up. This is a behavior-preserving frontend structural checkpoint, not a production-readiness claim.

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
- `frontend/src/composables/useSpaces.ts`
- `frontend/src/composables/useBatches.ts`
- `frontend/src/composables/useReviewQueue.ts`
- `frontend/src/composables/useWikiPages.ts`
- `frontend/src/composables/useGraph.ts`
- `frontend/src/composables/useAsk.ts`
- `frontend/src/composables/useSettings.ts`
- `frontend/src/composables/useGlobalChat.ts`
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
| REQ-FRONTEND-COMPONENTIZATION-009 | US-FRONTEND-COMPONENTIZATION-001, US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-008, T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010 |
| REQ-FRONTEND-COMPONENTIZATION-010 | US-FRONTEND-COMPONENTIZATION-004 | T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010 |
| REQ-FRONTEND-COMPONENTIZATION-011 | US-FRONTEND-COMPONENTIZATION-005 | T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-011 |

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

## Implementation Evidence

| Task IDs | Status | Evidence |
|---|---|---|
| T-FRONTEND-COMPONENTIZATION-001 | Completed | User accepted the SDD on 2026-07-08; `npm run agent:check-sdd -- --slice frontend-componentization --report docs/00-context/frontend-componentization-sdd-completion-report.md` passed before implementation. |
| T-FRONTEND-COMPONENTIZATION-002 | Completed | Added `frontend/src/domain/viewModels.ts` and `frontend/src/domain/viewModels.test.ts`; extracted pure view-model types and helpers for Ask governance labels, metric ratios, Wiki/Graph source trace formatting, graph/model labels, and secret status labels. |
| T-FRONTEND-COMPONENTIZATION-003 | Completed | Added `ProductShell`, `ProductSidebar`, `ProductHomeView`, and `GlobalChatView`; `App.vue` keeps `productView` and handler ownership while delegating shell/home/chat templates through typed props/events. |
| T-FRONTEND-COMPONENTIZATION-004 | Completed | Added `SpaceDetailView`, `SpaceDocumentsTab`, `SpaceConnectorsTab`, `SpaceReviewTab`, `SpaceWikiTab`, and `SpaceGraphTab`. `App.vue` still owns `activeSpaceTab`, selected IDs, form drafts, API calls, and handler side effects while the Space components receive typed props/events. |
| T-FRONTEND-COMPONENTIZATION-005 | Completed | Added `SettingsModal`, behavior-bearing settings panel components, and `ModelEditor`. `App.vue` still owns `settingsOpen`, `settingsPanel`, capability gates, form state, API/model side effects, and masked display state while settings components receive typed props/events. |
| T-FRONTEND-COMPONENTIZATION-006 | Completed | Added `frontend/src/composables/useProductUploadWorkflow.ts` and focused tests for the mock upload/processing/report workflow. `App.vue` still owns product navigation and passes the documents-tab callback; no API helper usage, backend contract, dependency, or runtime behavior changed. |
| T-FRONTEND-COMPONENTIZATION-007 | Completed | Ran final frontend regression, Atlas workflow gates, closeout gate, diff hygiene, dependency scan, focused new-network/private-path scan, and focused secret scan. No backend/provider checks were run because this slice did not touch backend/API/provider runtime behavior. |
| T-FRONTEND-COMPONENTIZATION-008 | Completed | Extended the bilingual spec/tasks for `frontend-state-extraction`, including the eight domain composables, explicit props/events data flow, factory-function rule, App.vue line-count target, and no Pinia/router/provide-inject constraint. |
| T-FRONTEND-COMPONENTIZATION-009 | Completed | Added factory composables `useSpaces`, `useBatches`, `useReviewQueue`, `useWikiPages`, `useGraph`, `useAsk`, `useSettings`, and `useGlobalChat`, with API calls still routed through `frontend/src/api.ts`. Connector sync remains inside the batches/workflow domain to avoid introducing a ninth state domain. |
| T-FRONTEND-COMPONENTIZATION-010 | Completed | Rewired `App.vue` into a thin orchestration shell and extracted `P0WorkbenchView` plus `SettingsOrchestrator`; `wc -l frontend/src/App.vue` reports 581 lines. Existing child props/events and E2E selectors are preserved. |
| T-FRONTEND-COMPONENTIZATION-011 | Completed | Added focused tests for every extracted state composable and kept `App.test.ts` as rendered orchestration/user-flow coverage. Full frontend lint, typecheck, unit, build, and E2E gates passed. |

Verification run on 2026-07-08:

- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run test -- --run` passed, 5 files / 30 tests.
- `npm --prefix frontend run build` passed.
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/full-stack-productization.spec.ts --project=chromium` passed, 2 tests.
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/full-stack-productization.spec.ts --project=chromium` passed after the Space tab-content extraction, 3 tests.
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-h-settings-administration.spec.ts --project=chromium` passed after settings extraction, 2 tests.
- `npm --prefix frontend run test -- --run src/composables/useProductUploadWorkflow.test.ts` passed after the mock upload workflow composable extraction, 1 file / 2 tests.
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts --project=chromium` passed after the mock upload workflow composable extraction, 2 tests.
- `npm --prefix frontend run e2e` passed, 16 tests.
- `npm run agent:check-sdd -- --slice frontend-componentization --report docs/00-context/frontend-componentization-sdd-completion-report.md` passed with the expected optional frontend-only API guide warning.
- `npm run agent:check-workflow -- --changed-slices` passed.
- `npm run agent:closeout` passed.
- `git diff --check` passed.
- Focused dependency, new-network/private-path, and secret scans passed.

Additional `frontend-state-extraction` verification run on 2026-07-08:

- `npm run agent:check-sdd -- --slice frontend-componentization` passed with the expected optional frontend-only API guide warning.
- Per-domain focused checks passed after extraction:
  - `npm --prefix frontend run test -- --run src/composables/useSpaces.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useBatches.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useReviewQueue.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useWikiPages.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useGraph.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useAsk.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useGlobalChat.test.ts` plus `npm --prefix frontend run typecheck`.
  - `npm --prefix frontend run test -- --run src/composables/useSettings.test.ts` plus `npm --prefix frontend run typecheck`.
- `npm --prefix frontend run lint` passed.
- `npm --prefix frontend run typecheck` passed.
- `npm --prefix frontend run test` passed, 14 files / 56 tests.
- `npm --prefix frontend run build` passed.
- `npm --prefix frontend run e2e` passed, 16 tests.
- `wc -l frontend/src/App.vue` reported 581 lines.
- `rg -n "\bany\b|as any" frontend/src` returned no matches.

Skipped:

- Backend/API/provider checks, because this frontend-only slice did not change backend endpoints, request/response payloads, persistence, adapter contracts, provider/runtime behavior, or deployment behavior.

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
