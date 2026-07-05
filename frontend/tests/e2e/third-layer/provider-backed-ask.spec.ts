import { expect, test, type APIRequestContext } from '@playwright/test'

const apiBaseUrl = process.env.ATLAS_API_BASE_URL ?? 'http://127.0.0.1:18082'
const spaceId = 'ibm-i-modernization'
const providerKey = normalizeProvider(process.env.ATLAS_MODEL_PROVIDER ?? 'deepseek')

test('third-layer provider-backed Ask answers with citations and API-backed graph evidence', async ({
  page,
  request
}) => {
  test.setTimeout(120_000)

  const timestamp = Date.now()
  const section = `Third Layer Evidence ${timestamp}`

  await expectApiOk(request, 'GET', `/api/spaces/${spaceId}`)

  const batchEnvelope = await expectApiOk(request, 'POST', `/api/spaces/${spaceId}/batches`, {
    name: `Third Layer Package ${timestamp}`,
    sourceKind: 'folder',
    owner: 'Third Layer E2E',
    files: [
      {
        sourcePath: `ThirdLayer/${timestamp}/provider-backed-flow.md`,
        sourceType: 'pdf',
        status: 'MARKDOWN_GENERATED',
        confidence: 0.94,
        reviewStatus: 'REVIEW_REQUIRED',
        markdownPath: `generated/md/third-layer-${timestamp}.md`,
        chunks: [
          {
            sourceFile: `provider-backed-flow-${timestamp}.md`,
            page: 1,
            section,
            confidence: 0.94,
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
    reviewer: 'third-layer-e2e',
    comment: 'Approved for local provider-backed E2E.',
    affectedChunks: [chunkId]
  })

  const wikiEnvelope = await expectApiOk(request, 'POST', `/api/files/${fileId}/publish`, {
    title: `Third Layer Wiki ${timestamp}`,
    owner: 'third-layer-e2e'
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
    requestedBy: 'third-layer-e2e',
    mode: 'mock',
    items: [{ sourceChunkId: chunkId, vector: [0.2, 0.3, 0.4] }]
  })

  const askEnvelope = await expectApiOk(request, 'POST', `/api/spaces/${spaceId}/ask`, {
    question: `What provider-backed evidence exists for ${section}?`,
    requestedBy: 'third-layer-e2e',
    reviewPolicy: 'APPROVED_ONLY',
    limit: 3,
    mode: 'configured',
    filters: {
      fileItemIds: [fileId],
      sourceTypes: ['pdf']
    }
  })
  expect(askEnvelope.data.status).toBe('SUCCEEDED')
  expect(askEnvelope.data.answer).toBeTruthy()
  expect(askEnvelope.data.answerReviewStatus).toBe('REVIEW_REQUIRED')
  expect(askEnvelope.data.evidence[0].sourceChunkId).toBe(chunkId)
  expect(askEnvelope.data.evidence[0].sourceFile).toContain('provider-backed-flow')

  const modelEnvelope = await expectApiOk(request, 'GET', `/api/model-runs/${askEnvelope.data.modelRunId}`)
  expect(modelEnvelope.data.adapterKey).toBe(providerKey)
  expect(modelEnvelope.data.mode).toBe('configured')
  expect(modelEnvelope.data.outputs[0].safeSummary).toBeTruthy()

  await page.goto('/')
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '图谱', exact: true }).click()

  const graph = page.getByTestId('vue-product-graph')
  await expect(graph).toContainText('Knowledge Graph')
  await expect(graph).not.toContainText('Using safe mock graph')

  await page.getByTestId('vue-graph-search').fill(section)
  await expect(page.getByTestId('vue-graph-node').filter({ hasText: section })).toBeVisible()
  await page.getByTestId('vue-graph-node').filter({ hasText: section }).click()
  await expect(page.getByTestId('vue-graph-detail')).toContainText(chunkId)
  await expect(page.getByTestId('vue-graph-detail')).toContainText('APPROVED')
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
    'X-Atlas-User': 'third-layer-e2e',
    'X-Atlas-Role': 'VIEWER'
  }
}

function graphWriteHeaders() {
  return {
    'X-Atlas-User': 'third-layer-e2e',
    'X-Atlas-Role': 'ADMIN'
  }
}

function normalizeProvider(provider: string) {
  return provider.trim().toLowerCase().replaceAll('_', '-')
}
