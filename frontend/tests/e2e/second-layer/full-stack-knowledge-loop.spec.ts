import { expect, test, type APIRequestContext } from '@playwright/test'

const apiBaseUrl = process.env.ATLAS_API_BASE_URL ?? 'http://127.0.0.1:18080'
const spaceId = 'ibm-i-modernization'

test('second-layer local stack publishes trusted knowledge, projects graph, and answers with citations', async ({
  page,
  request
}) => {
  const timestamp = Date.now()
  const section = `Second Layer Evidence ${timestamp}`

  await expectApiOk(request, 'GET', `/api/spaces/${spaceId}`)

  const batchEnvelope = await expectApiOk(request, 'POST', `/api/spaces/${spaceId}/batches`, {
    name: `Second Layer Package ${timestamp}`,
    sourceKind: 'folder',
    owner: 'Second Layer E2E',
    files: [
      {
        sourcePath: `SecondLayer/${timestamp}/trusted-flow.md`,
        sourceType: 'pdf',
        status: 'MARKDOWN_GENERATED',
        confidence: 0.93,
        reviewStatus: 'REVIEW_REQUIRED',
        markdownPath: `generated/md/second-layer-${timestamp}.md`,
        chunks: [
          {
            sourceFile: `trusted-flow-${timestamp}.md`,
            page: 1,
            section,
            confidence: 0.93,
            reviewStatus: 'APPROVED'
          }
        ]
      }
    ]
  })
  const batchId = batchEnvelope.data.id as string

  const filesEnvelope = await expectApiOk(request, 'GET', `/api/batches/${batchId}/files`)
  const fileId = filesEnvelope.data[0].id as string

  const chunksEnvelope = await expectApiOk(request, 'GET', `/api/files/${fileId}/chunks`)
  const chunkId = chunksEnvelope.data[0].id as string

  await expectApiOk(request, 'POST', `/api/files/${fileId}/reviews`, {
    action: 'APPROVE',
    reviewer: 'second-layer-e2e',
    comment: 'Approved for local second-layer E2E.',
    affectedChunks: [chunkId]
  })

  const wikiEnvelope = await expectApiOk(request, 'POST', `/api/files/${fileId}/publish`, {
    title: `Second Layer Wiki ${timestamp}`,
    owner: 'second-layer-e2e'
  })
  expect(wikiEnvelope.data.reviewStatus).toBe('PUBLISHED')
  expect(wikiEnvelope.data.sourceDocumentIds).toContain(fileId)

  const projectionEnvelope = await expectApiOk(
    request,
    'POST',
    `/api/spaces/${spaceId}/graph/projection-runs`,
    {
      scope: 'APPROVED_ONLY',
      adapterId: 'deterministic',
      dryRun: false
    },
    graphWriteHeaders()
  )
  expect(['SUCCEEDED', 'PARTIAL_FAILED']).toContain(projectionEnvelope.data.status)
  expect(projectionEnvelope.data.summary.createdCount).toBeGreaterThan(0)

  const graphEnvelope = await expectApiOk(
    request,
    'GET',
    `/api/spaces/${spaceId}/graph?q=${encodeURIComponent(section)}&evidenceOnly=true`,
    undefined,
    graphReadHeaders()
  )
  const graphNode = graphEnvelope.data.nodes.find(
    (node: { label: string; id: string }) => node.label === section
  )
  if (!graphNode) {
    throw new Error(`Projected graph node was not found for ${section}`)
  }

  const graphDetailEnvelope = await expectApiOk(
    request,
    'GET',
    `/api/spaces/${spaceId}/graph/nodes/${graphNode.id}`,
    undefined,
    graphReadHeaders()
  )
  expect(
    graphDetailEnvelope.data.evidenceReferences.map(
      (evidence: { sourceChunkId: string }) => evidence.sourceChunkId
    )
  ).toContain(chunkId)

  await expectApiOk(request, 'POST', `/api/spaces/${spaceId}/vector-runs`, {
    adapterKey: 'mock-vector',
    operation: 'INDEX',
    batchId,
    sourceChunkIds: [chunkId],
    reviewPolicy: 'APPROVED_ONLY',
    dimension: 3,
    requestedBy: 'second-layer-e2e',
    mode: 'mock',
    items: [{ sourceChunkId: chunkId, vector: [0.1, 0.2, 0.3] }]
  })

  const askEnvelope = await expectApiOk(request, 'POST', `/api/spaces/${spaceId}/ask`, {
    question: `What evidence exists for ${section}?`,
    requestedBy: 'second-layer-e2e',
    reviewPolicy: 'APPROVED_ONLY',
    limit: 3,
    mode: 'mock',
    filters: {
      fileItemIds: [fileId],
      sourceTypes: ['pdf']
    }
  })
  expect(askEnvelope.data.status).toBe('SUCCEEDED')
  expect(askEnvelope.data.answerReviewStatus).toBe('REVIEW_REQUIRED')
  expect(askEnvelope.data.evidence[0].sourceChunkId).toBe(chunkId)

  await page.goto('/')
  await expect(page.getByTestId('space-list')).toContainText('IBM i Modernization')
  await expect(page.getByTestId('coming-soon')).toHaveCount(3)
})

async function expectApiOk(
  request: APIRequestContext,
  method: 'GET' | 'POST',
  path: string,
  body?: unknown,
  headers?: Record<string, string>
) {
  const response =
    method === 'GET'
      ? await request.get(`${apiBaseUrl}${path}`, { headers })
      : await request.post(`${apiBaseUrl}${path}`, { data: body, headers })
  expect(response.ok(), `${method} ${path} returned ${response.status()}`).toBe(true)
  const envelope = await response.json()
  expect(envelope.success, `${method} ${path} envelope should be successful`).toBe(true)
  expect(envelope.data, `${method} ${path} should return data`).toBeTruthy()
  return envelope
}

function graphReadHeaders() {
  return {
    'X-Atlas-User': 'second-layer-e2e',
    'X-Atlas-Role': 'VIEWER'
  }
}

function graphWriteHeaders() {
  return {
    'X-Atlas-User': 'second-layer-e2e',
    'X-Atlas-Role': 'ADMIN'
  }
}
