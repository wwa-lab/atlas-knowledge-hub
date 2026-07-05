<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  approveFile as approveFileApi,
  clearDeepSeekConfiguration,
  createAskRun,
  createSampleBatch,
  getAskRun,
  getDeepSeekConfiguration,
  getGraph,
  getGraphNode,
  getSpace,
  listBatches,
  listChunks,
  listFiles,
  listModelAdapters,
  listSpaces,
  listWikiPages,
  publishFile as publishFileApi,
  getReviewQueues,
  refreshDownstreamEvidenceApi,
  saveDeepSeekConfiguration,
  uploadDocuments
} from '@/api'
import type {
  ApiAskRun,
  ApiBatch,
  ApiFileItem,
  ApiGraphEdge,
  ApiGraphEdgeType,
  ApiGraphNode,
  ApiGraphNodeDetail,
  ApiGraphNodeType,
  ApiGraphView,
  ApiModelCapability,
  ApiModelConfiguration,
  ApiReviewQueues,
  ApiReviewStatus,
  ApiSourceChunk,
  ApiSpace,
  ApiWikiPage
} from '@/types'

type ModelCategory = 'all' | 'chat' | 'embedding' | 'rerank' | 'vision' | 'speech'
type ModelSource = 'Ollama' | 'API'
type ThinkingFormat = 'none' | 'provider_default' | 'custom'
type ProductView = 'home' | 'chat' | 'space'
type SpaceTab = 'docs' | 'review' | 'wiki' | 'graph'
type SettingsPanel =
  'general' | 'members' | 'registration' | 'api' | 'models' | 'vector' | 'parser' | 'storage'
type MockUploadKind = 'folder' | 'zip'
type ProductAskMode = 'answered' | 'refusal' | 'review-warning'
type MockFileStatus =
  | 'PDF_CONVERT_FAILED'
  | 'OCR_REQUIRED'
  | 'LOW_CONFIDENCE'
  | 'REVIEW_REQUIRED'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'UNSUPPORTED'

