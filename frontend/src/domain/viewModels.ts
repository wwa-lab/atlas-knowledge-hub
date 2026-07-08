import type {
  ApiAskRun,
  ApiAuditEvent,
  ApiGraphEvidenceReference,
  ApiGraphNodeType,
  ApiModelCapability,
  ApiReviewStatus,
  ApiSecretStatus,
  ApiWikiPage,
  ManualUrlFetchIntent,
  SafeErrorCode
} from '@/types'

export type ModelCategory = 'all' | 'chat' | 'embedding' | 'rerank' | 'vision' | 'speech'
export type ModelSource = 'Ollama' | 'API'
export type ThinkingFormat = 'none' | 'provider_default' | 'custom'
export type ProductView = 'home' | 'chat' | 'space'
export type SpaceTab = 'docs' | 'connectors' | 'review' | 'wiki' | 'graph'
export type SettingsPanel =
  | 'general'
  | 'profile'
  | 'spaceInfo'
  | 'members'
  | 'audit'
  | 'messages'
  | 'registration'
  | 'api'
  | 'models'
  | 'vector'
  | 'parser'
  | 'storage'
export type PlaceholderSettingsPanel = Exclude<
  SettingsPanel,
  'general' | 'profile' | 'spaceInfo' | 'members' | 'audit' | 'messages' | 'api' | 'models'
>
export type GeneralLanguage = 'zh-CN' | 'en-US'
export type GeneralThemeMode = 'light' | 'dark' | 'system'
export type GeneralInterfaceFont = 'system' | 'pingfang' | 'microsoft'
export type GeneralCodeFont = 'system-mono' | 'sf-mono' | 'jetbrains'
export type GeneralFontSize = 'small' | 'normal' | 'large'
export type MockUploadKind = 'folder' | 'zip'
export type ProductAskMode = 'answered' | 'refusal' | 'review-warning'
export type MockFileStatus =
  | 'PDF_CONVERT_FAILED'
  | 'OCR_REQUIRED'
  | 'LOW_CONFIDENCE'
  | 'REVIEW_REQUIRED'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'UNSUPPORTED'

export interface MockInventoryFile {
  id: string
  path: string
  type: string
  size: string
  supported: boolean
  status: MockFileStatus
  confidence: number
  reviewStatus: 'REVIEW_REQUIRED' | 'APPROVED' | 'PUBLISHED'
  sourceTrace: string
  reason?: string
}

export interface MockUploadSession {
  kind: MockUploadKind
  packageName: string
  files: MockInventoryFile[]
}

export interface ManualUrlDraft {
  url: string
  title: string
  description: string
  fetchIntent: ManualUrlFetchIntent
}

export interface ApiBatchMetricSummary {
  total: number
  failed: number
  reviewRequired: number
  unsupported: number
  markdownGenerated: number
  batchCount: number
  fileCount: number
  chunkCount: number
}

export interface ProductBatchMetrics {
  total: number
  supported: number
  unsupported: number
  failed: number
  ocr: number
  lowConfidence: number
  reviewRequired: number
  approved: number
  published: number
}

export interface ProductProcessingIssue {
  id: string
  label: string
  type: string
  count: number
  status: string
  action: string
  source: string
}

export interface ProductReportSection {
  label: string
  files: MockInventoryFile[]
}

export interface ProductWikiPage {
  id: string
  title: string
  slug: string
  pageType: string
  aliases: string[]
  sourceRefs: string[]
  chunkRefs: string[]
  inLinks: string[]
  outLinks: string[]
  version: number
  sourceMode: string
  refreshPolicy: string
  confidence: number
  reviewStatus: 'PUBLISHED' | 'APPROVED' | 'REVIEW_REQUIRED'
  owner: string
  updatedAt: string
  sourceTrace: string
  entities: string[]
  sections: Array<{
    title: string
    body: string
    sourceTrace: string
    confidence: number
    reviewStatus: 'PUBLISHED' | 'APPROVED' | 'REVIEW_REQUIRED'
  }>
}

export interface ProductGraphNode {
  id: string
  label: string
  type: 'Wiki Page' | 'Entity' | 'Concept' | 'Document' | 'Review Required'
  reviewStatus: 'PUBLISHED' | 'APPROVED' | 'REVIEW_REQUIRED'
  confidence: number
  sourceTrace: string
  detail: string
  x: number
  y: number
}

export interface ProductGraphEdge {
  id: string
  source: string
  target: string
  label: string
  confidence: number
  sourceTrace: string
  reviewStatus: 'PUBLISHED' | 'APPROVED' | 'REVIEW_REQUIRED'
}

export interface SettingsSurface {
  title: string
  status: string
  summary: string
  rows: Array<{ label: string; value: string; note: string }>
}

