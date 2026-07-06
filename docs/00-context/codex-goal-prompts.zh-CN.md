# Codex Goal 提示词速查

最后更新：2026-07-07
用途：给 Codex Agent Goal Mode 使用的可复制提示词集合。

如果只需要一个入口，先看本文件。更完整的原始模板保留在：

- `docs/00-context/goal-prompts/single-slice-goal-prompt.md`
- `docs/00-context/goal-prompts/master-goal-prompt.md`

## 使用原则

- 一次只执行一个 active slice。
- Master goal 只管理队列，不同时实现多个 slice。
- SDD 未被人工接受前，不实现产品代码。
- 实现必须对齐 `docs/03-spec/{slice}-spec.md` 和 `docs/06-tasks/{slice}-tasks.md`。
- 完成前必须跑 task verification 和 `npm run agent:closeout`。
- PR/push 上的 `Agent Workflow Gate` 红灯时，不得标记 complete。
- 如果已有其他人或其他 agent 的未提交改动，必须保护，不得覆盖。
- 需要“一次复制就直接交付”的场景，使用 Prompt 7，并在 prompt 里给出明确的预授权边界。

## Prompt 0：只盘点当前 repo 状态

用于让 Codex 先读当前状态，不写代码。

```text
请以只读方式盘点 Atlas Knowledge Hub 当前 repo 状态，不要编辑文件。

必须读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/slice-roadmap.zh-CN.md
- 当前 active slice 的 traceability docs

请运行：
- git status --short
- git diff --stat

输出：
- 当前整体成熟度
- 当前 active slice
- 当前工作区是否干净
- 哪些文件属于未提交草稿
- 下一步门禁
- 不得宣称完成的原因
```

## Prompt 1：生成或更新 SDD，但不实现代码

用于 SDD 草案阶段。适合新 slice，或现有 SDD 需要补齐。

```text
请为 Atlas Knowledge Hub 执行一个 SDD-only goal。

Goal mode: single-slice / SDD-only
Slice: <slice slug>
Wave: <wave name>
Goal: <这个 slice 要解决的用户或工程问题>
Scope:
- <范围内>
Exclusions:
- <明确不做>

必须读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/SDD-BOOTSTRAP.md
- docs/SDD-BOOTSTRAP.zh-CN.md
- docs/00-context/sdd-profile.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/agent-goal-loop-quickstart.zh-CN.md
- 相关已有 SDD / traceability docs

必须使用项目本地 SDD skill chain：
- atlas-sdd-generate-all
- req-to-user-story
- user-story-to-spec
- spec-to-architecture
- architecture-to-design
- design-to-tasks
- review-doc-quality

要求：
- 生成或更新完整双语 SDD artifact set。
- 保持 English 和 .zh-CN.md 的 ID、scope、acceptance、task 一致。
- 如果涉及 backend/API，必须生成 API implementation guide。
- 更新 traceability。
- 生成 SDD completion report，列出 skill files read 和 review-doc-quality 结果。
- 本轮不要实现产品代码。

停止点：
- SDD 完成后停止，等待人工接受 spec/design/API guide/tasks。
```

## Prompt 2：执行一个完整 single-slice goal

用于 SDD 已存在或允许在同一 goal 中补 SDD，然后进入实现。

```text
请设置并执行一个 Atlas single-slice goal。

Goal mode: single-slice
Slice: <slice slug>
Goal: <用户可见或工程可验证的结果>
Scope:
- <范围内>
Exclusions:
- <明确不做>
Acceptance:
- <可观察完成标准>
Verification:
- <需要运行的命令>

必须读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/agent-goal-loop-workflow.md
- docs/00-context/agent-goal-loop-workflow.zh-CN.md
- docs/00-context/agent-goal-loop-quickstart.md
- docs/00-context/agent-goal-loop-quickstart.zh-CN.md
- docs/00-context/sdd-profile.md
- execution manifest: <manifest path>
- 当前 slice 的 requirements/stories/spec/architecture/design/tasks/traceability

执行规则：
- 先运行 git status --short，保护无关改动。
- 先报告 workflow tier。
- 检查 SDD 是否完整、双语同步、task 是否可执行。
- 如果 SDD 缺失或陈旧，先用项目本地 SDD skill chain 更新。
- 如果 SDD scope、API、data model、security、persistence 或 governance 发生变化，停止等待人工接受。
- 只有 SDD 被接受后，才按 docs/06-tasks/<slice>-tasks.md 实现。
- 只改当前 slice 需要的文件。
- 跑 task verification。
- 跑 npm run agent:closeout。
- 更新 traceability、roadmap/progress 状态。

完成报告必须包含：
- Goal mode
- Workflow tier
- Slice
- Status
- Maturity
- SDD skill chain used
- Docs changed
- Code changed
- Task IDs completed
- Verification run
- Skipped checks
- Evidence
- Residual risks
- Next action
- Resume point
```

