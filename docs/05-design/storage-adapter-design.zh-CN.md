# 详细设计：Storage Adapter

## 状态

草稿。Phase 3 adapter 切片。源自 `docs/03-spec/storage-adapter-spec.md` 与 `docs/04-architecture/storage-adapter-architecture.md`。

## 来源架构

Storage-adapter 是后端/API + adapter contract 切片。它在 metadata 控制平面上新增 storage capability/operation 行为，同时把存储引擎细节保留在 adapter 实现内部。设计刻意沿用 converter-adapter 与 parser-adapter 的 capability/run/report 形状，使 Codex 能以最少的新产品概念实现下一个 adapter 切片。

## Grounded 现有代码上下文

设计前已核对以下锚点：

| 现有元素 | 已核对锚点 | Storage-adapter 用途 |
|---|---|---|
| File 产物指针字段 | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` | 复用 `pdfPath`、`markdownPath`、`assetsPath`、`errorMessage` 作为经 metadata 边界更新的指针。 |
| 产物写入辅助方法 | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:85` | `setArtifacts(...)` 是保留 review 的指针写回路径；storage 期间不得改动 status/confidence。 |
| Review 保留范式 | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:92` | Storage 写回必须像 conversion 写入一样保留 review status。 |
| 相对路径校验器 | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:1` | 复用于 object-key 与 layer-path 校验。 |
| 相对路径注解 | `backend/src/main/java/com/atlas/metadata/validation/RelativePath.java:1` | 复用于请求 DTO 的 key 字段。 |
| Converter capability record | `backend/src/main/java/com/atlas/metadata/adapter/ConverterCapability.java:9` | 为 `StorageCapability` 沿用形状。 |
| Converter adapter status 枚举 | `backend/src/main/java/com/atlas/metadata/enums/ConverterAdapterStatus.java:1` | `StorageAdapterStatus` 的范式。 |
| Converter API 范式 | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:21` | Storage controller 应沿用 capability/create/get 形状。 |
| API envelope | `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:1` | 所有 storage 响应使用 `ApiEnvelope`。 |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | **必须更新**：adapter 范围断言 `.doesNotContain("S3")`（第 42 行）——对 storage adapter 包放宽，并将存储引擎名称加入非 adapter 禁用列表。 |

## 设计范围

### 范围内

- Storage adapter 领域契约、capability metadata、request/descriptor 形状。
- Storage operation 持久化与逐对象 descriptor 持久化。
- Storage service 行为：目标校验、adapter 解析、layer/key 解析、执行、descriptor 校验、汇总计算、安全错误处理、指针写回。
- Storage API guide 与 contract 测试。
- 用于确定性测试的 mock/内存 storage adapter。

### 范围外

- 真实存储运行拓扑、presigned URL、保留/生命周期策略、converter/parser 执行、LLM/OCR、前端 UI、图谱/Ask、Wiki publication、vector/model adapter、生产认证/RBAC。

## 模块设计

### Storage Adapter 契约

概念契约：

```text
StorageAdapter
  capability() -> StorageCapability
  put(StoragePutRequest) -> StorageObjectDescriptor
  get(StorageObjectRef) -> StorageObjectDescriptor
  exists(StorageObjectRef) -> boolean
  list(StorageListRequest) -> StorageListResult
  delete(StorageObjectRef) -> StorageDeleteResult
