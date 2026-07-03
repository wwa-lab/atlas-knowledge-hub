# 切片路线图与「双命令」SDD 循环

用 Agent 编码工具（Claude Code、Codex，或任何能读取本仓库技能的 Agent）驱动 Atlas Knowledge Hub 的操作手册。

每个切片是两件事：

- **生成 SDD** —— 一次调用产出完整的双语 SDD 文档集（需求 → 任务 + 溯源）。
- **实现** —— 一句话依据 `docs/06-tasks/{slice}-tasks.md` 对照 spec 执行完所有任务。

**两个 Agent 都能做这两件事。** 下方模板与具体 Agent 无关 —— `atlas-sdd-generate-all` 技能同时为 Claude Code（`.claude/skills/`）与 Codex（`.agents/skills/`）做了镜像，所以手边是哪个 Agent，它就能生成 SDD；你更顺手的那个就能实现。重要的是达成目标、通过门禁，而非哪个工具跑哪一步。一个常见且有效的默认分工是 Claude Code 写 SDD、Codex 批量实现 —— 但这只是约定而非铁律，哪种更快就怎么换。

本文件是切片待办与可复制提示词模板的唯一出处。它是关于「工作流」的元文档，不重新定义由 `docs/03-spec/` 拥有的行为。文档链与 ID 规则见 `docs/00-context/sdd-profile.md`，生成契约见 `atlas-sdd-generate-all` 技能。

英文正本：`docs/00-context/slice-roadmap.md`。

---

## 循环（每个切片）

```
1. 生成 SDD:  atlas-sdd-generate-all       →  {slice} 的完整双语 SDD 文档集
2. 人:        审阅 SDD（尤其 spec + tasks），接受或提出修改
3. 实现:      「依据 spec + tasks 实现 {slice}」  →  代码 + 测试
4. 评审:      review-code-against-design    →  一致性核对 + 经验沉淀
5. 人:        质量门通过后合并
```

第 1、3、4 步都可由 Claude Code 或 Codex 执行 —— 手边开着哪个就用哪个。第 1 步是一条命令，第 3 步是一句话。第 2、5 步是仅有的两个强制人工门。

**无论哪个 Agent 执行，阶段纪律都不可让步。** 在当前阶段的准入门未满足前，不要为后阶段切片生成 SDD，也不要让执行实现的 Agent 搭建后端/数据库/适配器（见矩阵）。CLAUDE.md 与 SDD profile 的门禁高于任何便利性。

---

## 切片待办

slug 是稳定的 kebab-case 标识。ID 遵循 profile：`REQ-{SLICE}-###`、`US-{SLICE}-###`、`T-{SLICE}-###`（slug 大写）。状态：✅ 完成 · 🔨 进行中 · ⬜ 未开始 · 🔒 门控（准入条件未满足）。

