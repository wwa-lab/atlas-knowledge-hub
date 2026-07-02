# SDD 启动指南：Atlas Knowledge Hub

## 目的

本文是 Atlas Knowledge Hub 的 Spec Driven Development 启动说明。它替代从
Control Tower 导入的原始 bootstrap 内容，改为使用 Atlas 项目的规则、路径、
角色分工、skills 和质量门禁。

当新的 Atlas slice 需要从产品意图推进到可实现的任务清单时，使用本文。

## 工作模式

Atlas 使用分工明确的协作模式：

- Claude Code 负责生成和审查 SDD 文档。
- Codex 负责实现、测试、验证和实现证据汇报。

默认情况下，Claude Code 不负责大范围实现代码；Codex 也不得静默扩大已接受
SDD 文档之外的产品范围。

## 一键 SDD Skill

Atlas slice 文档应从这个入口开始：

- `.claude/skills/atlas-sdd-generate-all/SKILL.md`

同一 skill 也镜像到 Codex 可见路径：

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`

这个 skill 是生成完整双语 SDD 文档集的一键入口。它必须编排项目本地 SDD
skills，而不是临时手写所有文档。

## 必须使用的 Skill 链

Claude Code 应按以下顺序使用项目本地 skills：

| 顺序 | Skill | 用途 |
|---|---|---|
| 1 | `atlas-sdd-generate-all` | 建立 slice contract、编排、文件路径、双语规则和最终一致性门禁。 |
| 2 | `req-to-user-story` | 将需求和 slice 范围转换为用户故事和验收标准。 |
| 3 | `user-story-to-spec` | 将用户故事转换为面向实现的 spec。 |
| 4 | `spec-to-architecture` | 从 spec 派生 architecture、data flow 和 data model。 |
| 5 | `architecture-to-design` | 派生 UX、组件、API、数据和实现设计。 |
| 6 | `design-to-tasks` | 将 design 转换为 Codex 可执行的实现任务。 |
| 7 | `review-doc-quality` | 审查完整性、可追溯性、质量和双语一致性。 |

当 slice 实质改变架构、后端/API contract、持久化、adapter 边界、安全或数据流时，
使用 `architecture-review`。

`review-code-against-design` 只在 Codex 已经完成实现之后使用，不属于初始 SDD
生成流程。

## 生成 SDD 前必须读取的上下文

Claude Code 必须读取或明确处理：

- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/01-requirements/requirement.md`
- `docs/01-*` 到 `docs/06-*` 下相关的现有 slice 文档
- 当前 FE 行为相关时，还必须参考：
  - `frontend/public/atlas-prototype.html`
  - `prototypes/index.html`

如果必要上下文缺失或过期，必须记录到 Open Questions 或 Assumptions，不能静默
编造替代内容。

## 推荐 Slice Goal 格式

让 Claude Code 生成 SDD 时，建议使用：

```text
Goal: <用户可感知的目标>
Slice: <稳定的 kebab-case slice slug>
Phase: <Phase 1 / Phase 2 / Phase 3 / Phase 4>
Scope: <范围内行为和明确排除项>
Sources: <产品文档、FE baseline、参考资料>
Acceptance: <可观察的完成标准>
Verification: <命令、检查或人工 review>
Constraints: <mock-only、adapter、安全、阶段、数据安全限制>
```

如果用户只给了自然语言目标，Claude Code 可以推断最小安全 slice contract，但必须
记录假设。

## 双语输出规则

每个新建或实质更新的 SDD 文档必须有两份：

- 英文：默认文件名。
- 简体中文：`.zh-CN.md` companion。

示例：

- `docs/01-requirements/{slice}-requirements.md`
- `docs/01-requirements/{slice}-requirements.zh-CN.md`
- `docs/03-spec/{slice}-spec.md`
- `docs/03-spec/{slice}-spec.zh-CN.md`

两种语言版本必须保持一致：

- Scope。
- Requirement IDs。
- User story IDs。
- Acceptance criteria。
- Architecture decisions。
- API contracts。
- Task IDs。
- Verification requirements。
- Open questions。

不要翻译稳定 ID。

## Atlas SDD 文档集

每个 slice 生成或更新以下文档。

| 阶段 | 英文路径 | 中文路径 | 是否必需 |
|---|---|---|---|
| Traceability | `docs/00-context/{slice}-traceability.md` | `docs/00-context/{slice}-traceability.zh-CN.md` | 是 |
| Requirements | `docs/01-requirements/{slice}-requirements.md` | `docs/01-requirements/{slice}-requirements.zh-CN.md` | 是 |
| User Stories | `docs/02-user-stories/{slice}-stories.md` | `docs/02-user-stories/{slice}-stories.zh-CN.md` | 是 |
| Specification | `docs/03-spec/{slice}-spec.md` | `docs/03-spec/{slice}-spec.zh-CN.md` | 是 |
| Architecture | `docs/04-architecture/{slice}-architecture.md` | `docs/04-architecture/{slice}-architecture.zh-CN.md` | 是 |
| Data Flow | `docs/04-architecture/{slice}-data-flow.md` | `docs/04-architecture/{slice}-data-flow.zh-CN.md` | 是 |
| Data Model | `docs/04-architecture/{slice}-data-model.md` | `docs/04-architecture/{slice}-data-model.zh-CN.md` | 是 |
| Design | `docs/05-design/{slice}-design.md` | `docs/05-design/{slice}-design.zh-CN.md` | 是 |
| API Guide | `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.zh-CN.md` | 后端/API 工作前必需 |
| Tasks | `docs/06-tasks/{slice}-tasks.md` | `docs/06-tasks/{slice}-tasks.zh-CN.md` | 是 |