```

`StorageCapability` 包含：

- adapter key 与 display name
- 安全 version/status
- 支持的 layers：`RAW`、`PDF`、`MARKDOWN`、`ASSETS`、`REPORTS`、`WIKI`
- default marker
- 脱敏配置摘要（endpoint/bucket/region/凭证仅显示状态）

`StoragePutRequest` 包含：

- operation id
- batch/workspace scope
- layer
- 安全相对 object key
- content type
- content reference（流/句柄；metadata 中绝不含原始字节）

`StorageObjectDescriptor` 包含：

- layer、object key
- size、checksum、content type
- adapter key、status、安全错误

`StorageListRequest` 包含：

- batch/workspace scope、layer 前缀
- page size 限制与 page/continuation marker

### Storage Service

职责：

- 校验 batch/workspace 及任何目标 file id。
- 校验每个操作：layer 归属、安全相对 key、命名空间归属。
- 按显式 key 或 default marker 解析 adapter。
- 创建 storage operation、标记 running、执行 adapter、持久化已校验 descriptor。
- 通过 `setArtifacts(...)` 式更新将返回的 key 写回正确的 `file_item` 指针字段，保留 status/confidence/review status。
- 计算汇总（计数 + 总字节数）与终态。
- 持久化/响应前脱敏安全消息与安全错误。

### Storage Summary Calculator

汇总从持久化的 stored-object descriptor 派生：

| 计数 | 规则 |
|---|---|
| `total` | 所有请求操作。 |
| `stored` | 状态 `STORED`。 |
| `deleted` | 状态 `DELETED`。 |
| `missing` | 状态 `MISSING`。 |
| `failed` | 状态 `FAILED`。 |
| `skipped` | 在 adapter 执行前被拒绝/不合格的操作。 |
| `totalBytes` | `STORED` descriptor 的 `sizeBytes` 之和。 |

### Storage 持久化

新增与数据模型等价的逻辑领域实体：

- `StorageOperation`
- `StorageObject`
- `StorageOperationType`、`StorageOperationStatus`、`StorageObjectStatus`、`StorageLayer`
- `StorageAdapterStatus` 仿照 converter status 命名范式。

仅为 storage operation/object 持久化与现有 file 指针更新使用 repository。`wiki_page` 保持不变。

### Storage Mapper / DTO

DTO 应匹配 API guide：

- `StorageCapabilityResponse`
- `CreateStorageOperationRequest`
- `StorageOperationResponse`
- `StorageObjectResponse`
- `StorageOperationSummaryResponse`
- `StorageObjectListResponse`（分页）

所有 API 响应使用 `ApiEnvelope`。

## API / 接口设计

API 实现 guide 对 payload 具权威性：

- `GET /api/storage-adapters`
- `POST /api/batches/{batchId}/storage-operations`
- `GET /api/storage-operations/{operationId}`
- `GET /api/batches/{batchId}/storage-objects`

校验失败使用现有 API envelope/错误处理风格。本切片认证保持推迟/仅内部。

## 数据设计

数据模型见 `docs/04-architecture/storage-adapter-data-model.md`。

关键不变量：

- 无新 `FileStatus` 值；storage 不改变 file status/confidence/review status。
- Object key 与 layer 路径必须在命名空间内通过相对路径校验。
- `source_chunk` 与 `wiki_page` 行保持不变。
- Storage operation 汇总从 descriptor 行派生。
- `STORED` descriptor 必须有 `checksum`。

## 工作流 / 执行设计

### 成功 Store

1. API 接收 storage operation 请求。
2. Service 校验 batch/workspace 与 file id。
3. Service 校验每个操作（layer、安全 key、命名空间）。
4. Registry 解析 storage adapter。
5. Service 创建 storage operation 并标记 `RUNNING`。
6. Adapter 存储每个对象并返回 descriptor。
7. Service 校验每个 descriptor。
8. Service 持久化 `storage_object`、更新 `file_item` 指针、持久化 operation record。
9. Service 计算汇总与终态。
10. API 返回 storage operation 响应。

### Adapter 不可用

- Storage operation 变为 `FAILED`。
- File item 指针保持不变。
- 响应仅含安全消息。

### 不安全操作

- 校验在写入或删除任何对象前失败。
- 响应使用校验错误形状。
- 无部分写入：将操作集判为非法拒绝；默认不改动 metadata。

## 校验与错误处理

| 场景 | 期望处理 |
|---|---|
| 未知 batch | 404 安全 not found。 |
| 未知目标文件（指针写回） | 404 安全 not found。 |
| 跨 batch file id | 400 校验错误或 404 安全 not found；不泄露无关 batch 归属。 |
| 未知/不支持 layer | 400 校验错误，带安全字段消息。 |
| 不安全 key（绝对/URI/盘符/traversal） | 持久化前 400 校验错误。 |
| 越出命名空间目标 | 400 校验错误；不触碰任何对象。 |
| Adapter 抛运行时异常 | Storage operation `FAILED`；指针不变；脱敏安全消息。 |
| Storage 输出含原始 endpoint/bucket/secret | 仅存储脱敏 `safeError`/`safeMessage`。 |

## 边界场景追踪

### Layer 解析

规则：`layer + objectKey` 在命名空间内解析为单一安全相对路径。

| 输入 | 结果 |
|---|---|
| `MARKDOWN` + `batch-001/BRD.md` | 接受 -> `markdown/batch-001/BRD.md` |
| `PDF` + `../raw/BRD.docx` | 拒绝（traversal） |
| 未知 layer `TEMP` | 拒绝（未知 layer） |

### Key 安全

规则：object key 必须是安全相对 key。

| 输入 | 结果 |
|---|---|
| `batch-001/report.json` | 接受 |
| `/var/data/report.json` | 拒绝 |
| `s3://bucket/report.json` | 拒绝 |
| Windows 盘符前缀 key | 拒绝 |

