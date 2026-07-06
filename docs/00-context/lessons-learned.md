# Lessons Learned

This file captures reusable lessons from acceptance review, implementation review, verification failures, and user feedback.

The purpose is not to keep a diary. Each lesson should prevent a repeated mistake by updating the right rule, SDD artifact, checklist, test, or implementation pattern.

## Learning Loop

When an outcome differs from expectation:

1. Capture the expectation gap.
2. Identify whether the root cause was goal ambiguity, missing requirement, weak spec, incomplete design, implementation drift, missing verification, or review blind spot.
3. Update the durable artifact that would have prevented the issue.
4. Add or update verification so the issue is checked next time.
5. Link the lesson to the affected slice, requirement, task, or quality gate.

## Where To Apply A Lesson

| Lesson Type | Durable Home |
|---|---|
| Product behavior was unclear | `docs/01-requirements/` and `docs/03-spec/` |
| User story or acceptance was missing | `docs/02-user-stories/` |
| Architecture or adapter boundary was wrong | `docs/04-architecture/` or an ADR when needed |
| UI behavior or component expectation was unclear | `docs/05-design/` |
| Work was missed during execution | `docs/06-tasks/` |
| Verification missed a defect | Task verification, `DEVELOPMENT_STANDARDS.md`, or future test coverage |
| Agent repeated a workflow mistake | `AGENTS.md` or `PROJECT_RULES.md` |
| Standard applies across slices | `DEVELOPMENT_STANDARDS.md` |
| Slice status drifted after implementation | `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, and `docs/00-context/{slice}-traceability.md` |

## Entry Template

```text
ID: LL-YYYYMMDD-###
Date:
Slice:
Source:
Expectation:
Observed:
Root cause:
Decision:
Durable updates:
New verification:
Status:
```

## Lessons

### LL-20260707-002 Space-Scoped Slugs Need Space-Scoped Generated IDs

ID: LL-20260707-002
Date: 2026-07-07
Slice: wiki-ingest-v0
Source: Atlas closeout review for Wave 3 / `wiki-ingest-v0`.
Expectation: Same-slug Auto Wiki candidates in different spaces must not conflict; the spec's space-scoped slug rule must hold through persistence identifiers, not only repository lookup.
Observed: The implementation looked up existing pages by `(spaceId, slug)` but generated new page IDs as `wiki-auto-{slug}`, so the same slug in another space could collide on the global `wiki_page.id`.
Root cause: Implementation drift at the persistence identifier boundary. Verification covered idempotent reruns and trusted slug collisions but did not include a positive cross-space same-slug case.
Decision: Generated Auto Wiki page IDs must include a normalized space slug when page IDs are globally unique.
Durable updates: Updated `WikiIngestService` page ID generation, added `WikiIngestServiceTest.sameCandidateSlugInDifferentSpaceGetsDistinctPageId`, and refreshed wiki-ingest-v0 task/traceability evidence.
New verification: `cd backend && mvn -q -Dtest=WikiIngestServiceTest,ReviewPublishServiceTest -DfailIfNoTests=false test` now covers the cross-space same-slug case.
Status: Applied.

### LL-20260707-001 Auth/RBAC Closeout Must Include Full Verify And Role E2E

ID: LL-20260707-001
Date: 2026-07-07
Slice: auth-space-rbac
Source: Atlas closeout review for Wave 3 / `auth-space-rbac`.
Expectation: A Must-level auth/RBAC slice can be marked complete only when API contracts, completed task IDs, traceability, roadmap status, role-specific E2E, and full backend verification all agree.
Observed: The slice was implemented, but T-AUTH-SPACE-RBAC-009 remained Partial, traceability still carried a dedicated role E2E gap, the roadmap still described the slice as SDD-only, `mvn verify` had not been proven green, and the membership update API drifted from the API guide's `PATCH /api/spaces/{spaceId}/members/{membershipId}` contract.
Root cause: Closeout review blind spot. Focused backend/frontend checks passed, but the full verification gate and status-document reconciliation were not treated as blocking evidence before claiming completion.
Decision: Auth/RBAC closeout must run `mvn verify`, include dedicated role E2E in the default frontend E2E suite, and assert membership update/delete by membership id before marking the slice complete.
Durable updates: Updated `docs/06-tasks/auth-space-rbac-tasks.md` / `.zh-CN.md`, traceability, slice roadmap, repo-status roadmap, and product roadmap to remove stale E2E-gap/SDD-only language. Added `frontend/tests/e2e/auth-space-rbac.spec.ts` and backend contract coverage for membership-id PATCH.
New verification: `cd backend && mvn verify`; `cd backend && mvn test -Dtest=AuthorizationServiceTest,AuthSpaceRbacApiContractIT`; `npm --prefix frontend run e2e`; `npm run agent:check-sdd -- --slice auth-space-rbac --require-api-guide`; `npm run agent:closeout`.
Status: Applied.

### LL-20260706-001 Closeout Must Reconcile Spec Claims, Task Evidence, And Canonical Status

ID: LL-20260706-001
Date: 2026-07-06
Slice: audit-log-foundation
Source: Closeout review found that task and traceability docs claimed implementation completion while the canonical repo-status entry still described the slice as an SDD draft, and the original spec/task language required broader emitters than the implemented foundation covered.
Expectation: A slice can be marked complete only when implementation behavior, spec language, completed task IDs, traceability, roadmap/progress status, and verification evidence describe the same scope.
Observed: `audit-log-foundation` implemented a prototype foundation with auth, membership, Wiki publish, Graph, Ask, read APIs, and Vue audit UI, but the SDD still implied Wiki ingest/linkify-lint and adapter/runtime emitters were part of the completed scope. The canonical repo-status file still said the slice was waiting for acceptance.
Root cause: Review blind spot and status drift. The closeout gate verified file presence and workflow hygiene but did not by itself prove semantic alignment between the implementation, completed task language, and the one-page repo status.
Decision: For governance slices, closeout must either implement every claimed emitter/behavior or update the SDD/task language before marking the task complete. Canonical status files must be updated in the same change as traceability.
Durable updates: Updated `docs/03-spec/audit-log-foundation-spec.md` / `.zh-CN.md`, `docs/05-design/audit-log-foundation-design.md` / `.zh-CN.md`, `docs/06-tasks/audit-log-foundation-tasks.md` / `.zh-CN.md`, traceability, `docs/00-context/repo-status-roadmap.zh-CN.md`, and `docs/00-context/product-goal-progress.zh-CN.md` to distinguish implemented foundation coverage from deferred emitters. Added focused backend/frontend tests for metadata allowlist, time-range filters, invalid time intervals, private-path literal hygiene, and governance-read UI gating.
New verification: During closeout review, compare implemented emitters and API filters against FR/AC rows, then check `docs/00-context/{slice}-traceability*`, `docs/00-context/slice-roadmap*`, `docs/00-context/repo-status-roadmap.zh-CN.md`, and progress docs for matching status language before claiming complete.
Status: Applied.

### LL-20260705-004 SDD Handoffs Must Force Project-Local Skill Usage

ID: LL-20260705-004
Date: 2026-07-05
Slice: wiki-foundation / SDD handoff
Source: User feedback that the downstream Codex prompt generated or guided SDD work without explicitly using the project-local `.agents/skills` SDD workflow.
Expectation: Full Atlas SDD generation must use `.agents/skills/atlas-sdd-generate-all/SKILL.md` and the project-local SDD skill chain: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality`. If a slice materially changes architecture, API contracts, persistence, security, adapter boundaries, or data flow, `architecture-review` must also be used.
Observed: The goal handoff plan said to generate or update complete bilingual SDD documents, but did not explicitly name the required `.agents/skills` files, did not require the downstream agent to report skill-chain usage, and did not define skipping the skill chain as a stop condition.
Root cause: Handoff prompt weakness and missing verification gate, not a missing project rule. `PROJECT_RULES.md`, `docs/SDD-BOOTSTRAP.md`, and `.agents/skills/atlas-sdd-generate-all/SKILL.md` already require the skill chain, but the copied Codex prompt and execution manifest did not surface that rule strongly enough for a separate execution thread.
Decision: Any future agent handoff that asks for Atlas SDD generation must pin the project-local skill files as required inputs, explicitly instruct the agent to use the skill chain before writing SDD artifacts, and require the completion report to state `SDD skill chain used: yes/no` with the list of skill files read.
Durable updates: Updated `PROJECT_RULES.md` so the SDD gate requires skill-chain evidence. Updated `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` with a mandatory SDD skill usage section and stronger Master/Single Slice prompts. Updated `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` with required skill files, SDD generation policy, quality gate, completion report fields, and stop conditions.
New verification: Before accepting future generated SDD docs, check that the completion report lists the project-local skill chain and `review-doc-quality` result. If the skill chain is absent, treat the SDD handoff as incomplete even if the Markdown files exist.
Status: Applied.

