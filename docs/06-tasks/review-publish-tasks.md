# Tasks: Review Publish

## Status

Draft. Derived through `design-to-tasks`. Do not implement until this SDD is accepted.

## Verification Row

Phase 4 hardening: full unit + integration + E2E for the touched layer. Also run baseline checks: `git diff --check`, new-network/new-dependency scan, secret/private-path scan.

## Task Summary

| Task | Title | Owner | Priority | Depends On |
|---|---|---|---|---|
| T-REVIEW-PUBLISH-001 | Add publish-ready queue contract | backend | Must | None |
| T-REVIEW-PUBLISH-002 | Harden review transition tests | backend | Must | None |
| T-REVIEW-PUBLISH-003 | Implement publish eligibility service | backend | Must | T-REVIEW-PUBLISH-001 |
| T-REVIEW-PUBLISH-004 | Add Wiki publish endpoints | backend | Must | T-REVIEW-PUBLISH-003 |
| T-REVIEW-PUBLISH-005 | Add/adjust persistence for publish metadata | backend | Must | T-REVIEW-PUBLISH-003 |
| T-REVIEW-PUBLISH-006 | Wire frontend Processing Center publish states | frontend | Must | T-REVIEW-PUBLISH-001 |
| T-REVIEW-PUBLISH-007 | Wire frontend Wiki published metadata state | frontend | Must | T-REVIEW-PUBLISH-004 |
| T-REVIEW-PUBLISH-008 | Add E2E review-to-publish coverage | QA | Must | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-007 |
| T-REVIEW-PUBLISH-009 | Run security, adapter, and no-network gates | security | Must | T-REVIEW-PUBLISH-001..008 |

## Task Details

### T-REVIEW-PUBLISH-001: Add publish-ready queue contract

- **Maps to:** REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008; spec `Review Queue And Processing Center`.
- **Scope:** Add backend DTO/service/controller behavior for `GET /api/spaces/{spaceId}/review-queues` using metadata only, including safe representative item metadata for each queue.
- **Constraints:** No network calls; no direct adapter calls; preserve trace/confidence/review metadata.
- **Verification:** `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT'`

### T-REVIEW-PUBLISH-002: Harden review transition tests

- **Maps to:** REQ-REVIEW-PUBLISH-002, REQ-REVIEW-PUBLISH-003; spec `SME Review State Machine`.
- **Scope:** Ensure `APPROVE`, `NEED_FIX`, `OCR_REQUIRED` map correctly; prove `PUBLISHED` is not set by review action; verify append-only history.
- **Constraints:** User-safe errors; no secret/path leakage.
- **Verification:** `cd backend && mvn test -Dtest='DomainInvariantTest,MetadataApiContractIT'`

### T-REVIEW-PUBLISH-003: Implement publish eligibility service

- **Maps to:** REQ-REVIEW-PUBLISH-004, REQ-REVIEW-PUBLISH-006, REQ-REVIEW-PUBLISH-010; spec `Publish Eligibility`.
- **Scope:** Add service logic that validates approved status, relative Markdown path, confidence, source document ids, source trace coverage, and blocked statuses before mutation.
- **Constraints:** Metadata-only; no parser/converter/model/vector/storage/search direct calls.
- **Verification:** `cd backend && mvn test -Dtest='ReviewPublishServiceTest'`

### T-REVIEW-PUBLISH-004: Add Wiki publish endpoints

- **Maps to:** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-007; spec `API Behavior`.
- **Scope:** Implement `POST /api/files/{fileId}/publish`, `GET /api/spaces/{spaceId}/wiki-pages`, and `GET /api/wiki-pages/{wikiPageId}` per API guide.
- **Constraints:** Use envelope; user-safe errors; relative paths only; secrets masked/status-only.
- **Verification:** `cd backend && mvn test -Dtest='ReviewPublishApiContractIT'`

### T-REVIEW-PUBLISH-005: Add/adjust persistence for publish metadata

- **Maps to:** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-006; spec `Publish Result`.
- **Scope:** Add repositories/mappers/migrations only if existing `wiki_page` metadata is insufficient; otherwise document no migration needed in implementation notes.
- **Constraints:** Do not mutate raw parser output or source chunks; no real data.
- **Verification:** `cd backend && mvn verify`

### T-REVIEW-PUBLISH-006: Wire frontend Processing Center publish states

- **Maps to:** REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009; spec `Review Queue And Processing Center`.
- **Scope:** Render blocked queues, ready-to-publish count, and publish action states from typed mock/API-shaped data.
- **Constraints:** No external network calls unless using accepted local API adapter; no production RBAC claims.
- **Verification:** `cd frontend && npm run test -- --run`

