# 需求：secret-manager-integration

状态：在完全落入 goal objective 边界内时，SDD 已按预授权接受，可进入实现。
最后更新：2026-07-07
阶段：Wave 3 / Trust And Governance

## 目标

将 Atlas 配置界面从 raw secret handling 推进到 secret-reference 与 masked-status 模型，覆盖模型供应商、parser/converter runtime、storage、vector 以及相邻 adapter 配置 metadata。API 和 UI 只能暴露状态、脱敏标签、替换/移除操作和未来 secret manager reference。

## 范围

- 增加产品面的 secret reference 与 secret status DTO。
- 保持当前 in-memory/runtime 配置 mock-safe。
- 保留 model、parser、converter、storage、vector 与 runtime 配置的 adapter 边界。
- Read API 只返回 configured/missing/disabled/env-configured 等状态。
- 更新 Vue settings/model/adapter 展示，消费 status-only 字段。
- 增加测试，证明 create/update/read 不返回 raw secret、private endpoint、private path、credential 或 raw runtime command。

## 排除项

- 不接入真实 secret manager、SSO/OIDC、自动轮换、SIEM export、生产监控或真实 provider connection test。
- 不改变 auth/RBAC 语义，只使用现有受保护 endpoint。
- 不新增 audit-log-foundation 之外的审计语义。
- 不引入外部云调用或真实公司配置。

## 需求

| ID | 需求 | 优先级 |
|---|---|---|
| REQ-SECRET-MANAGER-INTEGRATION-001 | 读取 provider/runtime/storage/vector 配置的 API 必须暴露 secret reference 与 masked status，不得暴露 raw value。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-002 | 写入 API 可以接收用于替换的 secret material，但必须 write-only，不能回显。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-003 | Model 配置必须支持 retain/replace/remove 风格，不返回 raw API key 或 endpoint。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-004 | Parser/converter runtime capability metadata 必须报告 command status，不能暴露 command value 或 local path。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-005 | Storage/vector adapter capability metadata 必须报告 credential/endpoint/collection 状态，不能暴露 raw infrastructure details。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-006 | 前端 settings 必须展示 configured/missing/replace/remove 状态，绝不显示 secret material。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-007 | 必须保留现有 adapter 边界；UI 或 product service 不得依赖具体 provider/engine secret format。 | Must |
| REQ-SECRET-MANAGER-INTEGRATION-008 | 测试和扫描必须证明 API response、UI source 与 fixture 不泄露 raw secret、token、password、credential、private endpoint、private path 或 runtime command。 | Must |

## 假设

- 现有 auth-space-rbac 与 audit-log-foundation 行为稳定，本切片不重新定义。
- Runtime 配置继续保持 local 与 mock-safe；生产 secret manager resolution 只作为 adapter boundary 表达。
- 引入 typed status 字段时保留现有 `maskedConfigSummary` 兼容性。

## SDD Skill Chain Evidence

SDD skill chain used: yes。已读取 `atlas-sdd-generate-all`、`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`、`review-doc-quality`、`architecture-review` 与 shared grounding rules。