### LL-20260705-003 Test-Ready Must Not Be Claimed As Product-Ready

ID: LL-20260705-003
Date: 2026-07-05
Slice: product acceptance / local run readiness
Source: User feedback after Atlas was described as ready to test while the next-phase plan still listed core product gaps.
Expectation: The user expects an out-of-the-box product, not only a green engineering baseline. When the agent says the product can be tested or tried, that claim must mean a first-time user can run the local product and complete the intended product workflow, or the agent must clearly label the scope as mock, prototype, API-backed demo, or regression-only.
Observed: The agent treated completed Phase A-J verification and passing E2E gates as enough to say Atlas could be tested. A manual run showed that the default frontend dev command fell back to mock data when the backend API was not configured on the expected port, the upload buttons opened a mock review panel rather than a real file picker or ingest flow, and the browser-only path did not automatically connect publish, graph projection, vector indexing, and Ask into one product workflow.
Root cause: Review blind spot and communication drift. Automated verification was interpreted as product readiness without a fresh manual "open the box" run through the primary user workflow. The status language did not separate test readiness, API-backed demo readiness, internal beta readiness, and product readiness.
Decision: Never use broad phrases such as "ready to test", "can be tried", "usable", "accepted", or "done" without naming the readiness level and the verified workflow. A test gate passing only proves that gate. Product-ready language requires a manual first-run smoke through the real user-facing surface and an explicit list of any mock-only, scripted-only, or missing core capabilities.
Durable updates: Updated `PROJECT_RULES.md` quality gates with a product readiness claim gate. Updated `DEVELOPMENT_STANDARDS.md` verification standards so product-readiness statements require a local first-run smoke and must distinguish automated test readiness from out-of-the-box usability.
New verification: Before telling the user a product is ready to test or try, run or explicitly decline a local product smoke: start the documented services from a clean command path, open the primary UI, confirm there is no unexpected `Failed to fetch` or fallback mode, complete the core user workflow from UI controls only, and report which parts are real, mock-only, API-scripted, or blocked. If the workflow cannot be completed from the UI, the final status must say "not product-ready" even when automated tests pass.
Status: Applied.

