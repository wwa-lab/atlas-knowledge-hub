# 详细设计：Converter Adapter

## 状态

草稿。来源于 `docs/03-spec/converter-adapter-spec.md` 和 `docs/04-architecture/converter-adapter-architecture.md`。

## 设计范围

范围内：

- Converter adapter interface 和 registry。
- 脱敏 capability metadata。
- Conversion run 创建、状态转换、report retrieval 和 result persistence。
- 通过 metadata boundary 更新 file item status/PDF/error。
- 测试用 mock/fake adapter implementation。
- 配置门控的可选 `trinity-office` wrapper；CI 不要求真实 binary。
- Static seam guard 更新。

范围外：

- Parser、OCR、storage bytes、vector/model、Wiki publish、graph、Ask、前端 UI、生产 auth/RBAC。

## 模块设计

### Converter API Module

职责：

- `GET /api/converter-adapters`：返回已注册 converter capabilities。
- `POST /api/batches/{batchId}/conversion-runs`：通过 adapter contract 创建并执行 conversion run。
- `GET /api/conversion-runs/{runId}`：返回 run summary 和逐文件 report。
- 返回现有 `ApiEnvelope` 形状和用户安全错误。

Controller 不得引用 `trinity-office`、命令执行 API、文件字节或外联 HTTP client。

### Converter Application Module

职责：

- 校验 batch 存在和 target file ids。
- 按 requested key 或 default adapter 解析 adapter。
- 创建 `REQUESTED` 状态的 `conversion_run`，切换到 `RUNNING`，再进入 terminal state。
- 只调用 `ConverterAdapter` interface。
- 将 adapter results 映射到现有 `FileStatus`。
- 持久化前清洗 error 并校验相对 artifact path。
- 更新 file item conversion metadata，并持久化 conversion result records。

### Converter Adapter Registry

职责：

- 从配置注册 converter adapters。
- 以稳定形状返回 capability metadata。
- 将 adapter status 标记为 `AVAILABLE`、`DISABLED` 或 `MISCONFIGURED`。
- 只提供脱敏配置摘要。

### Converter Adapter Interface

Adapter interface 面向产品，使用 Atlas domain terms。

必要概念：

- **Capability：** adapter key、display name、version、supported source types、output type、default marker、status、masked config。
- **Request：** run id、batch id、file item descriptors、可用时的 artifact root hint 和 correlation metadata。
- **Result：** 逐文件 status、可用时的相对 PDF path、可用时的 confidence、安全 error 和 adapter identity。

Interface 不得向产品层暴露 vendor-specific command flags。

### Trinity-office Adapter Boundary

具体 `trinity-office` adapter 可把 Atlas request 翻译为 command invocation，但只能在 adapter implementation code 内部完成。它必须：

- 未提供必要配置时保持 disabled。
- 必要配置缺失时返回 `MISCONFIGURED` capability status。
- 将 command output 清洗为有边界的安全错误。
- 不记录包含 secret 或私有路径的原始 command args。
- 可由其他 adapter 替换，而无需修改 controller 或 metadata service。

### Mock/Fake Converter Adapter

测试必须包含 mock/fake adapter。它必须确定性支持：

- Office success 到 `PDF_CONVERTED`。
- Office failure 到 `PDF_CONVERT_FAILED`。
- PDF pass-through 到 `PDF_CONVERTED`。
- Image 到 `OCR_REQUIRED`。
- Unsupported 到 `UNSUPPORTED`。
- Misconfigured adapter state。
- 在 service boundary 拒绝 unsafe path。

## API / Interface Design

完整 endpoint 和 payload 形状见 `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`。

实现规则：

- 使用现有 envelope conventions。
- 使用现有 `FileStatus`、`SourceType` 和 `ReviewStatus`。
- 不新增前端面向 routes。
- 不暴露原始 adapter config。

## 数据设计

使用 `docs/04-architecture/converter-adapter-data-model.md` 中的逻辑数据模型。

实现说明：

- 先添加 conversion run 和逐文件 result persistence，再启用 report retrieval。
- Conversion result 作为 evidence record 保存；不要只依赖可变的 `file_item` latest fields。
- `conversion_run.summary` 必须可由 result rows 重算。
- 现有 `file_item` 仍是产品可见最新状态承载者。

## Workflow / Execution Design

1. Request 携带 `batchId`、可选 `adapterKey`、可选 target `fileIds` 和 `requestedBy` 到达。
2. Service 校验 batch 和 target file metadata。
3. Service 解析 adapter。
4. 若 adapter unavailable/misconfigured，在可行时创建 failed run 并返回安全错误；file status 不变。
5. Service 将 run 转为 `RUNNING`。
6. Service 调用 `ConverterAdapter.convert(request)`。
7. Service 校验每个 result。
8. Service 持久化 result rows。
9. Service 更新 file item status、`pdfPath`、confidence 和安全 `errorMessage`。
10. Service 将 run 转为 `SUCCEEDED`、`PARTIAL_FAILED` 或 `FAILED`。
11. API 返回 run report。

## Validation And Error Handling

| Case | Behavior |
|---|---|
| Unknown batch | `404 NOT_FOUND` envelope。 |
| Unknown file target | 按现有 API pattern 返回 `400 VALIDATION_ERROR` 或 `404 NOT_FOUND`；不得部分启动 run。 |
| Adapter key unknown | `400 VALIDATION_ERROR`，带安全 adapter unavailable message。 |
| Adapter disabled/misconfigured | 可行时创建 failed run；返回用户安全错误。 |
| Unsafe `pdfPath` | 拒绝 result；记录安全 validation failure；不持久化 unsafe path。 |
| Raw/private error content | 持久化/响应前清洗。 |
| Adapter throws unexpected exception | Run 变为 `FAILED`；response 隐藏内部细节。 |

## Security / Audit / Reliability Design

- Configuration：使用外部化属性；capability response 中无原始 command path 或 secret。
- Audit：持久化 run id、adapter key、timestamps、result counts、target file ids 和安全 messages。
- Reliability：测试必须覆盖 adapter unavailable 和 partial failure。
- No-network：本切片 converter adapter 不得引入外部云/网络调用。
- Review safety：conversion 不设置 `APPROVED` 或 `PUBLISHED`。

## Testing Considerations

实现必需检查：

- result mapping、summary calculation、path validation、error sanitization 和 run state transition 的单元测试。
- 使用 mock/fake converter adapter 和 seeded PostgreSQL metadata 的集成测试。
- Capability、create run、get run endpoints 的 API contract tests。
- 静态 seam guard，证明 concrete engine references 只限 adapter implementation packages。
- 对 source、config、docs 和 tests 执行 secret/path scan。

## 风险 / 设计权衡

| 风险 | 设计响应 |
|---|---|
| 真实 command contract 未知。 | 先定义 interface 和 fake runner；真实 wrapper 保持配置门控。 |
| In-process execution 未必是最终 runtime。 | 保持 adapter boundary runtime-neutral。 |
| 现有 metadata API 没有 conversion-run records。 | 本切片添加显式 run/result model，用于 evidence 和 reports。 |

## 待确认问题

- OQ-CA-001：local process wrapper vs external worker。
- OQ-CA-002：PDF pass-through copy/reference policy。
- OQ-CA-003：确切 `trinity-office` command-line contract。
