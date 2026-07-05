# 需求：Knowledge Space

## 状态

草稿。此中文 companion 补齐当前 `knowledge-space` 历史切片的双语门禁，并覆盖 Product Goal Batch 1 的 Phase A「产品体验重置」与 Phase B「知识空间详情页对齐」。

## 背景

`knowledge-space` 切片把 Atlas 从静态原型推进为真实 Vue 产品界面。它覆盖知识空间首页、全局 Chat、`IBM i Modernization` 详情页、文档、处理中心、Wiki、图谱、审核与 Ask 的产品入口。

当前 Batch 1 的实现仍限定在 mock/sample 数据与真实 Vue 产品页；不得把 iframe、静态原型、workbench 或 debug panel 当成主产品验收路径。

## 目标

- 让真实 Vue 应用成为默认产品入口。
- 保留知识空间管理模型和持久侧边栏。
- 让 Global Chat 成为工作区级入口，而不是单个知识空间内的标签页。
- 在真实 Vue 详情页中展示 Documents、Processing Center、Wiki、Graph。
- 在 Wiki、Graph、Processing Center、Global Chat 中持续暴露 source trace、confidence 和 review status。
- 为后续 Vue API-backed cutover、Spring Boot、PostgreSQL 和适配器实现保留契约边界。

## 范围内

- 首页知识空间库与知识空间卡片。
- 全局 Chat 入口与多知识空间上下文选择。
- `IBM i Modernization` 知识空间详情页。
- 详情页面包屑、返回首页、上传文件夹 / 上传 ZIP mock 入口。
- 详情页标签：Documents、Processing Center、Wiki、Graph。
- 默认标签：Wiki。
- 设置浮层、模型管理、成员管理、引擎设置入口。
- 仅 mock/sample 数据的状态、指标、报告与证据展示。
- 响应式真实 Vue 产品路径验证。

## 范围外

- 生产后端、生产数据库、生产认证、SSO 或复杂权限系统。
- 真实 OCR、真实 RAG、真实 parser/converter/model/vector/storage 调用。
- 外部云调用或新增外部网络依赖。
- 真实公司文档、截图、凭证、日志、私有路径或机密数据。

## 需求

| ID | 需求 | 来源 | 优先级 |
|---|---|---|---|
| REQ-KS-001 | 首页必须展示知识空间卡片，包含名称、描述、文档数量、Wiki/能力状态、审核数量、所有者和状态。 | 原型请求、`docs/mvp-scope.md` | Must |
| REQ-KS-002 | 首页或工作区必须提供全局对话入口，用户可选择一个或多个知识空间作为上下文提问。 | 产品反馈、现代 AI-native knowledge product 参考 | Must |
| REQ-KS-003 | 选择 `IBM i Modernization` 必须打开真实 Vue 详情页，包含面包屑、返回按钮，以及 Documents、Processing Center、Wiki、Graph 标签。 | Roadmap Phase B | Must |
| REQ-KS-004 | Documents 标签必须展示文件夹/ZIP 上传入口、批次摘要、进度、文件树和解析/转换状态。 | `docs/batch-processing-design.md` | Must |
| REQ-KS-005 | Wiki 标签必须展示 LM Wiki 内容、概念、source trace、confidence、review status、来源文档和元数据。 | `docs/markdown-standard.md` | Must |
| REQ-KS-006 | Graph 标签必须展示可解释图谱、节点类型、计数、节点详情和证据关系。 | `docs/knowledge-graph-design.md` | Must |
| REQ-KS-007 | Processing Center 必须展示解析失败、OCR required、低置信、缺少 source_trace、LLM-generated review required 和 ready-to-publish 队列。 | `docs/review-workflow.md` | Must |
| REQ-KS-008 | Global Chat / Trusted Ask 必须使用来源明确、已审核或可发布内容，并显示 evidence references。 | `docs/product-vision.md` | Must |
| REQ-KS-009 | LLM 生成或不确定内容必须保持 review-required，除非 SME 批准或确定性校验通过。 | `PROJECT_RULES.md` | Must |
| REQ-KS-010 | 静态原型只能作为参考/调试入口；真实 Vue 产品主路径不得依赖 `iframe.product-frame`。 | Roadmap Phase A | Must |
| REQ-KS-011 | 后续 parser/converter/model/vector/storage/search 集成必须经过产品侧适配器。 | `docs/architecture.md` | Must |
| REQ-KS-012 | 数据模型必须分离 raw inputs、generated PDFs、Markdown、assets、reports 和 published Wiki content。 | `PROJECT_RULES.md` | Should |
| REQ-KS-013 | UI 必须支持中英文主要界面文本切换。 | 产品反馈 | Must |
| REQ-KS-014 | UI 必须支持 day/night mode 且保持主要视图可读。 | 产品反馈 | Must |
| REQ-KS-015 | 设置必须包含模型管理，并能区分 chat、embedding、rerank、vision、speech 等类别。 | 产品反馈 | Must |
| REQ-KS-016 | 模型配置不得在前端或原型中保存/展示真实 API key，只能显示脱敏或配置状态。 | `PROJECT_RULES.md` | Must |
| REQ-KS-017 | 设置必须包含 mock 注册配置或注册预览。 | 产品反馈 | Must |
| REQ-KS-018 | 知识空间必须支持 mock 成员管理，包括邀请、角色、成员列表和操作入口。 | 产品反馈 | Must |
| REQ-KS-019 | MVP 角色限定为 Owner、Admin、Reviewer、Viewer，不暗示生产权限已完成。 | `PROJECT_RULES.md` | Must |
| REQ-KS-020 | 设置必须包含常规设置、用户信息和 API 信息，并使用 mock/masked 数据。 | 产品反馈 | Must |
| REQ-KS-021 | 设置必须包含向量数据库、解析引擎和存储引擎配置入口，并保持适配器中立。 | `PROJECT_RULES.md` | Must |
| REQ-KS-022 | 设置必须从持久侧边栏可发现，并以浮层/弹层覆盖当前产品视图。 | Roadmap Phase A/H | Must |
| REQ-KS-023 | 真实 Vue 产品页必须在桌面、笔记本、平板和窄屏下保持可读，不出现明显重叠。 | 质量门禁 | Must |

## 开放问题

- MVP 的 Global Chat 是否默认选择一个还是多个知识空间？
- 低置信但已 source-traced 的内容是否可进入 Ask，还是必须经过 SME？
- 第一批真实 API-backed cutover 是否优先接知识空间元数据、批次状态还是 Wiki 页面？
