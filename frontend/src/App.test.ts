import { mount } from '@vue/test-utils'
import App from './App.vue'
import { trustedAskRun } from './data/atlasMock'

describe('Atlas P0 full-stack productization shell', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('renders API-backed space detail and disables unconnected prototype actions', async () => {
    mockP0Api()
    const wrapper = await mountWorkbench()

    expect(wrapper.get('[data-testid="space-list"]').text()).toContain('IBM i Modernization')
    expect(wrapper.get('[data-testid="space-detail"]').text()).toContain('Mock discovery package')
    expect(wrapper.findAll('[data-testid="coming-soon"]').length).toBeGreaterThanOrEqual(2)
    expect(wrapper.get('[data-tab="graph"]').attributes('data-state')).toBe('ready')
  })

  it('drives sample batch, review, publish, downstream refresh, and Ask through API calls', async () => {
    const api = mockP0Api()
    const wrapper = await mountWorkbench()

    await wrapper.get('[data-testid="create-sample-batch"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="batch-list"]').text()).toContain('P0 Browser Batch')
    expect(wrapper.get('[data-testid="file-list"]').text()).toContain('REVIEW_REQUIRED')
    expect(wrapper.get('[data-testid="chunk-list"]').text()).toContain('chunk-p0')

    await wrapper.get('[data-testid="approve-file"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="file-list"]').text()).toContain('APPROVED')

    await wrapper.get('[data-testid="publish-file"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="wiki-pages"]').text()).toContain('PUBLISHED')

    await wrapper.get('[data-testid="refresh-graph-evidence"]').trigger('click')
    await flushAsync()
    expect(api.graphProjectionCreated).toBe(true)
    expect(api.vectorRunCreated).toBe(true)

    await wrapper.get('[data-testid="ask-submit"]').trigger('click')
    await flushAsync()
    const answer = wrapper.get('[data-testid="ask-answer"]').text()
    expect(answer).toContain('SUCCEEDED')
    expect(answer).toContain('REVIEW_REQUIRED')
    expect(answer).toContain('chunk-p0')
  })

  it('shows safe space loading errors without hiding coming-soon guardrails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({
        success: false,
        data: null,
        error: { code: 'INTERNAL_ERROR', message: 'API failed safely' },
        meta: null
      })
    } as Response)

    const wrapper = await mountWorkbench()

    expect(wrapper.text()).toContain('API failed safely')
    expect(wrapper.findAll('[data-testid="coming-soon"]').length).toBeGreaterThan(0)
  })

  it('maps trusted Ask mock state to review-required answer evidence', () => {
    expect(trustedAskRun.status).toBe('SUCCEEDED')
    expect(trustedAskRun.answerReviewStatus).toBe('REVIEW_REQUIRED')
    expect(trustedAskRun.reviewPolicy).toBe('INCLUDE_REVIEW_REQUIRED')
    expect(trustedAskRun.evidence[0].reviewStatus).toBe('APPROVED')
    expect(trustedAskRun.safeMessage).not.toContain('https://')
  })
})

async function mountWorkbench() {
  const wrapper = mount(App)
  await flushAsync()
  await wrapper.get('.workbench-entry').trigger('click')
  await flushAsync()
  return wrapper
}

function mockP0Api() {
  const state = {
    batchCreated: false,
    fileReviewStatus: 'REVIEW_REQUIRED',
    wikiPublished: false,
    graphProjectionCreated: false,
    vectorRunCreated: false
  }

  vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
    const url = String(input)
    const method = init?.method ?? 'GET'

    if (url.endsWith('/api/spaces')) {
      return jsonOk([
        {
          id: 'ibm-i-modernization',
          name: 'IBM i Modernization',
          description: 'Mock discovery package for modernization planning.',
          type: 'document',
          indexStrategy: 'rag',
          owner: 'Platform Team',
          status: 'REVIEW_REQUIRED',
          documentCount: 6,
          wikiPageCount: state.wikiPublished ? 1 : 0,
          reviewCount: 1,
          createdAt: '2026-06-01T09:00:00Z',
          updatedAt: '2026-06-20T14:30:00Z'
        }
      ])
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization')) {
      return jsonOk({
        id: 'ibm-i-modernization',
        name: 'IBM i Modernization',
        description: 'Mock discovery package for modernization planning.',
        type: 'document',
        indexStrategy: 'rag',
        owner: 'Platform Team',
        status: 'REVIEW_REQUIRED',
        documentCount: 6,
        wikiPageCount: state.wikiPublished ? 1 : 0,
        reviewCount: 1,
        createdAt: '2026-06-01T09:00:00Z',
        updatedAt: '2026-06-20T14:30:00Z'
      })
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/batches') && method === 'POST') {
      state.batchCreated = true
      return jsonOk(batch(), 201)
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/batches')) {
      return jsonOk(state.batchCreated ? [batch()] : [])
    }

    if (url.endsWith('/api/batches/batch-p0/files')) {
      return jsonOk(state.batchCreated ? [file(state.fileReviewStatus)] : [])
    }

    if (url.endsWith('/api/files/file-p0/chunks')) {
      return jsonOk([chunk()])
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/review-queues')) {
      return jsonOk({
        spaceId: 'ibm-i-modernization',
        queues: [
          {
            type: 'READY_TO_PUBLISH',
            count: state.fileReviewStatus === 'APPROVED' ? 1 : 0,
            publishBlocked: false,
            representativeItems: []
          },
          {
            type: 'LOW_CONFIDENCE',
            count: 0,
            publishBlocked: true,
            representativeItems: []
          }
        ]
      })
    }

    if (url.endsWith('/api/files/file-p0/reviews') && method === 'POST') {
      state.fileReviewStatus = 'APPROVED'
      return jsonOk(
        {
          id: 1,
          targetType: 'file',
          targetId: 'file-p0',
          action: 'APPROVE',
          reviewer: 'p0-browser',
          comment: 'Approved',
          affectedChunks: ['chunk-p0'],
          createdAt: '2026-07-03T00:00:00Z'
        },
        201
      )
    }

    if (url.endsWith('/api/files/file-p0/publish') && method === 'POST') {
      state.wikiPublished = true
      return jsonOk(wikiPage(), 201)
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/wiki-pages')) {
      return jsonOk(state.wikiPublished ? [wikiPage()] : [])
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph/projection-runs')) {
      state.graphProjectionCreated = true
      return jsonOk(
        { runId: 'graph-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED', summary: {} },
        201
      )
    }

    if (url.includes('/api/spaces/ibm-i-modernization/vector-runs')) {
      state.vectorRunCreated = true
      return jsonOk(
        { runId: 'vector-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED' },
        201
      )
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph/nodes/node-p0')) {
      return jsonOk(graphDetail())
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph')) {
      return jsonOk(graphView())
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/ask') && method === 'POST') {
      return jsonOk(askRun(), 201)
    }

    if (url.endsWith('/api/ask-runs/ask-p0')) {
      return jsonOk(askRun())
    }

    return jsonOk(null)
  })

  return state
}

