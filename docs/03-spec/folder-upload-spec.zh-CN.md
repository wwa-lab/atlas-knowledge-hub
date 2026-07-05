# 规格说明：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。`folder-upload` 切片的行为唯一真相源。源自 `docs/02-user-stories/folder-upload-stories.md`。

## 来源文档

- `docs/01-requirements/folder-upload-requirements.md`
- `docs/02-user-stories/folder-upload-stories.md`
- `docs/batch-processing-design.md` —— Batch / File Item / File Status / Reports。
- `docs/markdown-standard.md` —— trace / confidence / review 元数据。
- `docs/mvp-scope.md` —— mock 批次状态与文件树。
- FE 基线：`frontend/public/atlas-prototype.html`（`renderDocs()`、`batch`、`.file-tree`、`statusClass`），镜像于 `prototypes/index.html`。

## 范围

Documents 标签页上的交互式 mock 上传：触发 → 清单 → 文件树 → 批次创建 → 进度/指标 → 报告。仅 mock、无网络、无适配器、无持久化。须与基线 Documents 标签页保持一致。

## 约束

- **仅 mock / 无网络：** 无真实文件读取、解压、上传、解析、适配器调用或外部请求（REQ-FU-011）。
- **适配器中立（记录）：** 未来入库经 converter/parser/storage 适配器；本切片不得硬编码或调用任何工具（REQ-FU-012）。
- **保留溯源：** 每个生成文件项与报告条目携带 source-trace、confidence、review-status（REQ-FU-010）。
- **一致性：** 新界面复用 Documents 标签页样式并保持与 `prototypes/index.html` 同步（REQ-FU-013）。
- **双语 + 主题：** 所有新文案入中英文案表；遵循日/夜 token（REQ-FU-015）。

## 视图行为

### Documents 标签页（宿主）

Documents 标签页保持双面板布局（批次摘要面板 + 文件树面板）。`Upload Folder` 与 `Upload ZIP` 成为可用触发器。会话内首次上传前，标签页展示当前活动批次（种子基线批次或最近创建批次）。

### 上传流程（新增）

触发上传打开一个 **Upload Review（上传审阅）** 界面（Documents 标签页上的内联面板或模态浮层 —— 见设计），包含：

1. **源头信息** —— 源类型（`Folder` 或 `ZIP`）、源包名、检测到的文件数、`Cancel` 控件。
2. **清单列表** —— 每个检测文件一行：相对路径、文件类型、mock 大小、支持/不支持徽章、mock confidence、mock review status。
3. **不支持分组** —— 不支持文件单独列出或加徽章，附简短原因。
4. **主操作** —— `Create Batch`（仅当 ≥1 支持文件时可用）与 `Cancel`。

用 `Create Batch` 确认后关闭 Upload Review 界面，Documents 标签页更新为展示新创建批次（指标、进度、文件树）并出现 `View Report` 控件。

### 批次报告（新增）

`View Report` 打开报告界面，汇总活动批次：清单计数、不支持列表、转换失败列表、低置信列表、待审核列表、已批准/已发布计数。每个列出项暴露 source-trace、confidence、review status。

## 状态模型

### 上传流程状态

```
IDLE ──触发(folder|zip)──▶ INVENTORY_READY
INVENTORY_READY ──cancel──▶ IDLE
INVENTORY_READY ──create(≥1 支持)──▶ BATCH_CREATED
INVENTORY_READY ──全不支持──▶ INVENTORY_EMPTY（Create Batch 禁用）
INVENTORY_EMPTY ──cancel──▶ IDLE
BATCH_CREATED ──view-report──▶ REPORT_OPEN
REPORT_OPEN ──close──▶ BATCH_CREATED
```

### 文件状态（单文件项）

使用 `docs/batch-processing-design.md` 的允许集合：`NEW`、`UPLOADED`、`PDF_CONVERTED`、`PDF_CONVERT_FAILED`、`MARKDOWN_GENERATED`、`OCR_REQUIRED`、`LOW_CONFIDENCE`、`REVIEW_REQUIRED`、`APPROVED`、`PUBLISHED`、`FAILED`、`UNSUPPORTED`。

批次创建时的 mock 推进遵循设计状态流：

```
NEW → UPLOADED → PDF_CONVERTED → MARKDOWN_GENERATED → REVIEW_REQUIRED → APPROVED → PUBLISHED
                     │                 │                    │
                     ▼                 ▼                    ▼
             PDF_CONVERT_FAILED   LOW_CONFIDENCE       OCR_REQUIRED
                     │
                     ▼
                   FAILED
不支持类型 → UNSUPPORTED（永不进入流程）
```

Phase 1 推进是确定性种子分配（每个 mock 文件得到固定目标状态），而非实时计时。生成内容的文件项不会自动转为 `APPROVED`/`PUBLISHED` —— 那需未来的 review 切片。

