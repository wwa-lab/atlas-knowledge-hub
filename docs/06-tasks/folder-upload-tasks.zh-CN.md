# 任务：文件夹上传（Folder Upload）

## 状态

草稿 —— 可交接实现。Phase 1 前端，仅 mock。源自 `docs/05-design/folder-upload-design.md`。行为唯一真相源：`docs/03-spec/folder-upload-spec.md`。

## 实现门

- 需求、故事、spec、架构、数据流、数据模型、设计齐备（本切片）。
- **无后端/API 在范围内** —— API guide 有意省略（记录于 `docs/00-context/folder-upload-traceability.md`）。
- 仅 mock：无真实上传/解析、无适配器、无网络、无新依赖。
- 主实现载体为 `frontend/public/atlas-prototype.html`，逐字节镜像到 `prototypes/index.html`。除非任务说明，**不要**搭建 `features/folder-upload/*.vue`（T-FU-011 是唯一、可选的提取任务）。
- 每个生成项保留 source trace、confidence、review status。

## 任务拆分

| ID | 任务 | 归属 | 依赖 | 映射 | 验证 |
|---|---|---|---|---|---|
| T-FU-001 | 扩展 `frontend/src/types.ts`（和/或原型内联类型注释）：`FileStatus`、`ReviewStatus`、`SourceKind`、`SourceType`、`UploadFlowState`、`SourceTrace`、`InventoryFile`、`Inventory`、`FileItem`、`BatchMetrics`、`BatchProgressStage`、`Batch`、`ReportEntry`、`Report`、`FolderUploadProvider`。与既有基线类型协调（扩展，不重复）。 | Codex | 数据模型 | REQ-FU-006、REQ-FU-010 | `cd frontend && npm run typecheck` 通过；`FileStatus` 等于 `docs/batch-processing-design.md` 的允许集合。 |
| T-FU-002 | 新增种子 mock 提供者（`createInventory`、`createBatch`、`buildReport`）实现 `FolderUploadProvider`，async 形态返回，状态分布具代表性。原型：内联种子 + 工厂函数；保持纯/确定性。 | Codex | T-FU-001 | REQ-FU-002、REQ-FU-005、REQ-FU-009、REQ-FU-011 | 单测：清单含支持+不支持；批次指标由 fileItems 派生；种子批次的报告分区非空。 |
| T-FU-003 | 让 `Upload Folder` / `Upload ZIP` 打开 Upload Review 界面（模态浮层，All-Settings 模式）。接线 `IDLE → INVENTORY_READY/INVENTORY_EMPTY`。无网络。 | Codex | T-FU-002 | REQ-FU-001、REQ-FU-011、REQ-FU-013 | 点击打开界面；devtools 网络面板无请求（AC-FU-01）。 |
| T-FU-004 | 渲染清单列表：每文件路径、类型、mock 大小、支持徽章、confidence、review status，复用 `.file-row`/`.badge`。 | Codex | T-FU-003 | REQ-FU-002、REQ-FU-010 | 每行显示路径/类型/大小（AC-FU-02）；行携带 confidence + review status（AC-FU-09）。 |
| T-FU-005 | 单独渲染不支持分组，带 `UNSUPPORTED` 徽章 + 原因；补齐缺失的 `statusClass` 映射（`NEW`、`UPLOADED`、`APPROVED`、`PUBLISHED`、`UNSUPPORTED`）。 | Codex | T-FU-004 | REQ-FU-003 | 不支持文件加徽章/分组（AC-FU-03）。 |
| T-FU-006 | 由清单路径（按 `/` 切分）构建层级文件树，带单文件状态徽章，超过展示上限显示截断提示。 | Codex | T-FU-004 | REQ-FU-004 | 由路径渲染嵌套文件夹/文件（AC-FU-04）；遵守上限不崩溃。 |
| T-FU-007 | 实现 `Create Batch`（当 ≥1 支持时可用）：经提供者创建批次，转 `BATCH_CREATED`，以新批次指标+进度重渲染 Documents 标签页（`renderDocs`）。禁用态显示帮助。 | Codex | T-FU-002、T-FU-004 | REQ-FU-005、REQ-FU-006、REQ-FU-007、REQ-FU-008 | 批次含时间/owner/名/计数+单文件状态（AC-FU-05）；指标+4 阶段进度渲染（AC-FU-06、AC-FU-07）。 |
| T-FU-008 | 新增 `View Report` 打开报告界面，含六个分区；每条显示路径、状态、confidence、review status 与 `source_trace:` 行。 | Codex | T-FU-007 | REQ-FU-009、REQ-FU-010 | 报告列出清单/不支持/失败/低置信/待审核并含计数（AC-FU-08）；trace 存在（AC-FU-09）。 |
| T-FU-009 | 处理空/错误态：无文件、全不支持（Create 禁用 + 消息）、Cancel（恢复原批次）、超限提示。 | Codex | T-FU-003、T-FU-007 | REQ-FU-014 | 各态按 spec 表行为且不崩溃（AC-FU-11）。 |
| T-FU-010 | 把所有新文案键加入 EN/zh 文案表；确保语言 + 日/夜切换保留上传/批次/报告状态。 | Codex | T-FU-003..T-FU-008 | REQ-FU-015、REQ-FU-013 | 语言/主题切换更新新文案并保留状态（AC-FU-12）。 |
| T-FU-011 | （可选/推迟）原型→Vue 提取开始后，把渲染函数提取到 `features/folder-upload/*.vue` + `frontend/src/data/folderUploadMock.ts`。非本切片验收所需。 | Codex | T-FU-001..T-FU-010 | REQ-FU-013 | 仅当排期：组件渲染一致；typecheck/build 通过。 |
| T-FU-012 | 把 `prototypes/index.html` 逐字节同步到 `frontend/public/atlas-prototype.html`；在溯源中记录省略的 API guide + 适配器中立未来。 | Codex | T-FU-003..T-FU-010 | REQ-FU-012、REQ-FU-013 | `diff frontend/public/atlas-prototype.html prototypes/index.html` 为空（AC-FU-10）。 |
| T-FU-013 | 用 `data-testid` 钩子为关键流程加 Playwright 冒烟：打开上传 → 清单 → 创建批次 → 查看报告。 | Codex | T-FU-003..T-FU-008 | REQ-FU-001、REQ-FU-005、REQ-FU-009 | `cd frontend && npm run e2e` 对 folder-upload spec 通过。 |

