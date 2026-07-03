# 需求：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。由 `atlas-sdd-generate-all` 为 `folder-upload` 切片生成。

## 背景

`knowledge-space` 切片已交付一个 Documents（文档）标签页，渲染静态批次摘要、进度列表和文件树，其中 `Upload Folder` / `Upload ZIP` 按钮尚无功能（见 `frontend/public/atlas-prototype.html` 的 `renderDocs()`）。本切片让这些按钮驱动一个交互式**mock**上传流程：用户选择文件夹或 ZIP，看到生成的文件清单与文件树，创建批次，并得到 mock 处理报告 —— 全程 mock 数据，无真实文件 I/O、无解析、无网络。

本切片细化而非重定义 `docs/batch-processing-design.md`（Batch / File Item / File Status / Reports 模型）、`docs/mvp-scope.md`（mock 批次状态与文件树）、`docs/markdown-standard.md`（trace/confidence/review 元数据）。

## 切片边界

- **本切片拥有：** 上传触发交互、mock 文件清单生成、不支持文件分类、由清单构建文件树、由清单创建批次、mock 单文件状态推进、由已创建批次派生的摘要/进度、以及 mock 批次报告。
- **`knowledge-space` 拥有：** Documents 标签页外壳、空间导航、既有静态基线批次。行为重叠处，`folder-upload` 在保持视觉一致的前提下取代静态 Documents 批次渲染。

## 目标

1. 让用户模拟把项目文档文件夹或 ZIP 上传进知识空间。
2. 从选择项生成可审阅的文件清单与层级文件树。
3. 创建带 mock 状态与 mock 处理报告的批次。
4. 在每个生成的文件项与报告条目上保留 source trace、confidence、review status。
5. 全流程仅 mock、适配器中立；未来真实入库仅记录不实现。

## 范围内

- Documents 标签页上可交互的 `Upload Folder` 与 `Upload ZIP`。
- Mock 文件清单生成：每个文件的原始相对路径、文件类型、mock 大小。
- 按类型区分支持/不支持，并单独凸显不支持文件。
- 由清单路径构建的层级文件树。
- 由已确认清单创建批次：上传时间、owner、源包名、文件总数、单文件初始状态。
- 跨允许 File Status 集合的 mock 单文件状态推进。
- 由已创建批次派生的摘要指标与处理进度。
- Mock 批次报告（清单、不支持、转换失败、低置信、待审核）。
- 空 / 全不支持 / 取消 选择状态。
- 所有新界面的中英双语文案与日/夜主题一致性。

## 当前阶段范围外

- 真实的文件夹/ZIP 读取、解压或上传。
- 真实 Office→PDF 转换、PDF→Markdown 解析、OCR 或 asset 抽取。
- 后端 API、持久化或任务队列（本切片无 API guide）。
- converter/parser/storage 适配器实现（仅记录接口）。
- 认证、真实归属或权限执行。
- 任何外部网络调用、新增运行时依赖、真实公司文档、凭据或私有路径。

## 需求

| ID | 需求 | 来源 | 优先级 |
|---|---|---|---|
| REQ-FU-001 | Documents 标签页须提供 `Upload Folder` 与 `Upload ZIP`，启动 mock 上传流程，无真实文件 I/O 或网络调用。 | `docs/batch-processing-design.md`、FE 基线 `renderDocs()` | Must |
| REQ-FU-002 | 启动上传须生成 mock 文件清单，每条含原始相对路径、文件类型、mock 大小。 | `docs/batch-processing-design.md`（File Item） | Must |
| REQ-FU-003 | 清单须按类型区分支持/不支持，并单独凸显不支持文件。 | `docs/batch-processing-design.md`（`UNSUPPORTED`、Reports） | Must |
| REQ-FU-004 | 系统须由清单路径派生层级文件树（文件夹与嵌套文件）。 | `docs/mvp-scope.md`（mock 文件树）、FE 基线 `.file-tree` | Must |
| REQ-FU-005 | 确认清单须创建新批次，记录上传时间、owner、源包名、文件总数、单文件初始状态。 | `docs/batch-processing-design.md`（Batch） | Must |
| REQ-FU-006 | 每个文件项须携带允许 File Status 集合中的状态，初始化后再走 mock 推进。 | `docs/batch-processing-design.md`（File Status） | Must |
| REQ-FU-007 | 批次视图须展示摘要指标 —— 总数、PDF converted、Markdown generated、review required、failed、unsupported —— 与批次模型一致。 | `docs/batch-processing-design.md`、FE 基线指标 | Must |
| REQ-FU-008 | 批次视图须展示处理进度列表（Office→PDF、PDF→Markdown、Source Trace 校验、SME Review），带 mock 百分比。 | FE 基线 `batch.progress` | Must |
| REQ-FU-009 | 系统须生成 mock 批次报告，汇总清单、不支持文件、转换失败、低置信页、待审核项。 | `docs/batch-processing-design.md`（Reports） | Must |
| REQ-FU-010 | 每个生成的文件项与报告条目须保留 source-trace、confidence、review-status 字段（即便是 mock 值）。 | `docs/markdown-standard.md`、`PROJECT_RULES.md`（Trace And Review） | Must |
| REQ-FU-011 | 上传、清单、批次创建、推进、报告须仅 mock：Phase 1 无真实上传、解析、适配器或网络调用。 | `PROJECT_RULES.md`（Phase Discipline、Data Safety）、`docs/mvp-scope.md` | Must |
| REQ-FU-012 | 设计须记录：未来真实入库把文件夹/ZIP 处理、转换、解析走 converter/parser/storage 适配器，绝不直连。 | `PROJECT_RULES.md`（Adapter Boundaries、Parser Neutral） | Must |
| REQ-FU-013 | 新上传界面须与基线 Documents 标签页保持视觉与交互一致，并同步静态镜像。 | `CLAUDE.md`（Prototype-as-baseline）、`PROJECT_RULES.md`（Prototype To Product） | Must |
| REQ-FU-014 | 流程须处理空选择、全不支持选择、取消上传，并给出清晰、非阻塞的提示。 | 编码规范错误处理、产品质量门 | Should |
| REQ-FU-015 | 所有新标签、帮助文本、状态消息须提供中英双语并遵循日/夜 token。 | REQ-KS-013、REQ-KS-014（既有切片）、产品反馈 | Must |

## 假设

- mock「选择」由种子化的内存样本集表示（非真实文件选择器结果）；未来可接入真实 `<input type="file" webkitdirectory>`，但 Phase 1 须降级为 mock 数据。
- Owner 默认取当前 mock 用户/空间 owner；不引入真实身份。
- 支持类型遵循 `docs/markdown-standard.md` 的 source types（pptx、docx、pdf、xlsx、图片）；其余为 `UNSUPPORTED`。

## 待解问题

- mock 文件选择器用浏览器目录选择器（仅视觉、仍 mock 清单）还是纯合成样本？默认：合成样本 + 可选的无功能选择器按钮。（推迟至设计；见溯源。）
- 本切片是否可列出一个空间的多个批次，还是仅最近创建的一个？默认：单个已创建批次在演示中取代基线静态批次。（推迟。）
