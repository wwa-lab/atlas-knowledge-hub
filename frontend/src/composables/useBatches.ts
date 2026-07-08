import { computed, ref } from 'vue'
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
import { safeError } from '@/composables/apiErrors'
import type {
  ApiBatch,
  ApiConnectorDefinition,
  ApiConnectorSyncItem,
  ApiConnectorSyncRun,
  ApiFileItem,
  ApiManualUrlSource,
  ApiSourceChunk,
  ManualUrlFetchIntent
} from '@/types'

export interface UseBatchesOptions {
  selectedSpaceId: () => string
  canWriteContent: () => boolean
  refreshReviewState?: (spaceId: string) => Promise<void>
  refreshWikiState?: (spaceId: string) => Promise<void>
}

export function useBatches(options: UseBatchesOptions) {
  const batches = ref<ApiBatch[]>([])
  const selectedBatchId = ref('')
  const files = ref<ApiFileItem[]>([])
  const selectedFileId = ref('')
  const chunks = ref<ApiSourceChunk[]>([])
  const manualUrlSources = ref<ApiManualUrlSource[]>([])
  const connectorDefinitions = ref<ApiConnectorDefinition[]>([])
  const connectorSyncRun = ref<ApiConnectorSyncRun | null>(null)
  const connectorSyncItems = ref<ApiConnectorSyncItem[]>([])
  const selectedConnectorItemId = ref('')
  const manualUrlDraft = ref({
    url: 'https://example.com/reference/page',
    title: 'Vendor reference page',
    description: 'Sample-safe manual URL metadata seed.',
    fetchIntent: 'FETCH_LATER' as ManualUrlFetchIntent
  })

  const isCreatingBatch = ref(false)
  const isCreatingManualUrl = ref(false)
  const isLoadingConnectors = ref(false)
  const isStartingConnectorSync = ref(false)
  const workflowMessage = ref('')
  const workflowError = ref('')
  const manualUrlError = ref('')
  const connectorError = ref('')

  const selectedBatch = computed(
    () => batches.value.find(batch => batch.id === selectedBatchId.value) ?? null
  )
  const selectedFile = computed(
    () => files.value.find(file => file.id === selectedFileId.value) ?? null
  )
  const selectedChunkIds = computed(() => chunks.value.map(chunk => chunk.id))

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
  const selectedConnectorItem = computed(
    () =>
      connectorSyncItems.value.find(item => item.id === selectedConnectorItemId.value) ??
      connectorSyncItems.value[0] ??
      null
  )

  async function loadBatchContext(spaceId = options.selectedSpaceId()) {
    workflowError.value = ''
    try {
      const [batchList, urlSources] = await Promise.all([
        listBatches(spaceId),
        listManualUrlSources(spaceId)
      ])
      batches.value = batchList
      manualUrlSources.value = urlSources
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
    }
  }

  async function selectBatch(batchId: string) {
    selectedBatchId.value = batchId
    const fileList = await listFiles(batchId)
    files.value = fileList
    const preferred = fileList.find(file => file.reviewStatus !== 'PUBLISHED') ?? fileList[0]
    if (preferred) {
      await selectFile(preferred.id)
    } else {
      selectedFileId.value = ''
      chunks.value = []
    }
  }

  async function selectFile(fileId: string) {
    selectedFileId.value = fileId
    chunks.value = await listChunks(fileId)
  }

  async function createBatchFromBrowser() {
    if (!options.selectedSpaceId() || !options.canWriteContent()) {
      return
    }
    isCreatingBatch.value = true
    workflowError.value = ''
    workflowMessage.value = ''
    const section = `P0 Browser Evidence ${Date.now()}`
    try {
      const batch = await createSampleBatch(options.selectedSpaceId(), section)
      workflowMessage.value = `Sample batch created: ${batch.name}`
      await refreshWorkflow(batch.id)
    } catch (error) {
      workflowError.value = safeError(error, 'Sample batch creation failed safely.')
    } finally {
      isCreatingBatch.value = false
    }
  }

  async function handleDocumentUpload(event: { target: unknown }) {
    if (!options.selectedSpaceId() || !options.canWriteContent()) {
      return
    }
    const input = event.target as { files: ArrayLike<File> | null; value: string }
    const selectedFiles = Array.from(input.files ?? [])
    input.value = ''
    if (selectedFiles.length === 0) {
      return
    }
    isCreatingBatch.value = true
    workflowError.value = ''
    workflowMessage.value = ''
    try {
      const response = await uploadDocuments(options.selectedSpaceId(), selectedFiles, 'p0-browser')
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
    if (!options.selectedSpaceId() || !options.canWriteContent()) {
      return
    }
    isCreatingManualUrl.value = true
    manualUrlError.value = ''
    workflowMessage.value = ''
    try {
      const source = await createManualUrlSource(options.selectedSpaceId(), {
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
    if (!options.selectedSpaceId() || connectorDefinitions.value.length === 0) {
      return
    }
    isStartingConnectorSync.value = true
    connectorError.value = ''
    try {
      const connectorKey = connectorDefinitions.value[0].connectorKey
      const created = await startConnectorSync(options.selectedSpaceId(), connectorKey)
      connectorSyncRun.value = await getConnectorSyncRun(created.runId)
      connectorSyncItems.value = await listConnectorSyncItems(created.runId)
      selectedConnectorItemId.value = connectorSyncItems.value[0]?.id ?? ''
    } catch (error) {
      connectorError.value = safeError(error, 'Connector sync failed safely.')
    } finally {
      isStartingConnectorSync.value = false
    }
  }

  async function refreshWorkflow(batchId = selectedBatchId.value, fileId = selectedFileId.value) {
    const spaceId = options.selectedSpaceId()
    const [batchList, urlSources] = await Promise.all([
      listBatches(spaceId),
      listManualUrlSources(spaceId),
      options.refreshReviewState?.(spaceId) ?? Promise.resolve(),
      options.refreshWikiState?.(spaceId) ?? Promise.resolve()
    ]).then(([nextBatches, nextUrlSources]) => [nextBatches, nextUrlSources] as const)
    batches.value = batchList
    manualUrlSources.value = urlSources
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

  function setWorkflowMessage(message: string) {
    workflowMessage.value = message
  }

  function setWorkflowError(message: string) {
    workflowError.value = message
  }

  return {
    batches,
    selectedBatchId,
    files,
    selectedFileId,
    chunks,
    manualUrlSources,
    connectorDefinitions,
    connectorSyncRun,
    connectorSyncItems,
    selectedConnectorItemId,
    manualUrlDraft,
    isCreatingBatch,
    isCreatingManualUrl,
    isLoadingConnectors,
    isStartingConnectorSync,
    workflowMessage,
    workflowError,
    manualUrlError,
    connectorError,
    selectedBatch,
    selectedFile,
    selectedChunkIds,
    apiBatchMetrics,
    manualUrlReviewRequiredCount,
    selectedConnectorItem,
    loadBatchContext,
    selectBatch,
    selectFile,
    createBatchFromBrowser,
    handleDocumentUpload,
    submitManualUrlSource,
    loadConnectorDefinitions,
    startMockConnectorSync,
    refreshWorkflow,
    setWorkflowMessage,
    setWorkflowError
  }
}
