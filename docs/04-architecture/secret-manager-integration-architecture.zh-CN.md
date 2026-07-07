# 架构：secret-manager-integration

状态：已按预授权接受
最后更新：2026-07-07

## 概览

本切片在 Spring Boot API 层引入共享 secret status contract，并由 Vue settings surface 消费。它不新增 secret manager runtime。Raw values 保持在现有 runtime/model adapter 边界内，API response 只暴露状态与 logical references。

## 架构图

```text
┌──────────────────────────────────────────────────────────────┐
│ Platform Admin                                                │
└──────────────────────────────┬───────────────────────────────┘
                               │ HTTPS / JSON
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Settings Surface                                          │
│ model · parser · converter · storage · vector status display  │
└──────────────────────────────┬───────────────────────────────┘
                               │ REST / ApiEnvelope
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                      │
│ model config endpoints · capability endpoints · DTO mappers   │
├──────────────────────────────────────────────────────────────┤
│ Secret Status Contract                                        │
│ SecretReferenceResponse · SecretStatusResponse                │
├──────────────────────────────────────────────────────────────┤
│ Adapter Boundaries                                            │
│ model · parser · converter · storage · vector                 │
└──────────────────────────────┬───────────────────────────────┘
                               │ raw values stay internal
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Runtime / Environment Configuration                           │
│ process env and in-memory runtime config only                 │
└──────────────────────────────────────────────────────────────┘
```

## 组件责任

| Component | Responsibility |
|---|---|
| Shared DTOs | 表示 logical secret references 与 status-only configuration。 |
| ModelRuntimeConfigurationService | 接收 write-only replacement input，保留/清除 runtime state，返回 masked model configuration。 |
| Adapter capability mappers | 将 typed secret statuses 附加到现有 masked capability summaries。 |
| Adapter implementations | 将 raw provider/runtime values 保持在 execution boundary 内。 |
| Vue settings | 渲染 status chips 与 replace/remove affordances，不显示 raw values。 |

## 架构约束

- UI 不直接依赖 provider secret formats。
- 不新增外部 provider 或 secret-manager call。
- `maskedConfigSummary` 保持兼容。
- Typed secret status 是新 UI 与测试的优先 contract。

## Architecture Review Result

Architecture review required: yes，因为本切片触及 secret handling、API contracts 与 adapter boundaries。
Result: SDD 范围内未发现 architecture blocker；production secret manager adapter 明确延后。
