# Batch Processing Design

Batch processing organizes uploaded document packages into traceable conversion and review work.

## Model

### Workspace

A Knowledge Space for a project, domain, or delivery stream. It owns batches, files, Wiki pages, review queues, reports, and graph data.

### Batch

A single upload and processing run. A batch records upload time, owner, source package, adapter configuration, processing summary, and reports.

### File Item

A file inside a batch. Each file item records the original path, type, generated PDF path, generated Markdown path, assets path, confidence, status, errors, and review status.

### File Status

Allowed statuses:

- `NEW`
- `UPLOADED`
- `PDF_CONVERTED`
- `PDF_CONVERT_FAILED`
- `MARKDOWN_GENERATED`
- `OCR_REQUIRED`
- `LOW_CONFIDENCE`
- `REVIEW_REQUIRED`
- `APPROVED`
- `PUBLISHED`
- `FAILED`
- `UNSUPPORTED`

### Reports

Reports summarize:

- File inventory.
- Unsupported files.
- Conversion failures.
- Low-confidence pages.
- Review-required items.
- Approved and published pages.

## Status Flow

```text
NEW -> UPLOADED -> PDF_CONVERTED -> MARKDOWN_GENERATED -> REVIEW_REQUIRED -> APPROVED -> PUBLISHED
                    |                 |                    |
                    v                 v                    v
            PDF_CONVERT_FAILED   LOW_CONFIDENCE      OCR_REQUIRED
                    |
                    v
                  FAILED
```

Unsupported files move to `UNSUPPORTED` with a clear report entry.
