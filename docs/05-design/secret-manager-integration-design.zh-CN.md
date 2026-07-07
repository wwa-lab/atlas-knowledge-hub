# 设计：secret-manager-integration

状态：已按预授权接受
最后更新：2026-07-07

## 设计范围

在现有 configuration responses 上实现 typed status layer。设计保持 additive 与兼容。

## 后端设计

- 在 backend DTO 下增加 `SecretReferenceResponse` 与 `SecretStatusResponse` records。
- 如有帮助，增加小型 helper/factory，将 summary map entries 转换成 status DTO。
- 扩展 model 与 adapter capability response records，增加 `List<SecretStatusResponse> secretStatuses`。
- 保留现有 `maskedConfigSummary` maps。
- 更新 `ModelRuntimeConfigurationService`，为 credential 与 endpoint 返回 secret status entries。
- 更新 capability mappers/adapters，使 parser/converter/storage/vector/model 暴露 typed status entries。

## 前端设计

- 扩展 TypeScript API types，增加 secret reference/status types。
- 优先使用 `secretStatuses` 渲染 model credential 与 adapter settings。
- 保持现有 settings 文案和布局风格。
- 不在 frontend persisted state 中保存 raw keys；transient editor input 只在本地，保存/关闭后清理。

## API / Interface Design

不新增 endpoint。现有 endpoints 返回 additive fields。写入 request 保持当前 endpoint，key material 仅作为 replacement input。

## 错误处理

- Validation errors 只命名 invalid fields，不回显 values。
- Adapter failures 继续返回 sanitized messages。
- UI failure copy 保持通用且安全。

## 测试

- Backend unit tests：read/save/clear status 以及 response 中无 raw values。
- Backend API contract tests：capability 与 configuration endpoints 包含 typed secret statuses，且排除 raw values。
- Frontend tests：model settings 渲染 status-only secret labels，并发送 replacement，不在 UI 回显。

## 风险

- In-memory runtime configuration 不是 production secret storage。traceability 必须记录此残余风险。
- 现有 request field name 为兼容性保留；本切片将其作为 write-only，但不做破坏性 payload rename。
