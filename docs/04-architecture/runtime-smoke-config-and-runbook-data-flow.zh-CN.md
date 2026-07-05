# 数据流：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 概述

本切片只移动 smoke configuration、本地 command outcome 与安全 documentation evidence。它不移动 source documents、generated Markdown、Wiki pages、graph edges、Ask citations、secrets 或 production audit records。

## Flow 1：Smoke Disabled 的默认验证

```text
┌────────────────────────┐
│ Developer / CI command │
│ cd backend && mvn verify│
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ Optional smoke IT      │
│ reads enable env var   │
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ enable != true         │
│ JUnit assumption skip  │
└────────────┬───────────┘
             ▼
┌────────────────────────┐
│ Safe evidence          │
│ skipped by design      │
└────────────────────────┘
```

### 数据对象

| Object | Source | Destination | Safety rule |
|---|---|---|---|
| Enable flag | Local environment | Smoke test assumption | 不持久化。 |
| Skip reason | JUnit result | Verification summary | 可作为 generic status 安全报告。 |

## Flow 2：Approved Runtime Command Smoke

```text
┌─────────────────────────────┐
│ Platform administrator      │
│ sets local env outside repo  │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Focused smoke test          │
│ reads command + args env    │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Runtime executor            │
│ bounded local command check │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Sanitizer                   │
│ masks paths/hosts/secrets   │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Safe closeout evidence      │
│ pass/fail/timeout summary   │
└─────────────────────────────┘
```

### 字段映射

| Input | Processing | Output |
|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | 大小写不敏感的精确 `true` 检查 | enabled 或 skipped |
| command env var | 按本地敏感路径处理 | 不复制到 evidence |
| smoke args env var | 拆分为 command args；默认 `--version` | 只报告 generic args behavior |
| stdout/stderr | Bounded capture 与 sanitization | safe diagnostic summary |
| exit code / timeout | Result classification | pass、fail 或 timeout |

## Flow 3：Documentation Closeout

```text
┌─────────────────────────┐
│ Verification result     │
│ skip/pass/fail/timeout  │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Safety scan             │
│ secret/path/network     │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Traceability            │
│ safe evidence only      │
└────────────┬────────────┘
             ▼
┌─────────────────────────┐
│ Roadmap                 │
│ smoke readiness status  │
└─────────────────────────┘
```

## 校验规则

| Rule | Edge case | Expected result |
|---|---|---|
| Smoke disabled unless enable flag is `true` | Env var absent | Skip。 |
| Smoke disabled unless enable flag is `true` | Env var `TRUE` | 如果 command 存在则执行。 |
| Smoke disabled unless enable flag is `true` | Env var `yes` | Skip。 |
| Command value is sensitive | Command contains a local path | 不在 docs/report 中包含 raw value。 |
| Diagnostics are sanitized | Output contains path/host/token-like value | 报告前 mask。 |
| Default CI remains mock-safe | 未安装 runtime binaries | `mvn verify` 仍以 skips 通过。 |

## 数据生命周期

1. Smoke env vars 在仓库外设置。
2. Test code 在 runtime 读取 env vars。
3. Local command output 被 bounded 并 sanitized。
4. 只有 safe status summaries 进入 traceability/final response。
5. Raw env values 与 diagnostics 被丢弃。

## 排除项

- 没有 source document content 进入本流。
- 不持久化 raw command value。
- 不创建 production audit event。
- 本切片不引入 external network call。
