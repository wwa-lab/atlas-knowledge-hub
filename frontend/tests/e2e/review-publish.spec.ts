import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('review and publish states use backend queues and Wiki metadata', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await page.getByTestId('create-sample-batch').click()
  await expect(page.getByTestId('review-queues')).toContainText('READY_TO_PUBLISH 0')
  await expect(page.getByTestId('publish-file')).toBeDisabled()

  await page.getByTestId('approve-file').click()
  await expect(page.getByTestId('review-queues')).toContainText('READY_TO_PUBLISH 1')

  await page.getByTestId('publish-file').click()
  await expect(page.getByTestId('wiki-pages')).toContainText('P0 Wiki')
  await expect(page.getByTestId('wiki-pages')).toContainText('PUBLISHED')
  await expect(page.getByTestId('wiki-pages')).toContainText('file-p0')
})