## Prompt 3：Master goal 管理多个 slice 队列

用于 roadmap / wave / queue。注意：Master 只选择下一个 eligible slice，执行时仍然一次一个。

```text
请设置并执行一个 Atlas master goal。

Goal mode: master
Goal: 按 Atlas Agent Goal Loop Workflow 推进 <roadmap / wave / slice queue>，一次只执行一个 eligible slice，并在每个 gate 处留下可验证证据。

必须读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/slice-roadmap.zh-CN.md
- docs/00-context/agent-goal-loop-workflow.zh-CN.md
- docs/00-context/agent-goal-loop-quickstart.zh-CN.md
- execution manifest: <manifest path>
- 当前 roadmap/progress/traceability docs

Slice queue:
1. <slice-a>
2. <slice-b>
3. <slice-c>

Master 行为：
- 先判断当前 repo 状态和 active slice。
- 从 queue 里选择下一个 eligible slice。
- 对该 slice 执行 single-slice loop。
- 不混入其他 slice 的实现。
- 遇到 SDD acceptance gate 必须停下。
- 每完成一个 slice，更新 traceability 和 roadmap/progress，再推荐下一个 slice。

Stop conditions:
- required docs 或 skill files 缺失。
- SDD skill chain 不可用。
- 需要人工接受但尚未接受。
- 涉及真实公司数据、raw secrets、private paths、未批准外部 provider。
- verification 失败且无法在当前 slice 内修复。
- 会覆盖用户或其他 agent 的未提交改动。
```

## Prompt 4：SDD 已接受后，只执行实现

用于用户已经明确接受 spec/tasks 后。

```text
任务：实现 <slice slug> 切片。

接受状态：
- 用户已接受 docs/03-spec/<slice>-spec.md
- 用户已接受 docs/05-design/<slice>-design.md
- 用户已接受 docs/06-tasks/<slice>-tasks.md
- 若涉及 API/backend，也已接受 API implementation guide

契约：
- docs/03-spec/<slice>-spec.md 是行为唯一真相源。
- docs/06-tasks/<slice>-tasks.md 是实现 checklist。
- docs/05-design/<slice>-design.md 和 data-model/API guide 是设计契约。

规则：
- 先运行 git status --short，保护无关改动。
- 按 task ID 顺序实现。
- 不扩大 scope。
- 不实现后续 slice。
- 不绕过 adapter boundaries。
- 不引入真实公司数据、raw secrets、private paths 或未批准外部调用。
- 如果实现需要偏离 spec/task，停下并报告 mismatch，不要静默重设计。

完成：
- 跑每条 task 的 verification。
- 跑 npm run agent:closeout。
- 更新 traceability 和必要 roadmap/progress。
- 报告 changed files、task IDs、verification、residual risks。
```

## Prompt 5：Closeout / Review goal

用于实现后收尾，检查能不能标记完成。

```text
请执行 Atlas closeout review，不要扩大功能范围。

Slice: <slice slug>

读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/03-spec/<slice>-spec.md
- docs/05-design/<slice>-design.md
- docs/06-tasks/<slice>-tasks.md
- docs/00-context/<slice>-traceability.md
- docs/00-context/<slice>-traceability.zh-CN.md

检查：
- implementation 是否对齐 spec。
- completed task IDs 是否可追溯。
- docs/code/test 是否只属于当前 slice。
- security/data/adapter boundaries 是否保持。
- traceability 和 roadmap/progress 是否更新。
- lessons learned 是否需要沉淀。

运行：
- task verification commands
- npm run agent:closeout

输出：
- 可以标记 complete / 不能标记 complete
- 阻塞项
- 必须修复项
- 已跑验证
- skipped checks 和原因
- residual risks
```

