# 规格：Parser Adapter

## 状态

草稿。Phase 3 adapter 切片。`parser-adapter` 的行为唯一真相源。由 `docs/02-user-stories/parser-adapter-stories.md` 派生。

## 来源文档

- `docs/01-requirements/parser-adapter-requirements.md`
- `docs/02-user-stories/parser-adapter-stories.md`
- `docs/03-spec/converter-adapter-spec.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/markdown-standard.md`
- `docs/batch-processing-design.md`
- `docs/architecture.md`

## 范围

Atlas 必须支持一个 PDF 到 Markdown/images 的 parser 切片，并且所有解析都通过产品侧 parser adapter contract。该切片包括 parser capability 行为、parser run 行为、Markdown/assets/source chunk 输出契约、metadata 写回、low-confidence/OCR/failure 状态映射、mock-engine 验证与 adapter seam guard。不包括真实 OCR、LLM enrichment、图谱抽取、Ask/RAG、发布到 Wiki、前端 UI、生产 auth/RBAC 或 storage-adapter 对象操作。

## 约束

- **仅 adapter：** 产品 workflow code 依赖 parser interfaces 与 registry contracts，不直接依赖 `document-normalize` 或其他 parser engines（REQ-PA-001）。
- **不硬编码单一实现：** `document-normalize` 是首个目标，但必须通过 adapter configuration 与 capability metadata 可替换（REQ-PA-002）。
- **mock-engine 验证：** 自动化测试必须使用 mock/fake parser engines，不要求真实文档语料或本地 parser binary（REQ-PA-012）。
- **secret/path 安全：** 所有持久化或返回路径都是相对且无 traversal；response/log 中不得出现 raw secrets、private endpoints、stack traces、raw parser logs 或私有绝对路径（REQ-PA-008、REQ-PA-011）。
- **保留 trace/review：** Parser result 必须保留 source path、PDF path、converter metadata、confidence、source chunks、parser name 与 review status；生成内容绝不自动 approve（REQ-PA-009、REQ-PA-010、REQ-PA-014）。
- **无外部云调用：** 本切片不引入外部网络依赖或云服务。

## 角色

| 角色 | 作用 |
|---|---|
| 知识库管理员 | 在 conversion 后触发或监控 parser runs。 |
| 平台管理员 | 查看 parser adapter 可用性与脱敏配置。 |
| Delivery lead | 使用 parser reports 了解 review 工作量与阻塞文件。 |
| SME reviewer | 对照 PDF 证据 review 生成的 Markdown 与 source chunks。 |
| Codex 实现 agent | SDD 接受后严格依据本 spec 与 task checklist 实现。 |

## 功能需求

### Adapter Boundary

- **FR-PA-001：** Parser workflow 必须在任何 parsing 前通过 registry/capability contract 解析 parser adapter。（US-PA-001）
- **FR-PA-002：** Non-adapter product layers 不得为 parsing 直接引用 `document-normalize`、MinerU、Docling、PaddleOCR、直接命令执行 API 或 outbound HTTP clients。（US-PA-001、US-PA-005）
- **FR-PA-003：** Adapter registry 必须支持至少一个已配置默认 parser adapter，并安全暴露 unavailable/misconfigured 状态。（US-PA-002）

### Capability Metadata

- **FR-PA-004：** Capability metadata 必须包含 adapter key、display name、version、输入类型 `pdf`、输出类型 `markdown` 与 `assets`、default marker、health/status、low-confidence threshold、masked configuration summary。（US-PA-002）
- **FR-PA-005：** Capability metadata 不得暴露原始命令字符串、环境变量值、凭证、hostname、原始本地路径或私有 endpoint。（US-PA-002）

### Parser Run

- **FR-PA-006：** Parser run 必须接受 batch id、adapter key 或 default marker、requested actor、mode，以及目标 file ids 或全部符合条件的 batch files。（US-PA-001）
- **FR-PA-007：** 符合条件的 target 是同一 batch 中 status 为 `PDF_CONVERTED` 且有安全相对 `pdfPath` 的 file item。（US-PA-001、US-PA-005）
- **FR-PA-008：** 不符合条件的文件必须以安全 report entry 跳过，且不得传给 parser adapter。（US-PA-004）
- **FR-PA-009：** Parser run records 必须保留 adapter key/name、input count、output counts、started/completed timestamps、result status 与 user-safe summary。（US-PA-004）

### Markdown、Assets 与 Chunks

