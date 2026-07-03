# 规格说明：Storage Adapter

## 状态

草稿。Phase 3 adapter 切片。`storage-adapter` 的行为唯一事实来源。源自 `docs/02-user-stories/storage-adapter-stories.md`。

## 来源文档

- `docs/01-requirements/storage-adapter-requirements.md`
- `docs/02-user-stories/storage-adapter-stories.md`
- `docs/03-spec/converter-adapter-spec.md`
- `docs/03-spec/parser-adapter-spec.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/architecture.md`
- `docs/batch-processing-design.md`

## 范围

Atlas 必须通过产品侧 storage adapter 契约，为批次产物提供对象存储。本切片包含 storage capability 行为、store/get/list/delete/exists 操作、工作区分层策略、stored-object descriptor、storage operation record、metadata 指针写回、mock-engine 验证和 adapter seam guard。它不实现真实 S3/MinIO 执行、presigned URL、保留/生命周期策略、converter/parser 执行、LLM/OCR/图谱/Ask、Wiki publication 语义、前端 UI、生产认证/RBAC，也不实现 vector/model adapter。

## 约束

- **仅 adapter：** 产品 workflow code 依赖 storage 接口与 registry 契约，而不直连 S3 SDK、MinIO client 或文件系统对象存储（REQ-SA-001）。
- **无单一硬编码实现：** S3 兼容存储是首个目标，但必须通过 adapter 配置与 capability metadata 可替换（REQ-SA-002）。
- **mock-engine 验证：** 自动化测试必须使用 mock/内存存储引擎，不得要求真实 endpoint、真实 bucket 或云凭证（REQ-SA-012）。
- **secret/路径安全：** 所有 object key 与 layer 前缀都是安全相对路径；不返回或记录原始 endpoint、bucket、region、凭证、SDK 输出、stack trace 或私有绝对路径（REQ-SA-009、REQ-SA-010）。
- **保留 trace/review：** storage 操作保留 source path、source type、converter/parser metadata、confidence 与 review status；存储内容绝不自动 approve（REQ-SA-011、REQ-SA-014）。
- **无外部云调用：** 本切片不引入外部网络依赖或云服务。

## 参与者

| 参与者 | 角色 |
|---|---|
| 知识库管理员 | 触发或监控批次产物的 storage 操作。 |
| 平台管理员 | 查看 storage adapter 可用性与脱敏配置。 |
| 交付负责人 | 使用 storage operation record 与列表审计产物分层。 |
| SME reviewer | Review 时把 file metadata 追溯到已存储对象。 |
| Codex 实现 agent | SDD 验收后严格按本 spec 与任务清单实现。 |

## 功能需求

### Adapter 边界

- **FR-SA-001：** storage workflow 必须在任何对象操作前，通过 registry/capability 契约解析 storage adapter。（US-SA-001）
- **FR-SA-002：** 非 adapter 产品层不得为存储引用 S3 SDK、MinIO client、文件系统对象存储或 outbound HTTP/S3 client。（US-SA-001、US-SA-005）
- **FR-SA-003：** Adapter registry 必须支持至少一个已配置默认 storage adapter，并安全暴露不可用/配置错误状态。（US-SA-002）

### Capability Metadata

- **FR-SA-004：** Capability metadata 必须包含 adapter key、display name、version、支持的 layers、default marker、health/status 与脱敏配置摘要。（US-SA-002）
- **FR-SA-005：** Capability metadata 不得暴露原始 endpoint、bucket 名称、region、access key、secret、hostname、原始本地路径或私有 endpoint。（US-SA-002）

### Storage 操作

- **FR-SA-006：** Store（put）操作必须接受 workspace/batch scope、目标 layer、安全相对 object key、content type 与 content reference，并返回 stored-object descriptor。（US-SA-001、US-SA-004）
- **FR-SA-007：** Get/exists 操作必须按 layer 与 key 解析对象，返回安全 descriptor 或安全 not-found 结果。（US-SA-001）
- **FR-SA-008：** List 操作必须在 workspace/batch/layer 前缀内以有界、分页方式枚举对象。（US-SA-003）
- **FR-SA-009：** Delete 操作必须按 layer 与 key 删除对象、返回安全结果，且不得越出 workspace/batch 命名空间。（US-SA-005）
- **FR-SA-010：** Storage operation record 必须保留 adapter key、operation type、layer、对象数量、总字节数、结果计数、时间戳与 user-safe 摘要。（US-SA-004）

