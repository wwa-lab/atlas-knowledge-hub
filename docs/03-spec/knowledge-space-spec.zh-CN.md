# 规格：Knowledge Space

## 状态

草稿。此中文 companion 补齐当前 Product Goal Batch 1 所需的双语规格门禁，并与英文 `docs/03-spec/knowledge-space-spec.md` 的当前 Phase 1 IA 基线保持一致。

## 来源文档

- `frontend/public/atlas-prototype.html`
- `prototypes/index.html`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/architecture.md`
- `docs/batch-processing-design.md`
- `docs/markdown-standard.md`
- `docs/knowledge-graph-design.md`
- `docs/review-workflow.md`
- `docs/technology-decisions.md`
- `ROADMAP.md`
- `ROADMAP.zh-CN.md`

## 功能规格

### 首页知识空间库

- 打开 Vite 应用默认显示真实 Vue 产品页，而不是 iframe 原型。
- 首页是知识空间库，不是 dialogue-first hero。
- 首页展示标题、说明、创建入口、`我创建的` filter 和知识空间卡片网格。
- 卡片展示名称、描述、文档数量、轻量能力/状态图标和创建者标记。
- 点击 `IBM i Modernization` 进入真实 Vue 知识空间详情页。
- 主产品路径不得渲染 `iframe.product-frame`。

### Global Chat

- Chat 是工作区级表面，从侧边栏 `对话` 打开。
- Global Chat 支持选择一个或多个知识空间作为回答上下文。
- 选择数量在输入区域附近可见。
- 解析失败、未审核低置信和缺少 source_trace 的内容不得进入 mock Chat 上下文。
- Chat 行为在当前阶段为 visual/mock-only，不调用外部模型、搜索服务或 provider。

### 侧边栏与设置浮层

- 持久侧边栏是主导航与常用设置入口。
- 侧边栏包含知识库、智能体、共享空间、对话、近期对话、设置快捷入口和账户区域。
- 设置以浮层/弹层覆盖当前产品视图，不替换首页或详情页。
- 关闭设置后返回之前的产品视图，并保持当前详情页标签和设置 panel 状态。
- 设置快捷入口和设置左栏包含空间信息、成员管理、消息管理、模型管理、API 信息和数据/扩展引擎入口。
- 模型管理可以展示模型类别、模型卡片和编辑/新增 mock 流程；API key 只能显示 configured/not configured，不展示明文。
- 空间信息展示当前知识空间的 ID、名称、描述、状态、创建时间、存储配额、已使用存储和使用率。
- 空间名称和描述可在当前 Vue mock 会话中编辑，用于原型验收；不得调用真实后端更新、持久化生产数据或绕过未来 RBAC/审计。
- 消息管理用于展示工作区级聊天历史索引设置，包含“启用消息索引”开关、未配置索引统计空态和 Embedding 模型依赖说明。
- 当前阶段的消息索引为 mock-only，不保存真实聊天历史、不调用 embedding provider、不写入真实向量库。

### 知识空间详情页

- 详情页面包屑显示 `知识库 / IBM i Modernization`。
- 详情页包含返回首页、上传文件夹、上传 ZIP 入口。
- 标签页为 `文档`、`处理中心`、`Wiki`、`图谱`。
- 默认标签为 Wiki。
- 标签切换保留当前知识空间上下文。
- Ask 不作为详情页标签出现；对话由 Global Chat 承担。

### Documents

- Documents 标签展示文档分类/文件树和解析状态。
- 状态示例包括 `MARKDOWN_GENERATED`、`REVIEW_REQUIRED`、`PDF_CONVERT_FAILED`。
- 上传文件夹和上传 ZIP 入口为 mock-only，不读取真实文件内容。

### Processing Center

- Processing Center 是批量质量门禁工作台。
- 它展示 total documents、parse failures、OCR required、low confidence、missing source_trace、LLM-generated review required 和 ready-to-publish 等状态。
- issue 行必须解释为什么内容被排除在 Wiki、Graph 或 Ask 外。
- retry、OCR、repair trace、review 等操作当前为 mock-safe。

### Wiki

- Wiki 使用索引/内容布局，默认显示 `IBM i Modernization Index`。
- Wiki 内容必须像生成的企业知识库内容，信息密集并展示 source trace、confidence 和 review status。
- 概念或实体链接可视化为可点击样式，但当前可为 mock 交互。

### Graph

- Graph 是知识空间详情页的一部分，不是旁路 workbench。
- 节点类型包括 Wiki Page、Entity、Concept、Document、Review Required。
- 图谱必须展示节点详情和 evidence/source trace 文案。
- 当前阶段可使用 mock canvas/节点布局，不引入外部图谱库。

## 非功能规格

- 当前阶段使用 Vue 3 + Vite + TypeScript。
- 仅使用 mock/sample 数据。
- 不提交真实公司文档、截图、凭证、日志、私有路径或机密数据。
- 不新增外部云调用或外部网络依赖。
- parser/converter/model/vector/storage/search 后续都必须通过产品侧适配器。
- 响应式布局必须避免明显文本或控件重叠。

## Batch 1 验收矩阵

| Requirement | Acceptance Source |
|---|---|
| REQ-KS-001 | 首页真实 Vue 产品页渲染知识空间库和卡片。 |
| REQ-KS-002 | 侧边栏 Global Chat 可见，可选择多个知识空间并显示选择数量。 |
| REQ-KS-003 | 点击 `IBM i Modernization` 进入真实 Vue 详情页，默认 Wiki 标签。 |
| REQ-KS-004 | Documents 标签显示上传入口、文件树和状态。 |
| REQ-KS-005 | Wiki 标签显示 `IBM i Modernization Index`、source trace、confidence/review 文案。 |
| REQ-KS-006 | Graph 标签显示图谱 canvas、节点和 Source Trace 详情。 |
| REQ-KS-007 | Processing Center 显示质量门禁指标。 |
| REQ-KS-008 | Global Chat 说明 blocked content 不进入对话索引。 |
| REQ-KS-010 | 默认产品路径无 `iframe.product-frame`。 |
| REQ-KS-015/016 | 模型管理浮层可打开，且不展示明文 API key。 |
| REQ-KS-022 | 侧边栏设置入口打开浮层并可关闭回到当前产品路径。 |
| REQ-KS-024 | 设置浮层显示消息管理，可看到消息索引开关、Embedding 模型依赖、未配置统计空态和 mock/adapter 边界说明。 |
| REQ-KS-025 | 设置浮层显示空间信息，可看到当前空间元数据、存储额度/使用量，并支持名称/描述的 mock-only 编辑。 |