## Prompt 6：从中断状态恢复

用于上下文丢失、session 切换、或多 agent 并行后恢复。

```text
请从 durable repo state 恢复 Atlas goal，不依赖聊天记忆。

只读阶段：
- 读取 docs/00-context/repo-status-roadmap.zh-CN.md
- 读取 docs/00-context/product-goal-progress.zh-CN.md
- 读取 active slice traceability docs
- 读取 execution manifest（如果存在）
- 运行 git status --short
- 运行 git diff --stat

请先输出恢复判断：
- 当前 active slice
- 当前文档状态
- 当前工作区草稿状态
- 最近完成 checkpoint
- 下一 gate
- 是否可以继续实现
- 是否必须先等待人工接受

在输出恢复判断前，不要编辑文件。
```

## Prompt 7：自主 single-slice 全流程交付

用于你希望 Codex 一次完成：设置 goal、生成/更新 SDD、在预授权范围内接受 SDD、实现、验证、closeout、commit、push。

适用范围：

- Tier 0 / Tier 1 / 低风险 Tier 2 的单 slice。
- 用户已经在 prompt 中写清 Goal、Scope、Exclusions、Acceptance、Verification、Autonomy boundary。
- SDD 变化没有引入新的安全、权限、真实数据、外部 provider、破坏性 migration 或 production readiness 承诺。

不适用范围：

- auth、RBAC、audit、secret、真实公司数据、外部 provider、破坏性 migration、production readiness。
- API/data model/security/governance scope 不清楚。
- 当前工作区有会被覆盖的用户或其他 agent 改动。
- 需要组织级产品决策，而不是实现层保守决策。

```text
请设置并执行一个 Atlas autonomous single-slice full-delivery goal。

Goal mode: autonomous-single-slice
Autonomy level: standard-preauthorized
Slice: <slice slug>
Branch: <current branch or target branch>
Commit message: <type: short description>
Push: yes

Goal:
<用户可见或工程可验证的结果>

Scope:
- <范围内行为 1>
- <范围内行为 2>

Exclusions:
- <明确不做 1>
- <明确不做 2>

Acceptance:
- <可观察完成标准 1>
- <可观察完成标准 2>

Verification:
- <必须运行的命令 1>
- <必须运行的命令 2>
- npm run agent:closeout

Autonomy boundary:
- 本 prompt 预授权 Codex 在上述 Goal / Scope / Exclusions / Acceptance 内生成或更新 SDD。
- 如果 SDD 变化完全落在上述边界内，且通过 SDD gate，则视为本 prompt 已接受该 SDD，可继续实现。
- Codex 可以自行做保守实现决策，但必须优先使用现有 repo patterns。
- Codex 不需要为常规命名、文件组织、测试补充、文档同步、lint/format 修复、非破坏性小重构反复询问用户。
- Codex 必须停止并请求用户决策，如果发现任何 stop condition。

必须读取：
- AGENTS.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/agent-goal-loop-workflow.md
- docs/00-context/agent-goal-loop-workflow.zh-CN.md
- docs/00-context/agent-goal-loop-quickstart.md
- docs/00-context/agent-goal-loop-quickstart.zh-CN.md
- docs/00-context/codex-goal-prompts.zh-CN.md
- docs/00-context/sdd-profile.md
- docs/SDD-BOOTSTRAP.md
- docs/SDD-BOOTSTRAP.zh-CN.md
- 当前 slice 已有 SDD / traceability docs

执行步骤：
1. 创建或确认 goal objective：完成 <slice slug> 从 SDD 到实现、验证、closeout、commit、push 的单 slice 交付。
2. 运行 git status --short 和 git diff --stat，确认不会覆盖无关改动。
3. 选择并报告 workflow tier；standard-preauthorized 只允许 Tier 0/1/低风险 Tier 2。
4. 检查当前 slice SDD 是否存在、双语同步、task 可执行。
5. 如果 SDD 缺失或陈旧，必须使用项目本地 SDD skill chain 更新：
   - atlas-sdd-generate-all
   - req-to-user-story
   - user-story-to-spec
   - spec-to-architecture
   - architecture-to-design
   - design-to-tasks
   - review-doc-quality
6. 运行 SDD gate；若 SDD 完全落在 Autonomy boundary 内，则继续实现，不再询问。
7. 严格按 docs/06-tasks/<slice>-tasks.md 的 task ID 顺序实现。
8. 添加或更新必要测试。
9. 运行 Verification 中列出的命令。
10. 若验证失败，在当前 slice 内执行 scoped fix loop；不要扩大 scope。
11. 更新 traceability、roadmap/progress、lessons learned（如适用）。
12. 运行 npm run agent:closeout。
13. 运行 git diff --check，并确认 secret/private-path/real-data 没有新增风险。
14. 若所有 gate 通过，review git diff。
15. git add 当前 slice 相关文件。
16. git commit，使用上面的 Commit message；不要使用 --no-verify。
17. git push 到当前分支；若上游未设置，使用 git push -u origin <branch>。

Stop conditions:
- 任务属于 Tier 3，或触及 auth/RBAC/audit/secret/真实数据/外部 provider/破坏性 migration/production readiness，而 prompt 没有明确 high risk approval。
- SDD 变化超出 Goal / Scope / Exclusions / Acceptance。
- SDD skill chain 不可用或无法确认已使用。
- API/data model/security/governance 决策不明确。
- 工作区存在会被覆盖的用户或其他 agent 改动。
- 需要真实公司数据、raw secrets、private paths、confidential screenshots 或未批准外部调用。
- verification 或 closeout gate 失败，且无法在当前 slice 内修复。
- commit hook 或 push 失败。

完成报告：
- Goal objective
- Workflow tier
- Slice
- Autonomy boundary 是否满足
- SDD skill chain used
- SDD accepted by preauthorization: yes/no
- Docs changed
- Code changed
- Tests changed
- Task IDs completed
- Verification run
- Closeout gate result
- Commit hash
- Push target
- Residual risks
- Next recommended slice/action
```

