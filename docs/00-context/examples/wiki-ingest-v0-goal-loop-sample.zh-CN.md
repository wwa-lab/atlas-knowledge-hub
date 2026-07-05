# Sample Goal Loop：wiki-ingest-v0

这个 sample 展示一个完整 Atlas single-slice goal package 应该长什么样。它是示例，不是已接受的产品决策。

## Goal Contract

```text
Goal mode: single-slice
Workflow tier: Tier 1 Standard Single Slice
Slice: wiki-ingest-v0
Goal: 从 approved source chunks 生成 review-required Wiki page candidates，同时保护 manual Wiki pages 和 Atlas adapter boundaries。
Scope:
- 从 approved chunks 到 Wiki page candidates 的 deterministic ingest run。
- 按 slug 幂等 merge。
- 保留 `source_refs`、`chunk_refs`、`review_status` 和 `source_mode`。
- 生成页面默认 `REVIEW_REQUIRED`。
Exclusions:
- 不做 production auth/RBAC。
- 不做 connector sync。
- 默认不调用外部 model/provider。
- 没有未来 review-gate slice 前，不做 trusted publish。
Manifest: docs/00-context/execution-manifests/wiki-ingest-v0-YYYYMMDD.yaml
```

## Expected Command Flow

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode single-slice --wave 1 --purpose "Generate review-required Wiki page candidates from approved chunks"
```

把 `docs/00-context/goal-prompts/single-slice-goal-prompt.md` 复制到 Codex goal mode，并设置：

```text
Slice: wiki-ingest-v0
<manifest path>: docs/00-context/execution-manifests/wiki-ingest-v0-YYYYMMDD.yaml
```

SDD 生成后：

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide
```

Close-out 前：

```bash
git diff --check
cd backend && mvn verify
cd frontend && npm run typecheck && npm run test && npm run build
```

当 UI flow 或 full-stack product surface 改变时，运行 E2E 检查。

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

