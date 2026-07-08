# Local Runbook

This runbook is the hands-on path for running Atlas Knowledge Hub locally. It is intentionally operational: follow the commands in order, verify the expected result, and use the troubleshooting section when something does not start.

## 0. What You Can Run Locally

| Path | What it proves | Requires |
|---|---|---|
| Static prototype | Product shape and mock UX. | Browser only. |
| Frontend dev server | Vue shell and frontend behavior. | Node.js/npm. |
| Mock E2E loop | Local first-layer acceptance with mock/sample data. | Node.js/npm and Playwright browsers from frontend install. |
| Backend tests | API, adapter, Flyway, PostgreSQL contract behavior. | Java 21, Maven Wrapper, Docker for Testcontainers. |
| Second-layer E2E | Local full-stack browser plus API plus PostgreSQL loop. | Node.js/npm, Java 21, Maven Wrapper, Docker Desktop. |
| Third-layer E2E | Opt-in provider-backed Ask loop through a configured ModelAdapter provider, still using mock/sample knowledge data. | Node.js/npm, Java 21, Maven Wrapper, Docker Desktop, local provider key in `.env` or shell env. |
| Backend local API | Spring Boot API against your own PostgreSQL. | Java 21, Maven Wrapper, PostgreSQL config. |

The fastest path is the static prototype. The safest first verification path is `npm run e2e:first-layer`; use `npm run e2e:second-layer` when you want to prove the local browser is wired to the live Spring Boot API. Use `npm run e2e:third-layer` only when you intentionally want one real provider-backed Ask call from the backend adapter.

## 1. Prerequisites

Recommended versions:

```bash
node --version
npm --version
java --version
mvn --version
docker --version
```

Expected:

- Node.js with npm.
- Java 21 for backend.
- Maven Wrapper for backend commands.
- Docker Desktop running if you want to run backend integration tests with Testcontainers.

No real company data, private paths, or secrets are required for mock mode.

## 2. Clone And Install

From your workspace:

```bash
git clone https://github.com/wwa-lab/atlas-knowledge-hub.git
cd atlas-knowledge-hub
npm run setup
```

`npm run setup` installs frontend dependencies with:

```bash
npm --prefix frontend ci
```

## 3. Run The Static Prototype

Use this when you only need to inspect the product flow:

```bash
open prototypes/index.html
```

Expected result:

- Browser opens Atlas Knowledge Hub.
- You can enter the IBM i Modernization demo space.
- Documents, Wiki, Graph, Review, and Ask surfaces are visible with mock data.

This path does not start a server and does not require backend or database setup.

## 4. Run The Frontend Dev Server

```bash
cd frontend
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

Expected result:

- Vite starts successfully.
- The Vue app displays the Atlas prototype shell.
- In dev mode the API base defaults to `http://127.0.0.1:8080`; use `npm run dev:mock` for explicit mock fallback review or `npm run dev:fullstack` when the backend is running locally.

Stop it with `Ctrl+C`.

## 5. Run The Mock Knowledge Loop

From the repository root:

```bash
npm run e2e:loop:mock
```

This command:

1. Checks mock E2E config.
2. Resets `samples/output/e2e`.
3. Prepares `samples/input/e2e`.
4. Builds the frontend.
5. Runs Playwright acceptance tests.

Expected result:

```text
1 passed
Knowledge-loop E2E complete.
```

Reports:

```text
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

These reports are generated artifacts and should not be committed.

## 6. Run Frontend Checks

From `frontend/`:

```bash
npm run lint
npm run typecheck
npm run test
npm run build
npm run e2e
```

Expected result:

- Lint passes.
- TypeScript passes.
- Vitest passes.
- Build succeeds.
- Playwright E2E passes.

At the time this runbook was written, full frontend Playwright includes:

- Phase 1 smoke.
- Folder upload mock flow.
- Automated knowledge loop acceptance.
- Review publish surface.
- Knowledge graph surface.

## 7. Run Backend Verification

From the repository root:

```bash
cd backend
./mvnw verify
```

Expected result:

```text
BUILD SUCCESS
```

Notes:

- Integration tests use Testcontainers.
- Docker Desktop must be running.
- Maven creates `backend/target/`; this is build output and should not be committed.

Clean backend output:

```bash
rm -rf backend/target
```

## 8. Run Second-Layer E2E

Use this when you want one local command that starts PostgreSQL, starts Spring Boot, builds the frontend, and verifies the browser against the live local API.

From the repository root:

```bash
npm run e2e:second-layer
```

This command uses only mock/sample data. It does not call DeepSeek, Copilot, configured model providers, company storage, or external vector databases.

Expected result:

```text
Second-layer E2E complete.
```

Reports and logs:

```text
samples/output/e2e/second-layer-backend.log
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