- **FR-PA-010：** 成功解析必须写入相对 `markdownPath`、可选相对 `assetsPath`、parser key、completion timestamp、confidence 与 `MARKDOWN_GENERATED` 状态。（US-PA-003）
- **FR-PA-011：** 当 parser runtime 物化 Markdown 字节时，生成 Markdown 必须包含 `docs/markdown-standard.md` 所需 front matter 字段，包括 workspace、batch id、source file/path/type、PDF file、converter、parser、conversion status、review status、confidence、last updated、owner。本 metadata/API slice 记录 `markdownPath` 与 source chunk 证据；字节级对象写入不属于本 slice。（US-PA-003）
- **FR-PA-012：** 当 Markdown 字节被物化时，必须在主要 section 或 chunk 中包含 source trace blocks，含 source file、PDF file、page、section、chunk id、confidence 与 review status。本 slice 为 report/review flow 持久化等价的 source chunk metadata。（US-PA-003）
- **FR-PA-013：** Parser results 必须创建 source chunk records，含 file item id、source file、page、section、confidence 与 review status。（US-PA-003）
- **FR-PA-014：** Parser-adapter 不得创建已发布 Wiki pages；除非后续 publish/review 切片改变契约，否则 `wiki_page` 创建延后。（US-PA-003）

### 状态映射与失败行为

- **FR-PA-015：** Parser confidence 低于 `0.80` 必须将文件映射为 `LOW_CONFIDENCE`，并保留 `reviewStatus=REVIEW_REQUIRED`。（US-PA-004）
- **FR-PA-016：** 需要 OCR 的 parser output 必须映射为 `OCR_REQUIRED`，包含安全原因，且本切片不执行 OCR。（US-PA-004）
- **FR-PA-017：** 符合条件 PDF 的 parser failure 必须映射为 `FAILED`，并包含有界 user-safe error summary。（US-PA-004）
- **FR-PA-018：** 意外 adapter fault 必须返回 user-safe errors；除非存在安全的 per-file failure result，否则保持 file status 不变。（US-PA-005）
- **FR-PA-019：** Parser reports 必须汇总 total files、Markdown generated、low-confidence、OCR-required、failed、skipped/ineligible、unsupported outcomes。（US-PA-004）

### Metadata Write-Back 与校验

- **FR-PA-020：** Parser result writes 必须拒绝 absolute paths、URI-prefixed paths、traversal paths、unknown file ids、run batch 外文件，以及越界 confidence。（US-PA-005）
- **FR-PA-021：** Parser error summaries 必须脱敏，移除 raw parser logs、stack traces、secrets、private endpoints、hostnames 与 absolute/private paths。（US-PA-005）
- **FR-PA-022：** Parser result writes 必须保留 source path、source type、PDF path、converter evidence 与 review status，除非后续显式 review action 改变 review status。（US-PA-003、US-PA-005）

## 非功能需求

| 类别 | 需求 |
|---|---|
| Security | response 或持久化 safe message 中不得包含 raw secrets、command credentials、hostnames、private endpoints、private paths、stack traces 或 raw parser logs。 |
| Reliability | mock/fake engine tests 覆盖 success、low-confidence、OCR-required、failed、skipped/ineligible、unsafe path rejection、unknown file result、unavailable adapter、adapter fault。 |
| Extensibility | Parser adapter interface 允许未来 parser implementation 替换，而无需改变 product workflow callers。 |
| Auditability | Parser run 与逐文件 result metadata 可追溯到 batch/file ids、adapter identity、Markdown/assets paths 与 source chunks。 |
| Data safety | 测试夹具只用 mock/sample metadata；不得包含真实公司文档、原始文档内容、私有路径或外部云调用。 |

## Workflow

```text
+--------------------------+
| Batch/file metadata     |
| PDF_CONVERTED targets   |
+------------+-------------+
             |
             v
+--------------------------+       unavailable/misconfigured
| Resolve parser adapter   |------------------------------+
+------------+-------------+                              |
             | available                                  v
             v                                    +----------------+
+--------------------------+                      | Safe run error |
| Validate eligible files  |                      | no unsafe leak |
+------------+-------------+                      +----------------+
             |
             v
+--------------------------+
| Execute parser adapter   |
| mock/fake in CI          |
+------------+-------------+
             |
             v
+--------------------------+
| Map statuses + chunks    |
| markdown/assets paths    |
+------------+-------------+
             |
             v
+--------------------------+
| Persist metadata +       |
| parser run report        |
+--------------------------+
```

## 状态模型

### Parser Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`：adapter 已解析且输入校验通过。
- `RUNNING -> SUCCEEDED`：全部符合条件文件安全生成 Markdown。
- `RUNNING -> PARTIAL_FAILED`：至少一个文件生成 Markdown，且至少一个文件为 low-confidence、OCR-required、failed、skipped 或 unsupported。
- `RUNNING -> FAILED`：没有目标文件产出可用 Markdown，或 adapter-level failure 阻止逐文件结果。

### 逐文件状态映射

