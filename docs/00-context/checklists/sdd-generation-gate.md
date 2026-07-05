# SDD Generation Gate Checklist

Use this checklist before accepting any newly generated or materially updated Atlas SDD set.

This checklist tests whether the SDD is ready for implementation. It does not test whether code works.

## Required Evidence

The agent completion report must include:

- `SDD skill chain used: yes`
- Entry skill file read: `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- Downstream skill files read:
  - `.agents/skills/req-to-user-story/SKILL.md`
  - `.agents/skills/user-story-to-spec/SKILL.md`
  - `.agents/skills/spec-to-architecture/SKILL.md`
  - `.agents/skills/architecture-to-design/SKILL.md`
  - `.agents/skills/design-to-tasks/SKILL.md`
  - `.agents/skills/review-doc-quality/SKILL.md`
- `architecture-review` result or explicit `not applicable` reason when architecture, API, persistence, security, adapter boundary, or data-flow changes are in scope.
- `review-doc-quality` result or explicit blocked reason.

## Gate Checklist

| Check | Pass Criteria | Status |
|---|---|---|
| Goal contract | Goal, slice, scope, exclusions, acceptance, verification, and constraints are explicit. | |
| Required context | `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `AGENTS.md`, SDD profile, bootstrap docs, manifest, and relevant SDD docs were read or missing items reported. | |
| Skill chain | Full SDD generation used the project-local skill chain and did not hand-write the entire set ad hoc. | |
| Bilingual artifacts | English and Simplified Chinese copies exist for every required SDD artifact touched. | |
| Stable IDs | Requirement IDs, user story IDs, task IDs, and acceptance IDs match across languages. | |
| Requirements coverage | Requirements cover current-phase behavior, future exclusions, data safety, adapter boundaries, and review/source-trace expectations. | |
| Story coverage | User stories map to requirements and include Given/When/Then acceptance criteria. | |
| Spec quality | Spec defines happy path, empty state, loading state, error state, edge cases, and acceptance matrix. | |
| Architecture quality | Architecture names module ownership, adapter boundaries, security/data constraints, and API/persistence scope. | |
| Data flow quality | Data flow defines state transitions, review status, source trace, failure/retry behavior, and deferred behavior. | |
| Data model quality | Data model lists entities, fields, validation, lifecycle states, indexes when relevant, and migration constraints. | |
| Design quality | Design names UI behavior, component state, accessibility when relevant, test selectors, and frontend/backend integration. | |
| API guide | API guide exists when backend/API work is in scope, or deferral is documented for frontend-only work. | |
| Task quality | Tasks are ordered, scoped, verifiable, mapped to requirements/spec sections, and include exact verification commands where known. | |
| Traceability | Traceability maps sources -> requirements -> stories -> spec/design -> tasks -> verification. | |
| Open questions | Ambiguities are explicit; risky ambiguity blocks implementation instead of being silently decided. | |
| User acceptance | User acceptance is requested before implementation when scope, architecture, security, API, persistence, real-data, or external-provider behavior changed. | |

## Fail-Fast Conditions

Reject or block the SDD handoff if:

- `SDD skill chain used` is missing or `no`.
- Required project-local skill files were unavailable and the agent generated SDD anyway.
- English and Chinese docs disagree on scope, IDs, or acceptance.
- Requirements cannot be traced to tasks.
- Backend/API work is in scope but API guide is missing without a documented deferral.
- Implementation is already started before the required SDD gate and acceptance gate.

## Acceptance Note Template

```text
SDD gate result:
Slice:
Skill chain evidence:
Documents checked:
Issues found:
User acceptance required before code: yes/no
Decision: accepted | needs revision | blocked
```

