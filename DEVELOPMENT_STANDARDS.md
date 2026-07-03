# Development Standards

These standards define how Atlas Knowledge Hub work should be planned, implemented, reviewed, and verified.

They are intentionally staged. Phase 0 keeps the prototype lightweight. Later phases add stronger automated tests, CI, backend contracts, database controls, and security gates when the related implementation layer exists.

## Standard Model

Atlas uses a layered quality model:

1. Goal-driven SDD defines the intended outcome.
2. The active SDD slice defines behavior and acceptance.
3. Implementation follows the current phase and adapter boundaries.
4. Verification evidence proves the goal is complete.
5. Residual risks are reported instead of hidden.

A slice goal is complete only when the relevant documents, implementation, verification evidence, and residual risk report are all complete.

## Phase Applicability

| Phase | Focus | Required Standard Level |
|---|---|---|
| Phase 0 | Static prototype and SDD artifacts | Documentation, prototype safety, static checks, mock-only data. |
| Phase 1 | Vue frontend shell with mock data | TypeScript, component tests, frontend lint/typecheck, E2E for key flows. |
| Phase 2 | Backend metadata API and persistence | API contracts, validation, integration tests, PostgreSQL/Flyway discipline. |
| Phase 3 | Converter/parser/storage/vector/model adapters | Adapter contract tests, failure handling, secret handling, engine isolation. |
| Phase 4 | Review, publish, graph, and Ask hardening | End-to-end traceability, RBAC enforcement, evidence quality, operational checks. |

## Goal And SDD Standards

- Every meaningful implementation slice starts from a goal.
- A goal must define slice, scope, exclusions, acceptance, verification, and constraints.
- Required SDD artifacts must exist or be updated before implementation starts.
- `docs/03-spec/` is the behavior source of truth.
- `docs/06-tasks/` is the implementation checklist.
- Requirement IDs, user story IDs, task IDs, and verification evidence must remain traceable.
- If implementation behavior changes, update spec, design, and tasks before or with the code change.
- If slice implementation or acceptance status changes, update `docs/00-context/{slice}-traceability.md` and the slice row in `docs/00-context/slice-roadmap.md` / `.zh-CN.md` before close-out.
- Do not create parallel planning systems for the same change.

## Coding Standards

- Prefer readable, boring code over clever code.
- Keep changes small and scoped to the active goal.
- Use clear names that describe domain intent.
- Avoid speculative abstractions and features.
- Use immutable updates for UI/application state unless a local performance reason is documented.
- Keep functions small and focused.
- Avoid deep nesting; prefer guard clauses and explicit state handling.
- Replace magic numbers and strings with named constants when they encode product behavior.
- Add comments to explain why, not what.
- Public APIs, adapter interfaces, and non-obvious domain logic should have concise documentation.
- Do not leave unused helpers, dead code, console debugging, or stale mock branches created by the current change.

## Frontend Standards

Phase 0 prototype:

- Keep `prototypes/index.html` static and directly openable.
- Do not introduce a build step, framework, CDN, external image dependency, or external network call.
- Use mock data only.
- Preserve accepted prototype behavior unless the SDD spec changes.
- Keep language switching, day/night mode, settings, review, graph, wiki, and ask surfaces readable and coherent.
- Validate edited JavaScript syntax before completion.

Phase 1 Vue frontend:

- Use Vue 3, Vite, and TypeScript.
- Organize by feature/domain rather than by generic file type only.
- Keep UI components data-driven and replaceable.
- Reproduce accepted prototype behavior with mock data before connecting real APIs.
- Model UI state explicitly; avoid hidden global state.
- Use typed props, typed emits, and typed domain models.
- Component and unit tests are required for non-trivial state mapping, formatting, and interaction logic.
- Playwright E2E tests are required for critical flows such as home navigation, knowledge space detail, review workflow, and Ask evidence display.

## Backend Standards

Backend standards apply when Phase 2 starts:

- Use Java and Spring Boot.
- Organize code by feature/domain.
- Keep API/controller, application/service, adapter, repository, and migration responsibilities separate.
- Use DTO validation at system boundaries.
- Return a consistent API response envelope.
- Do not leak internal exceptions, secrets, private paths, or stack traces in user-facing responses.
- Keep business workflow orchestration out of controllers.
- Keep infrastructure tools behind adapters.
- Add unit tests for domain logic and integration tests for API contracts and persistence behavior.

## API Standards

- Define API behavior in the slice API implementation guide before backend implementation.
- Use stable resource names and predictable HTTP semantics.
- Include pagination, filtering, and status fields when list endpoints can grow.
- Return masked secret state only; never return raw API keys, passwords, or tokens.
- Error responses should be actionable but should not expose sensitive internals.
- API changes must update SDD contracts, data model docs, and task verification.

