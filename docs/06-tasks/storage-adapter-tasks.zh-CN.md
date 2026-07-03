# 任务：Storage Adapter

## 状态

`storage-adapter` 的草稿实现清单。本文档仅在 SDD 集被验收后可执行。本轮 SDD pass 不实现产品代码。

## 来源设计

- Spec：`docs/03-spec/storage-adapter-spec.md`
- Design：`docs/05-design/storage-adapter-design.md`
- API guide：`docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model：`docs/04-architecture/storage-adapter-data-model.md`

## 每个任务的约束

- 自动化测试使用 mock/内存存储引擎。
- 存储执行保持在产品侧 adapter 契约之后。
- 不得从 controller/service/repository/domain 层调用 S3 SDK、MinIO client、文件系统对象存储或 outbound HTTP client。
- 不引入外部网络依赖或云调用。
- 保留 source trace、confidence、review status；storage 不改变 `file_item` 的 status/confidence/review status。
- 脱敏 secret、endpoint、bucket、region、凭证、原始 SDK 输出、hostname、stack trace 与私有路径。
- 仅使用 mock/示例 metadata；绝不在 metadata 中持久化原始对象字节。

## 工作流

| 工作流 | 任务 |
|---|---|
| 领域与持久化 | T-SA-001、T-SA-002 |
| Adapter 契约 | T-SA-003、T-SA-004 |
| Service 行为 | T-SA-005、T-SA-006、T-SA-007 |
| API 契约 | T-SA-008 |
| 验证与 guard | T-SA-009、T-SA-010 |

## 任务细节

### T-SA-001：新增 storage operation 领域模型与 migration

- **映射到：** REQ-SA-004、REQ-SA-013、REQ-SA-014；spec 章节 “Storage Operations”“Layer And Namespace Policy”；data model `storage_operation`、`storage_object`。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** 无
- **范围：** 新增 `StorageLayer`、`StorageOperationType`、`StorageOperationStatus`、`StorageObjectStatus`、`StorageAdapterStatus` 枚举；`StorageOperation` 与 `StorageObject` 实体；repository；以及存储执行证据的 Flyway migration。不得新增 `FileStatus` 值。
- **约束：** Adapter 边界；仅 mock 测试数据；trace/review 保留；secret/路径安全。
- **验证：**

```bash
cd backend && mvn verify
git diff --check
```

### T-SA-002：新增 storage DTO 与映射契约

- **映射到：** REQ-SA-003、REQ-SA-013；spec 章节 “Capability Metadata”“API / Interface Surface”；API guide 响应形状。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-001
- **范围：** 新增 storage capability、create-operation 请求、operation 响应、summary 响应、object 响应与分页 object-list 响应映射。所有响应经 controller 使用 `ApiEnvelope`。
- **约束：** capability 摘要 secret 脱敏；无原始运行细节。
- **验证：**

```bash
cd backend && mvn test
git diff --check
```

### T-SA-003：定义 storage adapter 接口与 capability model

- **映射到：** REQ-SA-001、REQ-SA-002、REQ-SA-003；spec 章节 “Adapter Boundary”“Capability Metadata”；design “Storage Adapter Contract”。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-002
- **范围：** 新增产品侧 `StorageAdapter` 接口（`capability`、`put`、`get`、`exists`、`list`、`delete`）、capability record、put-request record、object-ref record、list-request record、descriptor record。包含 adapter key、安全 status、支持的 layers、default marker 与脱敏配置摘要。
- **约束：** 仅产品概念；接口中不直连存储引擎；无网络/client 依赖；内容经 reference 交换，绝不含原始字节。
- **验证：**

```bash
cd backend && mvn -Dtest=StorageAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter/StorageAdapter.java
```

### T-SA-004：新增 mock 与 configured storage adapter 实现

- **映射到：** REQ-SA-002、REQ-SA-012；spec 章节 “Adapter Boundary”“Storage Operations”；design “Storage Adapter Contract”。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-003
- **范围：** 新增用于 CI 的确定性 mock/内存 storage adapter，以及报告安全 capability metadata 的 configured S3 兼容 storage adapter 边界。真实存储执行保持在 adapter 之后，可在运行拓扑确定前作为安全占位。
- **约束：** 仅 mock 验证；无外部网络；capability 响应中无原始 endpoint/bucket/凭证/路径/secret。
- **验证：**

```bash
cd backend && mvn -Dtest=StorageAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-SA-005：实现 storage adapter registry 与 capability listing

- **映射到：** REQ-SA-001、REQ-SA-003；spec 章节 “Adapter Boundary”“Capability Metadata”；API guide `GET /api/storage-adapters`。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-003、T-SA-004
- **范围：** 新增对显式/默认 storage adapter key 的 registry 解析及不可用/配置错误状态。新增带脱敏配置的 capability listing service 行为。
- **约束：** Adapter 边界；secret 脱敏；不得把单一实现硬编码为唯一未来选项。
- **验证：**

```bash
cd backend && mvn -Dtest=StorageAdapterRegistryTest test
git diff --check
```

### T-SA-006：实现 storage operation service 与 layer/key 校验

