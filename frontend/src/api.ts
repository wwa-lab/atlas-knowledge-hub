import type {
  ApiAnswerReviewStatus,
  ApiAskRun,
  ApiAskSessionDetail,
  ApiAskSessionSummary,
  ApiAuditEvent,
  ApiBatch,
  ApiEnvelope,
  ApiFileItem,
  ApiGraphNodeDetail,
  ApiGraphProjectionRun,
  ApiGraphView,
  ApiIngestionResponse,
  ApiMe,
  ApiModelCapability,
  ApiModelConfiguration,
  ApiReview,
  ApiReviewQueues,
  ApiSourceChunk,
  ApiSpace,
  ApiErrorBody,
  ApiDownstreamRefreshResponse,
  ApiVectorRun,
  ApiWikiPage,
  ApiWikiPageIssue,
  SafeErrorCode
} from '@/types'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code: SafeErrorCode = 'API_ERROR',
    readonly retryAfterSeconds: number | null = null,
    readonly correlationId: string | null = null
  ) {
    super(message)
    this.name = 'ApiError'
  }

  get safeCategory() {
    if (this.code === 'RATE_LIMITED') {
      return 'rate-limit'
    }
    if (this.code === 'AUTHENTICATION_REQUIRED' || this.code === 'PERMISSION_DENIED') {
      return 'access'
    }
    if (this.code === 'VALIDATION_FAILED') {
      return 'validation'
    }
    if (this.code === 'NOT_FOUND') {
      return 'not-found'
    }
    return 'system'
  }
}

const DEFAULT_DEV_API_BASE_URL = 'http://127.0.0.1:8080'

const atlasApiBaseUrl = normalizeApiBaseUrl(import.meta.env.VITE_ATLAS_API_BASE_URL)

const authHeaders = {
  'X-Atlas-User': import.meta.env.VITE_ATLAS_MOCK_USER || 'frontend-demo'
}

export function apiBaseUrl() {
  return atlasApiBaseUrl
}

export async function getCurrentUser() {
  return atlasFetch<ApiMe>('/api/auth/me')
}

export async function listSpaces() {
  return atlasFetch<ApiSpace[]>('/api/spaces')
}

export async function getSpace(spaceId: string) {
  return atlasFetch<ApiSpace>(`/api/spaces/${spaceId}`)
}

export async function createSpace(payload: {
  name: string
  description: string
  type: 'document' | 'faq'
  indexStrategy: 'rag' | 'wiki'
  owner: string
}) {
  return atlasFetch<ApiSpace>('/api/spaces', {
    method: 'POST',
    body: payload
  })
}

export async function listBatches(spaceId: string) {
  return atlasFetch<ApiBatch[]>(`/api/spaces/${spaceId}/batches`)
}

export async function createSampleBatch(spaceId: string, section: string) {
  const slug = section
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
  return atlasFetch<ApiBatch>(`/api/spaces/${spaceId}/batches`, {
    method: 'POST',
    body: {
      name: `P0 Productization Sample ${Date.now()}`,
      sourceKind: 'folder',
      owner: 'P0 Browser',
      files: [
        {
          sourcePath: `samples/p0/${slug}.md`,
          sourceType: 'pdf',
          status: 'MARKDOWN_GENERATED',
          confidence: 0.93,
          reviewStatus: 'REVIEW_REQUIRED',
          markdownPath: `generated/md/${slug}.md`,
          chunks: [
            {
              sourceFile: `${slug}.md`,
              page: 1,
              section,
              confidence: 0.93,
              reviewStatus: 'APPROVED'
            }
          ]
        }
      ]
    }
  })
}

export async function listFiles(batchId: string) {
  return atlasFetch<ApiFileItem[]>(`/api/batches/${batchId}/files`)
}

export async function getFile(fileId: string) {
  return atlasFetch<ApiFileItem>(`/api/files/${fileId}`)
}

export async function listChunks(fileId: string) {
  return atlasFetch<ApiSourceChunk[]>(`/api/files/${fileId}/chunks`)
}

