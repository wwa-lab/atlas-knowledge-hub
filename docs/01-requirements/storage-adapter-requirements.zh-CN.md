# 需求：Storage Adapter

## 状态

草稿。Phase 3 adapter 切片。本轮仅产出 SDD，不实现产品代码。

## 切片契约

- **目标：** Atlas 可以通过产品侧 storage adapter，存储和读取按工作区分层的产物（原始输入、生成 PDF、Markdown、抽取 assets、批次报告、已发布 Wiki），且产品 workflow 不耦合到 S3/MinIO 或任何单一对象存储引擎。
- **切片：** `storage-adapter`
- **阶段：** 3 adapter
- **来源：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/00-context/slice-roadmap.md`、`docs/01-requirements/requirement.md`、`docs/architecture.md`、`docs/batch-processing-design.md`、`docs/03-spec/converter-adapter-spec.md`、`docs/03-spec/parser-adapter-spec.md`、`docs/04-architecture/converter-adapter-data-model.md`，以及 grounding 时核对过的现有 backend metadata 实体。
- **验证行：** Phase 3 adapter 需要针对 mock engines 的单元测试 + 集成测试。
- **硬约束：** Parser/converter/model/vector/storage 只能走产品侧 adapter；禁止直连工具；不得硬编码单一实现；secret 和私有路径必须脱敏；必须保留 source trace、confidence、review status。

## 范围内

- 面向产品的 storage adapter 契约，覆盖对象 put/get/list/delete/exists 行为，并返回 stored-object descriptor。
- 默认 S3 兼容 storage adapter 的 capability metadata，并只暴露脱敏配置（endpoint、bucket、region、凭证仅显示状态，绝不显示原始值）。
- 工作区分层策略：原始输入、生成 PDF、Markdown、assets、reports、已发布 Wiki 分别存放在不同 layer 前缀下。
- workspace/batch 命名空间下的安全相对 object-key 规则。
- Stored-object descriptor：object key、layer、content type、size、checksum、status、安全错误和时间戳。
- 汇总 store/delete/list 结果的 storage operation record，用于审计。
- Metadata 指针写回：通过 metadata 边界，将返回的 storage key 记录到正确的 `file_item` 产物字段（或 report/wiki 指针）。
- Mock/内存存储引擎、单元/集成测试，以及 adapter seam guard 更新。
- 面向实现的内部 API 与 adapter contract guide。

## 排除项

- 超出 adapter seam contract 的真实 S3/MinIO/云对象存储执行、真实 bucket 创建、对真实 endpoint 签发 presigned URL、生命周期/保留策略、加密密钥管理、CDN 及跨区域复制。
- Converter/parser 执行、OCR、LLM enrichment、图谱抽取、Ask/RAG、Wiki publication 语义、vector/model adapter、前端界面、生产认证/RBAC、生产 secret manager 集成。
- 真实公司文档、私有路径、原始日志、凭证、外部云调用或外部网络依赖。

## 需求

| ID | 需求 | 优先级 | 来源 / 理由 |
|---|---|---|---|
| REQ-SA-001 | 产品 workflow code 必须通过 storage adapter registry 解析对象存储；adapter 范围外不得直连 S3 SDK、MinIO client、文件系统对象存储或 outbound HTTP/S3 client。 | Must | REQ-PROD-015、REQ-PROD-017 |
| REQ-SA-002 | S3 兼容对象存储必须作为一个可替换 storage adapter 表示，而不是唯一实现；未来文件系统或其他 S3 兼容引擎必须可配置。 | Must | REQ-PROD-017、REQ-PROD-060、REQ-PROD-061 |
| REQ-SA-003 | Storage capability metadata 必须暴露 adapter key、display name、version/status、支持的 layers、default marker 和脱敏配置摘要；endpoint、bucket、region、凭证必须仅显示状态（`configured`），绝不显示原始值。 | Must | Adapter Standards、REQ-PROD-049、REQ-PROD-058、REQ-PROD-061 |
| REQ-SA-004 | Adapter 必须强制工作区分层：原始输入、生成 PDF、Markdown、assets、reports、已发布 Wiki 存放在不同 layer 前缀下，不得混放。 | Must | REQ-PROD-014、Workspace Separation |
| REQ-SA-005 | Store（put）操作必须接受 workspace/batch scope、目标 layer、安全相对 object key、content type 和 content reference，并返回包含 key、layer、size、checksum、content type、时间戳的 stored-object descriptor。 | Must | REQ-PROD-014、REQ-PROD-018 |
| REQ-SA-006 | Retrieve（get）/exists 操作必须按 layer + key 解析对象，返回安全 descriptor 或安全 not-found 结果，不得暴露原始存储内部信息。 | Must | REQ-PROD-014 |
| REQ-SA-007 | List 操作必须在 workspace/batch/layer 前缀内以有界、分页的方式枚举对象。 | Should | REQ-PROD-013、REQ-PROD-014 |
| REQ-SA-008 | Delete 操作必须按 layer + key 删除对象、返回安全结果，且绝不越出 workspace/batch 命名空间。 | Must | Metadata/API safety |
| REQ-SA-009 | Storage key 与 layer 前缀必须是安全相对路径；绝对路径、盘符/URI/host 前缀和 traversal 必须在任何存储操作前被拒绝。 | Must | Security/Data Standards |
| REQ-SA-010 | Storage failure 必须返回脱敏的 user-safe error，不得包含原始 SDK 输出、stack trace、secret、endpoint、bucket ARN、凭证、hostname 或私有/绝对路径。 | Must | Security/Data Standards |
| REQ-SA-011 | Metadata 指针写回必须通过 metadata 边界，将返回的 storage key/descriptor 记录到正确的 `file_item` 产物字段或 report/wiki 指针，并保留 source trace、confidence、review status。 | Must | REQ-PROD-014、Trace/review preservation |
| REQ-SA-012 | 自动化验证必须使用 mock/内存存储引擎，不得要求真实 S3/MinIO endpoint、真实 bucket、外部网络调用或云凭证。 | Must | Phase 3 verification row |
| REQ-SA-013 | Storage operation record 必须汇总 operation type、layer、对象数量、总字节数、succeeded/failed/skipped 结果和安全消息，用于审计。 | Should | REQ-PROD-013、REQ-PROD-018 |
| REQ-SA-014 | 本切片必须保留现有产物 metadata 与 review status；存储或删除对象不得静默更改 file status、confidence 或 review status，除非某 workflow 明确映射。 | Must | Trace/review preservation |

## 验收

- Requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability 均有完整双语 SDD 产物。
- 英文与简体中文副本的 REQ/US/T ID 完全一致。
- Tasks 映射到 requirement IDs 和 spec sections，并包含确切验证命令。
- 本 Phase 3 adapter 切片定义内部 API/adapter contracts，因此包含 API guide。
- 本轮 SDD pass 不改产品代码。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-SA-001 | 首个真实存储引擎应仅为 S3 兼容对象存储（MinIO/S3），还是应先交付本地文件系统 adapter 以支持本地/隔离网部署？ | 部署细节；不阻塞 mock adapter contract。 |
| OQ-SA-002 | 对象字节是否应经过 metadata API，还是 adapter 始终以 content reference/流交换、使 API 层只处理 descriptor？ | 本 SDD 提交为 API 仅返回 descriptor；字节留在 adapter 之后。若需要字节透传 endpoint，请在实现前确认。 |
| OQ-SA-003 | Presigned/短时访问 URL 应纳入本切片，还是留给后续交付/访问切片？ | 本 SDD 推迟 presigned URL 签发；仅脱敏 capability 与 descriptor 在范围内。 |
