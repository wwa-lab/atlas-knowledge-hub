# 溯源：知识库空间（Knowledge Space）

## 状态

Phase 1 前端已实现，当前 IA 细化已被原型基线接受。

当前实现状态：

- `T-KS-001` 至 `T-KS-031` 已按当前 Phase 1 前端/原型范围完成。
- 已接受的知识库详情页标签为 `文档`、`处理中心`、`Wiki`、`图谱`。
- Global Chat / Trusted Ask 位于单个知识库详情页之外，可选择一个或多个知识库作为回答上下文。
- `frontend/public/atlas-prototype.html` 是当前 Phase 1 前端保真基线；`prototypes/index.html` 与其保持字节一致。
- 后端、数据库、认证、生产持久化、真实 parser/converter/model/vector/storage 适配器以及外部网络调用仍不在范围内。
- 历史 Knowledge Space SDD 全量中文同步推迟到独立文档同步任务；本文件先补齐 `docs/00-context/knowledge-space-traceability.md` 的状态伴随版本。

## 切片

`knowledge-space`

## 来源输入

| 来源 | 角色 |
|---|---|
| `frontend/public/atlas-prototype.html` | 当前 Phase 1 前端保真基线。 |
| `prototypes/index.html` | 当前静态 UI 原型与演示行为。 |
| `docs/product-vision.md` | 产品目的、目标用户和先可信后对话原则。 |
| `docs/mvp-scope.md` | MVP 边界与验收形态。 |
| `docs/architecture.md` | 适配器架构与未来技术栈方向。 |
| `docs/batch-processing-design.md` | 批次、文件项、状态与报告模型。 |
| `docs/markdown-standard.md` | Markdown 元数据、source trace、confidence、review status 规则。 |
| `docs/knowledge-graph-design.md` | 图谱节点与边模型。 |
| `docs/review-workflow.md` | SME review 状态与决策。 |
| `docs/technology-decisions.md` | Vue/Spring/PostgreSQL/Flyway 时机与实现约束。 |

## 产物映射

| 阶段 | 文件 |
|---|---|
| 需求 | `docs/01-requirements/knowledge-space-requirements.md` |
| 用户故事 | `docs/02-user-stories/knowledge-space-stories.md` |
| 规格 | `docs/03-spec/knowledge-space-spec.md` |
| 架构 | `docs/04-architecture/knowledge-space-architecture.md` |
| 数据流 | `docs/04-architecture/knowledge-space-data-flow.md` |
| 数据模型 | `docs/04-architecture/knowledge-space-data-model.md` |
| 设计 | `docs/05-design/knowledge-space-design.md` |
| API Guide | `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` |
| 任务 | `docs/06-tasks/knowledge-space-tasks.md` |

## 需求链接

| 需求 | 故事 | Spec 章节 | 任务 |
|---|---|---|---|
| REQ-KS-001 Knowledge Space home | US-KS-001, US-KS-002 | Home Knowledge Space Library | T-KS-001, T-KS-002, T-KS-028, T-KS-030 |
| REQ-KS-002 Create Knowledge Space | US-KS-002 | Create Knowledge Space | T-KS-028, T-KS-030 |
| REQ-KS-003 Space detail tabs | US-KS-003 | Space Detail Navigation | T-KS-004, T-KS-031 |
| REQ-KS-004 Document management and batch status | US-KS-004 | Document Batch | T-KS-005, T-KS-006, T-KS-030, T-KS-031 |
| REQ-KS-005 LM Wiki | US-KS-005 | Wiki | T-KS-007, T-KS-031 |
| REQ-KS-006 Knowledge graph | US-KS-006 | Graph | T-KS-008, T-KS-031 |
| REQ-KS-007 Processing Center quality gates | US-KS-007 | Processing Center | T-KS-009, T-KS-010, T-KS-031 |
| REQ-KS-008 Global Chat / Trusted Ask | US-KS-008 | Global Chat, Trusted Ask | T-KS-011, T-KS-031 |
| REQ-KS-009 Trace and review metadata | US-KS-005, US-KS-007, US-KS-008 | Data Contracts, Document Batch, Processing Center, Wiki, Graph, Global Chat | T-KS-012, T-KS-031 |
| REQ-KS-010 Prototype constraints | All | Non-Functional Requirements | T-KS-013 |
| REQ-KS-013 Language switching | US-KS-009 | Language Switching | T-KS-015, T-KS-017 |
| REQ-KS-014 Day/night mode | US-KS-010 | Day And Night Mode | T-KS-016, T-KS-017 |
| REQ-KS-015 Model management | US-KS-011 | Model Management | T-KS-018, T-KS-019 |
| REQ-KS-016 Model secret safety | US-KS-011 | Model Management, Non-Functional Requirements | T-KS-018, T-KS-019 |
| REQ-KS-017 Registration | US-KS-012 | Registration | T-KS-020, T-KS-022 |
| REQ-KS-018 Space member management | US-KS-013 | Knowledge Space Member Management | T-KS-021, T-KS-022 |
| REQ-KS-019 Simple RBAC safety | US-KS-013 | Knowledge Space Member Management, Non-Functional Requirements | T-KS-021, T-KS-022 |
| REQ-KS-020 Account settings | US-KS-014 | Account Settings | T-KS-023, T-KS-025 |
| REQ-KS-021 Data and extension engines | US-KS-015 | Data And Extension Engines | T-KS-024, T-KS-025 |
| REQ-KS-022 Sidebar settings shell | US-KS-016 | Sidebar And Settings Shell | T-KS-026 |
| REQ-KS-023 Responsive layout | US-KS-017 | Responsive Layout | T-KS-027 |

## 门禁状态

- Requirements：历史草稿基线已存在。
- Stories：历史草稿基线已存在。
- Spec：已更新到当前 Phase 1 IA 基线。
- Architecture：历史草稿基线已存在；Phase 1 IA 细化未新增后端/API 工作。
- Design：已更新到当前 Phase 1 IA 基线。
- Tasks：已更新到 `T-KS-031`。
- Implementation：`T-KS-001` 至 `T-KS-031` 的 Phase 1 前端/原型基线已完成。
- Verification：`T-KS-031` 已通过 `npm run build`、`npm run e2e`、镜像 diff 和 `git diff --check`；此前 Phase 1 shell 检查记录在 `docs/06-tasks/knowledge-space-tasks.md`。
