# Atlas 产品分阶段 Goal 执行计划

本文用于把 `ROADMAP.zh-CN.md` 拆成可交给 Codex `set goal` 模式逐阶段执行的目标。目标是让 Codex 按阶段完成产品建设，你最后做统一验收。

编写时间：2026-07-05  
基线分支：`develop-leo`  
基线提交：`49110f09700eaf35d74c8c3224ea01c72a96ed29`

> 注意：每次执行 goal 前，Codex 都必须先检查 `git status --short`。如果有未提交改动，不得覆盖无关改动；若改动影响当前目标，必须先读懂再继续。

## 使用方式

每次只复制一个阶段提示词给 Codex。不要一次要求 Codex 完成 A-J 全部阶段。

如果希望 Codex 持续执行多个阶段，使用本文的「持续执行总提示词」。持续执行仍然必须按 batch checkpoint 推进，不允许一次性把所有阶段混在一个无边界实现里。

推荐顺序：

1. Phase A：Vue 产品外壳对齐。
2. Phase B：知识空间详情页对齐。
3. Phase C：上传到批处理 mock 闭环。
4. Phase D：处理中心。
5. Phase E：LM Wiki。
6. Phase F：知识图谱。
7. Phase G：可信问答。
8. Phase H：设置与管理。
9. Phase I：API-backed Vue 切换，按 I1-I7 子目标执行。
10. Phase J：内部 Beta 加固准备。
11. Final：统一验收证据整理。

每个阶段完成后，Codex 的最终报告必须包含：

- 改了哪些文档。
- 改了哪些代码。
- 跑了哪些验证命令。
- 生成了哪些截图或 E2E 证据。
- 哪些检查跳过了，原因是什么。
- 当前成熟度等级：L1 prototype、L2 Vue parity、L3 API-backed、L4 internal beta 或 L5 production-ready。
- 是否更新了 `ROADMAP.md`、`ROADMAP.zh-CN.md`、相关 traceability 和 slice roadmap。
- 是否更新了 `docs/00-context/product-goal-progress.zh-CN.md`。

## 全局执行规则

这些规则适用于每一个阶段提示词：

