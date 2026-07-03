# 数据模型：Converter Adapter

## 状态

草稿。Phase 3 adapter 切片。以 conversion execution evidence 扩展 metadata model。

## 复用现有实体

| Entity | 现有契约 | 本切片用途 |
|---|---|---|
| `batch` | 拥有 file items 和 processing run context。 | Conversion run 归属于 batch。 |
| `file_item` | 保存 `source_path`、`source_type`、`status`、`confidence`、`review_status`、`pdf_path`、`error_message`。 | Converter 通过 metadata boundary 更新 status/PDF/error 字段。 |
| `source_chunk` | Parser trace records。 | converter-adapter 不创建。 |

## 新逻辑实体

### `conversion_run`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | 不透明 run id。 |
| `batchId` | String | Yes | 现有 batch id。 |
| `adapterKey` | String | Yes | 已注册 adapter key，例如 `trinity-office`。 |
| `adapterVersion` | String | No | 可用时的 adapter-reported version。 |
| `status` | Enum | Yes | `REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。 |
| `requestedBy` | String | No | 内部 actor/team。 |
| `startedAt` | Timestamp | Yes | Server timestamp。 |
| `completedAt` | Timestamp | No | Terminal state 后的 server timestamp。 |
| `summary` | Object | Yes | 按 outcome 的计数；无原始 engine output。 |
| `safeMessage` | String | No | 用户安全 run summary 或 error。 |

### `conversion_file_result`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | 不透明 result id。 |
| `runId` | String | Yes | 所属 conversion run。 |
| `fileItemId` | String | Yes | 现有 file item id。 |
| `sourcePath` | String | Yes | 从 file item 复制的相对 source path。 |
| `sourceType` | Enum | Yes | 现有 `SourceType`。 |
| `status` | Enum | Yes | 现有 `FileStatus` result。 |
| `pdfPath` | String | No | 相对 generated/pass-through PDF path。 |
| `confidence` | Decimal | No | adapter 可提供时位于 `[0,1]`。 |
| `adapterKey` | String | Yes | 产生结果的 adapter key。 |
| `safeError` | String | No | 仅清洗后的错误摘要。 |
| `createdAt` | Timestamp | Yes | Server timestamp。 |

## Enums

### `ConversionRunStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### File Status Reuse

本切片复用现有 file status 集合。不得引入新的 file lifecycle state。

```text
PDF_CONVERTED | PDF_CONVERT_FAILED | OCR_REQUIRED | FAILED | UNSUPPORTED
```

其他现有状态在更广泛模型中仍有效，但 converter result mapping 使用上方子集。

## 关系

```text
batch 1 ---- N conversion_run
conversion_run 1 ---- N conversion_file_result
file_item 1 ---- N conversion_file_result
file_item 1 ---- latest status/pdfPath/error fields updated by conversion result
```

## 不变量

- `conversion_file_result.sourcePath` 和 `pdfPath` 必须是相对路径且无目录穿越。
- `conversion_file_result.status` 使用现有 `FileStatus`；本切片不扩展 enum。
- `conversion_run.summary` 由关联 result rows 派生；若与重算结果不一致，不应独立信任。
- `safeMessage` 或 `safeError` 中绝不存储原始命令输出、原始本地路径、secret 或源文档内容。
- 对生成/派生转换输出，`file_item.review_status` 保持 `REVIEW_REQUIRED`，除非后续 review slice 改变。
- 本切片不触碰 `source_chunk`。

## API Shape Mapping

| API Field | Entity Field | Notes |
|---|---|---|
| `runId` | `conversion_run.id` | create 和 detail endpoints 返回。 |
| `adapterKey` | `conversion_run.adapterKey`, `conversion_file_result.adapterKey` | 必须匹配已注册 adapter。 |
| `results[].fileId` | `conversion_file_result.fileItemId` | 现有 file item id。 |
| `results[].pdfPath` | `conversion_file_result.pdfPath`, `file_item.pdf_path` | 仅相对路径。 |
| `results[].status` | `conversion_file_result.status`, `file_item.status` | 现有 `FileStatus`。 |
| `results[].safeError` | `conversion_file_result.safeError`, `file_item.error_message` | 仅清洗内容。 |

## 延后数据

- Object storage location 和 byte hash 属于 `storage-adapter`。
- Parser chunks、Markdown paths 和 asset paths 属于 `parser-adapter`。
- Review publish timestamps 属于 `review-publish`。
