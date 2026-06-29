# Markdown Standard

Atlas Markdown is the core durable asset. Every normalized page must include metadata, source trace, confidence, and review status.

## Front Matter

```yaml
---
workspace: "ibm-i-modernization"
batch_id: "batch-2026-001"
source_file: "modernization-overview.pptx"
source_path: "samples/input/modernization-overview.pptx"
source_type: "pptx"
pdf_file: "samples/output/batch-2026-001/modernization-overview.pdf"
converter: "trinity-office"
parser: "document-normalize"
conversion_status: "MARKDOWN_GENERATED"
review_status: "REVIEW_REQUIRED"
confidence: 0.86
last_updated: "2026-06-30"
owner: "sme-team"
---
```

## Required Fields

- `workspace`: Knowledge Space identifier.
- `batch_id`: Batch that produced the page.
- `source_file`: Original file name.
- `source_path`: Relative source path. Do not use confidential absolute paths.
- `source_type`: Original file type.
- `pdf_file`: Generated PDF path when available.
- `converter`: Converter adapter name.
- `parser`: Parser adapter name.
- `conversion_status`: Current file conversion status.
- `review_status`: Current human review status.
- `confidence`: Parser or normalizer confidence score.
- `last_updated`: Last update date.
- `owner`: Responsible team or reviewer.

## Page-Level Source Trace

Use source trace blocks below each major section or chunk:

```markdown
## Application Inventory

The portfolio contains batch jobs, RPG services, database objects, and integration endpoints.

<!-- source_trace:
source_file: "modernization-overview.pptx"
pdf_file: "modernization-overview.pdf"
page: 12
section: "Application Inventory"
chunk_id: "batch-2026-001:file-003:page-012:block-02"
confidence: 0.91
review_status: "REVIEW_REQUIRED"
-->
```

## Review Rule

Any content generated or modified by an LLM must remain `REVIEW_REQUIRED` unless an SME approves it or deterministic validation confirms it.
