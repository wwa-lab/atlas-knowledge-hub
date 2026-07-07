# 功能规格：secret-manager-integration

> Source stories: US-SECRET-MANAGER-INTEGRATION-001 through 004
> Spec status: Preauthorized accepted
> Last updated: 2026-07-07

## 概览

Atlas 必须将 secret-bearing configuration 暴露为 status-only metadata 与 future-compatible secret references。本切片强化现有 model configuration 与 adapter capability contracts，不接入真实 secret manager，也不改变 auth/audit 语义。

## 功能范围

范围内：

- 共享 secret reference 与 secret status DTO。
- Model configuration read/save/clear response 包含 typed secret status。
- Model、parser、converter、storage、vector capability response 包含 typed secret status。
- Frontend settings 基于 configured/missing/disabled status 展示。
- Redaction 测试与扫描。

范围外：

- 生产 secret manager 接入、数据库加密存储、SSO/OIDC、新审计语义、rate limit、live provider test、部署、监控或轮换自动化。

## 功能需求

| ID | Requirement |
|---|---|
| FR-SECRET-MANAGER-INTEGRATION-001 | Read response 绝不能包含 raw API key、token、password、credential、private endpoint、private path、runtime command、hostname 或 stack trace。 |
| FR-SECRET-MANAGER-INTEGRATION-002 | `SecretReferenceResponse` 必须标识 logical provider/scope/key，但不能包含 secret material。 |
| FR-SECRET-MANAGER-INTEGRATION-003 | `SecretStatusResponse` 必须暴露 status、source、masked label、replaceable flag、removable flag 与 reference metadata。 |
| FR-SECRET-MANAGER-INTEGRATION-004 | Model save 接收 write-only replacement secret input，response 只返回 typed status 与 masked summary。 |
| FR-SECRET-MANAGER-INTEGRATION-005 | Model secret input 省略时，如存在 runtime/environment credential，应保留 effective status。 |
| FR-SECRET-MANAGER-INTEGRATION-006 | Clear model configuration 移除 runtime state，并 fallback 到 environment 或 missing status。 |
| FR-SECRET-MANAGER-INTEGRATION-007 | Runtime parser/converter capability metadata 只将 command 报告为 disabled/configured/missing。 |
| FR-SECRET-MANAGER-INTEGRATION-008 | Storage/vector capability metadata 只报告 endpoint/credential/collection status。 |
| FR-SECRET-MANAGER-INTEGRATION-009 | Frontend settings 只展示 configured/missing/masked 状态以及 replace/remove affordances。 |
| FR-SECRET-MANAGER-INTEGRATION-010 | 为兼容性保留现有 `maskedConfigSummary`，但 consumer 应优先使用 typed secret statuses。 |

## 非功能需求

- Security：secret material write-only；本切片新增的 response、log、docs example、test、UI source 中不得出现 raw secret。
- Adapter boundary：产品层依赖 Atlas DTO 与 adapter capabilities，不依赖 provider secret format。
- Verification：backend unit/API tests、frontend tests、typecheck/build、secret/private-path scans 与 closeout gate。
- Data safety：仅 mock/sample-safe；无外部调用。

## 工作流

1. 管理员打开 settings 或调用 configuration endpoints。
2. Backend 根据 runtime state 与 process environment 计算 effective configuration status。
3. Backend 返回 `SecretStatusResponse` objects 与兼容的 masked summary maps。
4. 管理员通过现有 model configuration endpoints 替换或清除 model credential state。
5. Backend 在当前进程内保存 runtime replacement，并返回 status-only state。
6. Adapter execution 继续只在现有 adapter boundary 内解析 raw values。

## API Surface

- Existing: `GET /api/model-adapters`
- Existing: `GET /api/model-configurations/deepseek`
- Existing: `PUT /api/model-configurations/deepseek`
- Existing: `DELETE /api/model-configurations/deepseek`
- Existing: `GET /api/converter-adapters`
- Existing: `GET /api/parser-adapters`
- Existing: `GET /api/storage-adapters`
- Existing: `GET /api/vector-adapters`

无需新增 public endpoint。

## 验收矩阵

| Acceptance | Requirements | Evidence |
|---|---|---|
| API responses 只暴露 secret reference/status | REQ-001, REQ-002, REQ-003, REQ-008 | Backend unit/API tests and scans |
| Frontend 不显示 raw secret | REQ-006, REQ-008 | Frontend component tests |
| Adapter boundaries 保持 | REQ-004, REQ-005, REQ-007 | Capability DTO mapping and existing seam tests |
| Verification 通过 | REQ-008 | Required commands and closeout gate |

## 开放问题

本 goal 无开放问题。生产 secret storage、rotation 与 secret-manager provider selection 保留为未来切片。