## Database Standards

Database standards apply when persistent metadata is introduced:

- Use PostgreSQL.
- Use Flyway for schema migrations.
- Do not rely on automatic schema mutation for shared or production-like environments.
- Each migration must support a documented behavior or data contract.
- Preserve source trace, review status, confidence, and lifecycle status fields where relevant.
- Add indexes intentionally for query paths that are part of accepted behavior.
- Seed data must be mock/sample only.

## Adapter Standards

- Product logic must not call parser, converter, model, vector database, storage, or search engines directly.
- Adapter interfaces should expose Atlas product concepts, not vendor-specific implementation details.
- Each adapter should define capability metadata, configuration shape, success output, failure output, and retry/error behavior.
- Adapter failures must preserve enough context for review and troubleshooting without exposing secrets or private paths.
- Do not hardcode one parser, model provider, vector store, storage engine, or search engine as the only possible implementation.

## Security And Data Standards

- Do not commit real company documents, screenshots, logs, exports, or customer content.
- Do not commit API keys, passwords, tokens, private endpoints, private absolute paths, or raw credentials.
- Prototype configuration may show masked or status-only fields such as `configured`, never raw secrets.
- Authentication and RBAC must be enforced by the backend when implemented.
- Member management, API key rotation, and permission changes must be auditable when implemented.
- Input validation is required at every real system boundary.
- External cloud calls are out of scope unless a future SDD slice explicitly approves them.

## Testing And Verification Standards

Phase 0 verification:

- Run static syntax checks for edited HTML/CSS/JavaScript when relevant.
- Run `git diff --check`.
- Scan for external network calls and new dependencies.
- Scan for raw secrets, private paths, and real data.
- Use a manual UI checklist against the active spec.
- Confirm `docs/00-context` status files are current for the touched slice, especially traceability, slice roadmap, verification evidence, and deferred work.

Phase 1+ verification:

- Add unit tests for pure logic, mappers, and state transitions.
- Add component tests for meaningful UI behavior.
- Add integration tests for APIs, persistence, and adapter contracts.
- Add Playwright E2E tests for critical user flows.
- Report skipped checks with reasons. Do not imply skipped checks passed.

## Review Standards

Every implementation review should check:

- Does the change satisfy the stated goal and acceptance criteria?
- Do docs, spec, tasks, and implementation agree?
- Is the change scoped to the active slice?
- Are trace, confidence, and review states preserved?
- Are security and data rules respected?
- Are adapter boundaries preserved?
- Is verification evidence sufficient for the phase?
- Are residual risks visible?

## Lessons Learned Standards

Acceptance review findings should become durable project memory.

When the delivered result does not match the user's expectation, do not only fix the immediate issue. Capture the lesson and update the artifact that would have prevented the mismatch.

Use `docs/00-context/lessons-learned.md` for lessons that should survive beyond the current conversation.

Required learning loop:

1. Record the expected behavior and observed behavior.
2. Identify the root cause: goal ambiguity, missing requirement, weak spec, incomplete design, implementation drift, missing verification, or review blind spot.
3. Update the durable artifact: requirements, stories, spec, architecture, design, tasks, standards, project rules, or agent instructions.
4. Add or update verification so the same issue is checked in the next similar goal.
5. Link the lesson to the affected slice, requirement, task, or quality gate when possible.

A lesson is only complete when it has a prevention mechanism. A note without a rule, spec update, checklist update, or test is not enough.

## Git And PR Standards

- Use conventional commit style when committing: `<type>: <description>`.
- Keep commits scoped to coherent work.
- PRs should include goal, slice, SDD links, change summary, verification, security notes, and residual risks.
- Review diffs before pushing.
- Do not mix unrelated cleanup with feature work unless the cleanup is required by the goal.

## CI Direction

No CI is required for Phase 0 static prototype work.

When implementation phases begin, CI should grow in this order:

1. Formatting and diff hygiene checks.
2. Frontend lint, typecheck, unit tests, and build.
3. Backend compile, unit tests, and integration tests.
4. E2E smoke tests for critical flows.
5. Secret scanning and dependency/security checks.
6. Supply-chain health checks such as OpenSSF Scorecard when the repository is ready.

## External References

Atlas may borrow ideas from mature public practices, adapted to this repository's lightweight profile:

- GitHub Spec Kit for spec-first development workflow patterns.
- Google Engineering Practices for code review discipline.
- OpenSSF Scorecard and SCM Best Practices for repository and supply-chain hygiene.
- OWASP secure coding guidance for backend, API, authentication, and secret handling.

Do not blindly copy external standards when they conflict with Atlas phase discipline, product scope, or mock-only prototype constraints.
