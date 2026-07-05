# Implementation Tasks: Knowledge Graph

## Status

Implemented. Phase 4 hardening. Verification evidence is recorded in `docs/00-context/knowledge-graph-traceability.md`.

## Verification Row

From `docs/00-context/slice-roadmap.md`: Phase 4 hardening requires full unit + integration + E2E for the touched layer; preserve source trace, confidence, review status on all Markdown/metadata; LLM output stays review-required until verified; API guide is required where new endpoints are introduced.

## Task Details

| ID | Task | Owner | Priority | Depends on | Maps to | Verification |
|---|---|---|---:|---|---|---|
| T-KG-001 | Confirm entry gate: Phase 2 metadata API and required Phase 3 adapters are implemented; if `review-publish` is unavailable, create approved/published test fixtures only and record dependency. No product behavior may bypass review status. | backend | Must | None | REQ-KG-001 / Spec S1 | Static review of docs and fixtures; `git diff --check`. |
| T-KG-002 | Add additive Flyway migration and entities/repositories for projection runs, projection items, audit records, and additive graph evidence fields. Use mock/sample seed only. | backend | Must | T-KG-001 | REQ-KG-004, REQ-KG-010 / Spec S3, S6 | `cd backend && mvn verify`; Flyway migration validation in integration tests. |
| T-KG-003 | Implement graph projection eligibility service: approved/published-only, source trace required, confidence/review preservation, safe reason codes for skipped items. | backend | Must | T-KG-002 | REQ-KG-001, REQ-KG-009 / Spec S1 | Unit tests for every exclusion code; `cd backend && mvn verify`. |
| T-KG-004 | Implement product-facing `GraphProjectionAdapter` and deterministic adapter; add seam guard so graph/model/vector engine names are allowed only behind adapter/worker boundary. | backend | Must | T-KG-003 | REQ-KG-007, REQ-KG-011 / Spec S2 | Adapter contract tests and seam guard tests via `cd backend && mvn verify`. |
| T-KG-005 | Implement `GraphProjectionService` with idempotent node/edge creation, partial failure handling, sanitized errors, run/item summaries, and audit records. | backend | Must | T-KG-004 | REQ-KG-004, REQ-KG-010, REQ-KG-011 / Spec S2, S6 | Unit + integration tests via `cd backend && mvn verify`. |
| T-KG-006 | Implement graph DTOs and query service for bounded graph views, node details, filters, counts, evidence summaries, and no raw body/secret/path leakage. | backend | Must | T-KG-005 | REQ-KG-002, REQ-KG-003, REQ-KG-005, REQ-KG-011 / Spec S3, S4 | API/service tests via `cd backend && mvn verify`. |
| T-KG-007 | Implement graph endpoints from the API guide with backend auth/RBAC, validation, safe `400/401/403/404` envelopes, and audit for access failures. | backend/security | Must | T-KG-006 | REQ-KG-005, REQ-KG-006, REQ-KG-010 / Spec S4, S6 | Contract tests via `cd backend && mvn verify`. |
| T-KG-008 | Implement graph edge review action endpoint and append-only review/audit behavior. | backend | Must | T-KG-007 | REQ-KG-004, REQ-KG-010 / Spec S4, S6 | Integration tests via `cd backend && mvn verify`. |
| T-KG-009 | Add frontend graph API client/store/types with typed envelope handling, loading/error/unauthorized/empty states, no external network/CDN dependency, and no raw secret fields. | frontend | Must | T-KG-007 | REQ-KG-005, REQ-KG-008, REQ-KG-011 / Spec S5 | `cd frontend && npm run typecheck && npm run test`. |
| T-KG-010 | Replace mock-only Graph tab data path with API-backed graph view while preserving prototype canvas, search/filter, legend, hover/focus, selected detail, and evidence panel behavior. | frontend | Must | T-KG-009 | REQ-KG-008, REQ-KG-004 / Spec S5 | `cd frontend && npm run typecheck && npm run test && npm run build`. |
| T-KG-011 | Add frontend E2E for opening Graph, filtering/searching, selecting evidence-backed node/edge, seeing source trace/confidence/review status, and handling unauthorized/empty states. | QA/frontend | Must | T-KG-010 | REQ-KG-008, REQ-KG-012 / Spec Acceptance Matrix | `cd frontend && npm run e2e`; `npm run e2e:loop:mock`. |
| T-KG-012 | Run security/data scans: no new external network calls/dependencies, no raw secrets/private paths, no raw vectors/prompts/confidential bodies in graph responses or frontend fixtures. | security | Must | T-KG-011 | REQ-KG-006, REQ-KG-011, REQ-KG-012 / Spec S6 | `git diff --check`; `rg -n "(api[_-]?key|password|secret|token|/Users/|/home/|jdbc:|https?://)" docs backend frontend prototypes`. |
| T-KG-013 | Complete close-out review: verify SDD/API/tasks alignment, run full backend/frontend checks, and update traceability with implemented evidence and deferred work. | QA | Must | T-KG-012 | REQ-KG-012 / Spec Acceptance Matrix | `cd backend && mvn verify`; `cd frontend && npm run typecheck && npm run test && npm run build && npm run e2e`; `npm run e2e:loop:mock`; `git diff --check`. |

## Dependency Plan

Critical path: T-KG-001 -> T-KG-002 -> T-KG-003 -> T-KG-004 -> T-KG-005 -> T-KG-006 -> T-KG-007 -> T-KG-009 -> T-KG-010 -> T-KG-011 -> T-KG-012 -> T-KG-013.

T-KG-008 can run after T-KG-007 in parallel with frontend work.

## Constraints For Codex

- Full stack Phase 4 hardening, but no direct external graph/model/vector/parser/storage engine calls.
- Preserve source trace, confidence, and review status on all Markdown/metadata.
- LLM output remains review-required until verified.
- Secrets are masked/status-only; raw credentials, private paths, internal endpoints, raw vectors, and raw prompts are forbidden.
- Use mock/sample data only.

## Open Questions

- OQ-KG-001 through OQ-KG-003 from requirements remain visible; if they block implementation, stop and report the mismatch rather than redesigning silently.

## Code-Against-Design Review Addendum

Review date: 2026-07-03.

- Backend acceptance corrections applied: missing-source-trace candidates now skip with `MISSING_SOURCE_TRACE`; invalid graph edge review bodies return `400` validation envelopes; space-scoped graph auth failures write `ACCESS_DENIED` audit records.
- Frontend acceptance correction applied: the Vue `[data-tab="graph"]` surface now owns the API-backed graph data path with search, node/edge filters, evidence-only toggle, SVG canvas, legend, hover/focus affordances, node/edge selection, source-trace evidence detail, empty state, fallback state, and unauthorized state.
- Verification correction applied: `frontend/tests/e2e/knowledge-graph.spec.ts` asserts API-backed graph data, search/filter behavior, node and edge selection, source trace/confidence/review status, unauthorized state, and empty state on the real `[data-tab="graph"]` surface.
- Follow-up prevention: future T-KG-010/T-KG-011 close-out must keep E2E assertions on the actual spec-named user-facing surface, not on an adjacent transition panel or smoke-only prototype SVG check.

## Product Goal Batch 3 Task Addendum

| ID | Status | Evidence |
|---|---|---|
| T-KG-014 | Complete | Product Goal Batch 3 added the real Vue Knowledge Space Graph product surface with graph canvas, node types, search, legend, node selection, detail panel, confidence, review status, and evidence/source trace. Evidence: `frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`, `docs/00-context/evidence/phase-f-knowledge-graph.png`, frontend typecheck/test/build/E2E. |
