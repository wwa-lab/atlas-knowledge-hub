# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Is

Atlas Knowledge Hub is an internal, mock-only knowledge product that turns project document packages (PPTX/DOCX/PDF/spreadsheet/image folders) into reviewable, traceable Markdown Wiki pages, and later powers a knowledge graph and grounded Ask/RAG. It wraps existing internal tools (`trinity-office` for Office→PDF, `document-normalize` for PDF→Markdown/images) behind adapters rather than rebuilding parsers.

The core product asset is standardized Markdown carrying **source trace, confidence, and review status**.

## Read Before Changing Code

This repo is governed by explicit instruction files that take precedence over general defaults. Read them for any non-trivial change:

- `AGENTS.md` — agent operating rules (product direction, adapter rules, security, SDD/goal-driven execution).
- `PROJECT_RULES.md` — product/phase/adapter/data rules and quality gates.
- `DEVELOPMENT_STANDARDS.md` — staged engineering standards by phase.
- `ROADMAP.md` — phase definitions.
- Relevant slice docs under `docs/`.

## Commands

The frontend is the only buildable code. Everything runs from `frontend/`.

```bash
cd frontend
npm install
npm run dev            # Vite dev server on 127.0.0.1
npm run typecheck      # vue-tsc -b
npm run test           # vitest run (unit/component)
npm run test:coverage  # vitest with v8 coverage
npm run build          # vue-tsc -b && vite build
npm run e2e            # playwright critical-flow smoke tests
```

Run a single unit test: `npx vitest run src/data/atlasMock.test.ts` (or `npx vitest run -t "<test name>"`).
Run a single e2e test: `npx playwright test tests/e2e/phase1-smoke.spec.ts`.

The static prototype needs no build — open it directly: `open prototypes/index.html`.

## Architecture

### Phased delivery (do not skip phases)
Phase 0 static prototype + SDD → Phase 1 Vue shell w/ mock data → Phase 2 Spring Boot metadata API + PostgreSQL/Flyway → Phase 3 converter/parser/storage/vector/model adapters → Phase 4 review/publish/graph/Ask hardening. The repo is currently at Phase 0→1. `backend/` and `converter/` are placeholders/notes; **do not scaffold Spring Boot, a database, auth, or real integrations until the relevant phase begins or the user explicitly asks.**

### Prototype-as-baseline (important)
`prototypes/index.html` and `frontend/public/atlas-prototype.html` are byte-identical single-file (~4200 line) static demos and are the **fidelity baseline**. `frontend/src/App.vue` currently just hosts `atlas-prototype.html` in an iframe. Phase 1 work incrementally extracts this prototype into real Vue components while preserving visual/interaction parity. **If you change the FE experience, update the static mirror (`prototypes/index.html`) and the SDD docs in the same slice.**

### Frontend layout (`frontend/src/`)
`main.ts` mounts `App.vue`; `types.ts` holds typed domain models; `data/atlasMock.ts` holds mock-only data; `styles.css` global styles. Organize by feature/domain, use typed props/emits/models, and reproduce prototype behavior with mock data before wiring any real API.

### Adapter boundaries (core architectural rule)
Product logic must never call parser, converter, model, vector-DB, storage, or search engines directly. Everything (`document-normalize`, MinerU, Docling, PaddleOCR, PostgreSQL/pgvector, Ollama, S3-compatible storage, etc.) sits behind product-facing adapter interfaces. Never hardcode a single parser/model/store as the only implementation. Deterministic processing runs before LLM enrichment; LLM-generated content is review-required until verified.

## SDD Workflow

This is a Spec-Driven Development repo. Behavior lives in docs before code:

- `docs/00-context/sdd-profile.md` — the active SDD profile (profile-driven; don't assume a universal doc chain).
- `docs/03-spec/{slice}-spec.md` — **behavior source of truth**.
- `docs/06-tasks/{slice}-tasks.md` — **executable implementation checklist**.
- `docs/00-context/{slice}-traceability.md` — slice traceability.
- `docs/00-context/lessons-learned.md` — durable lessons; every acceptance mismatch must update the artifact (spec/design/task/standard/rule/test) that prevents recurrence, not just chat.

Docs are numbered `docs/01-requirements` → `02-user-stories` → `03-spec` → `04-architecture` → `05-design` → `06-tasks`. The current slice is `knowledge-space`. For goal-driven slices, move from doc creation/update → implementation → verification → evidence in one continuous flow; if implementation would diverge from the spec, update the spec/design/tasks first or surface the mismatch before coding.

## Hard Constraints

- **Mock data only.** Never commit real company documents, screenshots, credentials, logs, exports, or customer content.
- **No secrets or private data in source** — no API keys, tokens, private endpoints, internal hostnames, or private absolute paths. Mock config may show status-only fields like `configured`, never raw values.
- **No external network calls or new external dependencies** in the prototype/Phase 1.
- **Preserve trace/confidence/review status** on all Markdown and metadata.
- **Scope surgically.** Touch only files needed for the goal; every changed line should trace to the request, an SDD requirement, a rule, or a verification. Don't refactor adjacent code.

## Reference Product Policy

WeKnora may be used **only** as a UX/product benchmark. Do not copy its source, structure, assets, or implementation details. Map any inspired feature to an Atlas-specific use case (folder/ZIP upload, batch parsing, source trace, SME review, LM Wiki, graph, trusted Ask).

## Verification (Phase 0/1)

Before reporting a change complete: static syntax check on edited HTML/CSS/JS, `git diff --check`, scan for new network calls/dependencies and for raw secrets/private paths, and for Phase 1 run `npm run typecheck` + `npm run test` (+ `npm run e2e` for critical flows). Name any skipped check and why — never imply an unrun check passed.
