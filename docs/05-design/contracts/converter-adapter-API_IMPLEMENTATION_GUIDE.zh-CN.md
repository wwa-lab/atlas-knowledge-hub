# Converter Adapter — API / Adapter Implementation Guide

## 状态

草稿。Phase 3 实现前必需。切片 `converter-adapter`。

## 概览

本文定义 Office-to-PDF conversion 的内部 API 与 adapter contract。API 仅供 Atlas 内部使用；不暴露原始文档字节、原始命令配置或外部云调用。

## 基础约定

- Base path：`/api`。
- Envelope：复用 `ApiEnvelope`。
- 仅 JSON。
- Auth/RBAC：延后；本切片 internal-only。
- 路径：仅相对路径；拒绝目录穿越、盘符前缀、host 前缀和私有绝对路径。
- Secrets：仅脱敏/状态化；绝不返回原始 command path、token、credential 或 private endpoint。

## Adapter Capability Contract

### `GET /api/converter-adapters`

目的：列出已配置 converter adapters。

响应：

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "trinity-office",
      "displayName": "Trinity Office Converter",
      "version": "configured",
      "outputType": "pdf",
      "supportedSourceTypes": ["pptx", "docx", "xlsx", "pdf"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "maskedConfigSummary": {
        "command": "configured",
        "workingDirectory": "configured",
        "externalNetwork": "disabled"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

`status` values：`AVAILABLE`、`DISABLED`、`MISCONFIGURED`。

校验/安全：

- 不返回原始 command string 或绝对路径。
- `version` 可以是安全 adapter version 或 `configured`；不要仅为了 capability listing 去 shell out 获取版本。

## Create Conversion Run

### `POST /api/batches/{batchId}/conversion-runs`

目的：通过 converter adapter contract 对 batch 中选中的 file items 执行 conversion。

请求：

```json
{
  "adapterKey": "trinity-office",
  "fileIds": ["file-001", "file-002"],
  "requestedBy": "delivery-lead",
  "mode": "mock"
}
```

规则：

- `adapterKey` 可选；省略则使用 default converter adapter。
- `fileIds` 可选；省略则代表 batch 内所有 eligible files。
- `mode` 可选，必须是 `mock` 或 `configured`；测试使用 `mock`。
- Service 在启动 run 前校验 target files。

成功响应：

```json
{
  "success": true,
  "data": {
    "runId": "conv-run-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "trinity-office",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock conversion completed with partial failures.",
    "summary": {
      "total": 4,
      "pdfConverted": 2,
      "pdfConvertFailed": 1,
      "ocrRequired": 1,
      "unsupported": 0,
      "skipped": 0
    },
    "results": [
      {
        "fileId": "file-001",
        "sourcePath": "Discovery/BRD.docx",
        "sourceType": "docx",
        "status": "PDF_CONVERTED",
        "pdfPath": "generated/pdf/BRD.pdf",
        "confidence": 1.0,
        "reviewStatus": "REVIEW_REQUIRED",
        "safeError": null
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:03Z"
  },
  "error": null,
  "meta": null
}
```

错误场景：

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter key、unsafe path、invalid target list、unsupported request mode。 |
| 404 | `NOT_FOUND` | Batch 或 file id 不存在。 |
| 409 | `CONFLICT` | Batch 已被 active run 锁定，无法启动 conversion run。 |
| 500 | `INTERNAL_ERROR` | 未预期 adapter fault；细节只在服务端安全记录。 |

## Get Conversion Run

### `GET /api/conversion-runs/{runId}`

目的：返回 conversion run summary 和逐文件 report。

响应形状：与 create conversion run 的 `data` body 相同。

## Internal Adapter Interface Contract

概念接口：

```text
ConverterAdapter
  capability() -> ConverterCapability
  convert(ConverterRequest) -> ConverterResult
```

请求必需字段：

| Field | Description |
|---|---|
| `runId` | 服务端创建的 conversion run id。 |
| `batchId` | 现有 batch id。 |
| `files[]` | File item descriptors：file id、source path、source type、current status。 |
| `artifactRoot` | 可选相对 artifact root hint。 |

结果必需字段：

| Field | Description |
|---|---|
| `adapterKey` | 产生结果的 adapter。 |
| `files[]` | 逐文件 conversion result。 |
| `safeMessage` | 可选清洗后的 run-level summary。 |

逐文件 result 字段：

| Field | Description |
|---|---|
| `fileId` | 现有 file item id。 |
| `status` | 现有 `FileStatus` result。 |
| `pdfPath` | 可用时的相对 generated/pass-through path。 |
| `confidence` | 可选 `[0,1]`。 |
| `safeError` | 清洗后的错误摘要。 |

## Status Mapping

| Source Type / Outcome | Result Status |
|---|---|
| `pptx`、`docx`、`xlsx` success | `PDF_CONVERTED` |
| `pptx`、`docx`、`xlsx` failure | `PDF_CONVERT_FAILED` |
| `pdf` pass-through | `PDF_CONVERTED` |
| `image` requires OCR | `OCR_REQUIRED` |
| `unsupported` | `UNSUPPORTED` |

本切片不得新增 `FileStatus` 值。

## Contract Tests

完成前运行：

```bash
cd backend && mvn verify
git diff --check
rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md
```

查找 command/network API 的 `rg` 命令在非 adapter 产品层应无匹配。Adapter implementation packages 只有在 seam guard tests 覆盖时才可包含 command-runner boundaries。
