# Atlas Knowledge Hub Copilot Instructions

These instructions apply to GitHub Copilot Chat, Copilot agent mode, Copilot code review, and other Copilot surfaces that read repository custom instructions.

Before editing files, read:

- `AGENTS.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/agent-execution-modes.md`
- `docs/00-context/agent-goal-loop-quickstart.md`
- the relevant SDD files under `docs/01-*` through `docs/06-*`

Atlas is SDD-first. For feature work, do not implement behavior that is not represented in the current slice spec under `docs/03-spec/` and task list under `docs/06-tasks/`.

In GitHub Copilot Chat Mode:

- Treat the human developer as the loop operator.
- Do not assume Codex goal mode, Codex memory, or project-local skill execution is available.
- If required commands cannot be run directly, ask the developer to run them and paste the output.
- If Atlas SDD skill-chain evidence is required but unavailable, say so instead of claiming the SDD workflow was completed.
- Keep changes scoped to the active slice or explicitly stated docs-only task.
- Do not introduce real company data, secrets, private paths, external cloud calls, or unapproved dependencies.
- Preserve adapter boundaries for parser, converter, model, vector database, storage, and search integrations.

Before marking work complete, ensure the appropriate task verification commands have run and run:

```bash
npm run agent:closeout
```

If `npm run agent:closeout` or the GitHub Actions `Agent Workflow Gate` fails, the work is not complete.
