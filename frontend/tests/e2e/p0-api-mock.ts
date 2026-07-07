import type { Page, Route } from '@playwright/test'

export const p0Section = 'P0 Browser Evidence'

interface P0MockState {
  batchCreated: boolean
  fileReviewStatus: 'REVIEW_REQUIRED' | 'APPROVED'
  wikiPublished: boolean
  graphProjectionCreated: boolean
  vectorRunCreated: boolean
}

type MockRole = 'VIEWER' | 'KNOWLEDGE_MANAGER' | 'SPACE_OWNER'

interface MockCurrentUser {
  role: MockRole
  capabilities: string[]
}

interface P0MockOptions {
  currentUser?: MockCurrentUser
}

export async function mockP0Api(page: Page, options: P0MockOptions = {}) {
  const state: P0MockState = {
    batchCreated: false,
    fileReviewStatus: 'REVIEW_REQUIRED',
    wikiPublished: false,
    graphProjectionCreated: false,
    vectorRunCreated: false
  }
  const currentUser = options.currentUser ?? ownerUser()

  await page.route('**/api/**', async route => {
    const request = route.request()
    const url = new URL(request.url())
    const path = url.pathname
    const method = request.method()

    if (path === '/api/auth/me') {
      return fulfill(route, authMe(currentUser))
    }

    if (path === '/api/spaces') {
      return fulfill(route, [space(state)])
    }

    if (path === '/api/spaces/ibm-i-modernization') {
      return fulfill(route, space(state))
    }

    if (path === '/api/spaces/ibm-i-modernization/batches' && method === 'POST') {
      if (!currentUser.capabilities.includes('CONTENT_WRITE')) {
        return fulfillError(route, 403, 'FORBIDDEN', 'Action is not allowed.')
      }
      state.batchCreated = true
      return fulfill(route, batch(), 201)
    }

    if (path === '/api/spaces/ibm-i-modernization/batches') {
      return fulfill(route, state.batchCreated ? [batch()] : [])
    }

    if (path === '/api/batches/batch-p0/files') {
      return fulfill(route, state.batchCreated ? [file(state.fileReviewStatus)] : [])
    }

    if (path === '/api/files/file-p0/chunks') {
      return fulfill(route, [chunk()])
    }

    if (path === '/api/spaces/ibm-i-modernization/review-queues') {
      return fulfill(route, reviewQueues(state))
    }

    if (path === '/api/files/file-p0/reviews' && method === 'POST') {
      state.fileReviewStatus = 'APPROVED'
      return fulfill(route, review(), 201)
    }

    if (path === '/api/files/file-p0/publish' && method === 'POST') {
      state.wikiPublished = true
      return fulfill(route, wikiPage(), 201)
    }

    if (path === '/api/spaces/ibm-i-modernization/wiki-pages') {
      return fulfill(route, state.wikiPublished ? [wikiPage()] : [])
    }

    if (path === '/api/spaces/ibm-i-modernization/wiki-page-issues') {
      return fulfill(route, state.wikiPublished ? [wikiIssue()] : [])
    }

    if (path === '/api/spaces/ibm-i-modernization/audit-events') {
      return fulfill(route, auditEvents())
    }

    if (path === '/api/spaces/ibm-i-modernization/graph/projection-runs') {
      state.graphProjectionCreated = true
      return fulfill(route, { runId: 'graph-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED', summary: { createdCount: 1 } }, 201)
    }

    if (path === '/api/spaces/ibm-i-modernization/vector-runs') {
      state.vectorRunCreated = true
      return fulfill(route, { runId: 'vector-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED' }, 201)
    }

    if (path === '/api/spaces/ibm-i-modernization/downstream-refresh' && method === 'POST') {
      state.graphProjectionCreated = true
      state.vectorRunCreated = true
      return fulfill(route, {
        graphRun: { runId: 'graph-run-p0', spaceId: 'ibm-i-modernization', adapterId: 'deterministic', scope: 'APPROVED_ONLY', status: 'SUCCEEDED', summary: { createdCount: 1 } },
        vectorRun: { runId: 'vector-run-p0', spaceId: 'ibm-i-modernization', batchId: 'batch-p0', adapterKey: 'mock-vector', operation: 'INDEX', status: 'SUCCEEDED' },
        message: 'Downstream evidence refreshed.'
      })
    }

    if (path === '/api/spaces/ibm-i-modernization/graph/nodes/node-p0') {
      return fulfill(route, graphDetail())
    }

    if (path === '/api/spaces/ibm-i-modernization/graph') {
      return fulfill(route, graphView())
    }

    if (path === '/api/spaces/ibm-i-modernization/ask' && method === 'POST') {
      return fulfill(route, askRun(), 201)
    }

    if (path === '/api/ask-runs/ask-p0') {
      return fulfill(route, askRun())
    }

    if (path === '/api/spaces/ibm-i-modernization/ask-sessions') {
      return fulfill(route, askSessions())
    }

    if (path === '/api/ask-sessions/ask-session-p0') {
      return fulfill(route, askSessionDetail())
    }

    if (path === '/api/model-adapters') {
      return fulfill(route, modelAdapters())
    }

    if (path === '/api/model-configurations/deepseek') {
      if (method === 'PUT') {
        return fulfill(route, modelConfiguration('CONFIGURED'))
      }
      if (method === 'DELETE') {
        return fulfill(route, modelConfiguration('MISSING'))
      }
      return fulfill(route, modelConfiguration('MISSING'))
    }

    return fulfill(route, null, 404, false)
  })

  return state
}