Useful overrides:

```bash
ATLAS_E2E_POSTGRES_PORT=55433 npm run e2e:second-layer
ATLAS_E2E_BACKEND_PORT=18081 npm run e2e:second-layer
KEEP_ATLAS_E2E_STACK=1 npm run e2e:second-layer
```

By default the script removes the temporary Docker PostgreSQL container when it exits.

## 9. Run Third-Layer Provider-Backed E2E

Use this when you want one local command that starts PostgreSQL, starts Spring Boot, builds the frontend, and verifies a real provider-backed Ask journey through the backend ModelAdapter.

Third-layer is opt-in and not part of default frontend E2E, first-layer, or second-layer. It still uses mock/sample knowledge data only.

From the repository root, copy the local env template and fill provider values in `.env`:

```bash
cp configs/atlas.company.example.env .env
$EDITOR .env
```

Required values:

```bash
ATLAS_MODEL_PROVIDER=deepseek
ATLAS_MODEL_ENDPOINT=https://api.deepseek.com
ATLAS_MODEL_API_KEY=<local-deepseek-api-key>
ATLAS_MODEL_NAME=deepseek-chat
```

GitHub Models alternative:

```bash
ATLAS_MODEL_PROVIDER=github-models
ATLAS_MODEL_ENDPOINT=https://models.github.ai/inference
ATLAS_MODEL_API_KEY=<local-github-token-with-models-access>
ATLAS_MODEL_NAME=openai/gpt-4.1
```

Do not use or export an IDE Copilot session token. The `github-models` provider expects an approved GitHub token for GitHub Models inference.

Then run:

```bash
npm run e2e:third-layer
```

Expected result:

```text
Third-layer provider-backed E2E complete.
```

Reports and logs:

```text
samples/output/e2e/third-layer-backend.log
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

What the command does:

1. Fails before starting services if `ATLAS_MODEL_API_KEY` is missing.
2. Starts a temporary Docker PostgreSQL container, default port `55434`.
3. Starts Spring Boot API, default port `18082`.
4. Builds the frontend against the local API.
5. Seeds approved sample evidence, publishes Wiki, projects graph evidence, indexes vector evidence, and asks with `mode=configured`.
6. Verifies answer success, evidence/citation/source trace, and live API-backed graph state.
7. Scans generated provider-backed artifacts for obvious secrets and private paths.
8. Cleans up the temporary backend process and PostgreSQL container by default.

Useful overrides:

```bash
ATLAS_E2E_POSTGRES_PORT=55435 npm run e2e:third-layer
ATLAS_E2E_BACKEND_PORT=18083 npm run e2e:third-layer
KEEP_ATLAS_E2E_STACK=1 npm run e2e:third-layer
```

Manual cleanup if you used `KEEP_ATLAS_E2E_STACK=1`:

```bash
docker rm -f atlas-e2e-third-postgres
```

Safety:

- Do not commit `.env` files or shell exports containing real keys.
- Do not paste real keys into frontend code, Playwright specs, docs, screenshots, traces, or reports.
- Do not use real company documents; the test seeds only mock/sample evidence.
- If the provider returns 429, 5xx, or a network error, the adapter sanitizes the failure and the E2E run should be treated as blocked by local/provider conditions.

## 10. Run Backend API Locally

Use this only when you want to manually call the Spring Boot API against a local PostgreSQL database.

Create a local database and user using your preferred PostgreSQL setup. Then export:

```bash
export ATLAS_DB_URL='jdbc:postgresql://127.0.0.1:5432/atlas_knowledge_hub'
export ATLAS_DB_USERNAME='atlas_user'
export ATLAS_DB_PASSWORD='change-me-local-only'
export ATLAS_DB_SCHEMA='atlas'
```

Start the backend:

```bash
cd backend
./mvnw spring-boot:run
```

Expected result:

- Spring Boot starts.
- Flyway validates and applies migrations.
- API is available on the default Spring Boot port unless overridden.

Do not commit the local password or database URL if it contains private information.

## 11. Configured Mode

Configured mode is for local/company integration after the required services exist.

Start from the template:

```bash
cp configs/atlas.company.example.env .env
```

Then fill approved local values:

```text
ATLAS_MODE=configured
ATLAS_API_BASE_URL=...
ATLAS_DATABASE_URL=...
ATLAS_MODEL_PROVIDER=...
ATLAS_MODEL_ENDPOINT=...
ATLAS_MODEL_API_KEY=...
```

Run:

```bash
npm run e2e:loop:configured
```

Important:

- Do not commit `.env`.
- Do not paste real keys into docs, issues, screenshots, or frontend source.
- DeepSeek/Copilot/provider tests should be opt-in, not default.

## 12. Clean Local Generated Output

Safe cleanup:

```bash
rm -rf frontend/playwright-report frontend/test-results
rm -rf samples/output/e2e
rm -rf backend/target
docker rm -f atlas-e2e-third-postgres 2>/dev/null || true
```

Do not delete source directories or migrations.

## 13. Troubleshooting

### `npm run setup` fails

Try:

```bash
cd frontend
npm ci
```

If the lockfile and package file disagree, stop and inspect the diff before changing dependencies.

### Playwright says browsers are missing

From `frontend/`:

```bash
npx playwright install
```

Then rerun:

```bash
npm run e2e
```

### Port 5173 or 4173 is busy

Stop the existing process or run the command again after closing other Vite/preview sessions.

Find a process:

```bash
lsof -i :5173
lsof -i :4173
```

### Second-layer E2E says port 55432 or 18080 is busy

Use another local port:

```bash
ATLAS_E2E_POSTGRES_PORT=55433 ATLAS_E2E_BACKEND_PORT=18081 npm run e2e:second-layer
```

Or stop the conflicting local process before rerunning.

### Third-layer E2E says `ATLAS_MODEL_API_KEY` is missing

Set the key in the current shell and rerun:

```bash
export ATLAS_MODEL_API_KEY='<local-provider-api-key>'
npm run e2e:third-layer
```

Do not add the key to frontend source, Playwright specs, committed docs, or git-tracked config.

### Third-layer E2E says port 55434 or 18082 is busy

Use another local port:

```bash
ATLAS_E2E_POSTGRES_PORT=55435 ATLAS_E2E_BACKEND_PORT=18083 npm run e2e:third-layer
```

Or stop the conflicting local process before rerunning.

### Backend integration tests cannot find Docker

Start Docker Desktop, then rerun:

```bash
cd backend
./mvnw verify
```

### Backend local run fails with missing datasource values

Check:

```bash
echo "$ATLAS_DB_URL"
echo "$ATLAS_DB_USERNAME"
```

Do not echo passwords in shared terminals or recordings.

### Flyway migration validation fails

Do not edit applied migrations casually. Confirm whether your local database is stale. For local-only scratch databases, recreate the database and rerun.

### Generated files appear in `git status`

Check:

```bash
git status --short
```

Generated outputs should stay untracked/ignored:

```text
backend/target/
frontend/playwright-report/
frontend/test-results/
samples/output/
```

## 14. Before You Say Local Is Ready

Run this minimum set:

```bash
npm run e2e:first-layer
npm run e2e:second-layer
if [ -n "${ATLAS_MODEL_API_KEY:-}" ]; then npm run e2e:third-layer; fi
(cd backend && ./mvnw verify)
git diff --check
git status --short
```

Expected:

- E2E passes.
- Backend verify passes.
- Third-layer passes only when a local provider key is intentionally configured; otherwise record that it was skipped because `ATLAS_MODEL_API_KEY` was absent.
- No whitespace errors.
- No unexpected generated files or secrets are staged.

If you skip any command, record why.