| 阶段 | 切片 slug | 范围概述 | 归属边界 | 状态 |
|---|---|---|---|---|
| 1（前端） | `knowledge-space` | Vue 3 外壳复刻已接受 IA：首页知识库列表、全局多知识库对话、空间外壳、文档、处理中心、Wiki、图谱、设置 —— 仅 mock 数据 | 仅前端，无 API | ✅（T-KS-001→031） |
| 1（前端） | `folder-upload` | 文件夹/ZIP 上传 mock：文件清单、树、批次创建、mock 状态/报告 | 仅前端，无 API | ✅ 已实现（T-FU-001→013；T-FU-011 可选推迟） |
| 2（API） | `metadata-api` | Spring Boot 元数据服务：批次/文件/空间实体、PostgreSQL + Flyway、去 mock 契约 | 后端 + DB | ✅ 已实现（T-MA-001→013；T-MA-014 延后 FE 切换） |
| 3（适配器） | `converter-adapter` | `trinity-office` Office→PDF 置于 converter 接口之后 | 适配器，禁止直连 | ✅ 已实现（T-CA-001→010；mock-engine 已验证；真实 runtime contract 延后） |
| 3（适配器） | `parser-adapter` | `document-normalize` PDF→Markdown/图片 置于 parser 接口之后 | 适配器，禁止直连 | ✅ 已实现（T-PA-001→010；mock-engine 已验证；真实 runtime contract 延后） |
| 3（适配器） | `storage-adapter` | S3 兼容对象存储置于 storage 接口之后 | 适配器，禁止直连 | ✅ 已实现（T-SA-001→010；`mvn verify` 已通过，使用 mock storage） |
| 3（适配器） | `vector-adapter` | pgvector/向量库置于 vector 接口之后 | 适配器，禁止直连 | 🔒 需 Phase 2 |
| 3（适配器） | `model-adapter` | LLM/embedding 供应商置于 model 接口之后，secret 脱敏 | 适配器，禁止直连 | ✅ 已实现 |
| 4（加固） | `review-publish` | SME 审核状态机 + 已批准 Markdown 发布到 Wiki | 全栈 | 🔒 需 Phase 3 |
| 4（加固） | `knowledge-graph` | 从已批准页面抽取节点/边 + 可解释可视化 | 全栈 | 🔒 需 Phase 3 |
| 4（加固） | `ask-rag` | 基于已批准内容、带溯源、审核感知的 Ask | 全栈 | 🔒 需 Phase 3 |

切片边界是指引而非铁律：若任务清单会超出「一次可评审的实现量」，就拆分并在溯源中记录。Phase 3 每个适配器切片可作为独立的 generate-all 单元，保持契约小而清晰。

---

## 各阶段验证与约束矩阵

generate-all 提示词必须把对应行写进切片的 `Verification` 与 `Constraints`，且 tasks 文档要携带确切命令。

| 阶段 | 验证（报「完成」前必须跑） | 硬约束 | API guide |
|---|---|---|---|
| 1 前端 | `npm run typecheck` · `npm run test` · `npm run build` · `npm run e2e` | 同步镜像 `prototypes/index.html` ↔ `frontend/public/atlas-prototype.html`；仅 mock 数据；**禁止**新增网络调用或外部依赖 | 可省 —— tasks 须注明「本 slice 无 API 契约」，溯源须记录该省略 |
| 2 API | `mvn verify` · Flyway 迁移校验 · API 契约测试 | 不硬编码单一 DB 为唯一实现；secret 只做**脱敏/状态**，绝不明文；无真实公司数据 | **必需** —— data-model + API guide 须在实现开始**前**被接受 |
| 3 适配器 | 针对**mock 引擎**的单元 + 集成测试 | parser/converter/model/vector/storage **只**走产品面适配器；禁止直连工具；禁止硬编码单一实现 | 每个适配器切片需适配器契约 |
| 4 加固 | 对所改层的完整单元 + 集成 + E2E | 所有 Markdown/元数据保留 source trace、confidence、review status；LLM 产物在核验前保持 review-required | 引入新端点处必需 |

每个阶段还要跑 CLAUDE.md 的 Phase 0/1 基线检查：对改动的 HTML/CSS/JS 静态语法检查、`git diff --check`、新增网络/依赖扫描、secret/私有路径扫描。跳过任何检查须写明原因 —— 绝不暗示未跑的检查通过了。

---

## 模板 A —— 一次生成全套 SDD

在**本仓库**内粘贴给你的 SDD Agent（**Claude Code 或 Codex**）。它填好 goal 块并调用 `atlas-sdd-generate-all`，后者串联 `req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality`，并把每个文件（EN + `.zh-CN.md`）写到 Atlas 路径。

