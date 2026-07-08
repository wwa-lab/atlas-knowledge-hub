import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createManualUrlSource,
  createSampleBatch,
  getConnectorSyncRun,
  listBatches,
  listChunks,
  listConnectorDefinitions,
  listConnectorSyncItems,
  listFiles,
  listManualUrlSources,
  startConnectorSync,
  uploadDocuments
} from '@/api'
import { useBatches } from '@/composables/useBatches'
import type {
  ApiBatch,
  ApiConnectorDefinition,
  ApiConnectorSyncItem,
  ApiConnectorSyncRun,
  ApiFileItem,
  ApiIngestionResponse,
  ApiManualUrlSource,
  ApiSourceChunk
} from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    createManualUrlSource: vi.fn(),
    createSampleBatch: vi.fn(),
    getConnectorSyncRun: vi.fn(),
    listBatches: vi.fn(),
    listChunks: vi.fn(),
    listConnectorDefinitions: vi.fn(),
    listConnectorSyncItems: vi.fn(),
    listFiles: vi.fn(),
    listManualUrlSources: vi.fn(),
    startConnectorSync: vi.fn(),
    uploadDocuments: vi.fn()
  }
})

const mockCreateManualUrlSource = vi.mocked(createManualUrlSource)
const mockCreateSampleBatch = vi.mocked(createSampleBatch)
const mockGetConnectorSyncRun = vi.mocked(getConnectorSyncRun)
const mockListBatches = vi.mocked(listBatches)
const mockListChunks = vi.mocked(listChunks)
const mockListConnectorDefinitions = vi.mocked(listConnectorDefinitions)
const mockListConnectorSyncItems = vi.mocked(listConnectorSyncItems)
const mockListFiles = vi.mocked(listFiles)
const mockListManualUrlSources = vi.mocked(listManualUrlSources)
const mockStartConnectorSync = vi.mocked(startConnectorSync)
const mockUploadDocuments = vi.mocked(uploadDocuments)

describe('useBatches', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads batch context and selects the first reviewable file and chunks', async () => {
    mockListBatches.mockResolvedValue([batch()])
    mockListManualUrlSources.mockResolvedValue([manualUrl()])
    mockListFiles.mockResolvedValue([
      file({ id: 'file-published', reviewStatus: 'PUBLISHED' }),
      file()
    ])
    mockListChunks.mockResolvedValue([chunk()])

    const state = useBatches({ selectedSpaceId: () => 'space-1', canWriteContent: () => true })
    await state.loadBatchContext('space-1')

    expect(state.selectedBatchId.value).toBe('batch-1')
    expect(state.selectedFileId.value).toBe('file-1')
    expect(state.chunks.value).toHaveLength(1)
    expect(state.apiBatchMetrics.value).toMatchObject({
      total: 2,
      batchCount: 1,
      fileCount: 2,
      chunkCount: 1
    })
    expect(state.manualUrlReviewRequiredCount.value).toBe(1)
  })

  it('creates a sample batch and refreshes review/wiki state through explicit callbacks', async () => {
    const refreshReviewState = vi.fn<() => Promise<void>>().mockResolvedValue(undefined)
    const refreshWikiState = vi.fn<() => Promise<void>>().mockResolvedValue(undefined)
    mockCreateSampleBatch.mockResolvedValue(batch({ id: 'batch-new', name: 'Created Batch' }))
    mockListBatches.mockResolvedValue([batch({ id: 'batch-new', name: 'Created Batch' })])
    mockListManualUrlSources.mockResolvedValue([])
    mockListFiles.mockResolvedValue([file()])
    mockListChunks.mockResolvedValue([chunk()])

    const state = useBatches({
      selectedSpaceId: () => 'space-1',
      canWriteContent: () => true,
      refreshReviewState,
      refreshWikiState
    })
    await state.createBatchFromBrowser()

    expect(mockCreateSampleBatch).toHaveBeenCalledWith(
      'space-1',
      expect.stringContaining('P0 Browser Evidence')
    )
    expect(refreshReviewState).toHaveBeenCalledWith('space-1')
    expect(refreshWikiState).toHaveBeenCalledWith('space-1')
    expect(state.workflowMessage.value).toContain('Created Batch')
    expect(state.selectedBatchId.value).toBe('batch-new')
  })

  it('registers manual URL metadata and uploads selected files without exposing raw input state', async () => {
    mockCreateManualUrlSource.mockResolvedValue(
      manualUrl({ batchId: 'batch-url', fileItemId: 'file-url', host: 'example.com' })
    )
    mockUploadDocuments.mockResolvedValue(ingestionResponse())
    mockListBatches.mockResolvedValue([batch({ id: 'batch-url' })])
    mockListManualUrlSources.mockResolvedValue([manualUrl()])
    mockListFiles.mockResolvedValue([file({ id: 'file-url' })])
    mockListChunks.mockResolvedValue([chunk()])

    const state = useBatches({ selectedSpaceId: () => 'space-1', canWriteContent: () => true })
    state.manualUrlDraft.value = {
      url: ' https://example.com/docs ',
      title: ' Manual URL ',
      description: ' ',
      fetchIntent: 'FETCH_LATER'
    }

    await state.submitManualUrlSource()
    expect(mockCreateManualUrlSource).toHaveBeenCalledWith('space-1', {
      url: 'https://example.com/docs',
      title: 'Manual URL',
      description: undefined,
      fetchIntent: 'FETCH_LATER',
      createdBy: 'frontend-user'
    })

    const target = { files: [new File(['mock'], 'mock.pdf')], value: 'selected' }
    await state.handleDocumentUpload({ target })
    expect(target.value).toBe('')
    expect(mockUploadDocuments).toHaveBeenCalledWith('space-1', target.files, 'p0-browser')
    expect(state.workflowMessage.value).toContain('Uploaded 1 file')
  })

  it('loads connector definitions and starts a connector sync run', async () => {
    mockListConnectorDefinitions.mockResolvedValue([connectorDefinition()])
    mockStartConnectorSync.mockResolvedValue(connectorRun({ runId: 'connector-created' }))
    mockGetConnectorSyncRun.mockResolvedValue(connectorRun({ runId: 'connector-created' }))
    mockListConnectorSyncItems.mockResolvedValue([connectorItem()])

    const state = useBatches({ selectedSpaceId: () => 'space-1', canWriteContent: () => true })
    await state.loadConnectorDefinitions()
    await state.startMockConnectorSync()

    expect(mockStartConnectorSync).toHaveBeenCalledWith('space-1', 'mock-local-fixture')
    expect(state.connectorSyncRun.value?.runId).toBe('connector-created')
    expect(state.selectedConnectorItem.value?.id).toBe('connector-item-1')
  })
})

