# 数据模型：真实 Office 解析 Runtime

## 状态

供用户审阅的草稿。

## 概述

本切片主要复用现有 converter/parser metadata models。只有实现确实需要时，才可能添加 runtime configuration metadata 或 runtime-attempt evidence；但 raw runtime config、credentials、command paths 和 private endpoints 不作为 product data 持久化。

## Entity Relationship Summary

```text
┌────────────┐ 1:N ┌────────────────┐ 1:N ┌────────────────────────┐
│ Batch      │────▶│ FileItem       │────▶│ ConversionFileResult   │
└────────────┘     └───────┬────────┘     └────────────────────────┘
                           │ 1:N
                           ▼
                    ┌────────────────┐ 1:N ┌─────────────┐
                    │ ParserFileResult│────▶│ SourceChunk │
                    └────────────────┘     └─────────────┘

┌───────────────┐ 1:N ┌────────────────────────┐
│ ConversionRun │────▶│ ConversionFileResult   │
└───────────────┘     └────────────────────────┘

┌───────────┐ 1:N ┌────────────────┐
│ ParserRun │────▶│ ParserFileResult│
└───────────┘     └────────────────┘
```

## Reused Entity Contracts

| Entity | Role In This Slice | Runtime-Specific Rule |
|---|---|---|
| `FileItem` | 最新 source file status、confidence、PDF path、Markdown path、assets path、safe error。 | Runtime updates 必须保留 review status 和 source identity。 |
| `ConversionRun` | Conversion execution record。 | Configured runtime attempts 使用现有 run states 和 safe summaries。 |
| `ConversionFileResult` | Per-file converter evidence。 | Runtime result paths 和 errors 持久化前必须安全。 |
| `ParserRun` | Parser execution record。 | Configured runtime attempts 使用现有 mode/status/threshold evidence。 |
| `ParserFileResult` | Per-file parser evidence。 | Runtime output 只能使用 accepted file statuses。 |
| `SourceChunk` | Parser-emitted source trace。 | 除非后续 review 改变，chunks 保持 review-required。 |

## Runtime Configuration Model

| Field | Type | Persistence | Rule |
|---|---|---|---|
| adapter key | string | request/config | 必须通过现有 registry 解析。 |
| runtime mode | enum-like string | request/run evidence | `mock` 或 `configured`；configured 为 opt-in。 |
| command status | status-only string | capability response | `configured`、`missing`、`disabled`；不暴露 raw path。 |
| worker endpoint status | status-only string | capability response | `configured`、`missing`、`disabled`；不暴露 raw endpoint。 |
| timeout seconds | integer | config | 实现前必须记录默认值。 |
| max output bytes | integer | config | 实现前必须记录默认值。 |

## Status Models

### Capability Status

```text
DISABLED -> MISCONFIGURED -> AVAILABLE
```

### Conversion File Status Mapping

| Runtime result | File status |
|---|---|
| PDF output produced | `PDF_CONVERTED` |
| Runtime conversion failed for supported Office input | `PDF_CONVERT_FAILED` |
| Runtime reports OCR needed | `OCR_REQUIRED` |
| Runtime cannot process source type | `UNSUPPORTED` |
| Adapter-level fault without per-file results | File metadata unchanged; run `FAILED` |

### Parser File Status Mapping

| Runtime result | File status |
|---|---|
| Markdown output confidence >= threshold | `MARKDOWN_GENERATED` |
| Markdown output confidence < threshold | `LOW_CONFIDENCE` |
| Runtime reports OCR needed | `OCR_REQUIRED` |
| Runtime parse failed | `FAILED` |
| Runtime cannot process target | `UNSUPPORTED` 或 adapter 前 skip |

## Validation Rules

- Paths 必须是 relative 且 traversal-free。
- URI、host-prefixed、absolute、drive-prefixed 和 private root paths 被拒绝。
- Confidence values 如存在必须在 `[0,1]`。
- Runtime messages 必须有界且脱敏。
- Runtime result adapter key 必须匹配 resolved adapter。
- Runtime-generated chunks 在 run 内必须有唯一 ids。

## No New Trust State

本切片不新增 review status 或 Wiki trust state。Generated 或 low-confidence runtime outputs 在后续显式 review 或 publish workflow 改变前保持 review-required。
