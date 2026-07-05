# Atlas Goal Loop Quickstart

Use this one-page guide to choose the right workflow tier and start a Codex goal without rereading every workflow document.

For company environments where GitHub Copilot Chat is the primary tool, read `docs/00-context/agent-execution-modes.md` first. Copilot Chat uses the same gates, but the human developer acts as the loop operator.

## 1. Choose A Tier

| Task | Tier |
|---|---|
| Typo, link, formatting, small docs clarification | Tier 0: Light Maintenance |
| One feature slice or durable behavior change | Tier 1: Standard Single Slice |
| Roadmap, wave, or multi-slice queue | Tier 2: Master Slice Queue |
| Auth, RBAC, secrets, audit, external provider, real data, destructive migration, production readiness | Tier 3: High-Risk / Governance |

When unsure, choose the higher tier.

## 2. Create Or Reuse A Manifest

For a new single slice:

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode single-slice --wave 1 --purpose "Generate review-required Wiki page candidates from approved chunks"
```

For a master queue:

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode master --queue wiki-ingest-v0,wiki-linkify-lint,real-office-parser-runtime --wave 1 --purpose "Advance Wiki Foundation slices one at a time"
```

## 3. Start Codex Goal Mode

Use one of:

- `docs/00-context/goal-prompts/single-slice-goal-prompt.md`
- `docs/00-context/goal-prompts/master-goal-prompt.md`

Replace `<manifest path>` with the generated manifest.

## 4. Check SDD Before Code

After SDD generation and before accepting implementation:

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide
```

If the agent produced a completion report file:

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide --report docs/00-context/examples/wiki-ingest-v0-goal-loop-sample.md
```

## 5. Run The Workflow Gate

Before close-out, run the same gate that CI calls:

```bash
npm run agent:check-workflow -- --changed-slices
```

For a specific slice:

```bash
npm run agent:check-workflow -- --slice wiki-ingest-v0 --require-api-guide
```

For docs-only or Tier 0 work without a slice:

```bash
npm run agent:check-workflow
```

The GitHub Actions workflow `Agent Workflow Gate` runs automatically on `pull_request` and `push`. It also remains available through `workflow_dispatch`.

## 6. Close Out

Before reporting complete:

- Run the task verification commands.
- Run `npm run agent:closeout`.
- Update traceability and roadmap/progress docs.
- Use `docs/00-context/checklists/goal-closeout-gate.md`.

## Minimal Decision Rule

If a result cannot show:

- which tier was used,
- which manifest was used,
- whether the SDD skill chain was used,
- what verification ran,
- what remains risky,

then the goal is not ready to close.

If `npm run agent:closeout` or `Agent Workflow Gate` is red, do not mark the goal complete.
