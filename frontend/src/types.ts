export type ViewName = 'home' | 'space'
export type SpaceTab = 'documents' | 'wiki' | 'graph' | 'review' | 'ask'
export type Locale = 'zh' | 'en'
export type ThemeMode = 'day' | 'night'
export type SettingsPanel = 'member' | 'model' | 'api' | 'vector' | 'parser' | 'storage'
export type ModelCategory = 'all' | 'chat' | 'embedding' | 'rerank' | 'vision' | 'speech'

export interface KnowledgeSpace {
  id: string
  name: string
  description: string
  owner: string
  updatedAt: string
  status: 'Ready' | 'Review Required' | 'Parsing'
  documents: number
  wikiPages: number
  reviews: number
}

export interface BatchMetric {
  label: string
  value: number
}

export interface ProgressItem {
  label: string
  value: number
}

export type FileStatus =
  | 'NEW'
  | 'UPLOADED'
  | 'PDF_CONVERTED'
  | 'PDF_CONVERT_FAILED'
  | 'MARKDOWN_GENERATED'
  | 'OCR_REQUIRED'
  | 'LOW_CONFIDENCE'
  | 'REVIEW_REQUIRED'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'FAILED'
  | 'UNSUPPORTED'

export type ReviewStatus = 'REVIEW_REQUIRED' | 'APPROVED' | 'REJECTED'
export type SourceKind = 'folder' | 'zip'
export type SourceType = 'pptx' | 'docx' | 'pdf' | 'xlsx' | 'image' | 'unsupported'
export type UploadFlowState =
  'IDLE' | 'INVENTORY_READY' | 'INVENTORY_EMPTY' | 'BATCH_CREATED' | 'REPORT_OPEN'

export interface SourceTrace {
  sourceFile: string
  sourcePath: string
  page?: number
  chunkId?: string
}

export interface InventoryFile {
  path: string
  sourceType: SourceType
  sizeLabel: string
  supported: boolean
  confidence: number
  reviewStatus: ReviewStatus
  reason?: string
}

export interface Inventory {
  sourceKind: SourceKind
  packageName: string
  files: InventoryFile[]
  detectedCount: number
  supportedCount: number
  unsupportedCount: number
  truncated: boolean
  displayCap: number
}

export interface FileItem {
  path: string
  sourceType: SourceType
  status: FileStatus
  confidence: number
  reviewStatus: ReviewStatus
  sourceTrace: SourceTrace
  errors?: string[]
}

export interface BatchMetrics {
  totalFiles: number
  pdfConverted: number
  markdownGenerated: number
  reviewRequired: number
  failed: number
  unsupported: number
}

export interface BatchProgressStage {
  label: string
  value: number
}

export interface Batch {
  id: string
  name: string
  sourceKind: SourceKind
  uploadedAt: string
  owner: string
  fileItems: FileItem[]
  metrics: BatchMetrics
  progress: BatchProgressStage[]
}

export interface ReportEntry {
  path: string
  status: FileStatus
  confidence: number
  reviewStatus: ReviewStatus
  sourceTrace: SourceTrace
  note?: string
}

export interface Report {
  batchId: string
  inventory: ReportEntry[]
  unsupported: ReportEntry[]
  conversionFailures: ReportEntry[]
  lowConfidence: ReportEntry[]
  reviewRequired: ReportEntry[]
  approvedPublished: ReportEntry[]
}

export interface FolderUploadProvider {
  createInventory(sourceKind: SourceKind): Promise<Inventory>
  createBatch(inventory: Inventory): Promise<Batch>
  buildReport(batch: Batch): Promise<Report>
}

export interface SourceFile {
  path: string
  status: 'converted' | 'markdown' | 'review' | 'ocr' | 'failed'
}

export interface WikiSection {
  id: string
  title: string
  body: string
  confidence: 'High' | 'Medium' | 'Low'
  reviewStatus: 'Approved' | 'Review Required'
}

export interface GraphNode {
  id: string
  label: string
  type: 'Wiki Page' | 'Entity' | 'Concept' | 'Document' | 'Review Required'
  x: number
  y: number
  detail: string
}

export interface AskSource {
  title: string
  page: string
  confidence: 'High' | 'Medium' | 'Low'
}

export interface ModelConfig {
  id: string
  category: Exclude<ModelCategory, 'all'>
  name: string
  provider: string
  sourceType: 'Ollama' | 'API' | 'GitHub Models' | 'Copilot'
  status: 'Configured' | 'Available' | 'Mock'
}
