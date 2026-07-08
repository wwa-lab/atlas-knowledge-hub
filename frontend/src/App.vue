<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import GlobalChatView from '@/components/chat/GlobalChatView.vue'
import ProductHomeView from '@/components/home/ProductHomeView.vue'
import ProductShell from '@/components/layout/ProductShell.vue'
import ApiInfoSettingsPanel from '@/components/settings/ApiInfoSettingsPanel.vue'
import AuditLogSettingsPanel from '@/components/settings/AuditLogSettingsPanel.vue'
import GeneralSettingsPanel from '@/components/settings/GeneralSettingsPanel.vue'
import MembersSettingsPanel from '@/components/settings/MembersSettingsPanel.vue'
import MessageSettingsPanel from '@/components/settings/MessageSettingsPanel.vue'
import ModelEditor from '@/components/settings/ModelEditor.vue'
import ModelsSettingsPanel from '@/components/settings/ModelsSettingsPanel.vue'
import PlaceholderSettingsPanelView from '@/components/settings/PlaceholderSettingsPanel.vue'
import ProfileSettingsPanel from '@/components/settings/ProfileSettingsPanel.vue'
import SettingsModal from '@/components/settings/SettingsModal.vue'
import SpaceInfoSettingsPanel from '@/components/settings/SpaceInfoSettingsPanel.vue'
import SpaceConnectorsTab from '@/components/space/SpaceConnectorsTab.vue'
import SpaceDetailView from '@/components/space/SpaceDetailView.vue'
import SpaceDocumentsTab from '@/components/space/SpaceDocumentsTab.vue'
import SpaceGraphTab from '@/components/space/SpaceGraphTab.vue'
import SpaceReviewTab from '@/components/space/SpaceReviewTab.vue'
import SpaceWikiTab from '@/components/space/SpaceWikiTab.vue'
import { useProductUploadWorkflow } from '@/composables/useProductUploadWorkflow'
import {
  acknowledgeDeadLetterEntry,
  ApiError,
  approveFile as approveFileApi,
  clearDeepSeekConfiguration,
  createAskRun,
  createManualUrlSource,
  createSampleBatch,
  createSpace as createSpaceApi,
  getConnectorSyncRun,
  getAskQualityMetrics,
  getAskRun,
  getAskSession,
  getCurrentUser,
  getDeepSeekConfiguration,
  getGraph,
  getGraphNode,
  getSpace,
  listAuditEvents,
  listAskSessions,
  listBatches,
  listChunks,
  listConnectorDefinitions,
  listConnectorSyncItems,
  listDeadLetterEntries,
  listFiles,
  listManualUrlSources,
  listModelAdapters,
  listSpaces,
  listWikiPageIssues,
  listWikiPages,
  publishFile as publishFileApi,
  getReviewQueues,
  refreshDownstreamEvidenceApi,
  retryDeadLetterEntry,
  saveDeepSeekConfiguration,
  startConnectorSync,
  uploadDocuments
} from '@/api'
import type {
  ApiAskRun,
  ApiAskSessionDetail,
  ApiAskSessionSummary,
  ApiAuditEvent,
  ApiBatch,
  ApiConnectorDefinition,
  ApiConnectorSyncItem,
  ApiConnectorSyncRun,
  ApiDeadLetterEntry,
  ApiFileItem,
  ApiGraphEdge,
  ApiGraphEdgeType,
  ApiGraphNode,
  ApiGraphNodeDetail,
  ApiGraphNodeType,
  ApiGraphView,
  ApiMe,
  ApiManualUrlSource,
  ApiModelCapability,
  ApiModelConfiguration,
  ApiReviewQueues,
  ApiReviewStatus,
  ApiRetrievalRunQualityMetrics,
  ApiSourceChunk,
  ApiSpace,
  ApiWikiPage,
  ApiWikiPageIssue,
  ManualUrlFetchIntent
} from '@/types'
import {
  answerReuseHint,
  answerReviewReasonLine,
  formatGraphEvidenceReference,
  formatMetricRatio,
  formatWikiReference,
  isPlaceholderSettingsPanel,
  isSecretConfigured,
  modelTypeToCategory,
  productGraphNodeType,
  productReviewStatus,
  secretStatusByKey,
  uniqueValues,
  wikiSourceTrace
} from '@/domain/viewModels'
import type {
  ApiInfoState,
  CreateSpaceDraft,
  GeneralCodeFont,
  GeneralFontSize,
  GeneralInterfaceFont,
  GeneralLanguage,
  GeneralOption,
  GeneralSettingsState,
  GeneralThemeMode,
  MemberRole,
  MessageIndexStat,
  ModelCategory,
  ModelSource,
  PendingInvitation,
  PlaceholderSettingsPanel,
  ProductAskMode,
  ProductAskAnswer,
  ProductGraphEdge,
  ProductGraphNode,
  ProductSpaceCard,
  ProductView,
  ProductWikiPage,
  SafeErrorPreview,
  SettingsPanel,
  SettingsSurface,
  SpaceInfoDraft,
  SpaceInfoEditableField,
  SpaceInfoRow,
  SpaceOperationalMetadata,
  SpaceTab,
  SpaceMember,
  ThinkingFormat,
  VueModelConfig,
  VueModelDraft
} from '@/domain/viewModels'

const defaultSpaceId = 'ibm-i-modernization'
const prototypeSrc = '/atlas-prototype.html'
const defaultMockMe: ApiMe = {
  user: {
    id: 'frontend-demo',
    email: 'frontend-demo@example.test',
    displayName: 'Frontend Demo',
    status: 'ACTIVE',
    globalRoles: []
  },
  activeSpaceId: defaultSpaceId,
  memberships: [
    {
      id: 'membership-frontend-ibmi',
      spaceId: defaultSpaceId,
      spaceName: 'IBM i Modernization',
      role: 'SPACE_OWNER',
      status: 'ACTIVE'
    }
  ],
  capabilities: [
    'CONTENT_READ',
    'CONTENT_WRITE',
    'GOVERNANCE_READ',
    'KNOWLEDGE_OPERATE',
    'MEMBER_MANAGE',
    'SETTINGS_MANAGE',
    'SPACE_MANAGE',
    'SPACE_READ'
  ]
}

const activeExperience = ref<'atlas' | 'prototype' | 'p0'>('atlas')
const productView = ref<ProductView>('home')
const activeSpaceTab = ref<SpaceTab>('wiki')
const settingsOpen = ref(false)
const settingsPanel = ref<SettingsPanel>('models')
const generalSettings = ref<GeneralSettingsState>({
  language: 'zh-CN',
  themeMode: 'light',
  interfaceFont: 'system',
  codeFont: 'system-mono',
  fontSize: 'normal',
  memoryEnabled: true
})
const apiInfo = ref<ApiInfoState>({
  keyVersion: 1,
  baseUrl: '/api',
  docsPath: '/docs/api',
  status: ''
})
const safeErrorPreviews: SafeErrorPreview[] = [
  {
    code: 'AUTHENTICATION_REQUIRED',
    title: 'Authentication required',
    description: 'The request needs a valid Atlas user header.'
  },
  {
    code: 'PERMISSION_DENIED',
    title: 'Permission denied',
    description: 'The current user is not allowed to perform this action.'
  },
  {
    code: 'RATE_LIMITED',
    title: 'Rate limited',
    description: 'Too many requests. Please try again later.'
  },
  {
    code: 'SAFE_SYSTEM_ERROR',
    title: 'Safe system error',
    description: 'Unexpected server error with a correlation reference.'
  }
]
const messageIndexEnabled = ref(false)
const messageEmbeddingModel = ref('text-embedding-v4')
const spaces = ref<ApiSpace[]>([])
const currentUser = ref<ApiMe | null>(defaultMockMe)
const selectedSpace = ref<ApiSpace | null>(null)
const selectedSpaceId = ref(defaultSpaceId)
const batches = ref<ApiBatch[]>([])
const selectedBatchId = ref('')
const files = ref<ApiFileItem[]>([])
const selectedFileId = ref('')
const chunks = ref<ApiSourceChunk[]>([])
const manualUrlSources = ref<ApiManualUrlSource[]>([])
const manualUrlDraft = ref({
  url: 'https://example.com/reference/page',
  title: 'Vendor reference page',
  description: 'Sample-safe manual URL metadata seed.',
  fetchIntent: 'FETCH_LATER' as ManualUrlFetchIntent
})
const reviewQueues = ref<ApiReviewQueues | null>(null)
const wikiPages = ref<ApiWikiPage[]>([])
const wikiPageIssues = ref<ApiWikiPageIssue[]>([])
const auditEvents = ref<ApiAuditEvent[]>([])
const connectorDefinitions = ref<ApiConnectorDefinition[]>([])
const connectorSyncRun = ref<ApiConnectorSyncRun | null>(null)
const connectorSyncItems = ref<ApiConnectorSyncItem[]>([])
const selectedConnectorItemId = ref('')
const deadLetterEntries = ref<ApiDeadLetterEntry[]>([])
const selectedDeadLetterId = ref('')
const askRun = ref<ApiAskRun | null>(null)
const askQualityMetrics = ref<ApiRetrievalRunQualityMetrics | null>(null)
const askSessions = ref<ApiAskSessionSummary[]>([])
const selectedAskSession = ref<ApiAskSessionDetail | null>(null)
const askQuestion = ref('What evidence was published for the P0 browser flow?')
const workflowMessage = ref('')
const downstreamReady = ref(false)

