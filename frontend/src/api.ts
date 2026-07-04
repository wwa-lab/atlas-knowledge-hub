import type {
  ApiAskRun,
  ApiBatch,
  ApiEnvelope,
  ApiFileItem,
  ApiGraphNodeDetail,
  ApiGraphProjectionRun,
  ApiGraphView,
  ApiReview,
  ApiReviewQueues,
  ApiSourceChunk,
  ApiSpace,
  ApiVectorRun,
  ApiWikiPage
} from '@/types'

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code = 'API_ERROR'
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

const atlasApiBaseUrl = normalizeApiBaseUrl(import.meta.env.VITE_ATLAS_API_BASE_URL)

const graphReadHeaders = {
  'X-Atlas-User': 'frontend-demo',
  'X-Atlas-Role': 'VIEWER'
}

const graphAdminHeaders = {
  'X-Atlas-User': 'frontend-demo',
  'X-Atlas-Role': 'ADMIN'
}

export function apiBaseUrl() {
  return atlasApiBaseUrl
}

export async function listSpaces() {
  return atlasFetch<ApiSpace[]>('/api/spaces')
}

export async function getSpace(spaceId: string) {
  return atlasFetch<ApiSpace>(`/api/spaces/${spaceId}`)
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

export async function listWikiPages(spaceId: string) {
  return atlasFetch<ApiWikiPage[]>(`/api/spaces/${spaceId}/wiki-pages`)
}

export async function createGraphProjection(spaceId: string) {
  return atlasFetch<ApiGraphProjectionRun>(`/api/spaces/${spaceId}/graph/projection-runs`, {
    method: 'POST',
    headers: graphAdminHeaders,
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

export async function getGraph(spaceId: string, query = '') {
  const params = new URLSearchParams({ limit: '50', evidenceOnly: 'true' })
  if (query.trim()) {
    params.set('q', query.trim())
  }
  return atlasFetch<ApiGraphView>(`/api/spaces/${spaceId}/graph?${params}`, {
    headers: graphReadHeaders
  })
}

export async function getGraphNode(spaceId: string, nodeId: string) {
  return atlasFetch<ApiGraphNodeDetail>(`/api/spaces/${spaceId}/graph/nodes/${nodeId}`, {
    headers: graphReadHeaders
  })
}

export async function createAskRun(spaceId: string, question: string, fileId?: string) {
  return atlasFetch<ApiAskRun>(`/api/spaces/${spaceId}/ask`, {
    method: 'POST',
    body: {
      question,
      requestedBy: 'p0-browser',
      reviewPolicy: 'APPROVED_ONLY',
      limit: 3,
      mode: 'mock',
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

interface AtlasFetchOptions {
  method?: 'GET' | 'POST'
  body?: unknown
  headers?: Record<string, string>
}

async function atlasFetch<T>(path: string, options: AtlasFetchOptions = {}): Promise<T> {
  const response = await globalThis.fetch(apiUrl(path), {
    method: options.method ?? 'GET',
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  })
  const envelope = (await response.json().catch(() => null)) as ApiEnvelope<T> | null
  if (!response.ok || !envelope?.success || envelope.data == null) {
    const code = envelope?.error?.code ?? `HTTP_${response.status}`
    const message = envelope?.error?.message ?? `Atlas API returned ${response.status}`
    throw new ApiError(message, response.status, code)
  }
  return envelope.data
}

function apiUrl(path: string) {
  return atlasApiBaseUrl ? `${atlasApiBaseUrl}${path}` : path
}

function normalizeApiBaseUrl(value?: string) {
  const normalized = value?.trim()
  return normalized ? normalized.replace(/\/+$/, '') : ''
}
