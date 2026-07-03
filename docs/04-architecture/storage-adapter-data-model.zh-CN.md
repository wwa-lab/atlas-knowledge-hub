# 数据模型：Storage Adapter

## 状态

草稿。Phase 3 adapter 切片。以对象存储执行证据与 descriptor 扩展 metadata。

## 复用的现有实体

| 实体 | 现有契约 | 本切片用途 |
|---|---|---|
| `batch` | 拥有 file item 与处理运行上下文。 | Storage operation 以 batch（及其 workspace）为范围。 |
| `file_item` | 持有 `source_path`、`source_type`、`status`、`confidence`、`review_status`、`pdf_path`、`markdown_path`、`assets_path`、`error_message`。 | 产物路径字段被视为指针；storage 写回记录返回的 key，且不改变 review status/status/confidence。 |
| `wiki_page` | 推迟的 wiki page metadata 表。 | 本切片不创建；未来发布时可记录 wiki-layer 对象指针。 |

## 新增逻辑实体

### `storage_operation`

| 字段 | 类型 | 必填 | 描述 |
|---|---|---|---|
| `id` | String | 是 | 不透明 operation id。 |
| `batchId` | String | 是 | 现有 batch id。 |
| `workspaceId` | String | 否 | 可从 batch 解析时的现有 workspace id。 |
| `adapterKey` | String | 是 | 已注册 storage adapter key，如 `s3-compatible`。 |
| `adapterVersion` | String | 否 | adapter 报告的安全版本。 |
| `operationType` | Enum | 是 | `STORE`、`DELETE`、`LIST`（操作集意图）。 |
| `status` | Enum | 是 | `REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。 |
| `requestedBy` | String | 否 | 内部 actor/team。 |
| `mode` | String | 是 | `mock` 或 `configured`。测试用 `mock`。 |
| `startedAt` | Timestamp | 是 | 服务器时间戳。 |
| `completedAt` | Timestamp | 否 | 终态后的服务器时间戳。 |
| `summary` | Object | 是 | 按结果计数与总字节数；无原始存储输出。 |
| `safeMessage` | String | 否 | user-safe 操作汇总或错误。 |

### `storage_object`

| 字段 | 类型 | 必填 | 描述 |
|---|---|---|---|
| `id` | String | 是 | 不透明 descriptor id。 |
| `operationId` | String | 是 | 所属 storage operation。 |
| `batchId` | String | 是 | 现有 batch id。 |
| `fileItemId` | String | 否 | 对象映射到文件产物时的现有 file item id。 |
| `layer` | Enum | 是 | `RAW`、`PDF`、`MARKDOWN`、`ASSETS`、`REPORTS`、`WIKI`。 |
| `objectKey` | String | 是 | workspace/batch 命名空间内的安全相对 key。 |
| `contentType` | String | 否 | user-safe content type 标签。 |
| `sizeBytes` | Long | 否 | `STORED` descriptor 的非负字节大小。 |
| `checksum` | String | 否 | `STORED` descriptor 必须非空（如内容哈希）。 |
| `adapterKey` | String | 是 | 产生 descriptor 的 adapter key。 |
| `status` | Enum | 是 | `STORED`、`DELETED`、`MISSING`、`FAILED`。 |
| `safeError` | String | 否 | 仅脱敏错误摘要。 |
| `createdAt` | Timestamp | 是 | 服务器时间戳。 |
| `updatedAt` | Timestamp | 否 | 状态变更后的服务器时间戳。 |

## 新增枚举

### `StorageLayer`

```text
RAW | PDF | MARKDOWN | ASSETS | REPORTS | WIKI
```

### `StorageOperationType`

```text
STORE | DELETE | LIST
```

### `StorageOperationStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### `StorageObjectStatus`

```text
STORED | DELETED | MISSING | FAILED
```

### `StorageAdapterStatus`

```text
AVAILABLE | DISABLED | MISCONFIGURED
```

`StorageAdapterStatus` 仿照现有 `ConverterAdapterStatus` 命名范式；仅在跨 adapter 刻意引入时才复用共享的中立 adapter-status 枚举。

## File Status 说明

本切片**不引入新的 `FileStatus` 值**。Storage 操作不改变 `file_item.status`；只更新产物指针字段。现有 `FileStatus` 值仍归 converter/parser/review 切片所有。

## 关系

```text
batch 1 ---- N storage_operation
storage_operation 1 ---- N storage_object
file_item 0..1 ---- N storage_object   （经可选 fileItemId）
file_item 1 ---- pdf_path/markdown_path/assets_path 指针由 storage 写回更新
```

## 不变量

- `storage_object.objectKey` 与任何解析后的 layer 路径在 workspace/batch 命名空间内为相对且无 traversal。
- `storage_object.layer` 使用 `StorageLayer`；跨 layer 或跨命名空间目标被拒绝。
- `storage_object.status` 使用 `StorageObjectStatus`；本切片不扩展 `FileStatus`。
- `storage_object.sizeBytes` 非负；`STORED` descriptor 的 `checksum` 非空。
- `storage_operation.summary` 从关联 `storage_object` 行派生；若重算不一致，不独立采信。
- `safeMessage` 或 `safeError` 绝不存储原始存储 endpoint、bucket、region、凭证、SDK 输出、hostname 与私有绝对路径。
- 除非某 workflow 明确映射变更，`file_item.review_status`、`status` 与 `confidence` 不因 storage 操作改变。
- 本切片不触碰 `wiki_page` 行。

## API 形状映射

| API 字段 | 实体字段 | 说明 |
|---|---|---|
| `operationId` | `storage_operation.id` | 由 create 与 detail endpoint 返回。 |
| `adapterKey` | `storage_operation.adapterKey`、`storage_object.adapterKey` | 必须匹配已注册 adapter。 |
| `objects[].layer` | `storage_object.layer` | `StorageLayer` 值。 |
| `objects[].objectKey` | `storage_object.objectKey`、`file_item` 指针 | 仅相对。 |
| `objects[].sizeBytes` | `storage_object.sizeBytes` | 非负。 |
| `objects[].checksum` | `storage_object.checksum` | `STORED` 时存在。 |
| `objects[].status` | `storage_object.status` | `StorageObjectStatus`。 |
| `objects[].fileId` | `storage_object.fileItemId` | 可选现有 file item id。 |
| `objects[].safeError` | `storage_object.safeError` | 仅脱敏。 |
| `summary.totalBytes` | `storage_operation.summary` | 从对象行派生。 |

## 推迟数据

- 真实对象字节、presigned URL 与生命周期/保留 metadata 属于真实存储运行切片，不属于本契约。
- Wiki page publication metadata 属于 `review-publish`。
- Vector index metadata 属于 `vector-adapter`。
- Model/embedding metadata 属于 `model-adapter`。
- 图谱 node/edge 投影属于 `knowledge-graph`。
