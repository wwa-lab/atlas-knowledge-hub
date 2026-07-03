# 数据模型：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。`folder-upload` 切片面向 TypeScript 的领域类型。源自 `docs/03-spec/folder-upload-spec.md` 与 `docs/batch-processing-design.md`。这些扩展 `frontend/src/types.ts`；描述内存 mock 形状，而非持久化 schema。

## 枚举

```ts
// 与 docs/batch-processing-design.md 完全对齐 —— 不要在此新增状态
export type FileStatus =
  | 'NEW' | 'UPLOADED' | 'PDF_CONVERTED' | 'PDF_CONVERT_FAILED'
  | 'MARKDOWN_GENERATED' | 'OCR_REQUIRED' | 'LOW_CONFIDENCE'
  | 'REVIEW_REQUIRED' | 'APPROVED' | 'PUBLISHED' | 'FAILED' | 'UNSUPPORTED';

export type ReviewStatus = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED';

export type SourceKind = 'folder' | 'zip';

// 支持的源类型遵循 docs/markdown-standard.md
export type SourceType = 'pptx' | 'docx' | 'pdf' | 'xlsx' | 'image' | 'unsupported';

export type UploadFlowState =
  | 'IDLE' | 'INVENTORY_READY' | 'INVENTORY_EMPTY' | 'BATCH_CREATED' | 'REPORT_OPEN';
```

## 实体

### SourceTrace

每个生成项保留的 mock 溯源引用（`docs/markdown-standard.md` 的子集）。

```ts
export interface SourceTrace {
  sourceFile: string;      // 原始文件名
  sourcePath: string;      // 相对路径，绝不使用机密绝对路径
  page?: number;           // mock 页码
  chunkId?: string;        // mock chunk id，如 "batch-...:file-003:page-012:block-02"
}
```

### InventoryFile

批次创建前的单个检测文件。

```ts
export interface InventoryFile {
  path: string;            // 原始相对路径，如 "/Discovery/BRD/BRD.docx"
  sourceType: SourceType;
  sizeLabel: string;       // mock 人类可读大小，如 "1.2 MB"
  supported: boolean;      // false → sourceType 'unsupported'
  confidence: number;      // 0–1 mock
  reviewStatus: ReviewStatus; // 默认 REVIEW_REQUIRED
}
```

### Inventory

```ts
export interface Inventory {
  sourceKind: SourceKind;
  packageName: string;     // mock 包标签
  files: InventoryFile[];
  detectedCount: number;
  supportedCount: number;
  unsupportedCount: number;
  truncated: boolean;      // 若封顶到展示上限则为 true
}
```

### FileItem

已创建批次内的文件（在清单行基础上加状态 + 溯源）。

```ts
export interface FileItem {
  path: string;
  sourceType: SourceType;
  status: FileStatus;
  confidence: number;
  reviewStatus: ReviewStatus;
  sourceTrace: SourceTrace;
  errors?: string[];       // 存在于 FAILED / PDF_CONVERT_FAILED
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
  label: string;           // 如 "Office → PDF"
  value: number;           // 0–100 mock 百分比
}
```

### Batch

```ts
export interface Batch {
  id: string;              // mock id
  name: string;            // 源包名
  sourceKind: SourceKind;
  uploadedAt: string;      // mock ISO 风格时间戳
  owner: string;           // mock owner/团队
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

## 提供者接口（mock，未来适配器缝隙）

```ts
export interface FolderUploadProvider {
  createInventory(sourceKind: SourceKind): Promise<Inventory>;
  createBatch(inventory: Inventory): Promise<Batch>;
  buildReport(batch: Batch): Promise<Report>;
}
```

async 形态，使未来真实实现（storage + converter + parser 适配器）无需改视图调用点即可接入。Phase-1 mock 以种子数据立即解析。

## 不变式

- `FileStatus` 值恰为 `docs/batch-processing-design.md` 的允许集合；不新增状态。
- 任何生成或低于置信阈值的项 `reviewStatus = 'REVIEW_REQUIRED'`（默认绝不 `APPROVED`）。
- `sourcePath` 始终相对；绝不存储绝对或机密路径。
- `BatchMetrics` 计数由 `fileItems` 派生，不独立存储（单一真相源）。
- 不支持文件 `status = 'UNSUPPORTED'` 且永不获得处理状态。

## 与既有模型的关系

`FileStatus`、`Batch`、`FileItem` 可能已在 `frontend/src/types.ts` 由 `knowledge-space` 基线有部分定义。Codex 须协调（扩展而非重复），不得创建并行类型。若冲突，本数据模型加 `docs/batch-processing-design.md` 对状态集合具权威性。
