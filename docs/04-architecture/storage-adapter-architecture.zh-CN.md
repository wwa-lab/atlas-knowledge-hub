# 架构：Storage Adapter

## 状态

草稿。Phase 3 adapter 切片。源自 `docs/03-spec/storage-adapter-spec.md`。

## 概述

Storage-adapter 在现有 metadata 控制平面上扩展一个可替换的对象存储 seam。后端拥有 storage operation 状态、key/layer 校验、operation record 与 metadata 指针写回；对象存储执行被隔离在产品侧 adapter 契约之后。S3 兼容存储是首个命名目标，但架构保持文件系统存储与其他 S3 兼容引擎可替换。

## 架构驱动力

| 驱动力 | 影响 |
|---|---|
| Adapter 中立 | 产品服务通过 registry/capability 契约解析 storage adapter。 |
| 分层隔离 | 原始输入、PDF、Markdown、assets、reports 与 Wiki 输出存放在不同 layer 前缀下。 |
| 保留 trace | stored-object descriptor 与指针写回携带 workspace/batch/file scope、layer、key 与 checksum，且不改变 review status。 |
| 仅 mock 验证 | 测试使用 mock/内存存储引擎，不需要真实 endpoint、bucket 或云凭证。 |
| secret/路径安全 | key 为相对路径；错误与 capability 摘要经脱敏。 |
| 阶段纪律 | Storage-adapter 存储/读取/列举/删除对象，但不发布 Wiki 页、不运行 converter/parser、不签发 presigned URL。 |

## 现有 Metadata 上下文

当前 metadata 控制平面已拥有带产物路径字段、review status、confidence、路径安全规则与 adapter seam guard 的 file item。Storage-adapter 应复用这些产品概念，并把现有产物路径字段视为指向已存储对象的指针，而不是新建独立的 storage 专属 metadata 孤岛。代码级 grounding 锚点记录在 design 与 traceability 产物中——面向实现的细节归属于那里。

## 系统上下文

| 边界 | 职责 |
|---|---|
| 前端 | 本切片范围外。现有 UI 未来可消费 storage capability/listing API，但前端改动不属于 storage-adapter。 |
| 后端 API / metadata 控制平面 | 拥有 storage capability endpoint、storage operation 生命周期、校验、descriptor 持久化、指针写回与列表。 |
| Storage adapter seam | 封装存储引擎特定执行，返回 Atlas 产品概念（descriptor）。 |
| 存储引擎 / worker | 位于产品 workflow 之外。S3/MinIO 或文件系统存储仅在 adapter 契约之后。 |
| PostgreSQL metadata | 存储 storage operation record、stored-object descriptor 与 file item 上的产物指针更新。 |

## 高层架构

```text
+------------------------------------------------------------+
| 用户 / agent                                               |
| 管理员、交付负责人、SME reviewer、Codex 实现                |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| storage capability endpoint、storage operation endpoint、   |
| storage object listing endpoint                             |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Storage application service                                |
| layer/key 校验、adapter 解析、descriptor 映射、             |
| 安全错误处理、metadata 指针写回                             |
+------------------------------+-----------------------------+
                               |
                   产品侧 adapter 接口
                               v
+------------------------------+        +---------------------+
| Storage adapter registry     |        | Storage adapters     |
| capability + 默认策略        |------->| s3 兼容存储          |
+------------------------------+        | mock/内存 测试       |
                                        +----------+----------+
                                                   |
                                                   | 引擎/worker 边界
                                                   v
                                        +---------------------+
                                        | 对象存储             |
                                        | 位于产品流之外       |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                        |
| file_item 指针、storage_operation、storage_object           |
+------------------------------------------------------------+
```

## 组件拆解

### 后端 API

- **Storage adapter capability API：** 列出 storage 能力与脱敏配置。
- **Storage operation API：** 为批次执行 store/delete 操作集并返回 operation record。
- **Storage object listing API：** 在 workspace/batch/layer 前缀内列出 stored-object descriptor。
- **File API 复用：** 现有 file API 仍是产物指针字段的读侧。

### 应用服务

