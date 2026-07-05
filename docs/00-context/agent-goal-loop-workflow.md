# Atlas Agent Goal Loop Workflow

Date: 2026-07-05

This document defines the standard workflow for humans, Codex, Claude Code, Copilot, or any other coding agent working on Atlas Knowledge Hub through goal-driven execution.

The intent is stable output across models and operators. Prompt quality still matters, but the durable control surface is the workflow: rules, manifests, skills, gates, verification, and lessons learned.

## Design Basis

Atlas adapts these industry patterns:

- `AGENTS.md` as the predictable repository entrypoint for coding agents.
- GitHub Spec Kit's pattern of constitution, specification, plan, tasks, implementation, and checklists.
- Project-local agent skills as actionable, verifiable workflow modules.
- Loop engineering as a controlled goal loop: plan, act, observe, verify, adjust, and stop.

Atlas does not adopt a new source of truth that competes with existing SDD. This workflow sits on top of `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/SDD-BOOTSTRAP.md`, and `docs/00-context/sdd-profile.md`.

Reference patterns to review periodically:

- `AGENTS.md` format: `https://github.com/agentsmd/agents.md`
- GitHub Spec Kit: `https://github.com/github/spec-kit`
- Spec Kit docs: `https://github.github.com/spec-kit/`
- Agent Skills pattern: `https://github.com/addyosmani/agent-skills`
- Loop engineering discussion: `https://addyosmani.com/blog/loop-engineering/`

## Core Principle

Loops execute the workflow. Loops do not replace the workflow.

A model may iterate, repair, and continue, but every iteration must remain inside Atlas gates:

- SDD before implementation.
- Project-local SDD skill chain before full SDD generation.
- User acceptance before code when the SDD changes scope, architecture, security, API contract, real-data handling, or external-provider behavior.
- Verification before close-out.
- Lessons learned for reusable process failures.

## Workflow Surfaces

| Surface | Role |
|---|---|
| `AGENTS.md` | Agent entrypoint and repo-local operating instructions. |
| `PROJECT_RULES.md` | Atlas constitution and hard constraints. |
| `DEVELOPMENT_STANDARDS.md` | Engineering standards and verification expectations. |
| `docs/SDD-BOOTSTRAP.md` / `.zh-CN.md` | SDD generation entrypoint, document chain, and skill-chain rule. |
| `.agents/skills/*/SKILL.md` | Project-local executable workflow skills. |
| `docs/00-context/execution-manifests/*.yaml` | Pinned task contracts for agent execution. |
| `docs/00-context/goal-prompts/*.md` | Copyable Codex goal prompts for master and single-slice goals. |
| `docs/00-context/checklists/*.md` | Gates that test requirements, SDD, verification, and close-out evidence. |
| `docs/00-context/agent-goal-loop-quickstart.md` / `.zh-CN.md` | One-page operating guide for choosing the right workflow tier. |
| `docs/00-context/{slice}-traceability.md` | Slice status, source mapping, evidence, and deferred work. |
| `docs/00-context/lessons-learned.md` | Durable prevention for repeated workflow or acceptance failures. |

## Workflow Intensity Tiers

Use the lightest workflow tier that safely fits the task. Escalate when scope, risk, or traceability requirements increase.

| Tier | Use When | Required Workflow | Must Not Skip |
|---|---|---|---|
| Tier 0: Light Maintenance | Typos, formatting, link fixes, small metadata edits, or docs-only clarifications that do not change durable product behavior. | Read required repo docs, make scoped edit, run lightweight diff/format/secret checks, report changed files. Manifest and full SDD are optional. | Security/data rules, no unrelated edits, verification report. |
| Tier 1: Standard Single Slice | One feature slice, bug fix with durable behavior, API/UI contract change, or SDD update intended for implementation. | Use single-slice goal, create or reuse manifest, run SDD gate before code, implement accepted tasks, verify, close out. | SDD skill-chain gate, traceability, verification, closeout evidence. |
| Tier 2: Master Slice Queue | Roadmap, wave, multi-slice sequence, or long-running Codex goal. | Use master goal prompt and manifest slice queue. Execute one slice at a time through Tier 1. Update progress after each slice. | One-slice-at-a-time discipline, stop/resume protocol, status updates. |
| Tier 3: High-Risk / Governance | Auth, RBAC, secrets, audit, external provider calls, real-data handling, destructive migrations, production readiness, or security-sensitive behavior. | Use single-slice or master goal plus explicit user acceptance, architecture review, stricter verification, and conservative stop conditions. | User acceptance, security/data gate, adapter gate, rollback/resume evidence. |

Tier 0 is not a shortcut around safety. It is only a way to avoid full SDD ceremony for edits that do not create or change a durable behavior contract.

## Goal Modes

Atlas supports two Codex goal modes.

### Master Goal

Use master goal mode when the user wants a long-running roadmap, wave, or slice queue to progress over multiple checkpoints.

The master goal is a workflow runner. It must not mix code changes from unrelated slices.

Responsibilities:

- Read the execution manifest and slice queue.
- Select the next eligible slice.
- Start or continue the single-slice loop for that slice.
- Stop at human gates when required.
- Update traceability, roadmap/progress docs, and completion evidence after each slice.
- Recommend the next slice.

### Single-Slice Goal

Use single-slice goal mode when the user wants one explicit slice delivered from SDD to verification.