export interface GeneralSettingsState {
  language: GeneralLanguage
  themeMode: GeneralThemeMode
  interfaceFont: GeneralInterfaceFont
  codeFont: GeneralCodeFont
  fontSize: GeneralFontSize
  memoryEnabled: boolean
}

export interface GeneralOption<T extends string> {
  value: T
  label: string
}

export type MemberRole = 'owner' | 'admin' | 'reviewer' | 'viewer'

export interface SpaceMember {
  id: string
  name: string
  email: string
  role: MemberRole
  joinedAt: string
  removable: boolean
}

export interface PendingInvitation {
  id: string
  email: string
  role: MemberRole
  invitedAt: string
  inviter: string
}

export interface ApiInfoState {
  keyVersion: number
  baseUrl: string
  docsPath: string
  status: string
}

export interface SafeErrorPreview {
  code: SafeErrorCode
  title: string
  description: string
}

export interface MessageIndexStat {
  label: string
  value: string
  note: string
}

export interface AccountProfileRow {
  key: string
  testId: string
  label: string
  value: string
  note: string
}

export interface AuditSummary {
  total: number
  security: number
  denied: number
}

export interface ModelCategoryOption {
  key: ModelCategory
  label: string
}

export interface AddableModelTypeOption {
  key: Exclude<ModelCategory, 'all'>
  label: string
}

export interface ThinkingOption {
  value: ThinkingFormat
  label: string
}

export interface SpaceInfoDraft {
  name: string
  description: string
}

export type SpaceInfoEditableField = keyof SpaceInfoDraft | null

export interface SpaceInfoRow {
  key: string
  label: string
  note: string
  value: string
  field?: keyof SpaceInfoDraft
}

export interface SpaceOperationalMetadata {
  createdAt: string
  storageQuota: string
  storageUsed: string
  storageUsageRate: string
}

export interface ProductSpaceCard {
  id: string
  name: string
  description: string
  documents: number
  reviews: number
  owner: string
  status: string
  wikiPages: number
  source: 'api' | 'sample'
}

export interface CreateSpaceDraft {
  name: string
  description: string
  type: 'document' | 'faq'
  indexStrategy: 'rag' | 'wiki'
  owner: string
}

export interface ProductAskAnswer {
  status: string
  title: string
  body: string
  governanceLabel: string
  governanceReason: string
  reuseHint: string
  evidence: string[]
  warning: string
}

export interface VueModelConfig {
  id: string
  category: Exclude<ModelCategory, 'all'>
  displayName: string
  provider: string
  source: ModelSource
  name: string
  baseUrl: string
  apiKeyStatus: 'configured' | 'not_configured'
  secretStatuses: ApiSecretStatus[]
  supportsMultimodal: boolean
  thinkingFormat: ThinkingFormat
  createdInSettings?: boolean
  sourceLabel?: string
}

export interface VueModelDraft extends VueModelConfig {
  apiKeyEditing: boolean
}

export function mockFileStatusLabel(status: MockFileStatus) {
  const labels: Record<MockFileStatus, string> = {
    PDF_CONVERT_FAILED: 'PDF_CONVERT_FAILED',
    OCR_REQUIRED: 'OCR_REQUIRED',
    LOW_CONFIDENCE: 'LOW_CONFIDENCE',
    REVIEW_REQUIRED: 'REVIEW_REQUIRED',
    APPROVED: 'APPROVED',
    PUBLISHED: 'PUBLISHED',
    UNSUPPORTED: 'UNSUPPORTED'
  }
  return labels[status]
}

export function auditMetadataLabel(metadata: ApiAuditEvent['metadata']) {
  const entries = Object.entries(metadata ?? {})
  if (entries.length === 0) {
    return 'metadata none'
  }
  return entries.map(([key, value]) => `${key}: ${value}`).join(' · ')
}

export function memberRoleLabel(role: MemberRole, options: Array<GeneralOption<MemberRole>>) {
  return options.find(option => option.value === role)?.label ?? role
}

export function memberRoleClass(role: MemberRole) {
  return `role-${role}`
}

export function answerReviewReasonLine(run: ApiAskRun) {
  if (run.answerReviewReason) {
    return `${run.answerReviewReason} · ${run.answerReviewedBy ?? 'reviewer n/a'}`
  }
  return 'No reviewer reason recorded.'
}

export function answerReuseHint(run: ApiAskRun) {
  return run.answerReusable ? 'Approved reusable knowledge.' : 'Not approved reusable knowledge.'
}

export function formatMetricRatio(value: number | null | undefined) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'n/a'
  }
  return `${Math.round(value * 100)}%`
}