async function fulfill(route: Route, data: unknown, status = 200, success = true) {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify({
      success,
      data,
      error: success ? null : { code: 'NOT_FOUND', message: 'Mock route not found.' },
      meta: null
    })
  })
}

async function fulfillError(route: Route, status: number, code: string, message: string) {
  await route.fulfill({
    status,
    contentType: 'application/json',
    body: JSON.stringify({
      success: false,
      data: null,
      error: { code, message },
      meta: null
    })
  })
}

function space(state: P0MockState) {
  return {
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
}

function batch() {
  return {
    id: 'batch-p0',
    spaceId: 'ibm-i-modernization',
    name: 'P0 Browser Batch',
    sourceKind: 'folder',
    owner: 'P0 Browser',
    uploadedAt: '2026-07-03T00:00:00Z',
    metrics: { totalFiles: 1, pdfConverted: 0, markdownGenerated: 1, reviewRequired: 1, failed: 0, unsupported: 0 }
  }
}

function file(reviewStatus: P0MockState['fileReviewStatus']) {
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
    section: p0Section,
    confidence: 0.93,
    reviewStatus: 'APPROVED'
  }
}

function reviewQueues(state: P0MockState) {
  return {
    spaceId: 'ibm-i-modernization',
    queues: [
      { type: 'READY_TO_PUBLISH', count: state.fileReviewStatus === 'APPROVED' ? 1 : 0, publishBlocked: false, representativeItems: [] },
      { type: 'LOW_CONFIDENCE', count: 0, publishBlocked: true, representativeItems: [] }
    ]
  }
}

function review() {
  return {
    id: 1,
    targetType: 'file',
    targetId: 'file-p0',
    action: 'APPROVE',
    reviewer: 'p0-browser',
    comment: 'Approved',
    affectedChunks: ['chunk-p0'],
    createdAt: '2026-07-03T00:00:00Z'
  }
}

function wikiPage() {
  return {
    id: 'wiki-file-p0',
    spaceId: 'ibm-i-modernization',
    folderId: null,
    title: 'P0 Wiki',
    slug: 'p0-wiki',
    pageType: 'SOURCE_SUMMARY',
    markdownPath: 'generated/md/productization.md',
    sourceDocumentIds: ['file-p0'],
    aliases: ['P0 Evidence'],
    sourceRefs: [
      {
        type: 'FILE',
        id: 'file-p0',
        label: 'file-p0',
        locator: 'generated/md/productization.md'
      }
    ],
    chunkRefs: [
      {
        type: 'SOURCE_CHUNK',
        id: 'chunk-p0',
        label: 'source chunk',
        locator: 'page 1'
      }
    ],
    inLinks: [],
    outLinks: ['p0-browser-evidence'],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
    confidence: 0.93,
    reviewStatus: 'PUBLISHED',
    owner: 'p0-browser',
    lastUpdated: '2026-07-03T00:00:00Z'
  }
}

function wikiIssue() {
  return {
    id: 'wiki-issue-broken-link-001',
    spaceId: 'ibm-i-modernization',
    pageId: 'wiki-file-p0',
    issueType: 'BROKEN_LINK',
    severity: 'MEDIUM',
    status: 'OPEN',
    evidenceRefs: [
      {
        type: 'WIKI_PAGE',
        id: 'wiki-file-p0',
        label: 'p0-wiki',
        locator: null
      }
    ],
    message: 'Wiki link target does not exist in this Knowledge Space.',
    createdAt: '2026-07-05T00:00:00Z',
    resolvedAt: null
  }
}

function graphView() {
  return {
    spaceId: 'ibm-i-modernization',
    nodes: [
      { id: 'node-p0', label: p0Section, type: 'CONCEPT', reviewStatus: 'APPROVED', confidence: 0.93, evidenceCount: 1 }
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
        referenceType: 'SOURCE_CHUNK',
        sourceChunkId: 'chunk-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: p0Section,
        confidence: 0.93,
        reviewStatus: 'APPROVED'
      },
      {
        referenceType: 'WIKI_PAGE',
        wikiPageId: 'wiki-p0',
        label: p0Section,
        section: 'p0-browser-evidence',
        confidence: 0.93,
        reviewStatus: 'PUBLISHED'
      }
    ]
  }
}