### LL-20260705-002 Roadmap Done Must Not Mean Product Accepted

ID: LL-20260705-002
Date: 2026-07-05
Slice: product roadmap / Vue productization
Source: User feedback after reviewing the real Vue frontend direction and earlier completed slice roadmap.
Expectation: The roadmap should explain progress toward the user's actual target: an enterprise-grade internal knowledge-base management product inspired by modern AI-native knowledge products, with real Vue product pages for Knowledge Spaces, upload/batch workflow, Processing Center, LM Wiki, Graph, Ask, and settings.
Observed: The existing slice roadmap showed many slices as implemented or done, but those statuses often meant SDD completion, mock adapter contract completion, static prototype behavior, or adjacent workbench/API implementation. The real Vue user-facing product did not yet match the accepted prototype or the user's product goal, so the product still felt incomplete despite many green roadmap items.
Root cause: Roadmap status mixed task closure with product maturity. It did not clearly separate prototype, Vue parity, API-backed UI, internal beta readiness, and production readiness. Verification also sometimes accepted adjacent/debug surfaces instead of the primary user-facing product surface.
Decision: Maintain `ROADMAP.md` as the product-maturity roadmap and use `docs/00-context/slice-roadmap.md` only for SDD/task execution status. Roadmap entries must state the product maturity level and must not be called product-complete without real user-facing Vue evidence.
Durable updates: Replaced `ROADMAP.md` with a product-focused roadmap, current-state snapshot, retrospective, maturity scale, corrected phases, near-term execution plan, and roadmap rules. Updated `DEVELOPMENT_STANDARDS.md` so roadmap status must distinguish task completion from product acceptance.
New verification: Future roadmap close-out must report whether the work is L1 prototype, L2 Vue parity, L3 API-backed, L4 internal beta, or L5 production-ready, and must include screenshot or E2E evidence for the actual user-facing Vue surface.
Status: Applied.

