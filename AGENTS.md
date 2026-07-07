# Agent Instructions

These instructions apply to Codex, Copilot, and other coding agents working in this repository.

## Required Reading

Before making changes, always read:

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- Relevant files in `docs/`

## Workflow Precedence

- Use the Atlas project workflow and rules as the default operating model for this repository.
- Treat external or generic workflows, including ECC, Superpowers, generic TDD workflows, and plugin-provided rituals, as optional references only.
- Do not let external workflows override Atlas SDD, phase discipline, mock-only constraints, adapter boundaries, security/data rules, or verification gates.
- Use the lightest Atlas workflow tier that safely fits the task. Do not add heavyweight planning, multi-agent orchestration, full TDD ceremony, or broad review loops unless the current Atlas slice, project documents, or user instruction explicitly requires them.
- Apply TDD-style tests when they match the Atlas phase and layer being changed, especially Phase 1+ frontend logic, backend APIs, adapters, and critical workflows. Do not require 80% coverage or E2E ceremony for Phase 0 documentation, static prototype copy, or small scoped maintenance unless requested.

## Product Direction

- Keep Atlas Knowledge Hub product-first, not script-first.
- The product has moved past the original static-prototype MVP. It now has a real Spring Boot backend (`metadata-api`, Java 21 + PostgreSQL + Flyway) and a Vue 3 + Vite + TypeScript SPA frontend (currently a single-component `App.vue` prototype, pending componentization). See `README.md` → "Current Status" for the accurate picture.
- Do not expand backend scope, authentication/RBAC, production database schema, or complex permission logic beyond what the accepted SDD slices and `PROJECT_RULES.md` allow. New behavior must be represented in `docs/03-spec/` and traced to a slice before implementation.
- Keep the frontend honest about its prototype state; componentization happens only after the relevant SDD slice is accepted.

## Reference Product Policy

- WeKnora may be used only as a product experience and UX benchmark.
- The project may learn from WeKnora's information architecture, interaction flow, knowledge space concept, wiki experience, graph experience, and general product patterns.
- Do not copy WeKnora source code.
- Do not copy WeKnora project structure.
- Do not copy UI assets, icons, images, stylesheets, or proprietary-looking visual details.
- Do not reproduce implementation details from WeKnora.
- Implement Atlas Knowledge Hub independently according to this repository's own architecture, SDD documents, adapter-based converter design, Markdown standard, review workflow, and internal enterprise requirements.
- If a feature is inspired by WeKnora, describe it as "inspired by modern AI-native knowledge products" rather than copying exact behavior.
- Prefer internal product needs over external product parity.
- When proposing or implementing any feature that resembles WeKnora, first map it to an Atlas-specific use case, such as folder/ZIP upload, batch parsing, source trace, SME review, LM Wiki, knowledge graph, or trusted Ask.

## Architecture Rules

- Do not hardcode a single parser engine.
- Wrap parser and converter tools through adapter interfaces.
- Treat `trinity-office` and `document-normalize` as internal tools behind adapters.
- Preserve source trace fields in all Markdown and metadata.
- Treat LLM-generated content as review-required unless verified by an SME or deterministic validation.

## Security And Data Rules

- Do not introduce external cloud calls or external network dependencies.
- Do not commit real company documents, screenshots, credentials, logs, or confidential content.
- Use mock data only.
- Do not embed secrets, tokens, internal endpoints, or private file paths.

## Change Discipline

- Keep changes small, scoped, and documented.
- Update docs when behavior or architecture changes.
- Prefer deterministic processing before LLM enrichment.
- Keep generated documents, raw inputs, assets, reports, and Wiki pages separated.
- Before implementing, identify assumptions, unclear scope, and tradeoffs when the request can be interpreted in multiple ways.
- Prefer the minimum sufficient change that satisfies the user request and the current SDD contract.
- Touch only the files needed for the requested outcome. Do not refactor adjacent areas, restyle unrelated files, or clean up pre-existing issues unless explicitly requested.
- Every changed line should trace to the user request, an SDD requirement, a documented architecture rule, a failing verification, or a required adapter boundary.
- For multi-step tasks, define what "done" means and verify it with a concrete check such as syntax validation, static inspection, tests, or a documented manual checklist.

## SDD Workflow

