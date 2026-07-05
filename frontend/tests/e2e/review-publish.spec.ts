import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('review and publish states use backend queues and Wiki metadata', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档' }).click()
  await page.getByTestId('vue-api-create-batch').click()

  await page.getByRole('button', { name: '处理中心' }).click()
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('READY TO PUBLISH')
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('0')

  await page.getByTestId('vue-api-approve-file').click()
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('READY TO PUBLISH')
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('1')

  await page.getByRole('button', { name: 'Wiki', exact: true }).click()
  await page.getByTestId('vue-api-publish-file').click()
  await expect(page.getByTestId('vue-wiki-page')).toContainText('P0 Wiki')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('PUBLISHED')
  await expect(page.getByTestId('vue-wiki-page')).toContainText('file-p0')
})
