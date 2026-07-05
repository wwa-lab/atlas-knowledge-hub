# Single-Slice Goal Prompt Template

Use this prompt when Codex should complete one Atlas slice from SDD through implementation and verification.

Copy the prompt block and replace placeholders before sending it to Codex goal mode.

```text
请设置并执行一个 Atlas single-slice goal。

Goal mode: single-slice
Slice: <slice slug>
Goal: <user-facing and engineering outcome>
Scope:
- <included behavior>
Exclusions:
- <explicitly excluded behavior>

Required workflow:
- Read `AGENTS.md`.
- Read `PROJECT_RULES.md`.
- Read `DEVELOPMENT_STANDARDS.md`.
- Read `docs/00-context/agent-goal-loop-workflow.md` and `docs/00-context/agent-goal-loop-workflow.zh-CN.md`.
- Read `docs/00-context/agent-goal-loop-quickstart.md` and `docs/00-context/agent-goal-loop-quickstart.zh-CN.md`.
- Read `docs/SDD-BOOTSTRAP.md` and `docs/SDD-BOOTSTRAP.zh-CN.md`.
- Read `docs/00-context/sdd-profile.md`.
- Read execution manifest: `<manifest path>`.
- Read existing SDD docs for `<slice slug>` if present.
- Run `git status --short` before edits and protect unrelated user or other-agent changes.

Mandatory SDD skill usage:
- Before generating or materially updating a full SDD set, read `.agents/skills/atlas-sdd-generate-all/SKILL.md`.
- Use this project-local skill chain in order:
  1. `atlas-sdd-generate-all`
  2. `req-to-user-story`
  3. `user-story-to-spec`
  4. `spec-to-architecture`
  5. `architecture-to-design`
  6. `design-to-tasks`
  7. `review-doc-quality`
- Read each corresponding `.agents/skills/*/SKILL.md` file before using it.
- Use `architecture-review` when architecture, adapter boundaries, backend/API contract, persistence, security, or data flow materially changes.
- If required skill files are unavailable or the chain cannot be confirmed, stop and report. Do not hand-write the full SDD set ad hoc.

Execution:
- Choose and report the workflow tier before editing. Use Tier 1 by default for normal feature slices and Tier 3 for security, real-data, external-provider, auth/RBAC, destructive migration, or production-readiness work.
- First check whether required bilingual SDD artifacts exist and are current.
- If SDD is missing or stale, generate or update it through the skill chain.
- Run `docs/00-context/checklists/sdd-generation-gate.md`.
- If user acceptance is required, stop and request acceptance before product code edits.
- After SDD is accepted or safely inferable under project rules, implement only tasks from `docs/06-tasks/<slice>-tasks.md`.
- Run verification commands listed in the manifest and tasks.
- If verification fails, use a scoped fix loop within retry budget.
- Update traceability and status docs before close-out.

Acceptance:
- `SDD skill chain used: yes`.
- Bilingual SDD artifacts are complete and synchronized.
- Implementation maps to accepted spec and tasks.
- Adapter boundaries, source trace, confidence, review status, and data safety do not regress.
- Verification is run or skipped with explicit reasons.

Completion report:
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
```
