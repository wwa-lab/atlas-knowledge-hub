import { computed, ref } from 'vue'
import {
  acknowledgeDeadLetterEntry,
  approveFile as approveFileApi,
  getReviewQueues,
  listDeadLetterEntries,
  retryDeadLetterEntry
} from '@/api'
import { safeError } from '@/composables/apiErrors'
import type { ApiDeadLetterEntry, ApiFileItem, ApiReviewQueues } from '@/types'

export interface UseReviewQueueOptions {
  selectedFile: () => ApiFileItem | null
  selectedChunkIds: () => string[]
  selectedBatchId: () => string
  canOperateKnowledge: () => boolean
  refreshWorkflow: (batchId?: string, fileId?: string) => Promise<void>
  setWorkflowMessage: (message: string) => void
  setWorkflowError: (message: string) => void
}

export function useReviewQueue(options: UseReviewQueueOptions) {
  const reviewQueues = ref<ApiReviewQueues | null>(null)
  const deadLetterEntries = ref<ApiDeadLetterEntry[]>([])
  const selectedDeadLetterId = ref('')

  const isReviewing = ref(false)
  const isLoadingDeadLetters = ref(false)
  const isRetryingDeadLetter = ref(false)
  const isAcknowledgingDeadLetter = ref(false)
  const deadLetterError = ref('')

  const canApprove = computed(
    () =>
      options.canOperateKnowledge() &&
      Boolean(options.selectedFile()) &&
      options.selectedFile()?.reviewStatus !== 'APPROVED' &&
      options.selectedChunkIds().length > 0
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
  const selectedDeadLetterEntry = computed(
    () =>
      deadLetterEntries.value.find(entry => entry.id === selectedDeadLetterId.value) ??
      deadLetterEntries.value[0] ??
      null
  )

  async function loadReviewQueues(spaceId: string) {
    reviewQueues.value = await getReviewQueues(spaceId)
  }

  async function approveSelectedFile() {
    const file = options.selectedFile()
    if (!file || !options.canOperateKnowledge() || options.selectedChunkIds().length === 0) {
      return
    }
    isReviewing.value = true
    options.setWorkflowError('')
    try {
      await approveFileApi(file.id, options.selectedChunkIds())
      options.setWorkflowMessage(`Approved ${file.sourcePath}`)
      await options.refreshWorkflow(options.selectedBatchId(), file.id)
    } catch (error) {
      options.setWorkflowError(safeError(error, 'Review failed safely.'))
    } finally {
      isReviewing.value = false
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

  return {
    reviewQueues,
    deadLetterEntries,
    selectedDeadLetterId,
    isReviewing,
    isLoadingDeadLetters,
    isRetryingDeadLetter,
    isAcknowledgingDeadLetter,
    deadLetterError,
    canApprove,
    readyQueueCount,
    blockedQueueCount,
    reviewQueueCards,
    apiReadyToPublishCount,
    apiBlockedReviewCount,
    apiProcessingIssues,
    selectedDeadLetterEntry,
    loadReviewQueues,
    approveSelectedFile,
    loadDeadLetters,
    retrySelectedDeadLetter,
    acknowledgeSelectedDeadLetter
  }
}
