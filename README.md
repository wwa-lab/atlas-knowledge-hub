# Atlas Knowledge Hub

Atlas Knowledge Hub is an internal lightweight knowledge product for turning project document packages into reviewable, traceable knowledge pages.

It helps teams upload document folders or zip packages, convert Office documents to PDF through internal tools, parse PDFs into Markdown and images, normalize the result into a standard LM Wiki format, support SME review, and later power knowledge graph and Ask experiences.

## Why It Exists

Project knowledge often lives in scattered PPTX, DOCX, PDF, spreadsheet, and image-heavy folders. Teams need a repeatable way to make that content searchable, reviewable, and useful without sending confidential content to external cloud services.

Atlas wraps existing internal conversion tools instead of rebuilding parsers from scratch:

- `trinity-office` for Office-to-PDF conversion.
- `document-normalize` for PDF-to-Markdown and image extraction.
- Future parser adapters such as MinerU, Docling, PaddleOCR, internal OCR, or Copilot Vision.

The core product asset is standardized Markdown with source trace, confidence, review status, and metadata.

## High-Level Workflow

1. Create a Knowledge Space for a project or domain.
2. Upload a folder or zip package using mock/sample data in the prototype.
3. Convert source documents to PDF through a converter adapter.
4. Parse PDFs into Markdown and images through a parser adapter.
5. Normalize Markdown into the LM Wiki standard.
6. Flag low-confidence or LLM-generated sections for SME review.
7. Publish approved content to the internal Wiki.
8. Later, derive graph nodes, graph edges, and Ask/RAG indexes from approved content.

## MVP Scope

The initial MVP is intentionally lightweight:

- Static HTML prototype.
- Knowledge Space homepage.
- IBM i Modernization demo space.
- Document, Wiki, Graph, Review, and Ask tabs.
- Mock batch status and file tree.
- Mock Wiki content with source trace concepts.
- Mock graph and review screens.

The MVP does not include real OCR, authentication, production databases, external cloud services, or real RAG.

## Technology Direction

Atlas will use a lightweight SDD workflow for feature planning once implementation begins. The selected implementation stack is:

- Frontend: Vue 3 + Vite + TypeScript after the static prototype is validated.
- Backend: Java + Spring Boot.
- Database: PostgreSQL.
- Database migration: Flyway.

Do not scaffold these frameworks until the relevant roadmap phase begins or the user explicitly requests implementation.

## Development Standards

Atlas uses staged development standards so Phase 0 can stay lightweight while later implementation phases gain stronger tests, CI, API contracts, database controls, and security gates.

See `DEVELOPMENT_STANDARDS.md` for the full standard covering goal-driven SDD, coding, frontend, backend, API, database, adapter, security, testing, review, Git, and CI expectations.

New contributors can start with `docs/getting-started.md` for a step-by-step onboarding guide.
Use `docs/local-runbook.md` when you need command-by-command local startup and verification steps.
Chinese VS Code users can use `docs/local-runbook.zh-CN.md`.

Reusable acceptance findings are captured in `docs/00-context/lessons-learned.md` and should update the rule, spec, checklist, or test that prevents recurrence.

## Repository Structure

```text
.
├── backend/                 # Backend placeholder; no production service yet
├── converter/               # Converter adapter notes and prompt drafts
│   └── prompts/             # Normalization and Wiki-generation prompt drafts
├── docs/                    # Product, architecture, workflow, and data standards
├── frontend/                # Frontend placeholder; prototype is static first
├── prototypes/              # Single-file static demo
├── samples/
│   ├── input/               # Mock/sample inputs only
│   └── output/              # Mock/sample generated outputs only
├── AGENTS.md                # Coding-agent instructions
├── DEVELOPMENT_STANDARDS.md # Staged engineering standards and quality gates
├── PROJECT_RULES.md         # Project rules and constraints
└── ROADMAP.md               # Phased product roadmap
```

## Use the Prototype

Open the static prototype directly in a browser:

```bash
open prototypes/index.html
```

No install step, build system, server, CDN, React, or Vue is required.

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
