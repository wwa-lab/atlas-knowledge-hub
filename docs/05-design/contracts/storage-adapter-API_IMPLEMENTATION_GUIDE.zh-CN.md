# Storage Adapter — API / Adapter 实现 Guide

## 状态

草稿。Phase 3 storage-adapter 实现前必需。切片 `storage-adapter`。

## 概述

本 guide 定义批次产物对象存储的内部 API 与 adapter 契约。API 为 Atlas 内部；它不暴露原始对象字节、原始 SDK 输出、原始 endpoint/bucket/凭证配置、外部云调用或私有运行路径。

## 基础约定

- Base path：`/api`。
- Envelope：复用 `ApiEnvelope`。
- 仅 JSON。对象字节绝不经过 API；adapter 以 content reference/流交换。
- Auth/RBAC：推迟；本切片仅内部。
- Key：仅相对；拒绝 traversal、盘符前缀、URI 前缀、host 前缀与私有绝对路径。
- Secret：脱敏/仅状态；绝不返回原始 endpoint、bucket、region、access key、凭证、hostname 或私有路径。
- Mode：测试用 `mock`；已配置运行行为保留在 adapter 之后。

## Adapter Capability 契约

### `GET /api/storage-adapters`

用途：列出已配置 storage adapter。

响应：

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "s3-compatible",
      "displayName": "S3-Compatible Object Storage",
      "version": "configured",
      "supportedLayers": ["raw", "pdf", "markdown", "assets", "reports", "wiki"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "maskedConfigSummary": {
        "endpoint": "configured",
        "bucket": "configured",
        "region": "configured",
        "credentials": "configured",
        "externalNetwork": "disabled"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

`status` 取值：`AVAILABLE`、`DISABLED`、`MISCONFIGURED`。

校验/安全：

- 不返回原始 endpoint、bucket 名称、region、access key、凭证或绝对路径。
- `version` 可为安全 adapter 版本或 `configured`；不得仅为在 capability listing 期间发现版本而调用存储引擎。

## 创建 Storage Operation

### `POST /api/batches/{batchId}/storage-operations`

用途：通过 storage adapter 契约为批次产物执行 store/delete 操作集。

请求：

```json
{
  "adapterKey": "s3-compatible",
  "requestedBy": "delivery-lead",
  "mode": "mock",
  "operations": [
    {
      "operationType": "STORE",
      "layer": "markdown",
      "objectKey": "batch-2026-001/BRD.md",
      "contentType": "text/markdown",
      "fileId": "file-001"
    },
    {
      "operationType": "DELETE",
      "layer": "pdf",
      "objectKey": "batch-2026-001/OBSOLETE.pdf"
    }
  ]
}
```

规则：

- `adapterKey` 可选；省略表示默认 storage adapter。
- `mode` 可选，且必须为 `mock` 或 `configured`；测试用 `mock`。
- `operations[].layer` 必须是 `raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki` 之一。
- `operations[].objectKey` 必须是 workspace/batch 命名空间内的安全相对 key。
- `operations[].fileId` 可选；存在时触发指针写回，且必须引用 batch 中的文件。
- 内容字节通过 content reference/流带外提供，绝不内联于 JSON。
- Service 在启动运行前校验所有操作。

成功响应：

```json
{
  "success": true,
  "data": {
    "operationId": "storage-op-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "s3-compatible",
    "operationType": "STORE",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock storage completed with one failed object.",
    "summary": {
      "total": 2,
      "stored": 1,
      "deleted": 0,
      "missing": 0,
      "failed": 1,
      "skipped": 0,
      "totalBytes": 20480
    },
    "objects": [
      {
        "layer": "markdown",
        "objectKey": "batch-2026-001/BRD.md",
        "contentType": "text/markdown",
        "sizeBytes": 20480,
        "checksum": "sha256:configured",
        "adapterKey": "s3-compatible",
        "status": "STORED",
        "fileId": "file-001",
        "safeError": null
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:02Z"
  },
  "error": null,
  "meta": null
}
```

错误场景：

| HTTP | Code | 何时 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 未知 adapter key、unsafe key、未知 layer、非法 mode、越出命名空间目标、跨 batch file id、非法 descriptor。 |
| 404 | `NOT_FOUND` | Batch 或 file id 未知。 |
| 409 | `CONFLICT` | 因 batch 已有活跃 storage operation 而无法启动。 |
| 500 | `INTERNAL_ERROR` | 意外 adapter 故障；细节仅在服务端安全记录。 |

## 获取 Storage Operation

### `GET /api/storage-operations/{operationId}`

用途：返回 storage operation 汇总、逐对象 descriptor 与结果。

响应形状：与创建 storage operation 的 `data` 体相同。

## 列出 Storage Objects

### `GET /api/batches/{batchId}/storage-objects`

用途：在 workspace/batch/layer 前缀内列出 stored-object descriptor。

查询参数：

| 参数 | 必填 | 描述 |
|---|---|---|
| `layer` | 否 | 按 `StorageLayer` 值过滤；省略则列出所有 layer。 |
| `pageSize` | 否 | 有界 page size；服务端强制上限。 |
| `pageToken` | 否 | 下一页的 continuation marker。 |

响应：

```json
{
  "success": true,
  "data": {
    "objects": [
      {
        "layer": "markdown",
        "objectKey": "batch-2026-001/BRD.md",
        "contentType": "text/markdown",
        "sizeBytes": 20480,
        "checksum": "sha256:configured",
        "adapterKey": "s3-compatible",
        "status": "STORED",
        "fileId": "file-001"
      }
    ]
  },
  "error": null,
  "meta": {
    "pageSize": 50,
    "nextPageToken": null
  }
}
```

## 内部 Adapter 接口契约

概念接口：

```text
StorageAdapter
  capability() -> StorageCapability
  put(StoragePutRequest) -> StorageObjectDescriptor
  get(StorageObjectRef) -> StorageObjectDescriptor
  exists(StorageObjectRef) -> boolean
  list(StorageListRequest) -> StorageListResult
  delete(StorageObjectRef) -> StorageDeleteResult
```

必需 put-request 字段：

| 字段 | 描述 |
|---|---|
| `operationId` | 服务器创建的 storage operation id。 |
| `batchId` | 现有 batch id。 |
| `layer` | 目标 `StorageLayer`。 |
| `objectKey` | 安全相对 object key。 |
| `contentType` | user-safe content type。 |
| `contentRef` | 内容的流/句柄；metadata 中绝不含原始字节。 |

必需 descriptor 字段：

| 字段 | 描述 |
|---|---|
| `layer` | 对象存放的 layer。 |
| `objectKey` | 相对 object key。 |
| `sizeBytes` | `STORED` 的非负字节大小。 |
| `checksum` | `STORED` 时非空。 |
| `contentType` | user-safe content type。 |
| `adapterKey` | 产生 descriptor 的 adapter。 |
| `status` | `STORED` / `DELETED` / `MISSING` / `FAILED`。 |
| `safeError` | 脱敏错误摘要。 |

## 状态映射

| 来源 / 结果 | 对象状态 |
|---|---|
| 对象成功存储 | `STORED` |
| 对象成功删除 | `DELETED` |
| get/exists 未找到对象 | `MISSING` |
| 对象操作失败 | `FAILED` |
| 不安全/非法目标 | 写入/删除前拒绝，报告为 skipped |

本切片不允许新 `FileStatus` 值；storage 操作只更新产物指针字段。

## 契约测试

实现完成前运行：

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

Seam 扫描在非 adapter 产品层必须无匹配。Storage adapter 包仅在被更新后的 guard 测试覆盖时可包含存储引擎名称，且 outbound 网络 client 不得泄漏到产品层。
