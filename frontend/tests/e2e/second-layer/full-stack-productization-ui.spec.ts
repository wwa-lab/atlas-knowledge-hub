import { expect, test } from '@playwright/test'

test('second-layer browser UI completes the P0 full-stack productization loop', async ({ page }) => {
  const sectionPrefix = `P0 Browser Evidence`

  await page.goto('/')

  await expect(page.getByTestId('space-list')).toContainText('IBM i Modernization')
  await page.getByTestId('space-card').filter({ hasText: 'IBM i Modernization' }).click()
  await expect(page.getByTestId('space-detail')).toContainText('IBM i Modernization')

  await page.getByTestId('create-sample-batch').click()
  await expect(page.getByTestId('batch-list')).toContainText('P0 Productization Sample')
  await expect(page.getByTestId('file-list')).toContainText('REVIEW_REQUIRED')
  await expect(page.getByTestId('chunk-list')).toContainText(sectionPrefix)

  const chunkText = await page.getByTestId('chunk-list').innerText()
  const chunkId = chunkText.match(/chunk-[a-zA-Z0-9-]+/)?.[0]
  expect(chunkId, 'source chunk id should be visible in the browser UI').toBeTruthy()

  await page.getByTestId('approve-file').click()
  await expect(page.getByTestId('file-list')).toContainText('APPROVED')

  await page.getByTestId('publish-file').click()
  await expect(page.getByTestId('wiki-pages')).toContainText('PUBLISHED')

  await page.getByTestId('refresh-graph-evidence').click()
  await expect(page.getByText('Downstream evidence is ready.')).toBeVisible()

  const graph = page.locator('[data-tab="graph"]')
  await expect(graph).toHaveAttribute('data-state', 'ready')
  await graph.getByTestId('graph-search').fill(sectionPrefix)
  await expect(graph.locator('.graph-node-button', { hasText: sectionPrefix })).toBeVisible()
  await graph.locator('.graph-node-button', { hasText: sectionPrefix }).first().click()
  await expect(graph.getByTestId('graph-evidence-detail')).toContainText(chunkId!)
  await expect(graph.getByTestId('graph-evidence-detail')).toContainText('APPROVED')

  await page.getByTestId('ask-question').fill(`What evidence exists for ${sectionPrefix}?`)
  await page.getByTestId('ask-submit').click()
  await expect(page.getByTestId('ask-answer')).toContainText('REVIEW_REQUIRED')
  await expect(page.getByTestId('ask-answer')).toContainText(chunkId!)
})
