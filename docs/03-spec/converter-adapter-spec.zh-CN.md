# 规格：Converter Adapter

## 状态

草稿。Phase 3 adapter 切片。`converter-adapter` 的行为真相源。来源于 `docs/02-user-stories/converter-adapter-stories.md`。

## 来源文档

- `docs/01-requirements/converter-adapter-requirements.md`
- `docs/02-user-stories/converter-adapter-stories.md`
- `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/batch-processing-design.md`
- `docs/markdown-standard.md`

## 范围

Atlas 必须支持一个 Office-to-PDF 转换切片，所有转换都通过产品面 converter adapter contract 路由。本切片包括 adapter registry/capability 行为、conversion run 行为、metadata 写回、状态/错误映射、mock-engine 验证和 seam guard。不包括 parser、storage、OCR、vector/model、Wiki publish、graph、Ask、前端 UI 或生产 auth/RBAC。

## 约束

- **仅适配器：** 产品工作流代码依赖 converter interface 和 registry contract，而不直接依赖 `trinity-office`（REQ-CA-001）。
- **不硬编码单一实现：** `trinity-office` 是首个目标，但必须能通过 adapter configuration 和 capability metadata 替换（REQ-CA-002）。
- **Mock-engine 验证：** 自动化测试必须使用 mock/fake converter engine，且不得要求真实文档语料或本地 `trinity-office` binary（REQ-CA-010）。
- **Secret/path 安全：** 所有持久化或返回路径都是相对路径且无目录穿越；不返回或记录原始 secret、私有 endpoint、命令凭据、异常栈或私有绝对路径（REQ-CA-008、REQ-CA-009）。
- **Trace/review 保留：** 转换结果保留 source path、status、可用时的 confidence、adapter name 和 review status；转换绝不自动批准生成内容（REQ-CA-007）。
- **无外部云调用：** 本切片不引入外部网络依赖或云服务。

## 角色

| 角色 | 职责 |
|---|---|
| Knowledge base administrator | 触发或监控 batch conversion。 |
| Platform administrator | 查看 converter adapter 可用性和脱敏配置。 |
| Delivery lead | 使用 conversion report 理解哪些文件已准备进入 parser。 |
| SME reviewer | 使用 failure/OCR/unsupported 状态决定后续处理。 |
| Codex implementation agent | 严格依据本 spec 和任务清单实现。 |

## 功能需求

### Adapter Boundary

- **FR-CA-001：** converter workflow 必须在任何转换开始前通过 registry/capability contract 解析 converter adapter。（US-CA-001）
- **FR-CA-002：** 非 adapter 产品层不得为了转换引用 `trinity-office`、直接命令执行 API 或外联 HTTP client。（US-CA-001、US-CA-005）
- **FR-CA-003：** adapter registry 必须至少支持一个已配置 default converter adapter，并安全暴露 unavailable/misconfigured 状态。（US-CA-002）

### Capability Metadata

- **FR-CA-004：** Capability metadata 必须包含 adapter key、display name、version、output type `pdf`、supported source types、default marker、health/status 和脱敏配置摘要。（US-CA-002）
- **FR-CA-005：** Capability metadata 不得暴露原始命令路径、原始环境变量、凭据、hostname 或本地绝对路径。（US-CA-002）

### Conversion Run

- **FR-CA-006：** Conversion run 必须接收 batch id、adapter key 或 default marker，以及目标 file id 集合或 source metadata。（US-CA-001、US-CA-003）
- **FR-CA-007：** 对 `pptx`、`docx`、`xlsx`，转换成功必须产生 `PDF_CONVERTED`、相对 `pdfPath`、adapter name 和 completed timestamp。（US-CA-001）
- **FR-CA-008：** 已有 PDF 输入必须作为 pass-through candidate 处理，且不得调用 Office converter。确切 artifact copy/reference 策略仍为 OQ-CA-002。（US-CA-001）
- **FR-CA-009：** `image` 输入可分类为 `OCR_REQUIRED`；`unsupported` 输入必须分类为 `UNSUPPORTED`。（US-CA-004）
- **FR-CA-010：** 受支持 Office 输入转换失败必须映射到 `PDF_CONVERT_FAILED`，并带用户安全错误摘要。（US-CA-004）

### Metadata Write-Back

- **FR-CA-011：** Converter result 必须通过 metadata boundary 更新 file item status、`pdfPath`、可用时的 confidence 和适用时的 `errorMessage`。（US-CA-003）
- **FR-CA-012：** Converter result 写入前必须拒绝绝对路径、穿越路径、原始私有路径和不安全错误载荷。（US-CA-003）
- **FR-CA-013：** 对生成/派生转换产物，review status 必须保持 `REVIEW_REQUIRED`，除非后续明确 review action 改变它。（US-CA-003）
- **FR-CA-014：** Conversion run record 必须保留 adapter key/name、input count、output counts、started/completed timestamp、result status 和用户安全 summary。（US-CA-003、US-CA-004）

### Reporting And Failure Behavior

- **FR-CA-015：** Conversion report 必须汇总 total files、converted PDFs、unsupported files、failed files、OCR-required files 和 skipped/pass-through files。（US-CA-004）
- **FR-CA-016：** 原始 engine stderr/stdout 不得持久化或返回。可以记录清洗后的摘要。（US-CA-004）
- **FR-CA-017：** 未预期 adapter fault 必须返回用户安全错误，并在没有安全逐文件失败结果时保持 file status 不变。（US-CA-004）

## 非功能需求