const isLoadingSpaces = ref(true)
const isLoadingSpace = ref(false)
const isCreateSpaceOpen = ref(false)
const isCreatingSpace = ref(false)
const isCreatingBatch = ref(false)
const isCreatingManualUrl = ref(false)
const isReviewing = ref(false)
const isPublishing = ref(false)
const isRefreshingEvidence = ref(false)
const isAsking = ref(false)
const spacesError = ref('')
const authError = ref('')
const createSpaceError = ref('')
const createSpaceStatus = ref('')
const workflowError = ref('')
const manualUrlError = ref('')
const askError = ref('')
const askQualityError = ref('')
const askSessionError = ref('')
const auditError = ref('')
const isLoadingAuditEvents = ref(false)
const connectorError = ref('')
const isLoadingConnectors = ref(false)
const isStartingConnectorSync = ref(false)
const deadLetterError = ref('')
const isLoadingDeadLetters = ref(false)
const isRetryingDeadLetter = ref(false)
const isAcknowledgingDeadLetter = ref(false)

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
    secretStatuses: [],
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
    secretStatuses: [],
    supportsMultimodal: false,
    thinkingFormat: 'none'
  }
])
const modelCapabilities = ref<ApiModelCapability[]>([])
const deepSeekConfiguration = ref<ApiModelConfiguration | null>(null)
const modelApiError = ref('')
const modelSaveStatus = ref('')
const createSpaceDraft = ref<CreateSpaceDraft>({
  name: '',
  description: '',
  type: 'document' as 'document' | 'faq',
  indexStrategy: 'rag' as 'rag' | 'wiki',
  owner: '我创建'
})
const activeModelCategory = ref<ModelCategory>('all')
const isModelAddMenuOpen = ref(false)
const selectedModelId = ref('deepseek-flash')
const modelDraft = ref<VueModelDraft | null>(null)
const apiKeyInput = ref('')
const modelTestStatus = ref('')
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
const {
  uploadSession,
  activeProductBatchFiles,
  isProductReportOpen,
  visibleProductBatchFiles,
  productBatchMetrics,
  productProcessingIssues,
  reportSections,
  openMockUpload,
  cancelMockUpload,
  createProductBatch,
  openProductReport,
  closeProductReport
} = useProductUploadWorkflow({
  showDocumentsTab: () => {
    activeSpaceTab.value = 'docs'
  }
})
const selectedProductWikiPageId = ref('wiki-modernization-index')
const selectedProductGraphNodeId = ref('node-wiki')
const productGraphSearch = ref('')
const productAskQuestion = ref('哪些证据支持 IBM i 现代化知识空间可以发布到 Wiki？')
const productAskMode = ref<ProductAskMode>('answered')
const memberSearch = ref('')
const memberActionStatus = ref('')
const spaceInfoStatus = ref('')
const activeSpaceInfoEditField = ref<SpaceInfoEditableField>(null)
const spaceInfoDraft = ref<SpaceInfoDraft>({ name: '', description: '' })
const spaceInfoOverrides = ref<Record<string, SpaceInfoDraft>>({})
const pendingInvitations = ref<PendingInvitation[]>([])
const spaceMembers = ref<SpaceMember[]>([
  {
    id: 'member-leo',
    name: 'leo',
    email: 'leo@example.com',
    role: 'owner',
    joinedAt: '2026/06/23 13:19',
    removable: false
  },
  {
    id: 'member-pkos-admin',
    name: 'Atlas Admin',
    email: 'admin@example.com',
    role: 'owner',
    joinedAt: '2026/06/26 15:44',
    removable: true
  }
])

const productWikiPages: ProductWikiPage[] = [
  {
    id: 'wiki-modernization-index',
    title: 'IBM i Modernization Index',
    slug: 'modernization-index',
    pageType: 'INDEX',
    aliases: ['Modernization Hub'],
    sourceRefs: ['FILE: file-001 / generated/wiki/modernization-overview.md'],
    chunkRefs: ['SOURCE_CHUNK: chunk-file-001-p12-b02 / page 12'],
    inLinks: [],
    outLinks: ['source-trace-standard'],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
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
    pageType: 'TOPIC',
    aliases: ['Traceability Standard'],
    sourceRefs: ['FILE: file-001 / current-state-summary.md'],
    chunkRefs: ['SOURCE_CHUNK: trace-009 / section architecture'],
    inLinks: ['modernization-index'],
    outLinks: [],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
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
    pageType: 'SOURCE_SUMMARY',
    aliases: [],
    sourceRefs: ['FILE: file-005 / RPG_Scan_Result.xlsx'],
    chunkRefs: [],
    inLinks: [],
    outLinks: [],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
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

const languageOptions: Array<GeneralOption<GeneralLanguage>> = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]
const themeOptions: Array<GeneralOption<GeneralThemeMode>> = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'system', label: '跟随系统' }
]
const interfaceFontOptions: Array<GeneralOption<GeneralInterfaceFont>> = [
  { value: 'system', label: '系统默认' },
  { value: 'pingfang', label: '苹方 / PingFang SC' },
  { value: 'microsoft', label: '微软雅黑 / Microsoft YaHei' }
]
const codeFontOptions: Array<GeneralOption<GeneralCodeFont>> = [
  { value: 'system-mono', label: '系统默认' },
  { value: 'sf-mono', label: 'SF Mono' },
  { value: 'jetbrains', label: 'JetBrains Mono' }
]
const fontSizeOptions: Array<GeneralOption<GeneralFontSize>> = [
  { value: 'small', label: '小' },
  { value: 'normal', label: '正常' },
  { value: 'large', label: '大' }
]
const memberRoleOptions: Array<GeneralOption<MemberRole>> = [
  { value: 'owner', label: '所有者' },
  { value: 'admin', label: '管理员' },
  { value: 'reviewer', label: '审核者' },
  { value: 'viewer', label: '浏览者' }
]
const messageEmbeddingOptions: Array<GeneralOption<string>> = [
  { value: 'text-embedding-v4', label: 'text-embedding-v4' },
  { value: 'mock-embedding-1024', label: 'mock-embedding-1024' }
]
const accountProfileRows: Array<{
  key: string
  testId: string
  label: string
  value: string
  note: string
}> = [
  {
    key: 'id',
    testId: 'vue-user-info-id',
    label: '用户 ID',
    value: 'atlas-demo-user-001',
    note: '稳定 mock ID，用于前端设置页验收。'
  },
  {
    key: 'name',
    testId: 'vue-user-info-name',
    label: '用户名',
    value: 'leo',
    note: '用于设置页、审核记录和 mock 活动流展示。'
  },
  {
    key: 'email',
    testId: 'vue-user-info-email',
    label: '邮箱',
    value: 'leo@example.com',
    note: '示例账号信息，不代表真实身份系统。'
  },
  {
    key: 'registered',
    testId: 'vue-user-info-registered',
    label: '注册时间',
    value: '2026/06/23 13:19',
    note: 'Mock registration timestamp for UI parity.'
  },
  {
    key: 'role',
    testId: 'vue-user-info-role',
    label: '当前角色',
    value: 'Workspace Owner',
    note: '生产权限以后端 RBAC 为准。'
  }
]
const spaceOperationalMetadata: Record<string, SpaceOperationalMetadata> = {
  'ibm-i-modernization': {
    createdAt: '2026-06-23T13:19:00Z',
    storageQuota: '10 GB',
    storageUsed: '81.83 MB',
    storageUsageRate: '0.8%'
  },
  'legacy-spec-factory': {
    createdAt: '2026-06-18T10:30:00Z',
    storageQuota: '8 GB',
    storageUsed: '296 MB',
    storageUsageRate: '3.7%'
  },
  'ai-engineering-playbook': {
    createdAt: '2026-06-12T16:10:00Z',
    storageQuota: '6 GB',
    storageUsed: '144 MB',
    storageUsageRate: '2.4%'
  }
}
const maskedApiKey = '••••••••••••••••••••••••••••••••'
const apiKeyDisplayValue = computed(() => {
  void apiInfo.value.keyVersion
  return maskedApiKey
})