interface MockInventoryFile {
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

interface MockUploadSession {
  kind: MockUploadKind
  packageName: string
  files: MockInventoryFile[]
}

interface ProductWikiPage {
  id: string
  title: string
  slug: string
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

interface ProductGraphNode {
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

interface ProductGraphEdge {
  id: string
  source: string
  target: string
  label: string
  confidence: number
  sourceTrace: string
  reviewStatus: 'PUBLISHED' | 'APPROVED' | 'REVIEW_REQUIRED'
}

interface SettingsSurface {
  title: string
  status: string
  summary: string
  rows: Array<{ label: string; value: string; note: string }>
}

interface ProductSpaceCard {
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

interface VueModelConfig {
  id: string
  category: Exclude<ModelCategory, 'all'>
  displayName: string
  provider: string
  source: ModelSource
  name: string
  baseUrl: string
  apiKeyStatus: 'configured' | 'not_configured'
  supportsMultimodal: boolean
  thinkingFormat: ThinkingFormat
  createdInSettings?: boolean
  sourceLabel?: string
}

interface VueModelDraft extends VueModelConfig {
  apiKeyEditing: boolean
}

const defaultSpaceId = 'ibm-i-modernization'
const prototypeSrc = '/atlas-prototype.html'

const activeExperience = ref<'atlas' | 'prototype' | 'p0'>('atlas')
const productView = ref<ProductView>('home')
const activeSpaceTab = ref<SpaceTab>('wiki')
const settingsOpen = ref(false)
const settingsPanel = ref<SettingsPanel>('models')
const spaces = ref<ApiSpace[]>([])
const selectedSpace = ref<ApiSpace | null>(null)
const selectedSpaceId = ref(defaultSpaceId)
const batches = ref<ApiBatch[]>([])
const selectedBatchId = ref('')
const files = ref<ApiFileItem[]>([])
const selectedFileId = ref('')
const chunks = ref<ApiSourceChunk[]>([])
const reviewQueues = ref<ApiReviewQueues | null>(null)
const wikiPages = ref<ApiWikiPage[]>([])
const askRun = ref<ApiAskRun | null>(null)
const askQuestion = ref('What evidence was published for the P0 browser flow?')
const workflowMessage = ref('')
const downstreamReady = ref(false)

const isLoadingSpaces = ref(true)
const isLoadingSpace = ref(false)
const isCreatingBatch = ref(false)
const isReviewing = ref(false)
const isPublishing = ref(false)
const isRefreshingEvidence = ref(false)
const isAsking = ref(false)
const spacesError = ref('')
const workflowError = ref('')
const askError = ref('')

const graph = ref<ApiGraphView | null>(null)
const selectedDetail = ref<ApiGraphNodeDetail | null>(null)
const selectedEdge = ref<ApiGraphEdge | null>(null)
const isLoadingGraph = ref(true)
const graphError = ref('')
const graphState = ref<'loading' | 'ready' | 'empty' | 'unauthorized' | 'error'>('loading')
const searchText = ref('')
const nodeTypeFilter = ref<'ALL' | ApiGraphNodeType>('ALL')
const edgeTypeFilter = ref<'ALL' | ApiGraphEdgeType>('ALL')
const reviewStatusFilter = ref<'ALL' | ApiReviewStatus>('ALL')
const evidenceOnly = ref(true)
const hoveredNodeId = ref('')

const modelCategories: Array<{ key: ModelCategory; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'chat', label: '对话' },
  { key: 'embedding', label: 'Embedding' },
  { key: 'rerank', label: 'ReRank' },
  { key: 'vision', label: '视觉' },
  { key: 'speech', label: '语音' }
]
const addableModelTypes = modelCategories.filter(
  (category): category is { key: Exclude<ModelCategory, 'all'>; label: string } =>
    category.key !== 'all'
)
const modelProviderOptions = [
  'DeepSeek',
  'Aliyun DashScope',
  'OpenAI-compatible',
  'Custom API',
  'GitHub Models',
  'Ollama'
]
const thinkingOptions: Array<{ value: ThinkingFormat; label: string }> = [
  { value: 'none', label: '不写入思考参数' },
  { value: 'provider_default', label: '使用 provider 默认格式' },
  { value: 'custom', label: '自定义参数格式' }
]
const models = ref<VueModelConfig[]>([
  {
    id: 'deepseek-flash',
    category: 'chat',
    displayName: 'DeepSeek Flash',
    provider: 'DeepSeek',
    source: 'API',
    name: 'deepseek-v4-flash',
    baseUrl: 'mock://deepseek-compatible',
    apiKeyStatus: 'configured',
    supportsMultimodal: true,
    thinkingFormat: 'none'
  },
  {
    id: 'dashscope-embedding',
    category: 'embedding',
    displayName: 'text-embedding-v4',
    provider: 'Aliyun DashScope',
    source: 'API',
    name: 'text-embedding-v4',
    baseUrl: 'mock://dashscope-compatible',
    apiKeyStatus: 'configured',
    supportsMultimodal: false,
    thinkingFormat: 'none'
  }
])
const modelCapabilities = ref<ApiModelCapability[]>([])
const deepSeekConfiguration = ref<ApiModelConfiguration | null>(null)
const modelApiError = ref('')
const activeModelCategory = ref<ModelCategory>('all')
const isModelAddMenuOpen = ref(false)
const selectedModelId = ref('deepseek-flash')
const modelDraft = ref<VueModelDraft | null>(null)
const apiKeyInput = ref('')
const modelTestStatus = ref('')
const documentUploadInput = ref<{ click: () => void } | null>(null)
const productSpaceFallbacks: ProductSpaceCard[] = [
  {
    id: 'ibm-i-modernization',
    name: 'IBM i Modernization',
    description: '面向 IBM i 现代化项目的文档、Wiki、图谱和可信问答知识空间。',
    documents: 128,
    reviews: 35,
    owner: '我创建',
    status: 'REVIEW_REQUIRED',
    wikiPages: 42,
    source: 'sample'
  },
  {
    id: 'legacy-spec-factory',
    name: 'Legacy Spec Factory',
    description: '从遗留系统证据生成业务规格、接口说明和迁移边界。',
    documents: 64,
    reviews: 12,
    owner: '我创建',
    status: 'REVIEW_REQUIRED',
    wikiPages: 18,
    source: 'sample'
  },
  {
    id: 'ai-engineering-playbook',
    name: 'AI Engineering Playbook',
    description: '评测、RAG、提示词、模型网关、监控和安全实践知识库。',
    documents: 42,
    reviews: 8,
    owner: '我创建',
    status: 'READY',
    wikiPages: 12,
    source: 'sample'
  }
]
const selectedProductSpaceId = ref('ibm-i-modernization')
const selectedChatSpaces = ref(['IBM i Modernization', 'AI Engineering Playbook'])
const uploadSession = ref<MockUploadSession | null>(null)
const activeProductBatchFiles = ref<MockInventoryFile[]>([])
const isProductReportOpen = ref(false)
const selectedProductWikiPageId = ref('wiki-modernization-index')
const selectedProductGraphNodeId = ref('node-wiki')
const productGraphSearch = ref('')
const productAskQuestion = ref('哪些证据支持 IBM i 现代化知识空间可以发布到 Wiki？')
const productAskMode = ref<ProductAskMode>('answered')

const mockFolderInventory: MockInventoryFile[] = [
  {
    id: 'mock-brd',
    path: 'discovery/BRD_Methodology.pdf',
    type: 'PDF',
    size: '4.8 MB',
    supported: true,
    status: 'REVIEW_REQUIRED',
    confidence: 0.82,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012'
  },
  {
    id: 'mock-rpg',
    path: 'analysis/RPG_Scan_Result.xlsx',
    type: 'XLSX',
    size: '2.1 MB',
    supported: true,
    status: 'LOW_CONFIDENCE',
    confidence: 0.67,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42'
  },
  {
    id: 'mock-nightly',
    path: 'jobs/Nightly_Batch.docx',
    type: 'DOCX',
    size: '920 KB',
    supported: true,
    status: 'PDF_CONVERT_FAILED',
    confidence: 0.58,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'Nightly_Batch.docx / section 3',
    reason: 'Office conversion failed safely in mock status.'
  },
  {
    id: 'mock-ocr',
    path: 'screenshots/green-screen-flow.png',
    type: 'PNG',
    size: '1.4 MB',
    supported: true,
    status: 'OCR_REQUIRED',
    confidence: 0.61,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'green-screen-flow.png / image region 2',
    reason: 'Image-heavy source needs OCR before publication.'
  },
  {
    id: 'mock-approved',
    path: 'approved/current-state-summary.md',
    type: 'MD',
    size: '48 KB',
    supported: true,
    status: 'APPROVED',
    confidence: 0.93,
    reviewStatus: 'APPROVED',
    sourceTrace: 'current-state-summary.md / section architecture'
  },
  {
    id: 'mock-published',
    path: 'published/modernization-index.md',
    type: 'MD',
    size: '64 KB',
    supported: true,
    status: 'PUBLISHED',
    confidence: 0.96,
    reviewStatus: 'PUBLISHED',
    sourceTrace: 'modernization-index.md / section index'
  },
  {
    id: 'mock-unsupported',
    path: 'raw/archive/old-export.exe',
    type: 'EXE',
    size: '12 MB',
    supported: false,
    status: 'UNSUPPORTED',
    confidence: 0,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'not generated',
    reason: 'Executable files are excluded from the mock parser pipeline.'
  }
]

const productWikiPages: ProductWikiPage[] = [
  {
    id: 'wiki-modernization-index',
    title: 'IBM i Modernization Index',
    slug: 'modernization-index',
    confidence: 0.96,
    reviewStatus: 'PUBLISHED',
    owner: 'Atlas Delivery',
    updatedAt: '2026-07-05',
    sourceTrace: 'modernization-index.md / section index / chunk wiki-001',
    entities: ['IBM i', 'RPG Program', 'Batch Window', 'Source Trace'],
    sections: [
      {
        title: 'Modernization Scope',
        body: 'The approved knowledge base links discovery notes, RPG scan output, and batch workflow evidence into a single reviewed modernization map.',
        sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012',
        confidence: 0.96,
        reviewStatus: 'PUBLISHED'
      },
      {
        title: 'Batch Workflow',
        body: 'Nightly job documentation is visible but remains blocked from trusted downstream surfaces until conversion failures and OCR-required screenshots are remediated.',
        sourceTrace: 'Nightly_Batch.docx / section 3 / chunk batch-003',
        confidence: 0.88,
        reviewStatus: 'APPROVED'
      }
    ]
  },
  {
    id: 'wiki-source-trace',
    title: 'Source Trace Standard',
    slug: 'source-trace-standard',
    confidence: 0.91,
    reviewStatus: 'APPROVED',
    owner: 'SME Review',
    updatedAt: '2026-07-05',
    sourceTrace: 'current-state-summary.md / section architecture / chunk trace-009',
    entities: ['source_trace', 'SME Review', 'Confidence'],
    sections: [
      {
        title: 'Trace Coverage',
        body: 'Every Wiki section, graph edge, and Ask citation must carry file, page or section, chunk id, confidence, and review status.',
        sourceTrace: 'current-state-summary.md / section architecture / chunk trace-009',
        confidence: 0.91,
        reviewStatus: 'APPROVED'
      }
    ]
  },
  {
    id: 'wiki-review-hold',
    title: 'Unverified RPG Inventory Notes',
    slug: 'review-required-rpg-inventory',
    confidence: 0.67,
    reviewStatus: 'REVIEW_REQUIRED',
    owner: 'Parser pipeline',
    updatedAt: '2026-07-05',
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
    entities: ['RPG Program', 'Low Confidence'],
    sections: [
      {
        title: 'Review Boundary',
        body: 'This page is visible to reviewers as draft knowledge but excluded from trusted Ask answers until an SME approves the low-confidence rows.',
        sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
        confidence: 0.67,
        reviewStatus: 'REVIEW_REQUIRED'
      }
    ]
  }
]

const productGraphNodes: ProductGraphNode[] = [
  {
    id: 'node-wiki',
    label: 'Modernization Index',
    type: 'Wiki Page',
    reviewStatus: 'PUBLISHED',
    confidence: 0.96,
    sourceTrace: 'modernization-index.md / section index / chunk wiki-001',
    detail: 'Published Wiki page created from approved discovery and batch workflow metadata.',
    x: 52,
    y: 30
  },
  {
    id: 'node-ibmi',
    label: 'IBM i',
    type: 'Entity',
    reviewStatus: 'APPROVED',
    confidence: 0.94,
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012',
    detail: 'Core platform entity extracted from approved BRD evidence.',
    x: 26,
    y: 58
  },
  {
    id: 'node-rpg',
    label: 'RPG Program',
    type: 'Concept',
    reviewStatus: 'REVIEW_REQUIRED',
    confidence: 0.67,
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
    detail: 'Low-confidence concept kept visible as review-required and excluded from trusted Ask.',
    x: 72,
    y: 62
  },
  {
    id: 'node-doc',
    label: 'BRD Methodology',
    type: 'Document',
    reviewStatus: 'APPROVED',
    confidence: 0.92,
    sourceTrace: 'BRD_Methodology.pdf / page 12',
    detail: 'Approved source document that provides evidence for the Wiki page and entity nodes.',
    x: 50,
    y: 84
  },
  {
    id: 'node-review',
    label: 'Review Hold',
    type: 'Review Required',
    reviewStatus: 'REVIEW_REQUIRED',
    confidence: 0.61,
    sourceTrace: 'green-screen-flow.png / image region 2',
    detail: 'OCR-required screenshot evidence is intentionally gated before graph trust expansion.',
    x: 84,
    y: 34
  }
]

const productGraphEdges: ProductGraphEdge[] = [
  {
    id: 'edge-wiki-ibmi',
    source: 'node-wiki',
    target: 'node-ibmi',
    label: 'defines',
    confidence: 0.94,
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012',
    reviewStatus: 'APPROVED'
  },
  {
    id: 'edge-wiki-doc',
    source: 'node-wiki',
    target: 'node-doc',
    label: 'derived from',
    confidence: 0.92,
    sourceTrace: 'BRD_Methodology.pdf / page 12',
    reviewStatus: 'APPROVED'
  },
  {
    id: 'edge-rpg-review',
    source: 'node-rpg',
    target: 'node-review',
    label: 'blocked by',
    confidence: 0.61,
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
    reviewStatus: 'REVIEW_REQUIRED'
  }
]

const settingsSurfaces: Record<Exclude<SettingsPanel, 'models'>, SettingsSurface> = {
  general: {
    title: '常规设置',
    status: 'Mock workspace policy',
    summary: '管理产品语言、默认知识空间、审核门禁提示和企业知识库基础策略。',
    rows: [
      {
        label: '默认语言',
        value: '中文 / English ready',
        note: '仅影响当前 Vue shell display copy。'
      },
      { label: '默认空间', value: 'IBM i Modernization', note: 'Mock route，不读取真实租户配置。' },
      { label: '发布策略', value: 'SME approved only', note: '未审核内容保持 review-required。' }
    ]
  },
  members: {
    title: '成员管理',
    status: 'RBAC deferred',
    summary: '展示企业成员、角色意图和审核职责，但不实现生产身份认证或权限决策。',
    rows: [
      { label: 'Workspace Owner', value: 'Atlas Delivery', note: '可配置状态示例，非真实账号。' },
      { label: 'SME Reviewer', value: '3 mock users', note: '用于解释 review workflow。' },
      {
        label: 'Consumer',
        value: 'Read trusted Wiki / Graph / Ask',
        note: '真实 RBAC 留给后续安全切片。'
      }
    ]
  },
  registration: {
    title: '注册策略',
    status: 'Invite only',
    summary: '定义内部 Beta 前的加入策略、域名策略和审批边界，全部为 mock-safe 展示。',
    rows: [
      { label: '加入方式', value: '管理员邀请', note: '不开放自助注册。' },
      { label: '域名策略', value: 'example.internal masked', note: '不提交真实公司域名。' },
      { label: '审批', value: 'Workspace owner approval', note: '真实审批流未实现。' }
    ]
  },
  api: {
    title: 'API 信息',
    status: 'Internal contract preview',
    summary: '展示 API envelope、mock base path 和安全错误策略，不显示 token 或内部 endpoint。',
    rows: [
      { label: 'Envelope', value: 'ApiEnvelope<T>', note: 'success / data / error / meta。' },
      { label: 'Base path', value: '/api', note: '仅展示相对路径，不显示私有 host。' },
      { label: 'Auth', value: 'Deferred', note: '不生成或显示 token。' }
    ]
  },
  vector: {
    title: '向量数据库引擎',
    status: 'Adapter boundary',
    summary: '配置向量能力的产品面状态，不直连 pgvector、Milvus、Qdrant 或外部服务。',
    rows: [
      { label: 'Active adapter', value: 'mock-vector', note: '自动化验证不需要真实向量库。' },
      {
        label: 'Review policy',
        value: 'APPROVED_ONLY',
        note: 'review-required evidence 默认排除。'
      },
      { label: 'Health', value: 'configured metadata only', note: '不执行连接测试。' }
    ]
  },
  parser: {
    title: '解析引擎',
    status: 'Parser adapter',
    summary: '展示 document-normalize 这类内部能力的适配器状态，产品界面不依赖单一实现。',
    rows: [
      { label: 'PDF parser', value: 'mock-parser', note: '真实 parser runtime 留在 adapter 后。' },
      {
        label: 'OCR route',
        value: 'queued when required',
        note: '仅展示状态，不执行 OCR worker。'
      },
      {
        label: 'Trace validator',
        value: 'source_trace required',
        note: '缺失则阻塞 Wiki / Graph / Ask。'
      }
    ]
  },
  storage: {
    title: '存储引擎',
    status: 'Storage adapter',
    summary: '展示对象存储和生成资产路径的产品面配置，不显示 bucket secret 或私有路径。',
    rows: [
      {
        label: 'Object storage',
        value: 'mock-s3-compatible',
        note: '无真实 bucket 或 credential。'
      },
      { label: 'Generated assets', value: 'relative product paths', note: '禁止私有绝对路径。' },
      { label: 'Retention', value: 'review evidence kept', note: '原始机密文档不进入样例数据。' }
    ]
  }
}

const selectedBatch = computed(
  () => batches.value.find(batch => batch.id === selectedBatchId.value) ?? null
)
const selectedFile = computed(
  () => files.value.find(file => file.id === selectedFileId.value) ?? null
)
const selectedChunkIds = computed(() => chunks.value.map(chunk => chunk.id))
const canApprove = computed(
  () =>
    Boolean(selectedFile.value) &&
    selectedFile.value?.reviewStatus !== 'APPROVED' &&
    chunks.value.length > 0
)
const canPublish = computed(() => selectedFile.value?.reviewStatus === 'APPROVED')
const canAsk = computed(() => Boolean(selectedSpaceId.value && askQuestion.value.trim()))
const readyQueueCount = computed(
  () => reviewQueues.value?.queues.find(queue => queue.type === 'READY_TO_PUBLISH')?.count ?? 0
)
const blockedQueueCount = computed(
  () =>
    reviewQueues.value?.queues
      .filter(queue => queue.publishBlocked)
      .reduce((total, queue) => total + queue.count, 0) ?? 0
)

const activeNode = computed(() => selectedDetail.value?.node ?? graph.value?.nodes[0] ?? null)
const activeSelectionLabel = computed(() => {
  if (!selectedEdge.value) {
    return activeNode.value?.label ?? 'No graph object selected'
  }
  return `${edgeNodeLabel(selectedEdge.value.sourceNodeId)} -> ${edgeNodeLabel(selectedEdge.value.targetNodeId)}`
})
const evidenceReferences = computed(() => selectedDetail.value?.evidenceReferences ?? [])
const graphNodes = computed(() => graph.value?.nodes ?? [])
const graphEdges = computed(() => graph.value?.edges ?? [])
const visibleNodes = computed(() => graphNodes.value.filter(matchesNodeFilters))
const visibleNodeIds = computed(() => new Set(visibleNodes.value.map(node => node.id)))
const visibleEdges = computed(() =>
  graphEdges.value.filter(
    edge =>
      visibleNodeIds.value.has(edge.sourceNodeId) &&
      visibleNodeIds.value.has(edge.targetNodeId) &&
      matchesEdgeFilters(edge)
  )
)
const nodeTypeOptions = computed(() => uniqueValues(graphNodes.value.map(node => node.type)))
const edgeTypeOptions = computed(() => uniqueValues(graphEdges.value.map(edge => edge.type)))
const reviewStatusOptions = computed(() =>
  uniqueValues([
    ...graphNodes.value.map(node => node.reviewStatus),
    ...graphEdges.value.map(edge => edge.reviewStatus)
  ])
)
const canvasNodes = computed(() => {
  const count = Math.max(visibleNodes.value.length, 1)
  const radius = 118
  return visibleNodes.value.map((node, index) => {
    const angle = (Math.PI * 2 * index) / count - Math.PI / 2
    return {
      ...node,
      x: Math.round(180 + Math.cos(angle) * radius),
      y: Math.round(150 + Math.sin(angle) * radius)
    }
  })
})
const canvasNodeById = computed(
  () => new Map(canvasNodes.value.map(node => [node.id, node] as const))
)
const visibleModels = computed(() =>
  activeModelCategory.value === 'all'
    ? models.value
    : models.value.filter(model => model.category === activeModelCategory.value)
)
const modelApiStatus = computed(() =>
  modelCapabilities.value.length > 0 ? 'API-backed masked capabilities' : 'Sample model fallback'
)
const selectedProductSpace = computed(
  () =>
    productSpaceCards.value.find(space => space.id === selectedProductSpaceId.value) ??
    productSpaceCards.value[0]
)
const selectedProductApiSpace = computed(
  () => spaces.value.find(space => space.id === selectedProductSpaceId.value) ?? selectedSpace.value
)
const apiBackedProductSpace = computed(() => selectedProductApiSpace.value ?? selectedSpace.value)
const productSpaceCards = computed<ProductSpaceCard[]>(() => {
  const apiCards = spaces.value.map(space => ({
    id: space.id,
    name: space.name,
    description: space.description,
    documents: space.documentCount,
    reviews: space.reviewCount,
    owner: space.owner,
    status: space.status,
    wikiPages: space.wikiPageCount,
    source: 'api' as const
  }))
  const apiIds = new Set(apiCards.map(space => space.id))
  return [...apiCards, ...productSpaceFallbacks.filter(space => !apiIds.has(space.id))]
})
const apiBatchMetrics = computed(() => {
  const totals = batches.value.reduce(
    (acc, batch) => ({
      total: acc.total + (batch.metrics?.totalFiles ?? 0),
      failed: acc.failed + (batch.metrics?.failed ?? 0),
      reviewRequired: acc.reviewRequired + (batch.metrics?.reviewRequired ?? 0),
      unsupported: acc.unsupported + (batch.metrics?.unsupported ?? 0),
      markdownGenerated: acc.markdownGenerated + (batch.metrics?.markdownGenerated ?? 0)
    }),
    { total: 0, failed: 0, reviewRequired: 0, unsupported: 0, markdownGenerated: 0 }
  )
  return {
    ...totals,
    batchCount: batches.value.length,
    fileCount: files.value.length,
    chunkCount: chunks.value.length
  }
})
const reviewQueueCards = computed(() => reviewQueues.value?.queues ?? [])
const apiReadyToPublishCount = computed(
  () => reviewQueueCards.value.find(queue => queue.type === 'READY_TO_PUBLISH')?.count ?? 0
)
const apiBlockedReviewCount = computed(() =>
  reviewQueueCards.value
    .filter(queue => queue.publishBlocked)
    .reduce((total, queue) => total + queue.count, 0)
)
const apiProcessingIssues = computed(() =>
  reviewQueueCards.value.map(queue => ({
    id: queue.type.toLowerCase(),
    label: queue.type.replaceAll('_', ' '),
    type: queue.type,
    count: queue.count,
    status: queue.publishBlocked ? 'blocks Wiki / Graph / Ask' : 'eligible',
    action: queue.publishBlocked ? '查看阻塞项' : '发布 Wiki',
    source: queue.representativeItems[0]?.fileId ?? 'API review queue'
  }))
)
const apiProductWikiPages = computed<ProductWikiPage[]>(() =>
  wikiPages.value.map(page => ({
    id: page.id,
    title: page.title,
    slug: page.markdownPath,
    confidence: page.confidence ?? 0,
    reviewStatus: page.reviewStatus,
    owner: page.owner,
    updatedAt: page.lastUpdated,
    sourceTrace: `sources ${page.sourceDocumentIds.join(', ') || 'none'} / ${page.markdownPath}`,
    entities: ['API Wiki', 'Source Trace', page.reviewStatus],
    sections: [
      {
        title: 'API-backed published metadata',
        body: 'This Wiki page is loaded from the Atlas review-publish API and remains tied to reviewed source documents.',
        sourceTrace: page.sourceDocumentIds.join(', ') || page.markdownPath,
        confidence: page.confidence ?? 0,
        reviewStatus: page.reviewStatus
      }
    ]
  }))
)
const visibleProductWikiPages = computed(() =>
  apiProductWikiPages.value.length > 0 ? apiProductWikiPages.value : productWikiPages
)
const selectedProductWikiPage = computed(
  () =>
    visibleProductWikiPages.value.find(page => page.id === selectedProductWikiPageId.value) ??
    visibleProductWikiPages.value[0]
)
const apiProductGraphNodes = computed<ProductGraphNode[]>(() =>
  graphNodes.value.map((node, index) => ({
    id: node.id,
    label: node.label,
    type: productGraphNodeType(node.type),
    reviewStatus: productReviewStatus(node.reviewStatus),
    confidence: node.confidence ?? 0,
    sourceTrace: `API graph evidence ${node.evidenceCount}`,
    detail: `${node.type} loaded from Atlas graph API with ${node.evidenceCount} evidence reference(s).`,
    x: 18 + (index % 3) * 32,
    y: 20 + Math.floor(index / 3) * 28
  }))
)
const apiProductGraphEdges = computed<ProductGraphEdge[]>(() =>
  graphEdges.value.map(edge => ({
    id: edge.id,
    source: edge.sourceNodeId,
    target: edge.targetNodeId,
    label: edge.type,
    confidence: edge.confidence ?? 0,
    sourceTrace: `API edge evidence ${edge.evidenceCount}`,
    reviewStatus: productReviewStatus(edge.reviewStatus)
  }))
)
const productGraphNodeList = computed(() =>
  apiProductGraphNodes.value.length > 0 ? apiProductGraphNodes.value : productGraphNodes
)
const productGraphEdgeList = computed(() =>
  apiProductGraphNodes.value.length > 0 ? apiProductGraphEdges.value : productGraphEdges
)
const selectedProductGraphNode = computed(
  () =>
    productGraphNodeList.value.find(node => node.id === selectedProductGraphNodeId.value) ??
    productGraphNodeList.value[0]
)
const visibleProductGraphNodes = computed(() => {
  const query = productGraphSearch.value.trim().toLocaleLowerCase()
  if (!query) {
    return productGraphNodeList.value
  }
  return productGraphNodeList.value.filter(
    node =>
      node.label.toLocaleLowerCase().includes(query) ||
      node.type.toLocaleLowerCase().includes(query) ||
      node.reviewStatus.toLocaleLowerCase().includes(query)
  )
})
const visibleProductGraphNodeIds = computed(
  () => new Set(visibleProductGraphNodes.value.map(node => node.id))
)
const visibleProductGraphEdges = computed(() =>
  productGraphEdgeList.value.filter(
    edge =>
      visibleProductGraphNodeIds.value.has(edge.source) &&
      visibleProductGraphNodeIds.value.has(edge.target)
  )
)
const selectedProductGraphEvidence = computed(() => {
  const node = selectedProductGraphNode.value
  const apiEvidence = selectedDetail.value?.evidenceReferences ?? []
  if (apiEvidence.length > 0 && apiProductGraphNodes.value.some(item => item.id === node.id)) {
    return apiEvidence.map(
      evidence =>
        `${evidence.sourceChunkId} · ${evidence.sourceFile} · ${evidence.section ?? 'section n/a'} · ${evidence.reviewStatus} · confidence ${evidence.confidence ?? 'n/a'}`
    )
  }
  return [
    node.sourceTrace,
    ...productGraphEdgeList.value
      .filter(edge => edge.source === node.id || edge.target === node.id)
      .map(edge => edge.sourceTrace)
  ]
})
const productAskAnswer = computed(() => {
  if (productAskMode.value === 'answered' && askRun.value) {
    return {
      status: askRun.value.status,
      title: 'API-backed trusted answer',
      body: askRun.value.answer ?? askRun.value.safeMessage ?? 'Trusted Ask completed safely.',
      evidence: askRun.value.evidence.map(
        evidence =>
          `${evidence.sourceChunkId} · ${evidence.sourceFile} · ${evidence.section ?? 'section n/a'} · ${evidence.reviewStatus} · score ${evidence.score ?? 'n/a'}`
      ),
      warning: `Answer review status ${askRun.value.answerReviewStatus}; model run ${askRun.value.modelRunId ?? 'n/a'}`
    }
  }
  if (productAskMode.value === 'refusal') {
    return {
      status: 'NO_APPROVED_EVIDENCE',
      title: '证据不足，已可信拒答',
      body: '当前问题只命中 OCR required、low confidence 或缺少 source_trace 的内容；这些内容不会进入可信答案。',
      evidence: [] as string[],
      warning: 'Excluded: PDF_CONVERT_FAILED, OCR_REQUIRED, LOW_CONFIDENCE, MISSING_SOURCE_TRACE'
    }
  }
  if (productAskMode.value === 'review-warning') {
    return {
      status: 'REVIEW_REQUIRED',
      title: '答案需要 SME 审核',
      body: '可以基于已溯源内容生成草案，但其中包含 review-required evidence，因此答案不能被视为已发布知识。',
      evidence: [
        'RPG_Scan_Result.xlsx / sheet Programs / row 42 · REVIEW_REQUIRED · confidence 0.67'
      ],
      warning: 'Review-required evidence was explicitly included and marked.'
    }
  }
  return {
    status: 'SUCCEEDED',
    title: '可信答案',
    body: '已发布的 Modernization Index 和已批准的 BRD Methodology 共同支持该知识空间进入 Wiki 和图谱展示；未审核低置信内容仍被排除。',
    evidence: [
      'modernization-index.md / section index / chunk wiki-001 · PUBLISHED · confidence 0.96',
      'BRD_Methodology.pdf / page 12 / chunk brd-012 · APPROVED · confidence 0.94'
    ],
    warning: 'Generated answer remains REVIEW_REQUIRED until SME verification.'
  }
})
const currentSettingsSurface = computed(() =>
  settingsPanel.value === 'models' ? null : settingsSurfaces[settingsPanel.value]
)
const visibleProductBatchFiles = computed(() =>
  activeProductBatchFiles.value.length > 0 ? activeProductBatchFiles.value : mockFolderInventory
)
const productBatchMetrics = computed(() => {
  const files = visibleProductBatchFiles.value
  return {
    total: files.length,
    supported: files.filter(file => file.supported).length,
    unsupported: files.filter(file => file.status === 'UNSUPPORTED').length,
    failed: files.filter(file => file.status === 'PDF_CONVERT_FAILED').length,
    ocr: files.filter(file => file.status === 'OCR_REQUIRED').length,
    lowConfidence: files.filter(file => file.status === 'LOW_CONFIDENCE').length,
    reviewRequired: files.filter(file => file.reviewStatus === 'REVIEW_REQUIRED' && file.supported)
      .length,
    approved: files.filter(file => file.status === 'APPROVED').length,
    published: files.filter(file => file.status === 'PUBLISHED').length
  }
})
const productProcessingIssues = computed(() => [
  {
    id: 'parse-failures',
    label: '解析失败',
    type: 'PDF_CONVERT_FAILED',
    count: productBatchMetrics.value.failed,
    status: 'blocks Wiki / Graph / Ask',
    action: '重试转换',
    source: 'converter stage'
  },
  {
    id: 'ocr-required',
    label: '需要 OCR',
    type: 'OCR_REQUIRED',
    count: productBatchMetrics.value.ocr,
    status: 'excluded until OCR',
    action: '加入 OCR 队列',
    source: 'image-heavy sources'
  },
  {
    id: 'low-confidence',
    label: '低置信度',
    type: 'LOW_CONFIDENCE',
    count: productBatchMetrics.value.lowConfidence,
    status: 'SME review required',
    action: '打开审核',
    source: 'parser confidence'
  },
  {
    id: 'missing-trace',
    label: '缺少 source_trace',
    type: 'MISSING_SOURCE_TRACE',
    count: 2,
    status: 'blocks trusted Ask',
    action: '修复溯源',
    source: 'trace validator'
  },
  {
    id: 'llm-review',
    label: 'LLM 生成需审核',
    type: 'LLM_GENERATED',
    count: productBatchMetrics.value.reviewRequired,
    status: 'review required',
    action: '优先处理',
    source: 'normalization'
  },
  {
    id: 'ready-to-publish',
    label: '待发布',
    type: 'READY_TO_PUBLISH',
    count: productBatchMetrics.value.approved,
    status: 'eligible',
    action: '发布 Wiki',
    source: 'SME approved'
  }
])
const reportSections = computed(() => [
  { label: 'Inventory', files: visibleProductBatchFiles.value },
  { label: 'Unsupported', files: visibleProductBatchFiles.value.filter(file => !file.supported) },
  {
    label: 'Conversion failures',
    files: visibleProductBatchFiles.value.filter(file => file.status === 'PDF_CONVERT_FAILED')
  },
  {
    label: 'OCR required',
    files: visibleProductBatchFiles.value.filter(file => file.status === 'OCR_REQUIRED')
  },
  {
    label: 'Low confidence',
    files: visibleProductBatchFiles.value.filter(file => file.status === 'LOW_CONFIDENCE')
  },
  {
    label: 'Review required',
    files: visibleProductBatchFiles.value.filter(
      file => file.reviewStatus === 'REVIEW_REQUIRED' && file.supported
    )
  }
])

onMounted(() => {
  void initialize()
})

async function initialize() {
  await Promise.all([loadSpaces(), loadGraph(), loadModelCapabilities()])
}

async function loadSpaces() {
  isLoadingSpaces.value = true
  spacesError.value = ''
  try {
    spaces.value = await listSpaces()
    const preferred = spaces.value.find(space => space.id === defaultSpaceId) ?? spaces.value[0]
    if (preferred) {
      await selectSpace(preferred.id)
    }
  } catch (error) {
    spacesError.value = safeError(error, 'Unable to load Knowledge Spaces.')
    await loadSpaceContext(defaultSpaceId)
  } finally {
    isLoadingSpaces.value = false
  }
}

async function loadModelCapabilities() {
  modelApiError.value = ''
  try {
    const [capabilities, configuration] = await Promise.all([
      listModelAdapters(),
      getDeepSeekConfiguration()
    ])
    modelCapabilities.value = capabilities
    deepSeekConfiguration.value = configuration
    if (capabilities.length > 0) {
      models.value = capabilities.map(modelCapabilityToVueModel)
      selectedModelId.value = capabilities[0].modelKey
    }
  } catch (error) {
    modelApiError.value = safeError(error, 'Model capability API unavailable.')
  }
}

async function selectSpace(spaceId: string) {
  selectedSpaceId.value = spaceId
  selectedProductSpaceId.value = spaceId
  await Promise.all([loadSpaceContext(spaceId), loadGraph()])
}

async function loadSpaceContext(spaceId: string) {
  isLoadingSpace.value = true
  workflowError.value = ''
  try {
    const [space, batchList, queues, pages] = await Promise.all([
      getSpace(spaceId),
      listBatches(spaceId),
      getReviewQueues(spaceId),
      listWikiPages(spaceId)
    ])
    selectedSpace.value = space
    batches.value = batchList
    reviewQueues.value = queues
    wikiPages.value = pages
    const firstBatch = batchList[0]
    if (firstBatch) {
      await selectBatch(firstBatch.id)
    } else {
      selectedBatchId.value = ''
      files.value = []
      chunks.value = []
      selectedFileId.value = ''
    }
  } catch (error) {
    workflowError.value = safeError(error, 'Unable to load selected space workflow.')
  } finally {
    isLoadingSpace.value = false
  }
}

async function selectBatch(batchId: string) {
  selectedBatchId.value = batchId
  const fileList = await listFiles(batchId)
  files.value = fileList
  const preferred = fileList.find(file => file.reviewStatus !== 'PUBLISHED') ?? fileList[0]
  if (preferred) {
    await selectFile(preferred.id)
  }
}

async function selectFile(fileId: string) {
  selectedFileId.value = fileId
  chunks.value = await listChunks(fileId)
}

async function createBatchFromBrowser() {
  if (!selectedSpaceId.value) {
    return
  }
  isCreatingBatch.value = true
  workflowError.value = ''
  workflowMessage.value = ''
  const section = `P0 Browser Evidence ${Date.now()}`
  try {
    const batch = await createSampleBatch(selectedSpaceId.value, section)
    workflowMessage.value = `Sample batch created: ${batch.name}`
    await refreshWorkflow(batch.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Sample batch creation failed safely.')
  } finally {
    isCreatingBatch.value = false
  }
}

function openDocumentUpload() {
  documentUploadInput.value?.click()
}

async function handleDocumentUpload(event: { target: unknown }) {
  if (!selectedSpaceId.value) {
    return
  }
  const input = event.target as { files: ArrayLike<unknown> | null; value: string }
  const selectedFiles = Array.from(input.files ?? []) as Parameters<typeof uploadDocuments>[1]
  input.value = ''
  if (selectedFiles.length === 0) {
    return
  }
  isCreatingBatch.value = true
  workflowError.value = ''
  workflowMessage.value = ''
  try {
    const response = await uploadDocuments(selectedSpaceId.value, selectedFiles, 'p0-browser')
    const firstFileId = response.files[0]?.id
    workflowMessage.value = `Uploaded ${response.files.length} file(s) and started ${response.parserRun.adapterKey}.`
    await refreshWorkflow(response.batch.id, firstFileId)
  } catch (error) {
    workflowError.value = safeError(error, 'Document upload failed safely.')
  } finally {
    isCreatingBatch.value = false
  }
}

async function approveSelectedFile() {
  const file = selectedFile.value
  if (!file || chunks.value.length === 0) {
    return
  }
  isReviewing.value = true
  workflowError.value = ''
  try {
    await approveFileApi(file.id, selectedChunkIds.value)
    workflowMessage.value = `Approved ${file.sourcePath}`
    await refreshWorkflow(selectedBatchId.value, file.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Review failed safely.')
  } finally {
    isReviewing.value = false
  }
}

async function publishSelectedFile() {
  const file = selectedFile.value
  if (!file || !canPublish.value) {
    return
  }
  isPublishing.value = true
  workflowError.value = ''
  try {
    const title = `P0 Wiki ${new Date().toISOString().slice(0, 10)}`
    const page = await publishFileApi(file.id, title)
    workflowMessage.value = `Published Wiki page: ${page.title}`
    await refreshWorkflow(selectedBatchId.value, file.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Publish failed safely.')
  } finally {
    isPublishing.value = false
  }
}

async function refreshDownstreamEvidence() {
  const batch = selectedBatch.value
  if (!batch || chunks.value.length === 0) {
    return
  }
  isRefreshingEvidence.value = true
  workflowError.value = ''
  try {
    await refreshDownstreamEvidenceApi(selectedSpaceId.value, batch.id, selectedFileId.value)
    downstreamReady.value = true
    workflowMessage.value = 'Graph and Ask evidence refreshed.'
    await loadGraph(searchText.value)
  } catch (error) {
    workflowError.value = safeError(error, 'Downstream evidence refresh failed safely.')
  } finally {
    isRefreshingEvidence.value = false
  }
}

async function submitAsk() {
  if (!canAsk.value) {
    return
  }
  isAsking.value = true
  askError.value = ''
  askRun.value = null
  try {
    const created = await createAskRun(
      selectedSpaceId.value,
      askQuestion.value.trim(),
      selectedFileId.value
    )
    askRun.value = await getAskRun(created.runId)
  } catch (error) {
    askError.value = safeError(error, 'Trusted Ask failed safely.')
  } finally {
    isAsking.value = false
  }
}

async function submitProductAsk() {
  askQuestion.value = productAskQuestion.value
  productAskMode.value = 'answered'
  await submitAsk()
}

async function refreshWorkflow(batchId = selectedBatchId.value, fileId = selectedFileId.value) {
  const [batchList, queues, pages] = await Promise.all([
    listBatches(selectedSpaceId.value),
    getReviewQueues(selectedSpaceId.value),
    listWikiPages(selectedSpaceId.value)
  ])
  batches.value = batchList
  reviewQueues.value = queues
  wikiPages.value = pages
  const nextBatchId = batchId || batchList[0]?.id || ''
  if (nextBatchId) {
    selectedBatchId.value = nextBatchId
    const fileList = await listFiles(nextBatchId)
    files.value = fileList
    const nextFile = fileList.find(file => file.id === fileId) ?? fileList[0]
    if (nextFile) {
      await selectFile(nextFile.id)
    }
  }
}

async function loadGraph(query = searchText.value) {
  isLoadingGraph.value = true
  graphError.value = ''
  graphState.value = 'loading'
  try {
    const response = await getGraph(selectedSpaceId.value, query)
    graph.value = response
    graphState.value = response.nodes.length === 0 ? 'empty' : 'ready'
    const first = response.nodes[0]
    if (first) {
      await selectNode(first)
    } else {
      selectedDetail.value = null
      selectedEdge.value = null
    }
  } catch (error) {
    graph.value = null
    selectedDetail.value = null
    selectedEdge.value = null
    const message = safeError(error, 'Graph API unavailable.')
    graphError.value = message
    graphState.value = message.toLowerCase().includes('forbidden') ? 'unauthorized' : 'error'
  } finally {
    isLoadingGraph.value = false
  }
}

async function selectNode(node: ApiGraphNode) {
  selectedEdge.value = null
  try {
    selectedDetail.value = await getGraphNode(selectedSpaceId.value, node.id)
  } catch (error) {
    selectedDetail.value = {
      node,
      adjacentNodes: [],
      adjacentEdges: [],
      evidenceReferences: []
    }
    graphError.value = safeError(error, 'Graph detail unavailable.')
  }
}

async function selectEdge(edge: ApiGraphEdge) {
  selectedEdge.value = edge
  const targetNode = graphNodes.value.find(node => node.id === edge.targetNodeId)
  if (targetNode) {
    await selectNode(targetNode)
    selectedEdge.value = edge
  }
}

function matchesNodeFilters(node: ApiGraphNode) {
  const query = searchText.value.trim().toLocaleLowerCase()
  const matchesSearch =
    query.length === 0 ||
    node.label.toLocaleLowerCase().includes(query) ||
    node.type.toLocaleLowerCase().includes(query)
  const matchesType = nodeTypeFilter.value === 'ALL' || node.type === nodeTypeFilter.value
  const matchesReview =
    reviewStatusFilter.value === 'ALL' || node.reviewStatus === reviewStatusFilter.value
  const matchesEvidence =
    !evidenceOnly.value || node.evidenceCount > 0 || node.type === 'KNOWLEDGE_SPACE'
  return matchesSearch && matchesType && matchesReview && matchesEvidence
}

function matchesEdgeFilters(edge: ApiGraphEdge) {
  const query = searchText.value.trim().toLocaleLowerCase()
  const matchesSearch =
    query.length === 0 ||
    edge.type.toLocaleLowerCase().includes(query) ||
    edgeNodeLabel(edge.sourceNodeId).toLocaleLowerCase().includes(query) ||
    edgeNodeLabel(edge.targetNodeId).toLocaleLowerCase().includes(query)
  const matchesType = edgeTypeFilter.value === 'ALL' || edge.type === edgeTypeFilter.value
  const matchesReview =
    reviewStatusFilter.value === 'ALL' || edge.reviewStatus === reviewStatusFilter.value
  const matchesEvidence = !evidenceOnly.value || edge.evidenceCount > 0
  return matchesSearch && matchesType && matchesReview && matchesEvidence
}

function edgeNodeLabel(nodeId: string) {
  return graphNodes.value.find(node => node.id === nodeId)?.label ?? nodeId
}

function uniqueValues<T extends string>(values: T[]) {
  return Array.from(new Set(values)).sort()
}

function productReviewStatus(status: ApiReviewStatus): ProductGraphNode['reviewStatus'] {
  return status === 'PUBLISHED' || status === 'APPROVED' ? status : 'REVIEW_REQUIRED'
}

function productGraphNodeType(type: ApiGraphNodeType): ProductGraphNode['type'] {
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

function modelCapabilityToVueModel(capability: ApiModelCapability): VueModelConfig {
  const runtimeCredential = deepSeekConfiguration.value?.credentialStatus
    .toLowerCase()
    .replace('_', '-')
  const credential =
    capability.adapterKey === 'deepseek'
      ? runtimeCredential && runtimeCredential !== 'missing'
        ? runtimeCredential
        : capability.maskedConfigSummary.credential
      : capability.maskedConfigSummary.credential
  return {
    id: capability.modelKey,
    category: modelTypeToCategory(capability.modelType),
    displayName: capability.displayName,
    provider: capability.providerFamily,
    source: 'API',
    name: capability.modelKey,
    baseUrl:
      capability.adapterKey === 'deepseek'
        ? 'https://api.deepseek.com'
        : (capability.maskedConfigSummary.endpoint ?? ''),
    apiKeyStatus:
      credential === 'configured' || credential === 'env-configured' || credential === 'mock'
        ? 'configured'
        : 'not_configured',
    supportsMultimodal: capability.modelType === 'VISION',
    thinkingFormat: 'none',
    sourceLabel: `${capability.adapterKey} · ${capability.status}`
  }
}

function modelTypeToCategory(type: ApiModelCapability['modelType']): Exclude<ModelCategory, 'all'> {
  const categories: Record<ApiModelCapability['modelType'], Exclude<ModelCategory, 'all'>> = {
    CHAT: 'chat',
    EMBEDDING: 'embedding',
    RERANK: 'rerank',
    VISION: 'vision',
    SPEECH: 'speech'
  }
  return categories[type]
}

function safeError(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}

function showProductHome() {
  productView.value = 'home'
  settingsOpen.value = false
}

function showProductChat() {
  productView.value = 'chat'
  settingsOpen.value = false
}

function openProductSpace(spaceId: string) {
  selectedProductSpaceId.value = spaceId
  productView.value = 'space'
  activeSpaceTab.value = 'wiki'
  settingsOpen.value = false
  if (spaces.value.some(space => space.id === spaceId)) {
    void selectSpace(spaceId)
  }
}

function openMockUpload(kind: MockUploadKind) {
  uploadSession.value = {
    kind,
    packageName: kind === 'folder' ? 'IBM i discovery package' : 'ibm-i-modernization-evidence.zip',
    files: mockFolderInventory.map(file => ({ ...file }))
  }
  isProductReportOpen.value = false
  activeSpaceTab.value = 'docs'
}

function cancelMockUpload() {
  uploadSession.value = null
}

function createProductBatch() {
  const session = uploadSession.value
  if (!session || session.files.every(file => !file.supported)) {
    return
  }
  activeProductBatchFiles.value = session.files.map(file => ({ ...file }))
  uploadSession.value = null
  isProductReportOpen.value = false
}

function statusLabel(status: MockFileStatus) {
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

function openSettings(panel: SettingsPanel = 'general') {
  settingsPanel.value = panel
  settingsOpen.value = true
}

function closeSettings() {
  settingsOpen.value = false
  modelDraft.value = null
}

function toggleChatSpace(name: string) {
  selectedChatSpaces.value = selectedChatSpaces.value.includes(name)
    ? selectedChatSpaces.value.filter(space => space !== name)
    : [...selectedChatSpaces.value, name]
}

function selectProductWikiPage(pageId: string) {
  selectedProductWikiPageId.value = pageId
}

function selectProductGraphNode(nodeId: string) {
  selectedProductGraphNodeId.value = nodeId
  const apiNode = graphNodes.value.find(node => node.id === nodeId)
  if (apiNode) {
    void selectNode(apiNode)
  }
}

function productNodeStyle(node: ProductGraphNode) {
  return {
    left: `${node.x}%`,
    top: `${node.y}%`
  }
}

function modelCount(category: ModelCategory) {
  return category === 'all'
    ? models.value.length
    : models.value.filter(model => model.category === category).length
}

function modelIcon(category: VueModelConfig['category']) {
  const icons: Record<VueModelConfig['category'], string> = {
    chat: '□',
    embedding: '⌘',
    rerank: '⇅',
    vision: '◉',
    speech: '◇'
  }
  return icons[category]
}

function modelDetail(model: VueModelConfig) {
  if (model.category === 'embedding') return ' · 向量维度 1024'
  if (model.category === 'rerank') return ' · Top-K rerank'
  if (model.category === 'vision') return ' · 多模态'
  if (model.category === 'speech') return ' · 语音转写'
  return ''
}

function openModelEditor(model: VueModelConfig) {
  selectedModelId.value = model.id
  isModelAddMenuOpen.value = false
  modelTestStatus.value = ''
  modelDraft.value = { ...model, apiKeyEditing: false }
}

function closeModelEditor() {
  modelDraft.value = null
  modelTestStatus.value = ''
}

function setModelSource(source: ModelSource) {
  if (!modelDraft.value) return
  modelDraft.value = {
    ...modelDraft.value,
    source,
    provider:
      source === 'Ollama'
        ? 'Ollama'
        : modelDraft.value.provider === 'Ollama'
          ? 'DeepSeek'
          : modelDraft.value.provider,
    baseUrl: source === 'Ollama' ? 'http://localhost:11434' : modelDraft.value.baseUrl
  }
}

async function removeApiKey() {
  if (!modelDraft.value) return
  try {
    if (isDeepSeekDraft(modelDraft.value)) {
      deepSeekConfiguration.value = await clearDeepSeekConfiguration()
    }
    modelDraft.value = { ...modelDraft.value, apiKeyStatus: 'not_configured', apiKeyEditing: false }
    apiKeyInput.value = ''
    modelTestStatus.value = 'API key state cleared.'
  } catch (error) {
    modelApiError.value = safeError(error, 'Model key removal failed safely.')
  }
}

function startApiKeyReplace() {
  if (!modelDraft.value) return
  apiKeyInput.value = ''
  modelDraft.value = { ...modelDraft.value, apiKeyEditing: true }
}

function cancelApiKeyReplace() {
  if (!modelDraft.value) return
  apiKeyInput.value = ''
  modelDraft.value = { ...modelDraft.value, apiKeyEditing: false }
}

function confirmApiKeyReplace() {
  if (!modelDraft.value || apiKeyInput.value.trim().length === 0) return
  modelDraft.value = { ...modelDraft.value, apiKeyStatus: 'configured', apiKeyEditing: false }
}

function testModelConnection() {
  if (!modelDraft.value) return
  if (isDeepSeekDraft(modelDraft.value)) {
    modelTestStatus.value =
      modelDraft.value.apiKeyStatus === 'configured'
        ? 'DeepSeek configuration is ready; raw key remains masked.'
        : 'DeepSeek needs an API key before configured Ask can run.'
    return
  }
  if (modelDraft.value.sourceLabel?.startsWith('mock-model')) {
    modelTestStatus.value =
      modelDraft.value.apiKeyStatus === 'configured'
        ? 'Mock connection passed with masked credentials.'
        : 'Mock connection needs configured API key state.'
    return
  }
  modelTestStatus.value = 'Connection test is coming soon for this model type.'
}

function toggleModelMultimodal() {
  if (!modelDraft.value) return
  modelDraft.value = {
    ...modelDraft.value,
    supportsMultimodal: !modelDraft.value.supportsMultimodal
  }
}

async function saveModelEditor() {
  const draft = modelDraft.value
  if (!draft || draft.name.trim().length === 0) return
  const { apiKeyEditing, ...persisted } = {
    ...draft,
    name: draft.name.trim(),
    displayName: draft.displayName.trim() || draft.name.trim(),
    baseUrl: draft.baseUrl.trim()
  }
  void apiKeyEditing
  if (isDeepSeekDraft(draft)) {
    const apiKey = apiKeyInput.value.trim()
    if (apiKey.length === 0 && persisted.apiKeyStatus !== 'configured') {
      modelTestStatus.value = 'DeepSeek API key is required before saving this runtime model.'
      return
    }
    try {
      deepSeekConfiguration.value = await saveDeepSeekConfiguration({
        endpoint: persisted.baseUrl,
        modelName: persisted.name,
        ...(apiKey.length > 0 ? { apiKey } : {})
      })
    } catch (error) {
      modelApiError.value = safeError(error, 'Model configuration save failed safely.')
      return
    }
    persisted.apiKeyStatus =
      deepSeekConfiguration.value.credentialStatus === 'MISSING' ? 'not_configured' : 'configured'
  } else if (persisted.category !== 'chat') {
    persisted.apiKeyStatus = 'not_configured'
  }
  models.value = models.value.map(model => (model.id === persisted.id ? persisted : model))
  selectedModelId.value = persisted.id
  modelTestStatus.value = ''
  apiKeyInput.value = ''
  modelDraft.value = null
}

function isDeepSeekDraft(model: VueModelConfig) {
  return (
    model.provider.toLowerCase().includes('deepseek') || model.sourceLabel?.startsWith('deepseek')
  )
}
</script>

<template>
  <main
    v-if="activeExperience === 'atlas'"
    class="atlas-product-shell"
    data-testid="vue-product-page"
    aria-label="Atlas Knowledge Hub product page"
  >
    <aside class="atlas-sidebar" aria-label="Product navigation">
      <div class="atlas-brand"><span>A</span><strong>Atlas Knowledge Hub</strong></div>
      <button :class="{ active: productView === 'home' }" type="button" @click="showProductHome">
        □ 知识库
      </button>
      <button type="button">✦ 智能体</button>
      <button type="button">∞ 共享空间</button>
      <button :class="{ active: productView === 'chat' }" type="button" @click="showProductChat">
        ◱ 对话
      </button>
      <p>近7天</p>
      <span>外企职场常用语</span>
      <span>选择知识库内容</span>
      <span>用户问候或打招呼</span>
      <p>工作区设置</p>
      <button type="button" @click="openSettings('members')">♙ 成员管理</button>
      <button type="button" @click="openSettings('models')">⬡ 模型管理</button>
      <button type="button" @click="openSettings('vector')">◎ 向量数据库引擎</button>
      <button type="button" @click="openSettings('parser')">▧ 解析引擎</button>
      <button type="button" @click="openSettings('storage')">▱ 存储引擎</button>
      <button type="button" @click="openSettings('general')">⚙ 全部设置</button>
    </aside>

    <section class="atlas-product-main">
      <template v-if="productView === 'home'">
        <header class="atlas-page-head">
          <div>
            <h1>知识库</h1>
            <p>管理企业知识空间、文档包、Wiki、图谱和可信问答上下文。</p>
          </div>
          <button class="atlas-icon-action" type="button" aria-label="新建知识库">□＋</button>
        </header>
        <div class="atlas-library-toolbar">
          <button type="button">♙ 我创建的</button>
          <span>{{ productSpaceCards.length }}</span>
          <span data-testid="vue-api-space-status">
            {{ spaces.length > 0 ? 'API-backed metadata' : 'Sample fallback' }}
          </span>
          <span>⌄</span>
        </div>
        <section class="atlas-library-grid">
          <button
            v-for="space in productSpaceCards"
            :key="space.id"
            class="atlas-library-card"
            :data-testid="`vue-space-card-${space.id}`"
            type="button"
            @click="openProductSpace(space.id)"
          >
            <h2>{{ space.name }}</h2>
            <p>{{ space.description }}</p>
            <div>
              <span>□ {{ space.documents }}</span>
              <span>Wiki {{ space.wikiPages }}</span>
              <span>{{ space.status }}</span>
              <span>⚗</span>
              <span>{{ space.source === 'api' ? 'API' : 'Mock' }}</span>
              <strong>♙ {{ space.owner }}</strong>
            </div>
          </button>
        </section>
      </template>

      <template v-else-if="productView === 'chat'">
        <section class="atlas-chat-view" data-testid="vue-global-chat">
          <h1>Atlas，让你的知识触手可及</h1>
          <p>选择一个或多个知识库作为上下文，再开始基于来源的可信问答。</p>
          <div class="atlas-chat-spaces">
            <button
              v-for="space in productSpaceCards"
              :key="space.id"
              :class="{ active: selectedChatSpaces.includes(space.name) }"
              type="button"
              @click="toggleChatSpace(space.name)"
            >
              {{ selectedChatSpaces.includes(space.name) ? '✓' : '□' }} {{ space.name }}
            </button>
          </div>
          <div class="atlas-chat-box">
            <textarea v-model="productAskQuestion" data-testid="vue-ask-question"></textarea>
            <div>
              <span>知识库({{ selectedChatSpaces.length }})</span>
              <label>
                <span>模型</span>
                <select aria-label="Ask model selector">
                  <option v-for="model in visibleModels" :key="model.id">
                    {{ model.displayName }} · {{ model.sourceLabel ?? 'sample adapter' }}
                  </option>
                </select>
              </label>
              <label>
                <span>场景</span>
                <select v-model="productAskMode" data-testid="vue-ask-mode">
                  <option value="answered">可信回答</option>
                  <option value="refusal">证据不足拒答</option>
                  <option value="review-warning">Review-required warning</option>
                </select>
              </label>
              <button data-testid="vue-api-ask-submit" type="button" @click="submitProductAsk">
                API Ask
              </button>
            </div>
          </div>
          <section class="atlas-ask-answer" data-testid="vue-trusted-ask-answer">
            <header>
              <span>{{ productAskAnswer.status }}</span>
              <h2>{{ productAskAnswer.title }}</h2>
            </header>
            <p>{{ productAskAnswer.body }}</p>
            <strong>Evidence citations</strong>
            <ul v-if="productAskAnswer.evidence.length > 0">
              <li v-for="evidence in productAskAnswer.evidence" :key="evidence">{{ evidence }}</li>
            </ul>
            <p v-else class="atlas-inline-warning">No approved evidence citations available.</p>
            <small>{{ productAskAnswer.warning }}</small>
          </section>
          <small
            >未通过处理中心门禁的解析失败、低置信或缺少 source_trace 内容不会进入对话索引。</small
          >
        </section>
      </template>

      <template v-else>
        <div class="atlas-crumbs">
          知识库 / <strong>{{ selectedProductSpace.name }}</strong>
        </div>
        <header class="atlas-space-head" data-testid="vue-space-head">
          <div>
            <h1>{{ selectedProductSpace.name }}</h1>
            <p>{{ selectedProductSpace.description }}</p>
            <span>{{
              selectedProductSpace.source === 'api' ? 'API-backed Space' : 'Mock Space'
            }}</span>
            <span>{{ selectedProductSpace.reviews }} Review Required</span>
            <span>{{ selectedProductSpace.documents }} Documents</span>
            <span>{{ selectedProductSpace.wikiPages }} Wiki Pages</span>
            <span v-if="workflowError" class="atlas-inline-warning">{{ workflowError }}</span>
          </div>
          <div>
            <button type="button" @click="showProductHome">返回知识库</button>
            <button data-testid="vue-upload-folder" type="button" @click="openMockUpload('folder')">
              上传文件夹
            </button>
            <button data-testid="vue-upload-zip" type="button" @click="openMockUpload('zip')">
              上传 ZIP
            </button>
          </div>
        </header>
        <nav class="atlas-space-tabs" aria-label="Knowledge Space tabs">
          <button
            :class="{ active: activeSpaceTab === 'docs' }"
            type="button"
            @click="activeSpaceTab = 'docs'"
          >
            文档
          </button>
          <button
            :class="{ active: activeSpaceTab === 'review' }"
            type="button"
            @click="activeSpaceTab = 'review'"
          >
            处理中心
          </button>
          <button
            :class="{ active: activeSpaceTab === 'wiki' }"
            type="button"
            @click="activeSpaceTab = 'wiki'"
          >
            Wiki
          </button>
          <button
            :class="{ active: activeSpaceTab === 'graph' }"
            type="button"
            @click="activeSpaceTab = 'graph'"
          >
            图谱
          </button>
        </nav>
        <section class="atlas-space-panel" data-testid="vue-space-detail">
          <div v-if="activeSpaceTab === 'docs'" class="atlas-doc-layout">
            <aside>
              <h3>批次状态</h3>
              <button>API Batches {{ apiBatchMetrics.batchCount }}</button>
              <button>API Files {{ apiBatchMetrics.fileCount }}</button>
              <button>API Chunks {{ apiBatchMetrics.chunkCount }}</button>
              <button>Inventory {{ productBatchMetrics.total }}</button>
              <button>Supported {{ productBatchMetrics.supported }}</button>
              <button>Unsupported {{ productBatchMetrics.unsupported }}</button>
              <button>Review Required {{ productBatchMetrics.reviewRequired }}</button>
              <button
                data-testid="vue-view-report"
                type="button"
                @click="isProductReportOpen = true"
              >
                查看报告
              </button>
            </aside>
            <div class="atlas-upload-workflow" data-testid="vue-upload-workflow">
              <section class="atlas-api-metadata" data-testid="vue-api-metadata">
                <header>
                  <div>
                    <h2>API-backed metadata</h2>
                    <p>
                      {{ apiBackedProductSpace?.name ?? selectedProductSpace.name }} · upload,
                      parse, review and downstream refresh use Atlas API data.
                    </p>
                  </div>
                  <input
                    ref="documentUploadInput"
                    data-testid="vue-api-upload-input"
                    type="file"
                    accept=".pdf,.zip,application/pdf,application/zip"
                    multiple
                    hidden
                    @change="handleDocumentUpload"
                  />
                  <button
                    data-testid="vue-api-upload-documents"
                    type="button"
                    :disabled="isCreatingBatch || !apiBackedProductSpace"
                    @click="openDocumentUpload"
                  >
                    {{ isCreatingBatch ? 'Uploading...' : 'Upload PDF / ZIP' }}
                  </button>
                  <button
                    class="secondary"
                    data-testid="vue-api-create-batch"
                    type="button"
                    :disabled="isCreatingBatch || !apiBackedProductSpace"
                    @click="createBatchFromBrowser"
                  >
                    Create sample batch
                  </button>
                  <button
                    class="coming-soon-button"
                    data-testid="coming-soon"
                    type="button"
                    disabled
                  >
                    Office / OCR coming soon
                  </button>
                </header>
                <div class="atlas-metric-strip">
                  <span>Batches {{ apiBatchMetrics.batchCount }}</span>
                  <span>Files {{ apiBatchMetrics.fileCount }}</span>
                  <span>Chunks {{ apiBatchMetrics.chunkCount }}</span>
                  <span>Markdown {{ apiBatchMetrics.markdownGenerated }}</span>
                  <span>Review required {{ apiBatchMetrics.reviewRequired }}</span>
                </div>
                <p v-if="workflowMessage">{{ workflowMessage }}</p>
                <p v-if="workflowError" class="atlas-inline-warning">{{ workflowError }}</p>
                <div class="atlas-api-grid">
                  <article>
                    <strong>Batches</strong>
                    <p v-if="batches.length === 0">No API batches yet.</p>
                    <button
                      v-for="batch in batches"
                      :key="batch.id"
                      type="button"
                      @click="selectBatch(batch.id)"
                    >
                      {{ batch.name }} · {{ batch.sourceKind }} ·
                      {{ batch.metrics.totalFiles }} files
                    </button>
                  </article>
                  <article>
                    <strong>Files</strong>
                    <p v-if="files.length === 0">No API files yet.</p>
                    <button
                      v-for="file in files"
                      :key="file.id"
                      type="button"
                      @click="selectFile(file.id)"
                    >
                      {{ file.sourcePath }} · {{ file.status }} · {{ file.reviewStatus }}
                    </button>
                  </article>
                  <article>
                    <strong>Source chunks</strong>
                    <p v-if="chunks.length === 0">No API chunks yet.</p>
                    <ul>
                      <li v-for="chunk in chunks" :key="chunk.id">
                        {{ chunk.id }} · {{ chunk.sourceFile }} ·
                        {{ chunk.section ?? 'section n/a' }} · confidence
                        {{ chunk.confidence ?? 'n/a' }} · {{ chunk.reviewStatus }}
                      </li>
                    </ul>
                  </article>
                </div>
              </section>

              <section
                v-if="uploadSession"
                class="atlas-upload-review"
                data-testid="vue-upload-review"
              >
                <header>
                  <div>
                    <h2>
                      Upload Review · {{ uploadSession.kind === 'folder' ? 'Folder' : 'ZIP' }}
                    </h2>
                    <p>
                      {{ uploadSession.packageName }} · detected
                      {{ uploadSession.files.length }} files
                    </p>
                  </div>
                  <button type="button" @click="cancelMockUpload">Cancel</button>
                </header>
                <div class="atlas-inventory-list">
                  <article
                    v-for="file in uploadSession.files"
                    :key="file.id"
                    class="atlas-inventory-row"
                    data-testid="vue-inventory-row"
                  >
                    <strong>{{ file.path }}</strong>
                    <span>{{ file.type }} · {{ file.size }}</span>
                    <span
                      :class="[
                        'atlas-status-badge',
                        file.status.toLowerCase().replaceAll('_', '-')
                      ]"
                    >
                      {{ statusLabel(file.status) }}
                    </span>
                    <span
                      >confidence {{ file.confidence.toFixed(2) }} · {{ file.reviewStatus }}</span
                    >
                    <small v-if="file.reason">{{ file.reason }}</small>
                  </article>
                </div>
                <p
                  v-if="uploadSession.files.every(file => !file.supported)"
                  class="atlas-inline-warning"
                >
                  No supported files to process.
                </p>
                <button
                  data-testid="vue-create-batch"
                  type="button"
                  :disabled="uploadSession.files.every(file => !file.supported)"
                  @click="createProductBatch"
                >
                  Create Batch
                </button>
              </section>

              <section class="atlas-batch-summary" data-testid="vue-batch-summary">
                <header>
                  <div>
                    <h2>文件树与解析状态</h2>
                    <p>Mock batch keeps source_trace, confidence, and review status visible.</p>
                  </div>
                  <span>{{
                    activeProductBatchFiles.length > 0 ? 'Batch created' : 'Seeded preview'
                  }}</span>
                </header>
                <div class="atlas-metric-strip">
                  <span>Total {{ productBatchMetrics.total }}</span>
                  <span>Failed {{ productBatchMetrics.failed }}</span>
                  <span>OCR {{ productBatchMetrics.ocr }}</span>
                  <span>Low confidence {{ productBatchMetrics.lowConfidence }}</span>
                  <span>Published {{ productBatchMetrics.published }}</span>
                </div>
                <table>
                  <tbody>
                    <tr
                      v-for="file in visibleProductBatchFiles"
                      :key="file.id"
                      data-testid="vue-file-row"
                    >
                      <td>{{ file.path }}</td>
                      <td>{{ statusLabel(file.status) }}</td>
                      <td>{{ file.confidence.toFixed(2) }}</td>
                      <td>{{ file.sourceTrace }}</td>
                    </tr>
                  </tbody>
                </table>
              </section>

              <section
                v-if="isProductReportOpen"
                class="atlas-report-panel"
                data-testid="vue-batch-report"
              >
                <header>
                  <h2>Batch Report</h2>
                  <button type="button" @click="isProductReportOpen = false">Close</button>
                </header>
                <div class="atlas-report-grid">
                  <article v-for="section in reportSections" :key="section.label">
                    <strong>{{ section.label }} · {{ section.files.length }}</strong>
                    <p v-if="section.files.length === 0">No items.</p>
                    <ul>
                      <li v-for="file in section.files" :key="file.id">
                        {{ file.path }} · {{ statusLabel(file.status) }} · confidence
                        {{ file.confidence.toFixed(2) }} · {{ file.reviewStatus }}
                        <br />
                        source_trace: {{ file.sourceTrace }}
                      </li>
                    </ul>
                  </article>
                </div>
              </section>
            </div>
          </div>
          <div v-else-if="activeSpaceTab === 'review'" class="atlas-review-layout">
            <section class="atlas-processing-overview" data-testid="vue-processing-center">
              <article>
                <strong>{{ apiBlockedReviewCount }}</strong
                ><span>API blocked queues</span>
              </article>
              <article>
                <strong>{{ apiReadyToPublishCount }}</strong
                ><span>API ready to publish</span>
              </article>
              <article>
                <strong>{{ productBatchMetrics.total }}</strong
                ><span>total documents</span>
              </article>
              <article>
                <strong>{{ productBatchMetrics.failed }}</strong
                ><span>parse failures</span>
              </article>
              <article>
                <strong>{{ productBatchMetrics.ocr }}</strong
                ><span>OCR required</span>
              </article>
              <article>
                <strong>{{ productBatchMetrics.lowConfidence }}</strong
                ><span>low confidence</span>
              </article>
              <article><strong>2</strong><span>missing source_trace</span></article>
              <article>
                <strong>{{ productBatchMetrics.approved }}</strong
                ><span>ready to publish</span>
              </article>
            </section>
            <section class="atlas-api-review-queues" data-testid="vue-api-review-queues">
              <header>
                <h2>API review queues</h2>
                <span>{{
                  reviewQueueCards.length > 0 ? 'ApiEnvelope connected' : 'No API queues'
                }}</span>
                <button
                  data-testid="vue-api-approve-file"
                  type="button"
                  :disabled="!canApprove || isReviewing"
                  @click="approveSelectedFile"
                >
                  {{ isReviewing ? 'Approving...' : 'Approve API file' }}
                </button>
              </header>
              <article v-for="issue in apiProcessingIssues" :key="issue.id">
                <div>
                  <strong>{{ issue.label }}</strong>
                  <span>{{ issue.type }} · {{ issue.source }}</span>
                </div>
                <span>{{ issue.count }}</span>
                <span>{{ issue.status }}</span>
                <button type="button">{{ issue.action }}</button>
              </article>
            </section>
            <section class="atlas-processing-queues">
              <article
                v-for="issue in productProcessingIssues"
                :key="issue.id"
                data-testid="vue-processing-issue"
              >
                <div>
                  <strong>{{ issue.label }}</strong>
                  <span>{{ issue.type }} · {{ issue.source }}</span>
                </div>
                <span>{{ issue.count }}</span>
                <span>{{ issue.status }}</span>
                <button type="button">{{ issue.action }}</button>
              </article>
            </section>
          </div>
          <div v-else-if="activeSpaceTab === 'wiki'" class="atlas-wiki-layout">
            <aside data-testid="vue-wiki-index">
              <input placeholder="搜索 Wiki 页面..." />
              <button
                v-for="page in visibleProductWikiPages"
                :key="page.id"
                :class="{ active: selectedProductWikiPage.id === page.id }"
                type="button"
                @click="selectProductWikiPage(page.id)"
              >
                <strong>{{ page.title }}</strong>
                <span>{{ page.reviewStatus }} · confidence {{ page.confidence.toFixed(2) }}</span>
              </button>
            </aside>
            <article data-testid="vue-wiki-page">
              <header class="atlas-wiki-head">
                <div>
                  <h2>{{ selectedProductWikiPage.title }}</h2>
                  <p>{{ selectedProductWikiPage.slug }} · {{ selectedProductWikiPage.owner }}</p>
                </div>
                <span>{{ selectedProductWikiPage.reviewStatus }}</span>
                <button
                  data-testid="vue-api-publish-file"
                  type="button"
                  :disabled="!canPublish || isPublishing"
                  @click="publishSelectedFile"
                >
                  {{ isPublishing ? 'Publishing...' : 'Publish API Wiki' }}
                </button>
              </header>
              <div class="atlas-wiki-meta">
                <span>confidence {{ selectedProductWikiPage.confidence.toFixed(2) }}</span>
                <span>updated {{ selectedProductWikiPage.updatedAt }}</span>
                <span>source_trace: {{ selectedProductWikiPage.sourceTrace }}</span>
              </div>
              <nav class="atlas-entity-links" aria-label="Wiki entity links">
                <button
                  v-for="entity in selectedProductWikiPage.entities"
                  :key="entity"
                  type="button"
                >
                  {{ entity }}
                </button>
              </nav>
              <section
                v-for="section in selectedProductWikiPage.sections"
                :key="section.title"
                class="atlas-wiki-section"
              >
                <h3>{{ section.title }}</h3>
                <p>{{ section.body }}</p>
                <div>
                  <strong>source_trace</strong>
                  <span>{{ section.sourceTrace }}</span>
                  <span
                    >{{ section.reviewStatus }} · confidence
                    {{ section.confidence.toFixed(2) }}</span
                  >
                </div>
              </section>
            </article>
          </div>
          <div v-else class="atlas-graph-layout" data-testid="vue-product-graph">
            <section>
              <header class="atlas-product-graph-head">
                <div>
                  <h2>Knowledge Graph</h2>
                  <p>Graph belongs to this Knowledge Space and keeps evidence visible.</p>
                </div>
                <input
                  v-model="productGraphSearch"
                  data-testid="vue-graph-search"
                  placeholder="Search node, type, or review state"
                />
              </header>
              <div class="atlas-graph-canvas">
                <button
                  v-for="node in visibleProductGraphNodes"
                  :key="node.id"
                  :class="[
                    'atlas-product-node',
                    node.reviewStatus.toLowerCase().replaceAll('_', '-'),
                    { active: selectedProductGraphNode.id === node.id }
                  ]"
                  :style="productNodeStyle(node)"
                  type="button"
                  data-testid="vue-graph-node"
                  @click="selectProductGraphNode(node.id)"
                >
                  <strong>{{ node.label }}</strong>
                  <span>{{ node.type }}</span>
                </button>
                <span
                  v-for="edge in visibleProductGraphEdges"
                  :key="edge.id"
                  class="atlas-product-edge"
                >
                  {{ edge.label }} · confidence {{ edge.confidence.toFixed(2) }}
                </span>
              </div>
              <div class="atlas-graph-legend">
                <span>Wiki Page</span>
                <span>Entity</span>
                <span>Concept</span>
                <span>Document</span>
                <span>Review Required</span>
              </div>
            </section>
            <aside data-testid="vue-graph-detail">
              <h3>{{ selectedProductGraphNode.label }}</h3>
              <p>{{ selectedProductGraphNode.detail }}</p>
              <dl>
                <dt>Type</dt>
                <dd>{{ selectedProductGraphNode.type }}</dd>
                <dt>Review</dt>
                <dd>{{ selectedProductGraphNode.reviewStatus }}</dd>
                <dt>Confidence</dt>
                <dd>{{ selectedProductGraphNode.confidence.toFixed(2) }}</dd>
              </dl>
              <strong>Evidence / source trace</strong>
              <ul>
                <li v-for="evidence in selectedProductGraphEvidence" :key="evidence">
                  {{ evidence }}
                </li>
              </ul>
              <p class="atlas-inline-warning">
                Review Required nodes express trust boundaries and are excluded from trusted Ask.
              </p>
            </aside>
          </div>
        </section>
      </template>
    </section>

    <section v-if="settingsOpen" class="atlas-settings-modal" role="dialog" aria-modal="true">
      <div class="atlas-settings-window">
        <aside class="vue-settings-rail" aria-label="Settings navigation">
          <h2>设置</h2>
          <p>账户</p>
          <button
            :class="{ active: settingsPanel === 'general' }"
            type="button"
            @click="settingsPanel = 'general'"
          >
            常规设置
          </button>
          <button type="button">用户信息</button>
          <button
            :class="{ active: settingsPanel === 'api' }"
            type="button"
            @click="settingsPanel = 'api'"
          >
            API 信息
          </button>
          <p>空间</p>
          <button
            :class="{ active: settingsPanel === 'members' }"
            type="button"
            @click="settingsPanel = 'members'"
          >
            成员管理
          </button>
          <button
            :class="{ active: settingsPanel === 'registration' }"
            type="button"
            @click="settingsPanel = 'registration'"
          >
            注册配置
          </button>
          <p>模型</p>
          <button
            :class="{ active: settingsPanel === 'models' }"
            type="button"
            @click="settingsPanel = 'models'"
          >
            模型管理
          </button>
          <p>数据与扩展</p>
          <button
            :class="{ active: settingsPanel === 'vector' }"
            type="button"
            @click="settingsPanel = 'vector'"
          >
            向量数据库引擎
          </button>
          <button
            :class="{ active: settingsPanel === 'parser' }"
            type="button"
            @click="settingsPanel = 'parser'"
          >
            解析引擎
          </button>
          <button
            :class="{ active: settingsPanel === 'storage' }"
            type="button"
            @click="settingsPanel = 'storage'"
          >
            存储引擎
          </button>
        </aside>

        <section v-if="settingsPanel !== 'models'" class="atlas-settings-placeholder">
          <button class="atlas-settings-close" type="button" @click="closeSettings">×</button>
          <header class="atlas-admin-head" data-testid="vue-admin-panel">
            <div>
              <h1>{{ currentSettingsSurface?.title }}</h1>
              <p>{{ currentSettingsSurface?.summary }}</p>
            </div>
            <span>{{ currentSettingsSurface?.status }}</span>
          </header>
          <section class="atlas-admin-grid">
            <article v-for="row in currentSettingsSurface?.rows" :key="row.label">
              <strong>{{ row.label }}</strong>
              <span>{{ row.value }}</span>
              <p>{{ row.note }}</p>
            </article>
          </section>
          <section class="atlas-admin-boundary">
            <strong>Production boundary</strong>
            <p>
              当前面板只展示 mock-safe 配置状态；不会保存真实密钥、不会调用外部 provider、
              不执行生产 RBAC，也不会展示私有 endpoint 或本地绝对路径。
            </p>
          </section>
        </section>

        <section v-else class="vue-model-content" data-testid="vue-model-manager">
          <button class="atlas-settings-close" type="button" @click="closeSettings">×</button>
          <header class="vue-model-head">
            <div>
              <h1>模型配置</h1>
              <p>管理不同类型的 AI 模型，支持 Ollama 本地模型和远程 API。</p>
            </div>
            <div class="vue-model-actions">
              <button
                class="vue-add-model"
                data-testid="vue-add-model"
                type="button"
                @click="isModelAddMenuOpen = !isModelAddMenuOpen"
              >
                + 添加模型
              </button>
              <div v-if="isModelAddMenuOpen" class="vue-add-menu" role="menu">
                <button
                  v-for="category in addableModelTypes"
                  :key="category.key"
                  :data-testid="`vue-add-${category.key}`"
                  type="button"
                  disabled
                  data-coming-soon="true"
                >
                  {{ category.label }} · coming soon
                </button>
              </div>
            </div>
          </header>

          <section class="vue-model-info">
            <strong>内置模型</strong>
            <span data-testid="vue-api-model-status">{{ modelApiStatus }}</span>
            <p>内置模型对所有租户可见，敏感信息会被隐藏，当前界面不会展示完整密钥。</p>
            <p v-if="modelApiError" class="atlas-inline-warning">{{ modelApiError }}</p>
            <a href="#" aria-label="查看内置模型管理指南">查看内置模型管理指南 ↗</a>
          </section>

          <nav class="vue-model-tabs" aria-label="Model categories">
            <button
              v-for="category in modelCategories"
              :key="category.key"
              :class="{ active: activeModelCategory === category.key }"
              :data-testid="`vue-model-tab-${category.key}`"
              type="button"
              @click="activeModelCategory = category.key"
            >
              {{ category.label }}({{ modelCount(category.key) }})
            </button>
          </nav>

          <section class="vue-model-grid" aria-label="Configured models">
            <button
              v-for="model in visibleModels"
              :key="model.id"
              class="vue-model-card"
              :data-testid="`vue-model-card-${model.id}`"
              type="button"
              @click="openModelEditor(model)"
            >
              <span class="vue-model-icon">{{ modelIcon(model.category) }}</span>
              <span>
                <strong>{{ model.displayName }}</strong>
                <small
                  >{{ model.name }} · {{ model.provider }}{{ modelDetail(model) }} ·
                  {{ model.sourceLabel ?? 'sample' }}</small
                >
              </span>
            </button>
          </section>
        </section>
      </div>
    </section>

    <div v-if="modelDraft" class="vue-editor-scrim" @click="closeModelEditor"></div>
    <aside
      v-if="modelDraft"
      class="vue-model-editor"
      data-testid="vue-model-editor"
      role="dialog"
      aria-modal="true"
      aria-label="编辑模型"
    >
      <header class="vue-editor-head">
        <span class="vue-model-icon">{{ modelIcon(modelDraft.category) }}</span>
        <div>
          <h2>编辑模型</h2>
          <p>配置用于对话的大语言模型</p>
        </div>
      </header>

      <div class="vue-editor-body">
        <section>
          <h3>模型来源</h3>
          <div class="vue-source-toggle">
            <button
              :class="{ active: modelDraft.source === 'Ollama' }"
              type="button"
              @click="setModelSource('Ollama')"
            >
              Ollama
            </button>
            <button
              :class="{ active: modelDraft.source === 'API' }"
              type="button"
              @click="setModelSource('API')"
            >
              API
            </button>
          </div>
        </section>

        <section>
          <h3>接入配置</h3>
          <label>
            服务商
            <select v-model="modelDraft.provider" data-testid="vue-model-provider">
              <option v-for="provider in modelProviderOptions" :key="provider" :value="provider">
                {{ provider }}
              </option>
            </select>
          </label>
          <label class="required">
            模型名称
            <input v-model="modelDraft.name" data-testid="vue-model-name" />
          </label>
          <label>
            显示名称（可选）
            <input v-model="modelDraft.displayName" data-testid="vue-model-display-name" />
            <small>仅用于界面展示，实际调用仍使用上面的模型名称。</small>
          </label>
          <label class="required">
            Base URL
            <input v-model="modelDraft.baseUrl" data-testid="vue-model-base-url" />
          </label>
          <div class="vue-key-field">
            <strong>API Key（可选）</strong>
            <div class="vue-key-row">
              <span :class="{ empty: modelDraft.apiKeyStatus !== 'configured' }">
                {{ modelDraft.apiKeyStatus === 'configured' ? '已配置' : '未配置' }}
              </span>
              <span>
                <button data-testid="vue-key-replace" type="button" @click="startApiKeyReplace">
                  更换
                </button>
                <button class="danger" type="button" @click="removeApiKey">移除</button>
              </span>
            </div>
            <div v-if="modelDraft.apiKeyEditing" class="vue-key-editor">
              <input
                v-model="apiKeyInput"
                data-testid="vue-key-input"
                type="password"
                autocomplete="off"
                placeholder="输入新的 API Key（不会在原型中保存明文）"
              />
              <button data-testid="vue-key-confirm" type="button" @click="confirmApiKeyReplace">
                确认
              </button>
              <button type="button" @click="cancelApiKeyReplace">取消</button>
            </div>
            <small>出于安全考虑，API Key 保存后将不再显示，仅显示配置状态。</small>
          </div>
        </section>

        <section>
          <h3>高级选项</h3>
          <div class="vue-switch-row">
            <strong>支持视觉/多模态</strong>
            <button
              class="vue-switch"
              :class="{ active: modelDraft.supportsMultimodal }"
              type="button"
              @click="toggleModelMultimodal"
            ></button>
          </div>
          <label>
            思考模式参数格式
            <select v-model="modelDraft.thinkingFormat" data-testid="vue-model-thinking">
              <option v-for="option in thinkingOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
        </section>
      </div>

      <footer class="vue-editor-foot">
        <button
          class="secondary"
          data-testid="vue-test-model"
          type="button"
          @click="testModelConnection"
        >
          测试连接
        </button>
        <small v-if="modelTestStatus" data-testid="vue-model-test-status">{{
          modelTestStatus
        }}</small>
        <span>
          <button class="secondary" type="button" @click="closeModelEditor">取消</button>
          <button data-testid="vue-save-model" type="button" @click="saveModelEditor">保存</button>
        </span>
      </footer>
    </aside>

    <button class="prototype-entry" type="button" @click="activeExperience = 'prototype'">
      Prototype iframe
    </button>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </main>

  <main
    v-if="activeExperience === 'prototype'"
    class="product-experience"
    aria-label="Atlas logged-in product workspace"
  >
    <iframe
      class="product-frame"
      title="Atlas Knowledge Hub logged-in product experience"
      :src="prototypeSrc"
    ></iframe>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </main>

  <main
    v-if="activeExperience === 'p0'"
    class="app-shell"
    aria-label="Atlas Knowledge Hub P0 full-stack workspace"
  >
    <header class="app-header">
      <div>
        <p class="eyebrow">Atlas Knowledge Hub</p>
        <h1>Knowledge Workspace</h1>
        <p>API-driven review, publish, graph, and Ask flow using mock/sample metadata only.</p>
      </div>
      <div class="status-strip" aria-label="P0 status">
        <button class="mode-link" type="button" @click="activeExperience = 'prototype'">
          Product home
        </button>
        <span>Mock/sample only</span>
        <span>Adapter-backed</span>
        <span>No production auth</span>
      </div>
    </header>

    <section class="workspace-grid">
      <aside class="space-panel" data-testid="space-list" aria-label="Knowledge Space list">
        <div class="panel-heading">
          <h2>Knowledge Spaces</h2>
          <span v-if="isLoadingSpaces">Loading</span>
        </div>
        <p v-if="spacesError" class="state-message error">{{ spacesError }}</p>
        <p v-if="!isLoadingSpaces && spaces.length === 0 && !spacesError" class="state-message">
          No Knowledge Spaces are available.
        </p>
        <button
          v-for="space in spaces"
          :key="space.id"
          class="space-card"
          data-testid="space-card"
          type="button"
          :data-selected="selectedSpaceId === space.id"
          @click="selectSpace(space.id)"
        >
          <strong>{{ space.name }}</strong>
          <span>{{ space.description }}</span>
          <span>{{ space.owner }} · {{ space.status }}</span>
          <span
            >{{ space.documentCount }} docs · {{ space.wikiPageCount }} wiki ·
            {{ space.reviewCount }} reviews</span
          >
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Production file upload coming soon
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Production auth and member admin coming soon
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Real provider setup coming soon
        </button>
      </aside>

      <section class="flow-panel" data-testid="space-detail" aria-label="Knowledge Space detail">
        <div class="space-hero">
          <div>
            <p class="eyebrow">Selected Space</p>
            <h2>{{ selectedSpace?.name ?? selectedSpaceId }}</h2>
            <p>
              {{ selectedSpace?.description ?? 'Loading live space metadata from the Atlas API.' }}
            </p>
            <p class="space-meta">
              {{ selectedSpace?.owner ?? 'Owner pending' }} ·
              {{ selectedSpace?.status ?? 'Status pending' }}
            </p>
          </div>
          <div class="metric-row">
            <span>Documents {{ selectedSpace?.documentCount ?? 0 }}</span>
            <span>Wiki {{ selectedSpace?.wikiPageCount ?? wikiPages.length }}</span>
            <span>Review {{ selectedSpace?.reviewCount ?? blockedQueueCount }}</span>
          </div>
        </div>
        <p v-if="isLoadingSpace" class="state-message">Loading selected space workflow.</p>
        <p v-if="workflowError" class="state-message error">{{ workflowError }}</p>
        <p v-if="workflowMessage" class="state-message success">{{ workflowMessage }}</p>

        <div class="workflow-grid">
          <section class="panel" data-tab="documents" aria-label="Documents batch workflow">
            <div class="panel-heading">
              <h2>Documents</h2>
              <button
                data-testid="create-sample-batch"
                type="button"
                :disabled="isCreatingBatch || !selectedSpaceId"
                @click="createBatchFromBrowser"
              >
                {{ isCreatingBatch ? 'Creating...' : 'Create sample batch' }}
              </button>
            </div>
            <div class="split-list">
              <div data-testid="batch-list">
                <h3>Batches</h3>
                <button
                  v-for="batch in batches"
                  :key="batch.id"
                  class="list-button"
                  type="button"
                  :data-selected="batch.id === selectedBatchId"
                  @click="selectBatch(batch.id)"
                >
                  <strong>{{ batch.name }}</strong>
                  <span>{{ batch.id }}</span>
                  <span
                    >{{ batch.metrics.totalFiles }} files ·
                    {{ batch.metrics.reviewRequired }} review</span
                  >
                </button>
                <p v-if="batches.length === 0" class="state-message">No batches yet.</p>
              </div>
              <div data-testid="file-list">
                <h3>Files</h3>
                <button
                  v-for="file in files"
                  :key="file.id"
                  class="list-button"
                  type="button"
                  :data-selected="file.id === selectedFileId"
                  @click="selectFile(file.id)"
                >
                  <strong>{{ file.sourcePath }}</strong>
                  <span>{{ file.status }} · {{ file.reviewStatus }}</span>
                  <span>confidence {{ file.confidence ?? 'n/a' }}</span>
                </button>
                <p v-if="files.length === 0" class="state-message">No files loaded.</p>
              </div>
            </div>
            <div class="source-trace" data-testid="chunk-list">
              <h3>Source Trace</h3>
              <p v-if="chunks.length === 0" class="state-message">
                Select or create a file with chunks.
              </p>
              <ul>
                <li v-for="chunk in chunks" :key="chunk.id">
                  <strong>{{ chunk.id }}</strong>
                  <span
                    >{{ chunk.sourceFile }} · {{ chunk.section ?? 'section n/a' }} · page
                    {{ chunk.page ?? 'n/a' }}</span
                  >
                  <span>confidence {{ chunk.confidence ?? 'n/a' }} · {{ chunk.reviewStatus }}</span>
                </li>
              </ul>
            </div>
          </section>

          <section class="panel" data-tab="review" aria-label="Review workflow">
            <div class="panel-heading">
              <h2>Review</h2>
              <span>Ready {{ readyQueueCount }} · Blocked {{ blockedQueueCount }}</span>
            </div>
            <div class="queue-grid" data-testid="review-queues">
              <span v-for="queue in reviewQueues?.queues ?? []" :key="queue.type">
                {{ queue.type }} {{ queue.count }}
              </span>
            </div>
            <button
              data-testid="approve-file"
              type="button"
              :disabled="!canApprove || isReviewing"
              @click="approveSelectedFile"
            >
              {{ isReviewing ? 'Approving...' : 'Approve selected file' }}
            </button>
            <p class="state-message">
              Selected file: {{ selectedFile?.sourcePath ?? 'none' }} ·
              {{ selectedFile?.reviewStatus ?? 'n/a' }}
            </p>
          </section>

          <section class="panel" data-tab="wiki" aria-label="Wiki publish workflow">
            <div class="panel-heading">
              <h2>Wiki</h2>
              <button
                data-testid="publish-file"
                type="button"
                :disabled="!canPublish || isPublishing"
                @click="publishSelectedFile"
              >
                {{ isPublishing ? 'Publishing...' : 'Publish Wiki' }}
              </button>
            </div>
            <div data-testid="wiki-pages" class="wiki-list">
              <p v-if="wikiPages.length === 0" class="state-message">
                No published Wiki pages yet.
              </p>
              <article v-for="page in wikiPages" :key="page.id">
                <strong>{{ page.title }}</strong>
                <span>{{ page.reviewStatus }} · confidence {{ page.confidence ?? 'n/a' }}</span>
                <span>{{ page.markdownPath }}</span>
                <span>sources {{ page.sourceDocumentIds.join(', ') }}</span>
              </article>
            </div>
            <button
              data-testid="refresh-graph-evidence"
              type="button"
              :disabled="wikiPages.length === 0 || chunks.length === 0 || isRefreshingEvidence"
              @click="refreshDownstreamEvidence"
            >
              {{
                isRefreshingEvidence ? 'Refreshing evidence...' : 'Refresh Graph and Ask evidence'
              }}
            </button>
            <span v-if="downstreamReady" class="state-message success"
              >Downstream evidence is ready.</span
            >
          </section>

          <section class="panel ask-panel" data-tab="ask" aria-label="Trusted Ask workflow">
            <div class="panel-heading">
              <h2>Ask</h2>
              <span>Answer status {{ askRun?.answerReviewStatus ?? 'pending' }}</span>
            </div>
            <label>
              Question
              <textarea v-model="askQuestion" data-testid="ask-question" rows="3"></textarea>
            </label>
            <button
              data-testid="ask-submit"
              type="button"
              :disabled="!canAsk || isAsking"
              @click="submitAsk"
            >
              {{ isAsking ? 'Asking...' : 'Ask selected space' }}
            </button>
            <p v-if="askError" class="state-message error">{{ askError }}</p>
            <article v-if="askRun" class="ask-answer" data-testid="ask-answer">
              <strong>{{ askRun.status }} · {{ askRun.answerReviewStatus }}</strong>
              <p>{{ askRun.answer ?? askRun.safeMessage }}</p>
              <span>confidence {{ askRun.answerConfidence ?? 'n/a' }}</span>
              <ul>
                <li v-for="evidence in askRun.evidence" :key="evidence.evidenceId">
                  {{ evidence.sourceChunkId }} · {{ evidence.sourceFile }} ·
                  {{ evidence.section ?? 'section n/a' }} · score {{ evidence.score ?? 'n/a' }} ·
                  {{ evidence.reviewStatus }}
                </li>
              </ul>
            </article>
          </section>
        </div>
      </section>
    </section>

    <section
      class="graph-hardening-panel"
      aria-label="Knowledge Space Graph tab"
      data-tab="graph"
      :data-state="graphState"
    >
      <div class="graph-header">
        <p class="graph-kicker">Trusted Graph</p>
        <h1>{{ selectedSpace?.name ?? 'IBM i Modernization' }} Knowledge Graph</h1>
        <p>Evidence-backed nodes and edges preserve source trace, confidence, and review status.</p>
      </div>
      <div class="graph-status" role="status" data-testid="graph-state">
        <span v-if="isLoadingGraph">Loading graph evidence</span>
        <span v-else-if="graphState === 'unauthorized'"
          >Unauthorized graph access: {{ graphError }}</span
        >
        <span v-else-if="graphState === 'empty'"
          >No approved or published evidence is available for graph projection.</span
        >
        <span v-else-if="graphState === 'error'">{{ graphError }}</span>
        <span v-else>Graph API connected</span>
      </div>

      <div
        v-if="graph && graphState !== 'unauthorized'"
        class="graph-filter-bar"
        aria-label="Graph filters"
      >
        <label>
          Search
          <input
            v-model="searchText"
            data-testid="graph-search"
            type="search"
            placeholder="Find concept, document, or edge"
          />
        </label>
        <label>
          Node
          <select v-model="nodeTypeFilter" data-testid="graph-node-filter">
            <option value="ALL">All nodes</option>
            <option v-for="type in nodeTypeOptions" :key="type" :value="type">{{ type }}</option>
          </select>
        </label>
        <label>
          Edge
          <select v-model="edgeTypeFilter" data-testid="graph-edge-filter">
            <option value="ALL">All edges</option>
            <option v-for="type in edgeTypeOptions" :key="type" :value="type">{{ type }}</option>
          </select>
        </label>
        <label>
          Review
          <select v-model="reviewStatusFilter" data-testid="graph-review-filter">
            <option value="ALL">All review states</option>
            <option v-for="status in reviewStatusOptions" :key="status" :value="status">
              {{ status }}
            </option>
          </select>
        </label>
        <label class="graph-toggle">
          <input v-model="evidenceOnly" data-testid="graph-evidence-toggle" type="checkbox" />
          Evidence only
        </label>
      </div>

      <div v-if="graph && graphState !== 'unauthorized'" class="graph-shell">
        <div class="graph-canvas-wrap" aria-label="Graph canvas">
          <svg
            class="graph-canvas"
            viewBox="0 0 360 300"
            role="img"
            aria-label="API-backed graph canvas"
          >
            <line
              v-for="edge in visibleEdges"
              :key="edge.id"
              :x1="canvasNodeById.get(edge.sourceNodeId)?.x"
              :y1="canvasNodeById.get(edge.sourceNodeId)?.y"
              :x2="canvasNodeById.get(edge.targetNodeId)?.x"
              :y2="canvasNodeById.get(edge.targetNodeId)?.y"
              class="graph-edge-line"
              :class="{ active: selectedEdge?.id === edge.id }"
            />
            <g
              v-for="node in canvasNodes"
              :key="node.id"
              class="graph-svg-node"
              :class="{ active: activeNode?.id === node.id, hovered: hoveredNodeId === node.id }"
              :transform="`translate(${node.x}, ${node.y})`"
              tabindex="0"
              role="button"
              :aria-label="`${node.label} ${node.type} ${node.reviewStatus}`"
              @click="selectNode(node)"
              @keyup.enter="selectNode(node)"
              @focus="hoveredNodeId = node.id"
              @blur="hoveredNodeId = ''"
              @mouseenter="hoveredNodeId = node.id"
              @mouseleave="hoveredNodeId = ''"
            >
              <circle r="24" />
              <text y="4">{{ node.label.slice(0, 2).toUpperCase() }}</text>
            </g>
          </svg>
          <div class="graph-legend" aria-label="Graph legend">
            <span><i class="legend-node"></i>Node</span>
            <span><i class="legend-edge"></i>Evidence edge</span>
            <span><i class="legend-active"></i>Selected</span>
          </div>
        </div>

        <div class="graph-lists">
          <div>
            <h2>Nodes</h2>
            <button
              v-for="node in visibleNodes"
              :key="node.id"
              class="graph-node-button"
              type="button"
              :data-selected="activeNode?.id === node.id"
              @click="selectNode(node)"
            >
              <strong>{{ node.label }}</strong>
              <span>{{ node.type }} · {{ node.reviewStatus }}</span>
              <span
                >confidence {{ node.confidence ?? 'n/a' }} · evidence {{ node.evidenceCount }}</span
              >
            </button>
            <p v-if="visibleNodes.length === 0" class="graph-empty" data-testid="graph-empty">
              No graph nodes match the current filters.
            </p>
          </div>

          <div>
            <h2>Edges</h2>
            <button
              v-for="edge in visibleEdges"
              :key="edge.id"
              class="graph-edge-button"
              type="button"
              :data-selected="selectedEdge?.id === edge.id"
              @click="selectEdge(edge)"
            >
              <strong>{{ edge.type }}</strong>
              <span
                >{{ edgeNodeLabel(edge.sourceNodeId) }} ->
                {{ edgeNodeLabel(edge.targetNodeId) }}</span
              >
              <span
                >confidence {{ edge.confidence ?? 'n/a' }} · evidence {{ edge.evidenceCount }}</span
              >
            </button>
            <p v-if="visibleEdges.length === 0" class="graph-empty" data-testid="graph-no-evidence">
              No evidence-backed relationships match the current filters.
            </p>
          </div>
        </div>
      </div>

      <aside
        v-if="graph && graphState !== 'unauthorized'"
        class="graph-evidence-detail"
        data-testid="graph-evidence-detail"
      >
        <strong>{{ activeSelectionLabel }}</strong>
        <span v-if="selectedEdge"
          >{{ selectedEdge.type }} · {{ selectedEdge.reviewStatus }} · confidence
          {{ selectedEdge.confidence ?? 'n/a' }}</span
        >
        <span v-else-if="activeNode"
          >{{ activeNode.type }} · {{ activeNode.reviewStatus }} · confidence
          {{ activeNode.confidence ?? 'n/a' }}</span
        >
        <h2>Source Trace</h2>
        <ul v-if="evidenceReferences.length > 0">
          <li v-for="evidence in evidenceReferences" :key="evidence.sourceChunkId">
            {{ evidence.sourceChunkId }} · {{ evidence.sourceFile }} ·
            {{ evidence.section ?? 'section n/a' }} · confidence
            {{ evidence.confidence ?? 'n/a' }} · {{ evidence.reviewStatus }}
          </li>
        </ul>
        <p v-else class="graph-empty">No source-trace evidence is attached to this graph object.</p>
      </aside>

      <div v-if="graph" class="graph-counts" aria-label="Graph counts">
        <span>Nodes {{ graph.counts.nodes ?? graph.nodes.length }}</span>
        <span>Edges {{ graph.counts.edges ?? graph.edges.length }}</span>
        <span>Excluded {{ graph.counts.excluded ?? 0 }}</span>
      </div>
    </section>
  </main>
</template>
