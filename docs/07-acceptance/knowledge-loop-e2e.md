# Knowledge Loop Automated E2E

## Goal

Provide an out-of-the-box command that proves the Atlas knowledge loop can run with safe sample data.

Long-term target journey:

```text
sample folder or zip
  -> upload batch
  -> convert and parse
  -> standard Markdown with source trace
  -> local or PostgreSQL-backed metadata
  -> review approval
  -> Wiki publication
  -> graph evidence
  -> model-selected Ask answer with citations
```

## Commands

```bash
npm run setup
npm run e2e:first-layer
npm run e2e:second-layer
```

Mock knowledge-loop only:

```bash
npm run e2e:loop:mock
```

Configured mode:

```bash
cp configs/atlas.company.example.env .env
# Fill values from approved local/company config.
npm run e2e:loop:configured
```

## Acceptance IDs

| ID | Behavior | Current status |
|---|---|---|
| UAT-KLOOP-001 | Start from a clean local sample state. | Implemented by `scripts/e2e/reset-local-state.sh`. |
| UAT-KLOOP-002 | Run with mock mode and no external network dependency. | Implemented for first-layer local/mock acceptance. |
| UAT-KLOOP-003 | Simulate folder upload and batch creation. | Implemented in `frontend/tests/e2e/acceptance/knowledge-loop.spec.ts`. |
| UAT-KLOOP-004 | Verify generated report evidence includes source trace, low confidence, and review-required state. | Implemented against the mock frontend flow. |
| UAT-KLOOP-005 | Verify Wiki publication uses approved Markdown only. | Implemented locally by review-publish and Wiki surface assertions plus backend review-publish contracts. |
| UAT-KLOOP-006 | Verify graph nodes and edges point back to evidence. | Implemented locally by graph Playwright assertions and backend graph contracts. |
| UAT-KLOOP-007 | Verify Ask answers use selected knowledge spaces, selected model, and citations. | Implemented locally by Ask surface assertions and backend Ask contracts. |
| UAT-KLOOP-008 | Verify unapproved content is excluded from graph and Ask evidence. | Implemented locally by review gate assertions and backend graph/Ask contract tests. |

## First-Layer Gate

`npm run e2e:first-layer` is the local/mock gate for implemented Phase 1-4 behavior. It runs:

1. Mock config check, sample output reset, and sample input seeding.
2. Frontend build.
3. Full frontend Playwright E2E, including the mock knowledge-loop acceptance spec.
4. Backend `mvn -f backend/pom.xml verify` for API, adapter, review-publish, graph, vector, model, storage, parser, converter, and Ask contracts.
5. Diff whitespace hygiene and tracked-output hygiene checks.

Reports are generated at:

- `frontend/playwright-report/index.html`
- `frontend/test-results/e2e-junit.xml`
- `backend/target/surefire-reports`
- `backend/target/failsafe-reports`

Generated outputs such as `backend/target`, `frontend/playwright-report`, `frontend/test-results`, and `samples/output/e2e` are local artifacts and must not be committed.

## Second-Layer Gate

`npm run e2e:second-layer` is the local full-stack gate for Phase 1-4 behavior. It still uses mock/sample data only, but it verifies the browser against a live Spring Boot API and a temporary PostgreSQL database.

It runs:

1. Mock config check, sample output reset, and sample input seeding.
2. Temporary Docker PostgreSQL startup on `127.0.0.1:55432` by default.
3. Spring Boot API startup on `127.0.0.1:18080` by default.
4. Frontend build with `VITE_ATLAS_API_BASE_URL` set to the local API.
5. Playwright second-layer flow:
   - create a trusted sample batch through the API;
   - approve and publish the file to Wiki;
   - project graph nodes and edges with evidence;
   - index a mock vector item;
   - ask a mock model answer with citations;
   - open the browser and verify the graph panel is connected to the live API rather than fallback mock graph data.
6. Diff whitespace hygiene check.

Useful overrides:

```bash
ATLAS_E2E_POSTGRES_PORT=55433 npm run e2e:second-layer
ATLAS_E2E_BACKEND_PORT=18081 npm run e2e:second-layer
KEEP_ATLAS_E2E_STACK=1 npm run e2e:second-layer
```

Second-layer generated logs and reports:

- `samples/output/e2e/second-layer-backend.log`
- `frontend/playwright-report/index.html`
- `frontend/test-results/e2e-junit.xml`

## Out Of Scope

First-layer and second-layer E2E are not configured/company-provider E2E. They do not use real company documents, real credentials, external cloud calls, DeepSeek/Copilot/provider APIs, production auth/RBAC, real object storage, real vector databases, real model providers, or real external databases.

Configured integration should continue to use `npm run e2e:loop:configured` and approved local/company configuration when provider-backed E2E is intentionally in scope.
