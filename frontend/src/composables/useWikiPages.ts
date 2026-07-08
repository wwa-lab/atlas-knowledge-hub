import { computed, ref } from 'vue'
import {
  listWikiPageIssues,
  listWikiPages,
  publishFile as publishFileApi,
  refreshDownstreamEvidenceApi
} from '@/api'
import { safeError } from '@/composables/apiErrors'
import { formatWikiReference, wikiSourceTrace } from '@/domain/viewModels'
import type { ProductWikiPage } from '@/domain/viewModels'
import type { ApiBatch, ApiFileItem, ApiSourceChunk, ApiWikiPage, ApiWikiPageIssue } from '@/types'

export interface UseWikiPagesOptions {
  selectedSpaceId: () => string
  selectedBatch: () => ApiBatch | null
  selectedBatchId: () => string
  selectedFile: () => ApiFileItem | null
  selectedFileId: () => string
  chunks: () => ApiSourceChunk[]
  canOperateKnowledge: () => boolean
  refreshWorkflow: (batchId?: string, fileId?: string) => Promise<void>
  loadGraph: (query?: string) => Promise<void>
  currentGraphQuery: () => string
  setWorkflowMessage: (message: string) => void
  setWorkflowError: (message: string) => void
}

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

export function useWikiPages(options: UseWikiPagesOptions) {
  const wikiPages = ref<ApiWikiPage[]>([])
  const wikiPageIssues = ref<ApiWikiPageIssue[]>([])
  const selectedProductWikiPageId = ref('wiki-modernization-index')
  const isPublishing = ref(false)
  const isRefreshingEvidence = ref(false)
  const downstreamReady = ref(false)

  const canPublish = computed(
    () => options.canOperateKnowledge() && options.selectedFile()?.reviewStatus === 'APPROVED'
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

  async function loadWikiState(spaceId: string) {
    const [pages, issues] = await Promise.all([
      listWikiPages(spaceId, true),
      listWikiPageIssues(spaceId)
    ])
    wikiPages.value = pages
    wikiPageIssues.value = issues
  }

  async function publishSelectedFile() {
    const file = options.selectedFile()
    if (!file || !canPublish.value) {
      return
    }
    isPublishing.value = true
    options.setWorkflowError('')
    try {
      const title = `P0 Wiki ${new Date().toISOString().slice(0, 10)}`
      const page = await publishFileApi(file.id, title)
      options.setWorkflowMessage(`Published Wiki page: ${page.title}`)
      await options.refreshWorkflow(options.selectedBatchId(), file.id)
    } catch (error) {
      options.setWorkflowError(safeError(error, 'Publish failed safely.'))
    } finally {
      isPublishing.value = false
    }
  }

  async function refreshDownstreamEvidence() {
    const batch = options.selectedBatch()
    if (!batch || !options.canOperateKnowledge() || options.chunks().length === 0) {
      return
    }
    isRefreshingEvidence.value = true
    options.setWorkflowError('')
    try {
      await refreshDownstreamEvidenceApi(
        options.selectedSpaceId(),
        batch.id,
        options.selectedFileId()
      )
      downstreamReady.value = true
      options.setWorkflowMessage('Graph and Ask evidence refreshed.')
      await options.loadGraph(options.currentGraphQuery())
    } catch (error) {
      options.setWorkflowError(safeError(error, 'Downstream evidence refresh failed safely.'))
    } finally {
      isRefreshingEvidence.value = false
    }
  }

  function selectProductWikiPage(pageId: string) {
    selectedProductWikiPageId.value = pageId
  }

  return {
    wikiPages,
    wikiPageIssues,
    selectedProductWikiPageId,
    isPublishing,
    isRefreshingEvidence,
    downstreamReady,
    canPublish,
    apiWikiIssueCards,
    apiProductWikiPages,
    visibleProductWikiPages,
    selectedProductWikiPage,
    selectedProductWikiPageIssues,
    loadWikiState,
    publishSelectedFile,
    refreshDownstreamEvidence,
    selectProductWikiPage
  }
}
