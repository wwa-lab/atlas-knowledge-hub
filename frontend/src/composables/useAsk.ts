import { computed, ref } from 'vue'
import {
  createAskRun,
  getAskQualityMetrics,
  getAskRun,
  getAskSession,
  listAskSessions
} from '@/api'
import { safeError } from '@/composables/apiErrors'
import { answerReuseHint, answerReviewReasonLine, formatMetricRatio } from '@/domain/viewModels'
import type { ProductAskAnswer, ProductAskMode } from '@/domain/viewModels'
import type {
  ApiAskRun,
  ApiAskSessionDetail,
  ApiAskSessionSummary,
  ApiRetrievalRunQualityMetrics
} from '@/types'

export interface UseAskOptions {
  selectedSpaceId: () => string
  selectedFileId: () => string
  canOperateKnowledge: () => boolean
}

export function useAsk(options: UseAskOptions) {
  const askRun = ref<ApiAskRun | null>(null)
  const askQualityMetrics = ref<ApiRetrievalRunQualityMetrics | null>(null)
  const askSessions = ref<ApiAskSessionSummary[]>([])
  const selectedAskSession = ref<ApiAskSessionDetail | null>(null)
  const askQuestion = ref('What evidence was published for the P0 browser flow?')
  const productAskQuestion = ref('哪些证据支持 IBM i 现代化知识空间可以发布到 Wiki？')
  const productAskMode = ref<ProductAskMode>('answered')

  const isAsking = ref(false)
  const askError = ref('')
  const askQualityError = ref('')
  const askSessionError = ref('')

  const canAsk = computed(
    () =>
      options.canOperateKnowledge() &&
      Boolean(options.selectedSpaceId() && askQuestion.value.trim())
  )

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
        evidence: [],
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
        options.selectedSpaceId(),
        askQuestion.value.trim(),
        options.selectedFileId(),
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
      askSessions.value = await listAskSessions(options.selectedSpaceId())
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

  return {
    askRun,
    askQualityMetrics,
    askSessions,
    selectedAskSession,
    askQuestion,
    productAskQuestion,
    productAskMode,
    isAsking,
    askError,
    askQualityError,
    askSessionError,
    canAsk,
    productAskAnswer,
    askQualityChips,
    submitAsk,
    refreshAskSessions,
    submitProductAsk
  }
}
