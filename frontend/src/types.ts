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

export type ReviewStatus =
  'REVIEW_REQUIRED' | 'APPROVED' | 'NEED_FIX' | 'OCR_REQUIRED' | 'PUBLISHED'
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
  reviewStatus: 'Approved' | 'Review Required' | 'Published'
  sourceTrace?: string
}

export type ReviewQueueType =
  | 'PARSER_FAILURE'
  | 'OCR_REQUIRED'
  | 'LOW_CONFIDENCE'
  | 'MISSING_SOURCE_TRACE'
  | 'LLM_GENERATED_REVIEW_REQUIRED'
  | 'READY_TO_PUBLISH'

export interface ReviewQueueItem {
  type: ReviewQueueType
  count: number
  publishBlocked: boolean
  representativeItems: ReviewQueueRepresentativeItem[]
}

export interface ReviewQueueRepresentativeItem {
  fileId: string
  status: FileStatus
  reviewStatus: ReviewStatus
  confidence: number | null
  hasSourceTrace: boolean
}

export interface WikiPageMetadata {
  id: string
  spaceId: string
  title: string
  markdownPath: string
  sourceDocumentIds: string[]
  confidence: number
  reviewStatus: Extract<ReviewStatus, 'PUBLISHED'>
  owner: string
  lastUpdated: string
}

export interface GraphNode {
  id: string
  label: string
  type: 'Wiki Page' | 'Entity' | 'Concept' | 'Document' | 'Review Required'
  x: number
  y: number
  detail: string
}

export type ApiGraphNodeType =
  'KNOWLEDGE_SPACE' | 'DOCUMENT' | 'WIKI_PAGE' | 'CONCEPT' | 'ENTITY' | 'SOURCE_CHUNK'

export type ApiGraphEdgeType =
  | 'CONTAINS'
  | 'DERIVED_FROM'
  | 'MENTIONS'
  | 'DEFINES'
  | 'RELATED_TO'
  | 'BELONGS_TO'
  | 'USES'
  | 'DEPENDS_ON'
  | 'REVIEWED_BY'

export type ApiReviewStatus =
  'REVIEW_REQUIRED' | 'APPROVED' | 'NEED_FIX' | 'OCR_REQUIRED' | 'PUBLISHED'

export interface ApiGraphNode {
  id: string
  label: string
  type: ApiGraphNodeType
  reviewStatus: ApiReviewStatus
  confidence: number | null
  evidenceCount: number
}

export interface ApiGraphEdge {
  id: string
  sourceNodeId: string
  targetNodeId: string
  type: ApiGraphEdgeType
  reviewStatus: ApiReviewStatus
  confidence: number | null
  evidenceCount: number
}

export interface ApiGraphEvidenceReference {
  sourceChunkId: string
  sourceFile: string
  page?: number
  section?: string
  confidence: number | null
  reviewStatus: ApiReviewStatus
}

export interface ApiGraphView {
  spaceId: string
  nodes: ApiGraphNode[]
  edges: ApiGraphEdge[]
  counts: Record<string, number>
}

export interface ApiGraphNodeDetail {
  node: ApiGraphNode
  adjacentNodes: ApiGraphNode[]
  adjacentEdges: ApiGraphEdge[]
  evidenceReferences: ApiGraphEvidenceReference[]
}

export interface ApiEnvelope<T> {
  success: boolean
  data: T | null
  error: { code: string; message: string } | null
  meta: unknown
}

export type AskRunStatus =
  | 'REQUESTED'
  | 'RETRIEVING'
  | 'GENERATING'
  | 'SUCCEEDED'
  | 'NO_EVIDENCE'
  | 'PARTIAL_FAILED'
  | 'FAILED'
export type AskReviewPolicy = 'APPROVED_ONLY' | 'INCLUDE_REVIEW_REQUIRED'

export interface ApiAskEvidence {
  evidenceId: string
  sourceChunkId: string
  fileItemId: string
  sourceFile: string
  page?: number
  section?: string
  reviewStatus: ApiReviewStatus
  confidence: number | null
  vectorItemKey: string
  score: number | null
}

export interface ApiAskRun {
  runId: string
  spaceId: string
  question: string
  status: AskRunStatus
  reviewPolicy: AskReviewPolicy
  mode: 'mock' | 'configured'
  requestedBy: string
  answer: string | null
  answerConfidence: number | null
  answerReviewStatus: Extract<ApiReviewStatus, 'REVIEW_REQUIRED'>
  modelRunId: string | null
  safeMessage: string | null
  evidence: ApiAskEvidence[]
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
