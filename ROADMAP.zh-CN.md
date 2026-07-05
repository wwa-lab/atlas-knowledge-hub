# Atlas Knowledge Hub 产品路线图

本路线图是 Atlas Knowledge Hub 的产品执行计划，目标是把当前仓库推进成面向公司内部使用的企业级知识库管理产品，而不是只停留在脚本、静态原型或局部调试页。

英文版本：[ROADMAP.md](ROADMAP.md)。

分阶段 Codex goal 执行提示词：[docs/00-context/product-goal-execution-plan.zh-CN.md](docs/00-context/product-goal-execution-plan.zh-CN.md)。

WeKnora 只能作为产品体验、信息架构和工作流模式的参考。Atlas 不复制 WeKnora 的源码、项目结构、UI 资产、专有样式、图标或实现细节。Atlas 要按照本仓库自己的产品架构独立实现：文件夹/ZIP 上传、批处理解析、来源溯源、SME 审核、LM Wiki、知识图谱、可信问答，以及基于适配器的基础设施边界。

## 产品目标

Atlas 应成为一个真实的 Vue 3 + Spring Boot 内部产品，用于管理企业知识空间：

- 创建和浏览知识空间。
- 上传文件夹或 ZIP 包。
- 通过内部适配器转换、解析源文档。
- 将解析内容标准化为带来源溯源的 Markdown / LM Wiki 页面。
- 对低置信度、失败或 LLM 生成内容进入 SME 审核。
- 将审核通过的知识发布到 Wiki、知识图谱和可信问答。
- 管理成员、注册策略、模型、解析引擎、向量引擎和存储引擎。
- 所有模型、工具和基础设施集成都必须藏在产品侧适配器之后。
- 前端代码和测试夹具不得暴露真实密钥、私有路径或公司机密数据。

## 当前状态快照

截至 2026-07-05，仓库已经有较多架构文档、SDD 文档、mock 实现和验收脚手架，但还不是一个生产可用的企业知识库管理系统。

| 领域 | 当前状态 | 产品成熟度 |
|---|---|---|
| 静态原型 | 已覆盖首页、空间详情、文档、处理中心、Wiki、图谱、Ask、设置、模型管理。 | Demo 参考价值高。 |
| Vue 3 产品外壳 | 真实 Vue 产品壳已经开始，但只具备部分产品体验对齐；近期才开始从 iframe/原型依赖中迁移出来。 | 早期到中期。 |
| 后端元数据/API | Spring Boot 元数据和契约式 API 覆盖多个切片。 | 中期，但仍偏 demo/control-plane。 |
| 上传/批处理流程 | 有 mock/sample 流程，真实企业文件夹摄取尚未完成。 | 早期。 |
| 转换/解析适配器 | 有适配器契约和 mock-engine 验证，真实内部 runtime 加固延后。 | 架构中期，运行成熟度早期。 |
| 审核/发布 | 有审核状态和发布概念，并有测试。 | 中期。 |
| Wiki | 有 mock/API-backed 发布概念，但完整 Vue Wiki 产品体验不够。 | 早期到中期。 |
| 知识图谱 | 有图谱契约和 demo 表面，生产级布局、规模和证据交互仍不足。 | 早期到中期。 |
| 可信问答 | 有审核感知的 Ask mock/API 流程，检索质量和治理还未生产化。 | 早期到中期。 |
| 模型管理 | 原型和部分 Vue 设置交互已存在，真实后端持久化、密钥处理和连接测试未完成。 | 早期。 |
| Auth/RBAC/审计 | 按项目规则大多延后。 | 未达到生产级。 |
| 部署/运维 | 尚未成为独立产品轨道。 | 未开始。 |

## 为什么旧 Roadmap 看起来做完了，产品仍然没达到目标

之前的路线图把不同层级的“完成”混在了一起：

- SDD 文档集已生成。
- 后端契约已实现。
- mock 适配器测试通过。
- 静态 HTML 原型存在某个行为。
- Vue 宿主页有一个局部面板。
- 真实用户会进入的 Vue 产品页已经达到验收体验。

这些不是同一件事。

切片路线图正确追踪了很多工程任务和契约，但没有足够区分“任务完成”和“产品验收”。结果是，一些切片可以被标记为已实现，但真实 Vue 用户体验仍依赖 iframe 原型、旁路工作台或 mock 片段。这会让仓库看起来比真实产品进度更靠前。

