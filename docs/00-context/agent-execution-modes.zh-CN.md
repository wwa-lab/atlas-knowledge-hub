# Agent Execution Modes

Atlas 支持两种执行界面，但使用同一套 SDD workflow：

- Codex Agent Goal Mode：用于个人电脑或外部环境，适合 Codex 运行 goal loop。
- GitHub Copilot Chat Mode：用于公司内部默认环境，适合团队成员在 IDE 里用 Copilot Chat 协作。

两种模式的交付合同相同：SDD first、一次只做一个 slice、实现必须对齐 `docs/03-spec/` 和 `docs/06-tasks/`、完成前运行本地 closeout gate、PR/push 由 GitHub Actions 红灯拦截。

## Mode A：Codex Agent Goal Mode

当 Codex goal mode 和项目本地 skills 可用时，使用这个模式。

Codex 可以作为 loop runner：

1. 读取 `AGENTS.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md` 和相关 slice docs。
2. 从 `docs/00-context/agent-goal-loop-quickstart.md` 选择 workflow tier。
3. 创建或复用 `docs/00-context/execution-manifests/` 下的 execution manifest。
4. 使用 `docs/00-context/goal-prompts/single-slice-goal-prompt.md` 或 `docs/00-context/goal-prompts/master-goal-prompt.md`。
5. 生成或更新 SDD artifacts 时使用项目本地 SDD skills。
6. 只实现已接受的 slice scope。
7. 运行 task verification 和 `npm run agent:closeout`。
8. 报告 docs changed、code changed、verification evidence、residual risks 和 blocked checks。

Codex goal mode 是更强的自主模式，因为它可以在一个 agent session 里维持 execution contract、命令输出和修复循环。

## Mode B：GitHub Copilot Chat Mode

公司内部默认使用 GitHub Copilot Chat 时，使用这个模式。

Copilot Chat 是实现助手，不是规则裁判。在这个模式里，人是 loop operator，repo gate 才是 controller。

Copilot Chat 应该：

1. 使用 `.github/copilot-instructions.md` 作为 repository-wide entry instruction。
2. 改文件前读取 `AGENTS.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md` 和相关 SDD docs。
3. 基于明确的 slice、scope、source documents、acceptance criteria 和 verification list 工作。
4. 保持小步改动，并对齐已接受的 SDD。
5. 如果不能直接运行命令，要求开发者运行命令并粘贴输出。
6. 把 `npm run agent:closeout` 和 GitHub Actions 的 `Agent Workflow Gate` 作为最终裁判。

Copilot Chat 不应假设自己有 Codex goal state、Codex skill loading，或能执行项目本地 `.agents/skills`。如果任务要求 Atlas SDD skill chain，而 Copilot 实际不能使用这条 skill chain，它必须明确说明，并要求通过 Codex/Claude 生成 SDD artifacts，或只产出明确标注“缺少 skill-chain evidence”的草稿。

## 能力矩阵

| Capability | Codex Agent Goal Mode | GitHub Copilot Chat Mode |
|---|---|---|
| Primary operator | Codex loop | Human developer plus Copilot |
| Native goal loop | 有 | 没有；用明确 prompt 和 checklist 模拟 |
| Project-local skills | Codex 环境可用时可以使用 | 不要假设可用；必须读取文件或请求其他工具 |
| Repository instructions | `AGENTS.md` 加项目 docs | `.github/copilot-instructions.md`、`AGENTS.md` 和项目 docs |
| Terminal commands | Agent 可在 session 内运行和修复 | 开发者运行，或 Copilot agent mode 提议命令后由人批准 |
| Closeout gate | `npm run agent:closeout` | 同一命令，由开发者运行或批准 agent action |
| PR/push gate | GitHub Actions `Agent Workflow Gate` | 同一个 GitHub Actions gate |
| Best use | 完整 slice 执行、SDD-to-code loop、长验证 | 公司标准开发、定向实现、review、本地 PR 工作 |

## Copilot Chat 启动 Prompt

启动 Copilot Chat 任务时使用这个 prompt：

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

## 团队规则

团队不应该依赖某一个模型“记住流程”。

真正持久的约束栈是：

1. Repository instructions：`.github/copilot-instructions.md`、`AGENTS.md`、`PROJECT_RULES.md`。
2. SDD source of truth：`docs/03-spec/`、`docs/06-tasks/` 和 traceability docs。
3. Local gates：`npm run agent:check-sdd` 和 `npm run agent:closeout`。
4. CI gate：GitHub Actions `Agent Workflow Gate`。
5. Branch protection：merge 前要求 `Agent Workflow Gate` 通过。

## References

- GitHub Copilot repository custom instructions: https://docs.github.com/copilot/customizing-copilot/adding-custom-instructions-for-github-copilot
- GitHub Copilot custom instruction support matrix: https://docs.github.com/en/copilot/reference/custom-instructions-support
- GitHub Copilot agent mode overview: https://docs.github.com/en/copilot/get-started/features