- **Storage operation service：** 校验 workspace/batch/file 目标、layer 与 key；解析 adapter；执行 mock/configured 模式；映射 descriptor；持久化 operation record；写回指针。
- **Storage summary calculator：** 从 stored-object 结果派生总计（succeeded/failed/skipped、总字节数）。
- **Storage adapter registry：** 拥有默认 adapter 解析与不可用/配置错误行为。
- **Safety helpers：** 复用相对路径校验与安全错误脱敏范式；新增 layer 前缀解析。

### 集成 Adapter

- **StorageAdapter 契约：** 接受 Atlas storage 请求并返回 Atlas descriptor。
- **MockObjectStorageAdapter：** 用于 CI 与集成测试的确定性内存实现。
- **S3CompatibleStorageAdapter：** 真实 adapter 边界占位或已配置实现。它可以知道引擎细节（endpoint、bucket、SDK 类型），但产品层不得知道。

### 持久化

- 复用 `file_item` 产物路径字段（`pdf_path`、`markdown_path`、`assets_path`）作为指向已存储对象的指针。
- 新增 `storage_operation` 与 `storage_object` 逻辑实体，用于执行证据与 descriptor。
- 本切片不创建 `wiki_page` 行。

## 状态与状态策略

Storage operation 状态：

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Stored-object 状态映射：

- store 成功 -> `STORED`
- delete 成功 -> `DELETED`
- get/exists 未命中 -> `MISSING`
- 对象失败 -> `FAILED`
- 不安全/非法目标 -> 在写入/删除前拒绝

存储或删除对象不会改变 `file_item.review_status`、`status` 或 `confidence`，除非某 workflow 明确映射。

## API / 接口边界

| 接口 | 消费方 | 用途 |
|---|---|---|
| `GET /api/storage-adapters` | 管理员 / 实现测试 | 列出脱敏 storage 能力。 |
| `POST /api/batches/{batchId}/storage-operations` | 内部 workflow / 未来 UI | 为批次产物执行 store/delete 操作。 |
| `GET /api/storage-operations/{operationId}` | 交付负责人 / 未来 UI | 读取 operation record 与逐对象结果。 |
| `GET /api/batches/{batchId}/storage-objects` | 交付负责人 / 未来 UI | 按 layer 前缀列出 stored-object descriptor。 |
| `StorageAdapter` | Storage service | 在产品侧接口后执行 storage 工作。 |

## 安全 / 可靠性 / 可观测性

- Capability 响应为脱敏/仅状态。
- Storage 错误在持久化或响应前有界并脱敏。
- Object key 与 layer 路径在命名空间内被校验为安全相对路径。
- Descriptor 保留可审计证据（layer、key、size、checksum），且不在 metadata 或日志中嵌入原始文档字节。
- 自动化验证强制使用 mock/内存引擎。
- Adapter seam guard 必须扫描非 adapter 产品层是否有直连存储引擎、SDK 与 outbound client 引用；当前禁止 adapter 范围出现 `S3` 的断言必须仅对 storage adapter 包放宽。

## 风险 / 权衡

| ID | 风险 / 权衡 | 缓解 |
|---|---|---|
| R-SA-001 | 真实存储运行拓扑（S3 vs 文件系统）未定。 | 真实执行保持在 adapter 之后；mock 契约保持稳定。 |
| R-SA-002 | Storage 输出可能含原始 endpoint、bucket 或 SDK 错误。 | 持久化前校验 key 并脱敏安全消息。 |
| R-SA-003 | 现有 seam guard 在 adapter 范围禁止 `S3`，会阻断合法 S3 adapter。 | 更新 guard，仅在 storage adapter 包内允许存储引擎名称，仿照 `trinity-office` 例外。 |
| R-SA-004 | 通过 API 传递对象字节会膨胀响应并泄露内容。 | 提交为 API 仅返回 descriptor；字节保留在 adapter 之后。 |

## 待确认问题

- OQ-SA-001：首个真实存储引擎（S3 兼容 vs 文件系统）。
- OQ-SA-002：对象字节是否经过 API。
- OQ-SA-003：presigned/短时访问 URL 的推迟。