function askRun() {
  return {
    runId: 'ask-p0',
    sessionId: 'ask-session-p0',
    sessionTitle: 'P0 browser evidence',
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
        citationId: 'ask-cite-p0',
        sourceChunkId: 'chunk-p0',
        fileItemId: 'file-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: p0Section,
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        vectorItemKey: 'ibm-i-modernization/chunk-p0',
        score: 0.91,
        evidenceLabel: 'productization.md page 1',
        sourceLocator: `page 1 / ${p0Section} / chunk chunk-p0`,
        citationStatus: 'ELIGIBLE',
        reviewEligible: true,
        excludedReason: null,
        createdAt: '2026-07-03T00:00:00Z'
      }
    ],
    createdAt: '2026-07-03T00:00:00Z',
    completedAt: '2026-07-03T00:00:01Z'
  }
}

function askSessions() {
  return [
    {
      sessionId: 'ask-session-p0',
      spaceId: 'ibm-i-modernization',
      title: 'P0 browser evidence',
      createdBy: 'p0-browser',
      runCount: 1,
      latestStatus: 'SUCCEEDED',
      latestAnswerReviewStatus: 'REVIEW_REQUIRED',
      createdAt: '2026-07-03T00:00:00Z',
      updatedAt: '2026-07-03T00:00:01Z'
    }
  ]
}

function askSessionDetail() {
  return {
    ...askSessions()[0],
    runs: [askRun()]
  }
}

function modelAdapters() {
  return [
    {
      adapterKey: 'mock-model',
      modelKey: 'deepseek-flash',
      displayName: 'DeepSeek Flash',
      providerFamily: 'built-in mock',
      modelType: 'CHAT',
      supportedOperations: ['CHAT'],
      defaultModel: true,
      status: 'AVAILABLE',
      contextLimit: 8192,
      maskedConfigSummary: {
        credential: 'mock',
        endpoint: 'not_configured',
        externalNetwork: 'disabled'
      }
    }
  ]
}

function modelConfiguration(credentialStatus = 'MISSING') {
  return {
    adapterKey: 'deepseek',
    provider: 'deepseek',
    modelKey: 'deepseek-chat',
    credentialStatus,
    endpointStatus: 'CONFIGURED',
    mode: credentialStatus === 'CONFIGURED' ? 'runtime' : 'missing',
    maskedConfigSummary: {
      provider: 'deepseek',
      credential: credentialStatus.toLowerCase().replace('_', '-'),
      endpoint: 'configured',
      externalNetwork: credentialStatus === 'MISSING' ? 'disabled' : 'enabled'
    }
  }
}

export function viewerUser(): MockCurrentUser {
  return {
    role: 'VIEWER',
    capabilities: ['CONTENT_READ', 'SPACE_READ']
  }
}

export function knowledgeManagerUser(): MockCurrentUser {
  return {
    role: 'KNOWLEDGE_MANAGER',
    capabilities: ['CONTENT_READ', 'CONTENT_WRITE', 'GOVERNANCE_READ', 'KNOWLEDGE_OPERATE', 'SPACE_READ']
  }
}

export function ownerUser(): MockCurrentUser {
  return {
    role: 'SPACE_OWNER',
    capabilities: [
      'CONTENT_READ',
      'CONTENT_WRITE',
      'GOVERNANCE_READ',
      'KNOWLEDGE_OPERATE',
      'MEMBER_MANAGE',
      'SETTINGS_MANAGE',
      'SPACE_MANAGE',
      'SPACE_READ'
    ]
  }
}

function authMe(currentUser: MockCurrentUser) {
  return {
    user: {
      id: 'frontend-demo',
      email: 'frontend-demo@example.test',
      displayName: 'Frontend Demo',
      status: 'ACTIVE',
      globalRoles: []
    },
    activeSpaceId: 'ibm-i-modernization',
    memberships: [
      {
        id: 'membership-frontend-ibmi',
        spaceId: 'ibm-i-modernization',
        spaceName: 'IBM i Modernization',
        role: currentUser.role,
        status: 'ACTIVE'
      }
    ],
    capabilities: currentUser.capabilities
  }
}

function auditEvents() {
  return [
    {
      id: 'audit-auth-denied',
      createdAt: '2026-07-06T00:00:00Z',
      actorUserId: 'mock-viewer',
      actorDisplay: 'Atlas Viewer',
      action: 'AUTH_GOVERNANCE_READ_DENIED',
      category: 'AUTH',
      result: 'DENIED',
      severity: 'SECURITY',
      spaceId: 'ibm-i-modernization',
      targetType: 'api_route',
      targetId: '/api/spaces/ibm-i-modernization/audit-events',
      requestId: null,
      safeSummary: 'Denied governance read request.',
      metadata: {
        capability: 'GOVERNANCE_READ'
      }
    }
  ]
}