| 类别 | 需求 |
|---|---|
| Security | 响应/日志中无原始 secret、命令凭据、私有 endpoint、私有路径或异常栈。 |
| Reliability | Mock/fake engine 测试必须覆盖成功、不支持、失败、OCR-required、路径拒绝和 adapter unavailable 场景。 |
| Extensibility | Adapter interface 必须允许未来 converter implementation 替换，而不修改产品工作流调用方。 |
| Auditability | Conversion run 和逐文件 result metadata 必须可追溯到 batch/file id 和 adapter identity。 |
| Data safety | 只使用 mock/sample metadata；seed/test data 中无真实公司文档或本地绝对路径。 |

## 工作流

```text
+------------------+
| Batch/file data  |
+--------+---------+
         |
         v
+------------------+      unavailable/misconfigured
| Resolve adapter  |------------------------------+
+--------+---------+                              |
         | available                              v
         v                                +----------------+
+------------------+                      | Safe error     |
| Validate targets |                      | no file change |
+--------+---------+                      +----------------+
         |
         v
+------------------+
| Execute adapter  |
| mock/fake in CI  |
+--------+---------+
         |
         v
+------------------+
| Map per-file     |
| results/statuses |
+--------+---------+
         |
         v
+------------------+
| Persist metadata |
| + run report     |
+------------------+
```

## 状态模型

### Conversion Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`：adapter 已解析且输入校验通过。
- `RUNNING -> SUCCEEDED`：所有 eligible files 均转换成功，或安全 pass-through/skipped。
- `RUNNING -> PARTIAL_FAILED`：至少一个文件转换成功，且至少一个文件 failed/unsupported/OCR-required。
- `RUNNING -> FAILED`：没有目标文件产生可用转换结果，或 adapter-level failure 阻止逐文件结果产生。

### 逐文件状态映射

| 输入 / 结果 | File Status | 说明 |
|---|---|---|
| `pptx`/`docx`/`xlsx` success | `PDF_CONVERTED` | 需要相对 `pdfPath`。 |
| Existing `pdf` pass-through | `PDF_CONVERTED` | 不调用 Office converter；artifact 策略见 OQ-CA-002。 |
| Supported Office conversion failure | `PDF_CONVERT_FAILED` | 需要用户安全错误摘要。 |
| Image requiring OCR | `OCR_REQUIRED` | 本切片不执行 OCR。 |
| Unsupported source type | `UNSUPPORTED` | 需要用户安全原因。 |
| Adapter-level unavailable before execution | unchanged | Run 安全失败；file status 不变。 |

## 校验规则

- Batch id 和 target file ids 必须引用已有 metadata records。
- Target source type 必须是现有 `SourceType` 值之一：`pptx`、`docx`、`pdf`、`xlsx`、`image`、`unsupported`。
- `pdfPath` 和所有 artifact path 必须通过 metadata API 使用的相对路径安全规则。
- Confidence 值若存在，必须位于 `[0,1]`。
- Error summary 必须有长度边界、用户安全，并移除原始命令输出、异常栈、私有路径和 secret。
- Adapter key 必须解析到已注册 adapter，否则以用户安全 unavailable/misconfigured 状态失败。

## API / Interface Surface

完整契约见 `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`。

| Interface | 行为 |
|---|---|
| `GET /api/converter-adapters` | 列出已配置 converter adapter capabilities，配置已脱敏。 |
| `POST /api/batches/{batchId}/conversion-runs` | 对 file metadata 启动 conversion run 并记录结果。 |
| `GET /api/conversion-runs/{runId}` | 返回 conversion run summary 和逐文件 report。 |
| Internal converter adapter interface | 在产品面 contract 后执行转换；测试必须有 mock/fake 实现。 |

## 验收矩阵

| Check | 需求 | 可观察结果 |
|---|---|---|
| AC-CA-01 | REQ-CA-001、REQ-CA-011 | 若 adapter implementation 之外出现 direct engine references，静态 guard tests 失败。 |
| AC-CA-02 | REQ-CA-002、REQ-CA-003 | Capability endpoint/contract 返回脱敏 adapter metadata 和可替换 default marker。 |
| AC-CA-03 | REQ-CA-004、REQ-CA-005 | Mock run 将 Office metadata 转为 `PDF_CONVERTED`，并带相对 `pdfPath`。 |
| AC-CA-04 | REQ-CA-006 | unsupported/failed/OCR-needed 输入映射到现有状态，并带安全 report entries。 |
| AC-CA-05 | REQ-CA-007、REQ-CA-008 | Result writes 保留 source/review metadata，并拒绝不安全路径。 |
| AC-CA-06 | REQ-CA-009 | Error responses/log assertions 无原始 secret、私有路径或异常栈。 |
| AC-CA-07 | REQ-CA-010 | `cd backend && mvn verify` 仅用 mock/fake engines 通过。 |
| AC-CA-08 | REQ-CA-012 | API/adapter guide 和双语 SDD docs 在实现前存在。 |

## 范围外

- Parser adapter、Markdown 生成、source chunk 抽取、OCR 执行。
- Storage adapter 或对象存储 upload/copy 实现。
- Vector/model adapters、graph derivation、Ask/RAG。
- 前端页面或 settings UI 更新。
- 生产 auth/RBAC 和 secret manager 集成。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-CA-001 | 真实 `trinity-office` 执行采用 local process wrapper 还是 external worker。 | 影响部署拓扑，不影响当前 mock-engine contract。 |
| OQ-CA-002 | storage-adapter 存在前，PDF pass-through 采用 copy 还是 relative reference。 | 影响已有 PDF 的 artifact path 语义。 |
| OQ-CA-003 | 目标 `trinity-office` 命令行契约的确切形式。 | 影响真实 adapter 实现细节，不影响 interface 或 mock tests。 |
