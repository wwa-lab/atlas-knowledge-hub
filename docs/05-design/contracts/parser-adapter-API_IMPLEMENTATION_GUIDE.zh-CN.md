# Parser Adapter — API / Adapter 实现指南

## 状态

草稿。Phase 3 parser-adapter 实现前必需。切片 `parser-adapter`。

## 概览

本指南定义 PDF-to-Markdown/images parsing 的内部 API 与 adapter contracts。该 API 是 Atlas 内部 API；不暴露原始文档字节、raw parser logs、原始命令配置、外部云调用或私有 runtime paths。

## 基础约定

- Base path：`/api`。
- Envelope：复用 `ApiEnvelope`。
- 仅 JSON。
- Auth/RBAC：延后；本切片 internal-only。
- Paths：仅相对路径；拒绝 traversal、drive prefixes、URI prefixes、host prefixes 与私有绝对路径。
- Secrets：仅 masked/status-only；绝不返回 raw command paths、tokens、credentials、hostnames、endpoints 或 private paths。
- Mode：测试使用 `mock`；configured runtime behavior 保持在 adapter 后。

## Adapter Capability Contract

### `GET /api/parser-adapters`

目的：列出已配置 parser adapters。

Response：

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "document-normalize",
      "displayName": "Document Normalize Parser",
      "version": "configured",
      "inputTypes": ["pdf"],
      "outputTypes": ["markdown", "assets"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "lowConfidenceThreshold": 0.8,
      "maskedConfigSummary": {
        "runtime": "configured",
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

Validation/security：

- 不返回原始命令字符串、绝对路径、endpoints、环境变量值或 credentials。
- `version` 可以是安全 adapter version 或 `configured`；不要只为了 capability listing 获取版本而调用 parser engine。

## Create Parser Run

### `POST /api/batches/{batchId}/parser-runs`

目的：通过 parser adapter contract，对 batch 中选定且符合条件的 file items 执行 parsing。

Request：

```json
{
  "adapterKey": "document-normalize",
  "fileIds": ["file-001", "file-002"],
  "requestedBy": "delivery-lead",
  "mode": "mock"
}
```

规则：

- `adapterKey` 可选；省略表示默认 parser adapter。
- `fileIds` 可选；省略表示 batch 中全部符合条件文件。
- `mode` 可选，必须是 `mock` 或 `configured`；测试使用 `mock`。
- Service 在启动 run 前校验目标文件。
- 只有 `PDF_CONVERTED` 且拥有安全相对 `pdfPath` 的文件会传给 adapter。

成功响应：

```json
{
  "success": true,
  "data": {
    "runId": "parse-run-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "document-normalize",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock parsing completed with review-required output.",
    "summary": {
      "total": 4,
      "markdownGenerated": 2,
      "lowConfidence": 1,
      "ocrRequired": 1,
      "failed": 0,
      "skipped": 0,
      "unsupported": 0
    },
    "results": [
      {
        "fileId": "file-001",
        "sourcePath": "Discovery/BRD.docx",
        "pdfPath": "generated/pdf/BRD.pdf",
        "status": "MARKDOWN_GENERATED",
        "markdownPath": "generated/markdown/BRD.md",
        "assetsPath": "generated/assets/BRD",
        "confidence": 0.91,
        "reviewStatus": "REVIEW_REQUIRED",
        "chunkCount": 2,
        "skipped": false,
        "safeError": null
      }
    ],
    "chunks": [
      {
        "chunkId": "chunk-file-001-001",
        "fileId": "file-001",
        "sourceFile": "Discovery/BRD.docx",
        "page": 3,
        "section": "Business Rules",
        "confidence": 0.91,
        "reviewStatus": "REVIEW_REQUIRED"
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:03Z"
  },
  "error": null,
  "meta": null
}
```

错误情况：

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter key、unsafe path、invalid mode、no eligible targets、invalid confidence、cross-batch target、invalid parser result。 |
| 404 | `NOT_FOUND` | Batch 或 file id 未知。 |
| 409 | `CONFLICT` | Batch 已有 active parser run，不能启动。 |
| 500 | `INTERNAL_ERROR` | 意外 adapter fault；details 只在服务端安全记录。 |

## Get Parser Run

### `GET /api/parser-runs/{runId}`

目的：返回 parser run summary、逐文件 report 与 chunk summary。

Response shape：与 create parser run 的 `data` body 相同。

## Internal Adapter Interface Contract

概念接口：

```text
ParserAdapter
  capability() -> ParserCapability
  parse(ParserRequest) -> ParserResult
```

必需 request fields：

| Field | Description |
|---|---|
| `runId` | Server 创建的 parser run id。 |
| `batchId` | 现有 batch id。 |
| `files[]` | File item descriptors：file id、source path、source type、current status、PDF path。 |
| `artifactRoot` | 可选相对 artifact root hint，默认 `generated/markdown`。 |
| `lowConfidenceThreshold` | Decimal threshold，默认 `0.800`。 |

必需 result fields：

| Field | Description |
|---|---|
| `adapterKey` | 产出 result 的 adapter。 |
| `files[]` | 逐文件 parser result。 |
| `safeMessage` | 可选脱敏 run-level summary。 |

逐文件 result fields：

| Field | Description |
|---|---|
| `fileId` | 现有 file item id。 |
| `status` | 现有 `FileStatus` result。 |
| `markdownPath` | 可用时的相对生成 Markdown path。 |
| `assetsPath` | 可用时的相对生成 assets path。 |
| `confidence` | 可选 `[0,1]`。 |
| `reviewStatus` | 当前 file review status。 |
| `chunkCount` | 已接受 source chunk 数量。 |
| `skipped` | 仅 skipped/ineligible report rows 为 `true`；skipped rows 保留文件未变化的当前 status。 |
| `safeError` | 脱敏 error summary。 |
| `chunks[]` | 该文件的 source trace chunks。 |

Chunk fields：

| Field | Description |
|---|---|
| `chunkId` | Parser run 内唯一的 stable chunk id。 |
| `page` | 可用时为正 page number。 |
| `section` | User-safe section label。 |
| `confidence` | 可选 `[0,1]`。 |
| `reviewStatus` | 默认 `REVIEW_REQUIRED`。 |

## Status Mapping

| Source / Outcome | Result Status |
|---|---|
| 符合条件 PDF 成功，confidence `>= 0.800` | `MARKDOWN_GENERATED` |
| 符合条件 PDF 成功，confidence `< 0.800` | `LOW_CONFIDENCE` |
| Parser 表示需要 OCR | `OCR_REQUIRED` |
| Parser failure | `FAILED` |
| Unsupported parser outcome | `UNSUPPORTED` |
| Ineligible selected file | skipped report entry，file unchanged |

本切片不得新增 `FileStatus` 值。

## Contract Tests

实现完成前运行：

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

Seam scan 在 non-adapter product layers 中必须无匹配。Adapter implementation packages 只有在 guard tests 覆盖且 outbound network clients 不泄漏到 product layers 时，才可包含 parser engine names。
