# 用户故事：Storage Adapter

## 状态

草稿。源自 `docs/01-requirements/storage-adapter-requirements.md`。

## 故事地图

| Story ID | 标题 | 需求 |
|---|---|---|
| US-SA-001 | 通过 adapter 边界存储与读取产物 | REQ-SA-001、REQ-SA-002、REQ-SA-005、REQ-SA-006、REQ-SA-012 |
| US-SA-002 | 安全地查看 storage 能力 | REQ-SA-002、REQ-SA-003 |
| US-SA-003 | 保持产物分层且安全 | REQ-SA-004、REQ-SA-007、REQ-SA-009 |
| US-SA-004 | 将产物指针写回 metadata | REQ-SA-005、REQ-SA-011、REQ-SA-013、REQ-SA-014 |
| US-SA-005 | 保护 storage 操作与 adapter seam | REQ-SA-001、REQ-SA-008、REQ-SA-009、REQ-SA-010、REQ-SA-012 |

## US-SA-001：通过 adapter 边界存储与读取产物

**故事**
作为知识库管理员，
我希望批次产物通过 storage adapter 存储和读取，
以便 Atlas 能持久化生成文件，而不把产品 workflow 硬耦合到单一对象存储引擎。

### 验收标准

1. **假设** 某批次已有生成产物（PDF、Markdown、assets、report）
   **当** 请求 storage 操作时
   **那么** Atlas 在写入任何对象前先解析已注册的 storage adapter。
2. **假设** 扫描非 adapter 产品层
   **当** adapter 包外出现直连 S3 SDK、MinIO client 或 outbound storage client 引用时
   **那么** seam guard 验证失败。
3. **假设** 在 CI 中运行测试
   **当** 验证 storage 行为时
   **那么** 使用 mock/内存存储引擎，不需要真实 S3/MinIO endpoint 或云凭证。

### 说明 / 假设

- 现有 converter/parser adapter 切片是 registry/capability/run 行为的范式。
- storage 实现在 Phase 2 metadata API 之后开始，当前 roadmap 已将其标记为已实现。

### 依赖

- Metadata API 的 file、batch 和产物路径记录。
- Converter/parser 产生的待持久化产物。

### 范围外

- 真实对象存储执行、presigned URL、保留/生命周期策略、前端 UI、图谱、Ask 与发布行为。

### 待确认问题

- OQ-SA-001：首个真实存储引擎（S3 兼容 vs 文件系统）在 mock 引擎验证之外仍待定。

## US-SA-002：安全地查看 storage 能力

**故事**
作为平台管理员，
我希望看到已配置 storage adapter 的能力与状态，
以便了解可用存储，而不暴露 endpoint、bucket 或凭证。

### 验收标准

1. **假设** 已配置某 storage adapter
   **当** 列出能力时
   **那么** 响应包含 adapter key、display name、version/status、支持的 layers、default marker 和脱敏配置。
2. **假设** adapter 被禁用或配置错误
   **当** 列出或选择能力时
   **那么** Atlas 仅暴露安全状态，不泄露原始 endpoint、bucket 名称、region、access key、secret、hostname 或绝对路径。

### 说明 / 假设

- S3 兼容存储是首个命名 adapter，但未来存储（文件系统、其他 S3 兼容引擎）必须可替换。

### 依赖

- Adapter registry 与 capability metadata 契约。

### 范围外

- 生产 secret-manager 集成。

### 待确认问题

- 无。

## US-SA-003：保持产物分层且安全

**故事**
作为交付负责人，
我希望原始输入、PDF、Markdown、assets、reports 与 Wiki 输出存放在不同分层，
以便产物保持有序、可审计且互不覆盖。

### 验收标准

1. **假设** 存储某产物
   **当** 校验目标 layer 与 object key 时
   **那么** 对象被放入 workspace/batch 命名空间内正确的 layer 前缀下。
2. **假设** object key 是绝对路径、URI 前缀、盘符前缀或包含 traversal
   **当** 校验 storage 操作时
   **那么** 操作在写入任何对象前被拒绝。
3. **假设** 对某 layer 前缀请求 list 操作
   **当** 返回结果时
   **那么** 结果有界、分页，并限定在请求的 workspace/batch/layer 范围内。

### 说明 / 假设

- Layers 为 `raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki`。
- 分层遵循 `PROJECT_RULES.md` 的 Workspace Separation 与 REQ-PROD-014。

### 依赖

- 相对路径校验规则。

### 范围外

- 跨工作区对象复制与复制策略。

### 待确认问题

- 无。

## US-SA-004：将产物指针写回 metadata

**故事**
作为 SME reviewer，
我希望已存储产物能从 file metadata 追溯到，
以便把每个生成文件追回其存储对象，而不丢失 review 上下文。

### 验收标准

1. **假设** store 操作成功
   **当** 写回 metadata 时
   **那么** 通过 metadata 边界，将返回的 storage key/descriptor 记录到正确的 `file_item` 产物字段或 report/wiki 指针。
2. **假设** 持久化某 stored-object descriptor
   **当** 检查它时
   **那么** 它记录 object key、layer、content type、size、checksum、adapter key、status 与时间戳，且不含原始存储内部信息。
3. **假设** 存储或删除某产物
   **当** 更新 metadata 时
   **那么** source path、source type、confidence 与 review status 被保留，除非某 workflow 明确映射变更。

### 说明 / 假设

- Storage-adapter 记录指针/descriptor，但不改变转换或 review 语义。
- 生成内容在后续 review/publish 流程改变前保持 review-required。

### 依赖

- 现有 `file_item` 产物路径字段与 review status 规则。

### 范围外

- Wiki 发布与图谱投影。

### 待确认问题

- OQ-SA-002：对象字节是否经过 API，还是仅交换 descriptor。

## US-SA-005：保护 storage 操作与 adapter seam

**故事**
作为实现负责人，
我希望 storage 操作与 adapter seam 受到保护，
以便畸形 key 或存储错误无法越出命名空间或泄露敏感信息。

### 验收标准

1. **假设** storage 操作含 unsafe key、越出命名空间的目标或未知 layer
   **当** Atlas 校验该操作时
   **那么** 以字段级校验安全失败，不写入或删除任何对象。
2. **假设** storage 错误含 SDK 输出、secret、endpoint 或私有路径
   **当** 返回或存储该错误时
   **那么** 仅保留有界的脱敏摘要。
3. **假设** 运行自动化验证
   **当** seam guard 与 secret 扫描执行时
   **那么** 非 adapter 产品层不含直连存储引擎调用，生成的 SDD/代码不含原始 secret、endpoint 或私有路径。

### 说明 / 假设

- 现有 `AdapterSeamGuardTest` 断言 adapter 源码 `.doesNotContain("S3")`；本切片必须在 storage adapter 包内放宽该断言，并将存储引擎名称加入非 adapter 禁用列表。
- 相对 key 校验应尽量复用现有 metadata 路径校验器。

### 依赖

- 现有相对路径校验器、API 错误 envelope 与 adapter seam guard 测试。

### 范围外

- 完整生产凭证管理。

### 待确认问题

- 无。
