# 数据流：Converter Adapter

## 状态

草稿。`docs/04-architecture/converter-adapter-architecture.md` 的配套文档。

## Flow 1：Capability Discovery

```text
Client/Admin
    |
    v
GET /api/converter-adapters
    |
    v
Converter API -> Adapter Registry -> Registered Adapters
    |
    v
Masked capability list
```

| Data Object | 字段 |
|---|---|
| Converter capability | adapterKey, displayName, version, outputType, supportedSourceTypes, defaultAdapter, status, maskedConfigSummary |

安全规则：

- `maskedConfigSummary` 只包含 `configured`、`missing`、`disabled` 等状态值。
- 不返回原始命令路径、环境变量值、凭据、hostname、private endpoint 或绝对路径。

## Flow 2：Conversion Run

```text
POST /api/batches/{batchId}/conversion-runs
    |
    v
Validate batch + target files + adapter key
    |
    v
Create conversion_run REQUESTED
    |
    v
Resolve adapter
    |
    +-- unavailable -> conversion_run FAILED, file statuses unchanged
    |
    v
Set conversion_run RUNNING
    |
    v
Adapter executes through interface
    |
    v
Map per-file results
    |
    v
Persist conversion_file_result rows
    |
    v
Update file_item status/pdfPath/confidence/errorMessage
    |
    v
Set conversion_run SUCCEEDED / PARTIAL_FAILED / FAILED
```

## Flow 3：逐文件结果映射

| 输入 | Adapter Result | File Update |
|---|---|---|
| `pptx`、`docx`、`xlsx` success | generated relative `pdfPath` | `status=PDF_CONVERTED`、`pdfPath`、safe summary |
| `pptx`、`docx`、`xlsx` failure | safe error | `status=PDF_CONVERT_FAILED`、`errorMessage` |
| `pdf` pass-through | relative PDF reference | `status=PDF_CONVERTED`、`pdfPath` 策略待 OQ-CA-002 确认 |
| `image` | OCR required | `status=OCR_REQUIRED`、可选安全 reason |
| `unsupported` | unsupported | `status=UNSUPPORTED`、safe reason |
| Adapter unavailable before per-file execution | no per-file result | file status unchanged |

## Flow 4：Report Retrieval

```text
GET /api/conversion-runs/{runId}
    |
    v
Read conversion_run + conversion_file_result
    |
    v
Return envelope with run summary and per-file safe report
```

Report summary 字段：

- `total`
- `pdfConverted`
- `pdfConvertFailed`
- `ocrRequired`
- `unsupported`
- `skipped`
- `adapterKey`
- `status`
- `startedAt`
- `completedAt`

## Validation And Rejection Flow

```text
Incoming result
    |
    v
Path safety check
    |
    +-- unsafe -> reject result, record safe validation error
    |
    v
Status mapping check
    |
    +-- unknown status -> reject result, no enum expansion
    |
    v
Secret/error sanitization
    |
    +-- unsafe -> store sanitized summary only
    |
    v
Persist result + update file item
```

## 数据安全说明

- 本数据流不包含源文件字节。
- 原始 engine stdout/stderr 不持久化。
- 相对 artifact path 只是 metadata pointer；真实对象持久化属于后续 storage-adapter。
- 生成/派生输出的 review status 保持 `REVIEW_REQUIRED`。
