# 数据流：真实 Office 解析 Runtime

## 状态

供用户审阅的草稿。

## 概述

本文描述 configured runtime data 如何在 Atlas 中流动，同时 runtime details 保持在 adapter boundaries 后面。

## Flow 1: Capability Inspection

```text
Admin UI/API client
  -> Atlas capability endpoint
  -> Adapter registry
  -> Adapter capability
  -> Masked capability response
```

| Step | Data In | Data Out | Safety Rule |
|---|---|---|---|
| Request capability | none | adapter capability list | 不请求 raw runtime config。 |
| Resolve adapters | registered adapters | capability objects | Default marker 排在前面。 |
| Mask config | server-side config | status-only summary | 使用 `configured`、`missing`、`disabled`、`not-required`。 |
| Return response | capability objects | API envelope | 不返回 command path、endpoint、host、secret 或 private path。 |

## Flow 2: Configured Conversion

```text
Conversion request
  -> Validate batch/files
  -> Resolve converter adapter
  -> Check capability status
  -> Execute runtime inside adapter
  -> Translate runtime output to converter result
  -> Validate status/path/error
  -> Persist run/result/file metadata
```

| Runtime Output | Product Mapping |
|---|---|
| PDF created | `PDF_CONVERTED` 加安全相对 `pdfPath` |
| Office conversion failed | `PDF_CONVERT_FAILED` 加 safe error |
| OCR needed | `OCR_REQUIRED` 加 safe reason |
| Unsupported input | `UNSUPPORTED` 加 safe reason |
| Adapter fault / timeout | run `FAILED` 或 `PARTIAL_FAILED`；只保留 safe summary |

## Flow 3: Configured Parsing

```text
Parser request
  -> Validate batch/files
  -> Filter eligible PDF_CONVERTED files
  -> Resolve parser adapter
  -> Check capability status
  -> Execute runtime inside adapter
  -> Translate runtime manifest/output
  -> Validate markdown/assets/chunks
  -> Persist parser result + source chunks + file metadata
```

| Runtime Output | Product Mapping |
|---|---|
| Markdown generated with confidence >= threshold | `MARKDOWN_GENERATED` |
| Markdown generated with confidence < threshold | `LOW_CONFIDENCE` |
| No extractable content / OCR needed | `OCR_REQUIRED` |
| Parser failed | `FAILED` 加 safe error |
| Unsupported parse target | `UNSUPPORTED` 或 adapter 前 skip |

## Flow 4: Failure And Sanitization

```text
Runtime fault / unsafe output
  -> Capture bounded message
  -> Remove secrets, hosts, stack traces, private paths
  -> Reject unsafe path/status/confidence
  -> Persist safe run evidence when possible
  -> Return safe API envelope
```

## Data Lifecycle

| Data Object | Created By | Consumed By | Lifecycle |
|---|---|---|---|
| Runtime config | Server-side deployment config | Adapter capability/runtime executor | 不以 raw values 持久化到 product DB。 |
| Runtime stdout/stderr | Runtime process/worker | Adapter runtime implementation | 脱敏/截断；raw value 丢弃。 |
| PDF artifact path | Converter result | File metadata、parser target selection | 仅安全相对路径。 |
| Markdown/assets path | Parser result | 后续 Review/Wiki ingest | 仅安全相对路径。 |
| Source chunk | Parser result | Review、Wiki、graph、Ask | 除非后续批准，否则 review-required。 |

## Edge Cases

| Case | Required Data Flow |
|---|---|
| Missing runtime config | Capability 返回 misconfigured/disabled；如被请求则 run 安全失败。 |
| Runtime returns absolute path | Product validation 在 unsafe persistence 前拒绝 result。 |
| Runtime returns secret in stderr | Sanitizer 在 safe message 持久化前 mask/truncate。 |
| Partial file failure | 成功文件可产生 safe evidence；失败文件根据现有 run semantics 得到 safe status/error。 |
| No real binary in CI | Mock/fake adapter path 运行；opt-in runtime smoke tests self-skip。 |