### LL-20260705-001 Sample-Driven UI Fixes Must Cover The Full Interaction Chain

ID: LL-20260705-001
Date: 2026-07-05
Slice: model-settings prototype
Source: User acceptance feedback on the model management sample parity fix.
Expectation: When a user provides a UI sample for a prototype behavior, the implementation should match the complete visible interaction pattern, including entry points, menus, item selection, edit surfaces, save/cancel behavior, list updates, and visual layout.
Observed: The first fix addressed the visible Add Model button, dropdown, tab counts, and card styling, but missed the sample's card-click edit drawer. The user had to point out that model cards still could not be edited.
Root cause: Review blind spot and incomplete verification. The implementation treated the user's text request as isolated button/list behavior instead of using the sample screenshot as the acceptance target for the whole model-management workflow.
Decision: For sample-driven prototype work, infer and verify the full interaction chain represented by the sample before close-out, not only the most explicit textual defect.
Durable updates: Updated `DEVELOPMENT_STANDARDS.md` Phase 0 frontend verification guidance so sample-driven UI changes require a manual or automated interaction checklist covering trigger, navigation/open state, edit/input state, save/cancel or close behavior, visible state update, and responsive layout parity.
New verification: Future sample-driven UI prototype fixes must include evidence for the complete interaction chain; for model settings specifically, verify Add Model menu, category tabs, model card click, edit drawer, save/cancel, and list update behavior.
Status: Applied.

### LL-20260703-008 Queue Response Shape Must Be Contract-Tested

ID: LL-20260703-008
Date: 2026-07-03
Slice: review-publish
Source: Code-against-design review.
Expectation: `GET /api/spaces/{spaceId}/review-queues` must return publish-blocking queue counts and representative items so Processing Center users can triage blocked work without opening every file.
Observed: The implementation returned queue counts/categories only. API guide examples and tests also omitted representative item shape, so the mismatch stayed green.
Root cause: The API contract test asserted category presence but not the full response shape promised by the spec.
Decision: Review queue responses now include bounded safe representative item metadata and contract tests assert representative item fields.
Durable updates: Updated `ReviewQueueItemResponse`, added `ReviewQueueRepresentativeResponse`, updated `ReviewPublishService`, backend API/service tests, frontend mock types/tests, `docs/05-design/review-publish-design.md`, `docs/05-design/review-publish-design.zh-CN.md`, `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.md`, `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.zh-CN.md`, `docs/06-tasks/review-publish-tasks.md`, and `docs/06-tasks/review-publish-tasks.zh-CN.md`.
New verification: `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT'` asserts representative item shape; `cd frontend && npm run test -- --run` asserts frontend queue mocks include and clone representative items.
Status: Applied.

### LL-20260703-007 Frontend Acceptance Must Target The Real Slice Surface

ID: LL-20260703-007
Date: 2026-07-03
Slice: knowledge-graph
Source: Code-against-design review.
Expectation: Spec S5, T-KG-010, and T-KG-011 require the Knowledge Space Graph tab itself to become API-backed, preserve search/filter/selection/evidence interactions, and have E2E coverage for Graph tab behavior and error states.
Observed: The implementation added an API-backed host-level graph hardening panel while the iframe Graph tab remained prototype/mock-only. Existing E2E opened the Graph tab and checked the SVG, but did not verify API-backed Graph tab search/filter, selected evidence detail, unauthorized state, or empty state.
Root cause: Verification accepted an adjacent shell-level panel and a smoke check as evidence for the actual Graph tab surface.
Decision: Treat adjacent host panels as transitional aids only. A slice frontend task is complete only when tests exercise the actual user-facing surface named in the spec/design.
Durable updates: Updated `frontend/src/App.vue`, `frontend/src/styles.css`, `frontend/src/App.test.ts`, `frontend/tests/e2e/knowledge-graph.spec.ts`, `docs/05-design/knowledge-graph-design.md`, `docs/05-design/knowledge-graph-design.zh-CN.md`, `docs/06-tasks/knowledge-graph-tasks.md`, `docs/06-tasks/knowledge-graph-tasks.zh-CN.md`, and knowledge-graph traceability so the real `[data-tab="graph"]` surface is API-backed and tested.
New verification: `frontend/tests/e2e/knowledge-graph.spec.ts` now asserts API-backed data, search/filter, node and edge selection, evidence detail, unauthorized state, and empty state inside `[data-tab="graph"]`; host-level panels or smoke-only prototype SVG checks are not sufficient evidence.
Status: Applied.

