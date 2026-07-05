# SDD Bootstrap: Atlas Knowledge Hub

## Purpose

This document is the Atlas Knowledge Hub starter guide for Spec Driven Development. It replaces the imported Control Tower bootstrap content with Atlas-specific rules, paths, roles, skills, and quality gates.

Use it whenever a new Atlas slice needs to move from product intent to an implementation-ready task checklist.

## Operating Model

Atlas optimizes for the goal, not for a rigid tool boundary.

- Claude Code is the preferred SDD document generator when available.
- Codex may also generate or update SDD documents when that is the fastest safe path or when implementation reveals that docs must be repaired.
- Codex is the preferred implementation agent.
- Any agent that writes SDD must follow the same Atlas bootstrap, bilingual output rules, skill chain, traceability rules, and quality gates.

Do not treat SDD generation as Claude-only. Do not treat implementation as permission to ignore SDD.

## One-Entry SDD Skill

For Atlas slice documentation, start from:

- `.claude/skills/atlas-sdd-generate-all/SKILL.md` for Claude Code.
- `.agents/skills/atlas-sdd-generate-all/SKILL.md` for Codex visibility and use.

This skill is the one-entry workflow for generating the full bilingual SDD set. It must orchestrate the project-local SDD skills instead of writing every document ad hoc.

## Mandatory Skill Chain

Use the project-local skills in this order:

| Order | Skill | Purpose |
|---|---|---|
| 1 | `atlas-sdd-generate-all` | Establish slice contract, orchestration, file paths, bilingual rules, and final consistency gate. |
| 2 | `req-to-user-story` | Convert requirements and slice scope into user stories and acceptance criteria. |
| 3 | `user-story-to-spec` | Convert stories into implementation-facing behavior. |
| 4 | `spec-to-architecture` | Derive architecture, data flow, and data model from the spec. |
| 5 | `architecture-to-design` | Derive UX, component, API, data, and implementation design. |
| 6 | `design-to-tasks` | Convert design into Codex-ready implementation tasks. |
| 7 | `review-doc-quality` | Review completeness, traceability, quality, and bilingual consistency. |

Use `architecture-review` when the slice materially changes architecture, backend/API contracts, persistence, adapter boundaries, security, or data flow.

Use `review-code-against-design` only after implementation exists; it is not part of the initial SDD generation pass.

## Required Context Before Generating SDD

The SDD-generating agent must read or explicitly account for:

- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/SDD-BOOTSTRAP.md`
- `docs/SDD-BOOTSTRAP.zh-CN.md`
- `docs/00-context/sdd-profile.md`
- `docs/01-requirements/requirement.md`
- Relevant existing slice documents under `docs/01-*` through `docs/06-*`
- Current FE baseline when frontend behavior is involved:
  - `frontend/public/atlas-prototype.html`
  - `prototypes/index.html`

If required context is missing or stale, record that in Open Questions or Assumptions rather than silently inventing a replacement.

## Recommended Slice Goal Shape

Use this structure when asking an agent to generate SDD documents:

```text
Goal: <user-facing outcome>
Slice: <stable kebab-case slice slug>
Phase: <Phase 1 / Phase 2 / Phase 3 / Phase 4>
Scope: <included behavior and explicit exclusions>
Sources: <product docs, FE baseline, references>
Acceptance: <observable completion criteria>
Verification: <commands, checks, or manual review>
Constraints: <mock-only, adapter, security, phase, data-safety limits>
```

If the user provides only a plain-language goal, infer a minimal safe slice contract and document assumptions.

## Bilingual Output Rule

Every new or materially updated SDD document must have two copies:

- English: default filename.
- Simplified Chinese: `.zh-CN.md` companion.

Examples:

- `docs/01-requirements/{slice}-requirements.md`
- `docs/01-requirements/{slice}-requirements.zh-CN.md`
- `docs/03-spec/{slice}-spec.md`
- `docs/03-spec/{slice}-spec.zh-CN.md`

Both language copies must preserve the same scope, requirement IDs, user story IDs, acceptance criteria, architecture decisions, API contracts, task IDs, verification requirements, and open questions.

Do not translate stable IDs.

## Atlas SDD Document Set

Generate or update the following documents for each slice.

| Stage | English path | Chinese path | Required |
|---|---|---|---|
| Traceability | `docs/00-context/{slice}-traceability.md` | `docs/00-context/{slice}-traceability.zh-CN.md` | Yes |
| Requirements | `docs/01-requirements/{slice}-requirements.md` | `docs/01-requirements/{slice}-requirements.zh-CN.md` | Yes |
| User Stories | `docs/02-user-stories/{slice}-stories.md` | `docs/02-user-stories/{slice}-stories.zh-CN.md` | Yes |
| Specification | `docs/03-spec/{slice}-spec.md` | `docs/03-spec/{slice}-spec.zh-CN.md` | Yes |
| Architecture | `docs/04-architecture/{slice}-architecture.md` | `docs/04-architecture/{slice}-architecture.zh-CN.md` | Yes |
| Data Flow | `docs/04-architecture/{slice}-data-flow.md` | `docs/04-architecture/{slice}-data-flow.zh-CN.md` | Yes |
| Data Model | `docs/04-architecture/{slice}-data-model.md` | `docs/04-architecture/{slice}-data-model.zh-CN.md` | Yes |
| Design | `docs/05-design/{slice}-design.md` | `docs/05-design/{slice}-design.zh-CN.md` | Yes |
| API Guide | `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.zh-CN.md` | Required before backend/API work |
| Tasks | `docs/06-tasks/{slice}-tasks.md` | `docs/06-tasks/{slice}-tasks.zh-CN.md` | Yes |

For frontend-only Phase 1 slices, the API Guide may be deferred only when the tasks and traceability file explicitly say that backend/API work is out of scope.

## Document Order

Follow this order:

1. Establish slice contract.
2. Requirements.
3. User stories.
4. Specification.
5. Architecture.
6. Data flow.
7. Data model.
8. Design.
9. API Guide, when backend/API work is in scope.
10. Tasks.
11. Traceability update.
12. Document quality review.
13. User acceptance of SDD scope.
14. Implementation handoff.

## Minimum Gate Before Code

Do not start implementation until the slice can answer:

- What problem is this slice solving?
- Who are the users or actors?
- What is in scope?
- What is explicitly out of scope?
- What must happen in the happy path?
- What must happen in empty, error, loading, and edge states?
- What data shape or state contract is needed?
- Which component, module, API, or adapter owns each responsibility?
- How will acceptance be checked?
- Which verification commands or manual checks are required?
- Are English and Chinese SDD copies synchronized?

## Phase Discipline

Atlas uses staged delivery:

| Phase | Scope |
|---|---|
| Phase 0 | Static prototype and SDD artifacts. |
| Phase 1 | Vue frontend shell and mock data. |
| Phase 2 | Backend metadata API and persistence. |
| Phase 3 | Converter/parser/storage/vector/model adapters and processing pipeline. |
| Phase 4 | Production hardening: auth, RBAC, secrets, audit, monitoring, deployment. |

Do not skip phases unless the spec and tasks explicitly approve the change.

## Current Product Baseline

For current Phase 1 frontend work:

- `frontend/public/atlas-prototype.html` is the FE fidelity baseline.
- `prototypes/index.html` mirrors the FE baseline for static review.
- Visual and interaction parity should follow the current FE baseline, not the older dialogue-first prototype.

## Atlas-Specific Constraints

Every SDD slice must preserve these constraints:

- Mock-only for Phase 1.
- No real company documents.
- No credentials, raw API keys, tokens, private paths, or confidential screenshots.
- No external network calls or external cloud dependencies in prototype/frontend mock work.
- Parser, converter, model, vector database, storage, and search integrations stay behind adapter boundaries.
- Agent runtimes and tool-calling frameworks stay behind adapter/worker boundaries.
- `trinity-office` and `document-normalize` are internal tools behind adapters, not product workflow dependencies.
- Source trace, confidence, and review status remain visible in Wiki, Review, Graph, and Ask surfaces.
- LLM-generated or LLM-modified content remains review-required unless SME-approved or deterministically validated.

## Implementation Handoff

After the bilingual SDD set is produced and the user accepts the scope, handoff should be explicit:

```text
Codex: implement <slice> strictly against docs/03-spec/<slice>-spec.md and
docs/06-tasks/<slice>-tasks.md. Do not expand scope. Preserve current FE
baseline and run the verification listed in the tasks.
```

Codex should report documents read, code changed, tests and checks run, skipped checks with reasons, residual risks, and any SDD/implementation mismatch.

If Codex is also generating or repairing SDD documents, it must first complete the SDD gate, then proceed to implementation only when the scope is clear and accepted or safely inferable.

For Codex goal mode, use:

- `docs/00-context/agent-goal-loop-workflow.md` / `.zh-CN.md` as the loop workflow contract.
- `docs/00-context/agent-goal-loop-quickstart.md` / `.zh-CN.md` to choose the workflow tier.
- `docs/00-context/execution-manifests/TEMPLATE.yaml` as the manifest template.
- `docs/00-context/goal-prompts/master-goal-prompt.md` for roadmap or slice-queue execution.
- `docs/00-context/goal-prompts/single-slice-goal-prompt.md` for one-slice execution.
- `docs/00-context/checklists/sdd-generation-gate.md` / `.zh-CN.md` before accepting SDD output.
- `docs/00-context/checklists/goal-closeout-gate.md` / `.zh-CN.md` before marking a goal complete.
- `npm run agent:manifest -- --slice <slice> --mode <single-slice|master>` to create a manifest.
- `npm run agent:check-sdd -- --slice <slice>` to run the SDD gate checker.
- `npm run agent:closeout` to run the local closeout gate; the GitHub Actions `Agent Workflow Gate` runs the same gate automatically on PR/push.

## Quality Gates

Before SDD handoff, verify:

- English and Chinese SDD files exist for every touched artifact.
- IDs match across both languages.
- Requirements map to stories, spec, design, and tasks.
- The spec is the behavior source of truth.
- The tasks are actionable for Codex.
- API Guide is present when backend/API work is in scope.
- API Guide deferral is documented when frontend-only work omits it.
- Adapter boundaries are explicit.
- Current FE baseline is referenced for frontend work.
- Open questions are explicit.
- `review-doc-quality` has been applied or the reason for skipping is recorded.

Before implementation completion, verify according to the active task plan. For Phase 1 frontend work, this usually includes:

- `npm run typecheck`
- `npm run build`
- `npm run test:coverage`
- `npm run e2e`
- `git diff --check`
- Dependency/network scan.
- Secret/private-path scan.

## Rule Of Thumb

If an implementation decision cannot be traced to a requirement, story, spec, design, or task, the SDD is not ready for implementation.
