import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  createAskRun,
  getAskQualityMetrics,
  getAskRun,
  getAskSession,
  listAskSessions
} from '@/api'
import { useAsk } from '@/composables/useAsk'
import type {
  ApiAskRun,
  ApiAskSessionDetail,
  ApiAskSessionSummary,
  ApiRetrievalRunQualityMetrics
} from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    createAskRun: vi.fn(),
    getAskQualityMetrics: vi.fn(),
    getAskRun: vi.fn(),
    getAskSession: vi.fn(),
    listAskSessions: vi.fn()
  }
})

const mockCreateAskRun = vi.mocked(createAskRun)
const mockGetAskQualityMetrics = vi.mocked(getAskQualityMetrics)
const mockGetAskRun = vi.mocked(getAskRun)
const mockGetAskSession = vi.mocked(getAskSession)
const mockListAskSessions = vi.mocked(listAskSessions)

describe('useAsk', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('submits a trusted Ask run, loads quality metrics, and refreshes session history', async () => {
    mockCreateAskRun.mockResolvedValue(askRun({ runId: 'created-run' }))
    mockGetAskRun.mockResolvedValue(askRun())
    mockGetAskQualityMetrics.mockResolvedValue(qualityMetrics())
    mockListAskSessions.mockResolvedValue([sessionSummary()])
    mockGetAskSession.mockResolvedValue(sessionDetail())

    const state = useAsk({
      selectedSpaceId: () => 'space-1',
      selectedFileId: () => 'file-1',
      canOperateKnowledge: () => true
    })
    await state.submitAsk()

    expect(mockCreateAskRun).toHaveBeenCalledWith(
      'space-1',
      state.askQuestion.value,
      'file-1',
      undefined
    )
    expect(state.askRun.value?.answer).toContain('Published evidence')
    expect(state.askQualityChips.value).toEqual([
      'evidence 80%',
      'citations 90%',
      'confidence HIGH',
      'review ELIGIBLE',
      'evidence-backed'
    ])
    expect(state.selectedAskSession.value?.sessionId).toBe('session-1')
  })

  it('projects global chat answer modes without requiring an API run', () => {
    const state = useAsk({
      selectedSpaceId: () => 'space-1',
      selectedFileId: () => '',
      canOperateKnowledge: () => true
    })

    state.productAskMode.value = 'refusal'
    expect(state.productAskAnswer.value.status).toBe('NO_APPROVED_EVIDENCE')

    state.productAskMode.value = 'review-warning'
    expect(state.productAskAnswer.value.warning).toContain('Review-required evidence')
  })

  it('copies product Ask text into the API Ask state before submission', async () => {
    mockCreateAskRun.mockResolvedValue(askRun())
    mockGetAskRun.mockResolvedValue(askRun())
    mockGetAskQualityMetrics.mockRejectedValue(new Error('quality down'))
    mockListAskSessions.mockResolvedValue([])

    const state = useAsk({
      selectedSpaceId: () => 'space-1',
      selectedFileId: () => '',
      canOperateKnowledge: () => true
    })
    state.productAskQuestion.value = 'What supports publishing?'

    await state.submitProductAsk()

    expect(state.askQuestion.value).toBe('What supports publishing?')
    expect(state.askQualityError.value).toContain('quality down')
  })
})

function askRun(overrides: Partial<ApiAskRun> = {}): ApiAskRun {
  return {
    runId: 'ask-1',
    sessionId: 'session-1',
    sessionTitle: 'Session',
    spaceId: 'space-1',
    question: 'What evidence?',
    status: 'SUCCEEDED',
    reviewPolicy: 'APPROVED_ONLY',
    mode: 'mock',
    requestedBy: 'p0-browser',
    answer: 'Published evidence supports this answer.',
    answerConfidence: 0.91,
    answerReviewStatus: 'REVIEW_REQUIRED',
    answerReviewLabel: 'Review required',
    answerReviewReason: null,
    answerReviewedBy: null,
    answerReviewedAt: null,
    answerReusable: false,
    modelRunId: 'model-1',
    safeMessage: null,
    evidence: [
      {
        evidenceId: 'ev-1',
        citationId: 'cite-1',
        sourceChunkId: 'chunk-1',
        fileItemId: 'file-1',
        sourceFile: 'mock.md',
        page: 1,
        section: 'Evidence',
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        vectorItemKey: 'space-1/chunk-1',
        score: 0.88,
        evidenceLabel: 'Approved evidence',
        sourceLocator: 'page 1',
        citationStatus: 'ELIGIBLE',
        reviewEligible: true,
        excludedReason: null
      }
    ],
    ...overrides
  }
}

function qualityMetrics(): ApiRetrievalRunQualityMetrics {
  return {
    runId: 'ask-1',
    spaceId: 'space-1',
    status: 'SUCCEEDED',
    evidenceCoverage: {
      evidenceCount: 5,
      citedEvidenceCount: 4,
      coverageRatio: 0.8,
      missingEvidence: false
    },
    citationHealth: {
      evidenceCount: 5,
      healthyCitationCount: 4,
      uncitedEvidenceCount: 1,
      healthRatio: 0.9,
      unhealthy: false
    },
    confidence: {
      averageEvidenceConfidence: 0.91,
      answerConfidence: 0.9,
      band: 'HIGH'
    },
    reviewEligibility: {
      reviewEligible: true,
      status: 'ELIGIBLE',
      reasons: []
    },
    noEvidenceRefusal: false,
    safeDiagnostics: []
  }
}

function sessionSummary(): ApiAskSessionSummary {
  return {
    sessionId: 'session-1',
    spaceId: 'space-1',
    title: 'Session',
    createdBy: 'p0-browser',
    runCount: 1,
    latestStatus: 'SUCCEEDED',
    latestAnswerReviewStatus: 'REVIEW_REQUIRED',
    createdAt: '2026-07-08T00:00:00Z',
    updatedAt: '2026-07-08T00:00:00Z'
  }
}

function sessionDetail(): ApiAskSessionDetail {
  return {
    sessionId: 'session-1',
    spaceId: 'space-1',
    title: 'Session',
    createdBy: 'p0-browser',
    createdAt: '2026-07-08T00:00:00Z',
    updatedAt: '2026-07-08T00:00:00Z',
    runs: [askRun()]
  }
}
