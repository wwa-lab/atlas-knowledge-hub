import { expect, test } from '@playwright/test'
import { mockP0Api, p0Section } from './p0-api-mock'

test('full-stack-productization P0 loop is completed from browser controls', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-space-head')).toContainText('IBM i Modernization')

  await page.getByRole('button', { name: '文档' }).click()
  await page.getByTestId('vue-api-create-batch').click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('chunk-p0')

  await page.getByRole('button', { name: '处理中心' }).click()
  await page.getByTestId('vue-api-approve-file').click()
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('READY TO PUBLISH')

  await page.getByRole('button', { name: 'Wiki', exact: true }).click()
  await page.getByTestId('vue-api-publish-file').click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('PUBLISHED')

  await page.getByRole('button', { name: '图谱', exact: true }).click()
  await expect(page.getByTestId('vue-product-graph')).toContainText(p0Section)
  await expect(page.getByTestId('vue-graph-detail')).toContainText('chunk-p0')
  await expect(page.getByTestId('vue-graph-detail')).toContainText('APPROVED')

  await page.getByRole('button', { name: /对话/ }).click()
  await page.getByTestId('vue-ask-question').fill(`What evidence exists for ${p0Section}?`)
  await page.getByTestId('vue-api-ask-submit').click()
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('Mock chat summary')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('chunk-p0')
  await expect(page.getByTestId('vue-trusted-ask-answer')).toContainText('REVIEW_REQUIRED')
})
