import { expect, test } from '@playwright/test'
import { mockP0Api, p0Section } from './p0-api-mock'

test('full-stack-productization P0 loop is completed from browser controls', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await page.getByTestId('space-card').click()
  await expect(page.getByTestId('space-detail')).toContainText('IBM i Modernization')

  await page.getByTestId('create-sample-batch').click()
  await expect(page.getByTestId('batch-list')).toContainText('P0 Browser Batch')
  await expect(page.getByTestId('chunk-list')).toContainText('chunk-p0')

  await page.getByTestId('approve-file').click()
  await expect(page.getByTestId('file-list')).toContainText('APPROVED')

  await page.getByTestId('publish-file').click()
  await expect(page.getByTestId('wiki-pages')).toContainText('PUBLISHED')

  await page.getByTestId('refresh-graph-evidence').click()
  const graph = page.locator('[data-tab="graph"]')
  await graph.getByTestId('graph-search').fill(p0Section)
  await graph.locator('.graph-node-button', { hasText: p0Section }).click()
  await expect(graph.getByTestId('graph-evidence-detail')).toContainText('chunk-p0')
  await expect(graph.getByTestId('graph-evidence-detail')).toContainText('APPROVED')

  await page.getByTestId('ask-question').fill(`What evidence exists for ${p0Section}?`)
  await page.getByTestId('ask-submit').click()
  await expect(page.getByTestId('ask-answer')).toContainText('Mock chat summary')
  await expect(page.getByTestId('ask-answer')).toContainText('chunk-p0')
  await expect(page.getByTestId('ask-answer')).toContainText('REVIEW_REQUIRED')
})
