import { expect, test, type APIRequestContext } from '@playwright/test'

const apiBaseUrl = process.env.ATLAS_API_BASE_URL ?? 'http://127.0.0.1:18080'
const mockUser = process.env.VITE_ATLAS_MOCK_USER ?? 'frontend-demo'

test('second-layer browser UI completes the P0 full-stack productization loop', async ({
  page,
  request
}) => {
  const sectionPrefix = `P0 Browser Evidence`

  await page.goto('/')

  await expect(page.getByTestId('vue-api-space-status')).toContainText('API-backed metadata')
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-space-head')).toContainText('API-backed Space')

  await page.getByRole('button', { name: '文档' }).click()
  await page.getByTestId('vue-api-create-batch').click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('P0 Productization Sample')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('REVIEW_REQUIRED')
  await expect(page.getByTestId('vue-api-metadata')).toContainText(sectionPrefix)

  const chunkText = await page.getByTestId('vue-api-metadata').innerText()
  const chunkId = chunkText.match(/chunk-[a-zA-Z0-9-]+/)?.[0]
  expect(chunkId, 'source chunk id should be visible in the browser UI').toBeTruthy()

  await page.getByRole('button', { name: '处理中心' }).click()
  await page.getByTestId('vue-api-approve-file').click()
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('READY TO PUBLISH')

  await page.getByRole('button', { name: 'Wiki', exact: true }).click()
  await page.getByTestId('vue-api-publish-file').click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('PUBLISHED')

  const batchId = await latestBatchId(request)
  await request.post(`${apiBaseUrl}/api/spaces/ibm-i-modernization/graph/projection-runs`, {
    data: { scope: 'APPROVED_ONLY', adapterId: 'deterministic', dryRun: false },
    headers: graphWriteHeaders()
  })
  await request.post(`${apiBaseUrl}/api/spaces/ibm-i-modernization/vector-runs`, {
    data: {
      adapterKey: 'mock-vector',
      operation: 'INDEX',
      batchId,
      sourceChunkIds: [chunkId],
      reviewPolicy: 'APPROVED_ONLY',
      dimension: 3,
      requestedBy: 'second-layer-ui',
      mode: 'mock',
      items: [{ sourceChunkId: chunkId, vector: [0.1, 0.2, 0.3] }]
    },
    headers: requestHeaders()
  })

  await page.reload()
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '图谱', exact: true }).click()
  await page.getByTestId('vue-graph-search').fill('P0 Wiki')
  await page.getByTestId('vue-graph-node').filter({ hasText: 'P0 Wiki' }).first().click()
  await expect(page.getByTestId('vue-graph-detail')).toContainText(chunkId!)
  await expect(page.getByTestId('vue-graph-detail')).toContainText(sectionPrefix)
  await expect(page.getByTestId('vue-graph-detail')).toContainText('APPROVED')

  await page.getByRole('button', { name: /对话/ }).click()
  await page.getByTestId('vue-ask-question').fill(`What evidence exists for ${sectionPrefix}?`)
  await page.getByTestId('vue-api-ask-submit').click()
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('REVIEW_REQUIRED')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText(chunkId!)
})

async function latestBatchId(request: APIRequestContext) {
  const response = await request.get(`${apiBaseUrl}/api/spaces/ibm-i-modernization/batches`, {
    headers: requestHeaders()
  })
  expect(response.ok()).toBe(true)
  const envelope = await response.json()
  expect(envelope.success).toBe(true)
  return envelope.data[0].id as string
}

function graphWriteHeaders() {
  return {
    'X-Atlas-User': mockUser,
    'X-Atlas-Role': 'ADMIN'
  }
}

function requestHeaders() {
  return {
    'X-Atlas-User': mockUser
  }
}
