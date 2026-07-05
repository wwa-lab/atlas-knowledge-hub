# Project Rules

## Product First

Atlas Knowledge Hub is an internal knowledge product, not a one-off converter script. Features should support repeatable user workflows: upload, convert, normalize, review, publish, browse, and ask.

## SDD First

Use Spec Driven Development before implementation work begins.

- Use `docs/00-context/sdd-profile.md` as the Atlas SDD profile.
- Use `docs/03-spec/` as the behavior source of truth.
- Use `docs/06-tasks/` as the implementation checklist.
- Do not implement behavior that is not represented in the current slice spec.
- Keep slice traceability in `docs/00-context/{slice}-traceability.md`.
- Prototype-only refinements may remain lightweight only when they do not introduce new durable behavior contracts.
- From this point forward, every new or materially updated SDD document must be maintained in two language copies: English and Simplified Chinese.
- Use the default SDD filename for the English copy, such as `docs/01-requirements/{slice}-requirements.md`, and add a `.zh-CN.md` companion for the Chinese copy, such as `docs/01-requirements/{slice}-requirements.zh-CN.md`.
- The two language copies must describe the same scope, decisions, requirement IDs, acceptance criteria, and task IDs. If one copy is updated, update the other copy in the same change.
- Existing single-language historical SDD documents may be migrated gradually, but any future slice work that touches them must either add the missing language copy or explicitly record why the translation is deferred.

## Goal-Driven SDD

Atlas uses a goal-driven SDD mode for implementation slices.

A slice goal is a short execution contract that tells the agent what outcome to deliver end to end. When the user sets a slice goal, the agent should move through document creation or update, implementation, verification, and final evidence in one continuous workflow unless blocked.

For Codex goal mode and loop-style execution, use `docs/00-context/agent-goal-loop-workflow.md` / `.zh-CN.md` as the workflow contract and `docs/00-context/agent-goal-loop-quickstart.md` / `.zh-CN.md` to choose the lightest safe workflow tier. For environments where GitHub Copilot Chat is the primary tool, use `docs/00-context/agent-execution-modes.md` / `.zh-CN.md` and `.github/copilot-instructions.md` to emulate the same workflow with a human loop operator and repository gates. Master goals may manage a roadmap or slice queue, but must execute one slice at a time. Single-slice goals must complete one slice from SDD gate to verification evidence. Loops may iterate and fix scoped failures, but they must not bypass SDD, acceptance, security, data, adapter, verification, or lessons-learned gates. New manifests should be generated with `npm run agent:manifest`, SDD readiness should be checked with `npm run agent:check-sdd`, closeout should run `npm run agent:closeout`, and workflow gates should also run automatically on PR/push through the `Agent Workflow Gate` GitHub Actions workflow.

Each slice goal should include:

- Goal: the user-facing outcome to achieve.
- Slice: the feature slice name and stable slug.
- Scope: what is included and excluded.
- Source of truth: relevant SDD files, prototype surfaces, and product docs.
- Acceptance: observable behavior or documents that prove the goal is complete.
- Verification: commands, checks, or manual review steps required before completion.
- Constraints: mock-only, adapter boundaries, security, phase, and data-safety limits.

For goal-driven SDD work:

- Do not start implementation until the required SDD docs for the slice exist or have been updated.
- Use `docs/06-tasks/{slice}-tasks.md` as the executable checklist.
- Keep goal scope surgical. Split broad goals into multiple slices when one goal would cross unrelated domains.
- Treat verification evidence as part of the deliverable, not an optional summary.
- If the goal conflicts with Atlas phase discipline or product rules, stop and surface the conflict before coding.

## SDD And Implementation Collaboration

Use Claude Code and Codex as complementary roles when helpful, but optimize for the project goal rather than a rigid tool boundary.

Claude Code is the preferred SDD document owner when available. Codex may also generate or update SDD documents when that is the fastest safe path, when Claude Code is not being used, or when implementation work reveals that SDD docs must be corrected before coding can continue.

Use SDD-capable agents for:

- Product-level requirements.
- Slice requirements.
- User stories.
- Specifications.
- Architecture.
- Data flow.
- Data model.
- Design.
- API implementation guides.
- Task plans.
- English and Simplified Chinese SDD document synchronization.
- Traceability and acceptance criteria.
- SDD document quality review.