对于只涉及 Phase 1 前端的 slice，只有当 tasks 和 traceability 明确说明后端/API
不在范围内时，API Guide 才可以延期。

## 文档生成顺序

按以下顺序执行：

1. 建立 slice contract。
2. Requirements。
3. User stories。
4. Specification。
5. Architecture。
6. Data flow。
7. Data model。
8. Design。
9. API Guide，仅当后端/API 在范围内。
10. Tasks。
11. 更新 traceability。
12. 文档质量 review。
13. 用户接受 SDD scope。
14. Handoff 给 Codex 实现。

## 开始写代码前的最低门禁

开始实现前，slice 必须能回答：

- 这个 slice 解决什么问题？
- 用户或 actor 是谁？
- 范围内是什么？
- 明确排除什么？
- Happy path 必须发生什么？
- Empty、error、loading、edge states 必须发生什么？
- 需要什么数据结构或状态 contract？
- 哪个 component、module、API 或 adapter 拥有什么责任？
- 如何验收？
- 需要哪些验证命令或人工检查？
- 英文和中文 SDD 是否同步？

## 阶段纪律

Atlas 使用分阶段交付：

| 阶段 | 范围 |
|---|---|
| Phase 0 | 静态原型和 SDD artifacts。 |
| Phase 1 | Vue 前端 shell 和 mock data。 |
| Phase 2 | 后端 metadata API 和持久化。 |
| Phase 3 | Converter/parser/storage/vector/model adapters 和处理流水线。 |
| Phase 4 | 生产化：auth、RBAC、secrets、audit、monitoring、deployment。 |

除非 spec 和 tasks 明确批准，否则不得跳过阶段。

## 当前产品基线

当前 Phase 1 前端工作：

- `frontend/public/atlas-prototype.html` 是 FE fidelity baseline。
- `prototypes/index.html` 是静态 review 镜像。
- 视觉和交互 parity 应跟随当前 FE baseline，而不是旧的对话优先原型。

## Atlas 专属约束

每个 SDD slice 必须保留这些约束：

- Phase 1 使用 mock-only。
- 不使用真实公司文档。
- 不提交凭证、原始 API keys、tokens、私有路径或机密截图。
- 原型/前端 mock 工作不得引入外部网络调用或外部云依赖。
- Parser、converter、model、vector database、storage、search 必须在 adapter 边界后面。
- `trinity-office` 和 `document-normalize` 是 adapter 后的内部工具，不是产品 workflow 直接依赖。
- Wiki、Review、Graph、Ask 中必须保留 source trace、confidence 和 review status。
- LLM 生成或修改的内容，在 SME 审核或确定性验证前必须保持 review-required。

## Codex Handoff

Claude Code 生成双语 SDD 文档并由用户接受 scope 后，应明确 handoff 给 Codex：

```text
Codex: implement <slice> strictly against docs/03-spec/<slice>-spec.md and
docs/06-tasks/<slice>-tasks.md. Do not expand scope. Preserve current FE
baseline and run the verification listed in the tasks.
```

Codex 应报告：

- 已阅读的文档。
- 修改的代码。
- 已运行的测试和检查。
- 跳过的检查及原因。
- 剩余风险。
- SDD 与实现之间的任何 mismatch。

## 质量门禁

SDD handoff 前，检查：

- 每个被触碰的 artifact 都有英文和中文 SDD 文件。
- 两种语言的 IDs 一致。
- Requirements 能映射到 stories、spec、design 和 tasks。
- Spec 是行为 source of truth。
- Tasks 对 Codex 可执行。
- 后端/API 在范围内时 API Guide 存在。
- 前端-only 工作省略 API Guide 时已记录原因。
- Adapter 边界明确。
- 前端工作引用当前 FE baseline。
- Open questions 明确。
- 已执行 `review-doc-quality`，或记录跳过原因。

实现完成前，按当前 task plan 验证。Phase 1 前端通常包括：

- `npm run typecheck`
- `npm run build`
- `npm run test:coverage`
- `npm run e2e`
- `git diff --check`
- Dependency/network scan。
- Secret/private-path scan。

## 简单判断

如果某个实现决策无法追溯到 requirement、story、spec、design 或 task，说明 SDD
还没有准备好交给 Codex 实现。
