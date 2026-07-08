# Atlas Knowledge Hub

Atlas Knowledge Hub is an internal lightweight knowledge product for turning project document packages into reviewable, traceable knowledge pages.

It helps teams upload document folders or zip packages, convert Office documents to PDF through internal tools, parse PDFs into Markdown and images, normalize the result into a standard LM Wiki format, support SME review, and later power knowledge graph and Ask experiences.

> **Status note:** This README was refreshed to match the current repository state. The product has moved well past the original "static prototype" MVP — it now has a real Spring Boot backend and a Vue 3 single-page frontend. See [Current Status](#current-status).

## Why It Exists

Project knowledge often lives in scattered PPTX, DOCX, PDF, spreadsheet, and image-heavy folders. Teams need a repeatable way to make that content searchable, reviewable, and useful without sending confidential content to external cloud services.

Atlas wraps existing internal conversion tools instead of rebuilding parsers from scratch:

- `trinity-office` for Office-to-PDF conversion.
- `document-normalize` for PDF-to-Markdown and image extraction.
- Future parser adapters such as MinerU, Docling, PaddleOCR, internal OCR, or Copilot Vision.

The core product asset is standardized Markdown with source trace, confidence, review status, and metadata.

## High-Level Workflow

1. Create a Knowledge Space for a project or domain.
2. Upload a folder or zip package (mock/sample data in the prototype; real ingestion through the backend API).
3. Convert source documents to PDF through a converter adapter.
4. Parse PDFs into Markdown and images through a parser adapter.
5. Normalize Markdown into the LM Wiki standard.
6. Flag low-confidence or LLM-generated sections for SME review.
7. Publish approved content to the internal Wiki.
8. Later, derive graph nodes, graph edges, and Ask/RAG indexes from approved content.

## Current Status

Atlas has progressed past the Phase 0 static prototype into a working backend + SPA frontend. The original MVP section of this README is kept for history under [Original MVP Scope](#original-mvp-scope-historical).

### Backend (real, Spring Boot)

- **Stack:** Java 21 + Spring Boot, artifact `metadata-api` under package `com.atlas.metadata`.
- **Persistence:** Spring Data JPA + Flyway migrations against PostgreSQL (19 migrations, `V1__init_schema` through `V19__worker_retry_dead_letter`).
- **API surface (~23 controllers):** Space, Space Membership, File, Batch, Conversion, Parser, Storage, Vector, Model, Ingestion, Wiki Ingest, Wiki Linkify/Lint, Graph, Ask, Review, Review/Publish, Auth, Audit Log, Connector Sync, Manual URL Source, Downstream Refresh, Worker Recovery, Retrieval Quality Metrics.
- **Service layer (~27 services)** and **repository layer (~41 repositories)** follow a clean domain/adapter split.
- **Adapter boundaries:** conversion, parser, storage, vector, model, and connector integrations sit behind product-facing adapter interfaces — the core backend does not depend on one specific parser, model provider, or vector database.
- **Tests:** ~73 backend test classes (unit + integration, including Testcontainers for PostgreSQL).

### Frontend (Vue 3 SPA prototype)

- **Stack:** Vue 3 + Vite + TypeScript, scaffolded under `frontend/`.
- **Shape:** The UI currently lives in a single large component, `frontend/src/App.vue`, wired to the backend API through `frontend/src/api.ts` and `frontend/src/types.ts`, with mock data in `frontend/src/data/atlasMock.ts`. Componentization into feature modules is a planned slice (see [Roadmap](#technology--roadmap-direction)).
- **Tests:** Vitest unit specs (`frontend/src/*.test.ts`) and Playwright end-to-end specs under `frontend/tests/e2e/`, organized by product phase and acceptance layer.

### Honest limitations

- Default data is **mock / sample only**. No real company documents, OCR output, or model providers are used unless a local provider key is explicitly configured for the opt-in third-layer acceptance.
- Authentication/RBAC and audit logging are implemented at the metadata/control-plane level (see the `auth-space-rbac` and `audit-log-foundation` slices), but production-grade security hardening still follows the SDD phase discipline.
- The frontend is still effectively a single-page prototype; it is not yet split into reusable components.

## Technology & Roadmap Direction

Atlas uses a lightweight SDD workflow for feature planning. The original technology direction called for Vue 3 + Vite + TypeScript and Java + Spring Boot + PostgreSQL + Flyway; **that scaffolding is now in place** and should not be re-introduced.

Roadmap phases (see `ROADMAP.md` / `ROADMAP.zh-CN.md` for detail):

1. Phase 0 — static prototype and SDD artifacts. *(done)*
2. Phase 1 — Vue frontend shell with mock data. *(done as SPA prototype)*
3. Phase 2 — backend metadata API and persistence. *(done: Spring Boot + PostgreSQL + Flyway)*
4. Phase 3 — converter / parser / storage / vector / model / connector adapters. *(in progress)*
5. Phase 4 — review, publish, graph, and Ask integration hardening. *(in progress)*

Upcoming frontend work includes splitting `App.vue` into feature components once the relevant SDD slice is accepted.

Do not skip phases or introduce production authentication, SSO, or complex permission logic except through accepted SDD slices.

## Repository Structure

```text
.
├── backend/                 # Spring Boot metadata/control-plane API (Java 21)
│   ├── pom.xml              # artifact: metadata-api
│   └── src/main/java/com/atlas/metadata/   # controller, service, repository, domain, adapter, dto...
│   └── src/main/resources/db/migration/    # Flyway SQL migrations (V1..V19)
├── frontend/                # Vue 3 + Vite + TypeScript SPA prototype
│   └── src/                 # App.vue (monolithic UI), api.ts, types.ts, data/atlasMock.ts
│   └── tests/e2e/           # Playwright acceptance specs by phase / layer
├── converter/               # Converter adapter notes and prompt drafts
│   └── prompts/             # Normalization and Wiki-generation prompt drafts
├── docs/                    # Bilingual (EN + 简体中文) product, architecture, SDD artifacts
│   ├── 00-context/          # SDD profile, traceability, execution manifests, checklists
│   ├── 01-requirements/     # Slice requirements
│   ├── 02-user-stories/
│   ├── 03-spec/             # Behavior source of truth
│   ├── 04-architecture/
│   ├── 05-design/
│   ├── 06-tasks/            # Implementation checklists (bilingual)
│   └── 07-acceptance/       # Acceptance design (e.g. knowledge-loop-e2e)
├── prototypes/              # Original single-file static demo (index.html)
├── samples/
│   ├── input/               # Mock/sample inputs only
│   └── output/              # Mock/sample generated outputs only
├── configs/                 # Local env templates (e.g. atlas.company.example.env)
├── scripts/
│   ├── e2e/                 # Acceptance run scripts (first/second/third layer, knowledge-loop)
│   └── agent-workflow/      # SDD goal-manifest / check-sdd / check-workflow helpers
├── AGENTS.md                # Coding-agent instructions
├── DEVELOPMENT_STANDARDS.md # Staged engineering standards and quality gates
├── PROJECT_RULES.md         # Project rules and constraints
└── ROADMAP.md               # Phased product roadmap
```

## Development Standards

Atlas uses staged development standards so Phase 0 can stay lightweight while later implementation phases gain stronger tests, CI, API contracts, database controls, and security gates.

See `DEVELOPMENT_STANDARDS.md` for the full standard covering goal-driven SDD, coding, frontend, backend, API, database, adapter, security, testing, review, Git, and CI expectations.

New contributors can start with `docs/getting-started.md` for a step-by-step onboarding guide.
Use `docs/local-runbook.md` when you need command-by-command local startup and verification steps.
Chinese VS Code users can use `docs/local-runbook.zh-CN.md`.
Use `docs/05-design/runbooks/deployment-monitoring-runbook.md` for deployment readiness, monitoring signal, incident triage, rollback, and closeout evidence guidance.

Reusable acceptance findings are captured in `docs/00-context/lessons-learned.md` and should update the rule, spec, checklist, or test that prevents recurrence.

## Run Locally

### Frontend (Vue SPA)

```bash
npm --prefix frontend install
npm --prefix frontend run dev        # Vite dev server on 127.0.0.1; defaults to local API in dev
npm --prefix frontend run dev:mock   # Vite dev server with API calls disabled for mock fallback review
npm --prefix frontend run dev:fullstack # Vite dev server explicitly pointed at http://127.0.0.1:8080
npm --prefix frontend run build      # lint + typecheck + production build
npm --prefix frontend run test       # Vitest unit tests
npm --prefix frontend run e2e        # Playwright end-to-end specs
```

### Backend (Spring Boot)

The backend uses the Maven Wrapper committed under `backend/` and requires Java 21. Integration tests use Testcontainers, so Docker must be running for `verify`.

```bash
cd backend && ./mvnw spring-boot:run  # starts the metadata-api on its configured port
cd backend && ./mvnw verify           # backend unit/integration tests
```

For a deterministic full-stack run with PostgreSQL, use the second-layer acceptance script (it provisions PostgreSQL via Docker):

```bash
bash scripts/e2e/run-second-layer.sh
```

### Original static prototype

The original single-file demo still opens directly in a browser with no build step:

```bash
open prototypes/index.html
```

Only use mock data in this repository. Do not commit real company documents, screenshots, credentials, logs, or confidential content.

## Automated Acceptance

Atlas has three acceptance layers:

- `first-layer`: deterministic local/mock gate, no provider calls.
- `second-layer`: deterministic local full-stack gate with Docker PostgreSQL and Spring Boot, still no provider calls.
- `third-layer`: opt-in provider-backed gate for configured provider Ask through the backend ModelAdapter, using mock/sample knowledge data only. Supported local providers are `deepseek` and `github-models`.

Run the mock knowledge-loop E2E from the repository root:

```bash
npm run setup
npm run e2e:first-layer
```

Run the local full-stack second-layer E2E with Docker PostgreSQL, Spring Boot, frontend, graph, publish, vector, and Ask checks:

```bash
npm run e2e:second-layer
```

Run the opt-in provider-backed third-layer E2E only when a local approved provider key is already available. The command reads repository-root `.env` first, so the usual local flow is:

```bash
cp configs/atlas.company.example.env .env
# edit .env locally; do not commit it
npm run e2e:third-layer
```

For GitHub Models, use a GitHub token with Models access and set:

```bash
ATLAS_MODEL_PROVIDER=github-models
ATLAS_MODEL_ENDPOINT=https://models.github.ai/inference
ATLAS_MODEL_NAME=openai/gpt-4.1
```

Do not commit `.env` files, shell history exports, real API keys, provider logs, screenshots, or company data.

For the smaller mock knowledge-loop-only path:

```bash
npm run e2e:loop:mock
```

Configured company/local integration mode starts from `configs/atlas.company.example.env` and runs with:

```bash
npm run e2e:loop:configured
```

See `docs/07-acceptance/knowledge-loop-e2e.md` for the full acceptance design.

## Original MVP Scope (Historical)

The initial MVP was intentionally lightweight:

- Static HTML prototype.
- Knowledge Space homepage.
- IBM i Modernization demo space.
- Document, Wiki, Graph, Review, and Ask tabs.
- Mock batch status and file tree.
- Mock Wiki content with source trace concepts.
- Mock graph and review screens.

The MVP did not include real OCR, authentication, production databases, external cloud services, or real RAG. Most of this has since been superseded by the Spring Boot backend and Vue SPA described above; the static prototype in `prototypes/index.html` remains as a reference artifact.
