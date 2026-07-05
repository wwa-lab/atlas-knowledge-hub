# Atlas Agent Goal Loop Workflow

日期：2026-07-05

本文定义 Atlas Knowledge Hub 中人、Codex、Claude Code、Copilot 或其他 coding agent 使用 goal-driven execution 时必须遵守的标准工作流。

目标不是依赖某个更强模型，而是让不同模型、不同人、不同线程都能沿同一条可验证流水线产出稳定结果。

## 设计依据

Atlas 借鉴这些业内模式：

- `AGENTS.md` 作为 coding agent 的仓库入口。
- GitHub Spec Kit 的 constitution、specification、plan、tasks、implementation、checklist 结构。
- 项目本地 agent skills 作为可执行、可验证的 workflow 模块。
- Loop engineering 的受控循环：plan、act、observe、verify、adjust、stop。

Atlas 不引入与现有 SDD 竞争的新 source of truth。本文位于 `PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/SDD-BOOTSTRAP.md` 和 `docs/00-context/sdd-profile.md` 之下。

后续应定期回看这些参考模式：

- `AGENTS.md` format: `https://github.com/agentsmd/agents.md`
- GitHub Spec Kit: `https://github.com/github/spec-kit`
- Spec Kit docs: `https://github.github.com/spec-kit/`
- Agent Skills pattern: `https://github.com/addyosmani/agent-skills`
- Loop engineering discussion: `https://addyosmani.com/blog/loop-engineering/`

## 核心原则

Loop 执行 workflow，不能替代 workflow。

模型可以迭代、修复、继续推进，但每一轮都必须受 Atlas gate 约束：

- 先 SDD，后实现。
- 完整 SDD 生成前必须使用项目本地 SDD skill chain。
- 当 SDD 改变范围、架构、安全、API contract、真实数据处理或外部 provider 行为时，实现前必须获得用户接受。
- close-out 前必须验证。
- 可复用的流程失败必须进入 lessons learned。

## 工作流表面

| 表面 | 作用 |
|---|---|
| `AGENTS.md` | Agent 入口和仓库本地操作说明。 |
| `PROJECT_RULES.md` | Atlas constitution 和硬约束。 |
| `DEVELOPMENT_STANDARDS.md` | 工程标准和验证要求。 |
| `docs/SDD-BOOTSTRAP.md` / `.zh-CN.md` | SDD 生成入口、文档链和 skill-chain 规则。 |
| `.agents/skills/*/SKILL.md` | 项目本地可执行 workflow skills。 |
| `docs/00-context/execution-manifests/*.yaml` | 固定输入、范围、产物、gate 和验证的任务合同。 |
| `docs/00-context/goal-prompts/*.md` | 可复制给 Codex 的 master/single-slice goal prompts。 |
| `docs/00-context/checklists/*.md` | 用于检查 SDD、验证和 close-out 证据的 gate。 |
| `docs/00-context/agent-goal-loop-quickstart.md` / `.zh-CN.md` | 用于选择正确 workflow tier 的一页操作指南。 |
| `docs/00-context/{slice}-traceability.md` | Slice 状态、source mapping、证据和延期项。 |
| `docs/00-context/lessons-learned.md` | 防止重复流程或验收失败的持久机制。 |

## Workflow Intensity Tiers

使用能安全覆盖任务的最轻 workflow tier。范围、风险或 traceability 要求增加时，必须升级。

| Tier | 使用场景 | 必需流程 | 不得跳过 |
|---|---|---|---|
| Tier 0：Light Maintenance | 错别字、格式、链接、小型 metadata、不会改变 durable product behavior 的 docs-only clarification。 | 读取必需 repo docs，做 scoped edit，运行轻量 diff/format/secret checks，报告 changed files。Manifest 和完整 SDD 可选。 | Security/data rules、无关改动保护、verification report。 |
| Tier 1：Standard Single Slice | 一个 feature slice、带 durable behavior 的 bug fix、API/UI contract 变更、或准备实现的 SDD 更新。 | 使用 single-slice goal，创建或复用 manifest，代码前通过 SDD gate，实现已接受 tasks，验证并 close out。 | SDD skill-chain gate、traceability、verification、closeout evidence。 |
| Tier 2：Master Slice Queue | Roadmap、wave、多 slice 队列、长期 Codex goal。 | 使用 master goal prompt 和 manifest slice queue。一次只按 Tier 1 执行一个 slice。每个 slice 后更新进度。 | 一次一个 slice、stop/resume protocol、状态更新。 |
| Tier 3：High-Risk / Governance | Auth、RBAC、secrets、audit、外部 provider、真实数据处理、破坏性 migration、production readiness、安全敏感行为。 | 使用 single-slice 或 master goal，并加入明确用户接受、architecture review、更严格验证和保守 stop conditions。 | User acceptance、security/data gate、adapter gate、rollback/resume evidence。 |

Tier 0 不是绕过安全的捷径。它只用于不会创建或改变 durable behavior contract 的小改动，避免不必要的完整 SDD 仪式。

## Goal Modes

Atlas 支持两种 Codex goal mode。

