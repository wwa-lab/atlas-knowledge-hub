# SDD Profile: atlas-lightweight-product-sdd

## Applies To

This profile applies to Atlas Knowledge Hub while it is moving from static prototype to implementation-ready product slices.

Atlas is a lightweight internal knowledge product. SDD should preserve the product-first workflow without forcing production infrastructure before the MVP requires it.

## Profile Type

Custom profile derived from:

- `frontend-spa-sdd` for UI-heavy product slices.
- `standard-java-sdd` for future backend/API slices.
- Atlas project rules for parser-neutral adapters, traceability, review workflow, and mock-only prototype data.

## Document Chain

| Order | Stage | Required | Default Path | Purpose |
|---|---|---|---|---|
| 0 | Context | Yes | `docs/00-context/` | Durable project and slice traceability context. |
| 1 | Requirements | Yes | `docs/01-requirements/{slice}-requirements.md` | Product requirements and scope boundaries. |
| 2 | User Stories | Yes | `docs/02-user-stories/{slice}-stories.md` | Actor-centered acceptance behavior. |
| 3 | Specification | Yes | `docs/03-spec/{slice}-spec.md` | Source of truth for behavior, states, and acceptance. |
| 4 | Architecture | Yes | `docs/04-architecture/{slice}-architecture.md` | Component ownership and architectural constraints. |
| 5 | Data Flow | Yes | `docs/04-architecture/{slice}-data-flow.md` | Workflow and state transitions. |
| 6 | Data Model | Yes | `docs/04-architecture/{slice}-data-model.md` | Entities, fields, and state contracts. |
| 7 | Design | Yes | `docs/05-design/{slice}-design.md` | UX, interaction, and component design. |
| 8 | API Guide | Required before backend implementation | `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md` | Internal API and adapter contract expectations. |
| 9 | Tasks | Yes | `docs/06-tasks/{slice}-tasks.md` | Implementation tasks and verification plan. |

## Goal-Driven Execution

Atlas slices may be executed in Goal-Driven SDD mode.

In this mode, a user-provided goal becomes the execution contract for one slice. The agent should use the goal to produce or refresh the SDD document chain, implement the accepted tasks, verify the result, and report evidence.

Recommended goal shape:

```text
Goal: <user-facing outcome>
Slice: <stable slice name or slug>
Scope: <included behavior and explicit exclusions>
Sources: <prototype/docs/references to use>
Acceptance: <observable completion criteria>
Verification: <commands, checks, or manual review steps>
Constraints: <mock-only, adapter, security, phase, or data-safety limits>
```

If the user gives only a plain-language goal, infer a minimal safe scope from the current product docs and ask only when the ambiguity would materially change architecture, security, data handling, or user-facing behavior.

## Gates

- Prototype-only changes may remain lightweight when they do not introduce new behavior contracts.
- A product slice must have requirements, stories, spec, architecture, design, and tasks before implementation starts.
- Backend work must not start until the API guide and data model are accepted.
- Parser/converter integration work must keep tools behind adapters and must not hardcode `trinity-office`, `document-normalize`, or any future parser directly into product workflows.
- Ask, graph, and Wiki publication behavior must preserve source trace, confidence, and review status.
- Real company documents, private paths, credentials, screenshots, logs, and external cloud calls are out of bounds for prototypes and tests.
- Goal-driven execution is complete only when docs, implementation, and verification evidence have all been reported, or when a blocker is explicitly recorded.

## Quality Gates

Every goal-driven slice should pass these gates before it is reported complete:

| Gate | Required Evidence |
|---|---|
| Goal clarity | Goal, slice, scope, exclusions, acceptance, verification, and constraints are known or safely inferred. |
| SDD completeness | Required slice docs exist or are updated; tasks map to requirements and spec behavior. |
| Spec alignment | Implementation changes match `docs/03-spec/{slice}-spec.md`; spec/design/tasks are updated when behavior changes. |
| Security and data safety | No real company data, secrets, raw credentials, private paths, confidential screenshots, or external cloud calls are introduced. |
| Adapter boundaries | Parser, converter, model, vector database, storage, and search behavior stays behind product-facing interfaces. |
| Verification | Commands, tests, scans, or manual checks from the task plan are run or explicitly marked blocked/skipped with reasons. |
| Completion evidence | Final report names changed docs, changed code, verification performed, and residual risks. |
| Lessons learned | Acceptance mismatches that may recur are recorded in `docs/00-context/lessons-learned.md` and linked to updated prevention artifacts. |

Prototype-phase checks may be lightweight, but they should still include static syntax validation, `git diff --check`, dependency/network scans, secret scans, and manual UI review when relevant.

Implementation-phase checks should add automated unit, integration, API contract, and end-to-end tests according to the layer being changed.

## Traceability

Each slice should maintain a traceability file in `docs/00-context/{slice}-traceability.md` linking:

- Product docs and prototype surfaces.
- Requirements.
- User stories.
- Spec sections.
- Architecture and design decisions.
- Tasks and verification.

Requirement IDs use `REQ-{SLICE}-###`.
User story IDs use `US-{SLICE}-###`.
Task IDs use `T-{SLICE}-###`.

## Tool Routing

- Codex: use this profile before creating or implementing a new Atlas product slice.
- Claude Code: follow the same paths and IDs when operating in this repo.
- OpenCode or other agents: treat `docs/03-spec/` as the behavior source of truth and `docs/06-tasks/` as the implementation checklist.

## Relationship To Existing Docs

Existing top-level docs remain durable product context:

- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/architecture.md`
- `docs/batch-processing-design.md`
- `docs/markdown-standard.md`
- `docs/knowledge-graph-design.md`
- `docs/review-workflow.md`
- `docs/technology-decisions.md`

Slice documents should refine these sources, not redefine them silently.
