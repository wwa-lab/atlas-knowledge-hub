# Requirement: Atlas Knowledge Hub 产品级需求

## 状态

草稿。

## 文档目的

本文定义 Atlas Knowledge Hub 的产品级需求，作为后续 SDD 拆分、前端组件化、后端 metadata API、文档处理 adapter、Review 工作流、知识图谱和 Ask 能力建设的上游依据。

本文覆盖完整产品方向，不代表所有能力都在当前阶段实现。当前阶段仍以 Phase 1 前端和 mock data 为主；真实后端、数据库、文档解析、模型调用和生产权限能力必须在后续阶段按 SDD 文档继续拆分后再实现。

## 产品愿景

Atlas Knowledge Hub 帮助团队把分散的项目文档、交付材料、设计说明、业务知识和技术资产转换为可信、可追溯、可 Review、可问答的知识空间。

产品核心原则：

- 先让知识可信，再让知识可对话。
- Markdown Wiki 是长期可维护的知识资产。
- Source Trace、confidence、review status 必须贯穿 Wiki、Review、Graph 和 Ask。
- 任何 LLM 生成或改写的内容，在 SME 审核或确定性校验前都不能默认视为可信。
- 文档解析、模型、向量库、存储和搜索能力必须通过 adapter 边界接入，不能硬编码某个工具或供应商。

## 当前基线

当前前端视觉和交互基线为：

- `frontend/public/atlas-prototype.html`
- `prototypes/index.html`

`frontend/public/atlas-prototype.html` 是 Phase 1 FE fidelity baseline；`prototypes/index.html` 是直接静态 review 用的镜像。产品需求、spec、design 和 tasks 应以当前 FE 基线为准，而不是回退到旧版对话优先原型。

## 目标用户

| 用户 | 核心诉求 |
|---|---|
| 知识库管理员 | 创建知识空间，管理文档批次、成员、模型和系统配置。 |
| SME Reviewer | 审核解析后的 Markdown，确认术语、业务含义、技术关系和低置信度内容。 |
| Developer | 查看源文件、Source Trace、系统依赖、技术概念和图谱关系。 |
| BA / Analyst | 整理业务流程、需求、术语、规则和交付材料。 |
| Delivery Lead | 跟踪批处理进度、Review 状态、知识库完备度和发布状态。 |
| 普通知识使用者 | 浏览 Wiki、探索图谱，并通过 Ask 获取有来源依据的答案。 |
| 平台管理员 | 管理模型、parser、向量数据库、存储、API 信息和注册/成员策略。 |

## 产品范围

### 范围内

- 知识空间创建、浏览和管理。
- 文档包上传入口和批次状态展示。
- 原始文档、生成 PDF、Markdown、assets、report、Wiki page 的分层管理模型。
- 文档转换、解析、标准化和 source trace 生成的产品流程。
- Markdown Wiki 作为 durable knowledge layer。
- SME Review 队列、低置信度内容处理和 Review history。
- 知识图谱，用于展示概念、实体、文档、Wiki 页面和来源 chunk 的关系。
- Ask 界面，用于基于已审核知识和来源引用回答问题。
- 模型管理、向量数据库、解析引擎、存储引擎等配置入口。
- 成员管理、注册配置、用户信息、API 信息和常规设置。
- 中文/英文切换和日间/夜间模式。
- 响应式前端体验。
- SDD 驱动的需求、故事、spec、architecture、design、tasks 和 verification。

### 当前阶段范围外

- 生产级后端。
- 生产级数据库。
- 真实认证、SSO、复杂 RBAC 权限执行。
- 真实 OCR、真实 RAG、真实模型调用。
- 外部云调用、外部 CDN、外部网络依赖。
- 真实公司文档、凭证、日志、机密截图或私有路径。
- 生产级 secret 管理。

这些能力可以进入长期产品路线，但不得在未完成相应 SDD、架构、安全和验收设计前直接实现。

## 产品阶段

| 阶段 | 目标 | 主要交付 |
|---|---|---|
| Phase 0 | 静态原型和产品形态验证 | HTML 原型、mock 数据、核心流程演示、基础 SDD 文档。 |
| Phase 1 | 前端组件化和 mock app | Vue 3 + Vite + TypeScript、组件化 UI、typed mock data、E2E smoke test。 |
| Phase 2 | 后端 metadata API 和持久化 | Spring Boot API、PostgreSQL/Flyway、知识空间/批次/文件/Review/Wiki metadata。 |
| Phase 3 | Adapter 和处理流水线 | converter/parser adapter、Markdown normalizer、source trace、review queue、graph projection。 |
| Phase 4 | 生产化与治理 | 认证/RBAC、secret 管理、审计、部署、监控、权限与组织策略。 |

## 产品需求