如果是高风险 slice，但你确实希望一次 prompt 授权到底，必须把 `Autonomy level` 改成 `high_risk_explicitly_approved`，并在 prompt 中增加：

```text
High-risk explicit approval:
- 我已审阅并接受 <slice> 的 spec/design/API guide/tasks。
- 我授权 Codex 在这些已接受文档范围内实现、验证、closeout、commit、push。
- 如果实现需要改变安全、权限、审计、secret、真实数据、外部 provider 或 migration 语义，必须停止，不得自行决定。
```

## 当前 repo 示例：audit-log-foundation

当前总览显示 `audit-log-foundation` 处在 Wave 3 / Trust And Governance。

如果只是审阅 SDD：

```text
请只读审阅 audit-log-foundation SDD，不实现代码。

读取：
- docs/00-context/repo-status-roadmap.zh-CN.md
- docs/00-context/audit-log-foundation-traceability.zh-CN.md
- docs/03-spec/audit-log-foundation-spec.md
- docs/03-spec/audit-log-foundation-spec.zh-CN.md
- docs/05-design/audit-log-foundation-design.md
- docs/05-design/contracts/audit-log-foundation-API_IMPLEMENTATION_GUIDE.md
- docs/06-tasks/audit-log-foundation-tasks.md

输出：
- 是否可以接受 SDD
- open questions
- high-risk decisions
- 是否可以进入 implementation
- 如果不能，列出需要修改的 SDD sections
```

如果用户已经明确接受 SDD 后再实现：

```text
任务：实现 audit-log-foundation。

前提：用户已明确接受 audit-log-foundation 的 spec、design、API guide 和 tasks。

严格依据：
- docs/03-spec/audit-log-foundation-spec.md
- docs/05-design/audit-log-foundation-design.md
- docs/05-design/contracts/audit-log-foundation-API_IMPLEMENTATION_GUIDE.md
- docs/06-tasks/audit-log-foundation-tasks.md

按 task ID 顺序实现，只推进 audit-log-foundation，不混入 secret-manager、rate-limit 或其他治理 slice。
完成后运行 task verification、npm run agent:closeout，并更新 traceability / roadmap 状态。
```
