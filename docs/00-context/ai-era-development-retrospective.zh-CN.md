# AI 时代如何做开发：以 Atlas Knowledge Hub 为例

我们在 Atlas Knowledge Hub 里真正做的事情，不只是让 AI 写代码。

更准确地说，我们在搭一套让 AI 能稳定工作的开发系统：人定义方向和边界，SDD 定义事实来源，Goal 定义执行合同，Loop 负责推进和修复，CI/Gate 做裁判，Lessons Learned 让系统持续进化。

这篇文章记录这套模式是怎么长出来的，也记录它为什么适合在团队里推广。

## 一、先建项目规则，再写业务代码

AI 时代开一个新项目，最容易犯的错是马上让模型开始 scaffold、写组件、写接口。

Atlas 的经验刚好相反。这个项目最重要的早期资产，不是 Vue、Spring Boot 或数据库，而是这些文件：

- `AGENTS.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `.github/copilot-instructions.md`

这些文件的作用，是把团队的口头共识变成机器可读的项目规则。

`AGENTS.md` 告诉 agent 怎么工作。  
`PROJECT_RULES.md` 定义产品、架构、安全、SDD 的硬约束。  
`DEVELOPMENT_STANDARDS.md` 定义工程质量、测试、CI、review 的标准。  
`sdd-profile.md` 定义这个项目自己的 SDD 形态。  
`.github/copilot-instructions.md` 则给公司内部默认使用的 GitHub Copilot Chat 一个稳定入口。

这一步很关键。

没有这些规则，模型每次只能根据当前 prompt 猜测项目习惯。不同的人、不同模型、不同 IDE，会产生不同风格的代码和文档。短期看很快，长期看会变成混乱。

有了这些规则，项目就有了自己的运行环境。模型不是凭感觉工作，而是在一个被项目定义好的边界里工作。

所以，AI 时代的项目初始化，不应该只问“用什么技术栈”，还要问：

- agent 应该读哪些文件？
- 哪些行为绝对不能做？
- SDD 的 source of truth 是哪里？
- 什么叫完成？
- 什么情况必须红灯？
- 经验教训沉淀到哪里？

这就是项目的“AI 开发宪法”。

## 二、SDD：先定义行为，再实现行为

Atlas 不是从一句 prompt 直接跳到代码。

我们把 feature slice 拆成一条 SDD 链路：

```text
requirements
-> user stories
-> spec
-> architecture
-> data flow
-> data model
-> design
-> API implementation guide
-> tasks
-> implementation
```

其中最重要的约定是：

- `docs/03-spec/` 是行为 source of truth。
- `docs/06-tasks/` 是实现 checklist。
- `docs/00-context/{slice}-traceability.md` 记录执行证据和状态。
- 新的或被更新的 SDD 文档要保持英文和简体中文同步。

这套机制解决的是一个很实际的问题：AI 很擅长补全，但也很容易补过头。

如果没有 SDD，模型会把用户的一句话扩展成它认为合理的产品。它可能加权限、加数据库、加外部服务、加复杂抽象，看起来完整，但未必符合当前阶段。

Atlas 的规则是：实现不能超过已接受的 SDD。

如果代码行为和 spec 不一致，要么先更新 SDD，要么停止并报告 mismatch。这样一来，开发就从“模型觉得应该这么做”变成“实现必须对齐已接受的行为合同”。

SDD 的价值不是写很多文档。它的价值是把模糊空间变小，让 agent 少猜。

## 三、Goal 模式和 Loop Engineering：让 agent 有执行合同

一个好的 AI 开发任务，不应该只是：

```text
帮我实现 wiki ingest。
```

它应该被转换成一个 goal：

- Goal：要交付什么用户可见结果。
- Slice：属于哪个稳定 slice。
- Scope：包含什么。
- Out of scope：不包含什么。
- Source of truth：哪些 SDD 文件是准绳。
- Acceptance：什么现象证明完成。
- Verification：要跑哪些命令和检查。
- Constraints：哪些安全、数据、架构边界不能突破。

这就是 goal 模式的本质：它不是一句需求，而是一份执行合同。

在 Codex Agent Goal Mode 里，Codex 可以作为 loop runner。它可以读规则、读 SDD、执行任务、跑测试、修失败、再跑 gate，直到完成或遇到 blocker。

Loop Engineering 的价值在这里体现出来：

- 不是一次生成完就结束。
- 而是不断验证、修复、补证据。
- 但 loop 不能绕过 gate。

换句话说：

> Goal 决定边界，Loop 负责推进，Gate 负责刹车。

没有 Goal，agent 会越做越散。  
没有 Loop，SDD 只是一堆静态文档。  
没有 Gate，自动化会变成失控的自动化。

Atlas 现在用 `npm run agent:closeout` 和 GitHub Actions 的 `Agent Workflow Gate` 把 closeout 变成红灯/绿灯。agent 可以循环修复，但最终要接受 gate 的裁判。

## 四、稳定输出不是靠模型自觉，而是靠系统约束

团队推广 AI 开发时，一个核心问题是：

> 不同的人、不同模型、不同环境，怎么得到相对稳定的输出？

答案不是写一个更长的 prompt。

Prompt 是入口，不是制度。

Atlas 的做法是把稳定性拆成几层：

1. Repository instructions：`AGENTS.md`、`.github/copilot-instructions.md`、`PROJECT_RULES.md`。
2. SDD source of truth：`docs/03-spec/`、`docs/06-tasks/`、traceability docs。
3. Local gates：`npm run agent:check-sdd`、`npm run agent:closeout`。
4. CI gate：GitHub Actions `Agent Workflow Gate`。
5. Branch protection：把 `Agent Workflow Gate` 设置成 required check。

这样，即使不同人使用不同模型，只要走同一个 repo、同一套 SDD、同一个 PR/push gate，结果就会被拉回同一条轨道。

这也是为什么公司内部只使用 GitHub Copilot Chat，并不妨碍这套 workflow 落地。

Copilot Chat 不一定有 Codex 的 goal state，也不一定能执行项目本地 `.agents/skills`。但它可以读取 `.github/copilot-instructions.md`，按 SDD 实现，让开发者运行命令，最后由 `npm run agent:closeout` 和 CI gate 判断是否通过。

真正稳定的不是某个模型，而是模型外面的工程系统。

## 五、Lessons Learned：让 agent 和项目一起进化

AI 开发最浪费的事情，是同一个错误在不同对话里重复发生。

所以 Atlas 明确规定：接受评审发现 mismatch 时，不只是解释，也不只是修当前代码。

要判断它是不是一个可复用教训。如果是，就沉淀到：

- `docs/00-context/lessons-learned.md`
- 对应的 SDD 文档
- task verification
- development standards
- project rules
- agent instructions
- checklist
- tests

这背后的原则是：

> 一次错误，至少要改进一个预防机制。

如果只是把经验留在聊天记录里，下一次换模型、换人、换上下文，错误还会回来。

真正有用的 lesson learned，不是“我们以后注意”，而是：

- 哪个 requirement 应该补？
- 哪个 spec 应该改？
- 哪个 task 应该增加验收项？
- 哪个 gate 应该检查？
- 哪个 test 应该覆盖？
- 哪条 agent instruction 应该写死？

这就是 agent 持续进化的方式。

不是期待模型永远不犯错，而是让每次犯错都让系统更难犯同样的错。

## 六、适配不同执行模式：Codex Agent 和 GitHub Copilot Chat

Atlas 现在把两种模式分清楚了。

### Codex Agent Goal Mode

适合个人电脑、外部开发环境，或者 agent 能完整运行命令和修复循环的场景。

在这个模式下，Codex 是 loop runner：

- 读取项目规则和 SDD。
- 创建或复用 execution manifest。
- 使用 goal prompt。
- 必要时使用项目本地 SDD skills。
- 实现一个 slice。
- 跑测试和 gate。
- 修复失败。
- 输出 closeout evidence。

这个模式适合完整 slice、长任务、SDD-to-code、复杂验证。

### GitHub Copilot Chat Mode

适合公司内部默认环境。

在这个模式下，Copilot 是 implementation assistant，人是 loop operator，repo gate 是 controller。

Copilot Chat 应该做的是：

- 读取 `.github/copilot-instructions.md`。
- 读取 `AGENTS.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`。
- 读取相关 SDD。
- 小步实现。
- 告诉开发者要运行哪些命令。
- 不把未跑过的 gate 说成通过。

它不应该假设自己拥有 Codex goal state，也不应该假装已经跑过项目本地 SDD skill chain。

这不是降低标准，而是把责任边界说清楚。

Codex Agent 是更强的自动执行模式。  
Copilot Chat 是更普遍的团队协作模式。  
两者共享同一套 SDD、规则、gate 和 lesson learned。

## 七、这个项目形成的开发模式

如果用一句话总结 Atlas 当前的模式：

> Spec-driven + agent-executed + CI-governed software delivery.

更通俗一点：

> 让 AI 写代码，但让项目系统决定什么算正确。

这套模式不是传统敏捷，也不是简单 AI coding。它把 AI 放进一个工程闭环里：

```text
Project Rules
-> SDD
-> Goal
-> Loop
-> Implementation
-> Verification
-> CI Gate
-> Lessons Learned
-> Updated Rules / SDD / Tests
```

每一环都不是装饰。

Project Rules 让模型知道边界。  
SDD 让模型知道事实。  
Goal 让模型知道本次任务。  
Loop 让模型能推进和修复。  
Verification 让结果有证据。  
CI Gate 让失败变红灯。  
Lessons Learned 让系统持续变好。

## 八、如果要在团队大规模推广

推广重点不应该是培训大家“怎么和 AI 聊天”。

更应该标准化这些东西：

- repo 初始化模板
- `AGENTS.md`
- `.github/copilot-instructions.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- SDD profile
- SDD artifact templates
- goal prompt templates
- Copilot Chat 启动 prompt
- closeout checklist
- `agent:check-sdd`
- `agent:closeout`
- GitHub Actions gate
- branch protection required check
- lessons learned loop

团队可以允许不同的人使用不同工具，但不能允许每个人使用不同的完成标准。

这就是 AI 时代开发管理的变化：

- 过去重点是规范人怎么写代码。
- 现在还要规范 agent 怎么理解任务、怎么执行、怎么证明完成。

最终目标不是让每个人都成为 prompt expert。

最终目标是让项目本身足够清楚、足够可验证、足够能吸收教训，以至于不同人、不同模型进入项目后，都能沿着同一条轨道工作。

## 结语

Atlas 给我们的最大启发是：AI 开发的关键能力，不是单次生成能力，而是系统设计能力。

模型会变。工具会变。团队环境也会变。

但只要项目有清楚的规则、稳定的 SDD、明确的 goal、可循环的执行方式、自动化 gate、可沉淀的 lesson learned，它就能持续吸收不同 agent 的能力，而不是被不同 agent 的差异拖着走。

AI 时代，优秀开发团队的核心能力会从“写代码”扩展为：

> 设计一个让 AI 稳定产出正确代码的系统。

Atlas Knowledge Hub 正在变成这样一个系统。
