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

export interface ApiSpace {
  id: string
  name: string
  description: string
  type: string
  indexStrategy: string
  owner: string
  status: string
  documentCount: number
  wikiPageCount: number
  reviewCount: number
  createdAt: string
  updatedAt: string
}

export interface ApiBatchMetrics {
  totalFiles: number
  pdfConverted: number
  markdownGenerated: number
  reviewRequired: number
  failed: number
  unsupported: number
}

export interface ApiBatch {
  id: string
  spaceId: string
  name: string
  sourceKind: 'folder' | 'zip'
  owner: string
  uploadedAt: string
  metrics: ApiBatchMetrics
}

export interface ApiFileItem {
  id: string
  batchId: string
  sourcePath: string
  sourceType: SourceType
  status: FileStatus
  confidence: number | null
  reviewStatus: ApiReviewStatus
  pdfPath: string | null
  markdownPath: string | null
  assetsPath: string | null
  errorMessage: string | null
}

export interface ApiSourceChunk {
  id: string
  fileItemId: string
  sourceFile: string
  page: number | null
  section: string | null
  confidence: number | null
  reviewStatus: ApiReviewStatus
}

export interface ApiReviewQueueRepresentative {
  fileId: string
  status: FileStatus
  reviewStatus: ApiReviewStatus
  confidence: number | null
  hasSourceTrace: boolean
}

export interface ApiReviewQueueItem {
  type:
    | 'PARSER_FAILURE'
    | 'OCR_REQUIRED'
    | 'LOW_CONFIDENCE'
    | 'MISSING_SOURCE_TRACE'
    | 'LLM_GENERATED_REVIEW_REQUIRED'
    | 'READY_TO_PUBLISH'
  count: number
  publishBlocked: boolean
  representativeItems: ApiReviewQueueRepresentative[]
}

export interface ApiReviewQueues {
  spaceId: string
  queues: ApiReviewQueueItem[]
}

export interface ApiReview {
  id: number
  targetType: string
  targetId: string
  action: 'APPROVE' | 'NEED_FIX' | 'OCR_REQUIRED'
  reviewer: string
  comment: string | null
  affectedChunks: string[] | null
  createdAt: string
}

export interface ApiWikiPage {
  id: string
  spaceId: string
  title: string
  markdownPath: string
  sourceDocumentIds: string[]
  confidence: number | null
  reviewStatus: Extract<ApiReviewStatus, 'PUBLISHED'>
  owner: string
  lastUpdated: string
}

export interface ApiGraphProjectionRun {
  runId: string
  spaceId: string
  adapterId: string
  scope: string
  status: string
  safeMessage: string | null
  summary: Record<string, number>
}

export interface ApiVectorRun {
  runId: string
  spaceId: string
  batchId: string | null
  adapterKey: string
  operation: string
  status: string
  safeMessage: string | null
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
  page?: number | null
  section?: string | null
  reviewStatus: ApiReviewStatus
  confidence: number | null
  vectorItemKey: string | null
  score: number | null
  createdAt?: string
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
  answerReviewStatus: ApiReviewStatus
  modelRunId: string | null
  safeMessage: string | null
  evidence: ApiAskEvidence[]
  createdAt?: string
  completedAt?: string | null
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