function batch(overrides: Partial<ApiBatch> = {}): ApiBatch {
  return {
    id: 'batch-1',
    spaceId: 'space-1',
    name: 'Batch One',
    sourceKind: 'folder',
    owner: 'owner',
    uploadedAt: '2026-07-08T00:00:00Z',
    metrics: {
      totalFiles: 2,
      pdfConverted: 1,
      markdownGenerated: 1,
      reviewRequired: 1,
      failed: 0,
      unsupported: 0
    },
    ...overrides
  }
}

function file(overrides: Partial<ApiFileItem> = {}): ApiFileItem {
  return {
    id: 'file-1',
    batchId: 'batch-1',
    sourcePath: 'samples/mock.pdf',
    sourceType: 'pdf',
    status: 'MARKDOWN_GENERATED',
    confidence: 0.93,
    reviewStatus: 'REVIEW_REQUIRED',
    pdfPath: null,
    markdownPath: null,
    assetsPath: null,
    errorMessage: null,
    ...overrides
  }
}

function chunk(overrides: Partial<ApiSourceChunk> = {}): ApiSourceChunk {
  return {
    id: 'chunk-1',
    fileItemId: 'file-1',
    sourceFile: 'mock.pdf',
    page: 1,
    section: 'Evidence',
    confidence: 0.93,
    reviewStatus: 'APPROVED',
    ...overrides
  }
}

function manualUrl(overrides: Partial<ApiManualUrlSource> = {}): ApiManualUrlSource {
  return {
    id: 'url-1',
    spaceId: 'space-1',
    displayUrl: 'https://example.com/docs',
    host: 'example.com',
    title: 'Manual URL',
    description: 'Sample URL',
    fetchIntent: 'FETCH_LATER',
    fetchPolicy: 'NO_FETCH_METADATA_ONLY',
    ingestStatus: 'REGISTERED',
    reviewStatus: 'REVIEW_REQUIRED',
    eligibilityStatus: 'REVIEW_REQUIRED_ONLY',
    confidence: 0.5,
    sourceTrace: 'Manual URL metadata: https://example.com/docs',
    batchId: 'batch-1',
    fileItemId: 'file-1',
    createdBy: 'frontend-user',
    createdAt: '2026-07-08T00:00:00Z',
    updatedAt: '2026-07-08T00:00:00Z',
    ...overrides
  }
}

function ingestionResponse(): ApiIngestionResponse {
  return {
    batch: batch({ id: 'batch-upload' }),
    files: [file({ id: 'file-upload' })],
    chunks: [chunk({ fileItemId: 'file-upload' })],
    parserRun: {
      runId: 'parser-1',
      batchId: 'batch-upload',
      adapterKey: 'mock-parser',
      status: 'SUCCEEDED',
      safeMessage: null
    },
    message: 'uploaded'
  }
}

function connectorDefinition(): ApiConnectorDefinition {
  return {
    id: 'connector-1',
    connectorKey: 'mock-local-fixture',
    name: 'Mock Local Fixture',
    connectorType: 'LOCAL_FIXTURE',
    status: 'AVAILABLE',
    version: 'v0',
    capabilitySummary: 'Local fixture connector only',
    configurationState: 'MOCK_CONFIGURED',
    reviewPolicy: 'REVIEW_REQUIRED'
  }
}

function connectorRun(overrides: Partial<ApiConnectorSyncRun> = {}): ApiConnectorSyncRun {
  return {
    runId: 'connector-run-1',
    jobId: 'connector-job-1',
    spaceId: 'space-1',
    connectorKey: 'mock-local-fixture',
    status: 'REVIEW_REQUIRED',
    itemCount: 1,
    reviewRequiredCount: 1,
    failedCount: 0,
    safeMessage: null,
    startedAt: '2026-07-08T00:00:00Z',
    completedAt: null,
    ...overrides
  }
}

function connectorItem(): ApiConnectorSyncItem {
  return {
    id: 'connector-item-1',
    runId: 'connector-created',
    externalId: 'source-1',
    title: 'Connector Source',
    itemStatus: 'REVIEW_REQUIRED',
    sourceReference: 'fixture/source.md',
    sourceTrace: { source: 'fixture/source.md' },
    provenance: { connector: 'mock-local-fixture' },
    confidence: 0.81,
    reviewEligible: true,
    safeErrorCategory: 'NONE',
    safeErrorMessage: null,
    outputArtifacts: [],
    discoveredAt: '2026-07-08T00:00:00Z'
  }
}