### Master Goal

当用户希望长期推进 roadmap、wave 或 slice queue 时，使用 master goal mode。

Master goal 是 workflow runner。它不能把多个无关 slice 的代码混在同一次改动中。

职责：

- 读取 execution manifest 和 slice queue。
- 选择下一个 eligible slice。
- 对该 slice 启动或继续 single-slice loop。
- 遇到 human gate 时停下。
- 每个 slice 后更新 traceability、roadmap/progress 和 evidence。
- 推荐下一个 slice。

### Single-Slice Goal

当用户希望一次完成一个明确 slice 时，使用 single-slice goal mode。

职责：

- 读取必需仓库文档、manifest 和 skill files。
- 通过项目本地 skill chain 生成或更新双语 SDD。
- 通过 SDD generation gate。
- 需要用户接受时停下等待。
- 只实现已接受的 slice tasks。
- 运行验证，失败时进入受控 fix loop。
- 带证据和剩余风险 close out。

## Loop Layers

Atlas goal mode 使用四层 loop。

| Loop | 目的 | 停止条件 |
|---|---|---|
| Control Loop | 在 master mode 下选择和排序 slice。 | 队列完成、下一个 slice blocked，或用户停止。 |
| Work Loop | 把一个 slice 从 SDD 推进到实现和验证。 | Slice 通过 close-out gates，或触发 stop condition。 |
| Verification Loop | 运行检查、读失败、做 scoped fix、重跑验证。 | 检查通过、retry budget 用尽，或修复需要扩大范围。 |
| Learning Loop | 把可复用失败变成 durable prevention。 | lesson 和 prevention artifact 更新，或该问题不可复用。 |

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

Master mode 可以为当前 active slice 创建或更新 SDD，但不得一次性对多个 slice 做大范围代码修改。

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

## 必须通过的 Gates

| Gate | 通过条件 |
|---|---|
| Goal Gate | Goal、slice、scope、exclusions、acceptance、verification、constraints 明确，或可安全推断。 |
| Context Gate | 必需 repo docs、manifest、相关 SDD docs、required skill files 已读取，或缺失项已报告。 |
| SDD Skill Gate | Completion report 写明 `SDD skill chain used: yes`，列出 skill files，并记录 `review-doc-quality` 结果。 |
| SDD Quality Gate | 双语 SDD artifacts 存在，IDs 一致，requirements 可映射到 stories/spec/design/tasks，open questions 明确。 |
| User Acceptance Gate | SDD 改变产品范围、架构、API、持久化、安全、真实数据处理或外部 provider 行为时必须通过。 |
| Implementation Gate | 代码改动映射到已接受 spec/tasks，并限制在 active slice 内。 |
| Verification Gate | 必需检查已运行，或清楚说明跳过原因。 |
| Evidence Gate | 最终报告包含 docs changed、code changed、verification、skipped checks、residual risks、maturity level 和 next action。 |
| Learning Gate | 可复用的验收或流程失败已更新 `lessons-learned.md` 和 prevention artifact。 |

最终 close-out 证据使用 `docs/00-context/checklists/goal-closeout-gate.md` / `.zh-CN.md`。

## Stop Conditions

遇到以下情况必须停止并报告：

- 必需 repo docs、manifest 或项目本地 SDD skill files 缺失。
- Agent 准备绕过项目本地 skill chain 生成完整 SDD。
- 用户尚未接受会影响范围、架构、安全、API contract、持久化、真实数据处理或外部 provider 行为的 SDD 变更。
- 工作需要真实公司数据、raw secrets、私有路径、机密截图，或未批准的外部 provider 调用。
- 修复失败会扩大到 active slice 之外。
- 验证重复失败且根因不明确。
- 会覆盖用户或其他 agent 的既有改动。

## Resume Protocol

Goal loop 必须从持久状态恢复，而不是依赖聊天记忆。

恢复前：

1. 读取相关 execution manifest。
2. 读取当前 slice traceability 和 roadmap/progress docs。
3. 运行 `git status --short`。
4. 查看相关 diff，不回滚无关改动。
5. 找出最后完成的 gate 和下一个必需 gate。
6. 从第一个未完成 gate 继续。

如果状态文档过期，先更新状态，再宣称进展。

## Completion Report Schema

每个 master 或 single-slice goal 的报告必须包含：

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

默认 verification loop 重试预算：

- formatting、typecheck、unit、contract failures：最多 2 次 scoped fix。
- E2E failures：最多 1 次 scoped fix，除非根因明显且在 active slice 内。
- security、real-data、secret、external-provider、破坏性 migration、scope-expanding failures：不自动修复，直接停下报告。

重试预算耗尽后，停止并带证据报告 blocker。

## 与现有 SDD 的关系

本文不替代 slice SDD。它控制 slice SDD 如何创建、接受、实现、验证和恢复。

行为 source of truth 是 `docs/03-spec/{slice}-spec.md`。
实现任务清单是 `docs/06-tasks/{slice}-tasks.md`。
状态和证据记录在 `docs/00-context/{slice}-traceability.md`。
