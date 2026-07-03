# Data Flow: Converter Adapter

## Status

Draft. Companion to `docs/04-architecture/converter-adapter-architecture.md`.

## Flow 1: Capability Discovery

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

| Data Object | Fields |
|---|---|
| Converter capability | adapterKey, displayName, version, outputType, supportedSourceTypes, defaultAdapter, status, maskedConfigSummary |

Safety rules:

- `maskedConfigSummary` contains status-only values such as `configured`, `missing`, or `disabled`.
- No raw command path, environment variable value, credential, hostname, private endpoint, or absolute path is returned.

## Flow 2: Conversion Run

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

## Flow 3: Per-File Result Mapping

| Input | Adapter Result | File Update |
|---|---|---|
| `pptx`, `docx`, `xlsx` success | generated relative `pdfPath` | `status=PDF_CONVERTED`, `pdfPath`, safe summary |
| `pptx`, `docx`, `xlsx` failure | safe error | `status=PDF_CONVERT_FAILED`, `errorMessage` |
| `pdf` pass-through | relative PDF reference | `status=PDF_CONVERTED`, `pdfPath` policy pending OQ-CA-002 |
| `image` | OCR required | `status=OCR_REQUIRED`, `errorMessage` optional safe reason |
| `unsupported` | unsupported | `status=UNSUPPORTED`, safe reason |
| Adapter unavailable before per-file execution | no per-file result | file status unchanged |

## Flow 4: Report Retrieval

```text
GET /api/conversion-runs/{runId}
    |
    v
Read conversion_run + conversion_file_result
    |
    v
Return envelope with run summary and per-file safe report
```

Report summary fields:

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

## Data Safety Notes

- Source bytes are not represented in this data flow.
- Raw engine stdout/stderr is not persisted.
- Relative artifact paths are metadata pointers only; storage-adapter owns real object persistence later.
- Review status remains `REVIEW_REQUIRED` for generated/derived output.
