# 系统架构：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 概述

- **架构摘要:** 本切片是在现有 real-runtime adapter implementation 之上的运维文档与验证层。它不增加新的 runtime engine、product API、database table 或 frontend surface。
- **设计目标:** 让 optional real-runtime smoke checks 有意启用、安全执行，并且不阻塞默认 CI。
- **架构风格:** 保留 adapter 边界的 operational readiness layer。

## 来源规格

- **Feature / System name:** Runtime Smoke Config And Runbook
- **范围摘要:** 为 configured local `trinity-office` 与 `document-normalize` smoke checks 定义 smoke env vars、runbook outcome interpretation、safe evidence 与 adapter-boundary verification。

## 架构驱动

### 关键功能驱动

- Optional smoke checks 只有在显式启用时执行，否则 self-skip。
- Approved local commands 不得 raw commit 或 raw expose。
- Smoke-only env vars 与 Spring adapter runtime properties 必须分开记录。
- Runtime invocation 保持在 adapter/runtime implementation 与 test scope 内。

### 关键非功能驱动

- 默认 CI 保持 mock-safe。
- Diagnostics 保持 bounded 与 sanitized。
- Evidence 语言必须区分 smoke readiness 与 production readiness。

### 约束与假设

- 真实 runtime binaries 是 optional local dependencies，不是 repository dependencies。
- 本切片不新增公开 API。
- 本切片不新增持久化。
- 本切片不替代 adapter contract tests。

## 系统上下文

| Actor / System | 角色 |
|---|---|
| Platform administrator | 在仓库外设置 approved local smoke env vars。 |
| Backend test suite | 通过 JUnit self-skip behavior 运行 optional smoke tests。 |
| Runtime adapter implementation | 在 converter/parser adapters 后面拥有真实 configured runtime execution。 |
| Traceability / roadmap docs | 存储安全 readiness evidence 与成熟度语言。 |

## 高层架构

```text
┌──────────────────────────────────────────────────────────────┐
│  Platform Administrator                                      │
│  Provides approved local env vars outside the repository      │
└──────────────────────────────┬───────────────────────────────┘
                               │ local shell env, not committed
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Backend Verification                                         │
│  mvn verify · focused smoke IT · seam guard · safety scans     │
└──────────────────────────────┬───────────────────────────────┘
                               │ optional command smoke only
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Smoke Test Boundary                                          │
│  self-skip assumptions · safe args default · sanitized output  │
└──────────────────────────────┬───────────────────────────────┘
                               │ allowed direct process check
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Adapter Runtime Boundary                                     │
│  converter runtime · parser runtime · sanitizer · seam guard   │
└──────────────────────────────┬───────────────────────────────┘
                               │ safe evidence only
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Documentation Evidence                                       │
│  runbook · traceability · roadmap maturity statement           │
└──────────────────────────────────────────────────────────────┘
```

## 分层摘要

- **Documentation layer:** 拥有 runbook 与 evidence checklist。
- **Verification layer:** 拥有默认 backend verification、focused smoke execution、seam guard 与 scans。
- **Smoke-test layer:** 拥有 optional env var interpretation 与 self-skip behavior。
- **Adapter/runtime layer:** 仍是唯一允许 product runtime execution 的边界。

## 组件分解

### Backend / Verification Components

- **Optional runtime smoke test:** 只有在启用并配置时检查 command health。
- **Runtime output sanitizer:** 在 evidence 报告前 mask diagnostics。
- **Adapter seam guard:** 防止 direct runtime/process references 进入 product layers。

### Configuration / Administration Modules

- **Smoke env var contract:** 控制 test-only local runtime checks。
- **Spring adapter runtime properties:** 控制 backend runtime 中的 configured adapter execution，并与 smoke env vars 分开记录。

### Monitoring / Audit Modules

- **Traceability evidence:** 记录不依赖 raw command 的安全结果与 skipped checks。
- **Roadmap maturity language:** 防止 smoke readiness 被写成 production-readiness claim。