### 分层与命名空间策略

- **FR-SA-011：** 对象必须存放在不同 layer 前缀：`raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki`。（US-SA-003）
- **FR-SA-012：** Layer + key 必须解析为 workspace/batch 命名空间内单一安全相对路径；跨 layer 或跨命名空间目标被拒绝。（US-SA-003、US-SA-005）
- **FR-SA-013：** 未知或不支持的 layer 必须在任何 storage 操作前校验失败。（US-SA-003）

### Metadata 写回与 Descriptor

- **FR-SA-014：** 成功 store 必须通过 metadata 边界，将返回的 storage key/descriptor 记录到正确的 `file_item` 产物字段（或 report/wiki 指针）。（US-SA-004）
- **FR-SA-015：** Stored-object descriptor 必须记录 object key、layer、content type、size、checksum、adapter key、status 与时间戳，且不含原始存储内部信息。（US-SA-004）
- **FR-SA-016：** Storage 写回必须保留 source path、source type、confidence 与 review status，除非某 workflow 明确映射变更。（US-SA-004）

### 校验与失败行为

- **FR-SA-017：** Storage 操作必须在写入或删除任何对象前，拒绝绝对路径、URI 前缀路径、盘符前缀路径、traversal 路径、越出命名空间目标与未知 layer。（US-SA-005）
- **FR-SA-018：** Storage 错误摘要必须脱敏，去除原始 SDK 输出、stack trace、secret、endpoint、bucket ARN、hostname 与绝对/私有路径。（US-SA-005）
- **FR-SA-019：** 意外 adapter 故障必须返回 user-safe error，并保持 metadata 不变，除非存在安全的逐对象失败结果。（US-SA-005）
- **FR-SA-020：** Storage operation record 必须汇总 succeeded、failed、skipped 对象结果。（US-SA-004）

## 非功能需求

| 类别 | 需求 |
|---|---|
| 安全 | 响应或持久化安全消息中不含原始 endpoint、bucket、region、access key、secret、hostname、私有路径、stack trace 或原始 SDK 输出。 |
| 可靠性 | mock/内存引擎测试覆盖 store、get/exists、list 分页、delete、unsafe key 拒绝、未知 layer、越出命名空间目标、adapter 不可用与 adapter 故障场景。 |
| 可扩展性 | Storage adapter 接口允许未来存储实现（文件系统、其他 S3 兼容引擎）而不改动产品 workflow 调用方。 |
| 可审计性 | Storage operation record 与 stored-object descriptor 可追溯到 workspace/batch/file id、adapter identity、layer、key、size 与 checksum。 |
| 数据安全 | 仅使用 mock/示例内容；测试 fixture 中不含真实公司文档、metadata 里的原始文档字节、私有路径或外部云调用。 |

## 工作流

```text
+--------------------------+
| 批次/文件 metadata       |
| 生成的产物               |
+------------+-------------+
             |
             v
+--------------------------+       不可用/配置错误
| 解析 storage adapter     |------------------------------+
+------------+-------------+                              |
             | 可用                                       v
             v                                    +----------------+
+--------------------------+                      | 安全运行错误   |
| 校验 layer + keys        |                      | 无不安全泄露   |
+------------+-------------+                      +----------------+
             |
             v
+--------------------------+
| 执行 storage adapter     |
| CI 中 mock/内存          |
+------------+-------------+
             |
             v
+--------------------------+
| descriptor + 汇总        |
| 指针写回                 |
+------------+-------------+
             |
             v
+--------------------------+
| 持久化 descriptor +      |
| storage operation record |
+--------------------------+
```

## 状态模型

### Storage Operation Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`：adapter 解析成功且输入校验通过。
- `RUNNING -> SUCCEEDED`：所有请求对象安全 store/delete。
- `RUNNING -> PARTIAL_FAILED`：至少一个对象成功且至少一个失败或被跳过。
- `RUNNING -> FAILED`：没有请求对象成功，或 adapter 级失败导致无法产生逐对象结果。

### Stored-Object Status

