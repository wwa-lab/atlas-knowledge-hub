# 详细设计：Parser Adapter

## 状态

草稿。Phase 3 adapter 切片。由 `docs/03-spec/parser-adapter-spec.md` 与 `docs/04-architecture/parser-adapter-architecture.md` 派生。

## 来源架构

Parser-adapter 是 backend/API + adapter contract 切片。它向 metadata control plane 增加 parser capability/run 行为，同时把 parser engine 细节限制在 adapter implementations 内。设计有意镜像 converter-adapter 的 run/capability/report 形态，让 Codex 能用最少新产品概念实现下一个 adapter 切片。

## 已核对现有代码上下文

设计前已验证以下锚点：

| Existing element | Verified anchor | Parser-adapter use |
|---|---|---|
| File artifact fields | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` | 复用 `pdfPath`、`markdownPath`、`assetsPath`、`errorMessage`、confidence、review status。 |
| Review preservation pattern | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:92` | Parser result writes 应像 conversion writes 一样保留 review status。 |
| Source chunk entity | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` | 持久化 parser 创建的 source trace chunks。 |
| File statuses | `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` | 复用 `MARKDOWN_GENERATED`、`LOW_CONFIDENCE`、`OCR_REQUIRED`、`FAILED`、`UNSUPPORTED`。 |
| Relative path validator | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | 用于 Markdown/assets/PDF path validation。 |
| File/chunk read API | `backend/src/main/java/com/atlas/metadata/controller/FileController.java:48` | 现有 chunk list endpoint 仍作为 source chunks 读取侧。 |
| Converter API pattern | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:30` | Parser controller 应遵循 capability/create/get 形态。 |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | 扩展 parser adapter implementation 的 guard 期望。 |

## 设计范围

### 范围内

- Parser adapter domain contracts、capability metadata、request/result shapes。
- Parser run persistence 与逐文件 result persistence。
- Parser service 行为：target selection、adapter resolution、execution、validation、status mapping、summary calculation、safe error handling。
- Parser API guide 与 contract tests。
- Source chunk creation 与 Markdown/assets metadata write-back。Markdown 字节物化与对象存储写入延后到真实 runtime/storage integration。
- 用于确定性测试的 mock parser adapter。

### 范围外

- 真实 parser process topology、Markdown 字节物化、OCR 执行、LLM enrichment、前端 UI、storage object operations、graph/Ask、Wiki publication、production auth/RBAC。

## 模块设计

### Parser Adapter Contract

概念契约：

```text
ParserAdapter
  capability() -> ParserCapability
  parse(ParserRequest) -> ParserResult
```

`ParserCapability` 包含：

- adapter key 与 display name
- safe version/status
- supported input types: `pdf`
- output types: `markdown`, `assets`
- default marker
- low-confidence threshold，默认 `0.800`
- masked configuration summary

`ParserRequest` 包含：

- run id
- batch id
- artifact root hint，默认 `generated/markdown`
- low-confidence threshold
- file descriptors：file id、source path、source type、current status、PDF path、可用时的 converter adapter key

`ParserResult` 包含：

- adapter key
- safe message
- per-file results
- 每个文件的 source chunks

### Parser Service

职责：

- 校验 batch 与 requested file ids。
- 选择符合条件目标文件：`PDF_CONVERTED` 且 safe `pdfPath`。
- 将 skipped/ineligible files 记录进 report，但不传给 adapter。
- 通过 explicit key 或 default marker 解析 adapter。
- 创建 parser run、标记 running、执行 adapter、持久化已校验结果。
- 用已提交阈值映射状态：confidence `< 0.800` -> `LOW_CONFIDENCE`；否则 successful Markdown -> `MARKDOWN_GENERATED`。
- 保留 review status 与 source metadata。
- 持久化/响应前脱敏 safe messages 与 safe errors。

### Parser Summary Calculator

Summary 从已持久化 parser file results 派生：

| Count | Rule |
|---|---|
| `total` | 所有请求目标文件加 skipped/ineligible report rows。 |
| `markdownGenerated` | Status `MARKDOWN_GENERATED`。 |
| `lowConfidence` | Status `LOW_CONFIDENCE`。 |
| `ocrRequired` | Status `OCR_REQUIRED`。 |
| `failed` | Status `FAILED`。 |
| `skipped` | 未传给 adapter 的 ineligible targets。 |
| `unsupported` | adapter 安全返回时的 status `UNSUPPORTED`。 |

### Parser Persistence

新增与数据模型等价的逻辑 domain entities：

- `ParserRun`
- `ParserFileResult`
- `ParserRunStatus`
- `ParserAdapterStatus` 可复用 converter status 命名模式，或在有意引入时共享一个 neutral adapter status。

Repositories 只用于 parser run/result 持久化与现有 file/source chunk 持久化。`wiki_page` 保持不触碰。

### Parser Mapper / DTOs

DTOs 应匹配 API guide：

- `ParserCapabilityResponse`
- `CreateParserRunRequest`
- `ParserRunResponse`
- `ParserFileResultResponse`
- `ParserRunSummaryResponse`
- `ParserChunkResponse` 或适当复用 source chunk response