## 交互规则

- `Upload Folder` 与 `Upload ZIP` 都打开 Upload Review 界面；差别仅在记录的源类型与包标签。
- 无支持文件时 `Create Batch` 禁用；禁用态显示 tooltip/帮助说明原因。
- `Cancel` 丢弃待定清单，返回 Documents 标签页且不变更。
- 任意状态下语言切换保留当前清单/批次/报告状态（对齐 REQ-KS-013 行为）。
- 任何操作都不发起网络请求或写盘。

## 空态与错误态

| 情形 | 行为 |
|---|---|
| 选择中未检测到文件 | 显示「此包中未找到文件」并禁用 `Create Batch`。 |
| 全部文件不支持 | 显示不支持分组 + 「无可处理的支持文件」；`Create Batch` 禁用。 |
| 取消上传 | 返回 Documents 标签页，保留原活动批次。 |
| 超大 mock 包（保护） | 清单展示封顶到文档化上限（如 500 行）并附「仅显示前 N 条」；不崩溃。 |

## 溯源、置信与审核

- 每个清单行与批次文件项携带 `confidence`（0–1 mock）与 `review_status`（生成/低置信项默认 `REVIEW_REQUIRED`）。
- 报告条目携带 source-trace 引用（source_file + mock page/chunk），与 `docs/markdown-standard.md` 一致。
- 任何项默认都不标为可信/已批准。

## 适配器与未来真实说明（记录，不实现）

- 真实入库会把所选包交给 **storage 适配器**（持久化原始输入）、**converter 适配器**（`trinity-office` Office→PDF）、**parser 适配器**（`document-normalize` PDF→Markdown/图片）—— 全部置于产品面接口之后，绝不直连。
- 确定性处理（清单、分类、状态）先于任何未来 LLM 增强；LLM 产物保持 review-required。

## API / 接口预期

本切片无。无后端/API 契约在范围内；API guide 有意省略，并在 `docs/00-context/folder-upload-traceability.md` 记录该省略。未来端点（如 `POST /batches`）将在后续后端切片中定义。

## 验收矩阵

| 检查 | 需求 | 可观察结果 |
|---|---|---|
| AC-FU-01 | REQ-FU-001、011 | 点击 Upload Folder/ZIP 打开 Upload Review 界面且无网络请求（devtools 网络面板为空以验证）。 |
| AC-FU-02 | REQ-FU-002 | 每个清单行显示路径、类型、mock 大小。 |
| AC-FU-03 | REQ-FU-003 | 不支持文件加 `UNSUPPORTED` 徽章并分组/区分。 |
| AC-FU-04 | REQ-FU-004 | 文件树由清单路径渲染嵌套文件夹/文件。 |
| AC-FU-05 | REQ-FU-005、006 | Create Batch 产出含上传时间、owner、包名、计数、单文件（允许集合）状态的批次。 |
| AC-FU-06 | REQ-FU-007 | 批次展示 total/PDF converted/Markdown generated/review required/failed/unsupported 指标。 |
| AC-FU-07 | REQ-FU-008 | 进度列表展示四个处理阶段与 mock 百分比。 |
| AC-FU-08 | REQ-FU-009 | 报告列出清单、不支持、失败、低置信、待审核并含计数。 |
| AC-FU-09 | REQ-FU-010 | 每个项/报告条目暴露 source-trace、confidence、review-status；生成项默认 REVIEW_REQUIRED。 |
| AC-FU-10 | REQ-FU-013 | 新界面匹配 Documents 标签页样式；`prototypes/index.html` 镜像 `frontend/public/atlas-prototype.html`。 |
| AC-FU-11 | REQ-FU-014 | 空 / 全不支持 / 取消 态按表行为且不崩溃。 |
| AC-FU-12 | REQ-FU-015 | 语言与主题切换更新新文案并保留状态。 |

## 待解问题

- 目录选择器 vs 合成选择（设计决策；默认合成 + 可选无功能选择器）。
- 单一活动批次 vs 批次列表（默认单一活动批次）。

## Product Goal Batch 2 Vue 对齐补充

历史 `folder-upload` 切片最先完成的是静态原型路径。Product Goal Batch 2 将同一行为延展到真实 Vue 产品路径。

| Phase | Vue 产品验收 |
|---|---|
| Phase C 上传到批处理 mock 闭环 | 在真实 Vue `IBM i Modernization` 详情页中，上传文件夹/ZIP 会打开上传审阅界面，展示支持/不支持文件清单，创建 mock batch，显示指标、文件状态、source trace，并打开 batch report。 |

本补充不引入后端/API 行为、真实文件读取、parser/converter 调用、storage 写入或外部 provider 调用。仍然仅 mock 且保持适配器中立。
