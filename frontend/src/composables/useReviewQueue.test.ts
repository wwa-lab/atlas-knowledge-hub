import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  acknowledgeDeadLetterEntry,
  approveFile,
  getReviewQueues,
  listDeadLetterEntries,
  retryDeadLetterEntry
} from '@/api'
import { useReviewQueue } from '@/composables/useReviewQueue'
import type { ApiDeadLetterEntry, ApiFileItem, ApiReviewQueues } from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    acknowledgeDeadLetterEntry: vi.fn(),
    approveFile: vi.fn(),
    getReviewQueues: vi.fn(),
    listDeadLetterEntries: vi.fn(),
    retryDeadLetterEntry: vi.fn()
  }
})

const mockAcknowledgeDeadLetterEntry = vi.mocked(acknowledgeDeadLetterEntry)
const mockApproveFile = vi.mocked(approveFile)
const mockGetReviewQueues = vi.mocked(getReviewQueues)
const mockListDeadLetterEntries = vi.mocked(listDeadLetterEntries)
const mockRetryDeadLetterEntry = vi.mocked(retryDeadLetterEntry)

describe('useReviewQueue', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads review queues and derives blocked/ready processing summaries', async () => {
    mockGetReviewQueues.mockResolvedValue(reviewQueues())
    const state = useReviewQueue(testOptions())

    await state.loadReviewQueues('space-1')

    expect(state.readyQueueCount.value).toBe(2)
    expect(state.blockedQueueCount.value).toBe(3)
    expect(state.apiProcessingIssues.value[0]).toMatchObject({
      type: 'LOW_CONFIDENCE',
      status: 'blocks Wiki / Graph / Ask'
    })
  })

  it('approves the selected file and delegates workflow refresh explicitly', async () => {
    mockApproveFile.mockResolvedValue({
      id: 1,
      targetType: 'FILE',
      targetId: 'file-1',
      action: 'APPROVE',
      reviewer: 'p0-browser',
      comment: null,
      affectedChunks: ['chunk-1'],
      createdAt: '2026-07-08T00:00:00Z'
    })
    const refreshWorkflow = vi.fn<() => Promise<void>>().mockResolvedValue(undefined)
    const setWorkflowMessage = vi.fn()
    const state = useReviewQueue(
      testOptions({
        refreshWorkflow,
        setWorkflowMessage
      })
    )

    await state.approveSelectedFile()

    expect(mockApproveFile).toHaveBeenCalledWith('file-1', ['chunk-1'])
    expect(setWorkflowMessage).toHaveBeenCalledWith('Approved samples/mock.pdf')
    expect(refreshWorkflow).toHaveBeenCalledWith('batch-1', 'file-1')
  })

  it('loads and transitions selected dead-letter entries immutably', async () => {
    mockListDeadLetterEntries.mockResolvedValue([deadLetter({ id: 'dead-1', status: 'OPEN' })])
    mockRetryDeadLetterEntry.mockResolvedValue(deadLetter({ id: 'dead-1', status: 'RETRIED' }))
    mockAcknowledgeDeadLetterEntry.mockResolvedValue(
      deadLetter({ id: 'dead-1', status: 'ACKNOWLEDGED' })
    )
    const state = useReviewQueue(testOptions())

    await state.loadDeadLetters()
    await state.retrySelectedDeadLetter()
    expect(state.selectedDeadLetterEntry.value?.status).toBe('RETRIED')

    await state.acknowledgeSelectedDeadLetter()
    expect(state.selectedDeadLetterEntry.value?.status).toBe('ACKNOWLEDGED')
  })
})

function testOptions(overrides: Partial<Parameters<typeof useReviewQueue>[0]> = {}) {
  return {
    selectedFile: () => file(),
    selectedChunkIds: () => ['chunk-1'],
    selectedBatchId: () => 'batch-1',
    canOperateKnowledge: () => true,
    refreshWorkflow: vi.fn<() => Promise<void>>().mockResolvedValue(undefined),
    setWorkflowMessage: vi.fn(),
    setWorkflowError: vi.fn(),
    ...overrides
  }
}

function reviewQueues(): ApiReviewQueues {
  return {
    spaceId: 'space-1',
    queues: [
      {
        type: 'LOW_CONFIDENCE',
        count: 3,
        publishBlocked: true,
        representativeItems: [
          {
            fileId: 'file-1',
            status: 'LOW_CONFIDENCE',
            reviewStatus: 'REVIEW_REQUIRED',
            confidence: 0.62,
            hasSourceTrace: true
          }
        ]
      },
      {
        type: 'READY_TO_PUBLISH',
        count: 2,
        publishBlocked: false,
        representativeItems: []
      }
    ]
  }
}

function file(): ApiFileItem {
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
    errorMessage: null
  }
}

function deadLetter(overrides: Partial<ApiDeadLetterEntry> = {}): ApiDeadLetterEntry {
  return {
    id: 'dead-1',
    workerJobId: 'worker-1',
    status: 'OPEN',
    jobType: 'BATCH_INGEST',
    subjectType: 'FILE',
    subjectId: 'file-1',
    attemptSummary: 'failed terminal',
    safeErrorCode: 'SOURCE_UNREADABLE',
    safeErrorCategory: 'SOURCE_UNREADABLE',
    safeErrorMessage: 'Source unreadable.',
    sourceTrace: { file: 'mock.pdf' },
    reviewEligible: false,
    operatorActionBy: null,
    operatorActionAt: null,
    createdAt: '2026-07-08T00:00:00Z',
    job: {
      id: 'worker-1',
      jobType: 'BATCH_INGEST',
      subjectType: 'FILE',
      subjectId: 'file-1',
      status: 'DEAD_LETTERED',
      attemptCount: 1,
      maxAttempts: 1,
      retryDelaySeconds: null,
      nextRetryAt: null,
      sourceTrace: { file: 'mock.pdf' },
      reviewEligible: false,
      safeErrorCode: 'SOURCE_UNREADABLE',
      safeErrorCategory: 'SOURCE_UNREADABLE',
      safeErrorMessage: 'Source unreadable.',
      createdAt: '2026-07-08T00:00:00Z',
      updatedAt: '2026-07-08T00:00:00Z',
      attempts: []
    },
    attempts: [],
    ...overrides
  }
}
