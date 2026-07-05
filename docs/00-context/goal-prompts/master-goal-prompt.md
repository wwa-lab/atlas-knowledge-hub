# Master Goal Prompt Template

Use this prompt when Codex should operate as a long-running Atlas roadmap or slice-queue runner.

Copy the prompt block and replace placeholders before sending it to Codex goal mode.

```text
请设置并执行一个 Atlas master goal。

Goal mode: master
Goal: 按 Atlas Agent Goal Loop Workflow 推进 <roadmap / wave / slice queue>，一次只执行一个 eligible slice，并在每个 gate 处留下可验证证据。

Required workflow:
- Read `AGENTS.md`.
- Read `PROJECT_RULES.md`.
- Read `DEVELOPMENT_STANDARDS.md`.
- Read `docs/00-context/agent-goal-loop-workflow.md` and `docs/00-context/agent-goal-loop-workflow.zh-CN.md`.
- Read `docs/00-context/agent-goal-loop-quickstart.md` and `docs/00-context/agent-goal-loop-quickstart.zh-CN.md`.
- Read `docs/SDD-BOOTSTRAP.md` and `docs/SDD-BOOTSTRAP.zh-CN.md`.
- Read `docs/00-context/sdd-profile.md`.
- Read execution manifest: `<manifest path>`.
- Read current roadmap/progress/traceability docs named in the manifest.
- Run `git status --short` before edits and protect unrelated user or other-agent changes.

Master behavior:
- Confirm the workflow tier for the active slice before executing it. Master goals normally run Tier 2, while each active slice runs Tier 1 or Tier 3.
- Treat the manifest as the execution contract.
- Select the next eligible slice from `task.slice_queue`.
- For the selected slice, run the single-slice loop.
- Do not mix implementation changes from multiple slices.
- Do not skip SDD gates.
- Do not bypass `.agents/skills/atlas-sdd-generate-all/SKILL.md` for full SDD generation.
- Stop for user acceptance when SDD changes product scope, architecture, API contract, persistence, security, real-data handling, or external-provider behavior.
- After each slice, update traceability and roadmap/progress docs, then recommend the next eligible slice.

Loop gates:
- Goal gate.
- Context gate.
- SDD skill-chain gate.
- SDD quality gate.
- User acceptance gate when required.
- Implementation gate.
- Verification gate.
- Evidence gate.
- Learning gate.

Stop conditions:
- Required document or skill file is missing.
- Required project-local SDD skill chain is unavailable or not used.
- User acceptance is required but not yet given.
- Work requires real company data, raw secrets, private paths, confidential screenshots, or unapproved external provider calls.
- Verification failures cannot be fixed inside the active slice.
- Existing user or other-agent changes would be overwritten.

Completion report after each slice:
- Goal mode:
- Workflow tier:
- Slice:
- Status:
- Maturity:
- SDD skill chain used:
- Skill files read:
- Docs changed:
- Code changed:
- Task IDs completed:
- Verification run:
- Skipped checks:
- Evidence:
- Residual risks:
- Lessons recorded:
- Next action:
- Resume point:

Initial slice preference:
- <optional starting slice; otherwise choose next eligible slice from manifest>
```
