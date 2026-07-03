# 设计：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。源自 `docs/04-architecture/folder-upload-architecture.md` 并以 FE 基线为依据。

## 实现载体（权威）

当前 Vue 应用（`frontend/src/App.vue`）仍是 `frontend/public/atlas-prototype.html` 的 **iframe 宿主**；尚无 Vue 功能组件。依 `CLAUDE.md`（Prototype-as-baseline），`folder-upload` 的 Phase-1 实现落在单文件原型及其镜像：

- **主：** `frontend/public/atlas-prototype.html` —— 扩展内联 JS（`appData`、`uiState`、`renderDocs()`、文案表 `text`/`copy()`、`statusClass`），加入上传/清单/批次/报告状态与渲染函数。
- **镜像：** `prototypes/index.html` —— 改动后须与主文件保持逐字节一致。
- **typed 模型（仅未来提取目标）：** 架构文档中的 `features/folder-upload/*.vue` + `frontend/src/data/folderUploadMock.ts` 布局是后续原型→Vue 提取的目标，而非 Phase-1 交付物。除非任务清单明确说明，Codex 本切片不得搭建这些组件。

这维持一致性纪律：改体验 → 同一切片内更新静态镜像 + SDD。

## 体验原则

- 上传像 Documents 标签页的自然延伸，而非独立应用。
- 审核前一切都不显得可信：从清单起就可见 confidence 与 `REVIEW_REQUIRED`。
- 每个状态都可经可见控件到达并可逆（Cancel 始终干净返回）。

## 布局与交互模型

### Documents 标签页（宿主，扩展）

保留既有双面板 `doc-grid`（批次摘要面板 + 文件树面板）。摘要面板中的 `Upload Folder` / `Upload ZIP` 变为可用。上传流程打开时，把 **Upload Review** 界面作为替换 `doc-grid` 的内联面板，或作为其上的模态浮层（复用 All Settings 的模态/浮层模式）。建议：模态浮层，与 `REQ-KS-022` 设置模态行为一致。

### Upload Review 界面

```
┌ Upload Review ─────────────────────────────── ✕ ┐
│ 源: Folder · "2026-06 IBM i Discovery"            │
│ 检测到 238 文件 · 226 支持 · 12 不支持             │
│                                                  │
│ [ 清单 ]                                          │
│  /Discovery/Architecture/Target.pptx  pptx 1.2MB  ● conf 0.86  REVIEW_REQUIRED
│  /Discovery/BRD/BRD.docx              docx 480KB  ● conf 0.90  REVIEW_REQUIRED
│  ...                                             │
│ [ 不支持 (12) ]                                   │
│  /Discovery/raw/notes.zzz             — 不支持类型
│                                                  │
│           [ Cancel ]   [ Create Batch ]          │  ← 0 支持时 Create 禁用
└──────────────────────────────────────────────────┘
```

- 清单行复用原型已有的 `.file-row` / `.badge` 视觉样式。
- 不支持文件以独立分组渲染，带弱化徽章（`statusClass['UNSUPPORTED']`）。
- `Create Batch` 禁用态用既有禁用按钮样式 + 帮助行。

### 批次视图（扩展 `renderDocs`）

`Create Batch` 后，`renderDocs()` 渲染新创建批次而非种子基线：同样的 `metric-grid`、`progress-list`、`.file-tree` 标记，外加摘要面板 `button-row` 中新增的 `View Report` 按钮。

### 批次报告界面

复用模态/浮层模式。分区为可折叠列表：清单、不支持、转换失败、低置信、待审核、已批准/已发布。每行显示路径、状态徽章、confidence、review status，以及匹配 Wiki `.trace` 样式的 `source_trace:` 行。

## 文件树

由清单 `path` 字符串（按 `/` 切分）构建嵌套结构。文件夹渲染为标题，文件为带状态徽章的 `.file-row`。封顶到文档化最大行数；截断时显示弱化的「显示 M 中前 N 条」提示。保留既有 `.file-tree` 容器样式。

## 组件清单（供未来 Vue 提取）

| 组件 | 职责 | Props / State |
|---|---|---|
| `UploadReview` | 源头信息 + 清单 + 不支持 + 操作 | `inventory: Inventory`，emit `create`、`cancel` |
| `InventoryList` | 渲染支持清单行 | `files: InventoryFile[]` |
| `UnsupportedGroup` | 渲染不支持行 | `files: InventoryFile[]` |
| `BatchReport` | 报告分区 | `report: Report` |
| （共享）`BatchView`/`FileTree` | 指标/进度/树 | `batch: Batch` |

它们与 Phase-1 渲染函数 1:1 对应，使后续提取机械化。

## 数据契约

视图消费 `Inventory`、`Batch`、`Report`，与 `docs/04-architecture/folder-upload-data-model.md` 定义完全一致。Phase-1 mock 数据来自内联种子对象（原型）或 `folderUploadMock.ts`（未来）。任何视图都不独立计算指标 —— 读取 `batch.metrics`。

## 视觉系统

- 复用既有 token、`.panel`、`.badge`、`.metric-grid`、`.progress`、`.file-tree`、`.trace`、模态/浮层类。不新增配色系统。
- 状态徽章颜色复用已为基线状态定义的 `statusClass` 映射；为尚未出现的状态（如 `NEW`、`UPLOADED`、`APPROVED`、`PUBLISHED`、`UNSUPPORTED`）用既有徽章变体补充映射。

## 响应式行为

- Upload Review 与 Report 浮层遵循既有响应式模态规则（`REQ-KS-023`）：窄视口全宽，宽视口居中浮层。
- 清单/文件树列表在浮层内滚动；操作按钮保持固定/可见。

## 可访问性与键盘

- 上传触发、Cancel、Create Batch、View Report 均为带标签的真实 `<button>`。
- 模态浮层焦点陷阱，可经既有 `✕` 控件与 Escape 关闭（对齐 All Settings 行为）。
- 状态徽章含文字（状态字符串），不仅靠颜色。

## 测试钩子（稳定选择器）

为 E2E 添加稳定钩子：`data-testid="upload-folder"`、`upload-zip`、`upload-review`、`inventory-row`、`unsupported-row`、`create-batch`、`batch-metrics`、`batch-progress`、`file-tree`、`view-report`、`batch-report`。支撑任务中的 Playwright 冒烟测试。

## 双语文案键（加入文案表）

`uploadReviewTitle`、`sourceFolder`、`sourceZip`、`detectedFiles`、`supportedCount`、`unsupportedCount`、`createBatch`、`createBatchDisabledHelp`、`cancel`、`viewReport`、`reportTitle`、`reportInventory`、`reportUnsupported`、`reportFailures`、`reportLowConfidence`、`reportReviewRequired`、`reportApproved`、`emptyNoFiles`、`emptyNoSupported`、`showingFirstN`。每个需 EN + zh 值。

## 待解问题（已定默认）

- **选择器 vs 合成：** 默认合成种子；可展示可选的无功能「选择文件夹…」呈现，但绝不读真实文件。
- **单 vs 多批次：** 单一活动批次在演示中取代基线批次。
- **浮层 vs 内联：** 默认模态浮层（与 All Settings 一致）。若内联一致性更佳，Codex 可选并记录于任务。