### Descriptor 安全

规则：`STORED` descriptor 需要 checksum 与非负 size。

| 输入 | 结果 |
|---|---|
| `STORED`，size `1024`，checksum `sha256:...` | 接受 |
| `STORED`，size `1024`，checksum 空 | 拒绝 |
| `STORED`，size `-1` | 拒绝 |

## Seam Guard 更新（必须）

`AdapterSeamGuardTest` 当前断言 adapter 包 `.doesNotContain("S3")`（第 42 行）。Storage adapter 合法引用 S3 兼容引擎概念，因此实现必须：

- 放宽 adapter 范围断言，使 storage adapter 包可包含存储引擎名称（仿照 adapter 范围现有的 `trinity-office` 允许）。
- 将存储引擎名称与 SDK 类型（如 `AmazonS3`、`software.amazon.awssdk`、`MinioClient`、`S3Client`）加入非 adapter 的 `ENGINE_REFERENCES` 禁用列表，使其在 adapter 包外仍被禁止。
- 在非 adapter 层继续禁止 `WebClient`、`RestTemplate`、`HttpClient`。

在 tasks 与 traceability 中记录此变更，使 guard 更新是刻意而非偶然。

## 测试考量

必需实现验证：

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

测试覆盖必须包含：

- 使用 mock storage 引擎的 adapter contract 测试（store/get/exists/list/delete）。
- Summary calculator 单元测试。
- Service 校验、layer 解析与命名空间 scope 测试。
- 断言 review status/confidence/status 被保留的指针写回测试。
- API contract 集成测试。
- 存储引擎引用的 seam guard 测试更新。
- Secret/路径脱敏测试。

## 风险 / 设计权衡

| ID | 风险 / 权衡 | 决定 |
|---|---|---|
| DT-SA-001 | 现有 seam guard 在 adapter 范围禁止 `S3`。 | 更新 guard，仅在 storage adapter 包内允许存储引擎名称，其余处禁止。 |
| DT-SA-002 | 通过 API 传字节会膨胀/泄露。 | API 仅返回 descriptor；字节保留在 adapter 之后（OQ-SA-002）。 |
| DT-SA-003 | 逐对象非法结果可能被部分持久化。 | 默认在持久化前拒绝操作集，保持 metadata 干净。 |
| DT-SA-004 | Storage 可能意外改变文件生命周期状态。 | Storage 写回仅更新指针字段；status/confidence/review status 被保留。 |

## 待确认问题

- OQ-SA-001：首个真实存储引擎（S3 兼容 vs 文件系统）。
- OQ-SA-002：对象字节是否经过 API。
- OQ-SA-003：presigned/短时访问 URL 的推迟。