export async function getReviewQueues(spaceId: string) {
  return atlasFetch<ApiReviewQueues>(`/api/spaces/${spaceId}/review-queues`)
}

export async function approveFile(fileId: string, affectedChunks: string[]) {
  return atlasFetch<ApiReview>(`/api/files/${fileId}/reviews`, {
    method: 'POST',
    body: {
      action: 'APPROVE',
      reviewer: 'p0-browser',
      comment: 'Approved for P0 browser productization flow.',
      affectedChunks
    }
  })
}

export async function publishFile(fileId: string, title: string) {
  return atlasFetch<ApiWikiPage>(`/api/files/${fileId}/publish`, {
    method: 'POST',
    body: {
      title,
      owner: 'p0-browser'
    }
  })
}

export async function listWikiPages(spaceId: string, includeDrafts = false) {
  const query = includeDrafts ? '?includeDrafts=true' : ''
  return atlasFetch<ApiWikiPage[]>(`/api/spaces/${spaceId}/wiki-pages${query}`)
}

export async function listWikiPageIssues(spaceId: string, status = 'OPEN') {
  const query = status ? `?status=${encodeURIComponent(status)}` : ''
  return atlasFetch<ApiWikiPageIssue[]>(`/api/spaces/${spaceId}/wiki-page-issues${query}`)
}

export async function listAuditEvents(spaceId: string, category?: string) {
  const params = new URLSearchParams()
  if (category) {
    params.set('category', category)
  }
  const query = params.toString()
  return atlasFetch<ApiAuditEvent[]>(
    `/api/spaces/${spaceId}/audit-events${query ? `?${query}` : ''}`
  )
}

export async function createGraphProjection(spaceId: string) {
  return atlasFetch<ApiGraphProjectionRun>(`/api/spaces/${spaceId}/graph/projection-runs`, {
    method: 'POST',
    body: {
      scope: 'APPROVED_ONLY',
      adapterId: 'deterministic',
      dryRun: false
    }
  })
}

export async function createVectorRun(spaceId: string, batchId: string, chunkIds: string[]) {
  return atlasFetch<ApiVectorRun>(`/api/spaces/${spaceId}/vector-runs`, {
    method: 'POST',
    body: {
      adapterKey: 'mock-vector',
      operation: 'INDEX',
      batchId,
      sourceChunkIds: chunkIds,
      reviewPolicy: 'APPROVED_ONLY',
      dimension: 3,
      requestedBy: 'p0-browser',
      mode: 'mock',
      items: chunkIds.map((sourceChunkId, index) => ({
        sourceChunkId,
        vector: [0.1 + index / 100, 0.2, 0.3]
      }))
    }
  })
}

export async function uploadDocuments(spaceId: string, files: File[], owner = 'frontend-user') {
  const formData = new FormData()
  for (const file of files) {
    formData.append('files', file)
  }
  formData.append('owner', owner)
  return atlasFetch<ApiIngestionResponse>(`/api/spaces/${spaceId}/ingestions`, {
    method: 'POST',
    body: formData
  })
}

export async function refreshDownstreamEvidenceApi(
  spaceId: string,
  batchId?: string,
  fileItemId?: string
) {
  return atlasFetch<ApiDownstreamRefreshResponse>(`/api/spaces/${spaceId}/downstream-refresh`, {
    method: 'POST',
    body: {
      batchId,
      fileItemId,
      requestedBy: 'p0-browser'
    }
  })
}

export async function getGraph(spaceId: string, query = '') {
  const params = new URLSearchParams({ limit: '50', evidenceOnly: 'true' })
  if (query.trim()) {
    params.set('q', query.trim())
  }
  return atlasFetch<ApiGraphView>(`/api/spaces/${spaceId}/graph?${params}`)
}

export async function getGraphNode(spaceId: string, nodeId: string) {
  return atlasFetch<ApiGraphNodeDetail>(`/api/spaces/${spaceId}/graph/nodes/${nodeId}`)
}

