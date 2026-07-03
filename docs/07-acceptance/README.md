# Automated Acceptance

Atlas acceptance is designed for one-command local verification before company deployment.

The acceptance layer is separate from ordinary unit and component tests:

- Unit/component tests prove small implementation behavior.
- Playwright smoke tests prove critical UI paths.
- Automated acceptance tests prove product journeys with sample data and adapter modes.

## Modes

| Mode | Purpose | Command |
|---|---|---|
| `first-layer` | Local/mock gate for implemented Phase 1-4 surfaces: frontend critical flows, folder upload mock flow, review-publish, graph, Ask, mock knowledge-loop acceptance, and backend API/adapter/graph/Ask contracts. | `npm run e2e:first-layer` |
| `second-layer` | Local full-stack gate with Docker PostgreSQL, Spring Boot API, built frontend, live API-backed graph, publish, vector, and Ask checks using mock/sample data only. | `npm run e2e:second-layer` |
| `mock` | Frontend mock knowledge-loop acceptance without secrets, backend services, or company systems. | `npm run e2e:loop:mock` |
| `configured` | Company or local integration environment using configured backend, database, storage, and model adapters. | `npm run e2e:loop:configured` |

## Safety Rules

- Use mock/sample data only.
- Do not commit real company documents, screenshots, credentials, logs, exports, or private paths.
- Keep provider credentials in `.env` or an approved secret manager.
- Keep graph and Ask checks review-aware: unapproved content must not become graph or Ask evidence.

## Current Coverage

The first-layer suite provides a local/mock Phase 1-4 acceptance gate:

1. Build the Vue frontend.
2. Run frontend Playwright critical flows, including the accepted prototype shell, folder upload mock flow, review-publish, knowledge graph, Ask, and mock knowledge-loop acceptance.
3. Run `mvn -f backend/pom.xml verify` for backend metadata API, adapter, review-publish, graph, and Ask contract tests.
4. Run `git diff --check` and verify generated E2E/build outputs are not tracked.

The second-layer suite provides a local full-stack gate:

1. Start a temporary Docker PostgreSQL container.
2. Start the Spring Boot API against that database.
3. Build the frontend with `VITE_ATLAS_API_BASE_URL` pointed at the local API.
4. Use Playwright plus API requests to create a trusted sample batch, approve and publish it, project graph evidence, index a mock vector item, ask with citations, and verify the browser graph is connected to the live API.

Reports are generated at `frontend/playwright-report/index.html`, `frontend/test-results/e2e-junit.xml`, `backend/target/surefire-reports`, and `backend/target/failsafe-reports`.

First-layer and second-layer E2E intentionally do not cover configured company/provider integration, DeepSeek/Copilot/provider calls, real credentials, real company data, production auth/RBAC, real external storage/vector/model services, or real external databases. Second-layer E2E is live API-backed, but still local-only and mock/sample-data-only.