### LL-20260703-006 Ask Review Policy And No-Evidence Behavior Need Contract Assertions

ID: LL-20260703-006
Date: 2026-07-03
Slice: ask-rag
Source: Code-against-design review.
Expectation: `APPROVED_ONLY` Ask runs must not surface review-required evidence, even if a downstream retrieval service or mock returns it, and `NO_EVIDENCE` responses must include a safe no-answer message while skipping model generation.
Observed: The initial AskService relied on VectorService filtering and returned `answer=null` for `NO_EVIDENCE`; tests did not assert the defensive policy boundary or the no-answer text required by the API guide.
Root cause: Verification focused on vector policy propagation and model-call skipping, but missed the response payload contract and a defense-in-depth check at the Ask orchestration boundary.
Decision: AskService now defensively filters returned evidence by Ask review policy and completes no-evidence runs with a safe no-answer message.
Durable updates: Updated `docs/06-tasks/ask-rag-tasks.md`, `docs/06-tasks/ask-rag-tasks.zh-CN.md`, `AskServiceTest`, and `AskApiContractIT` to enforce the policy and no-evidence response contract.
New verification: `cd backend && mvn -Dtest=AskServiceTest,AskSummaryCalculatorTest test` and `cd backend && mvn -Dit.test=AskApiContractIT verify` now assert the corrected behavior.
Status: Applied.

### LL-20260703-004 Status Mapping Acceptance Needs Direct Test Evidence

ID: LL-20260703-004
Date: 2026-07-03
Slice: model-adapter
Source: Code-against-design review.
Expectation: AC-MODA-08 and T-MODA-006 require model run status and summary behavior to be verified for success, partial failure, failure, and unavailable/misconfigured adapters.
Observed: The implementation contained `PARTIAL_FAILED` and unavailable adapter handling, but the first test set did not directly assert those two acceptance paths. The API guide also described `mode` as optional while the spec and implementation require it.
Root cause: Verification focused on happy path, invalid output, and adapter fault, leaving status-matrix coverage implicit rather than explicit.
Decision: Treat every acceptance-matrix state as needing a direct test assertion, not only implementation logic.
Durable updates: Added `ModelServiceTest.unavailableAdapterCompletesFailedWithoutExecution` and `ModelServiceTest.partialOutputFailuresMapToPartialFailedSummary`; clarified `mode` as required in both API guides; updated T-MODA-006 task verification notes.
New verification: `cd backend && mvn -Dtest=ModelServiceTest,ModelSummaryCalculatorTest test` now covers success, partial failure, failed adapter fault, failed unavailable adapter, and invalid output rejection.
Status: Applied.

### LL-20260703-003 Explicit Inclusion Policies Need Positive And Default Tests

ID: LL-20260703-003
Date: 2026-07-03
Slice: vector-adapter
Source: Code-against-design review.
Expectation: `INCLUDE_REVIEW_REQUIRED` should include review-required source chunks for vector indexing, while the default `APPROVED_ONLY` policy should skip them.
Observed: The initial vector index implementation always filtered to approved/published chunks, so explicit review-required inclusion worked for query but not for index.
Root cause: Verification covered the default approved-only index path and review-aware query path, but missed the positive index case for `INCLUDE_REVIEW_REQUIRED`.
Decision: Make index eligibility policy-aware and preserve the review-required label in indexed item evidence.
Durable updates: Updated `VectorService` index eligibility and added `VectorServiceTest.indexRunIncludesReviewRequiredWhenPolicyAllowsIt`.
New verification: `cd backend && mvn -Dtest=VectorServiceTest test` now asserts both default skip and explicit inclusion behavior for index runs.
Status: Applied.

### LL-20260703-005 Adapter Evidence Validity Must Cover Missing Scores