### 1. 知识空间

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-001 | 系统必须支持 Knowledge Space 作为项目、领域或交付流的知识容器。 | Must | Phase 1 |
| REQ-PROD-002 | 首页必须以知识库卡片列表作为当前主入口，而不是旧版对话优先首页。 | Must | Phase 1 |
| REQ-PROD-003 | 知识库卡片必须展示名称、描述、文档数量、能力/状态图标和创建者标识。 | Must | Phase 1 |
| REQ-PROD-004 | 用户必须能够从首页进入指定知识空间详情页。 | Must | Phase 1 |
| REQ-PROD-005 | 系统必须支持创建知识空间的产品流程，包括类型、索引策略、名称和描述。 | Must | Phase 1 |
| REQ-PROD-006 | 新建知识库流程必须支持文档型和问答/FAQ 型选择。 | Must | Phase 1 |
| REQ-PROD-007 | 新建知识库流程必须支持 RAG 检索和 Wiki 知识库索引策略选择。 | Must | Phase 1 |
| REQ-PROD-008 | 未来后端必须持久化 Knowledge Space metadata，包括 owner、created time、updated time、status 和默认配置。 | Must | Phase 2 |

### 2. 文档批处理

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-009 | 系统必须支持文档包作为 batch 进行组织和追踪。 | Must | Phase 1 |
| REQ-PROD-010 | UI 必须展示 upload folder 和 upload ZIP 的产品入口。 | Must | Phase 1 |
| REQ-PROD-011 | Batch 必须展示总文件数、转换成功数、Markdown 生成数、Review required 数和失败数。 | Must | Phase 1 |
| REQ-PROD-012 | 每个 file item 必须有状态，包括 uploaded、PDF converted、Markdown generated、OCR required、low confidence、review required、approved、published、failed、unsupported。 | Must | Phase 2 |
| REQ-PROD-013 | 系统必须生成处理报告，包括文件清单、unsupported files、转换失败、低置信度、Review required、approved 和 published summary。 | Should | Phase 3 |
| REQ-PROD-014 | 原始输入、生成 PDF、Markdown、assets、reports、published Wiki 必须分层存储，不得混放。 | Must | Phase 2 |

### 3. Converter / Parser Adapter

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-015 | 产品工作流不得直接调用单一 parser 或 converter。 | Must | Phase 3 |
| REQ-PROD-016 | `trinity-office` 和 `document-normalize` 必须作为内部工具被 adapter 包装。 | Must | Phase 3 |
| REQ-PROD-017 | Adapter 边界必须允许未来替换或新增 MinerU、Docling、PaddleOCR、内部 OCR 或其他解析引擎。 | Must | Phase 3 |
| REQ-PROD-018 | Converter/parser 执行结果必须输出可追踪的 metadata，包括 adapter name、source file、page/section/chunk、confidence 和 error。 | Must | Phase 3 |
| REQ-PROD-019 | 解析失败、文件不支持、OCR required 和低置信度必须进入明确状态和报告。 | Must | Phase 3 |

### 4. Markdown Wiki

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-020 | Markdown 必须作为 Atlas 的 durable knowledge asset。 | Must | Phase 1 |
| REQ-PROD-021 | 每个 normalized Markdown page 必须包含 front matter。 | Must | Phase 3 |
| REQ-PROD-022 | Front matter 必须包含 workspace、batch id、source file、source path、source type、generated PDF、converter、parser、conversion status、review status、confidence、last updated、owner。 | Must | Phase 3 |
| REQ-PROD-023 | Wiki 页面必须在主要 section 或 chunk 级别保留 source trace。 | Must | Phase 3 |
| REQ-PROD-024 | Wiki 页面必须展示 confidence 和 review status。 | Must | Phase 1 |
| REQ-PROD-025 | Wiki 内容必须更像自动生成的知识页面，而不是营销型文章。 | Must | Phase 1 |
| REQ-PROD-026 | 未审核的 LLM 生成内容必须保持 `REVIEW_REQUIRED`。 | Must | Phase 3 |

### 5. Review 工作流

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-027 | 系统必须提供 Review 工作台，用于 SME 对照源文件和 Markdown。 | Must | Phase 1 |
| REQ-PROD-028 | Review 工作台必须展示 source preview、Markdown preview、confidence、comment 和 action。 | Must | Phase 1 |
| REQ-PROD-029 | Review action 必须至少包括 Approve、Need Fix、OCR Required。 | Must | Phase 1 |
| REQ-PROD-030 | Review 状态必须至少包括 Review Required、Approved、Need Fix、OCR Required、Published。 | Must | Phase 2 |
| REQ-PROD-031 | Review history 必须记录 reviewer、action、timestamp、comment 和 affected chunks。 | Must | Phase 2 |
| REQ-PROD-032 | 低置信度内容不得在未审核时发布为可信 Wiki。 | Must | Phase 3 |