| 结果 | 对象状态 | 说明 |
|---|---|---|
| 对象成功存储 | `STORED` | 需要安全 layer + key 与 descriptor。 |
| 对象成功删除 | `DELETED` | 需要安全 layer + key。 |
| get/exists 未找到对象 | `MISSING` | 安全 not-found 结果；无错误泄露。 |
| 对象操作失败 | `FAILED` | 需要 user-safe error 摘要。 |
| 不安全/非法目标 | 拒绝 | 写入/删除前校验错误；无状态变更。 |

## 校验规则

- 当请求指针写回时，workspace、batch、file id 必须引用现有 metadata 记录。
- Layer 必须是 `raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki` 之一。
- Object key 与解析后的 layer 路径必须是 workspace/batch 命名空间内的安全相对路径。
- Delete 与 get 操作不得解析到请求命名空间之外。
- Size 值（若有）必须非负；`STORED` descriptor 的 checksum 必须非空。
- List 操作必须受 page size 限制约束，并支持 page/continuation marker。
- 错误摘要必须有界、user-safe，并去除原始 SDK 输出、stack trace、私有路径、endpoint、bucket 与 secret。
- Adapter key 必须解析到已注册 adapter，否则以 user-safe 不可用/配置错误状态失败。

## API / 接口面

完整契约见 `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md`。

| 接口 | 行为 |
|---|---|
| `GET /api/storage-adapters` | 以脱敏配置列出已配置 storage adapter 能力。 |
| `POST /api/batches/{batchId}/storage-operations` | 为批次产物执行 store/delete 操作集并记录结果。 |
| `GET /api/storage-operations/{operationId}` | 返回 storage operation 汇总、descriptor 与逐对象结果。 |
| `GET /api/batches/{batchId}/storage-objects` | 在 workspace/batch/layer 前缀内列出 stored-object descriptor（有界、分页）。 |
| 内部 storage adapter 接口 | 在产品侧契约后存储/读取/列举/删除对象；测试需要 mock/内存实现。 |

## 验收矩阵

| 检查 | 需求 | 可观测结果 |
|---|---|---|
| AC-SA-01 | REQ-SA-001、REQ-SA-012 | 静态 guard 测试在 adapter 实现之外出现直连存储引擎引用时失败。 |
| AC-SA-02 | REQ-SA-002、REQ-SA-003 | Capability 契约返回脱敏 adapter metadata 与可替换 default marker。 |
| AC-SA-03 | REQ-SA-005、REQ-SA-006 | mock store 后 get 为安全 layer + key 返回 stored-object descriptor。 |
| AC-SA-04 | REQ-SA-004、REQ-SA-007、REQ-SA-009 | 对象放入正确 layer 前缀；list 有界/分页；unsafe key 被拒绝。 |
| AC-SA-05 | REQ-SA-008 | Delete 按 layer + key 删除对象且绝不解析到命名空间之外。 |
| AC-SA-06 | REQ-SA-011、REQ-SA-014 | 指针写回把 key 记录到正确 `file_item` 字段并保留 review status。 |
| AC-SA-07 | REQ-SA-012 | `cd backend && mvn verify` 仅使用 mock/内存存储引擎即通过。 |
| AC-SA-08 | REQ-SA-013 | Storage operation record 包含所有必需结果计数与总字节数。 |
| AC-SA-09 | REQ-SA-010 | 错误响应/日志断言显示无原始 secret、endpoint、bucket、私有路径或 SDK 输出。 |

## 范围外

- 超出 adapter seam contract 的真实 S3/MinIO 执行、presigned URL、保留/生命周期策略与 bucket 创建。
- Converter/parser 执行、OCR、LLM enrichment、图谱派生、Ask/RAG 与 Wiki publication 语义。
- Vector 与 model adapter 行为。
- 前端界面或设置 UI 更新。
- 生产认证/RBAC 与 secret manager 集成。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-SA-001 | S3 兼容对象存储 vs 本地文件系统 adapter 作为首个真实引擎。 | 影响部署拓扑，不影响当前 mock-engine 契约。 |
| OQ-SA-002 | 对象字节是否经过 API，还是仅交换 descriptor。 | 本 SDD 提交为 API 仅返回 descriptor；若需要字节透传，请在实现前确认。 |
| OQ-SA-003 | Presigned/短时访问 URL 推迟到后续交付/访问切片。 | 影响访问流程，不影响 storage 结果契约。 |