### T-REVIEW-PUBLISH-007: Wire frontend Wiki published metadata state

- **Maps to:** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009; spec `Publish Result`.
- **Scope:** Show published status, source/confidence metadata, and trust copy in Wiki/Global Chat surfaces.
- **Constraints:** Preserve FE baseline; do not implement graph extraction or Ask generation.
- **Verification:** `cd frontend && npm run typecheck && npm run build`

### T-REVIEW-PUBLISH-008: Add E2E review-to-publish coverage

- **Maps to:** REQ-REVIEW-PUBLISH-011; spec `Acceptance Matrix`.
- **Scope:** Cover approve-to-publish happy path and blocked missing-trace path through UI/API-visible behavior.
- **Constraints:** Mock/sample data only; no external services.
- **Verification:** `cd frontend && npm run e2e`

### T-REVIEW-PUBLISH-009: Run security, adapter, and no-network gates

- **Maps to:** REQ-REVIEW-PUBLISH-007, REQ-REVIEW-PUBLISH-010, REQ-REVIEW-PUBLISH-011; spec `Non-Functional Requirements`.
- **Scope:** Confirm no direct engine calls from publish logic, no new external dependencies/network calls, no raw secrets/private paths/real data, and diff hygiene.
- **Constraints:** Adapter boundary and secret masking are mandatory.
- **Verification:** `git diff --check`; `rg -n "https?://|fetch\\(|axios|XMLHttpRequest" backend/src frontend/src prototypes/index.html frontend/public/atlas-prototype.html`; `rg -n "(api[_-]?key|secret|password|token|/Users/|C:\\\\|BEGIN (RSA|OPENSSH|PRIVATE))" . -g '!frontend/node_modules/**' -g '!backend/target/**'`

## Dependency Plan

Critical path: T-REVIEW-PUBLISH-001 -> T-REVIEW-PUBLISH-003 -> T-REVIEW-PUBLISH-004 -> T-REVIEW-PUBLISH-007 -> T-REVIEW-PUBLISH-008 -> T-REVIEW-PUBLISH-009.

T-REVIEW-PUBLISH-002 can run in parallel with T-REVIEW-PUBLISH-001. T-REVIEW-PUBLISH-006 can begin after the queue DTO contract is stable.

## Definition Of Done

- All Must tasks complete.
- `cd backend && mvn verify` passes.
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e` passes.
- `git diff --check`, network/dependency scan, and secret/private-path scan pass.
- Any skipped check is named with a reason.

## Open Questions

- Resolved for implementation: duplicate publish is idempotent and updates/returns existing Wiki metadata.
- Resolved for implementation: publish sets only `wiki_page.review_status=PUBLISHED`; `file_item` and `source_chunk` review metadata remain unchanged.

## Implementation Evidence

Updated on 2026-07-03 after implementation verification.

| Task | Status | Evidence |
|---|---|---|
| T-REVIEW-PUBLISH-001 | Complete | Added review queue DTO/service/controller behavior with safe representative items; `mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT,DomainInvariantTest'` passed. |
| T-REVIEW-PUBLISH-002 | Complete | Added review publish service tests covering review action mapping and publish separation; targeted backend tests passed. |
| T-REVIEW-PUBLISH-003 | Complete | Added publish eligibility service with approved status, safe relative Markdown path, confidence, source trace, and blocked-status checks. |
| T-REVIEW-PUBLISH-004 | Complete | Added publish and Wiki page endpoints per API guide. |
| T-REVIEW-PUBLISH-005 | Complete | Reused existing `wiki_page` table; no migration needed because required fields already exist. |
| T-REVIEW-PUBLISH-006 | Complete | Added typed frontend review queue mock data and Processing Center publish state hooks. |
| T-REVIEW-PUBLISH-007 | Complete | Added published Wiki metadata mock data and visible source/confidence/published status in the prototype. |
| T-REVIEW-PUBLISH-008 | Complete | Added Playwright E2E coverage for ready-to-publish and missing-trace blocked states; `npm run e2e` passed. |
| T-REVIEW-PUBLISH-009 | Complete | `git diff --check`, network scan, secret/private-path scan, backend targeted tests, frontend typecheck, frontend unit tests, E2E, full `cd backend && mvn verify`, and `cd frontend && npm run build` passed after ask/knowledge-graph and `App.vue` lint blockers were resolved. |