function jsonOk(data: unknown, status = 200) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: async () => ({ success: true, data, error: null, meta: null })
  } as Response)
}

function batch() {
  return {
    id: 'batch-p0',
    spaceId: 'ibm-i-modernization',
    name: 'P0 Browser Batch',
    sourceKind: 'folder',
    owner: 'P0 Browser',
    uploadedAt: '2026-07-03T00:00:00Z',
    metrics: {
      totalFiles: 1,
      pdfConverted: 0,
      markdownGenerated: 1,
      reviewRequired: 1,
      failed: 0,
      unsupported: 0
    }
  }
}

function file(reviewStatus: string) {
  return {
    id: 'file-p0',
    batchId: 'batch-p0',
    sourcePath: 'samples/p0/productization.md',
    sourceType: 'pdf',
    status: 'MARKDOWN_GENERATED',
    confidence: 0.93,
    reviewStatus,
    pdfPath: null,
    markdownPath: 'generated/md/productization.md',
    assetsPath: null,
    errorMessage: null
  }
}

function chunk() {
  return {
    id: 'chunk-p0',
    fileItemId: 'file-p0',
    sourceFile: 'productization.md',
    page: 1,
    section: 'P0 Browser Evidence',
    confidence: 0.93,
    reviewStatus: 'APPROVED'
  }
}

function wikiPage() {
  return {
    id: 'wiki-file-p0',
    spaceId: 'ibm-i-modernization',
    title: 'P0 Wiki',
    markdownPath: 'generated/md/productization.md',
    sourceDocumentIds: ['file-p0'],
    confidence: 0.93,
    reviewStatus: 'PUBLISHED',
    owner: 'p0-browser',
    lastUpdated: '2026-07-03T00:00:00Z'
  }
}

function graphView() {
  return {
    spaceId: 'ibm-i-modernization',
    nodes: [
      {
        id: 'node-p0',
        label: 'P0 Browser Evidence',
        type: 'CONCEPT',
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        evidenceCount: 1
      }
    ],
    edges: [],
    counts: { nodes: 1, edges: 0, excluded: 0 }
  }
}

function graphDetail() {
  return {
    node: graphView().nodes[0],
    adjacentNodes: [],
    adjacentEdges: [],
    evidenceReferences: [
      {
        sourceChunkId: 'chunk-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: 'P0 Browser Evidence',
        confidence: 0.93,
        reviewStatus: 'APPROVED'
      }
    ]
  }
}

function askRun() {
  return {
    runId: 'ask-p0',
    spaceId: 'ibm-i-modernization',
    question: 'What evidence was published?',
    status: 'SUCCEEDED',
    reviewPolicy: 'APPROVED_ONLY',
    mode: 'mock',
    requestedBy: 'p0-browser',
    answer: 'Mock chat summary for the referenced Atlas evidence.',
    answerConfidence: 0.82,
    answerReviewStatus: 'REVIEW_REQUIRED',
    modelRunId: 'model-run-p0',
    safeMessage: 'Trusted ask completed.',
    evidence: [
      {
        evidenceId: 'ask-ev-p0',
        sourceChunkId: 'chunk-p0',
        fileItemId: 'file-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: 'P0 Browser Evidence',
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        vectorItemKey: 'ibm-i-modernization/chunk-p0',
        score: 0.91,
        createdAt: '2026-07-03T00:00:00Z'
      }
    ],
    createdAt: '2026-07-03T00:00:00Z',
    completedAt: '2026-07-03T00:00:01Z'
  }
}

async function flushAsync() {
  for (let index = 0; index < 6; index++) {
    await new Promise(resolve => setTimeout(resolve, 0))
  }
}