- 先读 `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`ROADMAP.md`、`ROADMAP.zh-CN.md`、`docs/00-context/sdd-profile.md`。
- 读当前阶段相关 SDD：`docs/03-spec/`、`docs/05-design/`、`docs/06-tasks/`、`docs/00-context/*traceability*`。
- 如果 SDD 缺失、过时或与目标冲突，先补齐或更新中英文 SDD，再编码。
- 实现必须在真实 Vue 产品页完成，不能只改 iframe 原型、旁路 workbench 或 debug panel。
- 原型可作为体验参考，但不得复制 WeKnora 源码、项目结构、UI 资产、图标、专有样式或实现细节。
- 只能使用 mock/sample 数据；不得提交真实公司资料、真实截图、密钥、私有路径、日志或机密内容。
- parser、converter、model、vector、storage、search 必须经过产品侧适配器边界。
- 不能让用户一步步手动验证常规行为；Codex 必须自己跑测试、截图、检查，并把证据交给用户。
- 如果目标不能完成，不要标记 complete；说明 blocker、已完成部分、下一步最小解法。

## SDD 文档处理协议

SDD 是每个阶段的准入门，不是收尾补丁。Codex 在每个 Phase 或 Phase I 子目标开始前必须先判断该阶段是否已有可执行 SDD。

必须检查的文档类型：

- `docs/01-requirements/{slice}-requirements.md` 与 `.zh-CN.md`。
- `docs/02-user-stories/{slice}-stories.md` 与 `.zh-CN.md`。
- `docs/03-spec/{slice}-spec.md` 与 `.zh-CN.md`。
- `docs/04-architecture/{slice}-architecture.md` 与 `.zh-CN.md`。
- `docs/04-architecture/{slice}-data-flow.md` 与 `.zh-CN.md`。
- `docs/04-architecture/{slice}-data-model.md` 与 `.zh-CN.md`。
- `docs/05-design/{slice}-design.md` 与 `.zh-CN.md`。
- `docs/05-design/contracts/{slice}-API_IMPLEMENTATION_GUIDE.md` 与 `.zh-CN.md`，仅当后端/API/适配器行为在范围内时必需。
- `docs/06-tasks/{slice}-tasks.md` 与 `.zh-CN.md`。
- `docs/00-context/{slice}-traceability.md` 与 `.zh-CN.md`。

处理规则：

- 如果 SDD 缺失：先补齐中英文 SDD，再实现。
- 如果 SDD 存在但与 `ROADMAP.zh-CN.md`、当前原型、真实 Vue 实现或用户目标冲突：先更新 SDD，再实现。
- 如果只是局部 UI parity 且已有更大切片 SDD 可覆盖，Codex 可以在 traceability 中记录复用哪个现有 SDD，但必须说明为什么不新建切片。
- 如果 SDD 变更会改变产品范围、架构边界、安全边界、真实数据/密钥处理、外部 provider 调用或 API contract，Codex 必须停下来让用户接受 SDD 后再编码。
- 如果 SDD 只是补齐已明确的 Roadmap 范围、验收标准、任务清单和中英文同步，本文授权 Codex 在同一个 goal 中先更新 SDD，再继续实现。
- 编码必须以 `docs/03-spec/{slice}-spec.md` 为行为真相源，以 `docs/06-tasks/{slice}-tasks.md` 为任务清单。
- 完成后必须更新 traceability、`docs/00-context/slice-roadmap.md`、`docs/00-context/slice-roadmap.zh-CN.md`，以及 `ROADMAP.md` / `ROADMAP.zh-CN.md` 中对应阶段成熟度。

SDD close-out 报告必须说明：

- 新增或更新了哪些中英文 SDD 文档。
- 哪个 spec 章节定义了本阶段行为。
- 哪些 task ID 已完成。
- 哪些 task 延后，为什么延后。
- 实现是否偏离 SDD；如有偏离，先修 SDD 或停止报告。

## Session 中断与恢复协议

持续执行时，Codex 不能依赖聊天记忆恢复状态。每个 batch 和 phase 完成后，必须更新进度账本：

```text
docs/00-context/product-goal-progress.zh-CN.md
```

恢复或新 session 开始时，Codex 必须先执行恢复协议：

1. 读取 `docs/00-context/product-goal-progress.zh-CN.md`。
2. 读取 `ROADMAP.md`、`ROADMAP.zh-CN.md` 和本文。
3. 运行 `git status --short`，确认有哪些未提交改动。
4. 查看最近改动：`git diff --stat`，必要时查看 `git diff`。
5. 对照进度账本确认最后完成的 batch、phase、task ID、验证证据和 blocker。
6. 如果发现代码已改但账本/roadmap/traceability 未更新，先补状态文档，再继续。
7. 如果发现验证没有跑完、失败或证据缺失，优先补跑验证或标记 blocked。
8. 如果发现上次中断在文件编辑中间，先检查语法、测试和 diff，再决定继续或回滚自己的未完成改动；不得回滚用户或其他 agent 的无关改动。

异常结束后的继续规则：

- 如果上一个 phase 已有完成证据、roadmap 状态和进度账本记录，可以从下一个 phase 继续。
- 如果上一个 phase 只有部分代码，没有验证证据，继续完成同一 phase，不得跳到下一 phase。
- 如果 SDD 已更新但代码未开始，从该 phase 的实现任务开始。
- 如果代码已实现但 SDD 未同步，先同步 SDD/traceability/roadmap，再验证。
- 如果出现冲突、测试失败、缺少密钥、需要真实公司数据、外部 provider 调用或用户授权，停止并报告 blocker。

## 持续执行总提示词

当你希望 Codex 持续分批执行，而不是每次手动复制单个 Phase 时，使用这个提示词。

```text
请设置并执行一个持续 Codex goal。

Goal: 按 docs/00-context/product-goal-execution-plan.zh-CN.md 分批完成 Atlas Knowledge Hub 产品路线图，从当前进度继续推进到 Final 验收准备。

恢复协议:
- 先读取 docs/00-context/product-goal-progress.zh-CN.md；如果文件不存在，创建初始进度账本。
- 读取 README.md、PROJECT_RULES.md、DEVELOPMENT_STANDARDS.md、ROADMAP.md、ROADMAP.zh-CN.md、docs/00-context/product-goal-execution-plan.zh-CN.md、docs/00-context/sdd-profile.md。
- 运行 git status --short 和 git diff --stat，识别未提交改动。
- 不覆盖用户或其他 agent 的无关改动。
- 如果上一轮异常中断，先根据进度账本、roadmap、traceability、git diff 和验证证据判断恢复点。

执行规则:
- 每次只执行一个 batch。
- 每个 batch 内按对应 Phase 提示词执行，不扩大范围。
- 每个 Phase 开始前先执行 SDD 文档处理协议：检查、补齐或更新中英文 SDD；如果 SDD 变更涉及产品范围、架构、安全、API contract、真实数据/密钥或外部 provider，停止让用户接受。
- 本 goal 授权 Codex 对已明确 Roadmap 范围内的 SDD 缺口做中英文补齐，并在同一 goal 中继续实现。
- 每个 Phase 必须在真实 Vue 产品页完成，不能只改 iframe、静态原型、workbench 或 debug panel。
- 每个 Phase 完成后必须运行对应验证，生成截图或 E2E 证据，更新 ROADMAP.md / ROADMAP.zh-CN.md、相关 traceability、slice-roadmap 中英文状态。
- 每个 batch 完成后更新 docs/00-context/product-goal-progress.zh-CN.md，输出 checkpoint 报告，然后自动进入下一个 batch。
- 如果遇到 blocker、SDD 范围冲突、验证失败、需要真实密钥/真实公司数据/外部 provider 调用，停止并报告，不要绕过。
- 最终只做到验收准备，是否通过由用户决定。

Batch 顺序:
1. Phase A + B
2. Phase C + D
3. Phase E + F + G
4. Phase H
5. Phase I1-I3
6. Phase I4-I7
7. Phase J
8. Final 验收证据整理

完成标准:
- 每个阶段报告成熟度：L2 Vue parity、L3 API-backed、L4 readiness 等。
- 每个阶段报告 SDD changed、docs changed、code changed、verification、screenshots/E2E evidence、residual risks。
- 每个 batch 都更新 product-goal-progress.zh-CN.md。
- 不把 task done 当成 product accepted。
```

## 通用验证基线

Codex 每个阶段至少要根据改动范围选择并报告这些检查：

```bash
git diff --check
```

前端改动：

```bash
cd frontend
npm run typecheck
npm run test
npm run build
```

关键用户流改动：

```bash
cd frontend
npm run e2e
```

后端/API 改动：

```bash
cd backend
mvn verify
```

安全与边界扫描应覆盖：

- 新增外部网络调用。
- 新增运行时依赖。
- 明文 secret、token、API key、password。
- 私有绝对路径。
- 真实公司数据或真实截图。
- 适配器外直连 provider/parser/vector/storage/model 引擎。

## Phase A 提示词：Vue 产品外壳对齐

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase A「产品体验重置」：真实 Vue 应用默认显示产品页，而不是 iframe 原型或 P0 workbench。
Slice: product-experience-reset
Scope:
- Vue 3 产品外壳：首页、持久侧边栏、知识空间卡片、全局 Chat 入口、设置浮层。
- 主产品路径不得依赖 iframe.product-frame。
- 原型 iframe 只能保留为参考/调试入口，不能作为默认体验。
- 对照 prototypes/index.html 和 frontend/public/atlas-prototype.html 的已接受布局做产品体验对齐。
Exclusions:
- 不接入真实后端数据。
- 不做生产认证、RBAC、真实上传、真实模型调用。
Sources:
- README.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- ROADMAP.zh-CN.md
- docs/00-context/sdd-profile.md
- prototypes/index.html
- frontend/public/atlas-prototype.html
- frontend/src/App.vue
- frontend/src/styles.css
- frontend/src/App.test.ts
Acceptance:
- 打开 Vite 应用默认看到真实 Vue 产品页。
- 首页、侧边栏、知识空间卡片、全局 Chat、设置浮层在真实 Vue 中可见可交互。
- 默认产品路径不使用 iframe。
- 生成桌面端截图证据，截图中能看到真实 Vue 产品页。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 对真实 Vue 产品页跑 Playwright 截图或 E2E smoke。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 不复制 WeKnora 资产或实现。
- 若 SDD 与当前目标不一致，先更新相关中英文 docs，再实现。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase A 状态与成熟度。
```

## Phase B 提示词：知识空间详情页对齐

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase B「知识空间详情页对齐」：点击 IBM i Modernization 卡片后进入真实 Vue 知识空间详情页。
Slice: knowledge-space-detail-parity
Scope:
- IBM i Modernization 详情页。
- 面包屑、返回首页、空间标题、状态摘要。
- 上传文件夹、上传 ZIP 操作入口。
- Documents、Processing Center、Wiki、Graph 标签页。
- 默认进入 Wiki 标签页。
- 标签切换保留当前空间上下文和必要状态。
Exclusions:
- 不实现真实文件读取、真实解析、真实后端上传。
- 不做生产权限控制。
Sources:
- ROADMAP.zh-CN.md Phase B
- docs/03-spec/knowledge-space-spec.md / .zh-CN.md（若存在）
- docs/06-tasks/knowledge-space-tasks.md / .zh-CN.md（若存在）
- docs/00-context/knowledge-space-traceability.md / .zh-CN.md（若存在）
- prototypes/index.html
- frontend/public/atlas-prototype.html
- frontend/src/App.vue
- frontend/src/styles.css
Acceptance:
- 点击首页 IBM i Modernization 卡片进入真实 Vue 详情页。
- 详情页不是 iframe，不是旁路 workbench。
- Documents、Processing Center、Wiki、Graph 标签页都能切换并显示真实 Vue 内容。
- 布局与原型基线足够接近，可给业务用户演示。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 Playwright E2E：从首页进入空间详情，并切换四个标签页。
- 生成详情页截图证据。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 必须先达到 Vue parity，再考虑 API-backed。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase B 状态与成熟度。
```

## Phase C 提示词：上传到批处理 mock 闭环

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase C「上传到批处理 mock 闭环」：用户能在真实 Vue 产品页模拟文件夹/ZIP 上传，看到 inventory、batch、status、report。
Slice: upload-batch-mock-loop
Scope:
- 文件夹/ZIP 上传 mock 入口。
- 文件清单、支持/不支持文件数量。
- 文件树。
- 创建 batch。
- 批处理状态、进度、报告。
- 展示转换失败、需要 OCR、低置信度、需审核、已批准、已发布等状态。
Exclusions:
- 不读取真实文件内容。
- 不调用真实 parser/converter。
- 不接入真实对象存储或数据库写入。
Sources:
- ROADMAP.zh-CN.md Phase C
- docs/03-spec/folder-upload-spec.md / .zh-CN.md
- docs/05-design/folder-upload-design.md / .zh-CN.md
- docs/06-tasks/folder-upload-tasks.md / .zh-CN.md
- docs/00-context/folder-upload-traceability.md / .zh-CN.md
- frontend/src/*
Acceptance:
- 用户可以在真实 Vue 页面模拟上传文件夹或 ZIP。
- 用户可以从 inventory 创建 batch。
- 用户可以看到 batch 状态、文件树和报告。
- 状态能自然流入 Processing Center 的后续工作流。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 Playwright E2E：upload -> inventory -> create batch -> report。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- 仅 mock 数据，不读取真实公司文档。
- 不新增外部依赖，除非 SDD 明确批准。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase C 状态与成熟度。
```

## Phase D 提示词：处理中心

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase D「处理中心」：把审核和数据质量门禁做成真实 Vue 产品工作台。
Slice: processing-center
Scope:
- 总文档数、解析失败、需要 OCR、低置信度、缺少 source_trace、LLM 生成需审核、待发布等指标。
- 队列列表展示来源、类型、数量、状态和 mock-safe 操作。
- 用户能看出为什么某个文档/chunk 不能进入 Wiki、Graph 或 Ask。
- 与 Phase C 的 batch/mock 状态保持一致。
Exclusions:
- 不做真实人工审核账号系统。
- 不做真实 remediation worker。
Sources:
- ROADMAP.zh-CN.md Phase D
- docs/03-spec/review-publish-spec.md / .zh-CN.md
- docs/05-design/review-publish-design.md / .zh-CN.md
- docs/06-tasks/review-publish-tasks.md / .zh-CN.md
- frontend/src/*
Acceptance:
- Processing Center 是真实 Vue 标签页/页面。
- 各队列 count、原因、状态和 mock 操作清晰可见。
- 页面能解释内容进入 Wiki/Graph/Ask 的阻塞原因。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 Playwright E2E：进入 Processing Center，检查各类队列和阻塞原因。
- 后端被改动时运行 cd backend && mvn verify。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 必须保留 source trace、confidence、review status 语义。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase D 状态与成熟度。
```

## Phase E 提示词：LM Wiki

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase E「LM Wiki」：让审核通过的知识在真实 Vue 中成为可浏览、信息密度高、可溯源的 Wiki。
Slice: lm-wiki-product-depth
Scope:
- Wiki 页面列表、索引页、Markdown 内容区。
- 来源溯源块、置信度、审核状态、页面元数据。
- 概念/实体链接。
- 与 Processing Center 的 publish/eligibility 状态保持一致。
Exclusions:
- 不接入真实 Markdown 生成器。
- 不接入真实文档内容。
Sources:
- ROADMAP.zh-CN.md Phase E
- docs/03-spec/review-publish-spec.md / .zh-CN.md
- docs/05-design/review-publish-design.md / .zh-CN.md
- docs/03-spec/full-stack-productization-spec.md / .zh-CN.md
- frontend/src/*
Acceptance:
- Wiki 看起来像企业知识库，而不是营销页或占位卡片。
- 用户能看到内容、来源溯源、审核状态、置信度。
- Wiki 页面可以作为后续 API-backed 切换的稳定产品界面。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 E2E：进入 Wiki、切换页面、查看 source trace 和 review status。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 任何 LLM 生成内容在未审核前必须显示 review-required。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase E 状态与成熟度。
```

## Phase F 提示词：知识图谱

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase F「知识图谱」：在真实 Vue 知识空间详情中提供可解释、可溯源的图谱体验。
Slice: knowledge-graph-product-depth
Scope:
- 图谱画布。
- 节点类型：Wiki Page、Entity、Concept、Document、Review Required。
- 节点点击/悬停与详情面板。
- 证据/source trace 展示。
- 搜索、图例、基础过滤。
Exclusions:
- 不做大规模生产图布局优化。
- 不接入真实图数据库，除非当前 SDD 已接受并已有安全 API 契约。
Sources:
- ROADMAP.zh-CN.md Phase F
- docs/03-spec/knowledge-graph-spec.md / .zh-CN.md
- docs/05-design/knowledge-graph-design.md / .zh-CN.md
- docs/06-tasks/knowledge-graph-tasks.md / .zh-CN.md
- frontend/src/*
Acceptance:
- Graph 是知识空间详情的一部分，不是旁路 workbench。
- 用户能点击节点查看它为什么存在，以及对应证据。
- Review Required 节点能表达未审核内容的边界。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 E2E：进入 Graph、选择节点、查看 evidence/source trace。
- 后端被改动时运行 cd backend && mvn verify。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 图谱必须保留 source trace、confidence、review status。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase F 状态与成熟度。
```

## Phase G 提示词：可信问答

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase G「可信问答」：把基于来源的 Ask 做成真实 Vue 产品界面。
Slice: trusted-ask-product-depth
Scope:
- 全局 Chat。
- 多知识空间上下文选择。
- 问题输入、模型选择器、答案区域。
- evidence citations。
- 审核感知的 eligibility 规则展示。
- 无证据/证据不足时的可信拒答状态。
Exclusions:
- 不调用真实外部模型。
- 不做生产 RAG 质量优化。
- 不保存 raw prompt、raw source text、raw vector 或 provider payload。
Sources:
- ROADMAP.zh-CN.md Phase G
- docs/03-spec/ask-rag-spec.md / .zh-CN.md
- docs/05-design/ask-rag-design.md / .zh-CN.md
- docs/06-tasks/ask-rag-tasks.md / .zh-CN.md
- frontend/src/*
Acceptance:
- Ask 在真实 Vue 产品页可用。
- 答案只使用已审核、已发布或明确可溯源内容。
- 低置信度、未审核、缺少 source trace 的内容被排除或明确标注。
- 答案包含证据引用。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 E2E：Ask 成功回答、无证据拒答、review-required warning。
- 后端被改动时运行 cd backend && mvn verify。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- mock/sample 数据 only。
- 模型相关行为必须经过 model adapter 边界。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase G 状态与成熟度。
```

## Phase H 提示词：设置与管理

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase H「设置与管理」：提供企业级管理界面的真实 Vue 结构，尤其修正模型管理的新增、编辑、API Key 更换、保存/取消体验。
Slice: settings-administration
Scope:
- 常规设置、成员管理、注册策略、模型管理、API 信息、向量数据库引擎、解析引擎、存储引擎。
- 设置以应用级浮层或产品级管理页打开。
- 模型管理支持 list、add、edit、save、cancel、masked API key state、replace/remove/test connection 的 mock-safe 行为。
- 其他设置面板要有真实 Vue 结构，并标明 mock/production 边界。
Exclusions:
- 不存储明文 API key。
- 不实现生产密钥管理器。
- 不实现真实 RBAC。
Sources:
- ROADMAP.zh-CN.md Phase H
- docs/03-spec/model-adapter-spec.md / .zh-CN.md
- docs/05-design/model-adapter-design.md / .zh-CN.md
- docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md / .zh-CN.md
- frontend/src/*
Acceptance:
- 设置入口从真实 Vue 产品壳打开。
- 模型管理新增、编辑、更换 API Key、取消、保存都有可见状态变化。
- API Key 只显示 configured/not configured/masked 状态，不显示明文。
- 其它管理页不是空壳，至少具备合理企业产品结构。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 增加或更新 E2E：打开设置、进入模型管理、新增模型、编辑模型、更换 API Key、取消、保存。
- 后端被改动时运行 cd backend && mvn verify。
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据。
Constraints:
- secret write-only 或状态化，不能进前端持久明文。
- model/vector/parser/storage 都必须保持 adapter boundary。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase H 状态与成熟度。
```

## Phase I 提示词：API-backed Vue 切换

Phase I 不要一次做完。按 I1-I7 执行，每个子目标都要独立验证。

| 子目标 | Surface | 目标 |
|---|---|---|
| I1 | Knowledge Space metadata | 首页和详情元数据接 API。 |
| I2 | Batch/file/chunk metadata | 上传批次、文件、chunk 状态接 API。 |
| I3 | Review queues | Processing Center 审核队列接 API。 |
| I4 | Wiki pages | Wiki 页面和发布状态接 API。 |
| I5 | Graph evidence | 图谱节点、边、证据接 API。 |
| I6 | Ask runs and citations | Ask 运行、答案、引用接 API。 |
| I7 | Model configuration metadata | 模型配置元数据和 masked secret state 接 API。 |

复制下面模板时，把 `<Ix>` 和 `<surface>` 替换为当前子目标。

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase I <Ix>「<surface> API-backed Vue 切换」：把对应真实 Vue 产品界面从 mock-only 数据切换到 Atlas 后端 API，同时保留 mock/sample 安全数据和稳定验收。
Slice: api-backed-vue-cutover-<Ix>
Scope:
- 只切换 <surface> 对应的真实 Vue 产品界面。
- 使用现有 backend API contract；若 contract 缺失或不满足产品界面，先更新中英文 API guide/spec/tasks，再实现。
- 前端 API client 使用 typed ApiEnvelope / typed domain model。
- 保留 loading、empty、error、safe fallback 状态。
Exclusions:
- 不扩大到其他 surface。
- 不接入真实公司数据。
- 不引入生产 auth/RBAC/secret manager，除非当前子目标 SDD 明确批准。
Sources:
- ROADMAP.zh-CN.md Phase I
- docs/03-spec/full-stack-productization-spec.md / .zh-CN.md
- docs/05-design/contracts/full-stack-productization-API_IMPLEMENTATION_GUIDE.md / .zh-CN.md
- 当前 <surface> 对应 spec/design/tasks/API guide
- frontend/src/api.ts
- frontend/src/types.ts
- frontend/src/App.vue
- backend/src/*
Acceptance:
- <surface> 的真实 Vue 产品界面调用 Atlas API，而不是只读本地 mock。
- API 响应仍只包含 mock/sample-safe 数据。
- 前端错误态不泄露 stack trace、secret、private path 或 provider internals。
- 现有 mock/E2E 流程仍通过。
Verification:
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- 针对 <surface> 增加或更新 Playwright E2E。
- cd backend && mvn verify
- git diff --check
- 扫描新增外部网络、secret、私有路径、真实数据、adapter boundary 泄漏。
Constraints:
- 每次只做一个 I 子目标。
- API-backed 不等于生产化；成熟度最多到 L3，除非内部 beta 验收另行通过。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase I 子目标状态与成熟度。
```

## Phase J 提示词：内部 Beta 加固准备

```text
请设置并执行一个 Codex goal。

Goal: 完成 Atlas Knowledge Hub Phase J「内部 Beta 加固准备」：把产品从 demo/API-backed 状态推进到可受控内部试用的准备状态，并明确仍缺哪些生产安全能力。
Slice: internal-beta-hardening-readiness
Scope:
- 梳理受控内部试用所需的 runtime adapter 配置、RBAC、审计日志、错误恢复、大批量性能、secret handling、部署和监控。
- 在不使用真实公司资料和真实密钥的前提下，补齐必要的 readiness 文档、配置样例、测试/检查清单。
- 如果仓库已有对应实现层，补齐安全测试、API contract、E2E smoke 和运维文档。
Exclusions:
- 未经用户明确提供并批准，不使用真实内部文档包。
- 未经用户明确批准，不进行外部 provider 调用。
- 不提交真实 secret、真实日志、真实截图、真实公司数据。
Sources:
- ROADMAP.zh-CN.md Phase J
- README.md
- PROJECT_RULES.md
- DEVELOPMENT_STANDARDS.md
- docs/07-acceptance/knowledge-loop-e2e.md
- backend/SECURITY.md
- configs/atlas.company.example.env（若存在）
Acceptance:
- 形成内部 Beta readiness 报告或文档。
- 明确哪些能力已经 L4-ready，哪些仍停留 L3 或更低。
- 所有示例仍是 mock/sample-safe。
- 若有代码改动，相关测试通过。
Verification:
- git diff --check
- cd frontend && npm run typecheck && npm run test && npm run build
- cd backend && mvn verify（如 backend 被触碰，或 readiness 涉及 API/security）
- npm run e2e:second-layer（若本地环境允许；不能运行时必须说明原因）
- secret/private-path/real-data scan。
Constraints:
- Phase J 是内部试用准备，不等于生产上线。
- 真实数据、真实密钥、真实 provider 调用必须由用户单独明确授权。
- 完成后更新 ROADMAP.md / ROADMAP.zh-CN.md 的 Phase J 状态与成熟度。
```

## Final 提示词：统一验收证据整理

你最后可以亲自验收；也可以先让 Codex 做一轮只读验收证据整理。这个 goal 默认不改产品代码，除非你明确要求它修复问题。

```text
请设置并执行一个 Codex goal。

Goal: 对 Atlas Knowledge Hub A-J 阶段做统一验收证据整理，输出产品完成度、缺口、风险和验收建议。默认只读审查，不修代码。
Slice: final-product-acceptance-readiness
Scope:
- 对照 ROADMAP.md / ROADMAP.zh-CN.md 审查 Phase A-J。
- 检查真实 Vue 产品页是否覆盖首页、空间详情、上传/批次、处理中心、Wiki、图谱、Ask、设置管理。
- 检查哪些 surface 达到 L2 Vue parity、哪些达到 L3 API-backed、哪些只是 L1 prototype。
- 运行可用的验证命令并整理证据。
- 生成验收报告，建议路径：docs/07-acceptance/product-acceptance-report.zh-CN.md。
Exclusions:
- 默认不修改产品代码。
- 默认不接入真实公司数据、真实密钥或外部 provider。
Sources:
- ROADMAP.md
- ROADMAP.zh-CN.md
- docs/00-context/product-goal-execution-plan.zh-CN.md
- docs/07-acceptance/knowledge-loop-e2e.md
- frontend/tests/e2e/*
- backend/src/*
- frontend/src/*
Acceptance:
- 报告列出每个 Phase 的实际成熟度、证据、缺口和建议。
- 报告区分 task done、prototype done、Vue parity、API-backed、internal beta-ready。
- 真实 Vue 页面截图或 E2E 结果作为证据。
- 发现的问题按 P0/P1/P2 排序。
Verification:
- git diff --check
- cd frontend && npm run typecheck
- cd frontend && npm run test
- cd frontend && npm run build
- cd frontend && npm run e2e
- cd backend && mvn verify
- npm run e2e:second-layer（若本地环境允许；不能运行时说明原因）
- secret/private-path/real-data scan。
Constraints:
- 这是验收准备，不是自动宣布通过。
- 最终是否接受由用户决定。
- 任何未运行的检查必须明确标注 skipped/blocked 和原因。
```

## 阶段完成判定

| 阶段 | 最低可接受成熟度 | 不接受的完成口径 |
|---|---|---|
| A | L2 Vue parity | 只改原型 iframe 或 workbench。 |
| B | L2 Vue parity | 卡片点进去仍是不可编辑/不可交互占位。 |
| C | L2 Vue parity | 只有按钮，没有 inventory -> batch -> report 闭环。 |
| D | L2 Vue parity | 只有统计卡，没有阻塞原因和队列。 |
| E | L2 Vue parity | Wiki 只是营销式空页面或 lorem ipsum。 |
| F | L2 Vue parity | 图谱在旁路调试页，不在空间详情中。 |
| G | L2 Vue parity | Ask 没有 evidence 或 review-aware 规则。 |
| H | L2 Vue parity | 模型管理新增/编辑/API key 更换不可用。 |
| I | L3 API-backed | 只创建 API client 但真实产品页仍读本地 mock。 |
| J | L4 readiness | 把 readiness 文档误写成生产已完成。 |

## 复盘防线

这份计划专门防止三类重复问题：

- 把静态原型、workbench、iframe 当成真实 Vue 产品页。
- 把 task done 当成 product accepted。
- 把 sample 截图当成局部样式参考，而不是完整交互链验收依据。

每个阶段失败或验收不一致时，Codex 必须更新 `docs/00-context/lessons-learned.md`，并同步更新对应 prevention artifact：spec、design、tasks、standards、roadmap 或测试。