### 6. 知识图谱

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-033 | 系统必须提供轻量、可解释的知识图谱视图。 | Must | Phase 1 |
| REQ-PROD-034 | 初始图谱应从 approved Wiki 和 source trace 派生，而不是直接从未审核 parser output 派生。 | Must | Phase 3 |
| REQ-PROD-035 | 图谱节点类型必须支持 KnowledgeSpace、Document、WikiPage、Concept、Entity、SourceChunk。 | Must | Phase 3 |
| REQ-PROD-036 | 图谱关系必须支持 contains、derived from、mentions、defines、related to、belongs to、uses、depends on、reviewed by。 | Should | Phase 3 |
| REQ-PROD-037 | 图谱关系必须能回指 Wiki page 和 source chunk 作为证据。 | Must | Phase 3 |
| REQ-PROD-038 | UI 必须支持图谱节点 hover、click、legend 和 detail panel。 | Must | Phase 1 |

### 7. Ask 与可信问答

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-039 | 系统必须提供 Ask 界面，用于基于知识空间提问。 | Must | Phase 1 |
| REQ-PROD-040 | Ask 回答必须展示 source references 和 confidence。 | Must | Phase 1 |
| REQ-PROD-041 | 真实 Ask 能力必须优先基于 approved knowledge，而不是未审核内容。 | Must | Phase 3 |
| REQ-PROD-042 | Ask 回答必须能够区分已审核依据和仍需 Review 的依据。 | Should | Phase 3 |
| REQ-PROD-043 | Phase 1 Ask 只能使用 mock answer，不得调用真实模型。 | Must | Phase 1 |

### 8. 设置与治理入口

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-044 | 设置必须以 modal/sheet 覆盖当前产品视图，关闭后回到原页面状态。 | Must | Phase 1 |
| REQ-PROD-045 | 常规设置必须包含语言、主题、默认知识空间和通知偏好。 | Must | Phase 1 |
| REQ-PROD-046 | 语言切换必须位于设置 > 常规设置。 | Must | Phase 1 |
| REQ-PROD-047 | 主题切换必须位于设置 > 常规设置。 | Must | Phase 1 |
| REQ-PROD-048 | 用户信息必须展示 mock profile、organization、role 和 recent activity。 | Should | Phase 1 |
| REQ-PROD-049 | API 信息必须展示 mock key status、scope、last used、rotate/revoke affordance，不得展示真实 secret。 | Must | Phase 1 |
| REQ-PROD-050 | 注册配置必须展示 mock registration policy 和 sign-up preview，但不得创建真实账户。 | Should | Phase 1 |
| REQ-PROD-051 | 成员管理必须支持 pending invitations、active members、roles、search、invite、remove affordances。 | Should | Phase 1 |
| REQ-PROD-052 | MVP 角色模型包括 Owner、Admin、Reviewer、Viewer。 | Must | Phase 1 |
| REQ-PROD-053 | 生产 RBAC enforcement 必须后端实现，前端 prototype 不得暗示已经具备真实权限控制。 | Must | Phase 4 |

### 9. 模型与引擎管理

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-054 | 模型管理必须采用当前 FE 的列表管理风格，而不是右侧固定编辑面板。 | Must | Phase 1 |
| REQ-PROD-055 | 模型管理必须包含 Add Model、内置模型说明、分类 tabs 和模型卡片。 | Must | Phase 1 |
| REQ-PROD-056 | 当前 mock 基线至少展示 `DeepSeek Flash` 和 `text-embedding-v4`。 | Must | Phase 1 |
| REQ-PROD-057 | 模型类型应支持 chat、embedding、rerank、vision、speech。 | Should | Phase 2 |
| REQ-PROD-058 | 模型配置不得在前端保存真实 API key 或 secret。 | Must | Phase 1 |
| REQ-PROD-059 | 未来真实模型 secret 必须进入 secret manager 或加密后端存储。 | Must | Phase 4 |
| REQ-PROD-060 | 向量数据库、解析引擎和存储引擎设置必须作为 adapter-backed mock options 展示。 | Must | Phase 1 |
| REQ-PROD-061 | 未来引擎配置必须支持 adapter type、status、default marker 和 configuration summary。 | Should | Phase 2 |

