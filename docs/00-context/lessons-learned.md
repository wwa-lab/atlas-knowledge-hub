# Lessons Learned

This file captures reusable lessons from acceptance review, implementation review, verification failures, and user feedback.

The purpose is not to keep a diary. Each lesson should prevent a repeated mistake by updating the right rule, SDD artifact, checklist, test, or implementation pattern.

## Learning Loop

When an outcome differs from expectation:

1. Capture the expectation gap.
2. Identify whether the root cause was goal ambiguity, missing requirement, weak spec, incomplete design, implementation drift, missing verification, or review blind spot.
3. Update the durable artifact that would have prevented the issue.
4. Add or update verification so the issue is checked next time.
5. Link the lesson to the affected slice, requirement, task, or quality gate.

## Where To Apply A Lesson

| Lesson Type | Durable Home |
|---|---|
| Product behavior was unclear | `docs/01-requirements/` and `docs/03-spec/` |
| User story or acceptance was missing | `docs/02-user-stories/` |
| Architecture or adapter boundary was wrong | `docs/04-architecture/` or an ADR when needed |
| UI behavior or component expectation was unclear | `docs/05-design/` |
| Work was missed during execution | `docs/06-tasks/` |
| Verification missed a defect | Task verification, `DEVELOPMENT_STANDARDS.md`, or future test coverage |
| Agent repeated a workflow mistake | `AGENTS.md` or `PROJECT_RULES.md` |
| Standard applies across slices | `DEVELOPMENT_STANDARDS.md` |

## Entry Template

```text
ID: LL-YYYYMMDD-###
Date:
Slice:
Source:
Expectation:
Observed:
Root cause:
Decision:
Durable updates:
New verification:
Status:
```

## Lessons

### LL-20260702-001 Settings Should Be Shell-Level, Not A Main Content Route

ID: LL-20260702-001
Date: 2026-07-02
Slice: knowledge-space
Source: Prototype acceptance feedback.
Expectation: Settings should follow the preferred WeKnora-like product pattern: common settings are discoverable from the persistent sidebar, and All Settings opens as a modal/sheet over the current product view.
Observed: The prototype treated Settings as a normal main view, replacing the Home or Knowledge Space content.
Root cause: The earlier SDD docs specified settings content but did not define settings as a shell-level overlay pattern.
Decision: Settings is now specified as a sidebar-driven shell surface. Common shortcuts open specific settings panels, and All Settings opens a modal/sheet without replacing the current view.
Durable updates: Updated `REQ-KS-022`, `Sidebar And Settings Shell`, `Settings Shell` design guidance, traceability, and task `T-KS-026`.
New verification: Verify settings shortcuts open the correct modal panel, All Settings opens over the current view, and close returns to the previous view without resetting state.
Status: Applied.
