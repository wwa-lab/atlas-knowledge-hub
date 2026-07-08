import { beforeEach, describe, expect, it, vi } from 'vitest'
import { listWikiPageIssues, listWikiPages, publishFile, refreshDownstreamEvidenceApi } from '@/api'
import { useWikiPages } from '@/composables/useWikiPages'
import type { ApiBatch, ApiFileItem, ApiSourceChunk, ApiWikiPage, ApiWikiPageIssue } from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    listWikiPageIssues: vi.fn(),
    listWikiPages: vi.fn(),
    publishFile: vi.fn(),
    refreshDownstreamEvidenceApi: vi.fn()
  }
})

const mockListWikiPageIssues = vi.mocked(listWikiPageIssues)
const mockListWikiPages = vi.mocked(listWikiPages)
const mockPublishFile = vi.mocked(publishFile)
const mockRefreshDownstreamEvidenceApi = vi.mocked(refreshDownstreamEvidenceApi)

describe('useWikiPages', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads API wiki pages and maps source trace metadata into product pages', async () => {
    mockListWikiPages.mockResolvedValue([wikiPage()])
    mockListWikiPageIssues.mockResolvedValue([wikiIssue()])
    const state = useWikiPages(testOptions())

    await state.loadWikiState('space-1')

    expect(state.visibleProductWikiPages.value[0]).toMatchObject({
      id: 'wiki-1',
      title: 'Published Wiki',
      sourceTrace: 'SOURCE_CHUNK: chunk-1 · Chunk One / page 1'
    })
    expect(state.apiWikiIssueCards.value[0]).toMatchObject({
      type: 'BROKEN_LINK',
      count: 1,
      status: 'review warning'
    })
    expect(state.selectedProductWikiPageIssues.value).toHaveLength(1)
  })

  it('publishes the selected approved file and refreshes workflow state', async () => {
    mockPublishFile.mockResolvedValue(wikiPage({ title: 'P0 Wiki Published' }))
    const refreshWorkflow = vi.fn<() => Promise<void>>().mockResolvedValue(undefined)
    const setWorkflowMessage = vi.fn()
    const state = useWikiPages(
      testOptions({
        refreshWorkflow,
        setWorkflowMessage
      })
    )

    await state.publishSelectedFile()

    expect(mockPublishFile).toHaveBeenCalledWith('file-1', expect.stringContaining('P0 Wiki'))
    expect(setWorkflowMessage).toHaveBeenCalledWith('Published Wiki page: P0 Wiki Published')
    expect(refreshWorkflow).toHaveBeenCalledWith('batch-1', 'file-1')
  })

  it('refreshes downstream evidence and delegates graph reload', async () => {
    mockRefreshDownstreamEvidenceApi.mockResolvedValue({
      graphRun: {
        runId: 'graph-1',
        spaceId: 'space-1',
        adapterId: 'deterministic',
        scope: 'APPROVED_ONLY',
        status: 'SUCCEEDED',
        safeMessage: null,
        summary: {}
      },
      vectorRun: {
        runId: 'vector-1',
        spaceId: 'space-1',
        batchId: 'batch-1',
        adapterKey: 'mock-vector',
        operation: 'INDEX',
        status: 'SUCCEEDED',
        safeMessage: null
      },
      message: 'ok'
    })
    const loadGraph = vi.fn<() => Promise<void>>().mockResolvedValue(undefined)
    const state = useWikiPages(testOptions({ loadGraph }))

    await state.refreshDownstreamEvidence()

    expect(mockRefreshDownstreamEvidenceApi).toHaveBeenCalledWith('space-1', 'batch-1', 'file-1')
    expect(loadGraph).toHaveBeenCalledWith('graph search')
    expect(state.downstreamReady.value).toBe(true)
  })
})

function testOptions(overrides: Partial<Parameters<typeof useWikiPages>[0]> = {}) {
  return {
    selectedSpaceId: () => 'space-1',
    selectedBatch: () => batch(),
    selectedBatchId: () => 'batch-1',
    selectedFile: () => file(),
    selectedFileId: () => 'file-1',
    chunks: () => [chunk()],
    canOperateKnowledge: () => true,
    refreshWorkflow: vi.fn<() => Promise<void>>().mockResolvedValue(undefined),
    loadGraph: vi.fn<() => Promise<void>>().mockResolvedValue(undefined),
    currentGraphQuery: () => 'graph search',
    setWorkflowMessage: vi.fn(),
    setWorkflowError: vi.fn(),
    ...overrides
  }
}

function batch(): ApiBatch {
  return {
    id: 'batch-1',
    spaceId: 'space-1',
    name: 'Batch One',
    sourceKind: 'folder',
    owner: 'owner',
    uploadedAt: '2026-07-08T00:00:00Z',
    metrics: {
      totalFiles: 1,
      pdfConverted: 1,
      markdownGenerated: 1,
      reviewRequired: 0,
      failed: 0,
      unsupported: 0
    }
  }
}

function file(): ApiFileItem {
  return {
    id: 'file-1',
    batchId: 'batch-1',
    sourcePath: 'samples/mock.md',
    sourceType: 'pdf',
    status: 'MARKDOWN_GENERATED',
    confidence: 0.95,
    reviewStatus: 'APPROVED',
    pdfPath: null,
    markdownPath: 'generated/mock.md',
    assetsPath: null,
    errorMessage: null
  }
}

function chunk(): ApiSourceChunk {
  return {
    id: 'chunk-1',
    fileItemId: 'file-1',
    sourceFile: 'mock.md',
    page: 1,
    section: 'Evidence',
    confidence: 0.95,
    reviewStatus: 'APPROVED'
  }
}

function wikiPage(overrides: Partial<ApiWikiPage> = {}): ApiWikiPage {
  return {
    id: 'wiki-1',
    spaceId: 'space-1',
    folderId: null,
    title: 'Published Wiki',
    slug: 'published-wiki',
    pageType: 'SOURCE_SUMMARY',
    markdownPath: 'generated/wiki.md',
    sourceDocumentIds: ['file-1'],
    aliases: [],
    sourceRefs: [{ type: 'SOURCE_CHUNK', id: 'chunk-1', label: 'Chunk One', locator: 'page 1' }],
    chunkRefs: [],
    inLinks: [],
    outLinks: [],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
    confidence: 0.95,
    reviewStatus: 'PUBLISHED',
    owner: 'owner',
    lastUpdated: '2026-07-08',
    ...overrides
  }
}

function wikiIssue(): ApiWikiPageIssue {
  return {
    id: 'issue-1',
    spaceId: 'space-1',
    pageId: 'wiki-1',
    issueType: 'BROKEN_LINK',
    severity: 'HIGH',
    status: 'OPEN',
    evidenceRefs: [],
    message: 'Broken link',
    createdAt: '2026-07-08T00:00:00Z',
    resolvedAt: null
  }
}
