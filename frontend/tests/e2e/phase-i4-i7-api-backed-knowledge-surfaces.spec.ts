import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase I4-I7 real Vue product surfaces use API-backed Wiki, graph, Ask, and model metadata', async ({
  page
}) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  const apiRequests: string[] = []
  page.on('request', request => {
    const url = new URL(request.url())
    if (url.pathname.startsWith('/api/')) {
      apiRequests.push(`${request.method()} ${url.pathname}`)
    }
  })

  await page.goto('/')
  await expect(page.getByTestId('vue-api-model-status')).toHaveCount(0)
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()

  await page.getByRole('button', { name: '文档' }).click()
  await page.getByTestId('vue-api-create-batch').click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('chunk-p0')

  await page.getByRole('button', { name: '处理中心' }).click()
  await page.getByTestId('vue-api-approve-file').click()
  await page.getByRole('button', { name: 'Wiki', exact: true }).click()
  await page.getByTestId('vue-api-publish-file').click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('P0 Wiki')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('API-backed published metadata')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('p0-wiki')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('SOURCE_SUMMARY')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('PUBLISHED_FILE')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('chunk-p0')

  await page.getByRole('button', { name: '图谱', exact: true }).click()
  await expect(page.getByTestId('vue-product-graph')).toContainText('P0 Browser Evidence')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('chunk-p0')

  await page.getByRole('button', { name: /对话/ }).click()
  await page.getByTestId('vue-api-ask-submit').click()
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText(
    'API-backed trusted answer'
  )
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('chunk-p0')

  await page.getByRole('button', { name: /模型管理/ }).click()
  await expect(page.getByTestId('vue-api-model-status')).toContainText(
    'API-backed masked capabilities'
  )
  await expect(page.getByTestId('vue-model-manager')).toContainText('mock-model')

  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/spaces/ibm-i-modernization/wiki-pages']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/spaces/ibm-i-modernization/graph']))
  expect(apiRequests).toEqual(expect.arrayContaining(['POST /api/spaces/ibm-i-modernization/ask']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/ask-runs/ask-p0']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/model-adapters']))

  await page.screenshot({
    path: '../docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png',
    fullPage: true
  })
})
