# Data Model: Folder Upload

## Status

Draft. Phase 1 FE, mock-only. TypeScript-facing domain types for the `folder-upload` slice. Derived from `docs/03-spec/folder-upload-spec.md` and `docs/batch-processing-design.md`. These extend `frontend/src/types.ts`; they describe in-memory mock shapes, not persisted schema.

## Enums

```ts
// Aligned exactly with docs/batch-processing-design.md — do not add statuses here
export type FileStatus =
  | 'NEW' | 'UPLOADED' | 'PDF_CONVERTED' | 'PDF_CONVERT_FAILED'
  | 'MARKDOWN_GENERATED' | 'OCR_REQUIRED' | 'LOW_CONFIDENCE'
  | 'REVIEW_REQUIRED' | 'APPROVED' | 'PUBLISHED' | 'FAILED' | 'UNSUPPORTED';

export type ReviewStatus = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED';

export type SourceKind = 'folder' | 'zip';

// Supported source types follow docs/markdown-standard.md
export type SourceType = 'pptx' | 'docx' | 'pdf' | 'xlsx' | 'image' | 'unsupported';

export type UploadFlowState =
  | 'IDLE' | 'INVENTORY_READY' | 'INVENTORY_EMPTY' | 'BATCH_CREATED' | 'REPORT_OPEN';
```

## Entities

### SourceTrace

Mock trace reference preserved on every generated item (subset of `docs/markdown-standard.md`).

```ts
export interface SourceTrace {
  sourceFile: string;      // original file name
  sourcePath: string;      // relative path, never a confidential absolute path
  page?: number;           // mock page
  chunkId?: string;        // mock chunk id, e.g. "batch-...:file-003:page-012:block-02"
}
```

### InventoryFile

One detected file before batch creation.

```ts
export interface InventoryFile {
  path: string;            // original relative path, e.g. "/Discovery/BRD/BRD.docx"
  sourceType: SourceType;
  sizeLabel: string;       // mock human size, e.g. "1.2 MB"
  supported: boolean;      // false → sourceType 'unsupported'
  confidence: number;      // 0–1 mock
  reviewStatus: ReviewStatus; // defaults REVIEW_REQUIRED
}
```

### Inventory

```ts
export interface Inventory {
  sourceKind: SourceKind;
  packageName: string;     // mock package label
  files: InventoryFile[];
  detectedCount: number;
  supportedCount: number;
  unsupportedCount: number;
  truncated: boolean;      // true if capped at display max
}
```

### FileItem

A file inside a created batch (extends inventory row with status + trace).

```ts
export interface FileItem {
  path: string;
  sourceType: SourceType;
  status: FileStatus;
  confidence: number;
  reviewStatus: ReviewStatus;
  sourceTrace: SourceTrace;
  errors?: string[];       // present for FAILED / PDF_CONVERT_FAILED
}
```

### BatchMetrics

```ts
export interface BatchMetrics {
  totalFiles: number;
  pdfConverted: number;
  markdownGenerated: number;
  reviewRequired: number;
  failed: number;
  unsupported: number;
}
```

### BatchProgressStage

```ts
export interface BatchProgressStage {
  label: string;           // e.g. "Office → PDF"
  value: number;           // 0–100 mock percent
}
```

### Batch

```ts
export interface Batch {
  id: string;              // mock id
  name: string;            // source package name
  sourceKind: SourceKind;
  uploadedAt: string;      // mock ISO-ish timestamp
  owner: string;           // mock owner/team
  fileItems: FileItem[];
  metrics: BatchMetrics;
  progress: BatchProgressStage[];
}
```

### ReportEntry / Report

```ts
export interface ReportEntry {
  path: string;
  status: FileStatus;
  confidence: number;
  reviewStatus: ReviewStatus;
  sourceTrace: SourceTrace;
  note?: string;
}

export interface Report {
  batchId: string;
  inventory: ReportEntry[];
  unsupported: ReportEntry[];
  conversionFailures: ReportEntry[];
  lowConfidence: ReportEntry[];
  reviewRequired: ReportEntry[];
  approvedPublished: ReportEntry[];
}
```

## Provider Interface (mock, future adapter seam)

```ts
export interface FolderUploadProvider {
  createInventory(sourceKind: SourceKind): Promise<Inventory>;
  createBatch(inventory: Inventory): Promise<Batch>;
  buildReport(batch: Batch): Promise<Report>;
}
```

Async-shaped so the future real implementation (storage + converter + parser adapters) drops in without changing view call sites. Phase-1 mock resolves immediately with seeded data.

## Invariants

- `FileStatus` values are exactly the allowed set from `docs/batch-processing-design.md`; no new statuses.
- Any item that is generated or below a confidence threshold has `reviewStatus = 'REVIEW_REQUIRED'` (never `APPROVED` by default).
- `sourcePath` is always relative; no absolute or confidential path is ever stored.
- `BatchMetrics` counts are derived from `fileItems`, not stored independently (single source of truth).
- Unsupported files have `status = 'UNSUPPORTED'` and never receive a processing status.

## Relationship To Existing Model

`FileStatus`, `Batch`, and `FileItem` may already have partial definitions in `frontend/src/types.ts` from the `knowledge-space` baseline. Codex must reconcile (extend, not duplicate) rather than create parallel types. If a conflict exists, this data model plus `docs/batch-processing-design.md` are authoritative for the status set.
