# 真实 Office 解析 Runtime - API 实现指南

## 状态

供用户审阅的草稿。用户接受前 backend implementation 阻塞。

## 概述

本切片复用现有 converter 和 parser API surfaces。API contract change 是行为层面的：configured runtime mode 可在 runtime adapters 可用时执行内部 runtime，同时所有 responses 仍保持安全、masked、envelope-based。

## Authentication

Production authentication 和 RBAC 不在范围内。本切片保持现有 internal/mock-safe API behavior 不变。

## Error Response Format

使用现有 Atlas API envelope 和 global error handling。Runtime errors 必须转换为安全 validation、conflict、not-found 或 failed-run responses，不暴露 raw runtime output。

## API Endpoints Summary

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| List converter adapters | GET | `/api/converter-adapters` | 查看安全 converter capability metadata。 |
| Create conversion run | POST | `/api/batches/{batchId}/conversion-runs` | 启动 mock 或 configured conversion run。 |
| Get conversion run | GET | `/api/conversion-runs/{runId}` | 获取 conversion run report。 |
| List parser adapters | GET | `/api/parser-adapters` | 查看安全 parser capability metadata。 |
| Create parser run | POST | `/api/batches/{batchId}/parser-runs` | 启动 mock 或 configured parser run。 |
| Get parser run | GET | `/api/parser-runs/{runId}` | 获取 parser run report。 |

## Capability Metadata Rules

Capability responses 可以包含：

```json
{
  "adapterKey": "trinity-office",
  "displayName": "Trinity Office Converter",
  "version": "configured",
  "defaultAdapter": false,
  "status": "AVAILABLE",
  "maskedConfig": {
    "command": "configured",
    "externalNetwork": "disabled"
  }
}
```

Capability responses 不得包含：

- raw command path
- raw endpoint
- hostname
- token、password、API key 或 credential value
- private absolute path
- raw environment variable value

## Create Conversion Run

### Request

```json
{
  "adapterKey": "trinity-office",
  "requestedBy": "knowledge-manager",
  "mode": "configured",
  "fileIds": ["file-001", "file-002"]
}
```

### Behavior

- `mode=mock` 或空值使用 mock-safe behavior。
- `mode=configured` 只有在 resolved adapter 报告 `AVAILABLE` 时才可使用 configured runtime。
- Unknown 或 unavailable adapters 安全失败。
- Runtime results 在持久化前由现有 conversion result rules 验证。

### Error Cases

| Case | Response behavior |
|---|---|
| Unknown batch | Safe not-found envelope。 |
| Unknown file id | Safe not-found 或 validation envelope。 |
| Adapter unavailable/misconfigured | 尽可能创建 failed run；只包含 safe summary。 |
| Runtime timeout | Failed 或 partial-failed run，包含 safe timeout summary。 |
| Unsafe `pdfPath` | Validation error；unsafe path 不持久化。 |

## Create Parser Run

### Request

```json
{
  "adapterKey": "document-normalize",
  "requestedBy": "knowledge-manager",
  "mode": "configured",
  "fileIds": ["file-pdf-001"]
}
```

### Behavior

- 只有带 safe `pdfPath` 的 eligible `PDF_CONVERTED` files 会发送到 parser runtime。
- Ineligible files 按现有 parser service behavior skip 或 reject。
- Runtime Markdown/assets paths 和 chunks 持久化前必须通过验证。
- Generated content 保持 review-required。

### Error Cases

| Case | Response behavior |
|---|---|
| No eligible target | Validation error。 |
| Adapter unavailable/misconfigured | 尽可能创建 failed run；只包含 safe summary。 |
| Runtime timeout | Failed 或 partial-failed run，包含 safe timeout summary。 |
| Unsafe Markdown/assets path | Validation error；unsafe path 不持久化。 |
| Duplicate chunk id | Unsafe chunk persistence 前 validation error。 |

## State Reference

```text
Run:
  REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED

Capability:
  AVAILABLE | DISABLED | MISCONFIGURED
```

## Concurrency

继续沿用现有 active-run rules。一个 batch 不得有同 run type 的冲突 active conversion 或 parser runs。

## Integration Dependencies

- Runtime command/worker config 只在 server-side。
- Timeout default 为每次 adapter invocation 120 秒。
- Captured output limit 为每次 invocation 65536 bytes。
- Runtime smoke tests 为 opt-in，缺少 config 时必须 self-skip。

## Testing Contract

- API contract tests 覆盖 capability masking，以及使用 fake runtime executor 的 configured mode failure/success paths。
- Service tests 覆盖 safe status mapping、timeout、invalid manifest、unsafe path 和 sanitized messages。
- Seam guard tests 覆盖 adapter boundary。
- 默认 test 不要求真实 `trinity-office` 或 `document-normalize`。