### 10. 前端体验

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-062 | Phase 1 前端必须使用 Vue 3 + Vite + TypeScript。 | Must | Phase 1 |
| REQ-PROD-063 | 当前 HTML 原型必须逐步组件化，而不是一次性重写导致视觉漂移。 | Must | Phase 1 |
| REQ-PROD-064 | 关键前端状态必须包含当前页面、当前知识空间、当前 tab、设置 panel、语言、主题、新建知识库选择项、模型分类。 | Must | Phase 1 |
| REQ-PROD-065 | 前端必须支持常见桌面、笔记本、平板和窄屏移动端，不得出现明显重叠或页面级异常横向溢出。 | Must | Phase 1 |
| REQ-PROD-066 | 视觉风格必须以 Atlas 当前风格为准；WeKnora 只能作为布局和体验参考，不能复制资产、样式或实现。 | Must | Phase 1 |
| REQ-PROD-067 | 非平凡组件和数据逻辑必须有测试。 | Should | Phase 1 |

### 11. 后端与数据

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-068 | 后端应使用 Java + Spring Boot 实现内部 API。 | Should | Phase 2 |
| REQ-PROD-069 | 持久化应使用 PostgreSQL。 | Should | Phase 2 |
| REQ-PROD-070 | 数据库 schema 变更必须使用 Flyway migration。 | Must | Phase 2 |
| REQ-PROD-071 | 初始实体应包括 workspace、batch、file_item、wiki_page、source_chunk、review_record、graph_node、graph_edge。 | Must | Phase 2 |
| REQ-PROD-072 | 后端必须分层：controller、application service、adapter、repository、migration。 | Must | Phase 2 |
| REQ-PROD-073 | Backend API 必须先有 API guide 和 data model，再开始实现。 | Must | Phase 2 |

### 12. 安全、数据与合规边界

| ID | 需求 | 优先级 | 阶段 |
|---|---|---|---|
| REQ-PROD-074 | Prototype 和 Phase 1 不得引入真实公司数据、凭证、私有路径、日志或机密截图。 | Must | Phase 0/1 |
| REQ-PROD-075 | Prototype 和 Phase 1 不得引入外部网络调用、外部 CDN 或真实 API。 | Must | Phase 0/1 |
| REQ-PROD-076 | 用户输入、文件名、metadata 和 API payload 在真实后端阶段必须进行 schema validation。 | Must | Phase 2 |
| REQ-PROD-077 | 生产错误信息不得泄露 secret、内部路径、原始异常栈或敏感文档内容。 | Must | Phase 4 |
| REQ-PROD-078 | 生产权限、审计、rate limit 和 secret 管理必须在 Phase 4 完成前纳入设计和测试。 | Must | Phase 4 |

## MVP 验收形态

MVP 应让 stakeholder 清楚看到以下端到端流程：

1. 用户创建或进入一个 Knowledge Space。
2. 用户看到文档包被组织为 batch。
3. 文件进入转换、解析、Markdown 生成和 Review 状态。
4. 标准化 Markdown 页面展示 source trace、confidence 和 review status。
5. SME 可以在 Review 工作台处理低置信度内容。
6. Approved 页面进入 Wiki。
7. Wiki 内容可以形成轻量图谱。
8. Ask 可以基于 mock answer 展示 source-grounded 回答形态。

## Phase 1 验收标准

- 当前 FE 主要界面已组件化，或已有明确组件化任务和测试保护。
- 首页、新建知识库、详情页、设置、模型管理核心流程可用。
- UI 与当前 FE 基线保持一致。
- 无真实后端、无真实 API、无外部网络调用。
- Mock 数据和 TypeScript 类型结构清晰。
- `npm run typecheck`、`npm run build`、`npm run test:coverage`、`npm run e2e` 通过，或明确记录无法执行原因。
- SDD spec、design、tasks 与实际 FE 行为一致。

## 非功能需求

- 产品体验应偏企业级、清晰、紧凑、可扫描，避免营销页风格。
- 文件、组件和文档应按产品功能组织。
- 所有关键产品行为必须能追溯到 requirement、story、spec 或 task。
- 图谱、Wiki、Review、Ask 必须优先展示证据和可信度，而不是只展示生成结果。
- 后续真实处理流程应优先 deterministic processing，再做 LLM enrichment。
- 测试覆盖应随风险提升：前端组件、API contract、adapter、数据库 migration、E2E 和安全检查分阶段补齐。

## 待确认问题

- Phase 1 是否要求完全移除 iframe，还是允许短期保留作为过渡？
- 新建知识库是否需要在 Phase 1 做本地表单校验和 mock create result？
- 模型管理 Add Model 是否需要 Phase 1 做 mock modal？
- Phase 2 后端优先做 Knowledge Space metadata，还是先做 batch/file/review metadata？
- 首个真实 adapter 是否固定为 `trinity-office` + `document-normalize`，还是先定义 adapter contract 再选择实现？
- Ask 的首个真实版本是否只允许基于 approved Wiki，还是允许 draft answer 但强标记 review status？
- 生产环境是否需要多租户 workspace，还是先做单 workspace 内部 demo？
