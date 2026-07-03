# 溯源：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。切片 `folder-upload`。

## 切片

`folder-upload` —— Documents 标签页上的交互式 mock 文件夹/ZIP 上传：清单 → 文件树 → 批次创建 → mock 报告。

## 来源输入

| 来源 | 角色 |
|---|---|
| `docs/00-context/slice-roadmap.md` | 切片待办 + Phase 1 FE 验证/约束行。 |
| `docs/batch-processing-design.md` | Batch、File Item、File Status、Reports 模型。 |
| `docs/mvp-scope.md` | mock 批次状态与文件树范围。 |
| `docs/markdown-standard.md` | trace、confidence、review-status 字段。 |
| `docs/01-requirements/requirement.md` | 产品级需求与适配器/可信原则。 |
| `frontend/public/atlas-prototype.html` | FE fidelity 基线（`renderDocs()`、`batch`、`.file-tree`、`statusClass`）。 |
| `prototypes/index.html` | 直接 review 的静态镜像。 |
| `docs/03-spec/knowledge-space-spec.md`（REQ-KS-004） | 本切片扩展的宿主 Documents 标签页行为。 |

## 产物映射

| 阶段 | EN | zh-CN |
|---|---|---|
| 需求 | `docs/01-requirements/folder-upload-requirements.md` | `…folder-upload-requirements.zh-CN.md` |
| 用户故事 | `docs/02-user-stories/folder-upload-stories.md` | `…folder-upload-stories.zh-CN.md` |
| 规格 | `docs/03-spec/folder-upload-spec.md` | `…folder-upload-spec.zh-CN.md` |
| 架构 | `docs/04-architecture/folder-upload-architecture.md` | `…folder-upload-architecture.zh-CN.md` |
| 数据流 | `docs/04-architecture/folder-upload-data-flow.md` | `…folder-upload-data-flow.zh-CN.md` |
| 数据模型 | `docs/04-architecture/folder-upload-data-model.md` | `…folder-upload-data-model.zh-CN.md` |
| 设计 | `docs/05-design/folder-upload-design.md` | `…folder-upload-design.zh-CN.md` |
| API guide | **省略 —— 见下方决定** | — |
| 任务 | `docs/06-tasks/folder-upload-tasks.md` | `…folder-upload-tasks.zh-CN.md` |
| 溯源 | `docs/00-context/folder-upload-traceability.md` | `…folder-upload-traceability.zh-CN.md` |

## API Guide 省略（记录决定）

本 Phase 1 前端、仅 mock 切片无后端/API 契约在范围内。依 SDD profile 与 `atlas-sdd-generate-all`，API guide 有意省略。未来真实入库（storage/converter/parser 适配器、`POST /batches`、状态轮询）将在后续后端切片定义。

## 需求 → 故事 → 规格 → 任务 链接

| 需求 | 故事 | 规格（验收） | 任务 |
|---|---|---|---|
| REQ-FU-001 | US-FU-001、US-FU-002 | AC-FU-01 | T-FU-003、T-FU-013 |
| REQ-FU-002 | US-FU-003 | AC-FU-02 | T-FU-002、T-FU-004 |
| REQ-FU-003 | US-FU-004 | AC-FU-03 | T-FU-005 |
| REQ-FU-004 | US-FU-005 | AC-FU-04 | T-FU-006 |
| REQ-FU-005 | US-FU-006 | AC-FU-05 | T-FU-002、T-FU-007、T-FU-013 |
| REQ-FU-006 | US-FU-006、US-FU-007 | AC-FU-05 | T-FU-001、T-FU-007 |
| REQ-FU-007 | US-FU-007 | AC-FU-06 | T-FU-007 |
| REQ-FU-008 | US-FU-007 | AC-FU-07 | T-FU-007 |
| REQ-FU-009 | US-FU-008 | AC-FU-08 | T-FU-002、T-FU-008、T-FU-013 |
| REQ-FU-010 | US-FU-003、US-FU-008、US-FU-009 | AC-FU-09 | T-FU-001、T-FU-004、T-FU-008 |
| REQ-FU-011 | US-FU-001、US-FU-002、US-FU-006 | AC-FU-01 | T-FU-002、T-FU-003 |
| REQ-FU-012 | US-FU-009 | （适配器说明） | T-FU-012 |
| REQ-FU-013 | US-FU-001、US-FU-005、US-FU-010 | AC-FU-10 | T-FU-010、T-FU-011、T-FU-012 |
| REQ-FU-014 | US-FU-004 | AC-FU-11 | T-FU-009 |
| REQ-FU-015 | US-FU-010 | AC-FU-12 | T-FU-010 |

## 与 knowledge-space 的切片边界

- `folder-upload` **拥有：** 上传触发交互、mock 清单、不支持分类、文件树构建、批次创建 + mock 状态推进、批次报告。
- `knowledge-space` **拥有：** Documents 标签页外壳（REQ-KS-004）、空间导航、全局文案表与主题 token。`folder-upload` 扩展 `renderDocs()` 并在保持一致的前提下取代静态基线批次渲染。
- 共享类型（`FileStatus`、`Batch`、`FileItem`）须在 `frontend/src/types.ts` 协调，不得重复。

## 验证证据计划

依 `docs/00-context/slice-roadmap.md` 的 Phase 1 FE 行：

- `cd frontend && npm run typecheck && npm run test && npm run build && npm run e2e`
- `git diff --check`
- `diff frontend/public/atlas-prototype.html prototypes/index.html`（须为空）
- 扫描 diff 中的新网络调用 / 依赖 / secret / 私有路径。

证据在实现后记录于此及任务完成矩阵。

## 使用的子技能（生成过程）

`atlas-sdd-generate-all` 编排链条：`req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality`。

## 引入的关键假设

- mock「选择」是种子化内存样本，而非真实文件选择器结果。
- Phase-1 实现载体是单文件原型（`frontend/public/atlas-prototype.html` + 镜像），因为 `frontend/src/App.vue` 仍是无 Vue 功能组件的 iframe 宿主。`features/folder-upload/*.vue` 布局仅为文档化的未来提取目标（T-FU-011，推迟）。
- 单一活动批次在演示中取代基线静态批次。

## 待解问题

- 目录选择器 vs 纯合成选择（默认合成 + 可选无功能选择器）。
- 单一活动批次 vs 批次列表（默认单一活动批次）。
- `folderUploadMock` 提取到 `frontend/src/data/` 在本切片还是下一切片（默认：推迟至原型→Vue 提取）。

## 推迟翻译

无。每个产物都有 EN 与 `.zh-CN.md` 两份，REQ/US/T/AC ID 完全一致。
