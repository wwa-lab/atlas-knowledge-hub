# Sample Goal Loop: wiki-ingest-v0

This sample shows what a complete Atlas single-slice goal package should look like. It is an example, not an accepted product decision.

## Goal Contract

```text
Goal mode: single-slice
Workflow tier: Tier 1 Standard Single Slice
Slice: wiki-ingest-v0
Goal: Generate review-required Wiki page candidates from approved source chunks while preserving manual Wiki pages and Atlas adapter boundaries.
Scope:
- Deterministic ingest run from approved chunks to Wiki page candidates.
- Idempotent merge by slug.
- Preserve `source_refs`, `chunk_refs`, `review_status`, and `source_mode`.
- Generated pages default to `REVIEW_REQUIRED`.
Exclusions:
- No production auth/RBAC.
- No connector sync.
- No external model/provider call by default.
- No trusted publish without future review-gate slice.
Manifest: docs/00-context/execution-manifests/wiki-ingest-v0-YYYYMMDD.yaml
```

## Expected Command Flow

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode single-slice --wave 1 --purpose "Generate review-required Wiki page candidates from approved chunks"
```

Copy `docs/00-context/goal-prompts/single-slice-goal-prompt.md` into Codex goal mode and set:

```text
Slice: wiki-ingest-v0
<manifest path>: docs/00-context/execution-manifests/wiki-ingest-v0-YYYYMMDD.yaml
```

After SDD generation:

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide
```

Before close-out:

```bash
git diff --check
cd backend && mvn verify
cd frontend && npm run typecheck && npm run test && npm run build
```

Run E2E checks when the UI flow or full-stack product surface changes.

## Expected SDD Artifacts

- `docs/01-requirements/wiki-ingest-v0-requirements.md`
- `docs/01-requirements/wiki-ingest-v0-requirements.zh-CN.md`
- `docs/02-user-stories/wiki-ingest-v0-stories.md`
- `docs/02-user-stories/wiki-ingest-v0-stories.zh-CN.md`
- `docs/03-spec/wiki-ingest-v0-spec.md`
- `docs/03-spec/wiki-ingest-v0-spec.zh-CN.md`
- `docs/04-architecture/wiki-ingest-v0-architecture.md`
- `docs/04-architecture/wiki-ingest-v0-architecture.zh-CN.md`
- `docs/04-architecture/wiki-ingest-v0-data-flow.md`
- `docs/04-architecture/wiki-ingest-v0-data-flow.zh-CN.md`
- `docs/04-architecture/wiki-ingest-v0-data-model.md`
- `docs/04-architecture/wiki-ingest-v0-data-model.zh-CN.md`
- `docs/05-design/wiki-ingest-v0-design.md`
- `docs/05-design/wiki-ingest-v0-design.zh-CN.md`
- `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.zh-CN.md`
- `docs/06-tasks/wiki-ingest-v0-tasks.md`
- `docs/06-tasks/wiki-ingest-v0-tasks.zh-CN.md`
- `docs/00-context/wiki-ingest-v0-traceability.md`
- `docs/00-context/wiki-ingest-v0-traceability.zh-CN.md`

## Sample Completion Report Shape

```text
Goal mode: single-slice
Workflow tier: Tier 1 Standard Single Slice
Slice: wiki-ingest-v0
Status: SDD generated; waiting for user acceptance before implementation
Maturity: SDD-ready, not implemented
Manifest: docs/00-context/execution-manifests/wiki-ingest-v0-YYYYMMDD.yaml
SDD skill chain used: yes
Skill files read:
- .agents/skills/atlas-sdd-generate-all/SKILL.md
- .agents/skills/req-to-user-story/SKILL.md
- .agents/skills/user-story-to-spec/SKILL.md
- .agents/skills/spec-to-architecture/SKILL.md
- .agents/skills/architecture-to-design/SKILL.md
- .agents/skills/design-to-tasks/SKILL.md
- .agents/skills/review-doc-quality/SKILL.md
Docs changed: <list>
Code changed: None
Task IDs completed: SDD tasks only
Verification run:
- npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide
- git diff --check
Skipped checks:
- Backend/frontend implementation checks skipped because code has not started.
Evidence:
- SDD gate passed.
Residual risks:
- User acceptance is still required before code.
Lessons recorded: None
Next action: User accepts or requests SDD revision.
Resume point: Start implementation from docs/06-tasks/wiki-ingest-v0-tasks.md after acceptance.
```

