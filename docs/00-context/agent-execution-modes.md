# Agent Execution Modes

Atlas supports two execution surfaces for the same SDD workflow:

- Codex Agent Goal Mode for personal or external environments where Codex can run a goal loop.
- GitHub Copilot Chat Mode for company environments where Copilot Chat is the default tool.

The workflow contract is the same in both modes: SDD first, one slice at a time, implementation aligned to `docs/03-spec/` and `docs/06-tasks/`, local closeout gate before completion, and GitHub Actions red light on PR/push.

## Mode A: Codex Agent Goal Mode

Use this mode when Codex goal mode and the project-local skills are available.

Codex is allowed to operate as the loop runner:

1. Read `AGENTS.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, and the relevant slice docs.
2. Choose a workflow tier from `docs/00-context/agent-goal-loop-quickstart.md`.
3. Create or reuse an execution manifest under `docs/00-context/execution-manifests/`.
4. Use `docs/00-context/goal-prompts/single-slice-goal-prompt.md` or `docs/00-context/goal-prompts/master-goal-prompt.md`.
5. Use project-local SDD skills when generating or updating SDD artifacts.
6. Implement only the accepted slice scope.
7. Run task verification and `npm run agent:closeout`.
8. Report docs changed, code changed, verification evidence, residual risks, and blocked checks.

Codex goal mode is the stronger autonomous mode because it can keep the execution contract, command output, and repair loop in one agent session.

## Mode B: GitHub Copilot Chat Mode

Use this mode for the company default path when developers primarily work through GitHub Copilot Chat in an IDE.

Copilot Chat is an implementation assistant, not the source of enforcement. In this mode, the human developer is the loop operator and the repository gates are the controller.

Copilot Chat should:

1. Consume `.github/copilot-instructions.md` as the repository-wide entry instruction.
2. Read `AGENTS.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, and relevant SDD docs before changing files.
3. Work from an explicit slice, scope, source documents, acceptance criteria, and verification list.
4. Keep changes small and aligned with the accepted SDD.
5. Ask the developer to run commands when it cannot run them directly.
6. Treat `npm run agent:closeout` and the `Agent Workflow Gate` GitHub Actions workflow as the final authority.

Copilot Chat must not assume it has Codex goal state, Codex skill loading, or project-local `.agents/skills` execution. If a task requires the Atlas SDD skill chain and Copilot cannot actually use that chain, it must say so and either ask for the SDD artifacts to be produced through Codex/Claude or create only a draft that is explicitly marked as missing skill-chain evidence.

## Capability Matrix

| Capability | Codex Agent Goal Mode | GitHub Copilot Chat Mode |
|---|---|---|
| Primary operator | Codex loop | Human developer plus Copilot |
| Native goal loop | Yes | No; emulate with an explicit prompt and checklist |
| Project-local skills | Can use when available in the Codex environment | Do not assume; must read files or ask for another tool |
| Repository instructions | `AGENTS.md` plus project docs | `.github/copilot-instructions.md`, `AGENTS.md`, and project docs |
| Terminal commands | Agent can run and repair within the session | Developer runs, or Copilot agent mode proposes commands for approval |
| Closeout gate | `npm run agent:closeout` | Same command, run by developer or approved agent action |
| PR/push gate | GitHub Actions `Agent Workflow Gate` | Same GitHub Actions gate |
| Best use | Full slice execution, SDD-to-code loops, long verification | Company-standard development, targeted implementation, review, local PR work |

## Copilot Chat Operating Prompt

Use this prompt when starting a Copilot Chat task:

```text
You are working in Atlas Knowledge Hub in GitHub Copilot Chat Mode.

Read and follow:
- .github/copilot-instructions.md
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/agent-execution-modes.md
- docs/00-context/agent-goal-loop-quickstart.md
- the relevant slice docs under docs/01-* through docs/06-*

Goal:
<describe the user-facing outcome>

Slice:
<slice slug or "none" for small docs-only work>

Scope:
<included work>

Out of scope:
<excluded work>

Acceptance:
<observable behavior, docs, or checks>

Before changing code, summarize the relevant SDD source of truth and task IDs.
After changes, tell me exactly which commands to run.
Do not mark the work complete unless npm run agent:closeout and the PR/push Agent Workflow Gate are green.
```

## Team Rule

The team should not depend on any single model remembering the workflow.

The durable enforcement stack is:

1. Repository instructions: `.github/copilot-instructions.md`, `AGENTS.md`, `PROJECT_RULES.md`.
2. SDD source of truth: `docs/03-spec/`, `docs/06-tasks/`, and traceability docs.
3. Local gates: `npm run agent:check-sdd` and `npm run agent:closeout`.
4. CI gate: GitHub Actions `Agent Workflow Gate`.
5. Branch protection: require `Agent Workflow Gate` before merge.

## References

- GitHub Copilot repository custom instructions: https://docs.github.com/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot
- GitHub Copilot custom instruction support matrix: https://docs.github.com/en/copilot/reference/custom-instructions-support
- GitHub Copilot agent mode overview: https://docs.github.com/en/copilot/get-started/features