export function isPlaceholderSettingsPanel(
  panel: SettingsPanel
): panel is PlaceholderSettingsPanel {
  return (
    panel !== 'general' &&
    panel !== 'profile' &&
    panel !== 'spaceInfo' &&
    panel !== 'members' &&
    panel !== 'messages' &&
    panel !== 'api' &&
    panel !== 'models'
  )
}

export function uniqueValues<T extends string>(values: T[]) {
  return Array.from(new Set(values)).sort()
}

export function productReviewStatus(status: ApiReviewStatus): ProductGraphNode['reviewStatus'] {
  return status === 'PUBLISHED' || status === 'APPROVED' ? status : 'REVIEW_REQUIRED'
}

export function formatWikiReference(ref: ApiWikiPage['sourceRefs'][number]) {
  const label = ref.label ? ` · ${ref.label}` : ''
  const locator = ref.locator ? ` / ${ref.locator}` : ''
  return `${ref.type}: ${ref.id}${label}${locator}`
}

export function wikiSourceTrace(page: ApiWikiPage) {
  const sourceRefs = (page.sourceRefs ?? []).map(formatWikiReference)
  if (sourceRefs.length > 0) {
    return sourceRefs.join('; ')
  }
  return `sources ${page.sourceDocumentIds.join(', ') || 'none'} / ${page.markdownPath}`
}

export function formatGraphEvidenceReference(evidence: ApiGraphEvidenceReference) {
  const referenceType = evidence.referenceType ?? 'SOURCE_CHUNK'
  if (referenceType === 'WIKI_PAGE') {
    return `Wiki page ${evidence.label ?? evidence.wikiPageId ?? 'unknown'} · ${evidence.wikiPageId ?? 'wiki n/a'} · ${evidence.section ?? 'section n/a'} · ${evidence.reviewStatus} · confidence ${evidence.confidence ?? 'n/a'}`
  }
  return `Source chunk ${evidence.sourceChunkId ?? 'chunk n/a'} · ${evidence.sourceFile ?? 'source n/a'} · ${evidence.section ?? 'section n/a'} · ${evidence.reviewStatus} · confidence ${evidence.confidence ?? 'n/a'}`
}

export function productGraphNodeType(type: ApiGraphNodeType): ProductGraphNode['type'] {
  const labels: Record<ApiGraphNodeType, ProductGraphNode['type']> = {
    KNOWLEDGE_SPACE: 'Concept',
    DOCUMENT: 'Document',
    WIKI_PAGE: 'Wiki Page',
    CONCEPT: 'Concept',
    ENTITY: 'Entity',
    SOURCE_CHUNK: 'Document'
  }
  return labels[type]
}

export function modelTypeToCategory(
  type: ApiModelCapability['modelType']
): Exclude<ModelCategory, 'all'> {
  const categories: Record<ApiModelCapability['modelType'], Exclude<ModelCategory, 'all'>> = {
    CHAT: 'chat',
    EMBEDDING: 'embedding',
    RERANK: 'rerank',
    VISION: 'vision',
    SPEECH: 'speech'
  }
  return categories[type]
}

export function productNodeStyle(node: ProductGraphNode) {
  return {
    left: `${node.x}%`,
    top: `${node.y}%`
  }
}

export function modelIcon(category: VueModelConfig['category']) {
  const icons: Record<VueModelConfig['category'], string> = {
    chat: '□',
    embedding: '⌘',
    rerank: '⇅',
    vision: '◉',
    speech: '◇'
  }
  return icons[category]
}

export function modelDetail(model: VueModelConfig) {
  if (model.category === 'embedding') return ' · 向量维度 1024'
  if (model.category === 'rerank') return ' · Top-K rerank'
  if (model.category === 'vision') return ' · 多模态'
  if (model.category === 'speech') return ' · 语音转写'
  return ''
}

export function secretStatusByKey(statuses: ApiSecretStatus[] | undefined, key: string) {
  return statuses?.find(status => status.reference.key === key)
}

export function isSecretConfigured(status: ApiSecretStatus | undefined) {
  return (
    status?.status === 'CONFIGURED' ||
    status?.status === 'ENV_CONFIGURED' ||
    status?.status === 'NOT_REQUIRED'
  )
}

export function modelSecretLabel(model: VueModelConfig) {
  const credential = secretStatusByKey(model.secretStatuses, 'credential')
  if (!credential) return model.apiKeyStatus === 'configured' ? '已配置' : '未配置'
  const labels: Record<ApiSecretStatus['status'], string> = {
    CONFIGURED: '已配置',
    ENV_CONFIGURED: '环境已配置',
    MISSING: '未配置',
    DISABLED: '已禁用',
    NOT_REQUIRED: '无需密钥'
  }
  return labels[credential.status]
}
