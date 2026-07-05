# 用户故事：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## Story 1

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
**标题:** 实现前接受 runtime smoke 范围

**故事:**
作为产品负责人，
我希望 runtime smoke 配置与 runbook 范围在实现前被接受，
以便运维文档不会扩展到已批准 adapter-safe runtime 切片之外。

### 验收标准

1. **Given** 本 SDD set 仍为 Draft
   **When** implementation agent 准备编辑代码、测试或 runbook docs
   **Then** agent 必须停止并请求用户接受。

2. **Given** 用户接受 SDD
   **When** implementation 开始
   **Then** traceability 记录 acceptance，且 implementation 遵循 `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md`。

### 说明 / 假设

- 本故事映射 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 与 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012。

### 依赖

- 现有 `real-office-parser-runtime` implementation 与 traceability。

### 范围外

- 用户接受前的产品代码改动。

### 待确认问题

- 无。

## Story 2

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
**标题:** 配置 optional local runtime smoke checks

**故事:**
作为平台管理员，
我希望有精确的 smoke-test 配置契约，
以便 approved local runtime binaries 可以被检查，同时不改变默认 CI 路径。

### 验收标准

1. **Given** `ATLAS_RUNTIME_SMOKE_ENABLED` 缺失或不是 `true`
   **When** smoke tests 运行
   **Then** 它们 self-skip，且不要求 runtime binaries。

2. **Given** smoke 已启用但 command env var 缺失
   **When** 对应 smoke test 运行
   **Then** 它以安全 missing-command reason self-skip。

3. **Given** smoke 已启用且 approved command 存在
   **When** smoke test 执行
   **Then** 它使用配置的 smoke args，或默认使用 `--version`。

### 说明 / 假设

- 本故事映射 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 到 004，以及 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009。

### 依赖

- 现有 `ConfiguredRuntimeSmokeIT` 行为。

### 范围外

- 安装 runtime binaries。

### 待确认问题

- 无。

## Story 3

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003
**标题:** 捕获安全 runtime smoke evidence

**故事:**
作为交付负责人，
我希望 smoke evidence 能区分 skip、pass 与 fail，同时不泄露本地细节，
以便可以安全审阅 readiness。

### 验收标准

1. **Given** smoke check 通过
   **When** evidence 被记录
   **Then** 它说明 runtime family 与结果，不包含 raw command paths。

2. **Given** smoke check 失败
   **When** diagnostics 被捕获
   **Then** diagnostics 在报告前必须 bounded 并 sanitized。

3. **Given** smoke check timeout 或 non-zero exit
   **When** runbook 解释结果
   **Then** 它提供安全 troubleshooting 与 rollback steps。

### 说明 / 假设

- 本故事映射 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005、006、010 与 011。

### 依赖

- 现有 runtime output sanitizer 与 smoke test diagnostics。

### 范围外

- 将 smoke evidence 持久化到生产 audit tables。

### 待确认问题

- 无。

## Story 4

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004
**标题:** 提供 mock-safe runtime runbook

**故事:**
作为实现或运维工程师，
我希望有本地 runtime smoke setup 与 closeout 的 runbook，
以便 approved binaries 能用可重复命令和安全 fixtures 验证。

### 验收标准

1. **Given** runbook 被创建
   **When** 工程师阅读
   **Then** 它包含 setup、required env vars、run commands、expected outcomes、troubleshooting、disable/rollback 与 evidence checklist。

2. **Given** runbook 引用数据或 fixtures
   **When** verification 被执行
   **Then** 它只使用 mock/sample-safe inputs，并禁止真实公司文档。

### 说明 / 假设

- 本故事映射 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007、010 与 011。

### 依赖

- 如需要，由用户决定 runbook path。

### 范围外

- 生产部署运维。

### 待确认问题

- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001。

## Story 5

**ID:** US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005
**标题:** 在 smoke readiness 工作中保留 adapter boundaries

**故事:**
作为架构师，
我希望 runtime smoke readiness 保持在 adapter/runtime boundaries 后面，
以便 Atlas 继续 parser-neutral，product services 不会变成 tool runners。

### 验收标准

1. **Given** 实现发生改动
   **When** seam guard tests 运行
   **Then** direct process/runtime references 仍限制在允许的 adapter/runtime scope 内。

2. **Given** roadmap 与 traceability 被更新
   **When** 切片 close
   **Then** 状态语言说明仅为 runtime smoke readiness，不宣称 production operations readiness。

### 说明 / 假设

- 本故事映射 REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008、009 与 012。

### 依赖

- 现有 `AdapterSeamGuardTest`。

### 范围外

- 改变 core parser/converter adapter contracts。

### 待确认问题

- 无。
