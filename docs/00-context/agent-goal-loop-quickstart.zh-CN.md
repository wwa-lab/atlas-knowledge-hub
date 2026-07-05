# Atlas Goal Loop Quickstart

这是一页版操作指南，用来快速选择 workflow tier，并启动 Codex goal，而不需要每次重读所有 workflow 文档。

## 1. 选择 Tier

| 任务 | Tier |
|---|---|
| 错别字、链接、格式、小型 docs clarification | Tier 0：Light Maintenance |
| 一个 feature slice 或 durable behavior change | Tier 1：Standard Single Slice |
| Roadmap、wave、多 slice queue | Tier 2：Master Slice Queue |
| Auth、RBAC、secrets、audit、外部 provider、真实数据、破坏性 migration、production readiness | Tier 3：High-Risk / Governance |

不确定时，选择更高 tier。

## 2. 创建或复用 Manifest

新建 single slice：

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode single-slice --wave 1 --purpose "Generate review-required Wiki page candidates from approved chunks"
```

新建 master queue：

```bash
npm run agent:manifest -- --slice wiki-ingest-v0 --mode master --queue wiki-ingest-v0,wiki-linkify-lint,real-office-parser-runtime --wave 1 --purpose "Advance Wiki Foundation slices one at a time"
```

## 3. 启动 Codex Goal Mode

使用其中一个模板：

- `docs/00-context/goal-prompts/single-slice-goal-prompt.md`
- `docs/00-context/goal-prompts/master-goal-prompt.md`

把 `<manifest path>` 替换成生成的 manifest。

## 4. 写代码前检查 SDD

SDD 生成后、接受实现前运行：

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide
```

如果 agent 产出了 completion report 文件：

```bash
npm run agent:check-sdd -- --slice wiki-ingest-v0 --require-api-guide --report docs/00-context/examples/wiki-ingest-v0-goal-loop-sample.md
```

## 5. 运行 Workflow Gate

Close-out 前，运行 CI 自动使用的同一个 gate：

```bash
npm run agent:check-workflow -- --changed-slices
```

检查指定 slice：

```bash
npm run agent:check-workflow -- --slice wiki-ingest-v0 --require-api-guide
```

对于没有 slice 的 docs-only 或 Tier 0 工作：

```bash
npm run agent:check-workflow
```

GitHub Actions 的 `Agent Workflow Gate` 会在 `pull_request` / `push` 上自动运行，也保留 `workflow_dispatch` 手动入口。

## 6. Close Out

报告完成前：

- 运行 task verification commands。
- 运行 `npm run agent:closeout`。
- 更新 traceability 和 roadmap/progress docs。
- 使用 `docs/00-context/checklists/goal-closeout-gate.md`。

## 最小判断规则

如果结果无法说明：

- 使用了哪个 tier，
- 使用了哪个 manifest，
- 是否用了 SDD skill chain，
- 跑了什么验证，
- 还有什么风险，

那这个 goal 还不能 close。

如果 `npm run agent:closeout` 或 PR/push 上的 `Agent Workflow Gate` 是红灯，不得把 goal 标记为 complete。