### Integration Adapters

- **`trinity-office` adapter boundary:** 现有 converter runtime boundary。
- **`document-normalize` adapter boundary:** 现有 parser runtime boundary。

## 数据架构

### 概念实体

| Entity | 描述 | 持久化 |
|---|---|---|
| Smoke configuration | Optional smoke checks 的本地 env var inputs | 不持久化 |
| Smoke outcome | 安全 skip/pass/fail/timeout evidence | 仅文档 |
| Runbook | 面向 operator 的 smoke workflow | Markdown docs |
| Roadmap status | 成熟度限定的切片状态 | Markdown docs |

### 状态 / 状态模型

- Smoke readiness: `Draft -> Accepted -> Implemented with skip evidence -> Implemented with approved pass evidence`
- Smoke execution: `Disabled -> Skipped | Enabled missing command -> Skipped | Enabled command -> Passed | Failed | Timed out`

### 持久化职责

本切片不引入数据库或 API 持久化。

## 集成架构

| Integration | 模式 | 交换数据 |
|---|---|---|
| Optional local runtime command | 仅在允许 test/runtime scope 中进行 local process smoke | Exit code 与 bounded diagnostics |
| Runtime adapter configuration | Spring server-side properties | Enabled flag 与 command configuration；product responses 中不 raw 暴露 |
| Documentation evidence | Closeout 期间人工更新 | Safe summaries、skipped checks、residual risks |

## 工作流 / Runtime 架构

### 请求流

无用户可见 API request flow。Operator 在 SDD acceptance 与 implementation 后运行本地 verification commands。

### 执行流

1. 默认 backend verification 在 smoke disabled 状态下运行并 skipped。
2. Optional focused smoke 只有在 env vars 显式启用时运行。
3. Smoke test 使用 approved local command values 执行 command health checks。
4. Output sanitizer 移除敏感 diagnostics。
5. Traceability 只记录 safe evidence。

### 失败与重试处理

- 缺少 enable flag：skip，不是 failure。
- 缺少 command：skip，不是 failure。
- Non-zero exit：focused smoke failure，不是 default CI failure。
- Timeout：focused smoke failure，并记录 safe timeout summary。
- Unsafe diagnostic：在 sanitized 前视为 failed evidence。

## API / Interface Boundaries

### 主要入站接口

无。本切片不引入新公开 API。

### 内部模块边界

- Smoke tests 可以使用 local runtime executor 进行 command health checks。
- Product services 与 controllers 必须继续只使用 converter/parser adapters。

### 出站集成

| Target | Protocol | Triggered by |
|---|---|---|
| Approved local runtime command | Local process | 仅 optional smoke test |

## 部署 / 环境考虑

- 本切片支持的环境：具备 approved binaries 的本地 developer/operator machine。
- 默认 CI 不应设置 smoke env vars。
- Local command env vars 视为敏感，不提交。
- 生产 deployment health checks 推迟到后续 operations slice。

## 安全 / 可靠性 / 可观测性

### Secret Protection

Raw command paths、private paths、credentials、shell history 与 raw diagnostics 不得提交或在报告中 raw echo。

### Reliability

Self-skip behavior 防止缺少 optional binaries 时破坏默认 verification。

### Monitoring / Logging

本切片 evidence 基于文档。Production logging 与 audit 不在范围内。

## 风险 / 权衡

| # | 风险 / 权衡 | 说明 |
|---|---|---|
| 1 | Command health smoke 弱于 document processing smoke | 保持本切片安全和小范围；后续 sample fixture smoke 可通过 accepted scope 增加。 |
| 2 | Env var command values 是本地路径 | Runbook 必须将其视为敏感，并避免 raw evidence。 |
| 3 | 默认 skip 可能掩盖本地 runtime drift | Closeout 必须说明 approved pass evidence 是已运行还是 skipped。 |

## 待确认问题

1. OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001：最终 runbook path。
2. OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002：未来是否增加 sample processing smoke。
