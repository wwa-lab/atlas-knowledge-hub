# 数据模型：Parser Adapter

## 状态

草稿。Phase 3 adapter 切片。为 metadata 增加 parser execution evidence。

## 复用现有实体

| Entity | 现有契约 | 本切片用途 |
|---|---|---|
| `batch` | 拥有 file items 与 processing run context。 | Parser run 以 batch 为范围。 |
| `file_item` | 保存 `source_path`、`source_type`、`status`、`confidence`、`review_status`、`pdf_path`、`markdown_path`、`assets_path`、`error_message`。 | Parser 通过 metadata boundary 更新 status、Markdown/assets/error/confidence 字段。 |
| `source_chunk` | 保存 file item 的 source trace records。 | Parser 从已解析 Markdown sections/chunks 创建 source chunks。 |
| `wiki_page` | 延后的 wiki page metadata table。 | 本切片不创建。 |

## 新逻辑实体

### `parser_run`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque run id。 |
| `batchId` | String | Yes | 现有 batch id。 |
| `adapterKey` | String | Yes | 注册 parser adapter key，例如 `document-normalize`。 |
| `adapterVersion` | String | No | 安全时由 adapter 报告的 version。 |
| `status` | Enum | Yes | `REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。 |
| `requestedBy` | String | No | 内部 actor/team。 |
| `mode` | String | Yes | `mock` 或 `configured`。测试使用 `mock`。 |
| `lowConfidenceThreshold` | Decimal | Yes | 默认 `0.800`。 |
| `startedAt` | Timestamp | Yes | Server timestamp。 |
| `completedAt` | Timestamp | No | Terminal state 后的 server timestamp。 |
| `summary` | Object | Yes | 按 outcome 计数；不含 raw parser output。 |
| `safeMessage` | String | No | User-safe run summary 或 error。 |

### `parser_file_result`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque result id。 |
| `runId` | String | Yes | 所属 parser run。 |
| `fileItemId` | String | Yes | 现有 file item id。 |
| `sourcePath` | String | Yes | 从 file item 复制的相对 source path。 |
| `pdfPath` | String | No | 作为 parser input 的相对 PDF path；仅 skipped/ineligible report row 可为空。 |
| `sourceType` | Enum | Yes | 现有 `SourceType`；conversion 后预期为 `pdf`。 |
| `status` | Enum | Yes | 现有 `FileStatus` parser result。 |
| `markdownPath` | String | No | 相对生成 Markdown path。 |
| `assetsPath` | String | No | 相对抽取 assets path。 |
| `confidence` | Decimal | No | parser 可提供时为 `[0,1]`。 |
| `adapterKey` | String | Yes | 产出结果的 adapter key。 |
| `chunkCount` | Integer | Yes | 该文件接受的 source chunks 数量。 |
| `skipped` | Boolean | Yes | 当该行是 skipped/ineligible report row 且 file item 未传给 adapter 时为 `true`。 |
| `safeError` | String | No | 仅脱敏错误摘要。 |
| `createdAt` | Timestamp | Yes | Server timestamp。 |

## 枚举复用

### `ParserRunStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### File Status 复用

Parser result mapping 只使用现有 `FileStatus`：

```text
MARKDOWN_GENERATED | LOW_CONFIDENCE | OCR_REQUIRED | FAILED | UNSUPPORTED
```

`PDF_CONVERTED` 是符合条件的输入状态。

## 关系

```text
batch 1 ---- N parser_run
parser_run 1 ---- N parser_file_result
file_item 1 ---- N parser_file_result
file_item 1 ---- N source_chunk
file_item 1 ---- latest status/markdownPath/assetsPath/error fields updated by parser result
```

## 不变量

- `parser_file_result.sourcePath`、`pdfPath`、`markdownPath`、`assetsPath` 在存在时均为相对且无 traversal。
- `parser_file_result.status` 使用现有 `FileStatus`；本切片不扩展 file enum。Skipped rows 保留未变化的当前 file status，并设置 `skipped=true`。
- `parser_run.summary` 从相关 result rows（包含 skipped/ineligible report rows）派生；若重新计算不一致，不独立信任。
- Raw parser logs、raw local paths、secrets、hostnames 与 source document content 永不存入 `safeMessage` 或 `safeError`。
- `file_item.review_status` 对 generated/low-confidence/OCR-required output 保持 `REVIEW_REQUIRED`，除非后续 review slice 改变。
- `source_chunk.review_status` 默认 `REVIEW_REQUIRED`。
- 本切片不触碰 `wiki_page` rows。

## API Shape Mapping

| API Field | Entity Field | Notes |
|---|---|---|
| `runId` | `parser_run.id` | create 与 detail endpoints 返回。 |
| `adapterKey` | `parser_run.adapterKey`、`parser_file_result.adapterKey` | 必须匹配已注册 adapter。 |
| `results[].fileId` | `parser_file_result.fileItemId` | 现有 file item id。 |
| `results[].markdownPath` | `parser_file_result.markdownPath`、`file_item.markdown_path` | 仅相对路径。 |
| `results[].assetsPath` | `parser_file_result.assetsPath`、`file_item.assets_path` | 存在时仅相对路径。 |
| `results[].status` | `parser_file_result.status`、`file_item.status` | 现有 `FileStatus`。 |
| `results[].safeError` | `parser_file_result.safeError`、`file_item.error_message` | 仅脱敏内容。 |
| `chunks[]` | `source_chunk` rows | 仅校验成功后创建。 |

## 延后数据

- Object storage byte hashes、signed URLs、copy/delete operations 属于 `storage-adapter`。
- Wiki page publication metadata 属于 `review-publish`。
- Graph node/edge projections 属于 `knowledge-graph`。
- Ask/RAG indexing metadata 属于 `ask-rag` 或 vector/model adapter slices。