修正原则：

- Roadmap 进度必须按产品界面和用户工作流衡量，不能只按 SDD/task 关闭衡量。
- “已实现”必须说明是静态原型、Vue UI、后端 API、适配器契约、API-backed UI，还是生产加固。
- 每个前端切片都必须指出真实用户面对的产品界面。
- 旁路调试页、工作台或宿主页面板不等于产品验收。
- sample/reference 截图代表交互预期，不只是孤立的样式提示。

## 产品成熟度分级

以后路线图状态统一使用这套分级。

| 等级 | 含义 | 验收证据 |
|---|---|---|
| L0 Concept | 只在文档或想法里存在。 | 产品目标和范围已写清。 |
| L1 Prototype | 静态或 mock 原型存在。 | 原型直接评审通过。 |
| L2 Vue Parity | 真实 Vue 组件达到已接受原型的行为。 | Vue 截图和交互对齐清单通过，主路径无 iframe 依赖。 |
| L3 API-backed | Vue 页面连接后端 API，使用安全 mock/sample 数据。 | API 契约测试、Vue 测试和 E2E 覆盖真实产品界面。 |
| L4 Internal Beta | 端到端流程能在受控内部环境运行。 | 上传到 Ask 的闭环在内部环境跑通。 |
| L5 Production-ready | 安全、RBAC、审计、运维、规模和治理达标。 | 部署、监控、安全、性能和回滚检查通过。 |

## 修正后的 Roadmap

### Phase A：产品体验重置

目标：让真实 Vue 应用成为产品入口，而不是 iframe 原型或单一工具页。

范围：

- 真实 Vue 产品外壳。
- 持久侧边栏。
- 知识空间库首页。
- 全局 Chat 入口。
- 设置浮层。
- 原型 iframe 只作为参考/调试入口。
- 与已接受原型进行视觉和交互对齐检查。

验收：

- 打开 Vite 应用默认看到真实 Vue 产品页。
- `iframe.product-frame` 不属于主产品路径。
- 首页、侧边栏导航、设置浮层、知识空间卡片符合已接受的产品方向。
- 截图经过原型基线对照。

状态：Batch 1 checkpoint 已达到 L2 Vue parity。真实 Vue 产品外壳已成为默认产品路径，截图和 E2E 证据记录在 `docs/00-context/evidence/`。这不代表最终产品验收通过。

### Phase B：知识空间详情页对齐

目标：用 Vue 重建已接受的知识空间详情体验。

范围：

- `IBM i Modernization` 详情页。
- 面包屑和返回首页。
- 上传文件夹 / 上传 ZIP 操作。
- 标签页：Documents、Processing Center、Wiki、Graph。
- 默认标签页：Wiki。
- 标签切换保留状态。

验收：

- 点击 IBM i 卡片进入真实 Vue 知识空间详情页。
- Documents、Processing Center、Wiki、Graph 都是真实 Vue 视图。
- 布局和交互足够接近原型基线，可用于干系人 demo。

状态：Batch 1 checkpoint 已达到 L2 Vue parity。真实 Vue `IBM i Modernization` 详情页可从首页卡片进入，默认 Wiki，并支持 Documents、Processing Center、Wiki、Graph 标签切换，已有截图和 E2E 证据。这不代表最终产品验收通过。

### Phase C：上传到批处理 mock 闭环

目标：在真实解析接入前，让产品入口流程可理解、可测试。

范围：

- 文件夹/ZIP 上传 mock。
- 文件清单。
- 支持/不支持文件数量。
- 批次创建。
- 文件树。
- 批次报告。
- 解析/转换状态映射。

验收：

- 用户可以模拟上传文件夹或 ZIP 包。
- 用户能看到清单、创建批次、查看状态和报告。
- 能看到转换失败、需要 OCR、低置信度、需审核、已批准、已发布等状态。

状态：Batch 2 checkpoint 已达到 L2 Vue parity。真实 Vue 知识空间详情页已支持 mock 上传审阅、清单、创建批次、状态映射、source trace 和批次报告证据。这不代表最终产品验收通过。

### Phase D：处理中心

目标：把审核和数据质量门禁做成企业级工作台。

范围：

