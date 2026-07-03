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

### LL-20260703-004 Adapter Evidence Validity Must Cover Missing Scores

ID: LL-20260703-004
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