- Use `docs/00-context/sdd-profile.md` as the project SDD profile before starting implementation slices.
- For feature slices, keep requirements, stories, spec, architecture, design, and tasks under `docs/01-*` through `docs/06-*`.
- Treat `docs/03-spec/` as the behavior source of truth and `docs/06-tasks/` as the implementation checklist.
- SDD is profile-driven. Do not assume one universal document chain; follow the active profile and slice traceability file.
- Before starting implementation for a product slice, verify the expected SDD artifact set exists for that slice: requirements, user stories, spec, architecture, data flow, data model, design, API guide when backend/API work is involved, and tasks.
- If implementation behavior differs from the accepted prototype, update the relevant spec/design/tasks first or call out the mismatch before coding.

## Goal-Driven Slice Execution

- When the user sets a goal for a slice, treat the goal as an execution contract from SDD docs to code to verification.
- For Codex goal mode and loop-style execution, use `docs/00-context/agent-goal-loop-workflow.md` / `.zh-CN.md` plus an execution manifest under `docs/00-context/execution-manifests/`.
- For mixed Codex and GitHub Copilot usage, use `docs/00-context/agent-execution-modes.md` / `.zh-CN.md` to choose the execution surface before starting.
- Use `docs/00-context/agent-goal-loop-quickstart.md` / `.zh-CN.md` to choose the lightest safe workflow tier before starting.
- Generate new manifests with `npm run agent:manifest -- --slice <slice> --mode <single-slice|master>`.
- Check SDD readiness with `npm run agent:check-sdd -- --slice <slice>` and use `docs/00-context/checklists/goal-closeout-gate.md` before close-out.
- Run the closeout gate with `npm run agent:closeout`; the same workflow gate runs automatically on PR/push through the GitHub Actions workflow `Agent Workflow Gate`.
- Use `docs/00-context/goal-prompts/master-goal-prompt.md` for roadmap or slice-queue goals, and `docs/00-context/goal-prompts/single-slice-goal-prompt.md` for one-slice goals.
- Convert the goal into explicit scope, source documents, acceptance criteria, constraints, task checklist, and verification evidence before editing implementation files.
- If required SDD artifacts are missing or stale, create or update them first, then continue into implementation when the scope is clear.
- Execute tasks from `docs/06-tasks/{slice}-tasks.md` and keep implementation changes aligned with `docs/03-spec/{slice}-spec.md`.
- Continue through verification in the same turn when feasible. Do not stop after writing docs or code if verification remains possible.
- Final responses for goal-driven work must summarize documents changed, code changed, verification performed, and any residual risks or blocked checks.

## Quality Gate Execution

- Before completing a slice goal, check the gates in `PROJECT_RULES.md`: goal clarity, SDD completeness, implementation/spec alignment, security and data safety, adapter boundaries, verification, and evidence.
- For prototype work, run lightweight checks such as syntax validation, `git diff --check`, network/dependency scans, secret scans, and manual spec checklist review when relevant.
- For implementation work, add tests appropriate to the layer touched, such as unit tests, integration tests, API contract tests, or end-to-end tests.
- If a check cannot be run, say which check was skipped and why. Do not imply unrun checks passed.
- If a critical gate fails, fix it before reporting completion. If it cannot be fixed in the current goal, report the blocker and do not mark the goal complete.

## Lessons Learned Execution

- When acceptance review finds a mismatch between expected and delivered behavior, decide whether it is a reusable lesson.
- Reusable lessons belong in `docs/00-context/lessons-learned.md`.
- For each lesson, identify the root cause and update the durable prevention point: SDD docs, task verification, development standards, project rules, agent instructions, checklist, or tests.
- Do not rely on memory or chat history to prevent recurrence.
- Final responses should mention any lesson recorded and which prevention artifact was updated.

## Agent Execution Context

- Agents should consume explicit context rather than guessing. Use the current SDD profile, slice docs, task list, repository rules, and user-provided references as the execution manifest for the task.
- Goal loops must resume from durable docs and manifests, not chat memory. Before resuming, read the relevant manifest, traceability/progress docs, and `git status --short`.
- When creating prompts or instructions for another tool or agent, prefer lightweight pointers to existing repository files instead of duplicating long document content.
- Search the repository before assuming file locations, especially for design docs, task docs, prompts, and prototype assets.
- Do not assume runtime versions, framework choices, model providers, database engines, or parser engines beyond what `README.md`, `PROJECT_RULES.md`, and the relevant SDD docs state.
- Keep MCP connectors, model providers, parser engines, vector stores, and storage engines behind product-facing adapter boundaries; do not make the UI depend on tool implementation details.
