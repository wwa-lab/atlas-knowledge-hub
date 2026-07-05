# Runtime Smoke Config And Runbook - API 实现指南

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 概述

本切片不引入新的公开 API endpoint，也不改变 request/response schema。本“contract”是 local runtime smoke configuration contract、与 Spring adapter runtime property 的区别，以及实现必须保留的验证行为。

## 公开 API 改动

无。

## 配置契约

### Optional Smoke Test Environment Variables

| Variable | 执行 smoke 是否必需 | 默认 / 行为 | Committed evidence 中是否允许 raw value |
|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | 是 | 任何非 `true` 值都 self-skip | 只允许变量名 |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Trinity smoke 必需 | 缺失则 Trinity smoke self-skip | 否 |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | 否 | 默认 `--version` | 避免机器特定值 |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Document Normalize smoke 必需 | 缺失则 Document Normalize smoke self-skip | 否 |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | 否 | 默认 `--version` | 避免机器特定值 |

### Spring Adapter Runtime Properties

这些属性属于 configured adapter execution，不属于 smoke-only command checks：

| Property | 默认 | API/docs 中是否允许 raw value |
|---|---|---|
| `atlas.runtime.trinity-office.enabled` | false | 只允许 property name |
| `atlas.runtime.trinity-office.command` | blank | 不允许 raw value |
| `atlas.runtime.document-normalize.enabled` | false | 只允许 property name |
| `atlas.runtime.document-normalize.command` | blank | 不允许 raw value |

## Error / Outcome Reference

| Outcome | 含义 | 必需报告 |
|---|---|---|
| `SKIPPED_DISABLED` | smoke enable flag 缺失或不是 `true` | safe skip summary |
| `SKIPPED_MISSING_COMMAND` | smoke 已启用但 command env var 缺失 | safe missing-command summary |
| `PASSED` | command exit zero 且无 timeout | runtime family pass summary，不含 raw command |
| `FAILED` | command exits non-zero | 仅 sanitized diagnostic |
| `TIMED_OUT` | command 未在允许 timeout 内完成 | safe timeout summary |
| `BLOCKED` | approved local binary 不可用或未批准 | skipped check with reason |

## Endpoint Reference

无适用 endpoint reference。

## State Reference

```text
DRAFT -> ACCEPTED -> IMPLEMENTED_WITH_SKIP_EVIDENCE
                    -> IMPLEMENTED_WITH_APPROVED_PASS_EVIDENCE
```

## Concurrency

不适用。本切片不引入持久化 API mutation。

## Integration Dependencies

- 如请求 Trinity smoke pass evidence，需要本地 approved `trinity-office` command。
- 如请求 Document Normalize smoke pass evidence，需要本地 approved `document-normalize` command。
- 默认 verification 不得依赖任一 command。

## Contract Test Expectations

- `ConfiguredRuntimeSmokeIT` 在 `ATLAS_RUNTIME_SMOKE_ENABLED` 不为 true 时 self-skip。
- `ConfiguredRuntimeSmokeIT` 在相关 command env var 缺失时 self-skip。
- Diagnostics 被 sanitized，且不包含 raw command value。
- `AdapterSeamGuardTest` 继续通过。
- `mvn verify` 在没有 local runtime binaries 时通过。
