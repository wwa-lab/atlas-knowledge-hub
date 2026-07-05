# 数据模型：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 概述

本切片不引入数据库实体或 schema 变更。数据模型是 optional smoke checks 与 safe evidence 的文档/配置模型。

## 实体关系图

```text
┌──────────────────────┐       produces       ┌──────────────────────┐
│ SmokeConfiguration   │ ───────────────────▶ │ SmokeOutcome         │
│ local env only       │                      │ safe summary only    │
└──────────┬───────────┘                      └──────────┬───────────┘
           │ documented by                              │ recorded in
           ▼                                            ▼
┌──────────────────────┐                      ┌──────────────────────┐
│ RuntimeSmokeRunbook  │                      │ TraceabilityStatus   │
│ markdown doc         │                      │ markdown doc         │
└──────────────────────┘                      └──────────────────────┘
```

## 实体定义

### SmokeConfiguration

- **Persistence:** 无；仅 local process environment。
- **Fields:**
  - `enabledFlag`: 映射 `ATLAS_RUNTIME_SMOKE_ENABLED`；精确 `true` 启用 smoke。
  - `runtimeFamily`: `trinity-office` 或 `document-normalize`。
  - `commandEnvVar`: command env var 名称之一；值敏感，不得持久化。
  - `argsEnvVar`: args env var 名称之一；值可能为本地配置，不得提交。
  - `defaultArgs`: args env var 缺失时为 `--version`。

### SmokeOutcome

- **Persistence:** 仅 safe documentation evidence。
- **Fields:**
  - `runtimeFamily`: runtime family label。
  - `status`: `SKIPPED`、`PASSED`、`FAILED`、`TIMED_OUT` 或 `BLOCKED`。
  - `safeReason`: sanitized reason 或 generic skip message。
  - `verificationCommand`: generic command name，不是 raw local command path。
  - `rawCommandPath`: 禁止。
  - `rawDiagnostic`: 禁止。

### RuntimeSmokeRunbook

- **Persistence:** 接受并实现后的 Markdown documentation。
- **Fields / sections:**
  - prerequisites
  - env var matrix
  - default skip verification
  - approved local smoke verification
  - outcome matrix
  - troubleshooting
  - disable/rollback
  - evidence checklist
  - safety bans

### TraceabilityStatus

- **Persistence:** Markdown documentation。
- **Fields / sections:**
  - SDD acceptance state
  - SDD skill chain evidence
  - changed docs/code summary
  - verification evidence
  - skipped checks
  - residual risks
  - maturity statement

## 状态模型

### SmokeConfiguration State

```text
UNCONFIGURED
  ├─ enable flag absent/not true ─▶ DISABLED
  └─ enable flag true
       ├─ command missing ───────▶ ENABLED_MISSING_COMMAND
       └─ command present ───────▶ ENABLED_WITH_COMMAND
```

### SmokeOutcome State

```text
DISABLED ───────────────▶ SKIPPED
ENABLED_MISSING_COMMAND ─▶ SKIPPED
ENABLED_WITH_COMMAND ────▶ PASSED
ENABLED_WITH_COMMAND ────▶ FAILED
ENABLED_WITH_COMMAND ────▶ TIMED_OUT
```

## 配置实体

| Name | Owner | Type | Validation | Docs 中允许 raw value? |
|---|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | Operator | string/boolean-like | 仅大小写不敏感的 `true` | 只允许名称 |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Operator | local command | 非空才执行 | 不允许 raw value |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | Operator | arg string | optional；按空白拆分 | 避免 raw local values |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Operator | local command | 非空才执行 | 不允许 raw value |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | Operator | arg string | optional；按空白拆分 | 避免 raw local values |

## 审计实体

不引入。Production audit for runtime operations 不在范围内。

## 字段映射

| Smoke test input | Safe evidence field |
|---|---|
| enable flag absent | `status=SKIPPED`, `safeReason=runtime smoke disabled` |
| command env var missing | `status=SKIPPED`, `safeReason=<runtime family> command missing` |
| command exits zero | `status=PASSED`, `safeReason=approved local smoke passed` |
| command exits non-zero | `status=FAILED`, sanitized diagnostic summary |
| command times out | `status=TIMED_OUT`, safe timeout summary |