| 输入 / 结果 | File Status | 说明 |
|---|---|---|
| 符合条件 PDF 成功且 confidence `>= 0.80` | `MARKDOWN_GENERATED` | 需要相对 `markdownPath`；可选相对 `assetsPath`。 |
| 符合条件 PDF 成功但 confidence `< 0.80` | `LOW_CONFIDENCE` | Review status 保持 `REVIEW_REQUIRED`。 |
| Parser 表示需要 OCR | `OCR_REQUIRED` | 本切片不执行 OCR。 |
| 符合条件 PDF 解析失败 | `FAILED` | 需要 user-safe error summary。 |
| 非 PDF 或非 `PDF_CONVERTED` target | unchanged / skipped | 不传给 parser adapter；在 report 中作为 skipped/ineligible。 |
| 执行前 adapter-level unavailable | unchanged | Run 安全失败；file status 不变。 |

## 校验规则

- Batch id 与目标 file ids 必须引用现有 metadata records。
- 目标文件必须属于请求的 batch。
- 传给 adapter 的目标必须 status 为 `PDF_CONVERTED` 且拥有安全相对 `pdfPath`。
- `markdownPath`、`assetsPath`、chunk asset references 与所有 artifact paths 必须是安全相对路径。
- Confidence 存在时必须在 `[0,1]`。
- Chunk page 存在时必须为正整数。
- Chunk ids 在一个 parser run 内必须唯一。
- Error summaries 必须有界、user-safe，并移除 raw parser output、stack traces、private paths、hostnames 与 secrets。
- Adapter key 必须解析到已注册 adapter，否则以 user-safe unavailable/misconfigured status 失败。

## API / Interface Surface

完整契约见 `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md`。

| Interface | 行为 |
|---|---|
| `GET /api/parser-adapters` | 列出已配置 parser adapter capabilities，并只返回脱敏配置。 |
| `POST /api/batches/{batchId}/parser-runs` | 为符合条件的 file metadata 启动 parser run 并记录结果。 |
| `GET /api/parser-runs/{runId}` | 返回 parser run summary 与逐文件/chunk report。 |
| Internal parser adapter interface | 通过产品侧 contract 解析 PDF；测试必须有 mock/fake implementation。 |

## 验收矩阵

| Check | Requirement | 可观察结果 |
|---|---|---|
| AC-PA-01 | REQ-PA-001, REQ-PA-012 | adapter implementation 外出现 direct parser engine reference 时 static guard tests 失败。 |
| AC-PA-02 | REQ-PA-002, REQ-PA-003 | Capability contract 返回脱敏 adapter metadata 与可替换 default marker。 |
| AC-PA-03 | REQ-PA-004, REQ-PA-005 | Mock parser run 将符合条件的 PDF metadata 转为 `MARKDOWN_GENERATED`，并带相对 Markdown/assets paths。 |
| AC-PA-04 | REQ-PA-006, REQ-PA-007, REQ-PA-008 | Low-confidence、OCR-required、failed output 映射到现有状态且有安全 report entries。 |
| AC-PA-05 | REQ-PA-009, REQ-PA-010, REQ-PA-014 | Markdown/source chunk output 保留 source trace、confidence、parser identity、PDF path、review status。 |
| AC-PA-06 | REQ-PA-011 | Unsafe paths、unknown files、cross-batch files、out-of-range confidence 在持久化前被拒绝。 |
| AC-PA-07 | REQ-PA-012 | `cd backend && mvn verify` 使用 mock/fake parser engines 通过。 |
| AC-PA-08 | REQ-PA-013 | Parser run report 包含所有必需 outcome counts。 |
| AC-PA-09 | REQ-PA-008 | Error responses/log assertions 不出现 raw secrets、private paths、raw parser logs 或 stack traces。 |

## 范围外

- 超出 adapter seam contract 的真实 `document-normalize` process/worker execution。
- OCR 执行、LLM enrichment、图谱派生、Ask/RAG、Wiki 发布。
- Storage adapter 的对象 copy/upload/delete 行为。
- Markdown 字节物化、对象存储写入，以及超出 mock path/chunk metadata 的字节级 front matter validation。
- 前端页面或设置 UI 更新。
- 生产 auth/RBAC 与 secret manager 集成。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-PA-001 | 真实 `document-normalize` 执行采用本地进程包装还是外部 worker，包括 Markdown 字节物化与 storage writes。 | 影响部署拓扑，不影响当前 mock-engine contract。 |
| OQ-PA-002 | Parser output 默认不创建 `wiki_page` 行；draft page row 应由后续 publish slice 还是本切片拥有？ | 影响 publish/review 归属，不影响 parser result contract。 |
| OQ-PA-003 | 本 SDD 提交 low-confidence threshold 为 `< 0.80`。 | 若产品希望不同阈值，实现前确认。 |
