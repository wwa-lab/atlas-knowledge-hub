# 用户故事：secret-manager-integration

状态：SDD 已按预授权接受，可进入实现
最后更新：2026-07-07

## US-SECRET-MANAGER-INTEGRATION-001：安全查看 Secret 状态

作为平台管理员，
我希望 provider 与 adapter settings 只展示 configured/missing/masked 状态，
以便在不查看 raw credential 或 private infrastructure 的情况下了解就绪度。

### 验收标准

1. Given model、runtime、storage 或 vector capability data 存在，when 管理员读取 API 或 UI，then 只能看到状态和 reference metadata。
2. Given secret 来自 runtime 或 environment state，when API 序列化 response，then raw value、endpoint value、command path 与 private path 均不存在。
3. Given adapter disabled 或缺少配置，when UI 展示状态，then 只显示安全的 missing/disabled 状态。

依赖：现有 model/parser/converter/storage/vector capability endpoints。
范围外：读取真实 secret manager。
开放问题：本 goal 边界内无。

## US-SECRET-MANAGER-INTEGRATION-002：替换或清除模型 Credential

作为平台管理员，
我希望替换或清除 model provider credential 时后端不回显 secret，
以便 Atlas 后续能更安全地接入 secret manager。

### 验收标准

1. Given request 包含 replacement secret material，when 保存后，then response 只包含 `CONFIGURED` 类状态与 secret reference。
2. Given update 未提供 replacement secret material，when 已存在 runtime 或 environment credential，then Atlas 保留 effective credential status。
3. Given 执行 clear，when runtime configuration 被移除，then response fallback 到 environment configured 或 missing status，不暴露值。

依赖：现有 `/api/model-configurations/deepseek` endpoints。
范围外：持久化加密存储或 rotation automation。
开放问题：本 goal 边界内无。

## US-SECRET-MANAGER-INTEGRATION-003：保留 Adapter 边界

作为实现维护者，
我希望通过产品 DTO 与 adapter capability summary 表示 secret metadata，
以便 parser、converter、model、storage 与 vector engines 保持可替换。

### 验收标准

1. Given 任意 capability endpoint，when 返回 configuration status，then 使用共享 secret status DTO shape。
2. Given product service 调用 adapters，when 执行发生，then raw secret resolution 保持在 adapter/provider 边界内。
3. Given tests 检查 responses，when 输入或 environment 中有 raw-looking values，then serialized read responses 不包含这些值。

依赖：adapter registries 与 capability mappers。
范围外：改变 engine execution 语义。
开放问题：本 goal 边界内无。

## US-SECRET-MANAGER-INTEGRATION-004：验证无泄露

作为 security reviewer，
我希望测试与扫描覆盖 secret-bearing configuration 行为，
以便 closeout 不只依赖人工查看。

### 验收标准

1. Backend unit/API tests 验证 save/read/clear redaction 和 capability status DTO。
2. Frontend tests 验证 settings surface 只渲染 masked/status-only values。
3. Secret/private-path scans 与 `git diff --check` 在 closeout 前通过。

依赖：现有 backend 与 frontend test suites。
范围外：provider-backed live connection verification。
开放问题：本 goal 边界内无。
