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
