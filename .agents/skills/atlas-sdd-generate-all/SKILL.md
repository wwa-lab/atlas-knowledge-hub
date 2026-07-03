---
name: atlas-sdd-generate-all
description: >
  Atlas Knowledge Hub project-specific SDD orchestration skill. Use this skill when the user asks
  Claude Code, Codex, or another SDD-capable agent to generate all SDD documents for an Atlas slice,
  bootstrap a feature slice, create bilingual SDD docs, or prepare implementation-ready documentation for Codex. This skill produces
  the full Atlas SDD document set in English and Simplified Chinese, using current project rules,
  FE baseline, product docs, and imported Control Tower SDD skills as the operating model.
---

# atlas-sdd-generate-all

Generate the complete Atlas Knowledge Hub SDD document set for one product slice.

This is a project-local orchestration skill. It does not replace the smaller SDD skills such as
`req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, and
`design-to-tasks`; it coordinates them into one Atlas-specific workflow.

## Mandatory Skill Chain

When generating a full SDD set, the SDD-generating agent must use the project-local SDD skills as a chain, not write every document from scratch in one pass.

Use this order:

| Order | Skill | Input | Output |
|---|---|---|---|
| 1 | `atlas-sdd-generate-all` | User goal, product docs, current FE baseline, project rules | Slice contract, orchestration, final consistency gate |
| 2 | `req-to-user-story` | Product requirement notes and slice scope | User stories and acceptance criteria |
| 3 | `user-story-to-spec` | User stories and requirements | Implementation-facing spec |
| 4 | `spec-to-architecture` | Spec and project architecture rules | Architecture, data flow, data model outline |
| 5 | `architecture-to-design` | Architecture, data flow, data model, FE baseline | Detailed design and implementation contracts |
| 6 | `design-to-tasks` | Design document | Implementation task breakdown for Codex |
| 7 | `review-doc-quality` | Full generated SDD set | Quality review, missing coverage, bilingual consistency issues |

Use `architecture-review` when the slice introduces or materially changes architecture, adapter boundaries, backend/API contracts, persistence, security, or data flow.

Use `review-code-against-design` only after Codex implementation exists; it is not part of the initial SDD generation pass.

The orchestration skill is responsible for writing the final files to Atlas paths and ensuring the outputs from the smaller skills are reconciled into one coherent bilingual SDD set.

## When To Use

Use this skill when the user says things like:

- "为这个 slice 生成完整 SDD 文档"
- "一键生成 SDD"
- "生成 Phase 1A 的 SDD"
- "prepare SDD docs for Codex implementation"
- "create bilingual requirements/stories/spec/design/tasks"
- "bootstrap a new Atlas feature slice"

## Required Project Context

Before generating documents, read the current repository context:

1. `PROJECT_RULES.md`
2. `AGENTS.md`
3. `DEVELOPMENT_STANDARDS.md`
4. `docs/00-context/sdd-profile.md`
5. `docs/01-requirements/requirement.md`
6. Relevant existing slice docs under `docs/01-*` through `docs/06-*`
7. Current FE baseline when the slice touches frontend behavior:
   - `frontend/public/atlas-prototype.html`
   - `prototypes/index.html`

If any required context file is missing, record it in Open Questions and continue with the safest available context.

## Agent / Implementation Boundary

Claude Code is the preferred SDD document generator when available. Codex may also generate or update SDD documents when that is the fastest safe path or when implementation reveals that the docs must be repaired.

Codex is the preferred implementation agent.

This skill must produce complete, reviewable SDD artifacts and task checklists for implementation. Do not perform broad implementation changes as part of this skill unless the user explicitly asks the same agent to continue into implementation.

## Output Language Rule

Every SDD document produced or materially updated by this skill must have two copies:

- English: default file name, for example `docs/03-spec/{slice}-spec.md`
- Simplified Chinese: `.zh-CN.md` companion, for example `docs/03-spec/{slice}-spec.zh-CN.md`

Both copies must preserve the same:

- Scope
- Requirement IDs
- User story IDs
- Acceptance criteria
- Architecture decisions
- API contracts
- Task IDs
- Verification requirements
- Open questions

Do not translate IDs. Keep stable identifiers identical across languages.

## Required Inputs

If the user does not provide a structured goal, infer a minimal safe one and document assumptions.

Recommended input shape:

```text
Goal: <user-facing outcome>
Slice: <stable slice slug>
Scope: <included behavior and explicit exclusions>
Sources: <prototype/docs/references>
Acceptance: <observable completion criteria>
Verification: <commands/checks/manual review>
Constraints: <mock-only, adapter, security, phase, data-safety limits>
```

If the slice name is missing, propose a kebab-case slug before writing files.

## Complete Atlas SDD Document Set

Generate or update these files:

1. Requirements
   - `docs/01-requirements/{slice}-requirements.md`
   - `docs/01-requirements/{slice}-requirements.zh-CN.md`
2. User Stories
   - `docs/02-user-stories/{slice}-stories.md`
   - `docs/02-user-stories/{slice}-stories.zh-CN.md`
3. Specification
   - `docs/03-spec/{slice}-spec.md`
   - `docs/03-spec/{slice}-spec.zh-CN.md`
4. Architecture
   - `docs/04-architecture/{slice}-architecture.md`
   - `docs/04-architecture/{slice}-architecture.zh-CN.md`
5. Data Flow
   - `docs/04-architecture/{slice}-data-flow.md`
   - `docs/04-architecture/{slice}-data-flow.zh-CN.md`
6. Data Model
   - `docs/04-architecture/{slice}-data-model.md`
   - `docs/04-architecture/{slice}-data-model.zh-CN.md`
7. Design
   - `docs/05-design/{slice}-design.md`
   - `docs/05-design/{slice}-design.zh-CN.md`
8. API Implementation Guide, only when backend/API work is involved
   - `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md`
   - `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.zh-CN.md`
9. Tasks
   - `docs/06-tasks/{slice}-tasks.md`
   - `docs/06-tasks/{slice}-tasks.zh-CN.md`
10. Traceability
   - `docs/00-context/{slice}-traceability.md`
   - `docs/00-context/{slice}-traceability.zh-CN.md`

For frontend-only Phase 1 slices, the API guide may be omitted only if the tasks explicitly state that no backend/API contract is in scope. Record that decision in traceability.

## Workflow

### Step 1: Establish Slice Contract

Define:

- Slice slug
- Phase
- Goal
- In-scope behavior
- Out-of-scope behavior
- Source documents
- FE baseline surfaces if relevant
- Acceptance criteria
- Verification requirements
- Security and data constraints

### Step 2: Requirements

Create requirements with stable IDs:

```text
REQ-{SLICE}-001
REQ-{SLICE}-002
...
```

Requirements must distinguish:

- Current phase behavior
- Future phase behavior
- Mock-only behavior
- Real implementation behavior
- Explicit exclusions

Then use the `req-to-user-story` skill to transform these requirements into user stories and acceptance criteria.

### Step 3: User Stories

Convert requirements into actor-centered stories with stable IDs:

```text
US-{SLICE}-001
US-{SLICE}-002
...
```

Use Given/When/Then acceptance criteria. Include assumptions, dependencies, out-of-scope items, and open questions.

### Step 4: Specification

Write implementation-facing behavior:

- Page/view behavior
- State transitions
- Empty states
- Error states
- Interaction rules
- Acceptance matrix mapping requirements to observable checks
- Mock-only and no-network constraints

Treat `docs/03-spec/` as the behavior source of truth.

Use the `user-story-to-spec` skill for this conversion and then adapt the result to Atlas paths, phase discipline, FE baseline, mock-only rules, and bilingual output requirements.

### Step 5: Architecture, Data Flow, And Data Model

Write architecture docs that name component ownership and boundaries.

For Atlas, always call out:

- FE component boundaries
- Backend/API boundaries if in scope
- Parser/converter/model/vector/storage adapter boundaries
- Source trace, confidence, and review status preservation
- Security and data-safety constraints

Use Mermaid only when helpful and keep syntax simple.

Use the `spec-to-architecture` skill to derive architecture from the spec. Split the result into architecture, data flow, and data model documents according to the Atlas SDD profile.

### Step 6: Design

Write UX and implementation design:

- Layout and interaction model
- Component inventory
- Data contracts and props/state expectations
- Visual system notes
- Responsive behavior
- Accessibility and keyboard considerations when relevant
- Test hooks or stable selectors when relevant

For frontend work, current FE baseline is the visual and interaction parity target.

Use the `architecture-to-design` skill to derive design from architecture. For frontend slices, ground visual and interaction decisions in `frontend/public/atlas-prototype.html` and `prototypes/index.html`.

### Step 7: API Guide

Generate an API guide only when backend/API work is in scope.

Include:

- Endpoint list
- Request/response JSON examples
- Error shapes
- Validation rules
- Pagination/filtering if relevant
- Security and secret handling
- Frontend integration expectations
- Contract test expectations

Do not invent production APIs for frontend-only mock slices.

### Step 8: Tasks

Create implementation tasks for Codex.

Tasks must:

- Map to requirement IDs and spec sections
- Be scoped for one implementation slice
- Include owner type, priority, dependencies, verification
- Avoid vague "implementation will decide" language
- State no-backend/no-network/mock-only constraints when applicable
- Include exact validation commands where known

Use the `design-to-tasks` skill to derive implementation tasks from the design. Then adapt task IDs, dependencies, verification, and Codex handoff notes to Atlas conventions.

### Step 9: Traceability

Create a traceability file linking:

- Source docs and FE surfaces
- Requirements
- User stories
- Spec sections
- Architecture/data/design decisions
- Tasks
- Verification evidence plan
- Open questions and deferred translations if any

## Output Quality Gate

Before finishing, verify:

- [ ] English and Chinese files exist for every touched SDD artifact
- [ ] IDs match across both languages
- [ ] Requirements map to stories/spec/tasks
- [ ] Tasks are actionable for Codex
- [ ] The slice does not skip Atlas phase discipline
- [ ] The slice preserves mock-only and no-external-call constraints when applicable
- [ ] Adapter boundaries are explicit
- [ ] Current FE baseline is referenced for frontend work
- [ ] Open questions are explicit instead of silently decided
- [ ] If backend/API is not in scope, the API guide omission is documented
- [ ] `review-doc-quality` has been applied to the generated SDD set, or the final response explains why it was skipped
- [ ] The final SDD set records which sub-skills were used and any important assumptions introduced during reconciliation

## Final Response Format

When done, summarize:

- Slice slug
- Files created/updated
- Whether API guide was included or deferred
- Key assumptions
- Open questions
- Recommended Codex handoff command, for example:

```text
Codex: implement {slice} strictly against docs/03-spec/{slice}-spec.md and docs/06-tasks/{slice}-tasks.md. Do not expand scope.
```