ID: LL-20260703-005
Date: 2026-07-03
Slice: vector-adapter
Source: Code-against-design review.
Expectation: Similarity query matches must carry scores in the inclusive range `0.000` to `1.000`; missing scores are invalid adapter output.
Observed: The initial query score validator allowed `null` scores, leaving sorting/response behavior under-specified for malformed adapter matches.
Root cause: Score validation was shared with index item evidence, where `score` may legitimately be null, instead of using stricter query-match validation.
Decision: Keep nullable score for index/deindex item results, but require non-null score for query matches.
Durable updates: Updated `VectorService.safeSortedMatches` to require query scores and added `VectorQueryServiceTest.queryRejectsMissingAdapterScoreAsInvalidEvidence`.
New verification: `cd backend && mvn -Dtest=VectorQueryServiceTest test` now fails on missing query scores.
Status: Applied.

### LL-20260702-001 Settings Should Be Shell-Level, Not A Main Content Route

ID: LL-20260702-001
Date: 2026-07-02
Slice: knowledge-space
Source: Prototype acceptance feedback.
Expectation: Settings should follow the preferred WeKnora-like product pattern: common settings are discoverable from the persistent sidebar, and All Settings opens as a modal/sheet over the current product view.
Observed: The prototype treated Settings as a normal main view, replacing the Home or Knowledge Space content.
Root cause: The earlier SDD docs specified settings content but did not define settings as a shell-level overlay pattern.
Decision: Settings is now specified as a sidebar-driven shell surface. Common shortcuts open specific settings panels, and All Settings opens a modal/sheet without replacing the current view.
Durable updates: Updated `REQ-KS-022`, `Sidebar And Settings Shell`, `Settings Shell` design guidance, traceability, and task `T-KS-026`.
New verification: Verify settings shortcuts open the correct modal panel, All Settings opens over the current view, and close returns to the previous view without resetting state.
Status: Applied.

### LL-20260703-002 API Guide Response Shape Must Be Contract-Tested

ID: LL-20260703-002
Date: 2026-07-03
Slice: storage-adapter
Source: Code-against-design acceptance review.
Expectation: `GET /api/batches/{batchId}/storage-objects` must follow the storage adapter API guide and return continuation-style metadata with `pageSize` and `nextPageToken`.
Observed: The endpoint reused the generic list pagination metadata shape (`page`, `size`, `total`), so the implementation did not match the storage-adapter design contract even though the data listing behavior worked.
Root cause: The implementation reused an existing envelope helper without a storage-adapter-specific contract assertion for continuation metadata.
Decision: Keep generic offset pagination for existing list endpoints, add continuation metadata support for storage object listing, and assert the storage API guide response shape in the storage API contract test.
Durable updates: Updated `PageMeta`, `StorageController`, and `StorageApiContractIT` so the API guide shape is enforced by tests.
New verification: `StorageApiContractIT` now creates more than one stored object and verifies `meta.pageSize` plus `meta.nextPageToken` on the storage object list endpoint.
Status: Applied.

### LL-20260703-001 Keep `docs/00-context` Status In The Close-Out Gate

ID: LL-20260703-001
Date: 2026-07-03
Slice: knowledge-space, folder-upload
Source: User acceptance feedback after the Knowledge Space IA refinement commit.
Expectation: When implementation, prototype acceptance, or task completion status changes, the `docs/00-context` status files should be updated in the same change as spec/design/tasks/code so the next slice decision starts from current state.
Observed: The Knowledge Space IA refinement updated spec, design, tasks, prototype, and tests, but initially left `docs/00-context/knowledge-space-traceability.md` and `docs/00-context/slice-roadmap.md` with stale task/status language. Folder Upload traceability also still described the slice as draft after implementation had completed.
Root cause: The quality gates required traceability in principle, but the close-out checklist did not explicitly require a `docs/00-context` status audit after implementation or acceptance status changed.
Decision: Add a Context status gate to project rules and add a Phase 0/1 verification check requiring traceability and slice-roadmap status to be current before close-out.
Durable updates: Updated `PROJECT_RULES.md` quality gates, `DEVELOPMENT_STANDARDS.md` goal/verification standards, `docs/00-context/slice-roadmap.md`, `docs/00-context/slice-roadmap.zh-CN.md`, `docs/00-context/knowledge-space-traceability.md`, `docs/00-context/knowledge-space-traceability.zh-CN.md`, and folder-upload traceability files.
New verification: For future slice work, search `docs/00-context` for stale status markers such as old task ranges, `impl pending`, `待实现`, `Implementation: Not started`, or draft status after implementation; final reports must say whether `docs/00-context` was checked or updated.
Status: Applied.