export async function createAskRun(
  spaceId: string,
  question: string,
  fileId?: string,
  sessionId?: string
) {
  return atlasFetch<ApiAskRun>(`/api/spaces/${spaceId}/ask`, {
    method: 'POST',
    body: {
      question,
      requestedBy: 'p0-browser',
      reviewPolicy: 'APPROVED_ONLY',
      limit: 3,
      mode: 'mock',
      sessionId,
      filters: fileId
        ? {
            fileItemIds: [fileId],
            sourceTypes: ['pdf']
          }
        : undefined
    }
  })
}

export async function getAskRun(runId: string) {
  return atlasFetch<ApiAskRun>(`/api/ask-runs/${runId}`)
}

export async function listAskSessions(spaceId: string) {
  return atlasFetch<ApiAskSessionSummary[]>(`/api/spaces/${spaceId}/ask-sessions`)
}

export async function getAskSession(sessionId: string) {
  return atlasFetch<ApiAskSessionDetail>(`/api/ask-sessions/${sessionId}`)
}

export async function reviewAskAnswer(
  spaceId: string,
  runId: string,
  payload: { status: ApiAnswerReviewStatus; reviewer: string; reason?: string }
) {
  return atlasFetch<ApiAskRun>(`/api/spaces/${spaceId}/ask/${runId}/review-actions`, {
    method: 'POST',
    body: payload
  })
}

export async function listModelAdapters() {
  return atlasFetch<ApiModelCapability[]>('/api/model-adapters')
}

export async function getDeepSeekConfiguration() {
  return atlasFetch<ApiModelConfiguration>('/api/model-configurations/deepseek')
}

export async function saveDeepSeekConfiguration(payload: {
  endpoint?: string
  modelName?: string
  apiKey?: string
}) {
  return atlasFetch<ApiModelConfiguration>('/api/model-configurations/deepseek', {
    method: 'PUT',
    body: {
      provider: 'deepseek',
      endpoint: payload.endpoint,
      modelName: payload.modelName,
      apiKey: payload.apiKey
    }
  })
}

export async function clearDeepSeekConfiguration() {
  return atlasFetch<ApiModelConfiguration>('/api/model-configurations/deepseek', {
    method: 'DELETE'
  })
}

interface AtlasFetchOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  headers?: Record<string, string>
}

async function atlasFetch<T>(path: string, options: AtlasFetchOptions = {}): Promise<T> {
  const isFormData = options.body instanceof FormData
  const requestBody = buildRequestBody(options.body)
  const response = await globalThis.fetch(apiUrl(path), {
    method: options.method ?? 'GET',
    headers: {
      ...(options.body && !isFormData ? { 'Content-Type': 'application/json' } : {}),
      ...authHeaders,
      ...options.headers
    },
    body: requestBody
  })
  const envelope = (await response.json().catch(() => null)) as ApiEnvelope<T> | null
  if (!response.ok || !envelope?.success || envelope.data == null) {
    const error = envelope?.error
    throw new ApiError(
      safeApiMessage(error, response.status),
      response.status,
      error?.code ?? `HTTP_${response.status}`,
      error?.retryAfterSeconds ?? null,
      error?.correlationId ?? null
    )
  }
  return envelope.data
}

function safeApiMessage(error: ApiErrorBody | null | undefined, status: number) {
  return error?.message ?? `Atlas API returned ${status}`
}

function buildRequestBody(body: unknown): BodyInit | undefined {
  if (body == null) {
    return undefined
  }
  if (body instanceof FormData) {
    return body
  }
  return JSON.stringify(body)
}

function apiUrl(path: string) {
  return atlasApiBaseUrl ? `${atlasApiBaseUrl}${path}` : path
}

function normalizeApiBaseUrl(value?: string) {
  const normalized = value?.trim()
  if (normalized) {
    return normalized.replace(/\/+$/, '')
  }
  return import.meta.env.DEV ? DEFAULT_DEV_API_BASE_URL : ''
}