- **映射到：** REQ-SA-004、REQ-SA-005、REQ-SA-006、REQ-SA-007、REQ-SA-008、REQ-SA-009、REQ-SA-013；spec 章节 “Storage Operations”“Layer And Namespace Policy”“Validation And Failure Behavior”；design “Storage Service”。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-001、T-SA-003、T-SA-005
- **范围：** 实现 storage operation 创建、layer 归属与安全 key 校验、命名空间 scope 强制、活跃运行冲突处理、adapter 执行（store/get/exists/list/delete）、descriptor 校验、汇总计算（计数 + 总字节数）与安全 adapter 故障处理。
- **约束：** 测试中 mock 引擎执行；持久化前拒绝 unsafe key 与越出命名空间目标；无外部网络。
- **验证：**

```bash
cd backend && mvn -Dtest=StorageServiceTest,StorageSummaryCalculatorTest test
git diff --check
```

### T-SA-007：实现 metadata 指针写回与 descriptor 持久化

- **映射到：** REQ-SA-010、REQ-SA-011、REQ-SA-014；spec 章节 “Metadata Write-Back And Descriptors”“Validation And Failure Behavior”；design “Data Design”。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-006
- **范围：** 持久化已校验 `storage_object` descriptor；经保留 review 的更新写回正确的 `file_item` 指针字段（`pdfPath`/`markdownPath`/`assetsPath`）；拒绝 unsafe key、未知 file id、跨 batch 结果、越出命名空间目标、负 size 与 `STORED` 的空 checksum。持久化前脱敏安全错误。
- **约束：** 保留 source trace、confidence、review status 与 file status；secret/路径脱敏；不创建 `wiki_page`。
- **验证：**

```bash
cd backend && mvn -Dtest=StorageDomainInvariantTest,StorageServiceTest test
git diff --check
```

### T-SA-008：新增 storage REST API endpoint

- **映射到：** REQ-SA-003、REQ-SA-004、REQ-SA-007、REQ-SA-013；spec 章节 “API / Interface Surface”；API guide 所有 endpoint。
- **负责人类型：** backend
- **优先级：** Must
- **依赖：** T-SA-005、T-SA-006、T-SA-007
- **范围：** 新增 storage controller endpoint `GET /api/storage-adapters`、`POST /api/batches/{batchId}/storage-operations`、`GET /api/storage-operations/{operationId}` 与 `GET /api/batches/{batchId}/storage-objects`（有界/分页）。响应必须使用 `ApiEnvelope` 并匹配 API guide。
- **约束：** 仅内部；不实现 auth/RBAC；无原始存储输出；对象字节绝不在 JSON 中返回。
- **验证：**

```bash
cd backend && mvn -Dit.test=StorageApiContractIT verify
git diff --check
```

### T-SA-009：更新 adapter seam 与安全 guard

- **映射到：** REQ-SA-001、REQ-SA-009、REQ-SA-010、REQ-SA-012；spec 章节 “Adapter Boundary”“Validation And Failure Behavior”；design “Seam Guard Update (Required)”。
- **负责人类型：** QA/backend
- **优先级：** Must
- **依赖：** T-SA-003、T-SA-004、T-SA-008
- **范围：** 更新 `AdapterSeamGuardTest`，使 adapter 范围断言允许 storage adapter 包内存在存储引擎名称（放宽当前 `.doesNotContain("S3")`），并将存储引擎名称与 SDK 类型（`AmazonS3`、`software.amazon.awssdk`、`MinioClient`、`S3Client`）加入非 adapter 的 `ENGINE_REFERENCES` 禁用列表。在 adapter 范围外继续禁止 `WebClient`/`RestTemplate`/`HttpClient`。为 storage 实现与文档新增 secret/私有路径扫描。
- **约束：** adapter 外无直连存储调用；仅 secret 脱敏输出。在 traceability 中刻意记录 guard 变更。
- **验证：**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

### T-SA-010：运行最终 storage-adapter 验证

- **映射到：** REQ-SA-012、REQ-SA-013、REQ-SA-014；spec 验收矩阵 AC-SA-01 至 AC-SA-09。
- **负责人类型：** QA/backend
- **优先级：** Must
- **依赖：** T-SA-001 至 T-SA-009
- **范围：** 运行完整验证集、审阅 diff，若后续实现改动文档/状态则以证据更新 traceability。
- **约束：** 仅 mock；仅 adapter；产品代码无外部网络/云调用；secret 脱敏；trace/review 保留；无新 `FileStatus`。
- **验证：**

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

## 依赖计划

- 关键路径：T-SA-001 -> T-SA-002 -> T-SA-003 -> T-SA-004 -> T-SA-005 -> T-SA-006 -> T-SA-007 -> T-SA-008 -> T-SA-009 -> T-SA-010
- T-SA-003 之后的并行机会：adapter contract 测试与 DTO mapper 测试可与 service 测试并行构建。

## 待确认问题 / 风险

- OQ-SA-001：首个真实存储引擎（S3 兼容 vs 文件系统）推迟；mock 契约不得依赖它。
- OQ-SA-002：API 仅返回 descriptor；若需要字节透传 endpoint，请在实现前确认。
- OQ-SA-003：presigned/短时访问 URL 推迟到后续交付/访问切片。
- R-SA-003 / DT-SA-001：现有 seam guard 在 adapter 范围禁止 `S3`；T-SA-009 必须刻意更新它，而非偶然。
