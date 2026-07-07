import { expect, test, type Page } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Graph tab searches, filters, selects evidence-backed graph objects', async ({ page }) => {
  await mockP0Api(page)
  await mockGraphApi(page, 'ready')

  await page.goto('/')
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '图谱', exact: true }).click()

  const graph = page.getByTestId('vue-product-graph')
  await expect(graph).toContainText('Knowledge Graph')
  await expect(graph).toContainText('RPGLE modernization')

  await page.getByTestId('vue-graph-search').fill('RPGLE')
  await expect(page.getByTestId('vue-graph-node').filter({ hasText: 'RPGLE modernization' })).toBeVisible()

  await page.getByTestId('vue-graph-node').filter({ hasText: 'RPGLE modernization' }).click()
  await expect(page.getByTestId('vue-graph-detail')).toContainText('chunk-file-001-p12-b02')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('Wiki page')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('wiki-rpgle-modernization')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('confidence 0.93')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('APPROVED')

  await page.getByTestId('vue-graph-search').fill('')
  await expect(page.getByTestId('vue-product-graph')).toContainText('MENTIONS')
})

test('Graph tab exposes unauthorized and empty states', async ({ page }) => {
  await mockP0Api(page)
  await mockGraphApi(page, 'unauthorized')
  await page.goto('/')
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '图谱', exact: true }).click()

  await expect(page.getByTestId('vue-product-graph')).toContainText('Knowledge Graph')
  await expect(page.getByTestId('vue-product-graph')).not.toContainText('Using safe mock graph')

  await mockGraphApi(page, 'empty')
  await page.reload()
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '图谱', exact: true }).click()

  await expect(page.getByTestId('vue-product-graph')).toContainText('Knowledge Graph')
  await expect(page.getByTestId('vue-product-graph')).not.toContainText('Using safe mock graph')
})

async function mockGraphApi(page: Page, state: 'ready' | 'unauthorized' | 'empty') {
  await page.unroute('**/api/spaces/ibm-i-modernization/graph**').catch(() => undefined)
  await page.route('**/api/spaces/ibm-i-modernization/graph**', async route => {
    const url = route.request().url()
    if (state === 'unauthorized') {
      await route.fulfill({
        status: 403,
        contentType: 'application/json',
        body: JSON.stringify({
          success: false,
          data: null,
          error: { code: 'FORBIDDEN', message: 'Forbidden' },
          meta: null
        })
      })
      return
    }

    if (state === 'empty') {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            spaceId: 'ibm-i-modernization',
            nodes: [],
            edges: [],
            counts: { nodes: 0, edges: 0, excluded: 3 }
          },
          error: null,
          meta: null
        })
      })
      return
    }

    if (url.includes('/nodes/node-concept-rpgle')) {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            node: {
              id: 'node-concept-rpgle',
              label: 'RPGLE modernization',
              type: 'CONCEPT',
              reviewStatus: 'APPROVED',
              confidence: 0.93,
              evidenceCount: 1
            },
            adjacentNodes: [],
            adjacentEdges: [],
            evidenceReferences: [
              {
                referenceType: 'SOURCE_CHUNK',
                sourceChunkId: 'chunk-file-001-p12-b02',
                sourceFile: 'Graph/Modernization.md',
                page: 1,
                section: 'RPGLE modernization',
                confidence: 0.93,
                reviewStatus: 'APPROVED'
              },
              {
                referenceType: 'WIKI_PAGE',
                wikiPageId: 'wiki-rpgle-modernization',
                label: 'RPGLE modernization',
                section: 'rpgle-modernization',
                confidence: 0.93,
                reviewStatus: 'PUBLISHED'
              }
            ]
          },
          error: null,
          meta: null
        })
      })
      return
    }

    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: {
          spaceId: 'ibm-i-modernization',
          nodes: [
            {
              id: 'node-document-target-architecture',
              label: 'Target Architecture',
              type: 'DOCUMENT',
              reviewStatus: 'APPROVED',
              confidence: 0.91,
              evidenceCount: 1
            },
            {
              id: 'node-concept-rpgle',
              label: 'RPGLE modernization',
              type: 'CONCEPT',
              reviewStatus: 'APPROVED',
              confidence: 0.93,
              evidenceCount: 1
            }
          ],
          edges: [
            {
              id: 'edge-source-mentions-rpgle',
              sourceNodeId: 'node-document-target-architecture',
              targetNodeId: 'node-concept-rpgle',
              type: 'MENTIONS',
              reviewStatus: 'APPROVED',
              confidence: 0.93,
              evidenceCount: 1
            }
          ],
          counts: { nodes: 2, edges: 1, excluded: 0 }
        },
        error: null,
        meta: null
      })
    })
  })
}