所有 API responses 使用 `ApiEnvelope`。

## API / Interface Design

API implementation guide 对 payloads 负责：

- `GET /api/parser-adapters`
- `POST /api/batches/{batchId}/parser-runs`
- `GET /api/parser-runs/{runId}`

Validation failures 使用现有 API envelope/error handling 风格。认证在本切片保持 deferred/internal-only。

## 数据设计

数据模型见 `docs/04-architecture/parser-adapter-data-model.md`。

关键不变量：

- 不新增 `FileStatus` 值。
- `markdownPath`、`assetsPath` 与 parser result paths 必须通过 relative-path validation。
- 本切片校验 metadata paths 并持久化 source chunk trace metadata；字节级 Markdown front matter validation 延后到真正物化 Markdown bytes 的 runtime/storage slice。
- Generated/low-confidence/OCR-required parser output 的 `reviewStatus` 保持 `REVIEW_REQUIRED`。
- `source_chunk` rows 仅在 parser file result 通过校验后插入。
- Parser run summaries 从 result rows 派生。

## Workflow / Execution Design

### Successful Parse

1. API 接收 parser run request。
2. Service 校验 batch 与 file ids。
3. Service 过滤 eligible files，并记录 skipped/ineligible results。
4. Registry 解析 parser adapter。
5. Service 创建 parser run 并标记为 `RUNNING`。
6. Adapter 为每个 eligible file 返回 Markdown/assets/chunk result。
7. Service 校验每个 result。
8. Service 更新 `file_item`、持久化 `source_chunk`、持久化 parser result。
9. Service 计算 summary 与 terminal status。
10. API 返回 parser run response。

### Adapter Unavailable

- Parser run 进入 `FAILED`。
- File items 不变。
- Response 只包含 safe message。

### Unsafe Result

- 在 unsafe metadata 持久化前 validation 失败。
- Response 使用 validation error shape。
- 除非实现显式支持 per-file safe partial persistence，否则默认丢弃 safe result rows 并拒绝整个 run result；默认：将 run result 视为 invalid。

## Validation And Error Handling

| Case | Expected handling |
|---|---|
| Unknown batch | 404 safe not found。 |
| Unknown target file | 404 safe not found。 |
| Cross-batch file id | 400 validation error 或 404 safe not found；不泄露其他 batch membership。 |
| No eligible targets | 400 validation error，带 safe field message。 |
| Unsafe path | 持久化前 400 validation error。 |
| Confidence outside `[0,1]` | 持久化前 400 validation error。 |
| Adapter throws runtime exception | Parser run `FAILED`；file items 不变；sanitized safe message。 |
| Parser output contains raw logs/secrets/paths | 只存储 sanitized `safeError`/`safeMessage`。 |

## Edge Case Trace

### Low-Confidence Threshold

规则：confidence `< 0.800` 映射为 `LOW_CONFIDENCE`；confidence `>= 0.800` 映射为 `MARKDOWN_GENERATED`。

| Input | Result |
|---|---|
| `0.799` | `LOW_CONFIDENCE` |
| `0.800` | `MARKDOWN_GENERATED` |
| `null` with success | `MARKDOWN_GENERATED`，并在 report note 中说明 confidence unavailable |

### Path Safety

规则：artifact paths 必须是安全相对路径。

| Input | Result |
|---|---|
| `generated/markdown/a.md` | Accepted |
| `/tmp/a.md` | Rejected |
| `../private/a.md` | Rejected |

### Eligibility

规则：adapter 只接收 `PDF_CONVERTED` 且 safe `pdfPath` 的文件。

| File | Result |
|---|---|
| `PDF_CONVERTED` + safe PDF path | Sent to adapter |
| `UPLOADED` + no PDF path | Skipped/ineligible |
| `PDF_CONVERT_FAILED` | Skipped/ineligible |

## Testing Considerations

实现所需验证：

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

测试覆盖必须包括：

- 使用 mock parser 的 adapter contract tests。
- Summary calculator unit tests。
- Service validation 与 status mapping tests。
- API contract integration tests。
- Parser engine references 的 seam guard test。
- Secret/path sanitization tests。

## 风险 / 设计取舍

| ID | Risk / Tradeoff | Decision |
|---|---|---|
| DT-PA-001 | Low-confidence threshold 后续可能需要产品调优。 | 当前提交 `< 0.800`，并在 capability metadata 中暴露。 |
| DT-PA-002 | 逐文件 invalid parser result 是否可部分持久化。 | 默认在持久化前拒绝 unsafe run result，保持 metadata 干净。 |
| DT-PA-003 | 为 preview 创建 Wiki page 可能有用。 | 延后 `wiki_page` 创建到 publish/review slice，以保留可信边界。 |

## 待确认问题

- OQ-PA-001：真实 parser runtime topology。
- OQ-PA-002：未来 `wiki_page` creation 归属。
- OQ-PA-003：产品确认 low-confidence threshold。
