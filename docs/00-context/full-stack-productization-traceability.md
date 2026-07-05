# Traceability: Full-Stack Productization

## Slice Contract

- Slice: `full-stack-productization`
- Goal: P0 browser-driven full-stack web service loop from space list through Ask.
- Phase: 4 hardening / P0 productization.
- Status: Implemented and verified on 2026-07-03.

## Source Documents

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/01-requirements/requirement.md`
- Existing SDD slices: `metadata-api`, `review-publish`, `knowledge-graph`, `ask-rag`, `model-adapter`, `vector-adapter`, `provider-backed-e2e`
- FE baseline/reference: `frontend/public/atlas-prototype.html`, `prototypes/index.html`

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-FSP-001 | US-FSP-001 | T-FSP-002, T-FSP-003 |
| REQ-FSP-002 | US-FSP-001 | T-FSP-002, T-FSP-003 |
| REQ-FSP-003 | US-FSP-002 | T-FSP-004 |
| REQ-FSP-004 | US-FSP-002 | T-FSP-004 |
| REQ-FSP-005 | US-FSP-003 | T-FSP-005 |
| REQ-FSP-006 | US-FSP-003 | T-FSP-005 |
| REQ-FSP-007 | US-FSP-004, US-FSP-005 | T-FSP-006 |
| REQ-FSP-008 | US-FSP-004 | T-FSP-006 |
| REQ-FSP-009 | US-FSP-005 | T-FSP-007 |
| REQ-FSP-010 | US-FSP-006 | T-FSP-003, T-FSP-008 |
| REQ-FSP-011 | US-FSP-007 | T-FSP-009 |
| REQ-FSP-012 | US-FSP-007 | T-FSP-010 |

## Verification Plan

- `npm run e2e:first-layer`
- `npm run e2e:second-layer`
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e`
- `cd backend && mvn verify`
- `git diff --check`
- Secret/private-path scan over changed files.
- Network/dependency scan for new external calls.

## Verification Evidence

- `npm run e2e:first-layer` passed.
- `npm run e2e:second-layer` passed after serializing second-layer Playwright execution to avoid shared live-backend state races.
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e` passed.
- `cd backend && mvn verify` passed.
- `git diff --check` passed.
- Secret/private-path scan found only static prototype label strings named `password`; no raw secret values, private paths, or real company data were introduced.
- Network/dependency scan found no new dependency additions and no new direct external provider/cloud calls; UI calls remain Atlas API calls through the configured API base.

## SDD Quality Gate

- English and Chinese companions exist for every new SDD artifact.
- IDs match across both languages.
- API guide is included because backend/API orchestration is in scope.
- Existing implementation was grounded against current controllers, services, DTOs, and E2E scripts.
- `review-doc-quality` checklist was applied as a self-review; no critical SDD blockers remain before implementation.

## Deferred Work

- Production file-byte upload.
- Production auth/RBAC.
- Production storage/vector/model providers.
- Real company data ingestion.

## Product Goal Batch 5 Status

| Phase | Task IDs | Maturity | Evidence |
|---|---|---|---|
| Phase I1-I3 API-backed Vue cutover | `T-FSP-011`; consumes `T-MA-014` scope for space/batch/file/chunk metadata and `review-publish` review queue API | L3 API-backed | `frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`; `docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png`; real Vue home, space header, Documents tab, and Processing Center consume Atlas API responses for Knowledge Space metadata, batch/file/chunk metadata, and review queues. |

Verification on 2026-07-05: `cd frontend && npm run typecheck && npm run test && npm run build`; focused Phase I1-I3 Playwright; Batch 1-5 Playwright regression; `cd backend && mvn verify`. This status does not mark final product acceptance and does not add new backend/API contracts.

## Product Goal Batch 6 Status

| Phase | Task IDs | Maturity | Evidence |
|---|---|---|---|
| Phase I4-I7 API-backed Vue cutover | `T-FSP-012`; consumes existing review-publish, knowledge-graph, ask-rag, and model-adapter APIs | L3 API-backed | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`; `docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`; real Vue Wiki, Graph, global Ask, and model settings surfaces consume Atlas API responses. |

Verification on 2026-07-05: `cd frontend && npm run typecheck && npm run test && npm run build`; focused Phase I4-I7 Playwright; Batch 1-6 Playwright regression; `cd backend && mvn verify`. This status does not mark final product acceptance and does not add production provider calls, raw secrets, real company data, or new backend/API contracts.

## Core Knowledge Loop v1 Traceability Addendum

| Requirement | Stories | Tasks | Verification |
|---|---|---|---|
| REQ-FSP-013 | User can configure model key normally | T-FSP-014, T-FSP-019 | Masked configuration API/UI checks; backend adapter tests |
| REQ-FSP-014 | User can upload real documents | T-FSP-015, T-FSP-019 | Multipart PDF/ZIP ingestion tests; manual UI upload |
| REQ-FSP-015 | Uploaded documents parse through adapter boundary | T-FSP-016 | Parser adapter tests; source trace inspection |
| REQ-FSP-016 | Review state is consistent across file and chunks | T-FSP-017 | Review service tests and queue inspection |
| REQ-FSP-017 | Backend refreshes downstream evidence | T-FSP-018, T-FSP-019 | Downstream refresh tests; graph/vector evidence checks |
| REQ-FSP-018 | Ask runs after publish/refresh | T-FSP-014, T-FSP-018, T-FSP-019 | Ask API/UI smoke with citations or safe state |
| REQ-FSP-019 | Unsupported features are visibly disabled | T-FSP-019 | Frontend disabled-state checks |
| REQ-FSP-020 | Closed-loop safety gates are recorded | T-FSP-020 | Final verification report |
