import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase E F G real Vue product path shows Wiki, Graph, and Trusted Ask evidence', async ({
  page
}) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  await page.goto('/')

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('IBM i Modernization Index')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('source_trace:')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('PUBLISHED')

  await page.getByRole('button', { name: /Source Trace Standard/ }).click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('Trace Coverage')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('confidence 0.91')
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-e-lm-wiki.png',
    fullPage: true
  })

  await page.getByRole('button', { name: '图谱' }).click()
  await expect(page.getByTestId('vue-product-graph')).toContainText('Knowledge Graph')
  await page.getByTestId('vue-graph-node').filter({ hasText: 'P0 Browser Evidence' }).click()
  await expect(page.getByTestId('vue-graph-detail')).toContainText('APPROVED')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('chunk-p0')
  await page.getByTestId('vue-graph-search').fill('P0')
  await expect(page.getByTestId('vue-product-graph')).toContainText('P0 Browser Evidence')
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-f-knowledge-graph.png',
    fullPage: true
  })

  await page.getByRole('button', { name: /对话/ }).click()
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('Evidence citations')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('REVIEW_REQUIRED')
  await page.getByTestId('vue-ask-mode').selectOption('refusal')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('NO_APPROVED_EVIDENCE')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText(
    'No approved evidence citations available'
  )
  await page.getByTestId('vue-ask-mode').selectOption('review-warning')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText(
    'Review-required evidence was explicitly included'
  )
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-g-trusted-ask.png',
    fullPage: true
  })
})