For full Atlas SDD generation, use `docs/SDD-BOOTSTRAP.md` / `docs/SDD-BOOTSTRAP.zh-CN.md` and the project-local `atlas-sdd-generate-all` workflow. Claude Code uses `.claude/skills/atlas-sdd-generate-all/`; Codex can use the mirrored `.agents/skills/atlas-sdd-generate-all/`. The workflow must orchestrate the project-local SDD skill chain: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality`. The SDD completion report must list the entry skill, downstream skills used, skill files read, and `review-doc-quality` result; if the required project-local skill chain is unavailable or not used, stop and report instead of generating ad hoc SDD documents.

SDD generation should stop at complete, reviewable SDD artifacts, implementation guidance, and task checklists unless the user explicitly asks the same agent to continue into implementation.

Codex is the preferred implementation owner. Use Codex for:

- Reading the accepted SDD documents and task checklist.
- Checking scope, assumptions, and acceptance criteria before editing code.
- Implementing Vue, TypeScript, HTML, CSS, tests, and other code changes.
- Running typecheck, build, unit/component tests, E2E tests, diff hygiene, dependency/network scans, and secret scans.
- Updating implementation-adjacent docs when behavior or verification evidence changes.
- Reporting completed tasks, verification evidence, residual risks, and blocked checks.

Implementation agents must not silently expand product scope beyond the accepted SDD. If the SDD is missing, stale, ambiguous, or conflicts with the current FE baseline or implementation reality, the agent should surface the mismatch and either update the SDD under the same rules or request an SDD update before continuing.

Default handoff, when using both tools:

1. Claude Code or Codex produces or updates the bilingual SDD documents for a slice.
2. The user accepts the SDD scope and tasks.
3. Codex implements strictly against `docs/03-spec/` and `docs/06-tasks/`.
4. Codex reports verification evidence and any SDD/implementation mismatches.
5. Material SDD changes go back through the SDD workflow. Claude Code is preferred when available, but Codex may update the documents directly when it is already handling the goal and follows the same rules.

Source-of-truth hierarchy for implementation:

1. Current user instruction.
2. `PROJECT_RULES.md`, `AGENTS.md`, and `DEVELOPMENT_STANDARDS.md`.
3. Current FE baseline for visual and interaction parity.
4. `docs/03-spec/` for behavior.
5. `docs/06-tasks/` for implementation checklist.
6. Other SDD and product context documents.

## Quality Gates

A slice goal is not complete until quality gates are checked and reported.

Use `DEVELOPMENT_STANDARDS.md` as the detailed engineering standard for coding, frontend, backend, API, database, adapter, security, testing, review, Git, and CI expectations.

Required gates:

1. Goal gate: scope, exclusions, acceptance criteria, verification, and constraints are clear enough to execute without hidden assumptions.
2. Product readiness claim gate: status language must match the verified user outcome. Do not describe work as "ready to test", "usable", "accepted", "done", "out-of-the-box", or product-ready unless the actual user-facing workflow was run from the documented local startup path. If only automated gates, mock surfaces, scripted API setup, or prototype flows were verified, say that explicitly.
3. SDD gate: required slice docs exist or are updated, English and Simplified Chinese copies are synchronized for touched SDD documents, requirement IDs are traceable, tasks map back to the spec, and full SDD generation reports the project-local skill chain used. Missing skill-chain evidence means the SDD gate is incomplete.
4. Implementation gate: changed behavior is represented in `docs/03-spec/`, and code changes are limited to the active goal and slice.
5. Security and data gate: no real company data, secrets, private paths, confidential screenshots, external cloud calls, or raw credentials are introduced.
6. Adapter gate: parser, converter, model, vector database, storage, and search integrations stay behind product-facing adapter boundaries.
7. Verification gate: the checks listed in the task plan are run, or any skipped check is named with a reason.
8. Context status gate: `docs/00-context/{slice}-traceability.md` and, when slice status changes, `docs/00-context/slice-roadmap.md` plus its `.zh-CN.md` companion reflect the final status, task range, verification evidence, and deferred work.
9. Evidence gate: the final response includes documents changed, code changed, verification evidence, residual risks, and whether `docs/00-context` status was checked or updated. Use `docs/00-context/checklists/goal-closeout-gate.md` / `.zh-CN.md` before marking a goal complete.

Prototype-phase verification should include, when relevant:

- Static syntax checks for edited HTML/CSS/JavaScript.
- `git diff --check`.
- Scans for external network calls or dependencies.
- Scans for raw secrets, private paths, or real data.
- Manual UI checklist against the active spec.

Implementation-phase verification should add unit, integration, and end-to-end tests as the Vue frontend, Spring Boot backend, database, and adapter layers become real.

## Lessons Learned

Acceptance mismatches must be turned into durable learning.

- Record reusable lessons in `docs/00-context/lessons-learned.md`.
- Do not treat a chat explanation as sufficient prevention.
- For each lesson, identify the root cause and update the artifact that would have prevented it: requirement, story, spec, architecture, design, task, standard, rule, checklist, or test.
- Add or update verification so the same issue is checked in the next similar slice goal.
- If a lesson changes cross-slice behavior, update `DEVELOPMENT_STANDARDS.md`, `PROJECT_RULES.md`, or `AGENTS.md` as appropriate.

## SDD Skill Source

When generating or updating SDD documents, prefer the skills and workflow patterns from:

- `https://github.com/wwa-lab/Agentic-SDLC-Control-Tower/tree/main/.claude/skills`

The expected document flow is:

1. Requirements.
2. User stories.
3. Specification.
4. Architecture.
5. Data flow.
6. Data model.
7. Design.
8. API implementation guide when backend/API work is involved.
9. Tasks.

Use these skills as the SDD operating model, but adapt outputs to Atlas' lightweight product-first profile.

## Control Tower Reference Rules