Responsibilities:

- Read required repository docs, manifest, and skill files.
- Generate or update the bilingual SDD set through the project-local skill chain.
- Pass the SDD generation gate.
- Wait for user acceptance when required.
- Implement only the accepted slice tasks.
- Run verification and fix loop when checks fail.
- Close out with evidence and residual risk.

## Loop Layers

Atlas goal mode uses four nested loops.

| Loop | Purpose | Stops When |
|---|---|---|
| Control Loop | Choose and sequence slices in master mode. | Queue is done, next slice is blocked, or user stops. |
| Work Loop | Execute one slice through SDD, implementation, and verification. | Slice passes close-out gates or hits a stop condition. |
| Verification Loop | Run checks, inspect failures, fix scoped defects, and rerun. | Checks pass, retry budget is exhausted, or fix would expand scope. |
| Learning Loop | Convert reusable failures into durable prevention. | Lesson and prevention artifact are updated or issue is not reusable. |

## Master Goal Loop

```text
while goal is active:
  read manifest, progress docs, roadmap, and git status
  choose next eligible slice from slice_queue
  if no eligible slice:
    report done or blocked
    stop

  run single-slice loop for selected slice

  if single-slice loop is blocked:
    report blocker, evidence, and resume point
    stop

  update traceability, roadmap/progress, and manifest evidence
  recommend next eligible slice
```

Master mode may create or update SDD docs for the active slice, but it must not create broad code changes across multiple slices in one pass.

## Single-Slice Loop

```text
read required repository docs
read execution manifest
read project-local SDD skills
check git status and protect unrelated changes

if required SDD artifacts are missing or stale:
  generate or update SDD through skill chain
  run SDD generation gate
  if user acceptance is required:
    stop and request acceptance

read accepted spec and tasks
implement tasks in order
run verification commands

while verification fails and retry budget remains:
  inspect failure
  fix only scoped defects
  rerun relevant checks

if checks pass:
  update traceability and status docs
  run close-out gate
  report evidence
else:
  report blocker and resume point
```

## Required Gates

| Gate | Pass Condition |
|---|---|
| Goal Gate | Goal, slice, scope, exclusions, acceptance, verification, and constraints are explicit or safely inferred. |
| Context Gate | Required repo docs, manifest, relevant SDD docs, and required skill files were read or missing files were reported. |
| SDD Skill Gate | Completion report says `SDD skill chain used: yes`, lists skill files read, and records `review-doc-quality` result. |
| SDD Quality Gate | Bilingual SDD artifacts exist, IDs match, requirements map to stories/spec/design/tasks, and open questions are explicit. |
| User Acceptance Gate | Required when SDD changes product scope, architecture, API, persistence, security, real-data handling, or external-provider behavior. |
| Implementation Gate | Code changes map to accepted spec/tasks and stay inside the active slice. |
| Verification Gate | Required checks run or are explicitly skipped with reasons. |
| Evidence Gate | Final report includes docs changed, code changed, verification, skipped checks, residual risks, maturity level, and next action. |
| Learning Gate | Reusable acceptance or workflow failures update `lessons-learned.md` and a prevention artifact. |

Use `docs/00-context/checklists/goal-closeout-gate.md` / `.zh-CN.md` for final close-out evidence.

## Stop Conditions

Stop and report instead of continuing when:

- Required repo docs, manifest, or project-local SDD skill files are missing.
- The agent is about to generate full SDD without the project-local skill chain.
- The user has not accepted an SDD change that affects scope, architecture, security, API contract, persistence, real-data handling, or external-provider behavior.
- Work would require real company data, raw secrets, private paths, confidential screenshots, or external provider calls outside approved opt-in verification.
- Fixing a failure would require expanding beyond the active slice.
- Verification fails repeatedly and the root cause is unclear.
- Existing user or other-agent changes would be overwritten.

## Resume Protocol

Goal loops must resume from durable state, not chat memory.

Before resuming:

1. Read the relevant execution manifest.
2. Read the current slice traceability and roadmap/progress docs.
3. Run `git status --short`.
4. Inspect relevant diffs without reverting unrelated work.
5. Identify the last completed gate and next required gate.
6. Continue from the first incomplete gate.

If state documents are stale, update them before claiming progress.

## Completion Report Schema

Every master or single-slice goal report must include:

```text
Goal mode:
Slice:
Status:
Maturity:
SDD skill chain used:
Skill files read:
Docs changed:
Code changed:
Task IDs completed:
Verification run:
Skipped checks:
Evidence:
Residual risks:
Lessons recorded:
Next action:
Resume point:
```

## Retry Budget

Default retry budget for a verification loop:

- 2 scoped fix attempts for formatting, typecheck, unit, or contract failures.
- 1 scoped fix attempt for E2E failures unless the cause is obvious and inside the active slice.
- 0 automatic attempts for security, real-data, secret, external-provider, migration-destructive, or scope-expanding failures.

After retry budget is exhausted, stop and report the blocker with evidence.

## Relationship To Existing SDD

This workflow does not replace slice SDD. It controls how slice SDD is created, accepted, implemented, verified, and resumed.

For behavior, use `docs/03-spec/{slice}-spec.md`.
For implementation tasks, use `docs/06-tasks/{slice}-tasks.md`.
For status and evidence, use `docs/00-context/{slice}-traceability.md`.