## 验证计划（Phase 1 FE 行）

报「完成」前在 `frontend/` 运行：

```bash
cd frontend
npm run typecheck
npm run test
npm run build
npm run e2e
```

外加 CLAUDE.md 仓库根基线检查：

```bash
git diff --check
diff frontend/public/atlas-prototype.html prototypes/index.html   # 须为空
# 扫描 diff 中的新网络调用 / 依赖 / secret / 私有路径
```

跳过任何检查须写明原因 —— 绝不暗示未跑的检查通过了。

## 约束（逐任务，全程适用）

- **仅 mock / 无网络 / 无依赖：** 无 `fetch`/`XHR`/websocket，无新增 npm 包，无真实文件读取。
- **适配器中立：** 视图仅依赖 `FolderUploadProvider`；绝不按名引用或调用 `trinity-office` / `document-normalize` / 任何工具。
- **保留溯源：** 生成项携带 source-trace、confidence、review status；生成/低置信默认 `REVIEW_REQUIRED`。
- **一致性：** 同一改动内更新 `prototypes/index.html`；复用既有样式/token。

## 偏离规则

若某任务无法按 `docs/03-spec/folder-upload-spec.md` 原文满足，停下并指出 spec/task 不一致（先更新 spec/design/tasks），而非绕过编码。

## 完成矩阵

| ID | 状态 | 证据 |
|---|---|---|
| T-FU-001 | 完成 | `frontend/src/types.ts`；`cd frontend && npm run typecheck` 通过。 |
| T-FU-002 | 完成 | `frontend/public/atlas-prototype.html` 内联 seeded provider；`frontend/src/folderUploadPrototype.test.ts`；`cd frontend && npm run test` 通过。 |
| T-FU-003 | 完成 | `Upload Folder` / `Upload ZIP` 打开 `data-testid="upload-review"`，未新增网络调用；`cd frontend && npm run e2e` 通过。 |
| T-FU-004 | 完成 | 清单行通过 `data-testid="inventory-row"` 渲染路径/类型/大小/confidence/review status；`cd frontend && npm run e2e` 通过。 |
| T-FU-005 | 完成 | 不支持分组渲染 `UNSUPPORTED` 行并补齐状态映射；`cd frontend && npm run e2e` 通过。 |
| T-FU-006 | 完成 | 文件树由清单路径构建，含状态徽章与截断提示支持；`cd frontend && npm run e2e` 通过。 |
| T-FU-007 | 完成 | `Create Batch` 创建派生指标与四阶段进度；`cd frontend && npm run e2e` 通过。 |
| T-FU-008 | 完成 | `View Report` 打开六个报告分区并显示 `source_trace:` 行；`cd frontend && npm run e2e` 通过。 |
| T-FU-009 | 完成 | 上传状态/渲染 helper 已实现无文件、全不支持、Cancel、截断提示处理；`cd frontend && npm run test` 与 `cd frontend && npm run e2e` 通过。 |
| T-FU-010 | 完成 | 已加入 EN/zh 文案键；重渲染/主题/语言切换保留状态；`cd frontend && npm run build` 通过。 |
| T-FU-011 | 推迟（可选） | 本切片未排期提取。 |
| T-FU-012 | 完成 | 已同步 `prototypes/index.html`；`diff frontend/public/atlas-prototype.html prototypes/index.html` 为空。溯源已记录省略 API guide 与适配器中立未来。 |
| T-FU-013 | 完成 | `frontend/tests/e2e/folder-upload.spec.ts`；`cd frontend && npm run e2e` 通过。 |
