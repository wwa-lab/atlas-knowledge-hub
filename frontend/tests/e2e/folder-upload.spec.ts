import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('metadata-only sample upload creates a real API batch and source trace view', async ({
  page
}) => {
  await mockP0Api(page)
  await page.goto('/')

  await expect(page.getByTestId('vue-api-space-status')).toContainText('API-backed metadata')
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档' }).click()

  await page.getByTestId('vue-api-create-batch').click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('P0 Browser Batch')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('samples/p0/productization.md')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('productization.md')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('confidence 0.93')
})