- 文档总数。
- 解析失败。
- 需要 OCR。
- 低置信度。
- 缺少 `source_trace`。
- LLM 生成内容需审核。
- 待发布。
- 队列行展示来源、类型、数量、状态和 mock 操作。

验收：

- 用户能看到什么阻塞 Wiki、图谱和 Ask 的可用性。
- 处理中心能解释某个文档或 chunk 为什么被排除。
- 在真实修复能力接入前，操作保持 mock-safe。

状态：Batch 2 checkpoint 已达到 L2 Vue parity。真实 Vue 处理中心已展示质量门禁指标，并用队列解释 Wiki、Graph、Ask 的可用性阻塞原因。这不代表最终产品验收通过。

### Phase E：LM Wiki

目标：让审核通过的知识可浏览、信息密度高、可溯源。

范围：

- Wiki 页面列表。
- 索引页。
- Markdown 内容区。
- 来源溯源块。
- 置信度和审核状态。
- 概念/实体链接。
- 页面元数据。

验收：

- Wiki 看起来像生成出来的企业知识库，而不是营销页。
- 关键内容展示来源溯源和审核状态。
- Wiki 页面后续可平滑接入 API，无需重做设计。

状态：Batch 3 checkpoint 已达到 L2 Vue parity。真实 Vue Wiki 标签页已提供可浏览页面索引、Markdown-like 内容、来源溯源、置信度、审核状态、实体链接和页面 metadata。这不代表最终产品验收通过。

### Phase F：知识图谱

目标：围绕已批准知识资产提供可解释图谱。

范围：

- 图谱画布。
- 节点类型：Wiki Page、Entity、Concept、Document、Review Required。
- 节点点击/悬停。
- 详情面板。
- 证据和来源溯源。
- 搜索和图例。

验收：

- 用户能查看节点或关系为什么存在。
- 图谱保持来源溯源感知。
- 图谱属于知识空间详情体验，而不是旁路工作台。

状态：Batch 3 checkpoint 已达到 L2 Vue parity。真实 Vue 图谱标签页已提供 source-trace-aware 画布、类型化节点、搜索、图例、节点详情、置信度、审核状态和证据。这不代表最终产品验收通过。

### Phase G：可信问答

目标：把基于来源的 Ask 做成工作区级产品界面。

范围：

- 全局 Chat。
- 多知识空间上下文选择。
- 问题输入。
- 模型选择器。
- 答案区。
- 证据引用。
- 审核感知的可用性规则。

验收：

- Ask 只使用已审核、已发布或明确可溯源的内容。
- 解析失败、未审核低置信度和缺少来源溯源的内容被排除。
- 答案包含证据引用。

状态：Batch 3 checkpoint 已达到 L2 Vue parity。真实 Vue 全局 Chat 表面已提供上下文选择、问题输入、模型选择器、证据引用、证据不足拒答和 review-required warning 状态。这不代表最终产品验收通过。

### Phase H：设置与管理

目标：提供企业管理界面，同时不假装生产安全能力已经完成。

范围：

- 常规设置。
- 成员管理。
- 注册策略。
- 模型管理。
- API 信息。
- 向量数据库引擎。
- 解析引擎。
- 存储引擎。

验收：

- 设置以应用级浮层打开。
- 模型管理支持列表、新增、编辑、保存/取消、API Key 脱敏状态。
- 其他设置面板有真实 Vue 结构，并清楚区分 mock 与生产边界。
- 不存储或展示明文密钥。

状态：Batch 4 checkpoint 已达到 L2 Vue parity。真实 Vue 设置浮层已包含非空的常规、成员、注册、API、模型、向量、解析和存储管理面板。模型管理支持 list、add、edit、cancel、save、masked API key replace/remove，以及 mock-safe 测试连接反馈。这不代表最终产品验收通过。

### Phase I：Vue 接入 API

目标：把 mock-only Vue 页面逐步替换为后端 API 数据。

优先级：

1. 知识空间列表/详情元数据。
2. 批次/文件/chunk 元数据。
3. 审核队列。
4. Wiki 页面。
5. 图谱节点/边/证据。
6. Ask 运行和引用。
7. 模型配置元数据。

验收：

- 真实 Vue 产品界面调用 Atlas API。
- 现有 API 契约保持稳定。
- mock/sample 数据仍安全、确定。
- 测试覆盖真实产品界面。