Use `wwa-lab/Agentic-SDLC-Control-Tower` as a reference source for mature project rules, SDD discipline, traceability, review gates, and implementation best practices.

- Borrow rules that improve clarity, safety, traceability, or implementation discipline.
- Do not blindly copy rules that are specific to Control Tower's domain, stack, or runtime.
- If a borrowed rule changes Atlas workflow materially, document the reason in the relevant SDD artifact or an ADR.
- Atlas project-local rules take precedence when they are more specific.

## Prototype To Product

The static prototype is a product behavior blueprint, not throwaway artwork.

- Preserve accepted prototype behavior unless the spec explicitly changes it.
- Phase 1 frontend implementation should first reproduce the prototype with mock data.
- Do not introduce backend, database, authentication, external model calls, or production storage only to support prototype behavior.
- Split the prototype into components only after the relevant SDD slice is accepted.

## Parser Neutral

The parser architecture must remain adapter-based. Do not bind the product to one parser engine. Current and future engines may include `document-normalize`, MinerU, Docling, PaddleOCR, internal OCR, or Copilot Vision.

The internal tools `trinity-office` and `document-normalize` are wrapped as converter adapters.

## Adapter Boundaries

Do not call infrastructure tools directly from product workflows.

Use adapter boundaries for:

- Converter tools.
- Parser/OCR tools.
- Agent runtimes and tool-calling frameworks.
- Vector database engines.
- Storage engines.
- Model providers.
- Search providers.

Product logic should depend on product-facing interfaces, not concrete engines such as `document-normalize`, MinerU, Docling, PaddleOCR, PostgreSQL/pgvector, Milvus, Qdrant, Ollama, DeepSeek, GitHub Models, or S3-compatible storage.

## Backend And Agent Architecture

Atlas backend direction is Java + Spring Boot as the control plane.

Spring Boot owns:

- Product APIs and validation.
- Knowledge Space, batch, file, Wiki, review, graph, member, model, and settings metadata.
- Workflow status and state transitions.
- Authentication, RBAC, and audit when production security is introduced.
- Agent run, tool invocation, artifact, and review-required output records.
- Adapter registry and policy decisions.

Agent, parser, OCR, document AI, model, vector, storage, and search execution belongs behind an adapter/worker plane. Workers may be implemented in Python, Node.js, Go, Java, or another appropriate runtime. The core backend must not depend directly on one agent SDK, one MCP runtime, one parser, one model provider, or one vector database.

Agent-generated or LLM-generated outputs are untrusted by default and must remain review-required unless SME-approved or deterministically validated. Store or expose source trace, tool-call metadata, artifacts, confidence/evidence, errors, and audit events when available.

## Trace And Review

- Source trace is mandatory for Markdown and metadata.
- Review status is mandatory for Markdown and metadata.
- LLM-generated content is review-required unless verified.
- Confidence values must be retained when available.

## Data Safety

- Do not commit real company documents.
- Do not commit confidential screenshots, credentials, logs, exports, or raw customer content.
- Use mock/sample files only.
- Do not embed API keys, passwords, tokens, private endpoints, internal hostnames, or private absolute paths.
- API keys and passwords must never appear in frontend source.
- Mock configuration may show masked or status-only secret fields such as `configured`, but never raw secret values.

## Auth And RBAC

Registration, account settings, API information, and member management may be represented in the prototype with mock data.

For real implementation:

- Authentication must be enforced by the backend.
- RBAC must be enforced by the backend, not only by frontend UI state.
- Member management operations must be auditable.
- Passwords must never be logged or stored in plaintext.
- API keys must be write-only on creation/rotation and masked in responses.
- Do not implement production authentication, SSO, or complex permission logic until the relevant SDD slice is accepted.

## Lightweight MVP

Keep the MVP small and understandable. Prefer static HTML/CSS/JS and documentation before introducing a framework, service mesh, database, or queue.

## Phase Discipline

Use staged implementation:

1. Phase 0: static prototype and SDD artifacts.
2. Phase 1: Vue frontend shell with mock data.
3. Phase 2: backend metadata API and persistence.
4. Phase 3: converter/parser/storage/vector/model adapters.
5. Phase 4: review, publish, graph, and Ask integration hardening.

Do not skip phases unless the spec and tasks explicitly approve the change.

## Technology Decisions

- Use lightweight SDD for feature planning when implementation begins.
- Frontend direction: Vue 3 + Vite + TypeScript after prototype validation.
- Backend direction: Java + Spring Boot.
- Database direction: PostgreSQL.
- Migration direction: Flyway.
- Do not introduce framework scaffolding, a production database, or migration files before the project reaches the relevant implementation phase or the user explicitly asks.

## Workspace Separation

Keep these assets separate:

- Raw documents.
- Generated Markdown.
- Extracted images/assets.
- Batch reports.
- Published Wiki pages.

## Processing Order

Prefer deterministic processing before LLM enrichment:

1. File inventory.
2. Office-to-PDF conversion.
3. PDF parsing.
4. Markdown normalization.
5. Confidence and trace validation.
6. Review queue.
7. Optional LLM enrichment.
8. SME approval.
9. Wiki publication.