```text
使用 atlas-sdd-generate-all 技能，为一个切片生成完整的双语 SDD 文档集。

Goal: <一句话描述面向用户的成果>
Slice: <kebab-case-slug>
Phase: <1 FE | 2 API | 3 adapter | 4 hardening>
Scope: <范围内行为> | Exclusions: <明确排除项>
Sources: <可依据的原型界面 + docs/*>
Acceptance: <可观察的完成标准>
Verification: <从 docs/00-context/slice-roadmap.md 复制对应行>
Constraints: <复制对应行：mock-only / adapter / secret 脱敏 / 保留溯源>

要求：
- 阅读 PROJECT_RULES.md、AGENTS.md、DEVELOPMENT_STANDARDS.md、docs/00-context/sdd-profile.md、
  docs/01-requirements/requirement.md；切片涉及前端时阅读 FE 基线文件。
- 每个改动的产物都写 EN 与 .zh-CN.md 两份；REQ/US/T 的 ID 跨语言保持一致。
- 任务必须可被 Codex 执行：每条映射到某个 REQ id + spec 章节，验证带确切命令，
  并在适用处注明 mock-only / 无网络 / 适配器 / secret 脱敏 约束。
- 若后端/API 不在范围内，省略 API guide 并在溯源中记录该决定。
- 本轮不实现产品代码，只产出 SDD。
- 以 review-doc-quality 门收尾，并给出推荐的 Codex 交接命令。
```

产物落在 `docs/01-requirements/`、`docs/02-user-stories/`、`docs/03-spec/`、`docs/04-architecture/`、`docs/05-design/`（含 `contracts/`）、`docs/06-tasks/`，以及 `docs/00-context/{slice}-traceability.md`。

---

## 模板 B —— 一句话执行完所有任务

SDD 经人工接受后，给你的实现 Agent（**Codex 或 Claude Code**）一句话：

```text
严格依据 docs/03-spec/{slice}-spec.md 与 docs/06-tasks/{slice}-tasks.md 实现 {slice} 切片：按 ID 顺序完成每一条任务，遵守每条任务标注的 Constraints 与 Verification，以 docs/03-spec 为行为唯一真相源，不扩大范围；若实现将偏离 spec，停下并指出不一致，而不是绕过它编码。
```

需要把护栏说全时的更稳版本：

```text
任务：实现 {slice} 切片。
契约：docs/06-tasks/{slice}-tasks.md 是清单；docs/03-spec/{slice}-spec.md 是行为唯一真相源；docs/05-design/{slice}-design.md 与 data-model/API guide 是设计契约。
规则：按 ID 顺序完成任务；跑每条任务的 Verification 并报告结果；parser/converter/model/vector/storage 全走适配器（Phase 3+）；Phase 1 仅 mock、无外部网络；secret 脱敏/状态化，绝不明文；所有 Markdown/元数据保留 source trace、confidence、review status；只改任务要求的文件。
偏离：若某条任务无法按原文满足，停下并报告 spec/task 的不一致 —— 不要静默重设计。
完成的含义：所有任务勾选、验证已跑（或显式标注 blocked 及原因），并给出最终报告，列明改动文件、已跑检查、残留风险。
```

---

## 模板 C —— 收尾评审（可选但推荐）

实现步骤报完成后，在任一 Agent 中运行：

```text
使用 review-code-against-design，将 {slice} 实现对照 docs/03-spec/{slice}-spec.md、
docs/05-design/{slice}-design.md、docs/06-tasks/{slice}-tasks.md 核对。按严重度报告一致性缺口。
跑该切片的 Verification 行。对任何验收不一致，更新能防止复发的产物
（spec/design/task/standard/rule/test），并记入 docs/00-context/lessons-learned.md —— 不只停留在对话里。
```

---

## 护栏速记

- **一次一个切片。** 走完循环（SDD → 接受 → 实现 → 评审）再开下一个。
- **门禁高于便利。** API guide + data-model 未接受前不做后端；Phase 2 未完成前不做适配器；任何时候禁止直连工具。
- **双语对齐。** 每个 SDD 产物都有 EN + `.zh-CN.md`；ID 从不翻译。
- **溯源/置信/审核** 在 Markdown 与元数据的每次转换中都要留存。
- **仅 mock、无 secret**（原型/Phase 1）；任何地方都不放真实公司数据、私有路径或明文凭据。
- **外科式范围。** 每一处改动都能追溯到 goal、某个 REQ、某条规则或某步验证。