状态：Batch 6 checkpoint 已完成优先级 1-7 的 L3 API-backed。真实 Vue 产品路径现在消费 Atlas API 的 Knowledge Space 列表/详情元数据、批次/文件/chunk 元数据、审核队列、Wiki pages、Graph evidence、Ask runs/citations 与 masked model capability metadata。Phase J hardening 仍待执行。这不代表最终产品验收通过。

### Phase J：内部 Beta 加固

目标：从 demo 进入受控内部试用。

范围：

- 真实内部文档包。
- runtime 适配器配置。
- RBAC。
- 审计日志。
- 错误恢复。
- 大批量性能。
- 密钥处理。
- 部署和监控。

验收：

- 受控用户可以完成：上传包 -> 批处理 -> 处理中心 -> Wiki -> 图谱 -> 可信问答。
- 敏感数据和密钥得到保护。
- 运维团队可以部署、监控和回滚。

状态：Batch 7 checkpoint 已完成 mock/sample-safe 内部 Beta 规划的 L4 readiness preparation。Readiness 报告已识别受控试用准入条件、安全配置模板、L4-ready 区域、L3/更低缺口，以及残留 P0/P1/P2 风险。Batch 8 Final 验收证据整理已完成，见 `docs/07-acceptance/product-acceptance-report.zh-CN.md`；Atlas 已可进入用户验收评审，但这不代表生产就绪或最终产品验收通过。

## 当前下一阶段 Wave 进度

2026-07-05 状态：

- Wave 1 / `wiki-foundation` / `wiki-data-model`：已作为 Wiki Foundation 数据模型切片完成。Atlas 现在具备 additive `wiki_page` metadata 字段、最小 folder/run/log/issue 读取模型、后端读取 APIs、contract/repository 覆盖，以及 Vue Wiki metadata 展示。
- 成熟度说明：这仍只是 Wiki Foundation 数据底座，不等于 Auto Wiki ingest 完成，也不代表生产就绪。
- Wave 1 / `wiki-ingest-v0`：Implementation 已作为 Auto Wiki ingest v0 foundation 完成验证：deterministic review-required candidates、safe run/log/issue evidence、显式 draft listing 和完整本地验证。本切片不代表 linkify/lint、review-gate、connector、model-assisted generation 或 production readiness。
- 当前 active slice：Wave 1 / `wiki-linkify-lint`。完整双语 SDD 草稿已生成，范围包括 deterministic Wiki link insertion、link metadata refresh、lint issue recording，以及 Processing Center/Wiki warnings。产品代码尚未开始；实现仍需用户先接受 spec、API guide 和 tasks。

## 近期执行顺序

按这个顺序推进：

1. **Vue 产品外壳对齐**
   - 真实 Vue 首页、侧边栏、知识空间卡片、全局 Chat、设置浮层。
   - 主路径不依赖 iframe。
   - 对照原型截图验收。

2. **知识空间详情页对齐**
   - 真实 Vue 空间详情。
   - Documents、Processing Center、Wiki、Graph 标签页。
   - 保留状态和布局。

3. **上传到 Wiki mock 闭环**
   - 文件夹/ZIP mock 上传。
   - inventory -> batch -> status -> Processing Center -> Wiki。

4. **Wiki/Graph/Ask 产品深度**
   - 让三个消费界面在 Vue 中具备可溯源、可 demo 的产品深度。

5. **API-backed 切换**
   - 按工作流逐个把真实 Vue 界面接到已有后端契约。

## Roadmap 规则

- 不要把路线图项标成 product-complete，除非真实用户面对的界面已经完成。
- 必须记录成熟度等级：L1 prototype、L2 Vue parity、L3 API-backed、L4 internal beta、L5 production。
- `docs/00-context/slice-roadmap.md` 用于 SDD/task 执行状态，不作为唯一产品真相源。
- 本 `ROADMAP.zh-CN.md` 和英文 `ROADMAP.md` 聚焦产品成熟度和用户可见结果。
- 每次路线图收尾必须包含真实 Vue 界面的截图或 E2E 证据。
- 旁路工作台和调试面板不计入产品完成。
- 前端界面必须先达到原型对齐，再扩展 API-backed 能力。