const settingsSurfaces: Record<PlaceholderSettingsPanel, SettingsSurface> = {
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
  vector: {
    title: '向量数据库引擎',
    status: 'Adapter boundary',
    summary: '配置向量能力的产品面状态，不直连 pgvector、Milvus、Qdrant 或外部服务。',
    rows: [
      { label: 'Active adapter', value: 'mock-vector', note: '自动化验证不需要真实向量库。' },
      {
        label: 'Secret reference',
        value: 'status only',
        note: 'endpoint 与 credential 只显示配置状态。'
      },
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
      { label: 'Runtime command', value: 'masked status', note: '不展示本地命令或私有路径。' },
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
      {
        label: 'Secret reference',
        value: 'missing / configured',
        note: '只显示状态，不显示 endpoint。'
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
const currentCapabilities = computed(() => new Set(currentUser.value?.capabilities ?? []))
const canManageSpaces = computed(() => hasCapability('SPACE_MANAGE'))
const canWriteContent = computed(() => hasCapability('CONTENT_WRITE'))
const canOperateKnowledge = computed(() => hasCapability('KNOWLEDGE_OPERATE'))
const canManageMembers = computed(() => hasCapability('MEMBER_MANAGE'))
const canReadGovernance = computed(() => hasCapability('GOVERNANCE_READ'))
const canApprove = computed(
  () =>
    canOperateKnowledge.value &&
    Boolean(selectedFile.value) &&
    selectedFile.value?.reviewStatus !== 'APPROVED' &&
    chunks.value.length > 0
)
const canPublish = computed(
  () => canOperateKnowledge.value && selectedFile.value?.reviewStatus === 'APPROVED'
)
const canAsk = computed(
  () => canOperateKnowledge.value && Boolean(selectedSpaceId.value && askQuestion.value.trim())
)
const readyQueueCount = computed(
  () => reviewQueues.value?.queues.find(queue => queue.type === 'READY_TO_PUBLISH')?.count ?? 0
)
const blockedQueueCount = computed(
  () =>
    reviewQueues.value?.queues
      .filter(queue => queue.publishBlocked)
      .reduce((total, queue) => total + queue.count, 0) ?? 0
)
const selectedConnectorItem = computed(
  () =>
    connectorSyncItems.value.find(item => item.id === selectedConnectorItemId.value) ??
    connectorSyncItems.value[0] ??
    null
)
const selectedDeadLetterEntry = computed(
  () =>
    deadLetterEntries.value.find(entry => entry.id === selectedDeadLetterId.value) ??
    deadLetterEntries.value[0] ??
    null
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
const messageIndexConfigured = computed(
  () => messageIndexEnabled.value && messageEmbeddingModel.value.length > 0
)
const selectedMessageEmbeddingModel = computed(
  () =>
    messageEmbeddingOptions.find(option => option.value === messageEmbeddingModel.value)?.label ??
    '未选择'
)
const messageIndexStats = computed<MessageIndexStat[]>(() => [
  {
    label: 'Embedding 模型',
    value: selectedMessageEmbeddingModel.value,
    note: '用于消息语义搜索的 mock-safe 模型配置。'
  },
  {
    label: '已索引消息',
    value: '248',
    note: '仅为演示统计，不读取真实聊天历史。'
  },
  {
    label: '已索引对话',
    value: '18',
    note: '只表示本地 Vue 状态，不写入向量库。'
  },
  {
    label: '最后索引',
    value: '2026/07/05 10:30',
    note: 'Mock timestamp for UI acceptance.'
  }
])
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
    name: spaceInfoOverrides.value[space.id]?.name ?? space.name,
    description: spaceInfoOverrides.value[space.id]?.description ?? space.description,
    documents: space.documentCount,
    reviews: space.reviewCount,
    owner: space.owner,
    status: space.status,
    wikiPages: space.wikiPageCount,
    source: 'api' as const
  }))
  const apiIds = new Set(apiCards.map(space => space.id))
  return [
    ...apiCards,
    ...productSpaceFallbacks
      .filter(space => !apiIds.has(space.id))
      .map(space => ({
        ...space,
        name: spaceInfoOverrides.value[space.id]?.name ?? space.name,
        description: spaceInfoOverrides.value[space.id]?.description ?? space.description
      }))
  ]
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
const manualUrlReviewRequiredCount = computed(
  () => manualUrlSources.value.filter(source => source.reviewStatus === 'REVIEW_REQUIRED').length
)
const reviewQueueCards = computed(() => reviewQueues.value?.queues ?? [])
const apiReadyToPublishCount = computed(
  () => reviewQueueCards.value.find(queue => queue.type === 'READY_TO_PUBLISH')?.count ?? 0
)
const apiBlockedReviewCount = computed(() =>
  reviewQueueCards.value
    .filter(queue => queue.publishBlocked)
    .reduce((total, queue) => total + queue.count, 0)
)
const apiWikiIssueCards = computed(() => {
  const groups = wikiPageIssues.value.reduce<Record<string, ApiWikiPageIssue[]>>((acc, issue) => {
    acc[issue.issueType] = [...(acc[issue.issueType] ?? []), issue]
    return acc
  }, {})
  return Object.entries(groups).map(([issueType, issues]) => ({
    id: `wiki-${issueType.toLowerCase()}`,
    label: `WIKI ${issueType.replaceAll('_', ' ')}`,
    type: issueType,
    count: issues.length,
    status: issues.some(issue => issue.severity === 'HIGH' || issue.severity === 'MEDIUM')
      ? 'review warning'
      : 'quality note',
    action: '查看 Wiki warning',
    source: issues[0]?.pageId ?? 'Wiki lint'
  }))
})
const apiProcessingIssues = computed(() => [
  ...reviewQueueCards.value.map(queue => ({
    id: queue.type.toLowerCase(),
    label: queue.type.replaceAll('_', ' '),
    type: queue.type,
    count: queue.count,
    status: queue.publishBlocked ? 'blocks Wiki / Graph / Ask' : 'eligible',
    action: queue.publishBlocked ? '查看阻塞项' : '发布 Wiki',
    source: queue.representativeItems[0]?.fileId ?? 'API review queue'
  })),
  ...apiWikiIssueCards.value
])
const auditSummary = computed(() => ({
  total: auditEvents.value.length,
  security: auditEvents.value.filter(event => event.severity === 'SECURITY').length,
  denied: auditEvents.value.filter(event => event.result === 'DENIED').length
}))
const apiProductWikiPages = computed<ProductWikiPage[]>(() =>
  wikiPages.value.map(page => ({
    id: page.id,
    title: page.title,
    slug: page.slug ?? page.markdownPath,
    pageType: page.pageType ?? 'SOURCE_SUMMARY',
    aliases: page.aliases ?? [],
    sourceRefs: (page.sourceRefs ?? []).map(formatWikiReference),
    chunkRefs: (page.chunkRefs ?? []).map(formatWikiReference),
    inLinks: page.inLinks ?? [],
    outLinks: page.outLinks ?? [],
    version: page.version ?? 1,
    sourceMode: page.sourceMode ?? 'PUBLISHED_FILE',
    refreshPolicy: page.refreshPolicy ?? 'MANUAL',
    confidence: page.confidence ?? 0,
    reviewStatus: page.reviewStatus,
    owner: page.owner,
    updatedAt: page.lastUpdated,
    sourceTrace: wikiSourceTrace(page),
    entities: [
      'API Wiki',
      page.pageType ?? 'SOURCE_SUMMARY',
      page.sourceMode ?? 'PUBLISHED_FILE',
      page.reviewStatus
    ],
    sections: [
      {
        title: 'API-backed published metadata',
        body: 'This Wiki page is loaded from the Atlas review-publish API and remains tied to reviewed source documents.',
        sourceTrace: wikiSourceTrace(page),
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
const selectedProductWikiPageIssues = computed(() =>
  wikiPageIssues.value.filter(issue => issue.pageId === selectedProductWikiPage.value?.id)
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
    return apiEvidence.map(formatGraphEvidenceReference)
  }
  return [
    node.sourceTrace,
    ...productGraphEdgeList.value
      .filter(edge => edge.source === node.id || edge.target === node.id)
      .map(edge => edge.sourceTrace)
  ]
})
const productAskAnswer = computed<ProductAskAnswer>(() => {
  if (productAskMode.value === 'answered' && askRun.value) {
    return {
      status: askRun.value.status,
      title: 'API-backed trusted answer',
      body: askRun.value.answer ?? askRun.value.safeMessage ?? 'Trusted Ask completed safely.',
      governanceLabel: askRun.value.answerReviewLabel,
      governanceReason: answerReviewReasonLine(askRun.value),
      reuseHint: answerReuseHint(askRun.value),
      evidence: askRun.value.evidence.map(
        evidence =>
          `${evidence.citationId} · ${evidence.sourceChunkId} · ${evidence.evidenceLabel} · ${evidence.citationStatus} · score ${evidence.score ?? 'n/a'}`
      ),
      warning: `Answer review status ${askRun.value.answerReviewStatus}; model run ${askRun.value.modelRunId ?? 'n/a'}`
    }
  }
  if (productAskMode.value === 'refusal') {
    return {
      status: 'NO_APPROVED_EVIDENCE',
      title: '证据不足，已可信拒答',
      body: '当前问题只命中 OCR required、low confidence 或缺少 source_trace 的内容；这些内容不会进入可信答案。',
      governanceLabel: 'Review required',
      governanceReason: 'No approved answer has been reviewed.',
      reuseHint: 'Not approved reusable knowledge.',
      evidence: [] as string[],
      warning: 'Excluded: PDF_CONVERT_FAILED, OCR_REQUIRED, LOW_CONFIDENCE, MISSING_SOURCE_TRACE'
    }
  }
  if (productAskMode.value === 'review-warning') {
    return {
      status: 'REVIEW_REQUIRED',
      title: '答案需要 SME 审核',
      body: '可以基于已溯源内容生成草案，但其中包含 review-required evidence，因此答案不能被视为已发布知识。',
      governanceLabel: 'Needs revision',
      governanceReason: 'Reviewer should resolve review-required evidence before reuse.',
      reuseHint: 'Not approved reusable knowledge.',
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
    governanceLabel: 'Review required',
    governanceReason: 'No reviewer reason recorded.',
    reuseHint: 'Not approved reusable knowledge.',
    evidence: [
      'modernization-index.md / section index / chunk wiki-001 · PUBLISHED · confidence 0.96',
      'BRD_Methodology.pdf / page 12 / chunk brd-012 · APPROVED · confidence 0.94'
    ],
    warning: 'Generated answer remains REVIEW_REQUIRED until SME verification.'
  }
})

const askQualityChips = computed(() => {
  const metrics = askQualityMetrics.value
  if (!metrics) {
    return []
  }
  return [
    `evidence ${formatMetricRatio(metrics.evidenceCoverage.coverageRatio)}`,
    `citations ${formatMetricRatio(metrics.citationHealth.healthRatio)}`,
    `confidence ${metrics.confidence.band}`,
    `review ${metrics.reviewEligibility.status}`,
    metrics.noEvidenceRefusal ? 'refusal tracked' : 'evidence-backed'
  ]
})

const currentSettingsSurface = computed(() =>
  isPlaceholderSettingsPanel(settingsPanel.value) ? settingsSurfaces[settingsPanel.value] : null
)
const selectedSpaceInfoMetadata = computed<SpaceOperationalMetadata>(
  () =>
    spaceOperationalMetadata[selectedProductSpace.value.id] ?? {
      createdAt: '2026-06-23T13:19:00Z',
      storageQuota: '5 GB',
      storageUsed: '0 MB',
      storageUsageRate: '0%'
    }
)
const selectedSpaceInfoRows = computed<SpaceInfoRow[]>(() => [
  {
    key: 'id',
    label: '空间 ID',
    note: '当前知识空间的唯一标识',
    value: selectedProductSpace.value.id
  },
  {
    key: 'name',
    label: '空间名称',
    note: '当前知识空间在 Atlas 中的显示名称',
    value: selectedProductSpace.value.name,
    field: 'name'
  },
  {
    key: 'description',
    label: '空间描述',
    note: '帮助成员理解此空间的内容边界',
    value: selectedProductSpace.value.description,
    field: 'description'
  },
  {
    key: 'status',
    label: '空间状态',
    note: '空间当前的运行与质量门禁状态',
    value: spaceInfoStatus.value || selectedProductSpace.value.status
  },
  {
    key: 'createdAt',
    label: '空间创建时间',
    note: '空间创建的 mock 时间戳',
    value: formatSpaceTimestamp(
      selectedProductApiSpace.value?.createdAt ?? selectedSpaceInfoMetadata.value.createdAt
    )
  },
  {
    key: 'storageQuota',
    label: '存储配额',
    note: '空间的总存储空间配额',
    value: selectedSpaceInfoMetadata.value.storageQuota
  },
  {
    key: 'storageUsed',
    label: '已使用存储',
    note: '已使用的 mock 存储空间',
    value: selectedSpaceInfoMetadata.value.storageUsed
  },
  {
    key: 'storageUsageRate',
    label: '存储使用率',
    note: '当前 mock 存储占用比例',
    value: selectedSpaceInfoMetadata.value.storageUsageRate
  }
])
const pendingInvitationCount = computed(() => pendingInvitations.value.length)
const filteredSpaceMembers = computed(() => {
  const query = memberSearch.value.trim().toLocaleLowerCase()
  if (!query) {
    return spaceMembers.value
  }
  return spaceMembers.value.filter(
    member =>
      member.name.toLocaleLowerCase().includes(query) ||
      member.email.toLocaleLowerCase().includes(query) ||
      memberRoleLabel(member.role).toLocaleLowerCase().includes(query)
  )
})
onMounted(() => {
  void initialize()
})

async function initialize() {
  await Promise.all([
    loadCurrentUser(),
    loadSpaces(),
    loadGraph(),
    loadModelCapabilities(),
    loadConnectorDefinitions(),
    loadDeadLetters()
  ])
}

async function loadCurrentUser() {
  try {
    currentUser.value = await getCurrentUser()
    authError.value = ''
  } catch (error) {
    authError.value = safeError(
      error,
      'Current user context unavailable; using local mock permissions.'
    )
  }
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

function openCreateSpacePanel() {
  if (!canManageSpaces.value) {
    createSpaceStatus.value = '当前账号没有创建知识库权限。'
    return
  }
  createSpaceDraft.value = {
    name: '',
    description: '',
    type: 'document',
    indexStrategy: 'rag',
    owner: '我创建'
  }
  createSpaceError.value = ''
  createSpaceStatus.value = ''
  isCreateSpaceOpen.value = true
}

function closeCreateSpacePanel() {
  isCreateSpaceOpen.value = false
  createSpaceError.value = ''
}

async function createProductSpace() {
  if (!canManageSpaces.value) {
    createSpaceError.value = '当前账号没有创建知识库权限。'
    return
  }
  const draft = {
    ...createSpaceDraft.value,
    name: createSpaceDraft.value.name.trim(),
    description: createSpaceDraft.value.description.trim(),
    owner: createSpaceDraft.value.owner.trim() || '我创建'
  }
  if (draft.name.length === 0) {
    createSpaceError.value = '请输入知识库名称。'
    return
  }
  isCreatingSpace.value = true
  createSpaceError.value = ''
  createSpaceStatus.value = ''
  try {
    const created = await createSpaceApi(draft)
    spaces.value = [created, ...spaces.value.filter(space => space.id !== created.id)]
    selectedSpaceId.value = created.id
    selectedProductSpaceId.value = created.id
    createSpaceStatus.value = '知识库已创建'
    isCreateSpaceOpen.value = false
  } catch (error) {
    createSpaceError.value = safeError(error, 'Knowledge Space creation failed safely.')
  } finally {
    isCreatingSpace.value = false
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

async function loadConnectorDefinitions() {
  isLoadingConnectors.value = true
  connectorError.value = ''
  try {
    connectorDefinitions.value = await listConnectorDefinitions()
  } catch (error) {
    connectorError.value = safeError(error, 'Connector definitions unavailable.')
  } finally {
    isLoadingConnectors.value = false
  }
}

async function startMockConnectorSync() {
  if (!selectedSpaceId.value || connectorDefinitions.value.length === 0) {
    return
  }
  isStartingConnectorSync.value = true
  connectorError.value = ''
  try {
    const connectorKey = connectorDefinitions.value[0].connectorKey
    const created = await startConnectorSync(selectedSpaceId.value, connectorKey)
    connectorSyncRun.value = await getConnectorSyncRun(created.runId)
    connectorSyncItems.value = await listConnectorSyncItems(created.runId)
    selectedConnectorItemId.value = connectorSyncItems.value[0]?.id ?? ''
  } catch (error) {
    connectorError.value = safeError(error, 'Connector sync failed safely.')
  } finally {
    isStartingConnectorSync.value = false
  }
}

async function loadDeadLetters() {
  isLoadingDeadLetters.value = true
  deadLetterError.value = ''
  try {
    deadLetterEntries.value = await listDeadLetterEntries()
    selectedDeadLetterId.value = deadLetterEntries.value[0]?.id ?? ''
  } catch (error) {
    deadLetterError.value = safeError(error, 'Worker recovery entries unavailable.')
  } finally {
    isLoadingDeadLetters.value = false
  }
}

async function retrySelectedDeadLetter() {
  const entry = selectedDeadLetterEntry.value
  if (!entry) {
    return
  }
  isRetryingDeadLetter.value = true
  deadLetterError.value = ''
  try {
    const updated = await retryDeadLetterEntry(entry.id)
    deadLetterEntries.value = deadLetterEntries.value.map(current =>
      current.id === updated.id ? updated : current
    )
    selectedDeadLetterId.value = updated.id
  } catch (error) {
    deadLetterError.value = safeError(error, 'Dead-letter retry transition failed safely.')
  } finally {
    isRetryingDeadLetter.value = false
  }
}

async function acknowledgeSelectedDeadLetter() {
  const entry = selectedDeadLetterEntry.value
  if (!entry) {
    return
  }
  isAcknowledgingDeadLetter.value = true
  deadLetterError.value = ''
  try {
    const updated = await acknowledgeDeadLetterEntry(entry.id)
    deadLetterEntries.value = deadLetterEntries.value.map(current =>
      current.id === updated.id ? updated : current
    )
    selectedDeadLetterId.value = updated.id
  } catch (error) {
    deadLetterError.value = safeError(error, 'Dead-letter acknowledge transition failed safely.')
  } finally {
    isAcknowledgingDeadLetter.value = false
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
    const [space, batchList, urlSources, queues, pages, issues, audits] = await Promise.all([
      getSpace(spaceId),
      listBatches(spaceId),
      listManualUrlSources(spaceId),
      getReviewQueues(spaceId),
      listWikiPages(spaceId, true),
      listWikiPageIssues(spaceId),
      canReadGovernance.value ? listAuditEvents(spaceId) : Promise.resolve([])
    ])
    selectedSpace.value = space
    batches.value = batchList
    manualUrlSources.value = urlSources
    reviewQueues.value = queues
    wikiPages.value = pages
    wikiPageIssues.value = issues
    auditEvents.value = audits
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
  if (!selectedSpaceId.value || !canWriteContent.value) {
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

async function handleDocumentUpload(event: { target: unknown }) {
  if (!selectedSpaceId.value || !canWriteContent.value) {
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

async function submitManualUrlSource() {
  if (!selectedSpaceId.value || !canWriteContent.value) {
    return
  }
  isCreatingManualUrl.value = true
  manualUrlError.value = ''
  workflowMessage.value = ''
  try {
    const source = await createManualUrlSource(selectedSpaceId.value, {
      url: manualUrlDraft.value.url.trim(),
      title: manualUrlDraft.value.title.trim() || undefined,
      description: manualUrlDraft.value.description.trim() || undefined,
      fetchIntent: manualUrlDraft.value.fetchIntent,
      createdBy: 'frontend-user'
    })
    workflowMessage.value = `Manual URL registered for review: ${source.host}`
    await refreshWorkflow(source.batchId, source.fileItemId)
  } catch (error) {
    manualUrlError.value = safeError(error, 'Manual URL registration failed safely.')
  } finally {
    isCreatingManualUrl.value = false
  }
}

async function approveSelectedFile() {
  const file = selectedFile.value
  if (!file || !canOperateKnowledge.value || chunks.value.length === 0) {
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
  if (!batch || !canOperateKnowledge.value || chunks.value.length === 0) {
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
  askQualityError.value = ''
  askRun.value = null
  askQualityMetrics.value = null
  try {
    const created = await createAskRun(
      selectedSpaceId.value,
      askQuestion.value.trim(),
      selectedFileId.value,
      selectedAskSession.value?.sessionId
    )
    askRun.value = await getAskRun(created.runId)
    try {
      askQualityMetrics.value = await getAskQualityMetrics(created.runId)
    } catch (qualityError) {
      askQualityError.value = safeError(qualityError, 'Retrieval quality metrics unavailable.')
    }
    await refreshAskSessions(askRun.value.sessionId)
  } catch (error) {
    askError.value = safeError(error, 'Trusted Ask failed safely.')
  } finally {
    isAsking.value = false
  }
}

async function refreshAskSessions(preferredSessionId?: string) {
  askSessionError.value = ''
  try {
    askSessions.value = await listAskSessions(selectedSpaceId.value)
    const sessionId = preferredSessionId ?? askSessions.value[0]?.sessionId
    selectedAskSession.value = sessionId ? await getAskSession(sessionId) : null
  } catch (error) {
    askSessionError.value = safeError(error, 'Ask session history failed safely.')
  }
}

async function submitProductAsk() {
  askQuestion.value = productAskQuestion.value
  productAskMode.value = 'answered'
  await submitAsk()
}

async function refreshWorkflow(batchId = selectedBatchId.value, fileId = selectedFileId.value) {
  const [batchList, urlSources, queues, pages, issues] = await Promise.all([
    listBatches(selectedSpaceId.value),
    listManualUrlSources(selectedSpaceId.value),
    getReviewQueues(selectedSpaceId.value),
    listWikiPages(selectedSpaceId.value, true),
    listWikiPageIssues(selectedSpaceId.value)
  ])
  batches.value = batchList
  manualUrlSources.value = urlSources
  reviewQueues.value = queues
  wikiPages.value = pages
  wikiPageIssues.value = issues
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
    graphState.value = isPermissionDeniedError(error, message) ? 'unauthorized' : 'error'
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

function modelCapabilityToVueModel(capability: ApiModelCapability): VueModelConfig {
  const configurationCredential =
    capability.adapterKey === 'deepseek'
      ? secretStatusByKey(deepSeekConfiguration.value?.secretStatuses, 'credential')
      : undefined
  const capabilityCredential = secretStatusByKey(capability.secretStatuses, 'credential')
  const credential = configurationCredential ?? capabilityCredential
  return {
    id: capability.modelKey,
    category: modelTypeToCategory(capability.modelType),
    displayName: capability.displayName,
    provider: capability.providerFamily,
    source: 'API',
    name: capability.modelKey,
    baseUrl: '',
    apiKeyStatus: isSecretConfigured(credential) ? 'configured' : 'not_configured',
    secretStatuses:
      capability.adapterKey === 'deepseek' && deepSeekConfiguration.value
        ? deepSeekConfiguration.value.secretStatuses
        : capability.secretStatuses,
    supportsMultimodal: capability.modelType === 'VISION',
    thinkingFormat: 'none',
    sourceLabel: `${capability.adapterKey} · ${capability.status}`
  }
}

function safeError(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    return safeApiErrorMessage(error)
  }
  return error instanceof Error ? error.message : fallback
}

function safeApiErrorMessage(error: ApiError) {
  const reference = error.correlationId ? ` Reference ${error.correlationId}.` : ''
  const retryAfter =
    error.code === 'RATE_LIMITED' && error.retryAfterSeconds != null
      ? ` Retry after ${error.retryAfterSeconds}s.`
      : ''
  return `${error.message}${retryAfter}${reference}`
}

function isPermissionDeniedError(error: unknown, message: string) {
  if (error instanceof ApiError) {
    return error.code === 'PERMISSION_DENIED' || error.status === 403
  }
  return message.toLowerCase().includes('permission denied')
}

function hasCapability(capability: string) {
  return currentCapabilities.value.has(capability)
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

function openSettings(panel: SettingsPanel = 'general') {
  if (panel === 'audit' && !canReadGovernance.value) {
    settingsPanel.value = 'general'
    settingsOpen.value = true
    return
  }
  settingsPanel.value = panel
  settingsOpen.value = true
  if (panel === 'audit') {
    void loadAuditEvents()
  }
}

function closeSettings() {
  settingsOpen.value = false
  modelDraft.value = null
  activeSpaceInfoEditField.value = null
}

async function loadAuditEvents(spaceId = selectedSpaceId.value) {
  if (!spaceId || !canReadGovernance.value) {
    auditEvents.value = []
    auditError.value = '当前账号没有审计日志读取权限。'
    return
  }
  isLoadingAuditEvents.value = true
  auditError.value = ''
  try {
    auditEvents.value = await listAuditEvents(spaceId)
  } catch (error) {
    auditError.value = safeError(error, 'Audit log API unavailable.')
  } finally {
    isLoadingAuditEvents.value = false
  }
}

function beginSpaceInfoEdit(field: keyof SpaceInfoDraft) {
  activeSpaceInfoEditField.value = field
  spaceInfoStatus.value = ''
  spaceInfoDraft.value = {
    name: selectedProductSpace.value.name,
    description: selectedProductSpace.value.description
  }
}

function cancelSpaceInfoEdit() {
  activeSpaceInfoEditField.value = null
  spaceInfoDraft.value = { name: '', description: '' }
}

function saveSpaceInfoEdit() {
  const name = spaceInfoDraft.value.name.trim()
  const description = spaceInfoDraft.value.description.trim()
  if (name.length === 0) {
    spaceInfoStatus.value = '空间名称不能为空。'
    return
  }
  const spaceId = selectedProductSpace.value.id
  const nextDraft = { name, description }
  spaceInfoOverrides.value = {
    ...spaceInfoOverrides.value,
    [spaceId]: nextDraft
  }
  spaces.value = spaces.value.map(space =>
    space.id === spaceId ? { ...space, name, description } : space
  )
  if (selectedSpace.value?.id === spaceId) {
    selectedSpace.value = { ...selectedSpace.value, name, description }
  }
  activeSpaceInfoEditField.value = null
  spaceInfoStatus.value = '空间信息已更新'
}

function formatSpaceTimestamp(value: string) {
  const [datePart, timePart = ''] = value.replace('Z', '').split('T')
  const date = datePart.replaceAll('-', '/')
  const time = timePart.slice(0, 5)
  return time ? `${date} ${time}` : date
}

function memberRoleLabel(role: MemberRole) {
  return memberRoleOptions.find(option => option.value === role)?.label ?? role
}

function handleMemberRoleChange(memberId: string, role: MemberRole) {
  const target = spaceMembers.value.find(member => member.id === memberId)
  spaceMembers.value = spaceMembers.value.map(member =>
    member.id === memberId ? { ...member, role } : member
  )
  memberActionStatus.value = `${target?.name ?? '成员'} 的角色已更新为 ${memberRoleLabel(role)}（mock only）。`
}

function inviteMockMember() {
  memberActionStatus.value = '邀请成员动作已记录为 mock，不发送邮件，也不创建真实账号。'
}

function copyInviteMockLink() {
  memberActionStatus.value = '邀请链接已复制为 mock 状态，不包含真实 workspace token。'
}

function removeSpaceMember(memberId: string) {
  const target = spaceMembers.value.find(member => member.id === memberId)
  if (!target || !target.removable) {
    memberActionStatus.value = '所有者账号不能在当前 mock 面板中移除。'
    return
  }
  spaceMembers.value = spaceMembers.value.filter(member => member.id !== memberId)
  memberActionStatus.value = `${target.name} 已从当前 mock 空间成员列表移除。`
}

function toggleApiKeyVisibility() {
  apiInfo.value = {
    ...apiInfo.value,
    status: 'API Key 保持隐藏（mock）'
  }
}

function copyApiInfoValue(successMessage: string) {
  apiInfo.value = {
    ...apiInfo.value,
    status: successMessage
  }
}

function refreshApiKey() {
  apiInfo.value = {
    ...apiInfo.value,
    keyVersion: apiInfo.value.keyVersion + 1,
    status: 'API Key 已刷新（mock）'
  }
}

function openApiDocumentation() {
  apiInfo.value = {
    ...apiInfo.value,
    status: 'API 文档入口已准备（mock）'
  }
}

function toggleMessageIndexing() {
  messageIndexEnabled.value = !messageIndexEnabled.value
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

function openModelEditor(model: VueModelConfig) {
  selectedModelId.value = model.id
  isModelAddMenuOpen.value = false
  modelTestStatus.value = ''
  modelSaveStatus.value = ''
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
    modelDraft.value = {
      ...modelDraft.value,
      apiKeyStatus: 'not_configured',
      apiKeyEditing: false,
      secretStatuses: deepSeekConfiguration.value?.secretStatuses ?? modelDraft.value.secretStatuses
    }
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
  modelApiError.value = ''
  modelSaveStatus.value = ''
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
    persisted.apiKeyStatus = isSecretConfigured(
      secretStatusByKey(deepSeekConfiguration.value.secretStatuses, 'credential')
    )
      ? 'configured'
      : 'not_configured'
    persisted.secretStatuses = deepSeekConfiguration.value.secretStatuses
    persisted.baseUrl = ''
  } else if (persisted.category !== 'chat') {
    persisted.apiKeyStatus = 'not_configured'
  }
  models.value = models.value.map(model => (model.id === persisted.id ? persisted : model))
  selectedModelId.value = persisted.id
  modelTestStatus.value = ''
  modelSaveStatus.value = '配置已更新'
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
  <ProductShell
    v-if="activeExperience === 'atlas'"
    :product-view="productView"
    :can-manage-members="canManageMembers"
    @show-home="showProductHome"
    @show-chat="showProductChat"
    @open-settings="openSettings"
  >
    <template v-if="productView === 'home'">
      <ProductHomeView
        :product-space-cards="productSpaceCards"
        :api-space-count="spaces.length"
        :can-manage-spaces="canManageSpaces"
        :is-create-space-open="isCreateSpaceOpen"
        :is-creating-space="isCreatingSpace"
        :create-space-draft="createSpaceDraft"
        :create-space-status="createSpaceStatus"
        :create-space-error="createSpaceError"
        @open-create-space="openCreateSpacePanel"
        @close-create-space="closeCreateSpacePanel"
        @create-space="createProductSpace"
        @open-space="openProductSpace"
        @update-create-space-draft="draft => (createSpaceDraft = draft)"
      />
    </template>

    <template v-else-if="productView === 'chat'">
      <GlobalChatView
        :product-space-cards="productSpaceCards"
        :selected-chat-spaces="selectedChatSpaces"
        :product-ask-question="productAskQuestion"
        :product-ask-mode="productAskMode"
        :visible-models="visibleModels"
        :product-ask-answer="productAskAnswer"
        :ask-quality-chips="askQualityChips"
        :ask-quality-error="askQualityError"
        @toggle-chat-space="toggleChatSpace"
        @update-product-ask-question="question => (productAskQuestion = question)"
        @update-product-ask-mode="mode => (productAskMode = mode)"
        @submit-product-ask="submitProductAsk"
      />
    </template>

    <template v-else>
      <SpaceDetailView
        :selected-product-space="selectedProductSpace"
        :active-space-tab="activeSpaceTab"
        :workflow-error="workflowError"
        @show-home="showProductHome"
        @open-mock-upload="openMockUpload"
        @update-active-tab="tab => (activeSpaceTab = tab)"
      >
        <SpaceDocumentsTab
          v-if="activeSpaceTab === 'docs'"
          :api-batch-metrics="apiBatchMetrics"
          :product-batch-metrics="productBatchMetrics"
          :api-backed-product-space="apiBackedProductSpace"
          :selected-product-space="selectedProductSpace"
          :is-creating-batch="isCreatingBatch"
          :can-write-content="canWriteContent"
          :manual-url-sources="manualUrlSources"
          :manual-url-draft="manualUrlDraft"
          :is-creating-manual-url="isCreatingManualUrl"
          :manual-url-error="manualUrlError"
          :workflow-message="workflowMessage"
          :workflow-error="workflowError"
          :batches="batches"
          :files="files"
          :chunks="chunks"
          :upload-session="uploadSession"
          :active-product-batch-files="activeProductBatchFiles"
          :visible-product-batch-files="visibleProductBatchFiles"
          :is-product-report-open="isProductReportOpen"
          :report-sections="reportSections"
          @document-upload="handleDocumentUpload"
          @create-api-batch="createBatchFromBrowser"
          @update-manual-url-draft="draft => (manualUrlDraft = draft)"
          @submit-manual-url-source="submitManualUrlSource"
          @select-batch="selectBatch"
          @select-file="selectFile"
          @cancel-mock-upload="cancelMockUpload"
          @create-product-batch="createProductBatch"
          @open-report="openProductReport"
          @close-report="closeProductReport"
        />
        <SpaceConnectorsTab
          v-else-if="activeSpaceTab === 'connectors'"
          :connector-definitions="connectorDefinitions"
          :is-loading-connectors="isLoadingConnectors"
          :is-starting-connector-sync="isStartingConnectorSync"
          :selected-space-id="selectedSpaceId"
          :connector-sync-run="connectorSyncRun"
          :connector-error="connectorError"
          :connector-sync-items="connectorSyncItems"
          :selected-connector-item="selectedConnectorItem"
          @start-connector-sync="startMockConnectorSync"
          @select-connector-item="itemId => (selectedConnectorItemId = itemId)"
        />
        <SpaceReviewTab
          v-else-if="activeSpaceTab === 'review'"
          :api-blocked-review-count="apiBlockedReviewCount"
          :api-ready-to-publish-count="apiReadyToPublishCount"
          :manual-url-review-required-count="manualUrlReviewRequiredCount"
          :product-batch-metrics="productBatchMetrics"
          :review-queue-cards="reviewQueueCards"
          :api-processing-issues="apiProcessingIssues"
          :can-approve="canApprove"
          :is-reviewing="isReviewing"
          :dead-letter-entries="deadLetterEntries"
          :is-loading-dead-letters="isLoadingDeadLetters"
          :dead-letter-error="deadLetterError"
          :selected-dead-letter-entry="selectedDeadLetterEntry"
          :is-retrying-dead-letter="isRetryingDeadLetter"
          :is-acknowledging-dead-letter="isAcknowledgingDeadLetter"
          :product-processing-issues="productProcessingIssues"
          @approve-selected-file="approveSelectedFile"
          @load-dead-letters="loadDeadLetters"
          @select-dead-letter-entry="entryId => (selectedDeadLetterId = entryId)"
          @retry-selected-dead-letter="retrySelectedDeadLetter"
          @acknowledge-selected-dead-letter="acknowledgeSelectedDeadLetter"
        />
        <SpaceWikiTab
          v-else-if="activeSpaceTab === 'wiki'"
          :visible-product-wiki-pages="visibleProductWikiPages"
          :selected-product-wiki-page="selectedProductWikiPage"
          :selected-product-wiki-page-issues="selectedProductWikiPageIssues"
          :can-publish="canPublish"
          :is-publishing="isPublishing"
          @select-wiki-page="selectProductWikiPage"
          @publish-selected-file="publishSelectedFile"
        />
        <SpaceGraphTab
          v-else
          :product-graph-search="productGraphSearch"
          :visible-product-graph-nodes="visibleProductGraphNodes"
          :visible-product-graph-edges="visibleProductGraphEdges"
          :selected-product-graph-node="selectedProductGraphNode"
          :selected-product-graph-evidence="selectedProductGraphEvidence"
          @update-product-graph-search="query => (productGraphSearch = query)"
          @select-graph-node="selectProductGraphNode"
        />
      </SpaceDetailView>
    </template>

    <SettingsModal
      v-if="settingsOpen"
      :settings-panel="settingsPanel"
      :can-read-governance="canReadGovernance"
      @close="closeSettings"
      @select-panel="panel => (settingsPanel = panel)"
      @open-panel="openSettings"
    >
      <GeneralSettingsPanel
        v-if="settingsPanel === 'general'"
        :general-settings="generalSettings"
        :language-options="languageOptions"
        :theme-options="themeOptions"
        :interface-font-options="interfaceFontOptions"
        :code-font-options="codeFontOptions"
        :font-size-options="fontSizeOptions"
        @close="closeSettings"
        @update-general-settings="settings => (generalSettings = settings)"
      />
      <ProfileSettingsPanel
        v-else-if="settingsPanel === 'profile'"
        :account-profile-rows="accountProfileRows"
        @close="closeSettings"
      />
      <SpaceInfoSettingsPanel
        v-else-if="settingsPanel === 'spaceInfo'"
        :selected-product-space="selectedProductSpace"
        :selected-space-info-rows="selectedSpaceInfoRows"
        :active-space-info-edit-field="activeSpaceInfoEditField"
        :space-info-draft="spaceInfoDraft"
        :space-info-status="spaceInfoStatus"
        @close="closeSettings"
        @begin-space-info-edit="beginSpaceInfoEdit"
        @cancel-space-info-edit="cancelSpaceInfoEdit"
        @save-space-info-edit="saveSpaceInfoEdit"
        @update-space-info-draft="draft => (spaceInfoDraft = draft)"
      />
      <MembersSettingsPanel
        v-else-if="settingsPanel === 'members'"
        :pending-invitation-count="pendingInvitationCount"
        :pending-invitations="pendingInvitations"
        :space-members="spaceMembers"
        :filtered-space-members="filteredSpaceMembers"
        :member-search="memberSearch"
        :can-manage-members="canManageMembers"
        :member-action-status="memberActionStatus"
        :member-role-options="memberRoleOptions"
        @close="closeSettings"
        @update-member-search="query => (memberSearch = query)"
        @note-audit-entry="memberActionStatus = '审计日志入口为 mock。'"
        @invite-mock-member="inviteMockMember"
        @copy-invite-mock-link="copyInviteMockLink"
        @handle-member-role-change="handleMemberRoleChange"
        @remove-space-member="removeSpaceMember"
      />
      <AuditLogSettingsPanel
        v-else-if="settingsPanel === 'audit'"
        :selected-product-space="selectedProductSpace"
        :audit-summary="auditSummary"
        :audit-events="auditEvents"
        :is-loading-audit-events="isLoadingAuditEvents"
        :audit-error="auditError"
        @close="closeSettings"
      />
      <ApiInfoSettingsPanel
        v-else-if="settingsPanel === 'api'"
        :api-info="apiInfo"
        :api-key-display-value="apiKeyDisplayValue"
        :safe-error-previews="safeErrorPreviews"
        @close="closeSettings"
        @toggle-api-key-visibility="toggleApiKeyVisibility"
        @copy-api-info-value="copyApiInfoValue"
        @refresh-api-key="refreshApiKey"
        @open-api-documentation="openApiDocumentation"
      />
      <MessageSettingsPanel
        v-else-if="settingsPanel === 'messages'"
        :message-index-enabled="messageIndexEnabled"
        :message-embedding-model="messageEmbeddingModel"
        :message-embedding-options="messageEmbeddingOptions"
        :message-index-configured="messageIndexConfigured"
        :message-index-stats="messageIndexStats"
        @close="closeSettings"
        @toggle-message-indexing="toggleMessageIndexing"
        @update-message-embedding-model="model => (messageEmbeddingModel = model)"
      />
      <PlaceholderSettingsPanelView
        v-else-if="currentSettingsSurface"
        :settings-surface="currentSettingsSurface"
        @close="closeSettings"
      />
      <ModelsSettingsPanel
        v-else
        :is-model-add-menu-open="isModelAddMenuOpen"
        :addable-model-types="addableModelTypes"
        :model-api-status="modelApiStatus"
        :model-save-status="modelSaveStatus"
        :model-api-error="modelApiError"
        :model-categories="modelCategories"
        :active-model-category="activeModelCategory"
        :models="models"
        :visible-models="visibleModels"
        @close="closeSettings"
        @toggle-model-add-menu="isModelAddMenuOpen = !isModelAddMenuOpen"
        @update-active-model-category="category => (activeModelCategory = category)"
        @open-model-editor="openModelEditor"
      />
    </SettingsModal>

    <ModelEditor
      v-if="modelDraft"
      :model-draft="modelDraft"
      :model-provider-options="modelProviderOptions"
      :thinking-options="thinkingOptions"
      :api-key-input="apiKeyInput"
      :model-test-status="modelTestStatus"
      @close="closeModelEditor"
      @update-model-draft="draft => (modelDraft = draft)"
      @update-api-key-input="value => (apiKeyInput = value)"
      @set-model-source="setModelSource"
      @start-api-key-replace="startApiKeyReplace"
      @remove-api-key="removeApiKey"
      @confirm-api-key-replace="confirmApiKeyReplace"
      @cancel-api-key-replace="cancelApiKeyReplace"
      @toggle-model-multimodal="toggleModelMultimodal"
      @test-model-connection="testModelConnection"
      @save-model-editor="saveModelEditor"
    />

    <button class="prototype-entry" type="button" @click="activeExperience = 'prototype'">
      Prototype iframe
    </button>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </ProductShell>

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
                :disabled="isCreatingBatch || !selectedSpaceId || !canWriteContent"
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
                <span
                  >{{ page.slug ?? page.markdownPath }} · {{ page.pageType ?? 'SOURCE_SUMMARY' }} ·
                  v{{ page.version ?? 1 }}</span
                >
                <span
                  >{{ page.sourceMode ?? 'PUBLISHED_FILE' }} ·
                  {{ page.refreshPolicy ?? 'MANUAL' }}</span
                >
                <span>{{ page.markdownPath }}</span>
                <span>sources {{ page.sourceDocumentIds.join(', ') }}</span>
                <span
                  >refs
                  {{
                    page.sourceRefs?.length > 0
                      ? page.sourceRefs.map(formatWikiReference).join('; ')
                      : 'none'
                  }}</span
                >
                <span
                  >links in {{ page.inLinks?.length ?? 0 }} / out
                  {{ page.outLinks?.length ?? 0 }}</span
                >
              </article>
            </div>
            <button
              data-testid="refresh-graph-evidence"
              type="button"
              :disabled="
                wikiPages.length === 0 ||
                chunks.length === 0 ||
                isRefreshingEvidence ||
                !canOperateKnowledge
              "
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
              <strong
                >{{ askRun.status }} · {{ askRun.answerReviewStatus }} ·
                {{ askRun.answerReviewLabel }} ·
                {{ askRun.sessionTitle ?? askRun.sessionId }}</strong
              >
              <p>{{ askRun.answer ?? askRun.safeMessage }}</p>
              <p class="source-line">{{ answerReuseHint(askRun) }}</p>
              <p class="source-line">{{ answerReviewReasonLine(askRun) }}</p>
              <span>confidence {{ askRun.answerConfidence ?? 'n/a' }}</span>
              <div v-if="askQualityChips.length > 0" class="ask-quality-signals">
                <span v-for="chip in askQualityChips" :key="chip">{{ chip }}</span>
              </div>
              <p v-if="askQualityError" class="state-message warning">{{ askQualityError }}</p>
              <ul>
                <li v-for="evidence in askRun.evidence" :key="evidence.evidenceId">
                  {{ evidence.citationId }} · {{ evidence.evidenceLabel }} ·
                  {{ evidence.sourceLocator }} · {{ evidence.citationStatus }} · score
                  {{ evidence.score ?? 'n/a' }}
                  <span v-if="!evidence.reviewEligible"> · {{ evidence.excludedReason }}</span>
                </li>
              </ul>
            </article>
            <p v-if="askSessionError" class="state-message error">{{ askSessionError }}</p>
            <article v-if="selectedAskSession" class="ask-answer" data-testid="ask-session-history">
              <strong>{{ selectedAskSession.title }}</strong>
              <span>{{ askSessions.length }} recent sessions</span>
              <ul>
                <li v-for="run in selectedAskSession.runs" :key="run.runId">
                  {{ run.question }} · {{ run.status }} · {{ run.evidence.length }} citations
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
          <li
            v-for="evidence in evidenceReferences"
            :key="`${evidence.referenceType ?? 'SOURCE_CHUNK'}-${evidence.sourceChunkId ?? evidence.wikiPageId}`"
          >
            {{ formatGraphEvidenceReference(evidence) }}
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
